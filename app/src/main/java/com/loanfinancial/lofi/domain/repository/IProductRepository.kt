package com.loanfinancial.lofi.domain.repository

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.AvailableProductDto
import com.loanfinancial.lofi.data.model.dto.ProductDto
import kotlinx.coroutines.flow.Flow

interface IProductRepository {
    fun getProducts(): Flow<Resource<List<ProductDto>>>
    fun getAvailableProduct(): Flow<Resource<AvailableProductDto>>
}
