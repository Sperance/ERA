package com.example.interfaces

import com.example.basemodel.IntBaseDataImpl

interface IntPostgreTableRepository <T : IntBaseDataImpl<T>> : IntPostgreTable<T>