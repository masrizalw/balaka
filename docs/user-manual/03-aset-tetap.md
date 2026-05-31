# Aset Tetap

Panduan pencatatan dan depresiasi aset tetap sesuai regulasi Indonesia.

## Konsep Depresiasi

### Apa Itu Depresiasi?

Depresiasi (penyusutan) adalah alokasi biaya perolehan aset tetap selama masa manfaatnya. Aset tetap seperti komputer, kendaraan, dan peralatan kehilangan nilai seiring waktu karena pemakaian.

### Mengapa Depresiasi Penting?

1. **Matching principle** - Biaya aset dicocokkan dengan pendapatan yang dihasilkan
2. **Pajak** - Beban penyusutan mengurangi penghasilan kena pajak
3. **Nilai aset akurat** - Neraca menunjukkan nilai buku yang realistis

### Metode Depresiasi

| Metode | Rumus | Karakteristik |
|--------|-------|---------------|
| **Garis Lurus** | (Harga Perolehan - Nilai Residu) / Masa Manfaat | Beban sama setiap tahun |
| **Saldo Menurun** | Nilai Buku × Tarif | Beban besar di awal, mengecil |

**Regulasi Indonesia (PMK 96/PMK.03/2009):**
- Kelompok 1 (4 tahun): 25% garis lurus, 50% saldo menurun
- Kelompok 2 (8 tahun): 12.5% garis lurus, 25% saldo menurun
- Kelompok 3 (16 tahun): 6.25% garis lurus, 12.5% saldo menurun
- Kelompok 4 (20 tahun): 5% garis lurus, 10% saldo menurun

---

## Kategori Aset

### Melihat Kategori Aset

Buka menu **Aset Tetap** > **Kategori**.

![Daftar Kategori Aset](screenshots/asset-categories-list.png)

### Kategori Standar (dari Seed Data)

| Kategori | Kelompok | Masa Manfaat | Metode |
|----------|----------|--------------|--------|
| Komputer & Laptop | 1 | 4 tahun | Garis Lurus |
| Peralatan Kantor | 1 | 4 tahun | Garis Lurus |
| Kendaraan Roda 2 | 1 | 4 tahun | Garis Lurus |
| Kendaraan Roda 4 | 2 | 8 tahun | Garis Lurus |
| Perabotan | 2 | 8 tahun | Garis Lurus |
| Bangunan Permanen | 4 | 20 tahun | Garis Lurus |

### Menambah Kategori Baru

1. Klik **Kategori Baru**
2. Isi:
   - Nama kategori
   - Kelompok pajak (1-4)
   - Masa manfaat (tahun)
   - Metode depresiasi
3. Klik **Simpan**

---

## Pencatatan Aset

### Melihat Daftar Aset

Buka menu **Aset Tetap** > **Daftar Aset**.

![Daftar Aset Tetap](screenshots/assets-list.png)

Kolom yang ditampilkan:
- Kode aset
- Nama aset
- Kategori
- Tanggal perolehan
- Harga perolehan
- Nilai buku saat ini
- Status

### Menambah Aset Baru

1. Klik **Aset Baru**

![Form Aset Tetap](screenshots/assets-form.png)

2. Isi data aset:

| Field | Keterangan |
|-------|------------|
| Kode Aset | Kode unik (contoh: AST-2025-001) |
| Nama Aset | Deskripsi aset |
| Kategori | Pilih dari dropdown |
| Sumber Dana Pembelian | **Wajib.** Akun bank/kas (jika dibayar tunai) atau hutang vendor (jika kredit) yang akan dikreditkan pada jurnal perolehan. Combobox — ketik kode/nama akun, pilih dari hasil. |
| Posting penyusutan otomatis | *Opsional, default tidak dicentang.* Jika dicentang, scheduler bulanan langsung mem-posting jurnal penyusutan; jika tidak, jurnal penyusutan tetap **PENDING** untuk persetujuan staf akuntansi. |
| Tanggal Perolehan | Tanggal pembelian |
| Harga Perolehan | Nilai beli (termasuk biaya instalasi jika ada) |
| Nilai Residu | Estimasi nilai jual akhir (biasanya 0 untuk pajak) |
| Lokasi | Penempatan aset |
| Keterangan | Catatan tambahan (nomor seri, vendor, dll) |

3. Klik **Simpan**

### Jurnal yang Terbentuk (saat pembelian)

Form aset membuat jurnal perolehan sebagai **DRAFT** otomatis lewat template "Pembelian Aset Tetap". Slot debit memakai akun aset dari kategori, slot kredit memakai **Sumber Dana Pembelian** yang dipilih operator.

```
Dr. Aset Tetap - [Kategori]    xxx
    Cr. [Sumber Dana Pembelian]    xxx
```

Status awal: **DRAFT** — staf akuntansi membuka transaksi dan klik **Posting** setelah verifikasi. Tidak ada posting otomatis pada saat pencatatan aset; pemisahan ini menjaga kontrol internal antara operasional (mencatat) dan akuntansi (mem-posting). Konsep yang sama berlaku untuk invoice, tagihan vendor, dan transaksi inventory — lihat [DRAFT-by-default](#draft-by-default) di bab Faktur & Tagihan.

---

## Jadwal Depresiasi

### Melihat Jadwal Depresiasi

Pada halaman detail aset, klik tab **Depresiasi**.

![Jadwal Depresiasi Aset](screenshots/assets-depreciation.png)

Tabel menampilkan:
- Bulan/tahun
- Beban penyusutan periode
- Akumulasi penyusutan
- Nilai buku akhir

### Proses Depresiasi Bulanan

Scheduler bulanan menghitung depresiasi otomatis. Jurnal yang dibuat:

```
Dr. Beban Penyusutan              xxx
    Cr. Akumulasi Penyusutan          xxx
```

Status entri ditentukan oleh flag **Posting penyusutan otomatis** pada aset:

- Centang dicentang → scheduler langsung mem-posting (status **POSTED**), saldo GL berubah pada hari scheduler berjalan.
- Centang tidak dicentang (default) → entri dibuat dengan status **PENDING**, menunggu staf akuntansi review. Status PENDING tidak mempengaruhi laporan keuangan.

Pendekatan default (PENDING) sesuai prinsip [DRAFT-by-default](16-faktur-tagihan.md#draft-by-default): operasi mencatat, akuntansi yang mem-posting. Centang opt-in dipakai untuk aset yang nilainya stabil dan tidak perlu review tiap bulan.

### Contoh Perhitungan (Garis Lurus)

| Data | Nilai |
|------|-------|
| Harga Perolehan | Rp 12.000.000 |
| Nilai Residu | Rp 0 |
| Masa Manfaat | 4 tahun |

**Depresiasi per tahun:** Rp 12.000.000 / 4 = Rp 3.000.000
**Depresiasi per bulan:** Rp 3.000.000 / 12 = Rp 250.000

---

## Laporan Penyusutan

### Mengakses Laporan

Buka menu **Laporan** > **Laporan Penyusutan**.

![Laporan Penyusutan](screenshots/reports-depreciation.png)

### Komponen Laporan

| Kolom | Keterangan |
|-------|------------|
| Nama Aset | Nama dan kode aset |
| Kategori | Kategori aset |
| Tgl Perolehan | Tanggal pembelian |
| Harga Perolehan | Nilai beli |
| Masa Manfaat | Umur ekonomis |
| Metode | Garis Lurus/Saldo Menurun |
| Penyusutan Tahun Ini | Beban tahun berjalan |
| Akum. Penyusutan | Total penyusutan sejak pembelian |
| Nilai Buku | Harga perolehan - Akum. penyusutan |

### Ekspor untuk SPT

Klik **Cetak** untuk menghasilkan laporan format Lampiran Khusus 1A SPT Tahunan Badan.

---

## Pelepasan Aset (Disposal)

### Kapan Dilakukan?

- Aset dijual
- Aset rusak/tidak terpakai
- Aset dihapus dari pembukuan

### Cara Mencatat Pelepasan

1. Buka detail aset
2. Klik **Lepas Aset**
3. Isi:
   - Tanggal pelepasan
   - Alasan (Dijual/Rusak/Lainnya)
   - Nilai jual (jika dijual)
4. Klik **Proses**

### Jurnal Pelepasan (jika dijual)

```
Dr. Kas/Bank                      xxx  (nilai jual)
Dr. Akumulasi Penyusutan          xxx  (akum. s.d. tanggal jual)
Dr/Cr. Laba/Rugi Penjualan Aset   xxx  (selisih)
    Cr. Aset Tetap                    xxx  (harga perolehan)
```

---

## Regulasi Indonesia

### Kelompok Aset menurut PMK 96/PMK.03/2009

**Kelompok 1 (Masa Manfaat 4 tahun):**
- Mebel dan peralatan dari kayu/rotan
- Mesin kantor (komputer, mesin ketik)
- Perlengkapan komunikasi
- Kendaraan roda 2

**Kelompok 2 (Masa Manfaat 8 tahun):**
- Mebel dan peralatan dari logam
- Kendaraan roda 4
- Mesin produksi ringan

**Kelompok 3 (Masa Manfaat 16 tahun):**
- Mesin produksi berat
- Peralatan pabrik

**Kelompok 4 (Masa Manfaat 20 tahun):**
- Bangunan permanen
- Konstruksi

### Tips untuk Pajak

1. **Dokumentasi lengkap** - Simpan faktur pembelian dan bukti pembayaran
2. **Konsisten** - Gunakan metode yang sama untuk aset sejenis
3. **Review tahunan** - Periksa apakah ada aset yang perlu dihapus

---

## Lihat Juga

- [Tutorial Akuntansi](02-tutorial-akuntansi.md) - Konsep penyesuaian
- [Perpajakan](04-perpajakan.md) - PPh dan deductibility
- [Referensi Amortisasi](12-lampiran-amortisasi.md) - Tabel masa manfaat
