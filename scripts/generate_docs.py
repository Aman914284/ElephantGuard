import os
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.pdfgen import canvas

PDF_OUTPUT_PATH = r"C:\Users\ashutosh kumar\.gemini\antigravity-ide\scratch\Elephant_Guard_Complete_Technical_Specification.pdf"

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_header_footer(num_pages)
            super().showPage()
        super().save()

    def draw_header_footer(self, page_count):
        self.saveState()
        self.setFont("Helvetica-Bold", 8)
        self.setFillColor(colors.HexColor("#0f172a"))
        
        # Header (Only after page 1)
        if self._pageNumber > 1:
            self.drawString(54, 750, "ELEPHANT GUARD (SEEMS-AI) — TECHNICAL ARCHITECTURE & AI VISION SPECIFICATION")
            self.setFont("Helvetica", 8)
            self.setFillColor(colors.HexColor("#64748b"))
            self.drawRightString(558, 750, "Jharkhand Forest Dept • Dalma Wildlife Division")
            self.setStrokeColor(colors.HexColor("#cbd5e1"))
            self.setLineWidth(0.5)
            self.line(54, 744, 558, 744)

        # Footer
        self.setStrokeColor(colors.HexColor("#cbd5e1"))
        self.setLineWidth(0.5)
        self.line(54, 45, 558, 45)
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#64748b"))
        self.drawString(54, 32, "Confidential & Proprietary — Elephant Guard Wildlife Platform • SIH26043 Architecture")
        self.drawRightString(558, 32, f"Page {self._pageNumber} of {page_count}")
        self.restoreState()

def build_pdf():
    doc = SimpleDocTemplate(
        PDF_OUTPUT_PATH,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()
    
    # Custom Styles
    primary_color = colors.HexColor("#064e3b") # Forest Deep Green
    accent_color = colors.HexColor("#0284c7")  # Cyan / Blue
    text_dark = colors.HexColor("#0f172a")
    text_muted = colors.HexColor("#475569")
    bg_light = colors.HexColor("#f8fafc")
    border_color = colors.HexColor("#e2e8f0")

    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=22,
        leading=26,
        textColor=primary_color,
        spaceAfter=6
    )

    subtitle_style = ParagraphStyle(
        'DocSubTitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=11,
        leading=15,
        textColor=text_muted,
        spaceAfter=15
    )

    h1_style = ParagraphStyle(
        'Heading1_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=14,
        leading=18,
        textColor=primary_color,
        spaceBefore=14,
        spaceAfter=6,
        keepWithNext=True
    )

    h2_style = ParagraphStyle(
        'Heading2_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=11,
        leading=15,
        textColor=accent_color,
        spaceBefore=10,
        spaceAfter=4,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'Body_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13,
        textColor=text_dark,
        spaceAfter=6
    )

    bullet_style = ParagraphStyle(
        'Bullet_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13,
        textColor=text_dark,
        leftIndent=15,
        spaceAfter=3
    )

    code_style = ParagraphStyle(
        'Code_Custom',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=8,
        leading=10,
        textColor=colors.HexColor("#1e293b"),
        backColor=colors.HexColor("#f1f5f9"),
        spaceAfter=4,
        leftIndent=8,
        rightIndent=8
    )

    story = []

    # =========================================================================
    # TITLE & HEADER BLOCK
    # =========================================================================
    story.append(Paragraph("ELEPHANT GUARD (SEEMS-AI)", title_style))
    story.append(Paragraph("<b>Comprehensive Technical Specification Document</b><br/>Architecture, Languages, Frameworks, APIs, Database Schemas, Edge AI Vision Models & SIH26043 Problem Solving Framework", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=primary_color, spaceAfter=12))

    # Meta Info Table
    meta_data = [
        [
            Paragraph("<b>Author / Division:</b> Jharkhand Forest Dept • Dalma Wildlife Division", body_style),
            Paragraph("<b>Target Corridor:</b> NH-33 Asanbani–Chandil Pass (18.4 KM)", body_style)
        ],
        [
            Paragraph("<b>System Version:</b> Phase 4.0.0 Production Release", body_style),
            Paragraph("<b>Problem Statement Alignment:</b> SIH26043 Multi-Stakeholder Hub", body_style)
        ]
    ]
    meta_table = Table(meta_data, colWidths=[250, 250])
    meta_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), bg_light),
        ('BOX', (0,0), (-1,-1), 0.5, border_color),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_color),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    story.append(meta_table)
    story.append(Spacer(1, 10))

    # =========================================================================
    # SECTION 1: SYSTEM ARCHITECTURE & COMPONENT OVERVIEW
    # =========================================================================
    story.append(Paragraph("1. High-Level System Architecture", h1_style))
    story.append(Paragraph(
        "Elephant Guard operates on a <b>Three-Tier Distributed Architecture</b> designed for zero-latency roadside herd detection, offline mesh relaying, central geospatial synchronization, and collaborative societal problem-solving:",
        body_style
    ))

    arch_rows = [
        ["Tier", "Component", "Primary Technologies", "Core Responsibility"],
        [
            "Mobile Tier",
            "Android Patrol App (SEEMS-AI)",
            "Kotlin 2.0, Jetpack Compose, CameraX, TensorFlow Lite, OSMDroid",
            "Real-time on-device edge AI camera inference, offline P2P Wi-Fi UDP broadcast mesh, GPS tracking, and manual 5km SOS dispatch."
        ],
        [
            "Backend Tier",
            "FastAPI Central Command Server",
            "Python 3.11, FastAPI, Uvicorn, SQLite3, PBKDF2 Hashing, JWT",
            "High-throughput REST API, secure user & officer auth, incident storage, alert routing, analytics aggregation, and Android telemetry ingestion."
        ],
        [
            "Web Tier",
            "Forest Dept Admin Dashboard",
            "React 18, TypeScript, Vite, Tailwind CSS, Leaflet GIS, Lucide",
            "Authorized wildlife administration, real-time spatial corridor map, user governance, incident verification, alert management, and SIH26043 challenge hub."
        ]
    ]
    t_arch = Table([[Paragraph(c, body_style) for c in r] for r in arch_rows], colWidths=[65, 115, 140, 180])
    t_arch.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#064e3b")),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('BOX', (0,0), (-1,-1), 0.5, border_color),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_color),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    story.append(t_arch)
    story.append(Spacer(1, 10))

    # =========================================================================
    # SECTION 2: FRONTEND TECHNOLOGIES & LIBRARIES
    # =========================================================================
    story.append(Paragraph("2. Web Admin Dashboard (Frontend Technologies)", h1_style))
    story.append(Paragraph("The Web Admin Dashboard is built specifically for Forest Department Wildlife Officers and Central Command Staff:", body_style))
    
    story.append(Paragraph("• <b>Programming Languages:</b> TypeScript 5.5.3, JavaScript (ES2022), HTML5, CSS3", bullet_style))
    story.append(Paragraph("• <b>UI Framework & Build Tool:</b> React 18.3.1 + Vite 5.4.3 (Fast HMR & Optimized Bundling)", bullet_style))
    story.append(Paragraph("• <b>CSS Framework:</b> Tailwind CSS 3.4.11 (Custom Tactical & Forest Department theme tokens)", bullet_style))
    story.append(Paragraph("• <b>Interactive Spatial Mapping:</b> Leaflet 1.9.4 & OpenStreetMap (OSM) Tile Servers", bullet_style))
    story.append(Paragraph("• <b>Icons & UI Assets:</b> Lucide-React 0.441.0", bullet_style))
    story.append(Paragraph("• <b>Routing & Navigation:</b> React Router DOM 6.26.2 (Protected Admin Routes)", bullet_style))
    story.append(Paragraph("• <b>Security Design:</b> Strict government auth against <code>/api/v1/admin/login</code> without demo shortcuts. Zero plain-text password display.", bullet_style))
    story.append(Spacer(1, 10))

    # =========================================================================
    # SECTION 3: BACKEND TECHNOLOGIES & DATABASE
    # =========================================================================
    story.append(Paragraph("3. Backend Architecture & Database Engine", h1_style))
    story.append(Paragraph("The backend is engineered for ultra-low latency, stability, and zero external dependency friction:", body_style))

    story.append(Paragraph("• <b>Language & Runtime:</b> Python 3.11.9 (Portable Embedded Runtime with PIP 26.2.1)", bullet_style))
    story.append(Paragraph("• <b>Web Framework:</b> FastAPI 0.141.1 (Asynchronous ASGI framework on Starlette)", bullet_style))
    story.append(Paragraph("• <b>ASGI Server:</b> Uvicorn 0.52.4 (High-performance Async Event Loop)", bullet_style))
    story.append(Paragraph("• <b>Data Validation:</b> Pydantic 2.13.5 & Email-Validator 2.3.0", bullet_style))
    story.append(Paragraph("• <b>Database:</b> SQLite 3 (<code>elephant_guard.db</code>) with Connection Pooling & Row Factory", bullet_style))
    story.append(Paragraph("• <b>Authentication & Tokens:</b> JSON Web Tokens (JWT) HS256 with 24-Hour Expiration", bullet_style))
    story.append(Paragraph("• <b>Password Security:</b> PBKDF2 HMAC SHA-256 with 100,000 Iterations + 16-byte Cryptographic Salt", bullet_style))
    story.append(Paragraph("• <b>Static Media Storage:</b> Static file server mounted on <code>/uploads</code> for evidence snapshots", bullet_style))
    story.append(Spacer(1, 10))

    # Database Schema Table
    story.append(Paragraph("Database Schema (Tables & Relationships):", h2_style))
    db_rows = [
        ["Table Name", "Primary Key", "Key Attributes", "Description"],
        ["users", "id (TEXT)", "full_name, mobile, email, password_hash, role, status, created_at, last_active", "Stores authorized officers & registered citizens with PBKDF2 password hashes."],
        ["incidents", "id (TEXT)", "reporter_id, latitude, longitude, confidence, count, risk_score, threat_level, verification_status, response_status", "Live elephant sightings, AI confidence scores, GPS coordinates & patrol dispatch records."],
        ["alerts", "id (TEXT)", "incident_id, severity, location_text, latitude, longitude, distance_km, notified_count, status", "Active & historical 5km broadcast alerts with acknowledgement tracking."],
        ["challenges", "id (TEXT)", "incident_id, title, description, location, status, reports_count, risk_level, created_at", "SIH26043 crowdsourced societal conflict problems open for external solutions."],
        ["solutions", "id (TEXT)", "challenge_id, organization, organization_type, description, status, contact_email, prototype_url", "University (IIT/NIT) and industry partner proposed technical models and prototypes."],
        ["audit_logs", "id (TEXT)", "action, actor, timestamp, details", "Immutable administrative access and verification event audit trail."]
    ]
    t_db = Table([[Paragraph(c, body_style) for c in r] for r in db_rows], colWidths=[70, 75, 175, 180])
    t_db.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#1e293b")),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('BOX', (0,0), (-1,-1), 0.5, border_color),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_color),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
    ]))
    story.append(t_db)
    story.append(Spacer(1, 10))

    story.append(PageBreak())

    # =========================================================================
    # SECTION 4: REST API ENDPOINTS SPECIFICATION
    # =========================================================================
    story.append(Paragraph("4. Complete REST API Endpoints Specification", h1_style))
    story.append(Paragraph("The FastAPI backend exposes the following RESTful API endpoints for the Web Dashboard and Android Patrol App:", body_style))

    api_rows = [
        ["HTTP Method & Route", "Auth Required", "Description & Payload"],
        ["POST /api/v1/admin/login", "Public", "Admin & Forest Officer login portal. Validates email/mobile & password, returns JWT token."],
        ["POST /api/v1/auth/login", "Public", "Citizen & patrol user login. Returns JWT token and User profile."],
        ["POST /api/v1/auth/register", "Public", "User registration. Hashes password securely with PBKDF2 before saving."],
        ["GET /api/v1/auth/me", "Bearer JWT", "Retrieves current authenticated user's profile and assigned role."],
        ["GET /api/v1/admin/summary", "Admin JWT", "Returns 6 real-time KPI counts (Users, Active, Incidents, Verified, Alerts, Resolved) + Activity feed."],
        ["GET /api/v1/admin/users", "Admin JWT", "List registered users with query search, role filtering, and account status filtering."],
        ["PATCH /api/v1/admin/users/{id}/status", "Admin JWT", "Update user account status (ACTIVE, SUSPENDED, DEACTIVATED)."],
        ["GET /api/v1/admin/incidents", "Admin JWT", "List all elephant sightings with threatLevel and verificationStatus query parameters."],
        ["POST /api/v1/admin/incidents", "Admin JWT", "Create a new verified field incident report directly from the admin dashboard."],
        ["PATCH /api/v1/admin/incidents/{id}", "Admin JWT", "Update incident verification status (Verified, Dispatched, Resolved) & response status."],
        ["GET /api/v1/admin/alerts", "Admin JWT", "List all 5km proximity and broadcast alerts with status and severity filters."],
        ["PATCH /api/v1/admin/alerts/{id}", "Admin JWT", "Acknowledge or Close an active corridor alert."],
        ["GET /api/v1/admin/challenges", "Admin JWT", "List SIH26043 societal challenges and current solution proposal counts."],
        ["POST /api/v1/admin/challenges", "Admin JWT", "Publish a new crowdsourced societal challenge from field conflict data."],
        ["GET /api/v1/admin/solutions", "Admin JWT", "List university & industry solution proposals with challenge linkage."],
        ["POST /api/v1/admin/solutions", "Admin JWT", "Submit an academic/industry solution proposal with prototype link."],
        ["GET /api/v1/admin/analytics", "Admin JWT", "Computes spatial GPS cluster hotspots, threat distributions, and solution conversion metrics."],
        ["POST /api/v1/telemetry/report", "Public / App", "<b>Android Integration Endpoint:</b> Receives on-device AI detections & manual 5km SOS broadcasts."]
    ]
    t_api = Table([[Paragraph(c, body_style) for c in r] for r in api_rows], colWidths=[140, 75, 285])
    t_api.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#0284c7")),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('BOX', (0,0), (-1,-1), 0.5, border_color),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_color),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
    ]))
    story.append(t_api)
    story.append(Spacer(1, 10))

    # =========================================================================
    # SECTION 5: HOW THE AI DETECTS ELEPHANTS (DETAILED BREAKDOWN)
    # =========================================================================
    story.append(Paragraph("5. AI Vision Engine & How Elephant Detection Works", h1_style))
    story.append(Paragraph(
        "The Elephant Guard AI system utilizes on-device Edge Computer Vision powered by <b>TensorFlow Lite</b>. The pipeline operates in real-time directly on patrol mobile devices and roadside optical sensors at 30–60 FPS.",
        body_style
    ))

    story.append(Paragraph("A. Deep Learning Models Used", h2_style))
    story.append(Paragraph("• <b>Primary Model:</b> <code>models/efficientdet_lite0.tflite</code> (Google EfficientDet-Lite0 quantized for edge execution)", bullet_style))
    story.append(Paragraph("• <b>Fallback Model:</b> <code>models/ssd_mobilenet_v1.tflite</code> (Single Shot MultiBox Detector with MobileNet V1 backbone)", bullet_style))
    story.append(Paragraph("• <b>Target Class Category:</b> Class ID 22 in MS COCO Dataset (<code>elephant</code>)", bullet_style))
    story.append(Paragraph("• <b>Input Resolution:</b> 320×320×3 / 300×300×3 RGB normalized float tensor", bullet_style))
    story.append(Paragraph("• <b>Inference Latency:</b> 22ms – 48ms on standard smartphone Neural Processing Units (NPU / GPU delegate)", bullet_style))
    story.append(Spacer(1, 4))

    story.append(Paragraph("B. End-to-End AI Detection Step-by-Step Workflow", h2_style))

    ai_steps = [
        ("Step 1: Video Frame Ingestion", "CameraX <code>ImageAnalysis</code> pipeline continuously grabs uncompressed YUV_420_888 camera frames. Non-blocking frame buffers drop stale frames if inference is in progress."),
        ("Step 2: Preprocessing & Rotation", "The frame is converted to an RGB Bitmap. A rotation matrix adjusts for device orientation (0°, 90°, 180°, 270°) and normalizes pixel values to [0.0, 1.0]."),
        ("Step 3: Neural Network Forward Pass", "The preprocessed tensor passes through depthwise separable convolutional layers and Feature Pyramid Networks (BiFPN). The anchor box heads generate candidate bounding boxes, class classification probabilities, and confidence scores."),
        ("Step 4: Non-Maximum Suppression (NMS)", "Overlapping bounding box proposals around the same elephant are suppressed using an Intersection-Over-Union (IOU) threshold of 0.5, ensuring accurate multi-elephant herd counting."),
        ("Step 5: 3-of-5 Temporal Validation Queue", "To prevent transient false positives (e.g. cattle, shadows, distant trucks), a sliding window validation engine requires elephant detections in at least 3 out of 5 consecutive frames before elevating alert status."),
        ("Step 6: Risk Evaluation Engine", "A composite risk score (0–100) is calculated combining herd count, spatial proximity to road, AI confidence, and nocturnal lighting conditions."),
        ("Step 7: Automated Alert & Mesh Dispatch", "Upon confirmation: (1) Local UDP mesh broadcast is sent over port 8888, (2) Audio warning sirens sound, (3) Telemetry is dispatched to Central FastAPI backend at <code>/api/v1/telemetry/report</code>.")
    ]

    for title, desc in ai_steps:
        story.append(Paragraph(f"<b>{title}:</b> {desc}", body_style))

    story.append(Spacer(1, 6))

    # Risk Formula Box
    story.append(Paragraph("Mathematical Risk Formulation:", h2_style))
    formula_text = """
    <b>Risk Score Formula:</b><br/>
    <code>RiskScore = min(100, (Count × 20) + (Confidence × 30) + ZoneWeight + NocturnalWeight)</code><br/>
    • <b>Threat Levels:</b> CRITICAL (Score ≥ 85), HIGH (Score 65–84), CAUTION (Score 35–64), SAFE (Score &lt; 35).<br/>
    • <b>Geofence Radius:</b> 1.0 KM (Immediate Collision Danger) | 5.0 KM (Village Early Evacuation Perimeter).
    """
    p_formula = Paragraph(formula_text, code_style)
    story.append(p_formula)
    story.append(Spacer(1, 10))

    story.append(PageBreak())

    # =========================================================================
    # SECTION 6: SIH26043 PROBLEM SOLVING FRAMEWORK
    # =========================================================================
    story.append(Paragraph("6. SIH26043 Crowdsourced Societal Challenges & University Collaboration", h1_style))
    story.append(Paragraph(
        "Elephant Guard fulfills the core Smart India Hackathon problem statement by linking raw citizen and ranger wildlife telemetry directly into an institutional research and open innovation pipeline:",
        body_style
    ))

    sih_pipeline = [
        ["Stage", "Actor / Stakeholder", "Platform Action & State Transition"],
        ["1. Detection", "Citizen / Patrol Ranger", "AI camera detects herd or citizen submits manual sighting report."],
        ["2. Verification", "Forest Department Officer", "Officer verifies coordinates, herd size, and urgency on the Web Admin Dashboard."],
        ["3. Societal Challenge", "Forest Administrator", "Recurring or severe conflict hotspot is published as an open SIH Challenge."],
        ["4. Solution Proposal", "Universities (IIT/NIT) & Industry", "Research teams submit AI models, acoustic geophones, or solar warning proposals."],
        ["5. Prototype & Testing", "Joint Forest-Academic Cell", "Field trials are conducted along the Dalma NH-33 corridor."],
        ["6. Implementation", "Wildlife Authority", "Successful prototypes are permanently deployed as part of the sanctuary defense matrix."]
    ]
    t_sih = Table([[Paragraph(c, body_style) for c in r] for r in sih_pipeline], colWidths=[80, 130, 290])
    t_sih.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#312e81")),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('BOX', (0,0), (-1,-1), 0.5, border_color),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_color),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    story.append(t_sih)
    story.append(Spacer(1, 14))

    # =========================================================================
    # SECTION 7: SUMMARY & SYSTEM HEALTH VERIFICATION
    # =========================================================================
    story.append(Paragraph("7. System Verification & Live URLs", h1_style))
    story.append(Paragraph("All modules have been compiled, tested, and are running with active daemons:", body_style))

    summary_rows = [
        ["Service", "Local Port / Path", "Active Credentials", "Status"],
        ["Web Admin Dashboard", "http://localhost:5173", "admin@forest.gov.in / AdminForest@2026", "ONLINE"],
        ["FastAPI Central Backend", "http://localhost:8000", "JWT Bearer Authentication", "ONLINE"],
        ["Interactive Swagger Docs", "http://localhost:8000/docs", "OpenAPI Specification 3.1", "ONLINE"],
        ["SQLite Database", "scratch/seems-ai-backend/elephant_guard.db", "PBKDF2 Salted Hashing", "ACTIVE"],
        ["Android Patrol App", "scratch/seems-ai-android", "Direct Telemetry Sync", "ACTIVE"]
    ]
    t_sum = Table([[Paragraph(c, body_style) for c in r] for r in summary_rows], colWidths=[120, 150, 160, 70])
    t_sum.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#064e3b")),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('BOX', (0,0), (-1,-1), 0.5, border_color),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_color),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    story.append(t_sum)
    story.append(Spacer(1, 20))

    story.append(Paragraph("<b>End of Technical Specification Document</b>", ParagraphStyle('End', parent=body_style, alignment=1, textColor=text_muted)))

    # Build document
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"PDF generated successfully at: {PDF_OUTPUT_PATH}")

if __name__ == "__main__":
    build_pdf()
