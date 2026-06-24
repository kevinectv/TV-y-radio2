package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Catalog
import com.example.data.model.CatalogItem

@Entity(tableName = "catalogs")
data class CatalogEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sourceType: String,
    val url: String,
    val isVisible: Boolean,
    val showInHome: Boolean,
    val showInRecommendations: Boolean,
    val showInSearch: Boolean,
    val numItems: Int,
    val status: String,
    val lastUpdated: String,
    val orderIndex: Int,
    val layoutType: String
)

fun CatalogEntity.toDomain(items: List<CatalogItem> = emptyList()): Catalog {
    return Catalog(
        id = id,
        name = name,
        sourceType = sourceType,
        url = url,
        isVisible = isVisible,
        showInHome = showInHome,
        showInRecommendations = showInRecommendations,
        showInSearch = showInSearch,
        numItems = numItems,
        status = status,
        lastUpdated = lastUpdated,
        orderIndex = orderIndex,
        layoutType = layoutType,
        items = items
    )
}

fun Catalog.toEntity(): CatalogEntity {
    return CatalogEntity(
        id = id,
        name = name,
        sourceType = sourceType,
        url = url,
        isVisible = isVisible,
        showInHome = showInHome,
        showInRecommendations = showInRecommendations,
        showInSearch = showInSearch,
        numItems = numItems,
        status = status,
        lastUpdated = lastUpdated,
        orderIndex = orderIndex,
        layoutType = layoutType
    )
}
