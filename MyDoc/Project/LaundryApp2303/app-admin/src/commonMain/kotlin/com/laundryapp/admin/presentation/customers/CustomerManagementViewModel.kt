package com.laundryapp.admin.presentation.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.User
import com.laundryapp.core.data.model.UserRole
import com.laundryapp.core.data.repository.AuthRepository
import kotlinx.coroutines.flow.*

class CustomerManagementViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val customers: StateFlow<List<User>> = combine(
        authRepository.getAllUsers(UserRole.CUSTOMER),
        _searchQuery
    ) { users, query ->
        if (query.isBlank()) users
        else users.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.email.contains(query, ignoreCase = true) ||
            it.phone.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
