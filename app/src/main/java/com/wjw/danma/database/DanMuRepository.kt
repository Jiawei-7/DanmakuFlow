package com.wjw.danma.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class DanMuRepository(context: Context) {
    private val dbHelper = DanMuDatabaseHelper(context)

    fun insertDanMu(content: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DanMuDatabaseHelper.COLUMN_CONTENT, content)
        }
        db.insert(DanMuDatabaseHelper.TABLE_NAME, null, values)
        db.close()
    }

    fun getAllDanMu(): List<String> {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            DanMuDatabaseHelper.TABLE_NAME,
            arrayOf(DanMuDatabaseHelper.COLUMN_CONTENT),
            null, null, null, null, null
        )

        val danMuList = mutableListOf<String>()
        with(cursor) {
            while (moveToNext()) {
                val content = getString(getColumnIndexOrThrow(DanMuDatabaseHelper.COLUMN_CONTENT))
                danMuList.add(content)
            }
        }
        cursor.close()
        db.close()
        return danMuList
    }

    fun clearAllDanMu() {
        val db = dbHelper.writableDatabase
        db.delete(DanMuDatabaseHelper.TABLE_NAME, null, null)
        db.close()
    }

    fun deleteDanMu(content: String): Boolean {
        val db = dbHelper.writableDatabase
        // 删除条件：匹配 content 字段
        val rowsDeleted = db.delete(
            DanMuDatabaseHelper.TABLE_NAME,
            "${DanMuDatabaseHelper.COLUMN_CONTENT} = ?",
            arrayOf(content)
        )
        db.close()
        // 返回是否成功删除
        return rowsDeleted > 0
    }

}
