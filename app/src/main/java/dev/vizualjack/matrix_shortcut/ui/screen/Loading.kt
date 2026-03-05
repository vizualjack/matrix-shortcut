package dev.vizualjack.matrix_shortcut.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.R
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme


@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize()) {
//        Text("Loading...", Modifier.align(Alignment.Center))
        CircularProgressIndicator(Modifier.size(150.dp).align(Alignment.Center))
        Image(
            painter = painterResource(R.drawable.splash_icon),
            contentDescription = null,
            modifier = Modifier.size(288.dp).align(Alignment.Center)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoadingScreen()
        }
    }
}