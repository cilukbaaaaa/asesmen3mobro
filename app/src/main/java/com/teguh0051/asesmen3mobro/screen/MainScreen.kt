package com.teguh0051.asesmen3mobro.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.teguh0051.asesmen3mobro.data.DummyData
import com.teguh0051.asesmen3mobro.model.Barang

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val barangList = remember {
        mutableStateListOf<Barang>().apply {
            addAll(DummyData.getBarang())
        }
    }

    var selectedBarang by remember {
        mutableStateOf<Barang?>(null)
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text("Inventaris Barang Kos")
                }
            )
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    showDialog = true
                }
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah"
                )
            }
        }

    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {

            items(barangList) { barang ->

                BarangCard(
                    barang = barang,
                    onClick = {
                        selectedBarang = barang
                    }
                )
            }
        }

        if (showDialog) {

            BarangDialog(

                onDismiss = {
                    showDialog = false
                },

                onSave = { nama, lokasi ->

                    barangList.add(
                        Barang(
                            id = barangList.size + 1,
                            nama = nama,
                            lokasi = lokasi
                        )
                    )

                    showDialog = false
                }
            )
        }

        selectedBarang?.let { barang ->

            DetailDialog(
                barang = barang,
                onDismiss = {
                    selectedBarang = null
                }
            )
        }
    }
}