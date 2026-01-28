package pepes.co.trofes.ui.recipe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pepes.co.trofes.R
import pepes.co.trofes.model.RecipeDetail
import pepes.co.trofes.ui.common.YouTubeInlinePlayer

@Composable
fun RecipeDetailScreen(
    recipe: RecipeDetail,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = Color.White, modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 24.dp),
        ) {
            TopBar(onBack = onBack)

            Spacer(Modifier.height(10.dp))

            HeroImage(recipe)

            Spacer(Modifier.height(14.dp))

            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = recipe.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E),
            )

            Spacer(Modifier.height(10.dp))

            RatingRow(recipe)

            Spacer(Modifier.height(14.dp))

            ChipsRow(recipe)

            Spacer(Modifier.height(22.dp))

            Text(
                text = recipe.ingredientsTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )

            Spacer(Modifier.height(12.dp))

            // Placeholder area (layout mockup punya ruang besar di sini)
            // Nanti bisa diganti ke list/compose chips measured ingredients.
            Spacer(Modifier.height(160.dp))

            HorizontalDivider(color = Color(0xFFEAEAEA))

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Instructions",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )

            Spacer(Modifier.height(12.dp))

            Steps(recipe)

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Video Tutorial",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )

            Spacer(Modifier.height(12.dp))

            // Inline YouTube
            YouTubeInlinePlayer(
                youtubeId = recipe.youtubeId,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                tint = Color(0xFF212121),
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "Detail",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(Modifier.weight(1f))
        // Spacer pengganti kanan (biar title center)
        Spacer(Modifier.width(48.dp))
    }
}

@Composable
private fun HeroImage(recipe: RecipeDetail) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
    ) {
        val painter = if (recipe.imageRes != 0) {
            painterResource(recipe.imageRes)
        } else {
            painterResource(R.drawable.banner__1_)
        }

        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun RatingRow(recipe: RecipeDetail) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.ic_heart_filled),
            contentDescription = null,
            tint = Color(0xFF212121),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = String.format(java.util.Locale.US, "%.1f", recipe.rating),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF212121),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "(${recipe.ratingCount})",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF9E9E9E),
        )
    }
}

@Composable
private fun ChipsRow(recipe: RecipeDetail) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        recipe.chips.take(4).forEach { chip ->
            Box(modifier = Modifier.width(80.dp)) {
                InfoChip(chip)
            }
        }
    }
}

@Composable
private fun InfoChip(chip: RecipeDetail.InfoChip) {
    val green = Color(0xFF2E7D32)

    Card(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, green),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .height(44.dp)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            // Placeholder icon circle (kamu bisa ganti dengan icon sesuai gambar)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFF5F5F5)),
            )
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = chip.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF212121),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = chip.value,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF212121),
                )
            }
        }
    }
}

@Composable
private fun Steps(recipe: RecipeDetail) {
    val green = Color(0xFF2E7D32)

    recipe.steps.forEachIndexed { index, step ->
        Text(
            text = "Step ${index + 1}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = green,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = step,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF212121),
        )
        Spacer(Modifier.height(18.dp))
    }
}
