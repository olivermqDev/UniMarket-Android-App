package com.atom.unimarket.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.atom.unimarket.data.local.dao.CartDao
import com.atom.unimarket.data.local.entity.CartEntity

@Database(entities = [CartEntity::class], version = 1, exportSchema = false)
abstract class CartDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}
