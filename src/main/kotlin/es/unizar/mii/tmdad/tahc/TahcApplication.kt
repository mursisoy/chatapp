package es.unizar.mii.tmdad.tahc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TahcApplication

fun main(args: Array<String>) {
	runApplication<TahcApplication>(*args)
}
