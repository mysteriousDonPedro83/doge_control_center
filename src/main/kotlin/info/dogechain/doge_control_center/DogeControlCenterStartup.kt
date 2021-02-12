package info.dogechain.doge_control_center

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class DogeControlCenterStartup : StartupActivity {
    override fun runActivity(project: Project) {
        service<DogeCoinPriceManager>().run()
    }


    companion object {
        private val LOG = Logger.getInstance(
            DogeControlCenterStartup::class.java
        )
    }
}