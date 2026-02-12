package com.loanfinancial.lofi.ui.features.loan.model

/**
 * Data class representing Terms and Conditions content
 */
data class LoanTnCData(
    val title: String = "Syarat dan Ketentuan Pengajuan Pinjaman",
    val sections: List<TnCSection> = defaultTnCSections(),
    val agreementText: String = "Saya telah membaca dan menyetujui seluruh syarat dan ketentuan yang berlaku",
) {
    companion object {
        fun defaultTnCSections(): List<TnCSection> =
            listOf(
                TnCSection(
                    title = "1. Persyaratan Pengajuan Pinjaman",
                    content =
                        """
                        1.1. Pemohon wajib berusia minimal 21 tahun dan maksimal 60 tahun pada saat pengajuan.
                        1.2. Pemohon wajib memiliki penghasilan tetap minimal Rp 3.000.000 per bulan.
                        1.3. Pemohon wajib memiliki rekening bank aktif atas nama sendiri.
                        1.4. Pemohon wajib melampirkan dokumen KTP yang masih berlaku.
                        1.5. Pemohon wajib melampirkan foto selfie dengan KTP.
                        """.trimIndent(),
                ),
                TnCSection(
                    title = "2. Ketentuan Pinjaman",
                    content =
                        """
                        2.1. Plafon pinjaman mulai dari Rp 1.000.000 hingga Rp 500.000.000.
                        2.2. Jangka waktu pinjaman (tenor) tersedia dari 3 bulan hingga 60 bulan.
                        2.3. Suku bunga pinjaman adalah 12% per tahun (flat rate).
                        2.4. Biaya administrasi sebesar Rp 50.000 akan dikenakan sekali pada awal pinjaman.
                        2.5. Biaya provisi sebesar 1% dari plafon pinjaman akan dikenakan.
                        """.trimIndent(),
                ),
                TnCSection(
                    title = "3. Kewajiban Pembayaran",
                    content =
                        """
                        3.1. Pemohon wajib membayar angsuran tepat waktu sesuai jadwal yang ditentukan.
                        3.2. Pembayaran angsuran dapat dilakukan melalui transfer bank, virtual account, atau auto-debit.
                        3.3. Keterlambatan pembayaran akan dikenakan denda sesuai ketentuan yang berlaku.
                        3.4. Denda keterlambatan dihitung sebesar 0.1% per hari dari jumlah angsuran yang tertunggak.
                        """.trimIndent(),
                ),
                TnCSection(
                    title = "4. Pelunasan Dipercepat",
                    content =
                        """
                        4.1. Pemohon dapat melakukan pelunasan dipercepat kapan saja selama masa pinjaman.
                        4.2. Biaya pelunasan dipercepat dikenakan sebesar 2% dari sisa pokok pinjaman.
                        4.3. Pengajuan pelunasan dipercepat wajib dilakukan minimal 3 hari kerja sebelumnya.
                        """.trimIndent(),
                ),
                TnCSection(
                    title = "5. Verifikasi dan Persetujuan",
                    content =
                        """
                        5.1. Bank berhak melakukan verifikasi data dan informasi yang diberikan oleh pemohon.
                        5.2. Bank berhak menolak pengajuan pinjaman tanpa kewajiban memberikan alasan.
                        5.3. Keputusan persetujuan pinjaman menjadi wewenang penuh Bank.
                        5.4. Bank dapat meminta dokumen tambahan jika diperlukan.
                        """.trimIndent(),
                ),
                TnCSection(
                    title = "6. Pernyataan dan Jaminan",
                    content =
                        """
                        6.1. Pemohon menyatakan bahwa seluruh informasi yang diberikan adalah benar dan akurat.
                        6.2. Pemohon bertanggung jawab penuh atas kebenaran data yang disampaikan.
                        6.3. Pemohon memberikan persetujuan untuk pemeriksaan data ke BI Checking dan SLIK OJK.
                        6.4. Pemohon memahami bahwa pemalsuan dokumen merupakan tindak pidana.
                        """.trimIndent(),
                ),
                TnCSection(
                    title = "7. Privasi dan Data",
                    content =
                        """
                        7.1. Bank menjaga kerahasiaan data pribadi pemohon sesuai peraturan perundangan.
                        7.2. Bank dapat menggunakan data pemohon untuk keperluan verifikasi dan marketing.
                        7.3. Pemohon dapat mengajukan permintaan penghapusan data sesuai ketentuan yang berlaku.
                        """.trimIndent(),
                ),
                TnCSection(
                    title = "8. Lain-lain",
                    content =
                        """
                        8.1. Syarat dan ketentuan ini dapat diubah sewaktu-waktu oleh Bank.
                        8.2. Perubahan akan diinformasikan melalui aplikasi atau media komunikasi resmi.
                        8.3. Hukum yang berlaku adalah hukum Republik Indonesia.
                        8.4. Sengketa akan diselesaikan secara musyawarah atau melalui lembaga perbankan terkait.
                        """.trimIndent(),
                ),
            )
    }
}

/**
 * Individual TnC section
 */
data class TnCSection(
    val title: String,
    val content: String,
)
