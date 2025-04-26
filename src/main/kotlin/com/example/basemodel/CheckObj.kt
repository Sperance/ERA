package com.example.basemodel

import com.example.enums.EnumHttpCode

data class CheckObj(val result: Boolean, val errorHttp: EnumHttpCode, val errorCode: Int, val errorText: String)