package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE status = :status ORDER BY updatedAt DESC")
    fun getProductsByStatus(status: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductFlow(id: Long): Flow<ProductEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("SELECT COUNT(*) FROM products WHERE status = 'AVAILABLE'")
    fun getAvailableCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE status = 'SOLD'")
    fun getSoldCount(): Flow<Int>

    @Query("SELECT SUM(purchasePrice + additionalCosts) FROM products WHERE status = 'AVAILABLE'")
    fun getTotalInvestedInStock(): Flow<Double?>
}
