package com.movil.paralelo.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.paralelo.data.model.User
import com.movil.paralelo.ui.theme.*
import com.movil.paralelo.ui.viewmodel.UsersViewModel
import com.movil.paralelo.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    usersViewModel: UsersViewModel,
    onBack: () -> Unit
) {
    val usersState by usersViewModel.usersListState.collectAsState()
    val userOpState by usersViewModel.userOpState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        usersViewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios (CRUD)", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { usersViewModel.loadUsers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Indigo500,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Usuario")
            }
        },
        containerColor = Slate900
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = usersState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        color = Indigo500,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Resource.Success -> {
                    val users = state.data
                    if (users.isEmpty()) {
                        Text(
                            "No hay usuarios registrados.",
                            color = Slate400,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(users, key = { it.id }) { user ->
                                UserCard(
                                    user = user,
                                    onEdit = { userToEdit = user },
                                    onDelete = { userToDelete = user }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.message}", color = Rose500)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { usersViewModel.loadUsers() }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {}
            }
        }
    }

    // Modal Crear Usuario
    if (showCreateDialog) {
        CreateUserDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { nombre, apellido, email, pass, foto ->
                usersViewModel.createUser(nombre, apellido, email, pass, foto)
                showCreateDialog = false
            }
        )
    }

    // Modal Editar Usuario
    userToEdit?.let { user ->
        EditUserDialog(
            user = user,
            onDismiss = { userToEdit = null },
            onConfirm = { nombre, apellido, email, foto ->
                usersViewModel.updateUser(user.id, nombre, apellido, email, foto)
                userToEdit = null
            }
        )
    }

    // Modal Confirmar Eliminación
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Estás seguro de eliminar a ${user.nombre} ${user.apellido}?") },
            confirmButton = {
                Button(
                    onClick = {
                        usersViewModel.deleteUser(user.id)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UserCard(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Iniciales Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Indigo600, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${user.nombre.take(1)}${user.apellido.take(1)}".uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.nombre} ${user.apellido}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = user.email,
                    color = Slate400,
                    fontSize = 13.sp
                )
                user.createdAt?.let {
                    Text(
                        text = "Registrado: ${it.take(10)}",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Cyan500)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Rose500)
            }
        }
    }
}

@Composable
fun CreateUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, apellido: String, email: String, pass: String, foto: String?) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var foto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = apellido, onValueChange = { apellido = it }, label = { Text("Apellido") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") })
                OutlinedTextField(value = foto, onValueChange = { foto = it }, label = { Text("URL Foto (opcional)") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, apellido, email, pass, foto.ifBlank { null }) }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, apellido: String, email: String, foto: String?) -> Unit
) {
    var nombre by remember { mutableStateOf(user.nombre) }
    var apellido by remember { mutableStateOf(user.apellido) }
    var email by remember { mutableStateOf(user.email) }
    var foto by remember { mutableStateOf(user.foto ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = apellido, onValueChange = { apellido = it }, label = { Text("Apellido") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = foto, onValueChange = { foto = it }, label = { Text("URL Foto (opcional)") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, apellido, email, foto.ifBlank { null }) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
