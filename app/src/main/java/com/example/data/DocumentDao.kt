package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY modifiedAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DocumentEntity)

    @Update
    suspend fun updateDocument(doc: DocumentEntity)

    @Delete
    suspend fun deleteDocument(doc: DocumentEntity)

    @Query("UPDATE documents SET title = :newTitle, modifiedAt = :timestamp WHERE id = :id")
    suspend fun renameDocument(id: String, newTitle: String, timestamp: Long)

    @Query("UPDATE documents SET watermarkText = :watermark WHERE id = :id")
    suspend fun updateWatermark(id: String, watermark: String)

    @Query("DELETE FROM documents WHERE id LIKE 'doc-1%' OR id LIKE 'doc-2%' OR id LIKE 'doc-3%' OR id LIKE 'doc-4%' OR id LIKE 'doc-5%' OR id LIKE 'doc-6%'")
    suspend fun deleteSampleDocuments()

    @Query("DELETE FROM documents")
    suspend fun deleteAllDocuments()
}
