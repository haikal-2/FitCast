# FitCast (CuacaGaya) — Android & UI/UX Agent Rules

> Aturan workspace ini disinkronkan dengan `.agent/rules/fitcast-rules.md` dan mengaktifkan seluruh skill lokal di `.agent/skills/`.

## 1. Mandatory Skill Activation
Selalu rujuk dan terapkan pedoman dari skill yang terpasang di `.agent/skills/` sebelum menulis atau memodifikasi kode:
- **android-skills** (`.agent/skills/android-skills/SKILL.md`): Gunakan standar resmi Jetpack Compose, Lifecycle, State Hoisting (`StateFlow` + `collectAsStateWithLifecycle`), Room Database, dan WorkManager.
- **ui-ux-pro-max** (`.agent/skills/ui-ux-pro-max/SKILL.md`): Terapkan hierarki visual editorial modern, Bento Grid layout, kontras tipografi yang tegas, micro-interactions halus, serta aksesibilitas touch target minimal `48dp`.
- **mobile-skill & mobile-dev-skills** (`.agent/skills/mobile-skill/SKILL.md`, `.agent/skills/mobile-dev-skills/SKILL.md`): Pastikan manajemen memori gambar optimal, penanganan izin runtime (`Location`, `Camera`, `Notifications`) aman dari crash, dan dukungan offline-first 100%.

## 2. Identitas & Arsitektur Proyek
- **Nama Aplikasi**: FitCast (CuacaGaya)
- **Package Name**: `com.fitcast.cuacagaya`
- **Tech Stack**: Kotlin 100%, Jetpack Compose (Material 3), MVVM + Repository Pattern, Room Database, Retrofit + Gson (OpenWeatherMap API), ML Kit Subject Segmentation, Android Palette API, Coil, WorkManager.
- **Aturan Keamanan**: Jangan pernah melakukan hardcode API Key di dalam kode sumber; selalu ambil dari `BuildConfig.WEATHER_API_KEY`.

## 3. Design System & UI/UX Specification
Rujuk selalu spesifikasi desain lengkap pada `DESIGN.md` di root proyek. Gunakan estetika modern fashion-tech editorial dengan palet warna berikut:
- **Volt Lime (`#BCFF5E`)**: Primary CTA buttons (*"Pakai Outfit Ini"*, *"Simpan ke Lemari"*), indikator tab aktif pada floating navigation bar, dan aksen interaktif utama.
- **Obsidian Black (`#181A1B`)**: Latar Splash Screen, kontainer floating bottom bar, kartu statistik sekunder, dan teks utama ber-kontras tinggi.
- **Graphite Slate (`#292B2F`)**: Permukaan kartu pada Dark Mode dan badge status gelap.
- **Electric Cobalt (`#0366FF`)**: Kartu statistik utama (*Total Baju*), badge cuaca/hujan, dan indikator fitur AI ML Kit.
- **Canvas Off-White (`#F6F7F9`) & Pure White (`#FFFFFF`)**: Latar kanvas utama Light Mode dan permukaan kartu dengan corner radius `20dp`–`28dp`.
- **Komponen Khas**:
  - Baris statistik 3 kolom (*Total Baju*, *Siap Pakai*, *Laundry*).
  - Hero Card *"Personalized Daily Outfit"* dengan badge suhu cuaca & tombol Shuffle bulat.
  - Grid katalog pakaian 2 kolom dengan preview bersih tanpa background dan titik indikator warna dominan (*color swatch*).
  - Floating pill Bottom Navigation Bar (`#181A1B`) dengan lingkaran aktif `#BCFF5E`.

## 4. Standar Kualitas Kode & Anti-Crash
- Setiap layar (`HomeScreen`, `WardrobeScreen`, `AddItemScreen`, `LaundryScreen`, `OnboardingScreen`) wajib mendukung Light Mode dan Dark Mode tanpa warna hardcoded yang bertabrakan.
- Selalu sediakan fallback offline (dari `weather_cache`) dan pemilihan kota manual apabila koneksi internet terputus atau izin GPS ditolak pengguna.
- Gunakan `@Preview` Compose yang rapi dan bebas dari error kompilasi sebelum menyelesaikan tugas.
