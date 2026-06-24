package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    @Query("SELECT * FROM catalogs ORDER BY orderIndex ASC")
    fun getAllCatalogs(): Flow<List<CatalogEntity>>

    @Query("SELECT * FROM catalogs ORDER BY orderIndex ASC")
    suspend fun getAllCatalogsList(): List<CatalogEntity>

    @Query("SELECT * FROM catalogs WHERE id = :id LIMIT 1")
    suspend fun getCatalogById(id: String): CatalogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalog(catalog: CatalogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogs(catalogs: List<CatalogEntity>)

    @Query("DELETE FROM catalogs WHERE id = :id")
    suspend fun deleteCatalogById(id: String)

    @Query("SELECT * FROM catalog_items WHERE catalogId = :catalogId")
    fun getItemsForCatalogFlow(catalogId: String): Flow<List<CatalogItemEntity>>

    @Query("SELECT * FROM catalog_items WHERE catalogId = :catalogId")
    suspend fun getItemsForCatalog(catalogId: String): List<CatalogItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogItems(items: List<CatalogItemEntity>)

    @Query("DELETE FROM catalog_items WHERE catalogId = :catalogId")
    suspend fun deleteItemsForCatalog(catalogId: String)

    @Query("SELECT COUNT(*) FROM catalog_items")
    fun getStoredItemsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM catalog_items")
    suspend fun getStoredItemsCount(): Int

    @Query("DELETE FROM catalog_items")
    suspend fun clearAllCatalogItems()

    @Query("DELETE FROM catalogs")
    suspend fun clearAllCatalogs()
}
