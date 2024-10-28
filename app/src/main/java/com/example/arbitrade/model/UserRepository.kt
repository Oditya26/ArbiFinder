package com.example.arbitrade.model

interface UserRepository {
    fun getUser(userId : Long) : UserDataModel
    fun getAllUser(): List<UserDataModel>
}