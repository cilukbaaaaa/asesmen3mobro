package com.teguh0051.asesmen3mobro.data

import com.teguh0051.asesmen3mobro.model.Barang

object DummyData {

    fun getBarang(): List<Barang> {
        return listOf(
            Barang(
                id = 1,
                nama = "Laptop Asus",
                lokasi = "Meja Belajar"
            ),
            Barang(
                id = 2,
                nama = "Mouse Logitech",
                lokasi = "Laci"
            ),
            Barang(
                id = 3,
                nama = "Printer Epson",
                lokasi = "Rak"
            )
        )
    }
}