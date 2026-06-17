package com.teguh0051.asesmen3mobro.screen

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import com.teguh0051.asesmen3mobro.model.Barang

@Composable
fun DetailDialog(
    barang: Barang,
    onDismiss: () -> Unit
) {

    Dialog(
        onDismissRequest = onDismiss
    ) {

        Card {

            Text(
                text = "Nama: ${barang.nama}"
            )

            Text(
                text = "Lokasi: ${barang.lokasi}"
            )
        }
    }
}