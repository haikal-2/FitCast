# DESIGN.md — FitCast (CuacaGaya) Mobile App

## 1. Design Direction & Aesthetic
- **Theme**: Editorial Fashion-Tech Minimalist + High-Contrast Bento Dashboard.
- **Visual Identity**: Memadukan kebersihan kanvas off-white ala katalog fashion modern dengan kontras tajam warna Obsidian Black, Electric Cobalt untuk indikator cuaca/AI, dan Volt Lime sebagai penarik perhatian utama pada tombol aksi (CTA) dan status aktif.
- **Target Viewport**: Mobile Portrait (`390 x 844 px` / Android `360 x 800 dp`), safe-area aware, Material Design 3 compatible.

## 2. Design Tokens

### Color Palette
| Token | Hex | Role |
| :--- | :--- | :--- |
| `--color-volt-lime` | `#BCFF5E` | Primary CTA button, active bottom-nav indicator, highlight badge |
| `--color-obsidian` | `#181A1B` | Splash background, floating nav bar, secondary stat card, primary text |
| `--color-graphite` | `#292B2F` | Dark mode card surface, secondary dark badges, subtle dark borders |
| `--color-cobalt` | `#0366FF` | Primary stat card (Total Baju), weather/rain indicators, ML Kit AI badge |
| `--color-canvas` | `#F6F7F9` | Main app background (Light Mode) |
| `--color-surface` | `#FFFFFF` | Primary bento cards, product detail sheet |
| `--color-card-neutral` | `#EFEFF1` | Garment photo backdrop (for ML Kit background-removed assets) |
| `--color-text-muted` | `#6B7280` | Metadata, secondary labels, subtitles |
| `--color-danger` | `#EF4444` | Dirty/laundry warning dot, heavy rain alert accent |

### Typography
- **Font Family**: `Plus Jakarta Sans` (Fallback: `Inter`, `Roboto`).
- **Display / Temp**: `36px`, Weight `800`, Line-height `1.0`, Tracking `-0.03em`.
- **Brand Wordmark**: `22px`, Weight `900`, Uppercase, Tracking `0.12em` (`FITCAST`).
- **Section Heading**: `18px`, Weight `700`.
- **Card Title**: `15px`, Weight `600`.
- **Body / Weather Advice**: `13px`, Weight `400`, Line-height `1.45`.
- **Micro Label / Chip**: `11px`, Weight `600`, Uppercase / Pill.

### Spacing, Radius & Elevation
- **Base Grid**: `4px` (`8, 12, 16, 20, 24px`).
- **Screen Padding**: `16px` horizontal.
- **Radius Scale**:
  - Hero / Editorial Preview Card: `28px`
  - Bento Stat Cards: `22px`
  - Wardrobe Grid Cards: `18px`
  - Floating Pill Nav & Buttons: `999px` (Full Pill)
- **Shadows**: Soft ambient elevation `0 10px 28px rgba(24, 26, 27, 0.08)` untuk kartu putih dan `0 14px 30px rgba(24, 26, 27, 0.28)` untuk *floating bottom navigation*.

## 3. Screen Architecture (5 Core Screens)

### Screen 1: Splash Screen
- Full-bleed Obsidian Black (`#181A1B`) dengan gradasi gelombang kain gelap (*silk monochrome wave*).
- Tipografi putih tebal di tengah: **FITCAST** dengan sub-label kecil `CUACAGAYA`.

### Screen 2: Home Dashboard (`Beranda`)
- **Top Header**: Wordmark `FITCAST` di kiri, *pill* pemilih kota (`Bandung • GPS`) dan tombol info di kanan.
- **Style Filter Row**: Horizontal chips (`Semua`, `Casual`, `Formal`, `Sport`).
- **Bento Stats Row (3 Columns)**:
  1. Cobalt Card (`#0366FF`, text white): `Total Baju` + ikon gantungan.
  2. Obsidian Card (`#181A1B`, text white): `Siap Pakai` + ikon centang.
  3. Surface Card (`#FFFFFF`, text dark): `Laundry` + ikon mesin cuci.
- **Hero Split Card — "Personalized Daily Outfit"**:
  - Kolom Kiri: Judul tebal *"Personalized daily outfit"*, *weather pill* (`24°C • Berawan • Hujan 60%`), dan tombol bulat `#181A1B` untuk *Shuffle* (Acak).
  - Kolom Kanan: Visual kombinasi pakaian berlapis (Atasan + Outer + Bawahan) di atas latar `#EFEFF1`.
- **Weather Alert & Primary CTA**: Banner ringkas peringatan payung/jas hujan + tombol penuh *Volt Lime* (`#BCFF5E`) bertuliskan **"Pakai Outfit Ini"**.

### Screen 3: Mix & Match Studio (`Outfit Detail`)
- **Full-Bleed Rounded Editorial Canvas (`28px`)**: Menampilkan pratinjau penuh setelan outfit dengan **Interactive Hotspot Pins** (titik putih berdenyut + label teks mengambang) yang menunjuk langsung ke Atasan, Outer, dan Bawahan.
- **Right Floating Controls**: 3 tombol bulat vertikal (*Lock Item*, *Shuffle*, *Simpan Favorit*).
- **Bottom Dark Action Sheet (`#181A1B`)**: Nama kombinasi, label harmoni warna (`Harmoni Netral`), tombol pill `#BCFF5E` (**Konfirmasi Pakai**), dan *carousel* thumbnail bulat item lemari untuk *swipe-to-swap*.

### Screen 4: Digital Wardrobe (`Lemari`)
- **Search & Quick Filter**: Kolom pencarian kapsul + filter kategori horizontal (`Semua`, `Atasan`, `Bawahan`, `Outer`, `Sepatu`).
- **High-Contrast Banner (`#181A1B`)**: Promo fitur AI lokal *"Auto Remove-BG & Color Detection (ML Kit)"* dengan tombol *pill* putih **+ Tambah Baju**.
- **2-Column Editorial Grid**: Kartu baju dengan foto di latar `#EFEFF1`, indikator status bersih/kotor di sudut, nama pakaian, **Color Swatch Dot** hasil `Palette API`, dan rasio pemakaian (`Pakai 1/2`).

### Screen 5: Laundry Tracker (`Laundry`)
- Header dengan jumlah item kotor & tombol teks **Cuci Semua**.
- Daftar kartu pakaian kotor dengan indikator jumlah pemakaian dan tombol pill `#BCFF5E` **Selesai Cuci**.

## 4. Navigation Component
- **Floating Bottom Pill Bar**: Lebar `calc(100% - 32px)`, tinggi `64px`, radius `999px`, latar `#181A1B` melayang `16px` di atas tepi bawah.
- **3 Tab Utama**: `Beranda`, `Studio`, `Lemari`, dan `Laundry` (dengan badge merah jika ada baju kotor).
- **Active State**: Ikon dibungkus lingkaran/kapsul `#BCFF5E` dengan ikon `#181A1B`.
