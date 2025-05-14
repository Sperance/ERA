package com.example.schedulers

val dailyTaskScheduler = DailyTaskScheduler
val hoursTaskScheduler = HoursTaskScheduler

fun configureSchedulers() {
    dailyTaskScheduler.start()
    hoursTaskScheduler.start()
}