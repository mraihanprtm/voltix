package com.example.voltix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.entity.UserPerangkatCrossRef
import com.example.voltix.data.relations.UserWithPerangkat
import com.example.voltix.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId

    private val _userWithPerangkat = MutableStateFlow<UserWithPerangkat?>(null)
    val userWithPerangkat: StateFlow<UserWithPerangkat?> = _userWithPerangkat

    fun insertUser(user: UserEntity) {
        viewModelScope.launch {
            val id = repository.insertUser(user)
            _userId.value = id.toInt()
        }
    }

    fun getUserWithPerangkat(id: Int) {
        viewModelScope.launch {
            val user = repository.getUserWithPerangkat(id)
            _userWithPerangkat.value = user
        }
    }

    fun addUserPerangkatCrossRef(ref: UserPerangkatCrossRef) {
        viewModelScope.launch {
            repository.insertUserPerangkatCrossRef(ref)
        }
    }
}
