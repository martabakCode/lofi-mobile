package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.AvailableProductDto
import com.loanfinancial.lofi.domain.repository.IProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAvailableProductUseCase
    @Inject
    constructor(
        private val repository: IProductRepository,
    ) {
        operator fun invoke(): Flow<Resource<AvailableProductDto>> = repository.getAvailableProduct()
    }
