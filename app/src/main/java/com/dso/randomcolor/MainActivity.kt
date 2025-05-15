package com.dso.randomcolor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.dso.randomcolor.ui.theme.RandomColorTheme
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private var colors = mutableStateListOf<Color>()
    private var context = this
    private val nowColor = mutableStateOf(Color.White)
    companion object {
        const val HISTORY_FP = "history.txt"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RandomColorTheme {
                MainCompose()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
        nowColor.value = if (colors.isEmpty()) Color.White else colors[colors.size - 1]
    }

    override fun onPause() {
        super.onPause()
        saveHistory()
    }

    @Composable
    fun MainCompose() {
        var nowColor by nowColor
        val lazyListState = rememberLazyListState()  // 维护 LazyColumn 的状态
        val coroutineScope = rememberCoroutineScope()  // 创建一个协程作用域


        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Row (
                modifier = Modifier.padding(innerPadding)
            ) {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        reverseLayout = true,
                        verticalArrangement = Arrangement.Top,
                        state = lazyListState
                    ) {
                        itemsIndexed(colors) { i, it ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                .background(it)
                                .clickable { nowColor = it }
                            ) {
                                Text(
                                    text = color2string(it),
                                    color = it.inverted(),
                                    modifier = Modifier
                                        .weight(1f),
                                )
                                IconButton(onClick = { delColor(i) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除历史")
                                }
                            }
                        }
                    }
                    Button(onClick = { delAllColor(); nowColor = Color.White }) {
                        Text(text = "Clear history")
                    }
                    Text(text = "Tap right to get a new color.")
                }
                Column (
                    modifier = Modifier.weight(1f).background(nowColor).clickable {
                        addColor()
                        coroutineScope.launch {
                            lazyListState.scrollToItem(colors.size - 1)
                        }
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = color2string(nowColor),
                            color = nowColor.inverted(),
                        )
                    }
                }
            }
        }
    }

    private fun color2string(color: Color) : String {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return String.format("#%02X%02X%02X", r, g, b)
    }

    private fun Color.inverted(): Color {
        val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
        return if (luminance > 0.5) Color.Black else Color.White
    }

    private fun addColor() {
        nowColor.value = randomColor()
        colors.add(nowColor.value)
    }

    private fun delColor(i: Int) {
        colors.removeAt(i)
    }

    private fun delAllColor() {
        colors.clear()
    }

    private fun saveHistory() {
        var text = ""
        colors.forEach {
            text += "${color2string(it)}\n"
        }
        val file = File(context.filesDir, HISTORY_FP)
        file.writeText(text)
    }

    private fun loadHistory() {
        val file = File(context.filesDir, HISTORY_FP)
        val lines = if (file.exists()) file.readLines() else listOf()
        colors.clear()
        colors.addAll(lines.mapNotNull { line ->
            try {
                Color(android.graphics.Color.parseColor(line.trim()))
            } catch (e: IllegalArgumentException) {
                null // 忽略非法行
            }
        }.toList())
    }

    private fun randomColor(): Color {
        val r = Random.nextInt(0, 256)
        val g = Random.nextInt(0, 256)
        val b = Random.nextInt(0, 256)
        return Color(r, g, b)
    }

    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        RandomColorTheme {
            MainCompose()
        }
    }
}
