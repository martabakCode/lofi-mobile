package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.ProductDto
import com.loanfinancial.lofi.domain.repository.IProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase
    @Inject
    constructor(
        private val repository: IProductRepository,
    ) {
        operator fun invoke(): Flow<Resource<List<ProductDto>>> = repository.getProducts()
    }
