package info.dogechain.doge_control_center

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class PriceIndicatorWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String {
        return "DogeControlCenter"
    }

    override fun getDisplayName(): String {
        return "Doge Coin Price Indicator"
    }

    override fun isAvailable(project: Project): Boolean {
        return true;
    }

    override fun createWidget(project: Project): StatusBarWidget {
        return DogeCoinPriceIndicator()
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        Disposer.dispose(widget);
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean {
        return true;
    }
}