package com.example.voltix.data.repository

import android.util.Log
import com.example.voltix.data.dao.RuanganDAO // Pastikan RuanganDAO di-inject
import com.example.voltix.data.dao.RuanganPerangkatCrossRefDAO // Opsional, jika dibutuhkan untuk detail waktu
import com.example.voltix.data.entity.RuanganPerangkatCrossRef // Opsional
import com.example.voltix.data.entity.RuanganWithPerangkat
import com.example.voltix.data.remote.dto.UserData // DTO User dari backend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_DASH_REPO = "DashboardRepository"

@Singleton
class DashboardRepository @Inject constructor(
    private val ruanganDao: RuanganDAO, // Inject RuanganDAO yang sudah diupdate
    // Inject RuanganPerangkatCrossRefDAO jika Anda memerlukannya secara eksplisit untuk waktu nyala/mati
    // private val crossRefDao: RuanganPerangkatCrossRefDAO
) {

    /**
     * Mengambil dan mengkalkulasi data dashboard untuk pengguna tertentu.
     * @param userData Objek UserData dari backend yang berisi firebaseUid pengguna.
     * @return Flow<DashboardData>
     */
    fun getDashboardDataForUser(userData: UserData): Flow<DashboardData> {
        Log.d(TAG_DASH_REPO, "Fetching dashboard data from local Room for user UID: ${userData.firebaseUid}")

        // Panggil metode DAO baru yang sudah difilter berdasarkan userFirebaseUid
        return ruanganDao.getAllRuanganWithPerangkatForUser(userData.firebaseUid)
            .map { ruanganListMilikUser -> // Ini adalah List<RuanganWithPerangkat> milik user
                Log.d(TAG_DASH_REPO, "User has ${ruanganListMilikUser.size} rooms with perangkat from local DB.")
                val costPerKWh = 1444.70f // IDR per kWh (sesuaikan jika perlu)
                var totalDevices = 0
                val hourlyPower = MutableList(24) { 0f } // Power dalam Watt per jam

                ruanganListMilikUser.forEach { ruanganWithPerangkat ->
                    // ruanganWithPerangkat adalah RuanganWithPerangkat (RuanganEntity & List<PerangkatEntity>)
                    ruanganWithPerangkat.perangkatList.forEach { perangkat ->
                        totalDevices += perangkat.jumlah // Asumsi PerangkatEntity punya field 'jumlah'

                        // PENTING: Mendapatkan waktuNyala dan waktuMati untuk kalkulasi hourlyPower
                        // Ini adalah bagian yang paling krusial dan bergantung pada bagaimana Anda menyimpan
                        // dan mengambil data relasi antara ruangan dan perangkat beserta waktunya.
                        //
                        // OPSI 1: Jika RuanganWithPerangkat Anda sudah dimodifikasi agar setiap PerangkatEntity
                        // di dalamnya memiliki informasi waktuNyala dan waktuMati (misalnya melalui @Relation dengan Junction).
                        // Jika ya, Anda bisa akses langsung.
                        //
                        // OPSI 2: Jika tidak, Anda perlu mengambilnya dari RuanganPerangkatCrossRefDAO secara manual.
                        // Ini memerlukan ID lokal RuanganEntity dan ID lokal PerangkatEntity.
                        // Contoh jika perlu query manual (membutuhkan crossRefDao di-inject):
                        // val crossRef = crossRefDao.getCrossRef(ruanganWithPerangkat.ruangan.id, perangkat.id)
                        // val waktuNyala = crossRef?.waktuNyala ?: LocalTime.of(6,0) // Default jika null
                        // val waktuMati = crossRef?.waktuMati ?: LocalTime.of(22,0) // Default jika null

                        // Untuk contoh ini, kita akan gunakan default, TAPI ANDA HARUS MENYESUAIKANNYA!
                        // Asumsikan Anda akan mengimplementasikan cara mendapatkan waktuNyala & waktuMati yang benar.
                        // Misalnya, Anda bisa menambahkan field waktuNyala & waktuMati ke PerangkatEntity
                        // saat di-query dalam konteks RuanganWithPerangkat jika memungkinkan.
                        // Atau, query crossRef secara terpisah.

                        // Placeholder untuk waktuNyala dan waktuMati (HARUS DIGANTI DENGAN LOGIKA YANG BENAR)
                        // Jika Anda tidak punya cara mendapatkan waktu spesifik per perangkat di ruangan,
                        // kalkulasi hourlyPower tidak akan akurat.
                        // Untuk sekarang, kita pakai default seperti di kode lama Anda:
                        val waktuNyala = LocalTime.of(6, 0)
                        val waktuMati = LocalTime.of(22, 0)
                        // CATATAN: Kode lama Anda mengambil crossRef:
                        // val crossRef = perangkatDao.getCrossRef(ruangan.ruangan.id, perangkat.id) ?: RuanganPerangkatCrossRef(...)
                        // Anda perlu memastikan `getCrossRef` ini ada di DAO yang sesuai (misal RuanganPerangkatCrossRefDAO)
                        // dan dipanggil dengan benar di sini jika ingin data waktu yang akurat.

                        val powerPerUnitPerangkat = perangkat.daya * perangkat.jumlah // Watt total untuk perangkat ini
                        val startHour = waktuNyala.hour
                        val endHour = waktuMati.hour

                        if (startHour < endHour) {
                            for (hour in startHour until endHour) {
                                hourlyPower[hour] += powerPerUnitPerangkat.toFloat()
                            }
                        } else if (startHour > endHour) { // Melewati tengah malam
                            for (hour in startHour until 24) {
                                hourlyPower[hour] += powerPerUnitPerangkat.toFloat()
                            }
                            for (hour in 0 until endHour) {
                                hourlyPower[hour] += powerPerUnitPerangkat.toFloat()
                            }
                        } else if (startHour == endHour && (startHour != 0 || endHour != 0)) {
                            // Jika sama tapi bukan tengah malam, anggap nyala 24 jam (atau 0 jam, tergantung definisi)
                            // Untuk contoh, anggap 24 jam jika start == end dan bukan 00:00
                            if (startHour != 0 || endHour != 0) { // Jika bukan 00:00 ke 00:00
                                for (hour in 0 until 24) {
                                    hourlyPower[hour] += powerPerUnitPerangkat.toFloat()
                                }
                            }
                        }
                        // Pertimbangkan kasus di mana startHour == endHour (misalnya nyala 24 jam atau tidak sama sekali)
                    }
                }

                val totalWhPerDay = hourlyPower.sum()
                val totalKWhPerDay = totalWhPerDay / 1000f
                val totalCost = totalKWhPerDay * costPerKWh

                Log.d(TAG_DASH_REPO, "Calculated User-Specific DashboardData: totalDevices=$totalDevices, totalKWhPerDay=$totalKWhPerDay, totalCost=$totalCost")

                DashboardData(
                    totalDevices = totalDevices,
                    totalPower = totalKWhPerDay,
                    totalCost = totalCost,
                    hourlyPower = hourlyPower
                )
            }
    }
}