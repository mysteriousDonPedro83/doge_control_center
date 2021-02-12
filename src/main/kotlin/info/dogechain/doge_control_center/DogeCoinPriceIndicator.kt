package info.dogechain.doge_control_center

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.components.service
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.ui.ColorUtil.softer
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.ui.JreHiDpiUtil
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.ImageUtil
import com.intellij.util.ui.UIUtil
import java.awt.*
import java.awt.image.BufferedImage
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.swing.JButton
import javax.swing.JComponent


class DogeCoinPriceIndicator() : JButton(), CustomStatusBarWidget {
    private val id = "info.dogechain.doge_control_center.PriceIndicator"

    private var systemColor: Color? = null
    private var ideColor: Color? = null

    private var myBufferedImage: Image? = null

    private var manager: DogeCoinPriceManager
    private var latest: Pair<Float, Float> = Pair(0f, 0f)
    private var previous: Pair<Float, Float> = Pair(0f, 0f)
    private var future: ScheduledFuture<*>
    private var poller: QueuePoller

    init {
        preferredSize = Dimension(250, 25)
        refreshColors()
        isOpaque = false
        isFocusable = false

        manager = service();
        poller = QueuePoller(this, manager.queue)
        future = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(poller, 500, 100, TimeUnit.MILLISECONDS)
    }


    override fun getComponent(): JComponent {
        return this
    }

    override fun dispose() {
        future.cancel(true)
    }

    override fun ID(): String {
        return id;
    }

    override fun install(statusBar: StatusBar) {
    }

    private fun refreshColors() {
        systemColor = if (UIUtil.isUnderDarcula()) JBColor.BLUE.darker() else JBColor.CYAN.darker()
        ideColor = if (UIUtil.isUnderDarcula()) JBColor.BLUE.darker().darker().darker() else softer(
            JBColor.CYAN
        )
    }

    override fun paintComponent(g: Graphics) {
        val pressed = getModel().isPressed
        val currentState = latest
        val stateChanged = previous != currentState
        var bufferedImage: Image? = myBufferedImage
        if (stateChanged) {
            val size = size
            //rare error
            if (size.width < 0 || size.height < 0) {
                return
            }
            val insets = insets
            bufferedImage = ImageUtil.createImage(g, size.width, size.height, BufferedImage.TYPE_INT_ARGB)
            val g2 = bufferedImage.getGraphics().create() as Graphics2D
            val system = latest.first
            val process = latest.second
            val totalBarLength = size.width - insets.left - insets.right - 3
            val barHeight = Math.max(size.height, font.size + 2)
            val yOffset = (size.height - barHeight) / 2
            val xOffset = insets.left

            // label
            g2.font = font

            val info: String = fixedLengthString(process.toString(), 3) + " USD | " + fixedLengthString(
                system.toString(),
                3
            ) + " BTC"
            val fontMetrics = g.fontMetrics
            val infoWidth = fontMetrics.charsWidth(info.toCharArray(), 0, info.length)
            val infoHeight = fontMetrics.ascent
            UISettings.setupAntialiasing(g2)
            val fg = if (pressed) UIUtil.getLabelDisabledForeground() else JBColor.foreground()
            g2.color = fg
            g2.drawString(
                info, xOffset + (totalBarLength - infoWidth) / 2,
                yOffset + infoHeight + (barHeight - infoHeight) / 2 - 1
            )

            g.clearRect(0, 0, size.width - 2, size.height - 1)
            // border
            g2.stroke = BasicStroke(1F)
            g2.color = JBColor.GRAY
            g2.drawRect(0, 0, size.width - 2, size.height - 1)
            g2.dispose()
            myBufferedImage = bufferedImage
        }
        if(null != bufferedImage){
            draw(g, bufferedImage)
        }
        previous = currentState
//        bufferedImage?.let { draw(g, it) }
    }

    private fun draw(g: Graphics, bufferedImage: Image) {
        UIUtil.drawImage(g, bufferedImage, 0, 0, null)
        if (JreHiDpiUtil.isJreHiDPI(g as Graphics2D) && !UIUtil.isUnderDarcula()) {
            val g2 = g.create(0, 0, width, height) as Graphics2D
            val s = JBUIScale.sysScale(g2)
            g2.scale((1 / s).toDouble(), (1 / s).toDouble())
            g2.setColor(if (UIUtil.isUnderIntelliJLaF()) Gray.xC9 else Gray.x91)
            g2.drawLine(0, 0, (s * width).toInt(), 0)
            g2.scale(1.0, 1.0)
            g2.dispose()
        }
    }


    private fun fixedLengthString(string: String?, length: Int): String {
        return String.format("%1$" + length + "s", string)
    }

    fun update(latest: Pair<Float, Float>): Boolean {
        var painted = false
        this.latest = latest

        if (!isShowing) {
            // noinspection ConstantConditions
            return painted
        }

        val graphics = graphics
        if (graphics != null) {
            paintComponent(graphics)
            painted = true
        }


        return painted
    }

}

