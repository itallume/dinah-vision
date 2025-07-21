package com.example.dinahvision.models

data class User(var username: String, var password: String){
    constructor(): this("", "")
}
