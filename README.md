# Refleksi 1 Modul - Aplikasi E-Shop

## Standar & Prinsip Coding yang Diterapkan

### Prinsip Clean Code
1.  **Single Responsibility Principle (SRP)** - Satu Tugas, Satu Kelas:
    -   Aplikasi ini arsitekturnya udah dipisah-pisah tugasnya dengan relatif rapi:
        -   `ProductController`: Ngurusin request HTTP sama navigasi antar halaman
        -   `ProductService`: Tempat kontrak/aturan logika bisnisnya
        -   `ProductRepository`: Ngatur data (nyimpennya di memori)
    -   Jadi dengan pemisahan seperti ini, kode jadi lebih gampang dirawat, di-test, dan dipahami orang lain

2.  **Dependency Inversion Principle (DIP)** - Bergantung pada Abstraksi:
    -   Di sini `ProductController` nggak langsung akses `ProductServiceImpl`, tapi lewat interface `ProductService` dulu. Kenapa? Tentunya agar lebih fleksibel aja. Mau ganti implementasi gampang, mau di-mock waktu testing juga enak

3.  **Penamaan yang Jelas & Bermakna**:
    -   Semua nama class (`ProductController`, `ProductRepository`), method (`findById`, `edit`, `delete`), sampe variabel dikasih nama yang straightforward. Jadi orang baca langsung paham fungsinya apa tanpa perlu baca komentar panjang lebar

4.  **DRY (Don't Repeat Yourself)** - Tidak Ngulang-ngulang Kode:
    -   Logika yang sering dipake kayak cari produk berdasarkan ID, cukup ditulis sekali aja di repository (`findById`), terus tinggal dipanggil sama method lain,

### Praktik Secure Coding
1.  **Pakai UUID untuk ID Produk**:
    -   Daripada pakai angka 1, 2, 3 yang gampang ditebak, lebih baik pakai `UUID` (`UUID.randomUUID()`). Soalnya dengan pakai angka berurutan, orang bisa nebak-nebak ID produk lain  **IDOR (Insecure Direct Object Reference)**. Dengan UUID yang random, risiko ini jadi jauh lebih kecil

## Yang Perlu Diperbaiki

### 1. Method HTTP untuk Delete Kurang Aman (Rawan CSRF)
**Masalahnya dimana:**
Waktu bikin fitur delete, aku pakai request `GET`:
```java
@GetMapping("/delete/{id}")
public String deleteProduct(...) { ... }
```

**Kenapa ini bermasalah:**
`GET` itu harusnya cuma hanya untuk baca data aja, bukan buat ngubah atau hapus data. Kalau pakai `GET` untuk delete, aplikasi kita jadi rentan kena serangan **CSRF (Cross-Site Request Forgery)**.

Bayangkan ada website jahat yang bikin admin tanpa sadar hapus produk cuma gara-gara klik link atau bahkan cuma buka gambar.

**Solusinya:**
-   Ganti jadi `@PostMapping` atau `@DeleteMapping` biar lebih aman
-   Di `productList.html`, ubah tombol delete jadi pakai `<form>` dengan `method="post"`, jangan cuma link `<a>` biasa

### 2. Belum Ada Validasi Input
**Masalahnya dimana:**
Aplikasi belum ngecek input dari user. Jadi user bisa aja bikin produk dengan nama kosong atau jumlahnya minus. Ini bisa bikin data jadi berantakan

**Solusinya:**
-   Tambahin anotasi validasi di model `Product` pakai **Jakarta Bean Validation**:
    ```java
    @NotBlank(message = "Nama produk wajib diisi")
    private String productName;
    
    @Min(value = 0, message = "Jumlah produk harus positif")
    private int productQuantity;
    ```
-   Di Controller, jangan lupa pakai `@Valid` dan tangani errornya lewat `BindingResult`

---

# Refleksi 2 Modul - Testing dan Code Quality

## 1. Unit Testing dan Code Coverage

### Perasaan setelah menulis Unit Test
Setelah menulis unit test, rasanya jauh lebih percaya diri. Kita jadi punya jaring pengaman yang memastikan kalau fitur yang kita buat (seperti Edit dan Delete) jalan sesuai rencana, baik saat skenario normal maupun saat ada input yang aneh-aneh (edge cases).

### Berapa banyak Unit Test dalam satu Class?
Sebenarnya nggak ada angka pasti harus berapa. Yang penting adalah **kualitas dan cakupannya**, bukan jumlahnya. Makin kompleks logika di dalam class, makin banyak variasi test yang dibutuhkan.

### Apakah Unit Test sudah cukup untuk verifikasi program?
Belum tentu. Unit test itu cuma ngecek bagian-bagian kecil (unit) secara terisolasi. Kita masih butuh:
-   **Integration Test:** Buat mastiin unit-unit yang beda itu bisa kerja bareng (misal Service ngobrol sama Repository).
-   **Functional/E2E Test:** Buat mastiin alur aplikasi dari sudut pandang user (seperti yang kita buat pake Selenium).
-   **Manual Testing:** Kadang ada hal visual atau UX yang susah ditangkep sama automated test.

### Tentang Code Coverage 100%
Punya 100% code coverage itu bagus karena artinya setiap baris kode kita pernah dieksekusi setidaknya sekali selama testing. **TAPI**, 100% coverage **TIDAK MENJAMIN** bebas bug.
-   Coverage cuma ngukur kuantitas (baris yang tersentuh), bukan kualitas logika test-nya.
-   Bisa aja barisnya tereksekusi tapi assertion-nya (pengecekannya) salah atau kurang lengkap.
-   Bug logika bisnis (logic error) seringkali nggak ketangkep cuma dengan coverage tinggi.

## 2. Refleksi tentang Functional Test Baru (Code Cleanliness)

### Masalah Duplikasi Kode
Kalau saya membuat class functional test baru untuk menghitung jumlah item (`CountItemFunctionalTest.java`) dengan menyalin prosedur setup dan variabel instance dari `CreateProductFunctionalTest.java`, itu akan melanggar prinsip **DRY (Don't Repeat Yourself)**.

**Isu Kebersihan Kode (Clean Code Issues):**
1.  **Duplikasi Setup:** Kode untuk setup server, port, dan base URL akan ditulis ulang di setiap class test.
2.  **Susah Maintain:** Kalau nanti cara setup server berubah (misal ganti config port), kita harus ubah di banyak file satu-satu.
3.  **Readability Menurun:** Fokus test jadi terganggu karena banyak kode boilerplate setup yang sama berulang-ulang.

### Saran Perbaikan
Untuk menjaga kode tetap bersih dan mudah dirawat, sebaiknya kita pakai **Inheritance (Pewarisan)** atau membuat **Base Test Class**.

**Solusi:**
Buat satu class abstrak, misalnya `BaseFunctionalTest.java`, yang isinya semua konfigurasi umum:
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(SeleniumJupiter.class)
abstract class BaseFunctionalTest {
    @LocalServerPort
    protected int serverPort;

    @Value("${app.baseUrl:http://localhost}")
    protected String testBaseUrl;

    protected String baseUrl;

    @BeforeEach
    void setupTest() {
        baseUrl = String.format("%s:%d", testBaseUrl, serverPort);
    }
}
```
Terus, test class lainnya (`CreateProductFunctionalTest`, `HomePageFunctionalTest`, dll) tinggal **extends** class ini. Jadi mereka cuma fokus ke logic test-nya aja, nggak perlu mikirin setup lagi.