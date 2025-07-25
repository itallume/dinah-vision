package com.example.dinahvision.models

data class User(var uid: String = "", var username: String, var password: String, var points:Int) {
    constructor(): this("", "",  "", 0)
    companion object {
    private var _currentUser: User? = null

    val currentUser: User?
        get() = _currentUser

    fun login(user: User) {
        _currentUser = user
    }

    fun logout() {
        _currentUser = null
    }

    val isLoggedIn: Boolean
        get() = _currentUser != null
}
}