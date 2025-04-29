package com.example.interfaces

import com.example.basemodel.BaseRepository
import com.example.basemodel.IntBaseDataImpl

interface IntPostgreTableRepository <T : IntBaseDataImpl<T>> : IntPostgreTable<T> {
    fun getRepository(): BaseRepository<T>
}