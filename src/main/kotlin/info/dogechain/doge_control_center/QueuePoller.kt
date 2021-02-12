package info.dogechain.doge_control_center

import com.intellij.openapi.diagnostic.Logger
import java.util.concurrent.ConcurrentLinkedQueue

class QueuePoller(private var component: DogeCoinPriceIndicator, private var queue: ConcurrentLinkedQueue<Pair<Float, Float>>) : Runnable {
    companion object {
        private val LOG = Logger.getInstance(
            DogeControlCenterStartup::class.java
        )
    }

    override fun run() {
        if (queue.isEmpty()) {
            return;
        }
        component.update(queue.poll());
    }
}