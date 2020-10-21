package com.ease.timezones

data class User(var name: String?,
                        var authId: String?,
                        var emailAddress: String?){
    constructor():this(null,null,null)
}
