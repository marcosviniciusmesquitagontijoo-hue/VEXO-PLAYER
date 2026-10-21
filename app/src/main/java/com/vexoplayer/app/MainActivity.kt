package com.vexoplayer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

private val Bg = Color(0xFF0D110F)
private val Panel = Color(0xFF19211C)
private val Green = Color(0xFF43E86B)
private val Muted = Color(0xFFA4B4A8)
private const val DEFAULT_SERVER = "http://agenciacinepoa.top"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("vexo", MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Green, background = Bg, surface = Panel,
                onPrimary = Color.Black, onBackground = Color.White, onSurface = Color.White
            )) {
                VexoApp(
                    initialServer = prefs.getString("server", DEFAULT_SERVER) ?: DEFAULT_SERVER,
                    onSave = { server, user ->
                        prefs.edit().putString("server", server).putString("user", user).apply()
                    },
                    initialUser = prefs.getString("user", "") ?: ""
                )
            }
        }
    }
}

@Composable
private fun VexoApp(initialServer: String, initialUser: String, onSave: (String, String) -> Unit) {
    var server by remember { mutableStateOf(initialServer) }
    var username by remember { mutableStateOf(initialUser) }
    var password by remember { mutableStateOf("") }
    var page by remember { mutableStateOf("login") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("VEXO PLAYER", color = Green, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(
                when (page) {
                    "login" -> "Entre para acessar seu conteúdo"
                    "settings" -> "Configurações"
                    else -> "Seu entretenimento em um só lugar"
                },
                color = Muted
            )
            Spacer(Modifier.height(28.dp))

            when (page) {
                "login" -> {
                    VexoField("Usuário", username, { username = it }, "Digite seu usuário")
                    Spacer(Modifier.height(12.dp))
                    VexoField("Senha", password, { password = it }, "Digite sua senha", secret = true)
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                message = "Digite usuário e senha."
                            } else {
                                loading = true
                                message = ""
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        validateXtream(server, username.trim(), password)
                                    }
                                    loading = false
                                    message = result.second
                                    if (result.first) {
                                        onSave(server, username.trim())
                                        page = "home"
                                        message = ""
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (loading) "CONECTANDO..." else "ENTRAR", color = Color.Black, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { page = "settings"; message = "" }) { Text("Configurações / Trocar servidor", color = Green) }
                }
                "settings" -> {
                    VexoField("URL do servidor", server, { server = it.trim() }, "http://seu-servidor:porta")
                    Spacer(Modifier.height(12.dp))
                    VexoField("Usuário (opcional)", username, { username = it }, "Usuário")
                    Spacer(Modifier.height(12.dp))
                    VexoField("Senha (não é salva)", password, { password = it }, "Senha para testar conexão", secret = true)
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val normalized = server.trim().trimEnd('/')
                            if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
                                message = "A URL deve começar com http:// ou https://."
                            } else {
                                server = normalized
                                onSave(server, username.trim())
                                message = "Servidor salvo. Entre com suas credenciais."
                                page = "login"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("SALVAR SERVIDOR", color = Color.Black, fontWeight = FontWeight.Bold) }
                    TextButton(onClick = { server = DEFAULT_SERVER; message = "Servidor padrão restaurado." }) {
                        Text("Restaurar servidor padrão", color = Green)
                    }
                    TextButton(onClick = { page = "login"; message = "" }) { Text("Voltar", color = Muted) }
                }
                "home" -> {
                    listOf("TV AO VIVO", "FILMES", "SÉRIES", "FAVORITOS", "CONFIGURAÇÕES", "RECARREGAR").forEach { label ->
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                if (label == "CONFIGURAÇÕES") page = "settings"
                                else if (label == "RECARREGAR") message = "Para carregar categorias e mídia, a API precisa responder corretamente."
                                else message = "$label: integração de catálogo/player ainda precisa ser concluída."
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Green),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(label, color = Green, fontWeight = FontWeight.Bold) }
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { page = "login"; password = ""; message = "" }) { Text("Sair", color = Muted) }
                }
            }
            if (message.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(message, color = if (message.contains("salvo")) Green else Color(0xFFFFB4AB))
            }
            Spacer(Modifier.height(18.dp))
            Text("Vexo Player • versão inicial", color = Color(0xFF66756B), fontSize = 12.sp)
        }
    }
}

@Composable
private fun VexoField(
    label: String, value: String, onValueChange: (String) -> Unit,
    placeholder: String, secret: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color(0xFFE7EEE9), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Muted) },
            singleLine = true,
            visualTransformation = if (secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = if (secret) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green, unfocusedBorderColor = Color(0xFF35463A),
                focusedContainerColor = Panel, unfocusedContainerColor = Panel,
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                cursorColor = Green
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

private fun validateXtream(server: String, user: String, pass: String): Pair<Boolean, String> {
    return try {
        val clean = server.trim().trimEnd('/')
        val encodedUser = URLEncoder.encode(user, "UTF-8")
        val encodedPass = URLEncoder.encode(pass, "UTF-8")
        val url = "$clean/player_api.php?username=$encodedUser&password=$encodedPass"
        val client = OkHttpClient.Builder().callTimeout(java.time.Duration.ofSeconds(15)).build()
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return Pair(false, "Servidor respondeu HTTP ${response.code}.")
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val info = json.optJSONObject("user_info")
            if (info != null && info.optString("auth") == "1") {
                val status = info.optString("status", "Active")
                if (status.equals("Active", ignoreCase = true)) Pair(true, "")
                else Pair(false, "A conta não está ativa: $status.")
            } else Pair(false, "Login não validado. Confira os dados e a compatibilidade da API.")
        }
    } catch (e: Exception) {
        Pair(false, "Não foi possível conectar. Verifique servidor, internet e API Xtream.")
    }
}
