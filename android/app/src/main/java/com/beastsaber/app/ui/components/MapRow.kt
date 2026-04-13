package com.beastsaber.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.beastsaber.app.R
import com.beastsaber.app.data.model.MapDetail
import com.beastsaber.app.data.model.displaySongName
import com.beastsaber.app.data.model.primaryVersion

@Composable
fun MapRow(
    map: MapDetail,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPreviewClick: (() -> Unit)? = null,
    previewShowsPause: Boolean = false,
    onAddToPlaylistClick: (() -> Unit)? = null
) {
    val cover = map.primaryVersion()?.coverURL
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cover != null) {
                    AsyncImage(
                        model = cover,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        map.displaySongName(),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    val artist = map.metadata?.songAuthorName?.takeIf { it.isNotBlank() }
                    if (artist != null) {
                        Text(
                            artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val mapper = map.metadata?.levelAuthorName?.takeIf { it.isNotBlank() }
                    val uploader = map.uploader?.name
                    val mapperLine = mapper ?: uploader.orEmpty()
                    if (mapperLine.isNotBlank() && mapperLine != artist) {
                        Text(
                            mapperLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (onPreviewClick != null || onAddToPlaylistClick != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (onPreviewClick != null) {
                        IconButton(onClick = onPreviewClick) {
                            Icon(
                                imageVector = if (previewShowsPause) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = stringResource(
                                    if (previewShowsPause) R.string.preview_pause else R.string.preview_play
                                )
                            )
                        }
                    }
                    if (onAddToPlaylistClick != null) {
                        IconButton(
                            onClick = onAddToPlaylistClick,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlaylistAdd,
                                contentDescription = stringResource(R.string.map_row_add_to_list)
                            )
                        }
                    }
                }
            }
        }
    }
}
