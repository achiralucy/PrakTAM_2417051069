package com.example.praktam_2417051069

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.praktam_2417051069.model.StudentPlanner
import com.example.praktam_2417051069.model.StudentSource
import com.example.praktam_2417051069.ui.theme.PrakTAM_2417051069Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrakTAM_2417051069Theme {
                // Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                //     Greeting(
                //         name = "Achira Desya Lucy",
                //         npm = "2417051069",
                //         modifier = Modifier.padding(innerPadding)
                //     )
                // }
                DaftarTugas()
            }
        }
    }
}

//@Composable
//fun Greeting(name: String, npm: String, modifier: Modifier = Modifier) {
//
//    val student = StudentSource.dummystudentplanner[0]
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(24.dp)
//    ) {
//
//        Text(text = "Halo $name saya dengan $npm siap belajar compose")
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//        Text(text = "Nama: ${student.nama}")
//        Text(text = "Deadline: ${student.deadline}")
//        Text(text = "Status: ${student.status}")
//
//        Image(
//            painter = painterResource(id = student.gambar),
//            contentDescription = student.nama,
//            modifier = Modifier.size(200.dp),
//            contentScale = ContentScale.Crop
//        )
//    }
//}

@Composable
fun DaftarTugas() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())

    ) {
        StudentSource.dummystudentplanner.forEach { student ->
            DetailScreen(studentplanner = student)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetailScreen(studentplanner: StudentPlanner) {
    var isFavorite by remember { mutableStateOf(false) }
    var isDone by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Box {
                Image(
                    painter = painterResource(id = studentplanner.gambar),
                    contentDescription = studentplanner.nama,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite Icon",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = studentplanner.nama,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Deadline: ${studentplanner.deadline}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = studentplanner.deskripsi,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { isDone = !isDone },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isDone) "Done" else "Not Yet")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DaftarTugasPreview() {
    PrakTAM_2417051069Theme {
        DetailScreen(studentplanner = StudentSource.dummystudentplanner[0])
    }
}
//fun GreetingPreview() {
//    PrakTAM_2417051069Theme {
//        Greeting(name = "Achira Desya Lucy", npm = "2417051069")
//    }
//}