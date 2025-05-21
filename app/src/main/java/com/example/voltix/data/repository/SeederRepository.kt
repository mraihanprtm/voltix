package com.example.voltix.data.repository

import com.example.voltix.data.dao.GolonganListrikDao
import com.example.voltix.data.entity.BiayaPemakaianEntity
import com.example.voltix.data.entity.GolonganListrikEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeederRepository @Inject constructor(
    private val golonganListrikDao: GolonganListrikDao
) {
    private fun getBiayaBeban(batasDaya: Int): Int {
        return 40 * (batasDaya / 10) * 1352
    }
    suspend fun seedIfNeeded() {
        println("seeding databases..")
        if (golonganListrikDao.getAllGolonganListrik().isEmpty()) {
            println("Seeding GolonganListrikEntity..")
            golonganListrikDao.insertAllGolonganListrik(
                GolonganListrikEntity(1, "R-1/TR", 450, 11000, false),
                GolonganListrikEntity(2, "R-1/TR", 900, 20000, false),
                GolonganListrikEntity(3, "R-1/TR", 900, getBiayaBeban(900), true),
                GolonganListrikEntity(4, "R-1/TR", 1300, getBiayaBeban(1300), true),
                GolonganListrikEntity(5, "R-1/TR", 2200, getBiayaBeban(2200), true),
                GolonganListrikEntity(6, "R-2/TR", 3500, getBiayaBeban(3500), true),
                GolonganListrikEntity(7, "R-2/TR", 5500, getBiayaBeban(5500), true),
                GolonganListrikEntity(8, "R-3/TR", 6600, getBiayaBeban(6600), true)
            )
        }

        if (golonganListrikDao.getAllBiayaPemakaian().isEmpty()) {
            println("Seeding BiayaPemakaianEntity..")
            golonganListrikDao.insertAllBiayaPemakaian(
                BiayaPemakaianEntity(1, 0, 169, 415),
                BiayaPemakaianEntity(1, 31, 360, 415),
                BiayaPemakaianEntity(1, 61, 495, 415),
                BiayaPemakaianEntity(2, 0, 275, 605),
                BiayaPemakaianEntity(2, 21, 445, 605),
                BiayaPemakaianEntity(2, 61, 495, 605),
                BiayaPemakaianEntity(3, 0, 1352, 1352),
                BiayaPemakaianEntity(4, 0, 1352, 1352),
                BiayaPemakaianEntity(5, 0, 1352, 1352),
                BiayaPemakaianEntity(6, 0, 1352, 1352),
                BiayaPemakaianEntity(7, 0, 1352, 1352),
                BiayaPemakaianEntity(8, 0, 1352, 1352),
            )
        }
//        println(golonganListrikDao.getAllGolonganListrik())
        println("Seeding completed.")
    }
}