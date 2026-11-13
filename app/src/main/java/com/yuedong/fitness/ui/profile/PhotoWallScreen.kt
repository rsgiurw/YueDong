package com.yuedong.fitness.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.PhotoWallEntity
import com.yuedong.fitness.ui.components.AppMedia
import com.yuedong.fitness.ui.components.EmptyState
import com.yuedong.fitness.ui.components.LocalMediaImage
import com.yuedong.fitness.util.AppClock
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 身材照片墙:按日期保存本地对比照,长按删除。
 * 纯本地:图片复制进 filesDir/media,记录入库。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoWallScreen(
    onBack: () -> Unit,
    viewModel: PhotoWallViewModel = viewModel(factory = PhotoWallViewModel.factory())
) {
    val photos by viewModel.photos.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var deleteTarget by remember { mutableStateOf<PhotoWallEntity?>(null) }
    var preview by remember { mutableStateOf<PhotoWallEntity?>(null) }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = AppMedia.loadBitmap(context.applicationContext, uri, maxDim = 1440)
                val bmpScaled = bmp ?: return@launch
                val path = AppMedia.saveBitmap(context.applicationContext, bmpScaled)
                if (path != null) viewModel.add(AppClock.todayString(), path)
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                "身材照片墙",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = "添加照片", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Text(
            "记录身体变化 · 长按照片可删除",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        if (photos.isEmpty()) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                EmptyState("还没有照片\n点右上角相机,记录现在的自己")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(photos, key = { it.id }) { photo ->
                    Column {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4f)
                                .clip(MaterialTheme.shapes.small)
                                .combinedClickable(
                                    onClick = { preview = photo },
                                    onLongClick = { deleteTarget = photo }
                                )
                        ) {
                            LocalMediaImage(
                                path = photo.path,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            photo.date,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // 全屏预览:点照片放大查看,底部提供删除
    preview?.let { photo ->
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { preview = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black)
                    .clickable { preview = null }
            ) {
                LocalMediaImage(
                    path = photo.path,
                    contentDescription = "照片预览",
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    photo.date,
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(16.dp)
                )
            }
        }
    }

    deleteTarget?.let { target ->
        com.yuedong.fitness.ui.components.YueDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除这张照片?", fontWeight = FontWeight.Bold) },
            text = { Text("${target.date} 的照片将从照片墙移除,本地文件同时删除。") },
            confirmButton = {
                Text(
                    "删除",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable {
                            scope.launch {
                                AppMedia.delete(target.path)
                                viewModel.delete(target.id)
                                deleteTarget = null
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            },
            dismissButton = {
                Text(
                    "取消",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clickable { deleteTarget = null }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        )
    }
}

/** 照片墙 ViewModel */
class PhotoWallViewModel(
    private val repository: com.yuedong.fitness.data.repository.PhotoWallRepository
) : ViewModel() {

    val photos = repository.observeAll()
        .stateIn(
            viewModelScope,
            kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun add(date: String, path: String) {
        viewModelScope.launch { repository.add(date, path) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                PhotoWallViewModel(app.container.photoWallRepository)
            }
        }
    }
}
