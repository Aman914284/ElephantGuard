import sqlite3
import os
import hashlib
import hmac
import time
from datetime import datetime

DB_PATH = os.path.join(os.path.dirname(__file__), "elephant_guard.db")

def get_db_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def hash_password(password: str, salt: str = None) -> str:
    """Secure password hashing using PBKDF2 HMAC SHA-256 with cryptographic salt."""
    if not salt:
        salt = os.urandom(16).hex()
    key = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt.encode('utf-8'), 100000)
    return f"{salt}:{key.hex()}"

def verify_password(stored_password_hash: str, provided_password: str) -> bool:
    """Verifies a password against the stored PBKDF2 hash without exposing plain text."""
    try:
        if ":" not in stored_password_hash:
            return False
        salt, key_hex = stored_password_hash.split(":", 1)
        new_key = hashlib.pbkdf2_hmac('sha256', provided_password.encode('utf-8'), salt.encode('utf-8'), 100000)
        return hmac.compare_digest(new_key.hex(), key_hex)
    except Exception:
        return False

def init_db():
    """Initializes all required tables with proper relations and indices."""
    conn = get_db_connection()
    cursor = conn.cursor()

    # 1. USERS table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS users (
        id TEXT PRIMARY KEY,
        full_name TEXT NOT NULL,
        mobile TEXT UNIQUE NOT NULL,
        email TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL,
        role TEXT NOT NULL DEFAULT 'Citizen',
        status TEXT NOT NULL DEFAULT 'ACTIVE',
        created_at TEXT NOT NULL,
        last_active TEXT NOT NULL
    )
    """)

    # 2. INCIDENTS table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS incidents (
        id TEXT PRIMARY KEY,
        reporter_id TEXT,
        reporter_name TEXT NOT NULL,
        latitude REAL NOT NULL,
        longitude REAL NOT NULL,
        timestamp TEXT NOT NULL,
        confidence REAL NOT NULL DEFAULT 1.0,
        elephant_count INTEGER NOT NULL DEFAULT 1,
        risk_score INTEGER NOT NULL DEFAULT 50,
        threat_level TEXT NOT NULL DEFAULT 'CAUTION',
        verification_status TEXT NOT NULL DEFAULT 'New',
        response_status TEXT NOT NULL DEFAULT 'Standby',
        photo_url TEXT,
        notes TEXT,
        is_sos INTEGER NOT NULL DEFAULT 0,
        source_device TEXT
    )
    """)

    # 3. ALERTS table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS alerts (
        id TEXT PRIMARY KEY,
        incident_id TEXT,
        severity TEXT NOT NULL,
        location_text TEXT NOT NULL,
        latitude REAL NOT NULL,
        longitude REAL NOT NULL,
        distance_km REAL NOT NULL DEFAULT 0.0,
        notified_count INTEGER NOT NULL DEFAULT 0,
        created_at TEXT NOT NULL,
        acknowledged_at TEXT,
        status TEXT NOT NULL DEFAULT 'Active',
        notes TEXT,
        FOREIGN KEY (incident_id) REFERENCES incidents (id)
    )
    """)

    # 4. CHALLENGES table (SIH26043 Crowdsourced Societal Challenges)
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS challenges (
        id TEXT PRIMARY KEY,
        incident_id TEXT,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        location TEXT NOT NULL,
        latitude REAL,
        longitude REAL,
        status TEXT NOT NULL DEFAULT 'Reported',
        reports_count INTEGER NOT NULL DEFAULT 1,
        risk_level TEXT NOT NULL DEFAULT 'HIGH',
        evidence_url TEXT,
        created_at TEXT NOT NULL,
        FOREIGN KEY (incident_id) REFERENCES incidents (id)
    )
    """)

    # 5. SOLUTIONS table (University & Industry Collaboration)
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS solutions (
        id TEXT PRIMARY KEY,
        challenge_id TEXT NOT NULL,
        organization TEXT NOT NULL,
        organization_type TEXT NOT NULL,
        description TEXT NOT NULL,
        status TEXT NOT NULL DEFAULT 'Submitted',
        contact_email TEXT NOT NULL,
        contact_phone TEXT,
        prototype_url TEXT,
        created_at TEXT NOT NULL,
        FOREIGN KEY (challenge_id) REFERENCES challenges (id)
    )
    """)

    # 6. AUDIT LOGS table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS audit_logs (
        id TEXT PRIMARY KEY,
        action TEXT NOT NULL,
        actor TEXT NOT NULL,
        timestamp TEXT NOT NULL,
        details TEXT
    )
    """)

    # Seed Default Administrator & Officer Accounts if not present
    now_str = datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")

    # Authentic Dalma Wildlife Division Roster
    default_personnel = [
        ("USR-ADMIN-001", "Shri Rajeev Ranjan, IFS", "9876500001", "admin@forest.gov.in", "AdminForest@2026", "Administrator"),
        ("USR-OFFICER-001", "Anand Soren (Dalma Range Officer)", "9876543210", "officer@elephantguard.org", "guard123", "Forest Officer"),
        ("USR-QRT-002", "Sunil Kumar Mahto (QRT Alpha Lead)", "9431122334", "qrt.dalma@forest.gov.in", "guard123", "Forest Officer"),
        ("USR-GUARD-003", "Birsa Munda (Asanbani Beat Guard)", "9835123456", "birsa.guard@dalma.org", "guard123", "Community Guard"),
        ("USR-GUARD-004", "Ramesh Murmu (Mirzadih VSS Head)", "9771234567", "ramesh.murmu@village.in", "guard123", "Community Guard"),
        ("USR-RES-005", "Dr. Priya Banerjee (Wildlife Biologist)", "9123456780", "priya.banerjee@wildlife.res.in", "guard123", "Forest Officer"),
        ("USR-OFFICER-006", "Subroto Ghosh (Mango Checkpost Officer)", "9470123456", "mango.checkpost@forest.gov.in", "guard123", "Forest Officer"),
        ("USR-CITIZEN-007", "Karan Mahato (Pardih Farmer Rep)", "9934112233", "karan.pardih@dalma.org", "guard123", "Citizen"),
    ]

    for uid, name, mob, eml, pwd, rle in default_personnel:
        cursor.execute("SELECT id FROM users WHERE email = ?", (eml,))
        if not cursor.fetchone():
            p_hash = hash_password(pwd)
            cursor.execute("""
                INSERT INTO users (id, full_name, mobile, email, password_hash, role, status, created_at, last_active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (uid, name, mob, eml, p_hash, rle, "ACTIVE", now_str, now_str))

    # Seed initial Dalma corridor real coordinates incidents if table is empty
    cursor.execute("SELECT COUNT(*) as count FROM incidents")
    inc_count = cursor.fetchone()["count"]
    if inc_count == 0:
        seed_incidents = [
            (
                "INC-DALMA-101",
                "USR-QRT-002",
                "Sunil Mahto (QRT Alpha Lead)",
                22.8942,
                86.2081,
                now_str,
                0.96,
                4,
                88,
                "CRITICAL",
                "Verified",
                "Dispatched",
                None,
                "Herd of 4 crossing NH-33 corridor near Asanbani ghat. Speed mitigation sirens engaged.",
                1,
                "UNIT-Pixel7-A1"
            ),
            (
                "INC-DALMA-102",
                "USR-GUARD-004",
                "Ramesh Murmu (Mirzadih VSS)",
                22.8810,
                86.2190,
                now_str,
                0.92,
                2,
                75,
                "HIGH",
                "Under Review",
                "Standby",
                None,
                "2 adult tuskers spotted near agricultural boundary of Mirzadih village.",
                1,
                "UNIT-Redmi-C4"
            ),
            (
                "INC-DALMA-103",
                "USR-OFFICER-001",
                "Anand Soren (Dalma Range)",
                22.9120,
                86.1950,
                now_str,
                0.88,
                1,
                45,
                "CAUTION",
                "Resolved",
                "Resolved",
                None,
                "Single bull elephant tracked moving away from highway buffer back toward sanctuary core.",
                0,
                "UNIT-Galaxy-B2"
            ),
            (
                "INC-DALMA-104",
                "USR-GUARD-003",
                "Birsa Munda (Asanbani)",
                22.9305,
                86.1820,
                now_str,
                0.94,
                3,
                91,
                "CRITICAL",
                "Verified",
                "Dispatched",
                None,
                "Family herd of 3 spotted moving towards Chandil railway line cut. Rapid patrol alerted.",
                1,
                "UNIT-Poco-D9"
            ),
            (
                "INC-DALMA-105",
                "USR-RES-005",
                "Dr. Priya Banerjee",
                22.8650,
                86.2310,
                now_str,
                0.98,
                5,
                60,
                "CAUTION",
                "Verified",
                "Standby",
                None,
                "Breeding herd foraging along Subarnarekha river buffer basin.",
                0,
                "UNIT-ThermalCam-03"
            )
        ]
        for inc in seed_incidents:
            cursor.execute("""
                INSERT INTO incidents (id, reporter_id, reporter_name, latitude, longitude, timestamp, confidence, elephant_count, risk_score, threat_level, verification_status, response_status, photo_url, notes, is_sos, source_device)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, inc)

        # Seed corresponding Alerts
        cursor.execute("""
            INSERT INTO alerts (id, incident_id, severity, location_text, latitude, longitude, distance_km, notified_count, created_at, acknowledged_at, status, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, ("ALT-901", "INC-DALMA-101", "CRITICAL", "NH-33 Asanbani Corridor (KM 234)", 22.8942, 86.2081, 0.8, 142, now_str, now_str, "Active", "Immediate highway caution activated."))

        cursor.execute("""
            INSERT INTO alerts (id, incident_id, severity, location_text, latitude, longitude, distance_km, notified_count, created_at, acknowledged_at, status, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, ("ALT-902", "INC-DALMA-102", "HIGH", "Mirzadih Village Buffer Zone", 22.8810, 86.2190, 1.4, 85, now_str, None, "Active", "P2P and SMS broadcast to fringe farmers."))

        # Seed SIH26043 Challenges
        cursor.execute("""
            INSERT INTO challenges (id, incident_id, title, description, location, latitude, longitude, status, reports_count, risk_level, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            "CHAL-SIH-001",
            "INC-DALMA-101",
            "Frequent Elephant Crossing & Vehicle Collision at NH-33 Asanbani Pass",
            "Continuous migration path intersects 4-lane highway with high night truck traffic causing fatal collisions.",
            "Dalma Corridor NH-33 (KM 232-236)",
            22.8942,
            86.2081,
            "Open for Solutions",
            14,
            "CRITICAL",
            now_str
        ))

        cursor.execute("""
            INSERT INTO challenges (id, incident_id, title, description, location, latitude, longitude, status, reports_count, risk_level, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            "CHAL-SIH-002",
            "INC-DALMA-102",
            "Paddy Crop Depredation & Fence Breaches around Mirzadih Fringe",
            "Elephant herds navigating dry stream beds at night breach conventional solar fencing during harvest season.",
            "Mirzadih Fringe Forest Boundary",
            22.8810,
            86.2190,
            "In Development",
            8,
            "HIGH",
            now_str
        ))

        # Seed University & Industry Solutions
        cursor.execute("""
            INSERT INTO solutions (id, challenge_id, organization, organization_type, description, status, contact_email, contact_phone, prototype_url, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            "SOL-IIT-01",
            "CHAL-SIH-001",
            "IIT Kharagpur Dept of Computer Science & AI",
            "University",
            "Thermal Edge Vision + Dynamic Solar Highway Variable Message Display (VMD) warning drivers 1.5km ahead.",
            "Field Testing",
            "wildlife.ai@iitkgp.ac.in",
            "+91 3222 282221",
            "https://iitkgp.ac.in/research/wildlife-vmd",
            now_str
        ))

        cursor.execute("""
            INSERT INTO solutions (id, challenge_id, organization, organization_type, description, status, contact_email, contact_phone, prototype_url, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            "SOL-NIT-02",
            "CHAL-SIH-001",
            "NIT Jamshedpur Geo-Informatics Research Cell",
            "University",
            "Sub-surface Geophone Acoustic & Seismic Infrasound sensor array for early herd detection in dense fog.",
            "Prototype",
            "geoinfo@nitjsr.ac.in",
            "+91 657 228 6610",
            "https://nitjsr.ac.in/seismic-elephant-grid",
            now_str
        ))

        cursor.execute("""
            INSERT INTO solutions (id, challenge_id, organization, organization_type, description, status, contact_email, contact_phone, prototype_url, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            "SOL-IND-01",
            "CHAL-SIH-002",
            "BioDefense Sensors Pvt Ltd",
            "Industry Partner",
            "Smart AI Non-Harmful Acoustic Bio-Acoustic Repellers (Beehive & Tiger audio harmonics with solar battery).",
            "In Development",
            "partners@biodefense-tech.com",
            "+91 9988776655",
            "https://biodefense-tech.com/solutions/elephant-repel",
            now_str
        ))

    conn.commit()
    conn.close()

if __name__ == "__main__":
    init_db()
    print("Database initialized successfully at:", DB_PATH)
