package com.yourname.aplikasitrackingpendayagunaan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FormItem(
    val title: String,
    val actions: List<String>
)


@Preview(showBackground = true)
@Composable
fun FormPengadaanScreen() {
    val sections = listOf(
        FormItem("Pengadaan Barang", listOf("Tambahkan Text", "Upload Foto")),
        FormItem("Penyerahan Barang", listOf("Tambah Bukti")),
        FormItem("Monitoring Bulanan", listOf("Tambah Bukti")),
        FormItem("Evaluasi Progress", listOf("Tambah Bukti")),
        FormItem("Tindak Lanjut", listOf("Tambah Bukti")),
        FormItem("Laporan Periode", listOf("Tambah Bukti")),
        FormItem("Laporan Final", listOf("Tambah Bukti"))
    )




    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape =     RoundedCornerShape()
            ) {  }
        }




    Button(
        onClick = {},
        modifier = Modifier
            .padding(16.dp)
            .align ( Alignment.CenterHorizontally)  ,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6650A4)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("UPZPRENEUR", fontWeight = FontWeight.Bold)
    }


        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal =  16.dp)
        ) {
            item {
                Text(
                    text = "Ananda Aulia Hanifah",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            itemsIndexed(sections) { index, section ->
                SectionWithDashedLine(
                    title = section.title,
                    actions = section.actions,
                    isLast = index == sections.lastIndex
                )
            }
        }


    }




}

@Composable
fun SectionWithDashedLine(
    title: String,
    actions: List<String>,
    isLast: Boolean
) {
    val dotColor = Color(0xFF5B8DEF)
    val dashColor = Color(0xFFAAAAAA)
    val dotRadiusDp = 8.dp

    Row(modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)
    ) {

        // Kolom kiri: dot besar + garis vertikal penghubung antar section
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val dotRadius = dotRadiusDp.toPx()
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                // Dot utama (biru) di header proses
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(centerX, dotRadius + 2f)
                )

                // Garis vertikal putus-putus ke bawah (penghubung ke section berikutnya)
                if (!isLast) {
                    drawLine(
                        color = dashColor,
                        start = Offset(centerX, dotRadius * 2 + 16f),
                        end = Offset(centerX, size.height),
                        strokeWidth = 2f,
                    )
                }
            }
        }

        // Kolom kanan: judul + branch actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, bottom = 16.dp)
        ) {
            // Header judul proses
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 0.dp, bottom = 8.dp)
            )

            // Branch tiap action
            actions.forEachIndexed { index, action ->
                val isLastAction = index == actions.lastIndex

                Row(modifier = Modifier.fillMaxWidth()) {

                    // Garis L putus-putus di kiri branch
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(48.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            val startX = 8f
                            val midY = size.height / 2
                            val endX = size.width

                            // Garis vertikal (hanya ke tengah kalau action terakhir)
                            drawLine(
                                color = dashColor,
                                start = Offset(startX, 0f),
                                end = Offset(startX, if (isLastAction) midY else size.height),
                                strokeWidth = 2f,
                                pathEffect = dashEffect
                            )

                            // Garis horizontal ke kanan
                            drawLine(
                                color = dashColor,
                                start = Offset(startX, midY),
                                end = Offset(endX, midY),
                                strokeWidth = 2f,
                                pathEffect = dashEffect
                            )
                        }
                    }

                    // Teks action + button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• $action",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6650A4)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+ $action",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}