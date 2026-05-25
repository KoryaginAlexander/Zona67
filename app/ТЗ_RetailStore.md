# Техническое задание: Клиент-серверное мобильное приложение розничного магазина электроники

---

## 1. Общее описание проекта

Android-приложение для розничного магазина электроники (смартфоны, ноутбуки, аксессуары и т.д.) с серверной частью на Ktor. Две роли: **Покупатель** и **Администратор**. Покупатель может browsе каталог, набирать корзину (в том числе без авторизации), оформлять заказы и следить за их статусом. Администратор управляет каталогом, категориями и заказами.

### Структура репозитория

**Это два полностью независимых проекта** в одном репозитории. Они не знают друг о друге и никак не связаны на уровне файлов — только через HTTP.

```
retail-store/               ← git репозиторий
├── backend/                ← Kotlin/Ktor проект (открывать в IntelliJ IDEA)
├── android/                ← Android проект (открывать в Android Studio)
├── docker-compose.yml      ← только для локального запуска бэка
└── .env                    ← секреты для docker-compose
```

> **Важно:** Android-проект (`android/`) должен быть создан как отдельный самостоятельный проект в папке `android/`. Не вкладывать его внутрь `backend/` и не смешивать их файлы. Единственная связь между ними — `BASE_URL` в настройках Android-приложения.

---

## 2. Стек технологий

### Backend
| Компонент | Технология |
|-----------|-----------|
| Язык | Kotlin |
| Фреймворк | Ktor |
| База данных | PostgreSQL |
| ORM | Exposed (Kotlin) |
| Аутентификация | Firebase Auth (верификация ID Token) + собственный JWT |
| Хранение изображений | Firebase Storage |
| DI | Koin |
| Сериализация | kotlinx.serialization |
| Миграции БД | Flyway |
| Connection pool | HikariCP |

### Android (Client)
| Компонент | Технология |
|-----------|-----------|
| Язык | Kotlin |
| UI | Jetpack Compose |
| Архитектура | Clean Architecture (Presentation → Domain → Data) |
| State management | ViewModel + StateFlow |
| DI | Hilt |
| Сеть | Retrofit2 + OkHttp3 |
| Изображения | Coil |
| Навигация | Navigation Compose |
| Локальное хранилище | DataStore (токены) + Room (гостевая корзина) |

---

## 3. Архитектура системы

```
[Android App]
     │
     │  HTTPS + JWT
     ▼
[Ktor Server]
     │
     ├─ Firebase Admin SDK → верифицирует Firebase ID Token при логине
     ├─ Выдаёт собственный JWT (access + refresh)
     │
     └─ PostgreSQL
          └─ Exposed ORM + Flyway миграции
```

### Поток аутентификации
1. Клиент логинится через Firebase Auth (email/password) → получает Firebase ID Token.
2. Клиент отправляет Firebase ID Token на сервер `POST /auth/login`.
3. Сервер верифицирует токен через Firebase Admin SDK.
4. Сервер находит или создаёт пользователя в PostgreSQL.
5. Сервер возвращает **JWT access token** (15 мин) + **refresh token** (30 дней).
6. Все последующие запросы: `Authorization: Bearer <access_token>`.
7. `AuthInterceptor` на клиенте при 401 автоматически вызывает `POST /auth/refresh` и повторяет запрос.

### Поток гостевой корзины
```
Гость добавляет товары
        │
        ▼
  Room (локально на устройстве)
        │
  Пользователь логинится
        │
        ▼
  POST /cart/merge
  { items: [{ productId, quantity }, ...] }
        │
  Логика мёрджа на сервере:
  - Товар есть только в гостевой → добавить на сервер
  - Товар есть только на сервере → оставить
  - Товар есть в обоих → гостевое количество перезаписывает серверное
        │
        ▼
  Локальная (Room) корзина очищается
  Дальше используется только серверная корзина
```

---

## 4. Роли пользователей

| Роль | Описание |
|------|----------|
| `CUSTOMER` | Гостевой просмотр каталога, корзина без авторизации, регистрация/вход, оформление заказов, история заказов, wishlist, профиль |
| `ADMIN` | Всё что у CUSTOMER + управление категориями, CRUD товаров, управление остатками, просмотр всех заказов, смена статуса заказа, отмена заказа, просмотр профиля покупателя |

Роль хранится в PostgreSQL в таблице `users`. Пользователь с email из `ADMIN_EMAIL` env-переменной автоматически получает роль `ADMIN` при первом входе.

---

## 5. Функциональность по ролям

### Покупатель (CUSTOMER)

| # | Функция | Детали |
|---|---------|--------|
| 1 | Просмотр каталога | Без авторизации. Список товаров с пагинацией |
| 2 | Поиск товаров | По названию, бренду. Debounce 300ms |
| 3 | Фильтрация | По категории, бренду, ценовому диапазону |
| 4 | Сортировка | По цене (↑↓), по новизне |
| 5 | Карточка товара | Галерея фото, характеристики (RAM, Storage и т.д.), наличие на складе, описание |
| 6 | Гостевая корзина | Добавление товаров без авторизации (хранится в Room) |
| 7 | Мёрдж корзины | После входа гостевая корзина сливается с серверной |
| 8 | Корзина | Изменение количества, удаление позиции, очистка, badge с количеством на иконке |
| 9 | Оформление заказа | Адрес доставки, комментарий. Без оплаты — только фиксация заказа |
| 10 | История заказов | Список с датой, статусом, суммой |
| 11 | Детали заказа | Список товаров (snapshot), адрес, статус, дата |
| 12 | Wishlist | Добавить/убрать товар из избранного. Список избранного |
| 13 | Профиль | Имя, email (readonly), телефон, адрес доставки (сохраняется для следующего заказа) |
| 14 | Регистрация | Email + пароль + имя |
| 15 | Выход | Инвалидация refresh token на сервере |

### Администратор (ADMIN)

| # | Функция | Детали |
|---|---------|--------|
| 1 | Всё что у покупателя | — |
| 2 | Управление категориями | Создать, переименовать, удалить категорию (с изображением) |
| 3 | Список товаров (admin) | Видит все товары включая деактивированные |
| 4 | Создание товара | Категория, название, бренд, модель, описание, цена, остаток на складе, фото (Firebase Storage), характеристики (key-value) |
| 5 | Редактирование товара | Любое поле включая цену и остаток |
| 6 | Деактивация товара | Скрыть товар из каталога без удаления (`is_active = false`) |
| 7 | Управление остатком | Поле `stock` при создании/редактировании. Система не позволяет оформить заказ если `stock = 0`. При оформлении заказа stock уменьшается |
| 8 | Список всех заказов | С фильтром по статусу (PENDING / CONFIRMED / PROCESSING / SHIPPED / DELIVERED / CANCELLED) |
| 9 | Смена статуса заказа | Dropdown со статусами. Допустимые переходы проверяются на сервере |
| 10 | Отмена заказа | Специальный action → статус CANCELLED + возврат stock |
| 11 | Просмотр профиля покупателя | Имя, email, телефон — для связи по заказу |

---

## 6. База данных (PostgreSQL)

```sql
-- Пользователи
CREATE TABLE users (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid VARCHAR(128) UNIQUE NOT NULL,
    email        VARCHAR(255) UNIQUE NOT NULL,
    full_name    VARCHAR(255),
    phone        VARCHAR(20),
    address      TEXT,
    role         VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now()
);

-- Refresh токены
CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(512) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Категории товаров
CREATE TABLE categories (
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    slug      VARCHAR(100) UNIQUE NOT NULL,
    image_url VARCHAR(512)
);

-- Товары
CREATE TABLE products (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id INT NOT NULL REFERENCES categories(id),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    price       NUMERIC(12, 2) NOT NULL,
    stock       INT NOT NULL DEFAULT 0,
    brand       VARCHAR(100),
    model       VARCHAR(100),
    image_urls  TEXT[],
    is_active   BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- Характеристики товаров (key-value, гибко для электроники)
CREATE TABLE product_specs (
    id         SERIAL PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    spec_key   VARCHAR(100) NOT NULL,
    spec_value VARCHAR(255) NOT NULL
);

-- Серверная корзина (только для авторизованных)
CREATE TABLE cart_items (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity   INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(user_id, product_id)
);

-- Wishlist
CREATE TABLE wishlist_items (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(user_id, product_id)
);

-- Заказы
CREATE TABLE orders (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id),
    status           VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_amount     NUMERIC(12, 2) NOT NULL,
    delivery_address TEXT,
    comment          TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);
-- Статусы: PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
--          Любой статус → CANCELLED (только админ)

-- Позиции заказа (snapshot цены и названия на момент заказа)
CREATE TABLE order_items (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id      UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id    UUID NOT NULL REFERENCES products(id),
    product_name  VARCHAR(255) NOT NULL,
    product_price NUMERIC(12, 2) NOT NULL,
    quantity      INT NOT NULL
);
```

---

## 7. API (Ktor — REST)

Базовый URL: `/api/v1`

Защищённые эндпоинты требуют: `Authorization: Bearer <access_token>`

### 7.1 Аутентификация

| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| POST | `/auth/register` | Public | Регистрация (Firebase createUser → login) |
| POST | `/auth/login` | Public | Firebase ID Token → JWT |
| POST | `/auth/refresh` | Public | Обновить access token |
| POST | `/auth/logout` | Auth | Инвалидировать refresh token |

**POST /auth/login — Request:**
```json
{ "firebaseIdToken": "eyJhbGci..." }
```
**Response:**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "abc123...",
  "user": { "id": "uuid", "email": "user@mail.com", "fullName": "Иван", "role": "CUSTOMER" }
}
```

---

### 7.2 Пользователи

| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| GET | `/users/me` | Auth | Профиль текущего пользователя |
| PATCH | `/users/me` | Auth | Обновить имя, телефон, адрес |
| GET | `/users` | ADMIN | Список всех пользователей |
| GET | `/users/{id}` | ADMIN | Профиль конкретного пользователя |

---

### 7.3 Категории

| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| GET | `/categories` | Public | Все категории |
| POST | `/categories` | ADMIN | Создать |
| PUT | `/categories/{id}` | ADMIN | Обновить |
| DELETE | `/categories/{id}` | ADMIN | Удалить |

---

### 7.4 Товары

| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| GET | `/products` | Public | Список с пагинацией и фильтрами |
| GET | `/products/{id}` | Public | Детальная карточка |
| POST | `/products` | ADMIN | Создать товар |
| PUT | `/products/{id}` | ADMIN | Обновить товар |
| PATCH | `/products/{id}/deactivate` | ADMIN | Деактивировать (скрыть) |
| PATCH | `/products/{id}/activate` | ADMIN | Активировать |

**GET /products — Query параметры:**
```
?page=1&limit=20
&categoryId=1
&brand=Samsung
&minPrice=10000&maxPrice=100000
&search=iPhone
&sortBy=price_asc|price_desc|newest
&activeOnly=true   // для публичного: true; для админа: false (видит все)
```

---

### 7.5 Корзина

| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| GET | `/cart` | Auth | Серверная корзина |
| POST | `/cart/items` | Auth | Добавить товар |
| PATCH | `/cart/items/{productId}` | Auth | Изменить количество |
| DELETE | `/cart/items/{productId}` | Auth | Удалить позицию |
| DELETE | `/cart` | Auth | Очистить корзину |
| POST | `/cart/merge` | Auth | Смёрджить гостевую корзину с серверной |

**POST /cart/merge — Request:**
```json
{
  "items": [
    { "productId": "uuid", "quantity": 2 },
    { "productId": "uuid2", "quantity": 1 }
  ]
}
```
Логика на сервере: если товар уже есть в серверной корзине — гостевое количество перезаписывает серверное. После ответа клиент очищает Room.

---

### 7.6 Wishlist

| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| GET | `/wishlist` | Auth | Список избранного |
| POST | `/wishlist/{productId}` | Auth | Добавить в избранное |
| DELETE | `/wishlist/{productId}` | Auth | Убрать из избранного |

---

### 7.7 Заказы

| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| POST | `/orders` | Auth | Оформить заказ из корзины |
| GET | `/orders/my` | Auth | История заказов текущего пользователя |
| GET | `/orders/my/{id}` | Auth | Детали своего заказа |
| GET | `/orders` | ADMIN | Все заказы (с фильтром по статусу) |
| GET | `/orders/{id}` | ADMIN | Детали любого заказа |
| PATCH | `/orders/{id}/status` | ADMIN | Сменить статус |
| PATCH | `/orders/{id}/cancel` | ADMIN | Отменить заказ (→ CANCELLED + возврат stock) |

**POST /orders — Request:**
```json
{
  "deliveryAddress": "г. Москва, ул. Пушкина, д. 1",
  "comment": "Позвоните перед доставкой"
}
```
Сервер: берёт товары из корзины, проверяет `stock >= quantity` для каждой позиции, делает snapshot цен, уменьшает `stock`, очищает корзину пользователя.

**PATCH /orders/{id}/status — Request:**
```json
{ "status": "CONFIRMED" }
```

Допустимые переходы статусов (проверяются на сервере):
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
  ↓            ↓           ↓          ↓
CANCELLED  CANCELLED  CANCELLED  CANCELLED
```

---

## 8. Структура проекта

### Backend (Ktor)

```
backend/
├── src/main/kotlin/com/retailstore/
│   ├── Application.kt
│   ├── plugins/
│   │   ├── Routing.kt
│   │   ├── Security.kt          # JWT конфигурация
│   │   ├── Serialization.kt
│   │   └── Database.kt          # HikariCP + Exposed + Flyway
│   ├── di/
│   │   └── AppModule.kt         # Koin модули
│   ├── data/
│   │   ├── database/
│   │   │   ├── tables/          # Exposed Table объекты
│   │   │   └── entities/        # Exposed Entity классы
│   │   └── repository/
│   │       ├── UserRepositoryImpl.kt
│   │       ├── ProductRepositoryImpl.kt
│   │       ├── CartRepositoryImpl.kt
│   │       ├── WishlistRepositoryImpl.kt
│   │       └── OrderRepositoryImpl.kt
│   ├── domain/
│   │   ├── model/               # Доменные data class
│   │   ├── repository/          # Интерфейсы репозиториев
│   │   └── usecase/
│   │       ├── auth/
│   │       ├── product/
│   │       ├── cart/
│   │       ├── wishlist/
│   │       └── order/
│   ├── presentation/
│   │   ├── routes/
│   │   │   ├── AuthRoutes.kt
│   │   │   ├── UserRoutes.kt
│   │   │   ├── CategoryRoutes.kt
│   │   │   ├── ProductRoutes.kt
│   │   │   ├── CartRoutes.kt
│   │   │   ├── WishlistRoutes.kt
│   │   │   └── OrderRoutes.kt
│   │   └── dto/                 # Request/Response DTO
│   └── utils/
│       ├── JwtUtils.kt
│       └── FirebaseUtils.kt
├── src/main/resources/
│   ├── application.conf
│   └── db/migration/
│       ├── V1__create_users.sql
│       ├── V2__create_categories_products.sql
│       ├── V3__create_cart_wishlist.sql
│       └── V4__create_orders.sql
└── build.gradle.kts
```

### Android (Client)

```
app/src/main/java/com/retailstore/
├── di/
│   └── AppModule.kt
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   ├── AuthApi.kt
│   │   │   ├── ProductApi.kt
│   │   │   ├── CartApi.kt
│   │   │   ├── WishlistApi.kt
│   │   │   └── OrderApi.kt
│   │   ├── dto/
│   │   └── interceptor/
│   │       └── AuthInterceptor.kt   # Bearer + auto-refresh при 401
│   ├── local/
│   │   ├── TokenDataStore.kt        # DataStore: access/refresh токены
│   │   └── cart/
│   │       ├── GuestCartDao.kt      # Room DAO
│   │       ├── GuestCartEntity.kt   # Room Entity
│   │       └── AppDatabase.kt
│   └── repository/
│       ├── AuthRepositoryImpl.kt
│       ├── ProductRepositoryImpl.kt
│       ├── CartRepositoryImpl.kt    # объединяет Room + API
│       ├── WishlistRepositoryImpl.kt
│       └── OrderRepositoryImpl.kt
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
│       ├── auth/
│       │   ├── LoginUseCase.kt
│       │   ├── RegisterUseCase.kt
│       │   └── LogoutUseCase.kt
│       ├── product/
│       │   ├── GetProductsUseCase.kt
│       │   └── GetProductDetailUseCase.kt
│       ├── cart/
│       │   ├── GetCartUseCase.kt
│       │   ├── AddToCartUseCase.kt
│       │   ├── UpdateCartItemUseCase.kt
│       │   └── MergeGuestCartUseCase.kt
│       ├── wishlist/
│       │   ├── GetWishlistUseCase.kt
│       │   ├── AddToWishlistUseCase.kt
│       │   └── RemoveFromWishlistUseCase.kt
│       └── order/
│           ├── PlaceOrderUseCase.kt
│           └── GetOrdersUseCase.kt
└── presentation/
    ├── navigation/
    │   └── NavGraph.kt
    ├── screens/
    │   ├── auth/
    │   │   ├── LoginScreen.kt + LoginViewModel.kt
    │   │   └── RegisterScreen.kt + RegisterViewModel.kt
    │   ├── catalog/
    │   │   ├── CatalogScreen.kt + CatalogViewModel.kt
    │   │   └── ProductDetailScreen.kt + ProductDetailViewModel.kt
    │   ├── cart/
    │   │   ├── CartScreen.kt + CartViewModel.kt
    │   │   └── CheckoutScreen.kt + CheckoutViewModel.kt
    │   ├── wishlist/
    │   │   └── WishlistScreen.kt + WishlistViewModel.kt
    │   ├── orders/
    │   │   ├── OrdersScreen.kt + OrdersViewModel.kt
    │   │   └── OrderDetailScreen.kt
    │   ├── profile/
    │   │   └── ProfileScreen.kt + ProfileViewModel.kt
    │   └── admin/
    │       ├── AdminPanelScreen.kt          # точка входа из ProfileScreen
    │       ├── products/
    │       │   ├── AdminProductListScreen.kt
    │       │   └── AdminProductFormScreen.kt
    │       └── orders/
    │           ├── AdminOrderListScreen.kt
    │           └── AdminOrderDetailScreen.kt
    └── components/
        ├── ProductCard.kt
        ├── CartItemRow.kt
        ├── OrderStatusChip.kt
        ├── WishlistButton.kt
        └── StockBadge.kt
```

---

## 9. Навигация (Android)

BottomNavigation **одинаковый для всех** (4 вкладки):

```
Каталог | Корзина | Избранное | Профиль
```

Админские функции спрятаны внутри ProfileScreen и появляются только если `role == ADMIN`.

---

### Покупатель (CUSTOMER)

```
LoginScreen / RegisterScreen
        ↓ (после входа)
BottomNavigation:
├── Каталог
│   ├── CatalogScreen (поиск, фильтры, список)
│   └── ProductDetailScreen (галерея, характеристики, "В корзину", "В избранное")
├── Корзина  [badge с количеством]
│   ├── CartScreen
│   └── CheckoutScreen → OrderSuccessScreen
├── Избранное
│   └── WishlistScreen → ProductDetailScreen
└── Профиль
    ├── ProfileScreen
    │   ├── Редактирование имени, телефона, адреса
    │   └── Мои заказы → OrdersScreen → OrderDetailScreen
    └── Кнопка «Выйти»
```

> Гость видит каталог и может добавлять в корзину (Room). При нажатии «Оформить заказ» или «Избранное» → редирект на LoginScreen. После входа → MergeGuestCart → продолжение.

---

### Администратор (ADMIN)

```
BottomNavigation (те же 4 вкладки):
├── Каталог  (как у покупателя)
├── Корзина  (как у покупателя)
├── Избранное (как у покупателя)
└── Профиль
    ├── ProfileScreen
    │   ├── Редактирование имени, телефона, адреса
    │   ├── Мои заказы → OrdersScreen → OrderDetailScreen
    │   └── [Кнопка] "Админ-панель"  ← только у ADMIN
    │              ↓
    │         AdminPanelScreen
    │         ├── [Перейти] Управление товарами
    │         │     ├── AdminProductListScreen
    │         │     ├── AdminProductFormScreen (создание)
    │         │     └── AdminProductFormScreen (редактирование)
    │         └── [Перейти] Управление заказами
    │               ├── AdminOrderListScreen (фильтр по статусу)
    │               └── AdminOrderDetailScreen (смена статуса, отмена, контакты)
    └── Кнопка «Выйти»
```

**Защита на уровне сервера:** даже если пользователь каким-то образом попадёт на admin route — сервер вернёт 403, потому что в JWT нет claim `role=ADMIN`.

---

## 10. Описание экранов

### LoginScreen
- Email + пароль, кнопка «Войти»
- Ссылка «Нет аккаунта? Зарегистрироваться»
- Firebase Auth → ID Token → POST /auth/login → сохранить токены в DataStore
- После входа: вызвать MergeGuestCartUseCase, затем роутинг по role

### RegisterScreen
- Имя + Email + пароль
- Firebase Auth createUserWithEmailAndPassword → POST /auth/login

### CatalogScreen
- Chips категорий (горизонтальный скролл)
- SearchBar (debounce 300ms)
- Кнопка «Фильтры» → BottomSheet (бренд, цена RangeSlider)
- Dropdown сортировки
- LazyVerticalGrid карточек товаров
- Пагинация при достижении конца списка

### ProductDetailScreen
- HorizontalPager изображений
- Название, бренд, цена, остаток (`В наличии: 5 шт` / `Нет в наличии`)
- Описание
- Таблица характеристик (RAM, Storage, Display и т.д.)
- Кнопка ♡ (добавить в wishlist)
- Кнопка «Добавить в корзину» (задизейблена если stock = 0)

### CartScreen
- LazyColumn позиций: фото, название, цена × количество, кнопки +/−, удалить
- Итоговая сумма
- Badge на иконке BottomNav обновляется реактивно
- Кнопка «Оформить заказ»

### CheckoutScreen
- Поле адреса доставки (предзаполнено из профиля если есть)
- Поле комментария
- Список товаров (readonly)
- Итоговая сумма
- Кнопка «Подтвердить» → POST /orders → OrderSuccessScreen

### OrdersScreen
- Список: номер заказа, дата, статус (цветной chip), сумма
- Тап → OrderDetailScreen: позиции, адрес, статус, комментарий

### WishlistScreen
- Grid карточек избранных товаров
- Кнопка удалить на карточке
- Кнопка «В корзину» на карточке

### ProfileScreen
- Имя, email (readonly), телефон, адрес доставки
- Кнопка «Редактировать» → inline editing
- Кнопка «Выйти»

### AdminPanelScreen
- Доступен только если `role == ADMIN` (кнопка в ProfileScreen)
- Две карточки-кнопки: «Управление товарами» и «Управление заказами»
- Является точкой входа во весь admin flow

### AdminProductListScreen
- Список всех товаров (включая неактивные, помечены визуально)
- FAB «+» → AdminProductFormScreen (создание)
- Свайп или кнопки: редактировать, активировать/деактивировать

### AdminProductFormScreen
- Dropdown категории
- Поля: название, бренд, модель, описание, цена, остаток на складе
- Загрузка фото из галереи → Firebase Storage → сохранение URL
- Динамический список характеристик: [key] [value] [удалить], кнопка «+ Добавить характеристику»

### AdminOrderListScreen
- FilterChips по статусу (Все / Новые / Подтверждены / В доставке / Доставлены / Отменены)
- Карточка: номер, дата, покупатель (имя), сумма, статус

### AdminOrderDetailScreen
- Данные покупателя (имя, телефон для связи)
- Список позиций
- Адрес доставки, комментарий
- Dropdown смены статуса (только допустимые переходы)
- Кнопка «Отменить заказ» (с подтверждением)

---

## 11. Безопасность

- JWT access token живёт **15 минут**, refresh token — **30 дней**
- Роль `ADMIN` проверяется на сервере через claim в JWT
- Пароли не хранятся в PostgreSQL — только Firebase UID
- При отмене заказа сервер возвращает `stock` обратно (в транзакции)
- Оформление заказа: проверка `stock >= quantity` — в транзакции, чтобы не уйти в минус
- Env-переменные: `JWT_SECRET`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `ADMIN_EMAIL`, `FIREBASE_CREDENTIALS_PATH`

---

## 12. Обработка ошибок

### Сервер — единый формат:
```json
{
  "error": "OUT_OF_STOCK",
  "message": "Товар 'iPhone 15' недоступен в запрошенном количестве",
  "statusCode": 409
}
```

Коды: 400 (невалидный запрос), 401 (не авторизован), 403 (нет прав), 404 (не найден), 409 (конфликт — нет на складе), 500 (серверная ошибка).

### Клиент:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val code: Int?, val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```
- ViewModel выставляет `UiState(loading, data, error)`
- При ошибке — Snackbar с текстом
- При 401 + неудачном refresh → logout + навигация на LoginScreen

---

## 13. Конфигурация

### Backend `application.conf`
```hocon
ktor {
    deployment { port = 8080 }
    application { modules = [ com.retailstore.ApplicationKt.module ] }
}
database {
    url      = ${DB_URL}
    driver   = "org.postgresql.Driver"
    user     = ${DB_USER}
    password = ${DB_PASSWORD}
}
jwt {
    secret                = ${JWT_SECRET}
    issuer                = "retail-store"
    audience              = "retail-store-users"
    accessTokenTtlMinutes = 15
    refreshTokenTtlDays   = 30
}
firebase {
    credentialsPath = ${FIREBASE_CREDENTIALS_PATH}
}
admin {
    email = ${ADMIN_EMAIL}
}
```

### Android `local.properties`
```
BASE_URL=http://10.0.2.2:8080/api/v1/
```

---

## 14. Зависимости (Gradle)

### Backend
```kotlin
dependencies {
    implementation("io.ktor:ktor-server-core:2.3.x")
    implementation("io.ktor:ktor-server-netty:2.3.x")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.x")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.x")
    implementation("io.ktor:ktor-server-auth:2.3.x")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.x")
    implementation("io.ktor:ktor-server-status-pages:2.3.x")
    implementation("org.jetbrains.exposed:exposed-core:0.44.x")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.x")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.x")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.x")
    implementation("org.postgresql:postgresql:42.7.x")
    implementation("com.zaxxer:HikariCP:5.x")
    implementation("org.flywaydb:flyway-core:10.x")
    implementation("io.insert-koin:koin-ktor:3.5.x")
    implementation("com.google.firebase:firebase-admin:9.x")
}
```

### Android
```kotlin
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.x"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.x")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.x")
    implementation("com.google.dagger:hilt-android:2.51.x")
    kapt("com.google.dagger:hilt-compiler:2.51.x")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.x")
    implementation("com.squareup.retrofit2:retrofit:2.11.x")
    implementation("com.squareup.retrofit2:converter-gson:2.11.x")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.x")
    implementation(platform("com.google.firebase:firebase-bom:33.x"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("androidx.datastore:datastore-preferences:1.1.x")
    implementation("androidx.room:room-runtime:2.6.x")
    implementation("androidx.room:room-ktx:2.6.x")
    kapt("androidx.room:room-compiler:2.6.x")
    implementation("io.coil-kt:coil-compose:2.6.x")
}
```

---

## 15. Порядок разработки

### Фаза 1 — Backend core
- [ ] Ktor проект + Exposed + PostgreSQL + HikariCP
- [ ] Flyway миграции (все таблицы)
- [ ] Firebase Admin SDK инициализация
- [ ] `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`
- [ ] JWT middleware + role-based авторизация

### Фаза 2 — Backend бизнес-логика
- [ ] CRUD категорий
- [ ] CRUD товаров (с фильтрами, пагинацией, управлением stock)
- [ ] Корзина + `/cart/merge`
- [ ] Wishlist
- [ ] Заказы: оформление (транзакция: stock check + decrement), смена статуса, отмена (возврат stock)

### Фаза 3 — Android core
- [ ] Hilt + Retrofit + Navigation Compose настройка
- [ ] Room (GuestCart)
- [ ] DataStore (токены)
- [ ] Firebase Auth — LoginScreen + RegisterScreen
- [ ] AuthInterceptor (Bearer + auto-refresh)
- [ ] Domain layer: модели, интерфейсы, UseCase'ы

### Фаза 4 — Android экраны (Покупатель)
- [ ] CatalogScreen + фильтры
- [ ] ProductDetailScreen
- [ ] CartScreen (гостевая + серверная + мёрдж)
- [ ] CheckoutScreen + OrderSuccessScreen
- [ ] OrdersScreen + OrderDetailScreen
- [ ] WishlistScreen
- [ ] ProfileScreen

### Фаза 5 — Android экраны (Администратор)
- [ ] AdminProductListScreen
- [ ] AdminProductFormScreen (с загрузкой фото в Firebase Storage)
- [ ] AdminOrderListScreen
- [ ] AdminOrderDetailScreen

---

## 16. Запуск локально (Docker Compose)

### Структура файлов деплоя

```
project-root/
├── backend/
│   ├── Dockerfile
│   ├── src/...
│   └── build.gradle.kts
├── docker-compose.yml
└── .env
```

### `.env` (секреты, не коммитить в git)

```env
DB_URL=jdbc:postgresql://db:5432/retailstore
DB_USER=postgres
DB_PASSWORD=postgres
JWT_SECRET=super-secret-key-change-in-production
ADMIN_EMAIL=admin@store.com
FIREBASE_CREDENTIALS_PATH=/app/firebase-credentials.json
```

### `docker-compose.yml`

```yaml
version: '3.8'

services:

  db:
    image: postgres:16
    container_name: retailstore_db
    environment:
      POSTGRES_DB: retailstore
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER} -d retailstore"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    container_name: retailstore_backend
    env_file: .env
    ports:
      - "8080:8080"
    volumes:
      - ./firebase-credentials.json:/app/firebase-credentials.json:ro
    depends_on:
      db:
        condition: service_healthy
    restart: on-failure

volumes:
  postgres_data:
```

### `backend/Dockerfile`

```dockerfile
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Команды

```bash
# Первый запуск / после изменений в коде
docker-compose up --build

# Запуск без пересборки (БД уже поднята)
docker-compose up

# Остановить всё
docker-compose down

# Остановить и удалить данные БД (полный сброс)
docker-compose down -v

# Посмотреть логи бэка
docker-compose logs -f backend
```

### Подключение Android эмулятора к локальному серверу

В эмуляторе `localhost` — это сам эмулятор, не твой компьютер. Используй специальный IP:

```
# local.properties или BuildConfig
BASE_URL=http://10.0.2.2:8080/api/v1/
```

### Что нужно положить вручную

| Файл | Откуда взять |
|------|-------------|
| `firebase-credentials.json` | Firebase Console → Project Settings → Service Accounts → Generate new private key |
| `.env` | Скопировать пример выше и заполнить |

### Порядок первого запуска

```
1. Установить Docker Desktop (уже есть ✓)
2. Скачать firebase-credentials.json из Firebase Console
3. Положить его в корень проекта
4. Скопировать .env и заполнить ADMIN_EMAIL, JWT_SECRET
5. docker-compose up --build
6. Flyway автоматически создаст все таблицы при старте бэка
7. Запустить Android-приложение в эмуляторе
```

---

## 17. Что НЕ входит в MVP

- Оплата заказов
- Push-уведомления о смене статуса
- Отзывы и рейтинги товаров
- Промокоды и скидки
- iOS версия
- Аналитика / дашборд для админа
