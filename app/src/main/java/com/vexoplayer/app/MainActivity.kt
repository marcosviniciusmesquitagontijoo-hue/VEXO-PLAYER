package com.vexoplayer.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

private val Bg = Color(0xFF0D110F)
private val Panel = Color(0xFF19211C)
private val Green = Color(0xFF43E86B)
private val Muted = Color(0xFFA4B4A8)
private const val DEFAULT_SERVER = "http://agenciacinepoa.top"

private val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()
}

private data class MediaItem(
    val id: String,
    val name: String,
    val categoryId: String = "",
    val streamType: String = "",
    val containerExtension: String = "",
    val cover: String = "",
    val plot: String = ""
)

private data class XtreamCredentials(
    val server: String,
    val username: String,
    val password: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("vexo", MODE_PRIVATE)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Green,
                    background = Bg,
                    surface = Panel,
                    onPrimary = Color.Black,
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                VexoApp(
                    initialServer = prefs.getString("server", DEFAULT_SERVER) ?: DEFAULT_SERVER,
                    initialUser = prefs.getString("user", "") ?: "",
                    onSave = { server, user ->
                        prefs.edit().putString("server", server).putString("user", user).apply()
                    }
                )
            }
        }
    }
}
