package com.example.dinahvision.models

data class User(var uid: String = "", var username: String, var password: String){
    constructor(): this("", "",  "")
}
