package org.example.calculator

import asSimpleIntValue
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import simpleValueOf

fun main() {
    CalcClient().start()
    println("ok")
}

class CalcClient {

    fun start() {
        val channel = ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build()

        val calculator = CalculatorGrpc.newStub(channel)

        runBlocking {
            unaryCall(calculator)
            clientStreaming(calculator)
            serverStreaming(calculator)
            bidirectional(calculator)
        }
    }

    private suspend fun unaryCall(calculator: CalculatorGrpc.CalculatorStub) {
        val z = 100.0
        val zSqrt = calculator.sqrt(simpleValueOf(z)).value
        println("sqrt($z): $zSqrt")
    }

    private suspend fun clientStreaming(calculator: CalculatorGrpc.CalculatorStub) {
        val standardDeviationCall = calculator.standardDeviation()
        val z = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        z.forEach {
            standardDeviationCall.send(simpleValueOf(it.toDouble()))
        }
        standardDeviationCall.close()
        val standardDeviation = standardDeviationCall.await().value
        println("standardDeviation($z): $standardDeviation")
    }

    private suspend fun serverStreaming(calculator: CalculatorGrpc.CalculatorStub) {
        val z3 = 12
        println("factorize($z3):")
        val serverResponses = calculator.factorize(z3.asSimpleIntValue())
        for (response in serverResponses) {
            println("    ${response.value}")
        }
    }

    private suspend fun bidirectional(calculator: CalculatorGrpc.CalculatorStub) {
        coroutineScope {
            val maxCall = calculator.max()
            launch {
                for (newMax in maxCall) {
                    println("new max: ${newMax.value}")
                }
                println("end max")
            }
            suspend fun sendWithDelay(value: Double) {
                delay(500)
                maxCall.send(simpleValueOf(value))
            }
            sendWithDelay(2.0)
            sendWithDelay(1.5)
            sendWithDelay(3.0)
            sendWithDelay(4.0)
            maxCall.close()
        }
    }
}
