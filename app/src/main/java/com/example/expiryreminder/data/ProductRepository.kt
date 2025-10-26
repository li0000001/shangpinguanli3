package com.example.expiryreminder.data

import com.example.expiryreminder.domain.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ProductRepository {
    fun getProducts(): Flow<List<Product>>
    suspend fun getProductById(id: Long): Product?
    suspend fun insertProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun deleteProductById(id: Long)
}

class ProductRepositoryImpl(private val productDao: ProductDao) : ProductRepository {
    override fun getProducts(): Flow<List<Product>> =
        productDao.getAllProducts().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getProductById(id: Long): Product? =
        productDao.getProductById(id)?.toDomain()

    override suspend fun insertProduct(product: Product): Long =
        productDao.insertProduct(product.toEntity())

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity())
    }

    override suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product.toEntity())
    }

    override suspend fun deleteProductById(id: Long) {
        productDao.deleteProductById(id)
    }
}
