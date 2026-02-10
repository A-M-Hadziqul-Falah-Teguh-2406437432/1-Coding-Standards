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
