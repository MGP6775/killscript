package dev.schlaubi.mastermind.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

private val LoomDispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

val Dispatchers.Loom get() = LoomDispatcher
