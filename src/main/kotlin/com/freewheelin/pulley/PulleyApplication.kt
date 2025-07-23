package com.freewheelin.pulley

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PulleyApplication

fun main(args: Array<String>) {
	runApplication<PulleyApplication>(*args)
}
