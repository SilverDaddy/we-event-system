package com.serverc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServerCApplication

fun main(args: Array<String>) {
    runApplication<ServerCApplication>(*args)
}
