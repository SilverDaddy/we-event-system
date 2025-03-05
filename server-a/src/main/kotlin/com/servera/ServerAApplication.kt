package com.servera

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServerAApplication

fun main(args: Array<String>) {
	runApplication<ServerAApplication>(*args)
}
