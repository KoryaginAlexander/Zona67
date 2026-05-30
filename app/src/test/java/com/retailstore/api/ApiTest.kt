package com.retailstore.api

import com.retailstore.data.remote.api.*
import com.retailstore.data.remote.dto.*
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var productApi: ProductApi
    private lateinit var cartApi: CartApi
    private lateinit var orderApi: OrderApi
    private lateinit var authApi: AuthApi
    private lateinit var wishlistApi: WishlistApi
    private lateinit var reviewApi: ReviewApi
    private lateinit var imageApi: ImageApi

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        productApi  = retrofit.create(ProductApi::class.java)
        cartApi     = retrofit.create(CartApi::class.java)
        orderApi    = retrofit.create(OrderApi::class.java)
        authApi     = retrofit.create(AuthApi::class.java)
        wishlistApi = retrofit.create(WishlistApi::class.java)
        reviewApi   = retrofit.create(ReviewApi::class.java)
        imageApi    = retrofit.create(ImageApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // 1. loginSuccess
    @Test
    fun `loginSuccess — сервер возвращает 201 с accessToken и refreshToken`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{"accessToken":"access-abc","refreshToken":"refresh-xyz",
                   "user":{"id":"u-1","email":"user@test.com","fullName":"Test User",
                           "phone":null,"address":null,"role":"CUSTOMER"}}"""
            )
        )

        val response    = authApi.login(LoginRequest(firebaseIdToken = "valid-firebase-token"))
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(201, response.code())
        assertNotNull(response.body()?.accessToken)
        assertNotNull(response.body()?.refreshToken)
        assertEquals("access-abc", response.body()?.accessToken)
        assertEquals("refresh-xyz", response.body()?.refreshToken)
        assertEquals("POST", httpRequest.method)
        assertEquals("/auth/login", httpRequest.path)
    }

    // 2. loginWithInvalidToken
    @Test
    fun `loginWithInvalidToken — сервер возвращает 401 Unauthorized`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(401).setBody(
                """{"error":"UNAUTHORIZED","message":"Invalid or expired Firebase token","status":401}"""
            )
        )

        val response = authApi.login(LoginRequest(firebaseIdToken = "invalid-expired-token"))

        assertEquals(401, response.code())
        assertFalse(response.isSuccessful)
        assertNull(response.body())
    }

    // 3. refreshTokenSuccess
    @Test
    fun `refreshTokenSuccess — сервер возвращает 200 с новым accessToken`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"accessToken":"new-access-token-999"}"""
            )
        )

        val response    = authApi.refresh(RefreshRequest(refreshToken = "refresh-xyz"))
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(200, response.code())
        assertTrue(response.isSuccessful)
        assertEquals("new-access-token-999", response.body()?.get("accessToken"))
        assertEquals("POST", httpRequest.method)
        assertEquals("/auth/refresh", httpRequest.path)
    }

    // 4. getProductsSuccess
    @Test
    fun `getProductsSuccess — сервер возвращает 200 со списком товаров и метаданными пагинации`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"items":[{"id":"p-1","categoryId":1,"name":"iPhone 15","description":null,
                   "price":99990.0,"stock":5,"brand":"Apple","model":"15",
                   "imageUrls":[],"isActive":true,"specs":[],
                   "averageRating":4.8,"reviewCount":10}],
                   "total":1,"page":1,"limit":20}"""
            )
        )

        val response    = productApi.getProducts(page = 1, limit = 20)
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(200, response.code())
        assertTrue(response.isSuccessful)
        assertFalse(response.body()?.items.isNullOrEmpty())
        assertEquals(1, response.body()?.total)
        assertEquals(1, response.body()?.page)
        assertEquals(20, response.body()?.limit)
        assertEquals("GET", httpRequest.method)
        assertTrue(httpRequest.path!!.startsWith("/products"))
    }

    // 5. getProductsWithFilter
    @Test
    fun `getProductsWithFilter — сервер возвращает 200 с товарами по фильтру категории и цены`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"items":[{"id":"p-2","categoryId":1,"name":"Samsung S24","description":null,
                   "price":79990.0,"stock":10,"brand":"Samsung","model":"S24",
                   "imageUrls":[],"isActive":true,"specs":[],
                   "averageRating":4.5,"reviewCount":5}],
                   "total":1,"page":1,"limit":20}"""
            )
        )

        val response    = productApi.getProducts(categoryId = 1, minPrice = 100.0, maxPrice = 5000.0)
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(200, response.code())
        assertTrue(response.isSuccessful)
        val path = httpRequest.path!!
        assertTrue(path.contains("categoryId=1"))
        assertTrue(path.contains("minPrice=100"))
        assertTrue(path.contains("maxPrice=5000"))
        assertEquals("GET", httpRequest.method)
    }

    // 6. getProductByIdNotFound
    @Test
    fun `getProductByIdNotFound — сервер возвращает 404 для несуществующего UUID`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(404).setBody(
                """{"error":"NOT_FOUND","message":"Product not found","status":404}"""
            )
        )

        val response = productApi.getProductById("00000000-0000-0000-0000-000000000000")

        assertEquals(404, response.code())
        assertFalse(response.isSuccessful)
        assertNull(response.body())
    }

    // 7. addToCartSuccess
    @Test
    fun `addToCartSuccess — авторизованный пользователь добавляет товар, сервер возвращает 201`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{"id":"ci-1","productId":"p-1","productName":"iPhone 15",
                   "productPrice":99990.0,"productImageUrl":null,"stock":5,"quantity":1}"""
            )
        )

        val response    = cartApi.addItem(AddCartItemRequest(productId = "p-1", quantity = 1))
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(201, response.code())
        assertTrue(response.isSuccessful)
        assertEquals("p-1", response.body()?.productId)
        assertEquals(1, response.body()?.quantity)
        assertEquals("POST", httpRequest.method)
        assertEquals("/cart/items", httpRequest.path)
    }

    // 8. addDuplicateToCart
    @Test
    fun `addDuplicateToCart — повторное добавление возвращает 200 с увеличенным количеством`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"id":"ci-1","productId":"p-1","productName":"iPhone 15",
                   "productPrice":99990.0,"productImageUrl":null,"stock":5,"quantity":2}"""
            )
        )

        val response    = cartApi.addItem(AddCartItemRequest(productId = "p-1", quantity = 1))
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(200, response.code())
        assertTrue(response.isSuccessful)
        assertEquals(2, response.body()?.quantity)
        assertEquals("POST", httpRequest.method)
        assertEquals("/cart/items", httpRequest.path)
    }

    // 9. mergeGuestCart
    @Test
    fun `mergeGuestCart — после входа гостевая корзина сливается с серверной, сервер возвращает 200`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"items":[{"id":"ci-1","productId":"p-1","productName":"iPhone 15",
                   "productPrice":99990.0,"productImageUrl":null,"stock":5,"quantity":2},
                   {"id":"ci-2","productId":"p-2","productName":"AirPods Pro",
                   "productPrice":29990.0,"productImageUrl":null,"stock":10,"quantity":1}],
                   "total":229970.0}"""
            )
        )

        val request     = MergeCartRequest(items = listOf(MergeItemDto("p-1", 2), MergeItemDto("p-2", 1)))
        val response    = cartApi.mergeCart(request)
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(200, response.code())
        assertTrue(response.isSuccessful)
        assertEquals(2, response.body()?.items?.size)
        assertEquals(229970.0, response.body()?.total ?: 0.0, 0.01)
        assertEquals("POST", httpRequest.method)
        assertEquals("/cart/merge", httpRequest.path)
        val body = httpRequest.body.readUtf8()
        assertTrue(body.contains("p-1"))
        assertTrue(body.contains("p-2"))
    }

    // 10. createOrderSuccess
    @Test
    fun `createOrderSuccess — заказ создаётся со статусом PENDING, сервер возвращает 201`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{"id":"ord-1","userId":"u-1","userEmail":"user@test.com","status":"PENDING",
                   "totalAmount":99990.0,"deliveryAddress":"ул. Ленина, 1","comment":null,
                   "items":[{"id":"oi-1","productId":"p-1","productName":"iPhone 15",
                             "productPrice":99990.0,"quantity":1}],
                   "createdAt":"2026-05-29T10:00:00","updatedAt":"2026-05-29T10:00:00"}"""
            )
        )

        val response    = orderApi.placeOrder(PlaceOrderRequest(deliveryAddress = "ул. Ленина, 1", comment = null))
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(201, response.code())
        assertTrue(response.isSuccessful)
        assertEquals("PENDING", response.body()?.status)
        assertEquals("ord-1", response.body()?.id)
        assertFalse(response.body()?.items.isNullOrEmpty())
        assertEquals("POST", httpRequest.method)
        assertEquals("/orders", httpRequest.path)
    }

    // 11. createOrderEmptyCart
    @Test
    fun `createOrderEmptyCart — создание заказа с пустой корзиной возвращает 400`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(400).setBody(
                """{"error":"BAD_REQUEST","message":"Cart is empty","status":400}"""
            )
        )

        val response = orderApi.placeOrder(PlaceOrderRequest(deliveryAddress = "ул. Ленина, 1", comment = null))

        assertEquals(400, response.code())
        assertFalse(response.isSuccessful)
        assertNull(response.body())
    }

    // 12. updateOrderStatusAdmin
    @Test
    fun `updateOrderStatusAdmin — ADMIN меняет статус заказа, сервер возвращает 200`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"id":"ord-1","userId":"u-1","userEmail":"user@test.com","status":"CONFIRMED",
                   "totalAmount":99990.0,"deliveryAddress":"ул. Ленина, 1","comment":null,
                   "items":[],"createdAt":"2026-05-29T10:00:00","updatedAt":"2026-05-29T11:00:00"}"""
            )
        )

        val response    = orderApi.updateOrderStatus("ord-1", UpdateStatusRequest(status = "CONFIRMED"))
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(200, response.code())
        assertTrue(response.isSuccessful)
        assertEquals("CONFIRMED", response.body()?.status)
        assertEquals("PATCH", httpRequest.method)
        assertEquals("/orders/ord-1/status", httpRequest.path)
        assertTrue(httpRequest.body.readUtf8().contains("CONFIRMED"))
    }

    // 13. updateOrderStatusCustomer
    @Test
    fun `updateOrderStatusCustomer — CUSTOMER пытается изменить статус, сервер возвращает 403`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(403).setBody(
                """{"error":"FORBIDDEN","message":"Access denied","status":403}"""
            )
        )

        val response = orderApi.updateOrderStatus("ord-1", UpdateStatusRequest(status = "CONFIRMED"))

        assertEquals(403, response.code())
        assertFalse(response.isSuccessful)
        assertNull(response.body())
    }

    // 14. addReviewSuccess
    @Test
    fun `addReviewSuccess — авторизованный пользователь оставляет отзыв, сервер возвращает 201`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{"id":"r-1","productId":"p-1","userId":"u-1","userName":"Алексей",
                   "rating":5,"comment":"Отличный товар!","createdAt":"2026-05-29T10:00:00"}"""
            )
        )

        val response    = reviewApi.addReview("p-1", CreateReviewRequest(rating = 5, comment = "Отличный товар!"))
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(201, response.code())
        assertTrue(response.isSuccessful)
        assertEquals(5, response.body()?.rating)
        assertEquals("r-1", response.body()?.id)
        assertEquals("POST", httpRequest.method)
        assertEquals("/products/p-1/reviews", httpRequest.path)
        assertTrue(httpRequest.body.readUtf8().contains("Отличный товар"))
    }

    // 15. addDuplicateReview
    @Test
    fun `addDuplicateReview — повторный отзыв на тот же товар возвращает 409 Conflict`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(409).setBody(
                """{"error":"CONFLICT","message":"Review already exists for this product","status":409}"""
            )
        )

        val response = reviewApi.addReview("p-1", CreateReviewRequest(rating = 4, comment = "Хорошо"))

        assertEquals(409, response.code())
        assertFalse(response.isSuccessful)
        assertNull(response.body())
    }

    // 16. createProductAdmin
    @Test
    fun `createProductAdmin — ADMIN создаёт товар, сервер возвращает 201`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{"id":"p-new","categoryId":2,"name":"Samsung S24","description":"Флагман 2024",
                   "price":79990.0,"stock":20,"brand":"Samsung","model":"S24",
                   "imageUrls":[],"isActive":true,"specs":[],
                   "averageRating":0.0,"reviewCount":0}"""
            )
        )

        val request = CreateProductRequest(
            categoryId  = 2,
            name        = "Samsung S24",
            description = "Флагман 2024",
            price       = 79990.0,
            stock       = 20,
            brand       = "Samsung",
            model       = "S24",
            imageUrls   = emptyList(),
            specs       = emptyList()
        )
        val response    = productApi.createProduct(request)
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(201, response.code())
        assertTrue(response.isSuccessful)
        assertEquals("p-new", response.body()?.id)
        assertEquals("Samsung S24", response.body()?.name)
        assertEquals("POST", httpRequest.method)
        assertEquals("/products", httpRequest.path)
    }

    // 17. createProductCustomer
    @Test
    fun `createProductCustomer — CUSTOMER пытается создать товар, сервер возвращает 403`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(403).setBody(
                """{"error":"FORBIDDEN","message":"Admin access required","status":403}"""
            )
        )

        val request = CreateProductRequest(
            categoryId  = 1,
            name        = "Fake Product",
            description = null,
            price       = 100.0,
            stock       = 1,
            brand       = "Brand",
            model       = "Model",
            imageUrls   = emptyList(),
            specs       = emptyList()
        )
        val response = productApi.createProduct(request)

        assertEquals(403, response.code())
        assertFalse(response.isSuccessful)
        assertNull(response.body())
    }

    // 18. uploadImageSuccess
    @Test
    fun `uploadImageSuccess — ADMIN загружает изображение, сервер возвращает 201 с url`() = runTest {
        val imageUuid = "550e8400-e29b-41d4-a716-446655440000"
        mockWebServer.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{"url":"images/$imageUuid"}"""
            )
        )

        val imageBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("image", "photo.jpg", requestBody)
        val response    = imageApi.uploadImage(part)
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(201, response.code())
        assertTrue(response.isSuccessful)
        assertNotNull(response.body()?.url)
        assertTrue(response.body()!!.url.startsWith("images/"))
        assertEquals("POST", httpRequest.method)
        assertEquals("/images", httpRequest.path)
        assertTrue(httpRequest.getHeader("Content-Type")!!.contains("multipart/form-data"))
    }

    // 19. getImageSuccess
    @Test
    fun `getImageSuccess — публичный запрос изображения по UUID возвращает 200 с бинарными данными`() {
        val imageUuid = "550e8400-e29b-41d4-a716-446655440000"
        val fakeImageBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "image/jpeg")
                .setBody(Buffer().write(fakeImageBytes))
        )

        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url(mockWebServer.url("/images/$imageUuid"))
            .get()
            .build()
        val response    = client.newCall(request).execute()
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(200, response.code)
        assertTrue(response.isSuccessful)
        val contentType = response.header("Content-Type")
        assertNotNull(contentType)
        assertTrue(contentType!!.contains("image/jpeg"))
        assertEquals("GET", httpRequest.method)
        assertEquals("/images/$imageUuid", httpRequest.path)
        val bodyBytes = response.body?.bytes()
        assertNotNull(bodyBytes)
        assertTrue(bodyBytes!!.isNotEmpty())
        response.close()
    }

    // 20. unauthorizedAccess
    @Test
    fun `unauthorizedAccess — запросы к защищённым эндпоинтам без токена возвращают 401`() = runTest {
        repeat(3) {
            mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody(
                """{"error":"UNAUTHORIZED","message":"Missing or invalid token","status":401}"""
            ))
        }

        val cartResponse     = cartApi.getCart()
        val wishlistResponse = wishlistApi.getWishlist()
        val ordersResponse   = orderApi.getMyOrders()

        assertEquals(401, cartResponse.code())
        assertFalse(cartResponse.isSuccessful)

        assertEquals(401, wishlistResponse.code())
        assertFalse(wishlistResponse.isSuccessful)

        assertEquals(401, ordersResponse.code())
        assertFalse(ordersResponse.isSuccessful)

        val cartRequest     = mockWebServer.takeRequest()
        val wishlistRequest = mockWebServer.takeRequest()
        val ordersRequest   = mockWebServer.takeRequest()
        assertEquals("/cart", cartRequest.path)
        assertEquals("/wishlist", wishlistRequest.path)
        assertEquals("/orders/my", ordersRequest.path)
    }

    // 21. addToWishlistSuccess
    @Test
    fun `addToWishlistSuccess — авторизованный пользователь добавляет товар в избранное, сервер возвращает 201`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{"id":"w-1","productId":"p-1","productName":"iPhone 15",
                   "productPrice":99990.0,"productImageUrl":null,"stock":5}"""
            )
        )

        val response    = wishlistApi.addToWishlist("p-1")
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(201, response.code())
        assertTrue(response.isSuccessful)
        assertEquals("p-1", response.body()?.productId)
        assertEquals("POST", httpRequest.method)
        assertEquals("/wishlist/p-1", httpRequest.path)
    }

    // 22. unknownRoute
    @Test
    fun `unknownRoute — запрос к несуществующему маршруту возвращает 404 с JSON-ошибкой`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"error":"NOT_FOUND","message":"Route not found","status":404}""")
        )

        val response    = productApi.getProductById("nonexistent-route")
        val httpRequest = mockWebServer.takeRequest()

        assertEquals(404, response.code())
        assertFalse(response.isSuccessful)
        assertNull(response.body())
        val contentType = response.headers()["Content-Type"]
        assertNotNull(contentType)
        assertTrue(contentType!!.contains("application/json"))
        assertEquals("GET", httpRequest.method)
    }
}
