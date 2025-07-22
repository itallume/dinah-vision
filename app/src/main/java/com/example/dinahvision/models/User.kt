package com.example.dinahvision.models

data class User(val uid: String = "", var username: String, var password: String){
    constructor(): this("", "",  "")
}
