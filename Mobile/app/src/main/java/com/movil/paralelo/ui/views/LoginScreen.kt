package com.movil.paralelo.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.paralelo.ui.theme.*
import com.movil.paralelo.ui.viewmodel.AuthViewModel
import com.movil.paralelo.utils.Constants
import com.movil.paralelo.utils.Resource
import com.movil.paralelo.data.network.RetrofitClient

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("admin@paralelo.com") }
    var password by remember { mutableStateOf("admin123") }
    var showIpDialog by remember { mutableStateOf(false) }
    var customIp by remember { mutableStateOf(Constants.BASE_URL) }

    val loginState by authViewModel.loginState.collectAsState()
    val registerState by authViewModel.registerState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is Resource.Success) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(registerState) {
        if (registerState is Resource.Success) {
            // Loguear automáticamente tras registrar
            authViewModel.login(email, password)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Text(
                    text = "⚡ Paralelo Móvil",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isRegisterMode) "Crear nueva cuenta" else "Autenticación JWT & Concurrencia",
                    fontSize = 14.sp,
                    color = Slate400,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = { Text("Apellido") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                )

                // Botón Principal
                Button(
                    onClick = {
                        if (isRegisterMode) {
                            authViewModel.register(nombre, apellido, email, password)
                        } else {
                            authViewModel.login(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                    enabled = loginState !is Resource.Loading && registerState !is Resource.Loading
                ) {
                    if (loginState is Resource.Loading || registerState is Resource.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isRegisterMode) "Registrarse" else "Iniciar Sesión",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                // Error message
                if (loginState is Resource.Error) {
                    Text(
                        text = (loginState as Resource.Error).message,
                        color = Rose500,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                if (registerState is Resource.Error) {
                    Text(
                        text = (registerState as Resource.Error).message,
                        color = Rose500,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle entre Login y Registro
                TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                    Text(
                        text = if (isRegisterMode) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate aquí",
                        color = Cyan500
                    )
                }

                Divider(color = Slate700, modifier = Modifier.padding(vertical = 12.dp))

                // Botón para configurar IP del servidor (vital para pruebas físicas/emulador)
                OutlinedButton(
                    onClick = { showIpDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configurar Servidor / IP", fontSize = 13.sp)
                }
            }
        }
    }

    if (showIpDialog) {
        AlertDialog(
            onDismissRequest = { showIpDialog = false },
            title = { Text("URL del Backend") },
            text = {
                Column {
                    Text(
                        "En emulador usa: http://10.0.2.2:8000/\nEn dispositivo físico usa la IP de tu PC: http://192.168.x.x:8000/",
                        fontSize = 12.sp,
                        color = Slate400,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = customIp,
                        onValueChange = { customIp = it },
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    Constants.BASE_URL = customIp
                    RetrofitClient.resetClient()
                    showIpDialog = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIpDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
