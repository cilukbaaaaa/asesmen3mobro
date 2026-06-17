package com.teguh0051.asesmen3mobro.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun BarangDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {

    var nama by remember {
        mutableStateOf("")
    }

    var lokasi by remember {
        mutableStateOf("")
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {

        Card {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                OutlinedTextField(
                    value = nama,
                    onValueChange = {
                        nama = it
                    },
                    label = {
                        Text("Nama Barang")
                    }
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = lokasi,
                    onValueChange = {
                        lokasi = it
                    },
                    label = {
                        Text("Lokasi")
                    }
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = {
                        onSave(
                            nama,
                            lokasi
                        )
                    }
                ) {
                    Text("Simpan")
                }
            }
        }
    }
}