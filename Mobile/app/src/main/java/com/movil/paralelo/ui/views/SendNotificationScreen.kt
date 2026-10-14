package com.movil.paralelo.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.paralelo.ui.theme.*
import com.movil.paralelo.ui.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendNotificationScreen(
    notificationViewModel: NotificationViewModel,
    onBack: () -> Unit
) {
    val uiState by notificationViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Envio de Notificacion",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = Slate900
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Formulario de Notificacion Pub/Sub",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Ingrese los datos del correo. El mensaje sera publicado en Google Cloud Pub/Sub y procesado por Cloud Run.",
                fontSize = 13.sp,
                color = Slate400,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Campo: Correo Destino
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { notificationViewModel.onEmailChanged(it) },
                label = { Text("Correo destino") },
                placeholder = { Text("ejemplo@correo.com") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Slate400)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Indigo500,
                    unfocusedBorderColor = Slate700,
                    focusedLabelColor = Indigo500,
                    unfocusedLabelColor = Slate400
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Campo: Asunto
            OutlinedTextField(
                value = uiState.subject,
                onValueChange = { notificationViewModel.onSubjectChanged(it) },
                label = { Text("Asunto") },
                placeholder = { Text("Asunto del mensaje") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Subject, contentDescription = null, tint = Slate400)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Indigo500,
                    unfocusedBorderColor = Slate700,
                    focusedLabelColor = Indigo500,
                    unfocusedLabelColor = Slate400
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Campo: Mensaje
            OutlinedTextField(
                value = uiState.message,
                onValueChange = { notificationViewModel.onMessageChanged(it) },
                label = { Text("Mensaje") },
                placeholder = { Text("Escriba el contenido del correo aqui...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Message, contentDescription = null, tint = Slate400)
                },
                minLines = 4,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Indigo500,
                    unfocusedBorderColor = Slate700,
                    focusedLabelColor = Indigo500,
                    unfocusedLabelColor = Slate400
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Boton de Envio
            Button(
                onClick = { notificationViewModel.sendNotification() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Enviando...", fontWeight = FontWeight.Bold)
                } else {
                    Text("Enviar Notificacion", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Mensajes de Resultado requeridos por el requerimiento
            uiState.feedbackMessage?.let { feedback ->
                val isSuccess = uiState.isSuccess == true
                val backgroundColor = if (isSuccess) Emerald500.copy(alpha = 0.2f) else Rose500.copy(alpha = 0.2f)
                val borderColor = if (isSuccess) Emerald500 else Rose500
                val textColor = if (isSuccess) Color(0xFF86EFAC) else Color(0xFFFCA5A5)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = feedback,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
