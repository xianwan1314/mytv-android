package top.yogiczy.mytv.ui.utils

import android.os.Build
import android.view.KeyEvent
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

/**
 * 监听短按、长按按键事件
 */
fun Modifier.handleKeyEvents(
    onKeyTap: Map<Int, () -> Unit> = emptyMap(),
    onKeyLongTap: Map<Int, () -> Unit> = emptyMap(),
): Modifier {
    val keyDownMap = mutableMapOf<Int, Boolean>()

    return onPreviewKeyEvent {
        when (it.nativeKeyEvent.action) {
            KeyEvent.ACTION_DOWN -> {
                if (it.nativeKeyEvent.repeatCount == 0) {
                    keyDownMap[it.nativeKeyEvent.keyCode] = true
                } else if (it.nativeKeyEvent.repeatCount == 1) {
                    keyDownMap.remove(it.nativeKeyEvent.keyCode)
                    onKeyLongTap[it.nativeKeyEvent.keyCode]?.invoke()
                }
            }

            KeyEvent.ACTION_UP -> {
                if (keyDownMap[it.nativeKeyEvent.keyCode] == true) {
                    keyDownMap.remove(it.nativeKeyEvent.keyCode)
                    onKeyTap[it.nativeKeyEvent.keyCode]?.invoke()
                }
            }
        }

        false
    }
}

/**
 * 监听手势滑动事件
 */
fun Modifier.handleDragGestures(
    onSwipeUp: () -> Unit = {},
    onSwipeDown: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
): Modifier {
    val speedThreshold = 100.dp
    val distanceThreshold = 10.dp

    val verticalTracker = VelocityTracker()
    var verticalDragOffset = 0f
    val horizontalTracker = VelocityTracker()
    var horizontalDragOffset = 0f


    return this then pointerInput(Unit) {
        detectVerticalDragGestures(
            onDragEnd = {
                if (verticalDragOffset.absoluteValue > distanceThreshold.toPx()) {
                    if (verticalTracker.calculateVelocity().y > speedThreshold.toPx()) {
                        onSwipeDown()
                    } else if (verticalTracker.calculateVelocity().y < -speedThreshold.toPx()) {
                        onSwipeUp()
                    }
                }
            },
        ) { change, dragAmount ->
            verticalDragOffset += dragAmount
            verticalTracker.addPosition(change.uptimeMillis, change.position)
        }
    }.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragEnd = {
                if (horizontalDragOffset.absoluteValue > distanceThreshold.toPx()) {
                    if (horizontalTracker.calculateVelocity().x > speedThreshold.toPx()) {
                        onSwipeRight()
                    } else if (horizontalTracker.calculateVelocity().x < -speedThreshold.toPx()) {
                        onSwipeLeft()
                    }
                }
            },
        ) { change, dragAmount ->
            horizontalDragOffset += dragAmount
            horizontalTracker.addPosition(change.uptimeMillis, change.position)
        }
    }
}

/**
 * 监听全方位的DPad按键事件（兼容触摸屏）
 */
fun Modifier.handleDPadKeyEvents(
    onLeft: () -> Unit = {},
    onLongLeft: () -> Unit = {},
    onRight: () -> Unit = {},
    onLongRight: () -> Unit = {},
    onUp: () -> Unit = {},
    onLongUp: () -> Unit = {},
    onDown: () -> Unit = {},
    onLongDown: () -> Unit = {},
    onSelect: () -> Unit = {},
    onLongSelect: () -> Unit = {},
    onSettings: () -> Unit = {},
    onNumber: (Int) -> Unit = {},
) = this then handleKeyEvents(
    onKeyTap = mapOf(
        KeyEvent.KEYCODE_DPAD_LEFT to onLeft,
        KeyEvent.KEYCODE_DPAD_RIGHT to onRight,
        KeyEvent.KEYCODE_DPAD_UP to onUp,
        KeyEvent.KEYCODE_CHANNEL_UP to onUp,
        KeyEvent.KEYCODE_DPAD_DOWN to onDown,
        KeyEvent.KEYCODE_CHANNEL_DOWN to onDown,

        KeyEvent.KEYCODE_DPAD_CENTER to onSelect,
        KeyEvent.KEYCODE_ENTER to onSelect,
        KeyEvent.KEYCODE_NUMPAD_ENTER to onSelect,

        KeyEvent.KEYCODE_MENU to onSettings,
        KeyEvent.KEYCODE_SETTINGS to onSettings,
        KeyEvent.KEYCODE_HELP to onSettings,
        KeyEvent.KEYCODE_H to onSettings,
        KeyEvent.KEYCODE_UNKNOWN to onSettings,

        KeyEvent.KEYCODE_0 to { onNumber(0) },
        KeyEvent.KEYCODE_1 to { onNumber(1) },
        KeyEvent.KEYCODE_2 to { onNumber(2) },
        KeyEvent.KEYCODE_3 to { onNumber(3) },
        KeyEvent.KEYCODE_4 to { onNumber(4) },
        KeyEvent.KEYCODE_5 to { onNumber(5) },
        KeyEvent.KEYCODE_6 to { onNumber(6) },
        KeyEvent.KEYCODE_7 to { onNumber(7) },
        KeyEvent.KEYCODE_8 to { onNumber(8) },
        KeyEvent.KEYCODE_9 to { onNumber(9) },
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT to onLeft
            KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT to onRight
            KeyEvent.KEYCODE_SYSTEM_NAVIGATION_UP to onUp
            KeyEvent.KEYCODE_SYSTEM_NAVIGATION_DOWN to onDown
        }
    },
    onKeyLongTap = mapOf(
        KeyEvent.KEYCODE_DPAD_LEFT to onLongLeft,
        KeyEvent.KEYCODE_DPAD_RIGHT to onLongRight,
        KeyEvent.KEYCODE_DPAD_UP to onLongUp,
        KeyEvent.KEYCODE_CHANNEL_UP to onLongUp,
        KeyEvent.KEYCODE_DPAD_DOWN to onLongDown,
        KeyEvent.KEYCODE_CHANNEL_DOWN to onLongDown,

        KeyEvent.KEYCODE_ENTER to onLongSelect,
        KeyEvent.KEYCODE_NUMPAD_ENTER to onLongSelect,
        KeyEvent.KEYCODE_DPAD_CENTER to onLongSelect,
    ),
).pointerInput(Unit) {
    detectTapGestures(
        onTap = { onSelect() },
        onLongPress = { onLongSelect() },
        onDoubleTap = { onSettings() },
    )
}

/**
 * 监听任意事件（按键、滑动、点击）
 */
fun Modifier.handleAnyActiveAction(onAction: () -> Unit = {}) =
    onPreviewKeyEvent { onAction(); false }.pointerInput(Unit) { detectDragGestures { _, _ -> onAction() } }
