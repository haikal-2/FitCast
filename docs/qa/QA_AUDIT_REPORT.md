# Laporan Audit Kualitas (QA & Deep Code Analysis) FitCast (CuacaGaya)

- **Package:** `com.fitcast.cuacagaya`
- **Target Audit:** Implementasi aktual dibandingkan referensi `docs/ui-reference/` (`01` s.d. `08`)
- **Acuan Standar:** `android-skills`, `ui-ux-pro-max`, `mobile-skill`, `mobile-dev-skills`, `DESIGN.md`
- **Tanggal Audit:** 27 November 2026
- **Status Kode Aplikasi (`app/src/main/`):** Belum diubah (audit murni + penambahan unit test di `app/src/test/`)

---

## 1. Ringkasan Skor Kesiapan (Skala 0 - 100)

| Pilar Audit | Skor | Status | Ringkasan Kondisi |
| :--- | :---: | :---: | :--- |
| **1. Sistem, Data & Anti-Crash** | **74 / 100** | Perlu Perbaikan | Room v2 + `StateFlow` + `collectAsStateWithLifecycle` sudah solid dan `BuildConfig` aman. Masih ada risiko OOM pada dekode bitmap resolusi tinggi, file gambar lokal yatim saat hapus item, potensi hang pada callback GPS/ML Kit tanpa `onCanceledListener`, serta `POST_NOTIFICATIONS` Android 13+ yang belum pernah diminta di UI. |
| **2. Layout & Presisi Visual** | **82 / 100** | Baik (Ada Celah Dark Mode & 1.3x) | Struktur visual Light Mode sangat mendekati referensi `01` s.d. `08`, font `Plus Jakarta Sans` konsisten, dan `contentPadding` bawah sudah menghindari `FitCastFloatingBottomNav` dan FAB `+`. Namun terdapat masalah kontras teks gelap di atas `SoftIceTintDark` pada Dark Mode serta risiko clipping pada `360dp` dengan Font Scale `1.3x`. |
| **3. Transisi & Mikro-Animasi** | **78 / 100** | Cukup Baik | Alur Splash ke Onboarding, backstack anti-kembali ke onboarding, morphing kapsul bottom bar, skala status `BERSIH`/`LAUNDRY`, dan animasi `Selesai Cuci` sudah berjalan baik. Masih kurang animasi `NavHost` (slide-up `AddItemScreen` dan crossfade tab), transisi `AnimatedContent` saat `Acak/Shuffle`, serta `Modifier.animateItem()` pada daftar "Baru Saja Dicuci". |
| **4. Feedback APK per Fitur** | **72 / 100** | Perlu Peningkatan | Snackbar sudah tersedia di sebagian besar layar dan tidak tertutup bottom nav, serta tombol `Simpan ke Lemari` sudah punya indikator loading. Masih belum ada `HapticFeedback`, belum ada anti double-tap pada `Pakai Outfit` / `Cuci Semua`, `isLoadingWeather` belum divisualisasikan di UI, dan Draft belum punya tampilan lanjutan. |
| **5. Logika Algoritma & Integritas Data** | **84 / 100** | Baik | Algoritma rekomendasi cuaca, lock item, fallback baju kotor, filter gaya, sinkronisasi `wearCount / maxWearBeforeWash`, dan metrik dinamis Laundry sudah terverifikasi lewat 16 unit test. Masih perlu mengecualikan `isDraft` di DAO/Engine, memanfaatkan `suitableWeathers`, serta memberi bobot ketebalan bahan pada estimasi air/waktu putar. |
| **6. Automated Testing & Build** | **92 / 100** | Sangat Baik | `testDebugUnitTest` (16/16 PASS), `assembleDebug` (0 error, 0 warning Kotlin), dan `lintDebug` (0 error, 40 warning/advisory non-fatal). |
| **Skor Gabungan Kesiapan** | **80 / 100** | **Siap Demo setelah Fix Prioritas** | Fondasi kuat; memperbaiki 1 temuan `CRITICAL` dan 7 temuan `HIGH` akan mengangkat kesiapan ke level produksi (>93/100). |

---

## 2. Daftar Temuan Berdasarkan Severity

### CRITICAL (Wajib Diperbaiki Sebelum Rilis / Demo)

#### [C-01] Potensi `OutOfMemoryError` (OOM) saat Memproses Foto Kamera/Galeri Resolusi Tinggi
- **Layar / Modul:** `AddItemScreen` / Image Processing (`ImageProcessor`)
- **File & Baris:** `app/src/main/java/com/fitcast/cuacagaya/domain/processor/ImageProcessor.kt:149-166`
- **Masalah Ditemukan:** `decodeSampledBitmap(uri, maxSize)` langsung memanggil `BitmapFactory.decodeStream(stream)` tanpa melakukan pass pertama `inJustDecodeBounds = true` dan menghitung `inSampleSize`. Selain itu, `InputStream` tidak dibungkus `use {}`, bitmap `raw` asal tidak di-`recycle()` setelah scaling, dan orientasi EXIF tidak dibaca.
- **Dampak ke User / Dosen:** Saat memilih foto kamera HP modern (12 MP sampai 50 MP, sekitar 50-200 MB bitmap mentah di memori) pada HP RAM 4-6 GB, aplikasi berisiko langsung *force close* (`OutOfMemoryError`) saat menambahkan pakaian, atau foto tampil terbalik 90 derajat.
- **Rencana Perbaikan:** Lakukan 2-pass decode (`inJustDecodeBounds = true` untuk membaca dimensi, hitung power-of-two `inSampleSize`, lalu decode dengan `inSampleSize` di dalam `use {}`), baca orientasi EXIF dengan `ExifInterface`, dan panggil `raw.recycle()` bila `createScaledBitmap` membuat instance baru.

---

### HIGH (Prioritas Utama)

#### [H-01] File Gambar Lokal Tidak Dihapus dari Storage saat Item Dihapus (*Orphan Files*)
- **Layar / Modul:** `WardrobeScreen` / `WardrobeRepository`
- **File & Baris:** `app/src/main/java/com/fitcast/cuacagaya/data/repository/WardrobeRepository.kt:15`, `app/src/main/java/com/fitcast/cuacagaya/ui/wardrobe/WardrobeViewModel.kt:107-109`
- **Masalah Ditemukan:** `deleteItem(item)` hanya menghapus entri Room (`dao.deleteItem(item)`), tidak menghapus file PNG di `context.filesDir` yang dirujuk `item.imagePath`.
- **Dampak ke User / Dosen:** Ruang penyimpanan internal aplikasi terus membengkak setiap kali pengguna menghapus atau mengganti koleksi baju.
- **Rencana Perbaikan:** Hapus `File(item.imagePath)` secara aman di `Dispatchers.IO` (dibungkus `runCatching`) di dalam `WardrobeRepository.deleteItem()` apabila `imagePath` tidak kosong dan berada dalam direktori internal aplikasi.

#### [H-02] Risiko Coroutine Hang & Penanganan Offline Kurang Ketat pada GPS, Weather API, dan ML Kit
- **Layar / Modul:** `WeatherRepository`, `WeatherApiService`, `ImageProcessor`
- **File & Baris:**
  - `app/src/main/java/com/fitcast/cuacagaya/data/repository/WeatherRepository.kt:24-72`
  - `app/src/main/java/com/fitcast/cuacagaya/data/remote/WeatherApiService.kt:30-36`
  - `app/src/main/java/com/fitcast/cuacagaya/domain/processor/ImageProcessor.kt:85-100`
- **Masalah Ditemukan:**
  1. `WeatherRepository.getCurrentLocation()` dan `ImageProcessor.removeBackgroundWithMlKit()` menggunakan `suspendCancellableCoroutine` tanpa `addOnCanceledListener`, sehingga jika Google Play Services membatalkan Task, coroutine dapat menggantung tanpa pernah selesai.
  2. `getCurrentLocation()` memakai `@SuppressLint("MissingPermission")` tanpa mengecek `ContextCompat.checkSelfPermission()` secara dinamis sebelum memanggil FusedLocationProvider.
  3. `WeatherApiService.create()` tidak mengatur `OkHttpClient` timeout eksplisit, dan `WeatherRepository` tetap melakukan HTTP call meski `BuildConfig.WEATHER_API_KEY.isBlank()`.
  4. `SubjectSegmentation.getClient(options)` pada `ImageProcessor` dibuat baru setiap panggilan tanpa `segmenter.close()`.
- **Dampak ke User / Dosen:** Saat GPS dibatalkan sistem, API key kosong, atau jaringan sangat lambat, indikator loading bisa macet dan konsumsi resource ML Kit menumpuk.
- **Rencana Perbaikan:** Tambahkan `addOnCanceledListener`, cek izin lokasi dinamis + `CancellationTokenSource` + `withTimeoutOrNull(8_000L)`, cek `BuildConfig.WEATHER_API_KEY.isBlank()` sebelum memanggil API agar langsung fallback ke cache kota terpilih, set `OkHttpClient` timeout (8s connect / 10s read), dan tutup `segmenter.close()` di `finally` / callback.

#### [H-03] Izin `POST_NOTIFICATIONS` Android 13+ Tidak Pernah Diminta & Penolakan Permanen Lokasi Belum Arahkan ke Settings
- **Layar / Modul:** `MainActivity`, `HomeScreen`, `StudioScreen`, `WardrobeScreen`, `LaundryScreen`, `MorningBriefingWorker`
- **File & Baris:**
  - `app/src/main/java/com/fitcast/cuacagaya/worker/MorningBriefingWorker.kt:25-80`
  - `app/src/main/java/com/fitcast/cuacagaya/ui/home/HomeScreen.kt:95-103`
- **Masalah Ditemukan:**
  1. `MorningBriefingWorker` sudah memeriksa `POST_NOTIFICATIONS` di API 33+ sehingga tidak crash `SecurityException`, tetapi aplikasi tidak pernah meluncurkan dialog permintaan izin `POST_NOTIFICATIONS` (misalnya saat masuk Beranda pertama kali atau lewat dialog ringkasan notifikasi).
  2. `MorningBriefingWorker.doWork()` belum dibungkus `try/catch` dan `notify()` belum dibungkus `try/catch (SecurityException)`.
  3. Penolakan permanen izin lokasi tidak memeriksa `shouldShowRequestPermissionRationale()` dan tidak menyediakan tombol pintasan ke `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`.
- **Dampak ke User / Dosen:** Pada perangkat Android 13/14/15, notifikasi briefing pagi pukul 06:30 tidak akan pernah muncul. Pengguna yang terlanjur menekan "Jangan tanya lagi" pada izin GPS juga tidak punya jalan cepat mengaktifkannya kembali.
- **Rencana Perbaikan:** Tambahkan permintaan izin `POST_NOTIFICATIONS` di Android 13+ saat pertama masuk Beranda (atau tombol aktifkan di dialog notifikasi), terapkan alur 3-state untuk izin lokasi dengan opsi "Buka Pengaturan", dan tambahkan `try/catch` di `MorningBriefingWorker`.

#### [H-04] Risiko Double-Tap Duplikasi Data & `isLoadingWeather` Tidak Ditampilkan di UI
- **Layar / Modul:** `HomeScreen`, `StudioScreen`, `LaundryScreen`
- **File & Baris:**
  - `app/src/main/java/com/fitcast/cuacagaya/ui/home/HomeViewModel.kt:159-174`
  - `app/src/main/java/com/fitcast/cuacagaya/ui/home/HomeScreen.kt:267-287`
  - `app/src/main/java/com/fitcast/cuacagaya/ui/studio/StudioScreen.kt:489-508`
  - `app/src/main/java/com/fitcast/cuacagaya/ui/wardrobe/LaundryScreen.kt:238-252`
- **Masalah Ditemukan:**
  1. Tombol `"Pakai Outfit Ini Hari Ini"` (`HomeScreen`) dan `"Pakai"` (`StudioScreen`) tidak memiliki guard `isWearingOutfit` / disable sementara, sehingga tap ganda cepat dapat menaikkan `wearCount` dua kali dan menyimpan duplikat `SavedOutfitEntity`.
  2. Tombol `"Cuci Semua"` di `LaundryScreen` tidak dinonaktifkan selama animasi `collapsingIds` berlangsung (~600ms).
  3. `HomeUiState.isLoadingWeather` sudah dikelola di `HomeViewModel`, namun belum dirender sebagai spinner/indikator pada `FitCastTopBar` atau sub-kartu cuaca saat GPS/kota diperbarui.
- **Dampak ke User / Dosen:** Double-tap tidak sengaja langsung mengotori baju atau menggandakan riwayat outfit; saat memperbarui cuaca lewat GPS, layar tampak diam tanpa indikasi proses berjalan.
- **Rencana Perbaikan:** Tambahkan state `isSavingWear: Boolean` di `HomeViewModel` untuk men-disable tombol sementara, disable `"Cuci Semua"` saat `confirmingIds.isNotEmpty() || collapsingIds.isNotEmpty()`, dan tampilkan `CircularProgressIndicator` mini di pill lokasi `FitCastTopBar` saat `isLoadingWeather == true`.

#### [H-05] Tidak Ada `HapticFeedback` pada Aksi Interaktif Utama
- **Layar / Modul:** `HomeScreen`, `StudioScreen`, `WardrobeScreen`, `AddItemScreen`, `LaundryScreen`
- **File & Baris:** Seluruh file UI di `app/src/main/java/com/fitcast/cuacagaya/ui/`
- **Masalah Ditemukan:** `LocalHapticFeedback.current` belum digunakan sama sekali pada interaksi kunci (Lock/Unlock Hotspot Pin, Shuffle outfit, Pakai Outfit, Selesai Cuci, Cuci Semua, Simpan Item/Draft, Hapus Item).
- **Dampak ke User / Dosen:** Pengalaman sentuhan (*tactile polish*) terasa kurang responsif saat diuji langsung pada perangkat Android fisik.
- **Rencana Perbaikan:** Sisipkan `haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)` untuk toggle ringan (Lock/Shuffle/Stepper) dan `HapticFeedbackType.LongPress` untuk aksi konfirmasi (Pakai Outfit, Selesai Cuci, Simpan, Hapus).

#### [H-06] Transisi `NavHost` Belum Disesuaikan (Tidak Ada Slide-Up `AddItemScreen` & Crossfade Tab)
- **Layar / Modul:** `FitCastNavGraph`
- **File & Baris:** `app/src/main/java/com/fitcast/cuacagaya/ui/navigation/FitCastNavGraph.kt:69-121`
- **Masalah Ditemukan:** `NavHost` memakai animasi bawaan tanpa kustomisasi per rute. `AddItemScreen` sebagai layar input penuh idealnya muncul dengan *slide-up* + *fade* dan keluar dengan *slide-down*, sementara perpindahan 4 tab utama menggunakan crossfade halus agar tidak terasa kaku.
- **Dampak ke User / Dosen:** Perpindahan dari tombol `+ Tambah` menuju form `AddItemScreen` terasa generik dan kurang kohesif dengan bahasa gerak modern FitCast.
- **Rencana Perbaikan:** Definisikan `enterTransition` / `exitTransition` (fade 200ms) di `NavHost` untuk tab utama, serta `slideInVertically { it / 5 } + fadeIn()` / `slideOutVertically { it / 5 } + fadeOut()` khusus rute `"add_item"` dan slide horizontal untuk `"about"`.

#### [H-07] Kontras Dark Mode Rusak pada Elemen dengan `ObsidianDark` di Atas `SoftIceTintDark`
- **Layar / Modul:** `WardrobeScreen`, `AddItemScreen`, `LaundryScreen`
- **File & Baris:**
  - `app/src/main/java/com/fitcast/cuacagaya/ui/wardrobe/WardrobeScreens.kt:308, 387, 707, 745, 868, 886, 1084, 1218, 1236, 1347`
  - `app/src/main/java/com/fitcast/cuacagaya/ui/wardrobe/LaundryScreen.kt:340, 353, 456`
- **Masalah Ditemukan:** Banyak tombol pill, ikon Sort/Filter, chip non-aktif, stepper `- 3x +`, dan badge `"10 Bersih"` menggunakan latar `fitColors.softIceTint` (yang berubah menjadi `#1E2A3D` di Dark Mode) namun tetap men-hardcode warna teks/ikon `ObsidianDark` (`#131816`).
- **Dampak ke User / Dosen:** Saat perangkat berada pada **Dark Mode**, teks dan ikon gelap (`#131816`) di atas biru tua (`#1E2A3D`) memiliki rasio kontras ~1.25:1 (gagal WCAG AA 4.5:1) sehingga nyaris tidak terbaca.
- **Rencana Perbaikan:** Tambahkan token semantik `onSoftIceTint` pada `FitCastExtendedColors` (`ObsidianDark` di Light Mode, `TextPrimaryDark` / `#F5F7F9` di Dark Mode) dan gunakan token tersebut secara konsisten pada seluruh komponen yang memakai latar `softIceTint`.

---

### MEDIUM (Perbaikan Kualitas, Edge State & Logika)

#### [M-01] Inkonsistensi Data `isDraft = true` antara `WardrobeDao`, `WardrobeScreen`, dan `OutfitRecommendationEngine`
- **Layar / Modul:** `WardrobeDao`, `WardrobeScreen`, `AddItemScreen`, `OutfitRecommendationEngine`
- **File & Baris:**
  - `app/src/main/java/com/fitcast/cuacagaya/data/local/dao/FitCastDaos.kt:11-18`
  - `app/src/main/java/com/fitcast/cuacagaya/domain/engine/OutfitRecommendationEngine.kt:23-24`
  - `app/src/main/java/com/fitcast/cuacagaya/ui/wardrobe/WardrobeScreens.kt:1265`
- **Masalah Ditemukan:** `WardrobeDao.getAllItems()` dan `getDirtyItems()` tidak memfilter `isDraft = 0`, sedangkan `HomeViewModel` memfilter `!it.isDraft`. Akibatnya saat pengguna menekan `"Simpan Sementara sebagai Draft"`, jumlah item di `HomeScreen` (`12`) tidak sama dengan `WardrobeScreen` (`13 ITEM`), draft ikut direkomendasikan di `OutfitRecommendationEngine`, dan tidak ada cara membuka kembali draft untuk dilanjutkan.
- **Dampak ke User / Dosen:** Kebingungan saat demo fitur Draft (angka statistik berbeda antar tab dan item draft muncul seperti baju final).
- **Rencana Perbaikan:** Pisahkan query `getActiveItems()` (`WHERE isDraft = 0`) dan `getDraftItems()` (`WHERE isDraft = 1`) di `WardrobeDao`, kecualikan draft di `OutfitRecommendationEngine`, dan tampilkan chip/banner `"Draft Tersimpan (N)"` di `WardrobeScreen` atau `AddItemScreen` agar draft bisa dibuka kembali dan dipublikasikan.

#### [M-02] Belum Ada Animasi Pergantian Konten saat `Acak / Shuffle` & `Modifier.animateItem()` pada Daftar Bersih
- **Layar / Modul:** `HomeScreen`, `StudioScreen`, `LaundryScreen`
- **File & Baris:**
  - `app/src/main/java/com/fitcast/cuacagaya/ui/home/HomeScreen.kt:595-606`
  - `app/src/main/java/com/fitcast/cuacagaya/ui/studio/StudioScreen.kt:296-322`
  - `app/src/main/java/com/fitcast/cuacagaya/ui/wardrobe/LaundryScreen.kt:372-377`
- **Masalah Ditemukan:** Saat tombol `Acak` ditekan, 3 kartu outfit di `HomeScreen` dan preview flat-lay di `StudioScreen` berubah seketika tanpa `AnimatedContent` / crossfade. Di `LaundryScreen`, kartu pada `"Baru Saja Dicuci"` belum menggunakan `Modifier.animateItem()`.
- **Dampak ke User / Dosen:** Perubahan shuffle terasa terlalu instan sehingga user kurang sadar kombinasi baru baru saja terbentuk.
- **Rencana Perbaikan:** Bungkus deretan 3 kartu outfit di `HomeScreen` dan komposisi pakaian di `StudioScreen` dengan `AnimatedContent` (fade + slight scale 220ms) dengan key kombinasi ID item, serta tambahkan `Modifier.animateItem()` pada item `"Baru Saja Dicuci"`.

#### [M-03] Risiko Clipping & Tumpang Tindih pada Layar `360dp` dan Mode *Font Scale 1.3x*
- **Layar / Modul:** `HomeScreen`, `StudioScreen`, `AddItemScreen`, `OnboardingAndAboutScreens`
- **File & Baris:**
  - `app/src/main/java/com/fitcast/cuacagaya/ui/studio/StudioScreen.kt:349-360` (Pin 1 `Outer` tidak memiliki `padding(end = 72.dp)` sehingga dapat bertabrakan dengan dock kontrol kanan)
  - `app/src/main/java/com/fitcast/cuacagaya/ui/home/HomeScreen.kt:426` (`BentoStatCard` memakai `height(114.dp)` fixed; berisiko sesak di Font Scale 1.3x)
  - `app/src/main/java/com/fitcast/cuacagaya/ui/wardrobe/WardrobeScreens.kt:829-915` (Baris 3 tombol `Ulang`, `Galeri`, `Auto-Cut AI` di `AddItemScreen` memakai `height(42.dp)` fixed dan bisa memotong teks pada 360dp + 1.3x)
  - `app/src/main/java/com/fitcast/cuacagaya/ui/onboarding/OnboardingAndAboutScreens.kt:541` (`SmartWardrobeCollage` fixed `356.dp` tanpa fallback scroll pada layar pendek 640dp)
- **Dampak ke User / Dosen:** Jika diuji dengan ukuran font sistem "Besar" (1.3x) atau layar 360dp, beberapa label terpotong atau pin menyentuh tombol dock kanan.
- **Rencana Perbaikan:** Tambahkan `padding(end = 72.dp)` pada Hotspot Pin 1 di `StudioScreen`, ganti `height(...)` fixed menjadi `defaultMinSize(minHeight = ...)` / `heightIn(min = ...)`, dan buat teks pill di `AddItemScreen` lebih adaptif (`maxLines = 1` + `TextOverflow.Ellipsis` / `FlowRow` fallback).

#### [M-04] Edge State "0 Baju Bersih" di `HomeScreen` Belum Menampilkan Peringatan Khusus
- **Layar / Modul:** `HomeScreen`
- **File & Baris:** `app/src/main/java/com/fitcast/cuacagaya/ui/home/HomeScreen.kt:230-265`
- **Masalah Ditemukan:** Saat seluruh item kotor (`readyCount == 0`), `OutfitRecommendationEngine` sudah aman tidak crash dan fallback ke semua item, tetapi `HomeScreen` tidak menampilkan banner info bahwa rekomendasi memakai pakaian yang sedang menunggu cucian (dengan tombol cepat ke tab `Laundry`).
- **Dampak ke User / Dosen:** Pengguna bisa mengira baju yang disarankan bersih padahal seluruh lemari sedang di keranjang cucian.
- **Rencana Perbaikan:** Tampilkan banner peringatan ringkas saat `state.totalCount > 0 && state.readyCount == 0` dengan aksi langsung menuju `LaundryScreen`.

#### [M-05] `OutfitRecommendationEngine` Belum Memanfaatkan `suitableWeathers` & Kalkulasi Laundry Belum Membedakan Berat Bahan
- **Layar / Modul:** `OutfitRecommendationEngine`, `LaundryScreen`
- **File & Baris:**
  - `app/src/main/java/com/fitcast/cuacagaya/domain/engine/OutfitRecommendationEngine.kt:44-56`
  - `app/src/main/java/com/fitcast/cuacagaya/ui/wardrobe/LaundryScreen.kt:392-393`
- **Masalah Ditemukan:**
  1. Field `suitableWeathers` pada `WardrobeItemEntity` v2 belum dipakai dalam penilaian skor rekomendasi cuaca (seperti mencocokkan `"Hujan Ringan"` atau `"Panas"`).
  2. `WardrobeRepository.wearItems()` terus menambah `wearCount` melebihi `maxWearBeforeWash` jika item yang sudah kotor dipakai ulang.
  3. Di `LaundryScreen`, `waterLiters = 10 + (count * 4)` dan `spinMinutes = 25 + (count * 5)` sudah dinamis berdasarkan jumlah item, tetapi belum membedakan item tebal (`WarmthLevel.HEAVY` / `OUTER`) vs tipis (`LIGHT`).
- **Dampak ke User / Dosen:** Rekomendasi cuaca dan estimasi siklus cuci belum sepenuhnya memaksimalkan metadata kaya dari Room v2.
- **Rencana Perbaikan:** Tambahkan bonus skor (+15) jika `suitableWeathers` cocok dengan kondisi cuaca aktif, batasi `wearCount` maksimum atau tampilkan status kelebihan pakai secara jelas, dan tambahkan bobot `WarmthLevel.HEAVY` pada kalkulasi air/waktu putar (tetap menghasilkan `18 Liter (Eco)` dan `35 Menit` untuk 2 item starter `Kaos Hitam Basic` + `Hoodie Abu Tua`).

#### [M-06] Tombol `"Ulang"` di `AddItemScreen` Belum Membuka Kamera Langsung
- **Layar / Modul:** `AddItemScreen`
- **File & Baris:** `app/src/main/java/com/fitcast/cuacagaya/ui/wardrobe/WardrobeScreens.kt:838-843`
- **Masalah Ditemukan:** Ikon tombol `"Ulang"` berupa kamera (`Icons.Outlined.PhotoCamera`), namun saat ditekan hanya mengosongkan `selectedUri = null` alih-alih meluncurkan `ActivityResultContracts.TakePicture` (dengan `FileProvider`) atau membuka ulang pemilih foto.
- **Dampak ke User / Dosen:** Jika dosen mencoba mengambil foto baju langsung lewat tombol berikon kamera, kamera tidak terbuka.
- **Rencana Perbaikan:** Konfigurasikan `FileProvider` di `AndroidManifest.xml` (dengan `pathPrefix` spesifik) dan hubungkan tombol kamera ke `TakePicture` (dengan pengecekan izin `CAMERA` 3-state) atau tawarkan aksi "Ambil Foto / Reset Foto".

---

### POLISH (Optimasi Lint, Kebersihan Kode & Detail Kecil)

#### [P-01] Temuan Android Lint Kode Sumber (`DiscouragedApi`, `LocalContextResourcesRead`, `ObsoleteSdkInt`, `UseKtx`, `UnusedResources`, `RedundantLabel`)
- **File & Baris:**
  - `GarmentArtwork.kt:49`: `context.resources.getIdentifier(...)` memicu Lint `DiscouragedApi` & `LocalContextResourcesRead`.
  - `GarmentArtwork.kt:32`, `FitCastNavGraph.kt:44`, `ImageProcessor.kt:161`: Memicu saran `UseKtx` (`toColorInt()`, `prefs.edit { ... }`, `raw.scale(...)`).
  - `MorningBriefingWorker.kt:54`: `Build.VERSION.SDK_INT >= Build.VERSION_CODES.O` memicu `ObsoleteSdkInt` karena `minSdk = 26`.
  - `MorningBriefingWorker.kt:72`: Ikon notifikasi masih menggunakan `android.R.drawable.ic_menu_compass`, sebaiknya menggunakan vektor ikon FitCast.
  - `AndroidManifest.xml:26`: `android:label="@string/app_name"` di `<activity>` redundan (`RedundantLabel`).
  - `res/values/colors.xml:3-9`: 7 warna template lama (`purple_200` s.d. `white`) tidak terpakai (`UnusedResources`).
- **Rencana Perbaikan:** Ganti lookup `getIdentifier` dengan `Map<String, Int>` statis untuk drawable starter, terapkan KTX extension (`core-ktx`), hapus pengecekan `SDK_INT >= O` yang usang, hapus label redundan dan warna template tak terpakai, serta pindahkan dependensi `app/build.gradle.kts` ke `gradle/libs.versions.toml` (`UseTomlInstead`).

#### [P-02] Hardcoded `Color(0xFF181A1B)` pada `HomeScreen`
- **File & Baris:** `app/src/main/java/com/fitcast/cuacagaya/ui/home/HomeScreen.kt:398, 697`
- **Masalah Ditemukan:** Kartu `SIAP PAKAI` dan `WeatherAlertDarkBanner` memakai `Color(0xFF181A1B)` langsung alih-alih token tema (`ObsidianDark` / `SurfaceCardDark`), sehingga batas kartu kurang tegas saat Dark Mode.
- **Rencana Perbaikan:** Gunakan `fitColors.darkCardSurface` di Dark Mode dan `ObsidianDark` di Light Mode dengan border halus `fitColors.subtleBorder`.

---

## 3. Tabel Matrix Feedback & Transisi per Layar

| Layar | Referensi UI | Visual & Layout | Transisi & Animasi | Feedback (Loading / Snackbar / Haptic) | Empty / Offline / Error State | Status Keseluruhan |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Splash 1 (`SplashLaunchScreen`)** | `01` | PASS (Canvas siluet, gradien, progress bar) | PASS (auto 1.8s / tap + `Crossfade`) | PASS (tap responsif) | N/A | **PASS** |
| **Onboarding 2 (`Peringatan Cuaca`)** | `02` | PASS (mockup lockscreen + glow Cobalt) | PASS (`HorizontalPager` + animated dot) | PASS (Lewati & Lanjut berfungsi) | N/A | **PASS** |
| **Onboarding 3 (`Lemari Digital Pintar`)** | `03` | PARTIAL (perlu scroll fallback di layar 640dp) | PASS (5 kartu float `graphicsLayer`) | PASS (pill `ID` interaktif, CTA berfungsi) | N/A | **PASS** |
| **Beranda (`HomeScreen`)** | `04` | PARTIAL (1.3x font scale pada bento/cuaca, `#181A1B` dark mode) | PARTIAL (belum ada `AnimatedContent` saat Shuffle) | PARTIAL (Snackbar OK; belum ada loading cuaca, anti double-tap Pakai, & haptic) | PARTIAL (offline fallback OK; belum ada banner khusus 0 baju bersih) | **PARTIAL** |
| **Mix & Match Studio (`StudioScreen`)** | `05` | PARTIAL (Pin 1 perlu `end = 72.dp` di layar 360dp) | PARTIAL (Lock color anim OK; belum ada animasi shuffle canvas) | PARTIAL (Snackbar OK; belum ada anti double-tap Pakai & haptic) | PASS (slot null aman tidak crash) | **PARTIAL** |
| **Lemari Digital (`WardrobeScreen`)** | `06` | PARTIAL (kontras Dark Mode `ObsidianDark` di `softIceTint`) | PASS (micro-animation `BERSIH`/`LAUNDRY` + scale spring) | PARTIAL (Snackbar OK; belum ada haptic; jumlah item bentrok jika ada draft) | PASS (empty state search/filter informatif) | **PARTIAL** |
| **Tambah Pakaian (`AddItemScreen`)** | `08` | PARTIAL (kontras Dark Mode & row 3 tombol di 360dp + 1.3x) | PARTIAL (belum slide-up modal di `NavHost`) | PARTIAL (loading ML Kit OK; tombol `Ulang` belum buka kamera; belum haptic) | PARTIAL (Draft tersimpan tapi belum bisa dibuka ulang) | **PARTIAL** |
| **Laundry Tracker (`LaundryScreen`)** | `07` | PARTIAL (kontras Dark Mode `softIceTint` pada chip/badge) | PARTIAL (`AnimatedVisibility` Selesai Cuci sangat baik; daftar bersih belum `animateItem()`) | PARTIAL (Snackbar OK; `Cuci Semua` belum lock saat animasi; belum haptic) | PASS (empty state keranjang kosong & filter informatif) | **PARTIAL** |
| **Tentang (`AboutScreen`)** | - | PASS (card editorial & tombol Privasi) | PASS (back stack bersih) | PASS (intent browser berfungsi) | N/A | **PASS** |

---

## 4. Hasil Output Gradle Test & Lint

Seluruh verifikasi dijalankan pada 27 November 2026 melalui perintah:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug --console=plain
```

### A. Ringkasan Status Build
- **Hasil Akhir:** `BUILD SUCCESSFUL in 2m 46s` (56 task, 19 dieksekusi, 37 up-to-date)
- **`assembleDebug`:** **PASS** (0 error kompilasi, 0 warning Kotlin)
- **`testDebugUnitTest`:** **PASS** (**16 / 16 test lulus**, 0 gagal, 0 error, 0 dilewati)
- **`lintDebug`:** **PASS** (**0 Error**, **40 Warning/Advisory**)

### B. Rincian Unit Test (`app/build/test-results/testDebugUnitTest/`)

1. **`OutfitRecommendationEngineTest` (8/8 PASS, 0.099s):**
   - `rainy weather recommends umbrella and includes outer layer`: PASS
   - `hot clear weather skips outer layer and does not recommend umbrella`: PASS
   - `cold dry weather includes outer layer without umbrella`: PASS
   - `dirty items are excluded when clean items exist`: PASS
   - `all dirty wardrobe falls back gracefully without returning null`: PASS
   - `locked items are always preserved even on hot weather across shuffles`: PASS
   - `style filter prioritizes matching style items`: PASS
   - `missing categories return null safely without crashing`: PASS
2. **`WardrobeAndLaundryLogicTest` (5/5 PASS, 0.045s):**
   - `wearing items increments wearCount and marks dirty once maxWearBeforeWash is reached`: PASS
   - `washing single item resets only that item while keeping others dirty`: PASS
   - `wash all cleans entire laundry queue and resets wear counts`: PASS
   - `toggleLaundryStatus switches clean to dirty at maxWear and back to clean`: PASS
   - `category and search filter match by name, colorName, material, and style`: PASS
3. **`StarterWardrobeSeederTest` (2/2 PASS, 0.098s):**
   - `seeds 12 starter items with complete metadata and 2 laundry items`: PASS
   - `does not duplicate items when database is already populated`: PASS
4. **`ExampleUnitTest` (1/1 PASS, 0.012s):**
   - `addition_isCorrect`: PASS

### C. Ringkasan Temuan Android Lint (`app/build/reports/lint-results-debug.sarif`)

| Kategori Rule Lint | Jumlah | Lokasi Utama |
| :--- | :---: | :--- |
| `UseTomlInstead` | 14 | `app/build.gradle.kts:61-85` |
| `GradleDependency` | 8 | `app/build.gradle.kts` & `gradle/libs.versions.toml` |
| `UnusedResources` | 7 | `app/src/main/res/values/colors.xml:3-9` |
| `NewerVersionAvailable` | 4 | `app/build.gradle.kts` & `gradle/libs.versions.toml` |
| `UseKtx` | 3 | `FitCastNavGraph.kt:44`, `GarmentArtwork.kt:32`, `ImageProcessor.kt:161` |
| `LocalContextResourcesRead` | 1 | `GarmentArtwork.kt:49` |
| `DiscouragedApi` | 1 | `GarmentArtwork.kt:49` (`Resources.getIdentifier`) |
| `ObsoleteSdkInt` | 1 | `MorningBriefingWorker.kt:54` |
| `RedundantLabel` | 1 | `AndroidManifest.xml:26` |
| **Total Error / Fatal** | **0** | - |
| **Total Warning / Advisory** | **40** | Laporan lengkap: `app/build/reports/lint-results-debug.html` |

---

## 5. Urutan Eksekusi Perbaikan yang Direkomendasikan (Tahap Remediation)

Jika disetujui untuk masuk ke tahap perbaikan kode, prioritas eksekusi yang paling efisien:
1. **Batch 1 (Anti-Crash & Data Integrity):** Fix `[C-01]` 2-pass bitmap sampling + EXIF, `[H-01]` hapus file gambar lokal saat delete, `[H-02]` timeout/cancel listener/permission guard di Weather & ML Kit, `[H-03]` permintaan `POST_NOTIFICATIONS` + 3-state Location Settings, dan `[M-01]` pemisahan query `isDraft = 0` + akses ulang Draft.
2. **Batch 2 (Dark Mode, Responsiveness 360dp/1.3x & UX Feedback):** Fix `[H-07]` token `onSoftIceTint` Dark Mode, `[M-03]` clipping 360dp/1.3x + Pin 1 margin, `[H-04]` anti double-tap + spinner cuaca, `[H-05]` `HapticFeedback`, dan `[M-04]` banner 0 baju bersih.
3. **Batch 3 (Transisi, Algoritma & Lint Polish):** Fix `[H-06]` transisi `NavHost`, `[M-02]` `AnimatedContent` shuffle + `animateItem()`, `[M-05]` skor `suitableWeathers` + bobot bahan di Laundry, `[M-06]` kamera `FileProvider` pada tombol Ulang, serta `[P-01]` pembersihan seluruh Lint warning kode sumber.
