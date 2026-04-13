package com.beastsaber.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beastsaber.app.R
import com.beastsaber.app.data.filters.BeatSaverFilterCatalog
import com.beastsaber.app.data.filters.FilterOptionKind
import com.beastsaber.app.data.filters.FilterSection
import com.beastsaber.app.data.repo.AutomapperFilter
import com.beastsaber.app.data.repo.DatePreset
import com.beastsaber.app.data.repo.LeaderboardFilter
import com.beastsaber.app.data.repo.MapFilterFormState
import com.beastsaber.app.data.repo.SearchSortOrder
import com.beastsaber.app.data.repo.toggledCatalogOption

private enum class ChipPalette {
    Style,
    Genre,
    EnvLegacy,
    EnvNew
}

@Composable
private fun chipColorsFor(palette: ChipPalette): SelectableChipColors {
    return when (palette) {
        ChipPalette.Style, ChipPalette.EnvLegacy -> FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFF1A2744),
            labelColor = Color(0xFF90CAF9),
            selectedContainerColor = Color(0xFF1565C0),
            selectedLabelColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
        ChipPalette.Genre -> FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFF1B3D2F),
            labelColor = Color(0xFFA5D6A7),
            selectedContainerColor = Color(0xFF2E7D32),
            selectedLabelColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
        ChipPalette.EnvNew -> FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFF2D1F3D),
            labelColor = Color(0xFFE1BEE7),
            selectedContainerColor = Color(0xFF7B1FA2),
            selectedLabelColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutomapperSegmented(
    selected: AutomapperFilter,
    onSelect: (AutomapperFilter) -> Unit
) {
    val items = listOf(
        AutomapperFilter.All to R.string.automapper_all,
        AutomapperFilter.HumanOnly to R.string.automapper_human,
        AutomapperFilter.AiOnly to R.string.automapper_ai
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, (value, labelRes) ->
            SegmentedButton(
                selected = selected == value,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size)
            ) {
                Text(stringResource(labelRes))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaderboardSegmented(
    selected: LeaderboardFilter,
    onSelect: (LeaderboardFilter) -> Unit
) {
    val items = listOf(
        LeaderboardFilter.All to R.string.leaderboard_all,
        LeaderboardFilter.Ranked to R.string.leaderboard_ranked,
        LeaderboardFilter.BeatLeader to R.string.leaderboard_beatleader,
        LeaderboardFilter.ScoreSaber to R.string.leaderboard_scoresaber
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, (value, labelRes) ->
            SegmentedButton(
                selected = selected == value,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size)
            ) {
                Text(stringResource(labelRes), maxLines = 1)
            }
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun MapFilterFormState.sectionAllSelected(section: FilterSection): Boolean =
    section.options.isNotEmpty() && section.options.all { opt ->
        when (opt.kind) {
            FilterOptionKind.Tag -> selectedTagSlugs.contains(opt.apiValue)
            FilterOptionKind.Environment -> selectedEnvironmentIds.contains(opt.apiValue)
        }
    }

private fun MapFilterFormState.withSectionSelectAll(section: FilterSection, selectAll: Boolean): MapFilterFormState {
    var tags = selectedTagSlugs
    var envs = selectedEnvironmentIds
    for (opt in section.options) {
        when (opt.kind) {
            FilterOptionKind.Tag -> {
                tags = if (selectAll) tags + opt.apiValue else tags - opt.apiValue
            }
            FilterOptionKind.Environment -> {
                envs = if (selectAll) envs + opt.apiValue else envs - opt.apiValue
            }
        }
    }
    return copy(selectedTagSlugs = tags, selectedEnvironmentIds = envs)
}

/**
 * Shared filter sheet body for Search and Browse (BeatSaver-style layout).
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BeatSaverFilterSheetContent(
    filters: MapFilterFormState,
    onFiltersChange: (MapFilterFormState) -> Unit,
    showSortOrder: Boolean,
    sortOrder: SearchSortOrder?,
    onSortOrderChange: ((SearchSortOrder) -> Unit)?,
    titleRes: Int,
    onClearFilters: () -> Unit,
    applyButtonLabelRes: Int,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    var advancedOpen by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onClearFilters) {
                Text(stringResource(R.string.clear_filters))
            }
        }
        Spacer(Modifier.height(12.dp))

        if (showSortOrder && sortOrder != null && onSortOrderChange != null) {
            SectionHeader(stringResource(R.string.filter_sheet_section_sort))
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchSortOrder.entries.forEach { o ->
                    FilterChip(
                        selected = sortOrder == o,
                        onClick = { onSortOrderChange(o) },
                        label = { Text(o.name) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        SectionHeader(stringResource(R.string.filter_section_general))
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.filter_mapper_type),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        AutomapperSegmented(filters.automapper) {
            onFiltersChange(filters.copy(automapper = it))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.filter_ranking_status),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        LeaderboardSegmented(filters.leaderboard) {
            onFiltersChange(filters.copy(leaderboard = it))
        }
        Spacer(Modifier.height(8.dp))
        SwitchRow(
            label = stringResource(R.string.filter_curated),
            checked = filters.curated == true,
            onCheckedChange = { on ->
                onFiltersChange(filters.copy(curated = if (on) true else null))
            }
        )
        SwitchRow(
            label = stringResource(R.string.filter_verified_mapper),
            checked = filters.verified == true,
            onCheckedChange = { on ->
                onFiltersChange(filters.copy(verified = if (on) true else null))
            }
        )
        SwitchRow(
            label = stringResource(R.string.filter_full_spread),
            checked = filters.fullSpread == true,
            onCheckedChange = { on ->
                onFiltersChange(filters.copy(fullSpread = if (on) true else null))
            }
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        SectionHeader(stringResource(R.string.filter_section_requirements))
        Spacer(Modifier.height(8.dp))
        SwitchRow(
            label = stringResource(R.string.filter_chroma),
            checked = filters.chroma == true,
            onCheckedChange = { on ->
                onFiltersChange(filters.copy(chroma = if (on) true else null))
            }
        )
        SwitchRow(
            label = stringResource(R.string.filter_noodle),
            checked = filters.noodle == true,
            onCheckedChange = { on ->
                onFiltersChange(filters.copy(noodle = if (on) true else null))
            }
        )
        SwitchRow(
            label = stringResource(R.string.filter_me),
            checked = filters.me == true,
            onCheckedChange = { on ->
                onFiltersChange(filters.copy(me = if (on) true else null))
            }
        )
        SwitchRow(
            label = stringResource(R.string.filter_cinema),
            checked = filters.cinema == true,
            onCheckedChange = { on ->
                onFiltersChange(filters.copy(cinema = if (on) true else null))
            }
        )
        SwitchRow(
            label = stringResource(R.string.filter_vivify),
            checked = filters.vivify == true,
            onCheckedChange = { on ->
                onFiltersChange(filters.copy(vivify = if (on) true else null))
            }
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        SectionHeader(stringResource(R.string.filter_section_tags))
        Spacer(Modifier.height(8.dp))

        val styleSec = remember { BeatSaverFilterCatalog.sections.first { it.id == "style" } }
        val genreSec = remember { BeatSaverFilterCatalog.sections.first { it.id == "genre" } }

        TagSubsection(
            title = stringResource(R.string.filter_section_style),
            section = styleSec,
            filters = filters,
            onFiltersChange = onFiltersChange,
            palette = ChipPalette.Style
        )
        Spacer(Modifier.height(12.dp))
        TagSubsection(
            title = stringResource(R.string.filter_section_genre),
            section = genreSec,
            filters = filters,
            onFiltersChange = onFiltersChange,
            palette = ChipPalette.Genre
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        SectionHeader(stringResource(R.string.filter_section_environments))
        Spacer(Modifier.height(8.dp))

        val legacySec = remember { BeatSaverFilterCatalog.sections.first { it.id == "env_legacy" } }
        val newSec = remember { BeatSaverFilterCatalog.sections.first { it.id == "env_new" } }

        TagSubsection(
            title = stringResource(R.string.filter_env_legacy_short),
            section = legacySec,
            filters = filters,
            onFiltersChange = onFiltersChange,
            palette = ChipPalette.EnvLegacy
        )
        Spacer(Modifier.height(12.dp))
        TagSubsection(
            title = stringResource(R.string.filter_env_new_short),
            section = newSec,
            filters = filters,
            onFiltersChange = onFiltersChange,
            palette = ChipPalette.EnvNew
        )

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { advancedOpen = !advancedOpen }) {
            Text(
                if (advancedOpen) stringResource(R.string.filter_advanced_hide)
                else stringResource(R.string.filter_advanced_show)
            )
        }
        AnimatedVisibility(visible = advancedOpen) {
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        stringResource(R.string.filter_upload_date),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DatePreset.entries.forEach { p ->
                            FilterChip(
                                selected = filters.datePreset == p,
                                onClick = { onFiltersChange(filters.copy(datePreset = p)) },
                                label = { Text(p.shortLabel) }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.filter_sheet_section_range),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = filters.minNps.orEmpty(),
                            onValueChange = {
                                onFiltersChange(filters.copy(minNps = it.takeIf { s -> s.isNotBlank() }))
                            },
                            label = { Text(stringResource(R.string.min_nps)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = filters.maxNps.orEmpty(),
                            onValueChange = {
                                onFiltersChange(filters.copy(maxNps = it.takeIf { s -> s.isNotBlank() }))
                            },
                            label = { Text(stringResource(R.string.max_nps)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = filters.minBpm.orEmpty(),
                            onValueChange = {
                                onFiltersChange(filters.copy(minBpm = it.takeIf { s -> s.isNotBlank() }))
                            },
                            label = { Text(stringResource(R.string.min_bpm)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = filters.maxBpm.orEmpty(),
                            onValueChange = {
                                onFiltersChange(filters.copy(maxBpm = it.takeIf { s -> s.isNotBlank() }))
                            },
                            label = { Text(stringResource(R.string.max_bpm)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(applyButtonLabelRes))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagSubsection(
    title: String,
    section: FilterSection,
    filters: MapFilterFormState,
    onFiltersChange: (MapFilterFormState) -> Unit,
    palette: ChipPalette
) {
    val allOn = filters.sectionAllSelected(section)
    val colors = chipColorsFor(palette)
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = allOn,
                onCheckedChange = { checked ->
                    onFiltersChange(filters.withSectionSelectAll(section, checked))
                }
            )
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = when (palette) {
                    ChipPalette.Style, ChipPalette.EnvLegacy -> Color(0xFF90CAF9)
                    ChipPalette.Genre -> Color(0xFFA5D6A7)
                    ChipPalette.EnvNew -> Color(0xFFE1BEE7)
                }
            )
        }
        Spacer(Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            section.options.forEach { opt ->
                val selected = when (opt.kind) {
                    FilterOptionKind.Tag -> filters.selectedTagSlugs.contains(opt.apiValue)
                    FilterOptionKind.Environment -> filters.selectedEnvironmentIds.contains(opt.apiValue)
                }
                FilterChip(
                    selected = selected,
                    onClick = {
                        onFiltersChange(filters.toggledCatalogOption(opt.kind, opt.apiValue))
                    },
                    label = { Text(opt.label) },
                    colors = colors
                )
            }
        }
    }
}
