# Faktur & Tagihan

Panduan lengkap untuk pengelolaan faktur (invoice) ke klien, tagihan dari vendor, pelacakan pembayaran, laporan umur piutang/hutang, dan laporan per klien/vendor.

## DRAFT-by-default

Balaka memisahkan **siapa mencatat** dari **siapa mem-posting ke buku besar**. Form-form di luar modul jurnal manual (faktur, tagihan vendor, perolehan aset, penyusutan, penyelesaian produksi BOM, transaksi inventory) tidak pernah memposting jurnal langsung. Mereka membuat **jurnal DRAFT** lewat template yang sudah dikonfigurasi, kemudian staf akuntansi membuka jurnal tersebut dan klik **Posting** setelah review.

```
Operasional                Akuntansi
  Kirim faktur     ──►     Buka DRAFT
  Setujui tagihan  ──►     Verifikasi
  Catat aset       ──►     Klik "Posting"
                                │
                                ▼
                          Saldo GL berubah
```

Konsekuensi praktis:

- Status jurnal yang baru dibuat selalu **DRAFT** — tidak mempengaruhi laporan keuangan sampai diposting.
- Bila jurnal salah, staf akuntansi cukup hapus DRAFT (atau kirim balik komentar) tanpa perlu jurnal koreksi.
- Beberapa fitur menyediakan opt-in (`autoPost`) untuk skenario tertentu — misal aset tetap dengan penyusutan stabil. Default tetap DRAFT.

Prasyarat: empat slot **Akun Posting (Jurnal Otomatis)** harus terisi di Pengaturan Perusahaan — Piutang, Hutang, PPN Keluaran, PPN Masukan. Lihat [Akun Posting Jurnal Otomatis](01-setup-awal.md#akun-posting-jurnal-otomatis).

---

## Faktur (Invoice)

### Konsep

Faktur adalah dokumen penagihan yang dikirim ke klien setelah pekerjaan selesai. Setiap faktur memiliki line item (rincian jasa/barang), tanggal jatuh tempo, dan status yang berubah sesuai alur kerja.

### Alur Kerja Faktur

```
DRAFT → SENT → PARTIAL → PAID
                  ↑          ↑
              (sebagian)  (lunas)
```

| Status | Keterangan |
|--------|------------|
| DRAFT | Baru dibuat, bisa diedit |
| SENT | Sudah dikirim ke klien, menunggu pembayaran |
| PARTIAL | Sebagian dibayar |
| PAID | Lunas |
| OVERDUE | Melewati tanggal jatuh tempo |

### Membuat Faktur

Buka menu **Proyek** > **Faktur** > **Faktur Baru**.

Isi data faktur:
- **Klien:** Combobox — ketik kode/nama klien, pilih dari hasil pencarian (maksimum 10).
- **Tanggal Faktur:** Tanggal penerbitan.
- **Tanggal Jatuh Tempo:** Batas waktu pembayaran.
- **Proyek** (opsional): Kaitkan ke proyek tertentu.

Tambahkan line item:
- **Produk/Jasa:** Combobox — ketik kode/nama produk, pilih dari hasil. Pilihan ini menentukan akun pendapatan yang dipakai pada DRAFT jurnal pengakuan pendapatan (lihat `Sales Account` pada master Produk).
- **Deskripsi:** Keterangan tambahan (terisi otomatis dari nama produk, bisa diubah).
- **Quantity:** Jumlah.
- **Harga Satuan:** Harga per unit (terisi otomatis dari Harga Jual produk jika kosong).
- **PPN %:** Tarif PPN baris (kosong = tanpa PPN).
- **Total:** Dihitung otomatis (qty × harga satuan + PPN).

Klik **Simpan** untuk menyimpan sebagai DRAFT. Pada tahap ini belum ada jurnal — faktur masih bisa diedit.

![Faktur Baru](screenshots/10-invoice-created.png)

### Mengirim Faktur

Dari halaman detail faktur, klik **Kirim**:

1. Status faktur berubah dari DRAFT ke SENT — faktur tidak bisa diedit lagi.
2. Sistem membuat **jurnal DRAFT** lewat template "Pengakuan Pendapatan Invoice". Jurnal mengikuti pola R2: satu DRAFT per akun pendapatan yang muncul di line items (line yang share `Sales Account` digabung jadi satu jurnal).
3. Jurnal tetap berstatus DRAFT — staf akuntansi membuka dan mem-posting setelah review.

```
Dr. Piutang Usaha                     xxx        (dari Akun Posting → Piutang Usaha)
    Cr. Pendapatan [per produk]           xxx    (dari Produk.Sales Account)
    Cr. PPN Keluaran                      xxx    (dari Akun Posting → PPN Keluaran, jika ada)
```

Faktur masuk ke laporan umur piutang segera setelah dikirim, terlepas dari status jurnal-nya. Jurnal yang masih DRAFT tidak mempengaruhi saldo GL — jadi laporan keuangan baru berubah setelah staf akuntansi mem-posting.

![Faktur Terkirim](screenshots/10-invoice-sent.png)

---

## Tagihan Vendor (Bill)

### Konsep

Tagihan (bill) adalah dokumen yang diterima dari vendor untuk pembelian barang atau jasa. Alur kerjanya mirip faktur, tapi dari sisi pengeluaran.

### Alur Kerja Tagihan

```
DRAFT → APPROVED → PARTIAL → PAID
                      ↑          ↑
                  (sebagian)  (lunas)
```

| Status | Keterangan |
|--------|------------|
| DRAFT | Baru dibuat, menunggu approval |
| APPROVED | Disetujui, menunggu pembayaran |
| PARTIAL | Sebagian dibayar |
| PAID | Lunas |
| OVERDUE | Melewati tanggal jatuh tempo |

### Membuat Tagihan

Buka menu **Pembelian** > **Tagihan** > **Tagihan Baru**.

Isi data:
- **Vendor:** Combobox — ketik kode/nama vendor, pilih dari hasil.
- **Tanggal Tagihan:** Tanggal penerbitan oleh vendor.
- **Tanggal Jatuh Tempo:** Batas pembayaran.
- **Nomor Referensi Vendor** (opsional): Nomor tagihan dari vendor.

Tambahkan line item:
- **Deskripsi:** Keterangan barang/jasa yang dibeli.
- **Akun Beban:** Combobox per baris — ketik kode/nama akun beban. Pilihan ini menentukan akun yang didebit pada DRAFT jurnal pengeluaran (sub-bagan dari kelas Beban / 5.x atau Aset / 1.x untuk pembelian persediaan).
- **Quantity, Harga Satuan, PPN %:** Sama seperti faktur.

Klik **Simpan** — tagihan tersimpan sebagai DRAFT, belum ada jurnal.

### Menyetujui Tagihan

Dari halaman detail tagihan, klik **Setujui**:

1. Status tagihan berubah dari DRAFT ke APPROVED.
2. Sistem membuat **jurnal DRAFT** lewat template "Tagihan Vendor". Pola R2 berlaku: satu DRAFT per akun beban yang muncul di line items.
3. Jurnal tetap DRAFT — staf akuntansi mem-posting setelah review.

```
Dr. [Akun Beban per line]              xxx     (dari BillLine.Akun Beban)
Dr. PPN Masukan                        xxx     (dari Akun Posting → PPN Masukan, jika ada)
    Cr. Hutang Usaha                       xxx (dari Akun Posting → Hutang Usaha)
```

![Tagihan Disetujui](screenshots/10-bill-approved.png)

Tagihan yang disetujui masuk ke laporan umur hutang segera, terlepas dari status jurnal-nya.

---

## Pelacakan Pembayaran

### Mencatat Pembayaran Faktur

Buka halaman detail faktur yang berstatus SENT, PARTIAL, atau OVERDUE. Klik **Catat Pembayaran** untuk membuka form pembayaran.

Isi data pembayaran:
- **Tanggal Pembayaran:** Tanggal dana diterima
- **Jumlah:** Nominal pembayaran (bisa sebagian)
- **Metode Pembayaran:** Transfer, Cash, Cek, Kartu Kredit, E-Wallet, Lainnya
- **Nomor Referensi:** Nomor bukti transfer/kuitansi
- **Catatan** (opsional): Keterangan tambahan

Klik **Simpan Pembayaran**.

**Pembayaran sebagian (partial):** Jika jumlah pembayaran kurang dari total faktur, status berubah ke PARTIAL. Sisa tagihan (balance due) ditampilkan di halaman detail.

![Pembayaran Sebagian](screenshots/10-invoice-partial-payment.png)

**Pembayaran lunas:** Jika total semua pembayaran sama dengan total faktur, status otomatis berubah ke PAID.

![Faktur Lunas](screenshots/10-invoice-paid.png)

### Mencatat Pembayaran Tagihan

Prosesnya sama dengan faktur. Buka halaman detail tagihan berstatus APPROVED, PARTIAL, atau OVERDUE, lalu catat pembayaran.

![Pembayaran Tagihan](screenshots/10-bill-payment.png)

### Riwayat Pembayaran

Setiap faktur/tagihan menampilkan tabel riwayat pembayaran di halaman detail:
- Tanggal pembayaran
- Jumlah
- Metode pembayaran
- Nomor referensi

### Validasi

- Pembayaran hanya bisa dicatat pada faktur/tagihan dengan status yang tepat
- Jumlah pembayaran tidak boleh melebihi sisa tagihan (overpayment ditolak)
- Total pembayaran + pembayaran baru <= total faktur/tagihan

---

## Laporan Umur Piutang & Hutang

### Konsep Aging

Laporan umur (aging report) mengelompokkan faktur/tagihan yang belum lunas berdasarkan berapa lama sudah jatuh tempo. Berguna untuk memantau risiko piutang tak tertagih dan prioritas pembayaran.

### Bucket Aging

| Bucket | Keterangan |
|--------|------------|
| Belum Jatuh Tempo | Belum melewati due date |
| 1-30 hari | Terlambat 1-30 hari |
| 31-60 hari | Terlambat 31-60 hari |
| 61-90 hari | Terlambat 61-90 hari |
| > 90 hari | Terlambat lebih dari 90 hari |

### Umur Piutang (Receivables Aging)

Buka menu **Laporan** > **Umur Piutang**.

![Laporan Umur Piutang](screenshots/10-aging-receivables-unpaid.png)

Menampilkan:
- **Ringkasan per bucket:** Total piutang per kategori umur
- **Tabel per klien:** Rincian saldo per klien di setiap bucket
- **Filter tanggal:** Pilih tanggal acuan (as-of date)

Setelah pembayaran sebagian, saldo di aging report berkurang sesuai jumlah yang sudah dibayar.

![Aging Setelah Pembayaran Sebagian](screenshots/10-aging-receivables-partial.png)

Setelah lunas, klien hilang dari laporan aging.

![Aging Setelah Lunas](screenshots/10-aging-receivables-cleared.png)

### Umur Hutang (Payables Aging)

Buka menu **Laporan** > **Umur Hutang**. Format sama dengan umur piutang, tapi untuk tagihan vendor.

![Laporan Umur Hutang](screenshots/10-aging-payables-unpaid.png)

---

## Laporan Klien & Vendor

### Konsep Statement

Laporan per klien/vendor (statement) menampilkan riwayat transaksi secara kronologis dengan saldo berjalan (running balance). Berguna untuk rekonsiliasi dengan klien/vendor dan verifikasi posisi piutang/hutang.

### Laporan Klien

Akses dari halaman detail klien: klik **Lihat Laporan**, atau langsung ke **Laporan** > **Laporan Klien**.

![Laporan Klien](screenshots/10-client-statement.png)

Informasi yang ditampilkan:
- **Periode:** Filter tanggal mulai dan akhir
- **Saldo Awal:** Total piutang sebelum periode
- **Tabel Transaksi:** Setiap baris menampilkan:
  - Tanggal
  - Tipe (Invoice atau Pembayaran)
  - Nomor referensi
  - Keterangan
  - Jumlah invoice / jumlah pembayaran
  - Saldo berjalan
- **Saldo Akhir:** Posisi piutang akhir periode
- **Cetak:** Buka versi cetak (print-friendly, A4 landscape)

### Laporan Vendor

Format sama dengan laporan klien, tapi menampilkan tagihan (bill) dan pembayaran ke vendor.

![Laporan Vendor](screenshots/10-vendor-statement.png)

Akses dari halaman detail vendor: klik **Lihat Laporan**.

---

## Contoh Alur Lengkap

Walk-through siklus penagihan dari awal sampai lunas.

### Langkah 1: Buat Faktur

Buat faktur untuk klien PT Telkom dengan 2 line item:
- Jasa Pengembangan Aplikasi: 1 x Rp 10.000.000
- Jasa Maintenance Bulanan: 5 x Rp 1.000.000
- **Total: Rp 15.000.000**

### Langkah 2: Kirim Faktur

Kirim faktur. Status berubah ke SENT. Faktur muncul di laporan umur piutang pada bucket "Belum Jatuh Tempo".

### Langkah 3: Terima Pembayaran Sebagian

Klien membayar Rp 5.000.000 via transfer. Status berubah ke PARTIAL. Sisa tagihan: Rp 10.000.000.

Di laporan aging, nominal berubah dari Rp 15.000.000 menjadi Rp 10.000.000.

### Langkah 4: Terima Pembayaran Final

Klien membayar sisa Rp 10.000.000. Status berubah ke PAID. Faktur hilang dari laporan aging.

### Langkah 5: Periksa Laporan Klien

Buka laporan klien PT Telkom. Terlihat:
1. Invoice Rp 15.000.000 → saldo naik
2. Pembayaran Rp 5.000.000 → saldo turun
3. Pembayaran Rp 10.000.000 → saldo menjadi 0

---

## Tips

1. **Kirim faktur segera** — Jangan biarkan faktur berlama-lama di status DRAFT. Semakin cepat dikirim, semakin cepat dibayar.
2. **Pantau aging mingguan** — Periksa laporan umur piutang minimal seminggu sekali. Piutang > 60 hari perlu tindakan aktif.
3. **Catat pembayaran saat diterima** — Jangan tunda pencatatan pembayaran agar saldo selalu akurat.
4. **Gunakan statement untuk rekonsiliasi** — Kirim statement ke klien secara berkala untuk memastikan kedua pihak sepakat tentang saldo.
5. **Setujui tagihan vendor tepat waktu** — Tagihan yang disetujui masuk ke aging, membantu perencanaan arus kas.

---

## Lihat Juga

- [Industri Jasa](07-industri-jasa.md) — Konteks invoice dalam bisnis jasa IT
- [Rekonsiliasi Bank](14-rekonsiliasi-bank.md) — Cocokkan pembayaran dengan mutasi bank
- [Tutorial Akuntansi](02-tutorial-akuntansi.md) — Jurnal piutang dan hutang
- [Peringatan](15-peringatan.md) — Alert otomatis untuk piutang overdue
