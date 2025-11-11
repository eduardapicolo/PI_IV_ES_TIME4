package br.com.salus
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun SplashScreenVideo(onVideoEnded: () -> Unit) {
    val context = LocalContext.current

    // 1. Criar e lembrar da instância do ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Caminho para o vídeo em res/raw
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.salus_}")
            val mediaItem = MediaItem.fromUri(videoUri)

            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true // Começa a tocar imediatamente
            repeatMode = Player.REPEAT_MODE_OFF // Não repetir
        }
    }

    // 2. Lidar com o ciclo de vida do player
    DisposableEffect(Unit) {
        onDispose {
            // Liberar o player quando o Composable for descartado
            exoPlayer.release()
        }
    }

    // 3. Ouvir o estado do player para saber quando o vídeo termina
    var videoEnded by remember { mutableStateOf(false) }
    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    videoEnded = true
                }
            }
        })
    }

    // 4. Chamar a função de navegação QUANDO o vídeo terminar
    // Usamos LaunchedEffect para garantir que onVideoEnded só seja chamado uma vez
    LaunchedEffect(videoEnded) {
        if (videoEnded) {
            onVideoEnded()
        }
    }

    // 5. Exibir o vídeo usando AndroidView para hospedar a PlayerView
    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false // Esconde os controles de play/pause
            }
        },
        modifier = Modifier.fillMaxSize())
}