package com.example.data

import com.example.model.*
import java.util.UUID

object SampleDocuments {
    val sampleEntities = listOf(
        DocumentEntity(
            id = "doc-blueprint-01",
            title = "Structural Blueprint - Level 3 HVAC & Framing (Rev 4.2)",
            category = DocumentCategory.BLUEPRINT.name,
            pageCount = 4,
            fileSizeFormatted = "18.4 MB",
            createdAt = System.currentTimeMillis() - 86400000L * 3,
            modifiedAt = System.currentTimeMillis() - 3600000L * 2,
            isPasswordProtected = false,
            watermarkText = "CONFIDENTIAL - STAGE 4 BIDDING",
            scaleRealDistance = 10f,
            scalePixelDistance = 100f,
            scaleUnit = "ft"
        ),
        DocumentEntity(
            id = "doc-contract-02",
            title = "Master Services Agreement & SOW - Project Apex",
            category = DocumentCategory.CONTRACT.name,
            pageCount = 3,
            fileSizeFormatted = "3.2 MB",
            createdAt = System.currentTimeMillis() - 86400000L * 7,
            modifiedAt = System.currentTimeMillis() - 3600000L * 5,
            isPasswordProtected = true,
            watermarkText = "LEGAL PRIVILEGED - DRAFT",
            scaleRealDistance = 10f,
            scalePixelDistance = 100f,
            scaleUnit = "ft"
        ),
        DocumentEntity(
            id = "doc-inspection-03",
            title = "Commercial Building Permit & Environmental Inspection Form",
            category = DocumentCategory.INSPECTION_FORM.name,
            pageCount = 2,
            fileSizeFormatted = "1.8 MB",
            createdAt = System.currentTimeMillis() - 86400000L * 1,
            modifiedAt = System.currentTimeMillis() - 1800000L,
            isPasswordProtected = false,
            watermarkText = "",
            scaleRealDistance = 10f,
            scalePixelDistance = 100f,
            scaleUnit = "ft"
        )
    )

    fun getInitialAnnotations(docId: String): List<AnnotationItem> {
        return when (docId) {
            "doc-blueprint-01" -> listOf(
                AnnotationItem(
                    id = "ann-1",
                    pageIndex = 0,
                    type = MarkupType.REVISION_CLOUD,
                    points = listOf(
                        Point2D(120f, 180f),
                        Point2D(340f, 180f),
                        Point2D(340f, 320f),
                        Point2D(120f, 320f)
                    ),
                    colorHex = 0xFFEF4444, // Red revision cloud
                    strokeWidth = 3f,
                    author = "Chief Structural Eng."
                ),
                AnnotationItem(
                    id = "ann-2",
                    pageIndex = 0,
                    type = MarkupType.CALLOUT,
                    points = listOf(
                        Point2D(340f, 250f),
                        Point2D(420f, 220f)
                    ),
                    colorHex = 0xFFEF4444,
                    text = "Verify shear wall studs @ 16\" O.C. per IBC 2024",
                    author = "Chief Structural Eng."
                ),
                AnnotationItem(
                    id = "ann-3",
                    pageIndex = 0,
                    type = MarkupType.STAMP,
                    points = listOf(Point2D(520f, 100f)),
                    stampType = StampType.FOR_CONSTRUCTION,
                    author = "City Engineering Dept."
                ),
                AnnotationItem(
                    id = "ann-4",
                    pageIndex = 0,
                    type = MarkupType.HIGHLIGHTER,
                    points = listOf(
                        Point2D(80f, 480f),
                        Point2D(300f, 480f),
                        Point2D(300f, 520f),
                        Point2D(80f, 520f)
                    ),
                    colorHex = 0xFFFBBF24, // Amber translucent
                    opacity = 0.45f,
                    author = "HVAC Lead"
                )
            )
            "doc-contract-02" -> listOf(
                AnnotationItem(
                    id = "ann-c1",
                    pageIndex = 0,
                    type = MarkupType.REDACTION_BOX,
                    points = listOf(
                        Point2D(100f, 340f),
                        Point2D(350f, 375f)
                    ),
                    colorHex = 0xFF000000,
                    text = "REDACTED: SSN 982-45-XXXX",
                    author = "Compliance Officer"
                ),
                AnnotationItem(
                    id = "ann-c2",
                    pageIndex = 0,
                    type = MarkupType.STAMP,
                    points = listOf(Point2D(480f, 80f)),
                    stampType = StampType.CONFIDENTIAL,
                    author = "Legal Counsel"
                )
            )
            else -> emptyList()
        }
    }

    fun getInitialMeasurements(docId: String): List<MeasurementItem> {
        val ftScale = ScaleCalibration(pixelDistance = 100f, realDistance = 10f, unit = "ft")
        return when (docId) {
            "doc-blueprint-01" -> listOf(
                MeasurementItem(
                    id = "meas-1",
                    pageIndex = 0,
                    type = MeasurementType.DISTANCE_LINE,
                    points = listOf(Point2D(80f, 140f), Point2D(380f, 140f)),
                    colorHex = 0xFF0284C7,
                    strokeWidth = 3f,
                    label = "North Main Span",
                    scale = ftScale
                ),
                MeasurementItem(
                    id = "meas-2",
                    pageIndex = 0,
                    type = MeasurementType.DISTANCE_LINE,
                    points = listOf(Point2D(80f, 140f), Point2D(80f, 440f)),
                    colorHex = 0xFF0284C7,
                    strokeWidth = 3f,
                    label = "West Corridor Width",
                    scale = ftScale
                ),
                MeasurementItem(
                    id = "meas-3",
                    pageIndex = 0,
                    type = MeasurementType.AREA_POLYGON,
                    points = listOf(
                        Point2D(120f, 200f),
                        Point2D(300f, 200f),
                        Point2D(300f, 360f),
                        Point2D(120f, 360f)
                    ),
                    colorHex = 0xFF10B981,
                    strokeWidth = 2.5f,
                    label = "Conference Suite Alpha",
                    scale = ftScale
                )
            )
            else -> emptyList()
        }
    }

    fun getInitialFormFields(docId: String): List<FormFieldItem> {
        return when (docId) {
            "doc-inspection-03" -> listOf(
                FormFieldItem(
                    id = "ff-1",
                    pageIndex = 0,
                    type = FormFieldType.TEXT_INPUT,
                    x = 80f,
                    y = 170f,
                    width = 240f,
                    height = 38f,
                    label = "General Contractor",
                    value = "Vanguard Heavy Civil Ltd."
                ),
                FormFieldItem(
                    id = "ff-2",
                    pageIndex = 0,
                    type = FormFieldType.TEXT_INPUT,
                    x = 340f,
                    y = 170f,
                    width = 220f,
                    height = 38f,
                    label = "Master License #",
                    value = "CA-ENG-849204-B"
                ),
                FormFieldItem(
                    id = "ff-3",
                    pageIndex = 0,
                    type = FormFieldType.CHECKBOX,
                    x = 80f,
                    y = 240f,
                    width = 28f,
                    height = 28f,
                    label = "Seismic Bracing Installed (Zone 4 Compliant)",
                    isChecked = true
                ),
                FormFieldItem(
                    id = "ff-4",
                    pageIndex = 0,
                    type = FormFieldType.CHECKBOX,
                    x = 80f,
                    y = 285f,
                    width = 28f,
                    height = 28f,
                    label = "Fire Suppression Flow Rate Verified (120 PSI)",
                    isChecked = true
                ),
                FormFieldItem(
                    id = "ff-5",
                    pageIndex = 0,
                    type = FormFieldType.CHECKBOX,
                    x = 80f,
                    y = 330f,
                    width = 28f,
                    height = 28f,
                    label = "ADA Wheelchair Ingress Ramp 1:12 Verified",
                    isChecked = false
                ),
                FormFieldItem(
                    id = "ff-6",
                    pageIndex = 0,
                    type = FormFieldType.DROPDOWN,
                    x = 80f,
                    y = 390f,
                    width = 240f,
                    height = 38f,
                    label = "Inspection Result",
                    value = "CONDITIONAL APPROVAL",
                    options = listOf("PASSED - APPROVED", "CONDITIONAL APPROVAL", "REJECTED - RE-INSPECT", "INCOMPLETE")
                ),
                FormFieldItem(
                    id = "ff-7",
                    pageIndex = 0,
                    type = FormFieldType.SIGNATURE_BOX,
                    x = 80f,
                    y = 470f,
                    width = 280f,
                    height = 90f,
                    label = "Certified Inspector Signature",
                    value = "Verified by Digital Token #8491-X",
                    isSigned = true
                )
            )
            "doc-contract-02" -> listOf(
                FormFieldItem(
                    id = "ff-c1",
                    pageIndex = 2,
                    type = FormFieldType.SIGNATURE_BOX,
                    x = 80f,
                    y = 480f,
                    width = 280f,
                    height = 90f,
                    label = "Client Authorized Signatory",
                    value = "",
                    isSigned = false
                ),
                FormFieldItem(
                    id = "ff-c2",
                    pageIndex = 2,
                    type = FormFieldType.SIGNATURE_BOX,
                    x = 380f,
                    y = 480f,
                    width = 280f,
                    height = 90f,
                    label = "Provider Authorized Officer",
                    value = "",
                    isSigned = false
                )
            )
            else -> emptyList()
        }
    }

    fun getInitialComments(docId: String): List<CommentThread> {
        return when (docId) {
            "doc-blueprint-01" -> listOf(
                CommentThread(
                    id = "comm-1",
                    pageIndex = 0,
                    position = Point2D(340f, 210f),
                    author = "Sarah Lin (AIA)",
                    message = "@David please confirm duct clearance above suspended ceiling grid before pouring concrete slab.",
                    status = "Open",
                    replies = listOf(
                        CommentReply(
                            id = "rep-1",
                            author = "David Miller (MEP)",
                            message = "Confirmed 18\" clearance on section D-D. Revision stamped."
                        )
                    )
                )
            )
            else -> emptyList()
        }
    }

    fun getDocumentText(docId: String): String {
        return when (docId) {
            "doc-blueprint-01" -> """
                PROJECT: PACIFIC HORIZON TECH TOWER - PHASE 3
                SHEET: S-301 - LEVEL 3 HVAC DUCTING & SHEAR FRAMING
                SCALE: 1/4" = 1'-0" (CALIBRATION: 100 PX = 10.0 FT)
                ENGINEER OF RECORD: AUSTIN & PARTNERS STRUCTURAL LLP
                GENERAL NOTES:
                1. ALL STRUCTURAL STEEL SHALL CONFORM TO ASTM A992 GRADE 50.
                2. FIREPROOFING: 2-HOUR RATED SPRAY-APPLIED MINERAL FIBER ON ALL PRIMARY GIRDERS.
                3. SEISMIC CATEGORY D - MOMENT RESISTING FRAMES WITH COMPLETE PENETRATION WELDS.
                4. REVISION 4.2: EXPANDED DUCT PENETRATIONS IN GRID LINE C-4 TO ACCOMMODATE CHILLED WATER RISER.
                ROOM SCHEDULE:
                - ROOM 301: EXECUTIVE BOARDROOM (32' x 24' - 768 SQ FT)
                - ROOM 302: MECHANICAL SHAFT & ELECTRICAL RISER (14' x 18')
                - ROOM 303: DATA CENTER / SERVER ROOM (CRAC REDUNDANT N+1)
                - ROOM 304: RESTROOM CORE (ADA COMPLIANT)
            """.trimIndent()

            "doc-contract-02" -> """
                MASTER SERVICES AGREEMENT & SCOPE OF WORK
                CONTRACT ID: APX-2026-9042
                PARTIES:
                - APEX DIGITAL SYSTEMS INC. ("CLIENT"), 100 Montgomery St, San Francisco, CA.
                - VORTEX INFRASTRUCTURE CORP ("CONTRACTOR"), 450 Lexington Ave, New York, NY.
                
                SECTION 1: CONFIDENTIALITY & NON-DISCLOSURE
                All proprietary blueprints, database schemas, cryptographic certificates, and financial models remain strictly confidential.
                
                SECTION 2: COMPENSATION & PAYMENT SCHEDULE
                Total Contract Sum: $1,450,000 USD payable across 4 milestone deliverables.
                Direct wire deposit account: routing #021000021, account #98234-884-12.
                Signatory SSN: 982-45-7712 (CONFIDENTIAL PII).
                Contact Email: somay.director@apexenterprise.internal
                Credit card on file for recurring cloud infrastructure: 4532-8921-9042-3319.
                
                SECTION 3: GOVERNING LAW & JURISDICTION
                This Agreement shall be governed by and construed in accordance with the laws of the State of Delaware.
            """.trimIndent()

            "doc-inspection-03" -> """
                DEPARTMENT OF BUILDING AND SAFETY - COMPLIANCE DIVISION
                PERMIT AND OCCUPANCY CERTIFICATION REPORT
                PROJECT ADDRESS: 742 EVERGREEN PARKWAY, SECTOR 9
                INSPECTION TYPE: STRUCTURAL, SEISMIC, AND LIFE SAFETY
                CONTRACTOR: Vanguard Heavy Civil Ltd.
                MASTER LICENSE: CA-ENG-849204-B
                
                CHECKLIST:
                - Seismic Bracing Installed (Zone 4 Compliant): VERIFIED
                - Fire Suppression Flow Rate (120 PSI): PASS
                - Emergency Egress Lighting & Signage: PASS
                - ADA Ingress Ramp Gradient (1:12): PENDING HANDRAIL CALIBRATION
                
                FINAL DETERMINATION:
                CONDITIONAL APPROVAL ISSUED FOR INTERIOR FIT-OUT ONLY.
                CERTIFICATE OF OCCUPANCY CONTINGENT ON FINAL ADA RE-INSPECTION.
            """.trimIndent()

            else -> "Document text unavailable."
        }
    }
}
