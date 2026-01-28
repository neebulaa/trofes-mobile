package pepes.co.trofes.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pepes.co.trofes.R

/**
 * UI model for recommendation card (mirrors the existing XML cards).
 */
data class RecommendationCardUi(
    val id: String,
    val title: String,
    val rating: String,
    val likesCount: Int,
    val caloriesText: String,
    val timeText: String,
    val tagText: String,
    val tagColor: Color,
    @DrawableRes val imageRes: Int,
)

@Composable
fun RecommendationRow(
    items: List<RecommendationCardUi>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.wrapContentHeight(),
        // XML: paddingStart=24dp, paddingEnd=14dp
        contentPadding = PaddingValues(start = 24.dp, end = 14.dp),
        // XML: card marginEnd=10dp
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items, key = { it.id }) { item ->
            RecommendationCard(item = item)
        }
    }
}

@Composable
private fun RecommendationCard(
    item: RecommendationCardUi,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var liked by remember(item.id) { mutableStateOf(false) }

    val shownLikes = item.likesCount + if (liked) 1 else 0

    OutlinedCard(
        // XML: android:layout_width="164dp"
        modifier = modifier.width(164.dp),
        // XML: app:cardCornerRadius="18dp"
        shape = RoundedCornerShape(18.dp),
        // XML: cardElevation=0dp + white background
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        // XML: strokeWidth=1dp, strokeColor=#E0E0E0
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
    ) {
        Column {
            Box(
                modifier = Modifier
                    // XML: FrameLayout height="120dp"
                    .height(120.dp)
                    .fillMaxWidth(),
            ) {
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Rating chip (top-end)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "★",
                            color = Color(0xFFFFC107),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.rating,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Text(
                text = item.title,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1F1F),
            )

            Row(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val heartRes = if (liked) R.drawable.ic_heart_filled else R.drawable.ic_heart

                Icon(
                    painter = painterResource(id = heartRes),
                    contentDescription = context.getString(R.string.cd_recommendation_like),
                    tint = if (liked) Color(0xFFE53935) else Color(0xFF424242),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { liked = !liked },
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = shownLikes.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F),
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Calories
                Icon(
                    painter = painterResource(id = R.drawable.ic_fire),
                    contentDescription = null,
                    tint = Color(0xFF1F1F1F),
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.caloriesText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F),
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Time
                Icon(
                    painter = painterResource(id = R.drawable.ic_clock),
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.timeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F),
                )
            }

            Surface(
                modifier = Modifier
                    .padding(start = 12.dp, end = 10.dp, top = 8.dp, bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = item.tagColor,
            ) {
                Text(
                    text = item.tagText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
