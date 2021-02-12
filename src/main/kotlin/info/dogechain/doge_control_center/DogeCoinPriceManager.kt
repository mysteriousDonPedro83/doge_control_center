package info.dogechain.doge_control_center

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.concurrency.AppExecutorUtil
import java.net.URL
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class DogeCoinPriceManager {
    companion object {
        private val LOG = Logger.getInstance(
            DogeCoinPriceManager::class.java
        )
    }

    private val task = Runnable { update() }
    private var schedule: ScheduledFuture<*>? = null
    private var mapper: ObjectMapper = ObjectMapper().registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    var queue = ConcurrentLinkedQueue<Pair<Float, Float>>()
        private set


    init {
        mapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
    }

    fun run() {
        if (null != schedule) {
            return
        }
        queue.add(Pair(999.0f, 999.0f))
        schedule =
            AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(task, 1, 60, TimeUnit.SECONDS)
    }

    private fun update() {
        val dogeBtc = getSymbolPrice("DOGEBTC")
        val dogeUsd = getSymbolPrice("DOGEUSDT")

        queue.add(Pair(dogeBtc.price.toFloat(), dogeUsd.price.toFloat()))
    }

    private fun getSymbolPrice(symbol: String): SymbolPrice {
        return mapper.readValue(
            URL("https://api.binance.com/api/v3/ticker/price?symbol=${symbol}"),
            SymbolPrice::class.java
        ) ?: return SymbolPrice(symbol, "999.0")
    }
}