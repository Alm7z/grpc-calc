package org.example.calculator

import asSimpleValue
import io.grpc.ServerBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import org.nield.kotlinstatistics.standardDeviation
import simpleIntValueOf
import simpleValueOf
import java.util.concurrent.Executors

fun main() {
    val server = ServerBuilder.forPort(8080)
        .addService(CalcService())
        .build()
    server.start()
    server.awaitTermination()
}

@ExperimentalCoroutinesApi
class CalcService : CalculatorImplBase(
    coroutineContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
) {

    override suspend fun sqrt(request: SimpleValue): SimpleValue {
        return simpleValueOf(
            kotlin.math.sqrt(request.value)
        )
    }

    override suspend fun standardDeviation(requests: ReceiveChannel<SimpleValue>): SimpleValue {
        return requests.toList()
            .map { it.value }
            .standardDeviation()
            .asSimpleValue()
    }

    override fun factorize(request: SimpleIntValue) = produce<SimpleIntValue> {
        request.value.factorize().forEach {
            delay(500)
            send(simpleIntValueOf(it))
        }
    }

    private fun Int.factorize(): List<Int> {
        val result = mutableListOf<Int>()

        var t = this
        for (i in 2..kotlin.math.sqrt(this.toDouble()).toInt()) {
            while (t % i == 0) {
                result.add(i)
                t /= i
            }
        }
        if (t > 1) result.add(t)

        return result
    }

    override fun max(requests: ReceiveChannel<SimpleValue>) = produce<SimpleValue> {
        var currentMax: Double? = null
        requests.consumeAsFlow()
            .map { it.value }
            .collect {
                if (currentMax == null || it > currentMax!!) {
                    currentMax = it
                    send(simpleValueOf(it))
                }
            }
    }
}
