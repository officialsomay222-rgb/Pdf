package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class DocViewMode {
    LIST,
    GRID
}

enum class DocSortOption(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    NAME_ASC("Title (A-Z)"),
    NAME_DESC("Title (Z-A)"),
    SIZE_DESC("File Size (Largest)")
}

enum class CamScannerNavTab(val label: String) {
    DOCS("Docs"),
    TOOLS("Tools"),
    SCAN("Scan"),
    FOLDERS("Folders"),
    SETTINGS("Settings")
}

enum class DocumentCategory {
    BLUEPRINT,
    CONTRACT,
    INSPECTION_FORM,
    SPECIFICATION,
    BLANK,
    ID_CARD,
    RECEIPT,
    NOTES,
    BOOK,
    CERTIFICATE
}

enum class NavigationMode {
    CONTINUOUS_SCROLL,
    SINGLE_PAGE,
    TWO_PAGE_FACING
}

enum class WorkspaceToolMode {
    VIEW_NAVIGATE,
    MARKUP_DRAW,
    CAD_MEASURE,
    FORMS_FILL,
    TEXT_EDIT,
    AI_COPILOT,
    SECURITY_ASSEMBLY
}

enum class MarkupType {
    PEN,
    HIGHLIGHTER,
    RECTANGLE,
    ELLIPSE,
    ARROW,
    REVISION_CLOUD,
    CALLOUT,
    TEXT_NOTE,
    STAMP,
    MAGIC_ERASER,
    LASER_POINTER,
    REDACTION_BOX
}

enum class MeasurementType {
    DISTANCE_LINE,
    AREA_POLYGON,
    PERIMETER_PATH,
    ANGLE_ARC
}

enum class FormFieldType {
    TEXT_INPUT,
    CHECKBOX,
    RADIO_GROUP,
    DROPDOWN,
    DATE_PICKER,
    SIGNATURE_BOX
}

enum class StampType(val label: String, val colorHex: Long) {
    APPROVED("APPROVED", 0xFF10B981),
    REJECTED("REJECTED", 0xFFEF4444),
    FOR_CONSTRUCTION("FOR CONSTRUCTION", 0xFF0284C7),
    CONFIDENTIAL("CONFIDENTIAL", 0xFFDC2626),
    DRAFT("DRAFT", 0xFFF59E0B),
    FINAL("FINAL REVIEW", 0xFF8B5CF6),
    PAID("PAID & CERTIFIED", 0xFF059669)
}

data class Point2D(val x: Float, val y: Float)

data class ScaleCalibration(
    val pixelDistance: Float = 100f,
    val realDistance: Float = 10f,
    val unit: String = "ft" // "ft", "in", "m", "mm"
) {
    fun toReal(pixels: Float): Float {
        if (pixelDistance <= 0f) return 0f
        return (pixels / pixelDistance) * realDistance
    }

    fun format(pixels: Float): String {
        val real = toReal(pixels)
        return when (unit) {
            "ft" -> {
                val feet = real.toInt()
                val inches = ((real - feet) * 12).toInt()
                "$feet' - $inches\""
            }
            "m" -> String.format("%.2f m", real)
            "mm" -> String.format("%.0f mm", real)
            "in" -> String.format("%.1f in", real)
            else -> String.format("%.2f %s", real, unit)
        }
    }

    fun formatArea(pixelArea: Float): String {
        if (pixelDistance <= 0f) return "0 sq $unit"
        val scaleRatio = realDistance / pixelDistance
        val realArea = pixelArea * scaleRatio * scaleRatio
        return when (unit) {
            "ft" -> String.format("%,.1f sq ft", realArea)
            "m" -> String.format("%,.2f m²", realArea)
            else -> String.format("%,.1f sq %s", realArea, unit)
        }
    }
}

data class AnnotationItem(
    val id: String = UUID.randomUUID().toString(),
    val pageIndex: Int = 0,
    val type: MarkupType,
    val points: List<Point2D> = emptyList(),
    val colorHex: Long = 0xFF0284C7,
    val strokeWidth: Float = 3f,
    val opacity: Float = 1f,
    val text: String = "",
    val fontSize: Float = 14f,
    val stampType: StampType? = null,
    val author: String = "Architect",
    val timestamp: Long = System.currentTimeMillis()
)

data class MeasurementItem(
    val id: String = UUID.randomUUID().toString(),
    val pageIndex: Int = 0,
    val type: MeasurementType,
    val points: List<Point2D> = emptyList(),
    val colorHex: Long = 0xFFF59E0B,
    val strokeWidth: Float = 2.5f,
    val label: String = "Measurement",
    val scale: ScaleCalibration = ScaleCalibration(),
    val timestamp: Long = System.currentTimeMillis()
)

data class FormFieldItem(
    val id: String = UUID.randomUUID().toString(),
    val pageIndex: Int = 0,
    val type: FormFieldType,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val label: String,
    val value: String = "",
    val isChecked: Boolean = false,
    val options: List<String> = emptyList(),
    val isRequired: Boolean = false,
    val isSigned: Boolean = false
)

data class DigitalSignature(
    val id: String = UUID.randomUUID().toString(),
    val signerName: String,
    val signerTitle: String,
    val organization: String,
    val timestampIso: String,
    val documentHash: String,
    val certificateId: String,
    val points: List<List<Point2D>> = emptyList() // strokes
)

data class CommentThread(
    val id: String = UUID.randomUUID().toString(),
    val pageIndex: Int = 0,
    val position: Point2D,
    val author: String,
    val message: String,
    val status: String = "Open", // "Open", "In Review", "Resolved"
    val timestamp: Long = System.currentTimeMillis(),
    val replies: List<CommentReply> = emptyList()
)

data class CommentReply(
    val id: String = UUID.randomUUID().toString(),
    val author: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class PageRotation(
    val pageIndex: Int,
    val degrees: Int = 0 // 0, 90, 180, 270
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val pageCount: Int,
    val fileSizeFormatted: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val isPasswordProtected: Boolean = false,
    val watermarkText: String = "",
    val scaleRealDistance: Float = 10f,
    val scalePixelDistance: Float = 100f,
    val scaleUnit: String = "ft",
    val filePath: String = "",
    val uriString: String = "",
    val folder: String = "All Docs"
)

data class DocumentWorkspaceTab(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val title: String,
    val category: DocumentCategory,
    val activePageIndex: Int = 0,
    val pageCount: Int = 3,
    val zoomLevel: Float = 1.0f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,
    val isModified: Boolean = false
)
