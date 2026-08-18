package dev.danielkindl.ocho.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import dev.danielkindl.ocho.ui.theme.JetBrainsMonoFamily
import dev.danielkindl.ocho.R

private const val VIRTUAL_MULTIPLIER = 1_000

/**
 * Infinite-scroll drum-roll picker with snap-to-center behavior.
 * Shows 3 items; the middle one is the selected value.
 * Values wrap around (e.g., 59 → 0 → 1).
 *
 * Layout: the center item is always at [firstVisibleItemIndex + 1],
 * because snap aligns the top-slot item to y=0 with no content padding.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    count: Int,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 44.dp,
    pickerWidth: Dp = 80.dp,
    formatter: (Int) -> String = { "%02d".format(it) },
    contentDescription: String = stringResource(R.string.duration_picker_value),
) {
    val virtualCount = count * VIRTUAL_MULTIPLIER
    // Align to a known multiple so modulo is clean, then back off by 1 so
    // the selected item lands in the CENTER slot (index 1 of the 3 visible items).
    val selectedVirtual = virtualCount / 2 - (virtualCount / 2 % count) + selected
    val initialIndex = (selectedVirtual - 1).coerceAtLeast(0)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val fadeColor = MaterialTheme.colorScheme.background
    val centerIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex + 1 }
    }

    // Notify parent when scroll settles; center = FVI + 1.
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            onSelect((listState.firstVisibleItemIndex + 1) % count)
        }
    }

    // Animate to new position when value changes externally (e.g. preset loaded).
    LaunchedEffect(selected) {
        if (!listState.isScrollInProgress) {
            val currentCenter = (listState.firstVisibleItemIndex + 1) % count
            if (currentCenter != selected) {
                val delta = ((selected - currentCenter) % count + count) % count
                val target = if (delta <= count / 2) {
                    listState.firstVisibleItemIndex + delta
                } else {
                    listState.firstVisibleItemIndex - (count - delta)
                }
                listState.animateScrollToItem(target)
            }
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * 3)
            .width(pickerWidth),
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapBehavior,
            modifier = Modifier
                .matchParentSize()
                .semantics {
                    this.contentDescription = contentDescription
                    stateDescription = formatter(selected)
                },
        ) {
            items(virtualCount) { virtualIndex ->
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = formatter(virtualIndex % count),
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFamily),
                        color = if (virtualIndex == centerIndex) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }

        // Lines framing the selected (center) slot.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.Center)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(8.dp),
                ),
        ) {
            HorizontalDivider(modifier = Modifier.align(Alignment.TopCenter))
            HorizontalDivider(modifier = Modifier.align(Alignment.BottomCenter))
        }

        // Top fade — dims the non-selected items above.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(fadeColor, Color.Transparent))),
        )

        // Bottom fade — dims the non-selected items below.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, fadeColor))),
        )
    }
}
