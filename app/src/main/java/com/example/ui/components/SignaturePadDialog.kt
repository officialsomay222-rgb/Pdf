package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DigitalSignature
import com.example.model.Point2D
import com.example.ui.theme.CamScannerTeal
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SignaturePadDialog(
    onDismiss: () -> Unit,
    onSignatureConfirmed: (DigitalSignature) -> Unit
) {
    var signerName by remember { mutableStateOf("Verified Signer") }
    var signerTitle by remember { mutableStateOf("Authorized Signatory") }
    var organization by remember { mutableStateOf("Docs Z Verified") }

    val strokes = remember { mutableStateListOf<MutableList<Point2D>>() }
    var currentStroke by remember { mutableStateOf<MutableList<Point2D>?>(null) }

    val dateIso = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()) }
    val simulatedCertId = remember { "PAdES-" + UUID.randomUUID().toString().take(8).uppercase() }
    val simulatedHash = remember { "SHA256:" + UUID.randomUUID().toString().replace("-", "").take(16) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = CamScannerTeal,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Digital Signature",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = signerName,
                    onValueChange = { signerName = it },
                    label = { Text("Signer Name", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("signer_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = signerTitle,
                    onValueChange = { signerTitle = it },
                    label = { Text("Signer Role / Title", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("signer_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Draw Signature:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Interactive Drawing Pad
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, CamScannerTeal.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val newStroke = mutableListOf(Point2D(offset.x, offset.y))
                                    currentStroke = newStroke
                                    strokes.add(newStroke)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentStroke?.add(Point2D(change.position.x, change.position.y))
                                },
                                onDragEnd = { currentStroke = null },
                                onDragCancel = { currentStroke = null }
                            )
                        }
                        .testTag("signature_drawing_canvas")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Baseline guide line
                        drawLine(
                            color = Color(0x3364748B),
                            start = androidx.compose.ui.geometry.Offset(20f, size.height * 0.75f),
                            end = androidx.compose.ui.geometry.Offset(size.width - 20f, size.height * 0.75f),
                            strokeWidth = 1f
                        )

                        // Render strokes
                        for (stroke in strokes) {
                            if (stroke.size > 1) {
                                val path = Path()
                                path.moveTo(stroke[0].x, stroke[0].y)
                                for (i in 1 until stroke.size) {
                                    path.lineTo(stroke[i].x, stroke[i].y)
                                }
                                drawPath(
                                    path = path,
                                    color = Color(0xFF0D9488),
                                    style = Stroke(
                                        width = 3.5f,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }

                    // Clear button
                    IconButton(
                        onClick = { strokes.clear() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(28.dp)
                            .testTag("clear_signature_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (strokes.isEmpty()) {
                        Text(
                            text = "Sign on baseline here...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Certificate Metadata Badge
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VERIFIED DIGITAL AUDIT SEAL",
                                color = Color(0xFF10B981),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Cert ID: $simulatedCertId • Timestamp: $dateIso",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSignatureConfirmed(
                        DigitalSignature(
                            signerName = signerName,
                            signerTitle = signerTitle,
                            organization = organization,
                            timestampIso = dateIso,
                            documentHash = simulatedHash,
                            certificateId = simulatedCertId,
                            points = strokes.map { it.toList() }
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_signature_button")
            ) {
                Text("Apply Signature", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
