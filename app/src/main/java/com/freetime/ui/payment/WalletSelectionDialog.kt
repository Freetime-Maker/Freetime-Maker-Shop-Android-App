package com.freetime.ui.payment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freetime.domain.payment.ExternalWalletApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSelectionDialog(
    wallets: List<ExternalWalletApp>,
    onWalletSelected: (ExternalWalletApp) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = "Select Wallet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(wallets) { wallet ->
                    WalletItem(
                        wallet = wallet,
                        onWalletSelected = onWalletSelected,
                        onDismiss = onDismiss
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun WalletItem(
    wallet: ExternalWalletApp,
    onWalletSelected: (ExternalWalletApp) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onWalletSelected(wallet)
                onDismiss()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Supports: ${wallet.supportedCoins.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (wallet.isInstalled) {
                SuggestionChip(
                    onClick = { },
                    label = { Text("Installed") }
                )
            } else {
                OutlinedButton(
                    onClick = { /* Handle install */ },
                    modifier = Modifier.heightIn(min = 32.dp)
                ) {
                    Text("Install", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
