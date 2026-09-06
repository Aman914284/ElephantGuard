from typing import Optional, List
from pydantic import BaseModel, EmailStr, Field

class UserRegisterRequest(BaseModel):
    fullName: str = Field(..., min_length=2)
    mobile: str = Field(..., min_length=10)
    email: EmailStr
    password: str = Field(..., min_length=6)
    role: Optional[str] = "Citizen"

class AdminUserCreateRequest(BaseModel):
    fullName: str = Field(..., min_length=2)
    mobile: str = Field(..., min_length=10)
    email: EmailStr
    password: str = Field(..., min_length=6)
    role: Optional[str] = "Forest Officer"
    status: Optional[str] = "ACTIVE"

class UserLoginRequest(BaseModel):
    identifier: str # Email or Mobile
    password: str

class UserResponse(BaseModel):
    id: str
    fullName: str
    mobile: str
    email: str
    role: str
    status: str
    createdAt: str
    lastActive: str

class LoginResponse(BaseModel):
    success: bool
    token: str
    user: UserResponse

class UserStatusUpdateRequest(BaseModel):
    status: str # ACTIVE, SUSPENDED, DEACTIVATED

class IncidentCreateRequest(BaseModel):
    reporterName: Optional[str] = "Forest Patrol Officer"
    latitude: float
    longitude: float
    confidence: Optional[float] = 1.0
    elephantCount: Optional[int] = 1
    riskScore: Optional[int] = 50
    threatLevel: Optional[str] = "CAUTION" # SAFE, CAUTION, HIGH, CRITICAL
    verificationStatus: Optional[str] = "New" # New, Under Review, Verified, Dispatched, Resolved, Rejected
    responseStatus: Optional[str] = "Standby" # Standby, Dispatched, Resolved
    photoUrl: Optional[str] = None
    notes: Optional[str] = ""
    isSos: Optional[bool] = False
    sourceDevice: Optional[str] = "Web Portal"

class IncidentUpdateRequest(BaseModel):
    verificationStatus: Optional[str] = None # New, Under Review, Verified, Dispatched, Resolved, Rejected
    responseStatus: Optional[str] = None # Standby, Dispatched, Resolved
    notes: Optional[str] = None
    threatLevel: Optional[str] = None

class AlertUpdateRequest(BaseModel):
    status: Optional[str] = None # Active, Acknowledged, Closed
    notes: Optional[str] = None

class ChallengeCreateRequest(BaseModel):
    incidentId: Optional[str] = None
    title: str
    description: str
    location: str
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    riskLevel: Optional[str] = "HIGH"
    status: Optional[str] = "Reported"
    evidenceUrl: Optional[str] = None

class ChallengeUpdateRequest(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    status: Optional[str] = None # Reported, Verified, Open for Solutions, In Development, Field Testing, Implemented, Resolved
    riskLevel: Optional[str] = None

class SolutionCreateRequest(BaseModel):
    challengeId: str
    organization: str
    organizationType: str # University, Industry Partner, Research Lab, NGO
    description: str
    contactEmail: EmailStr
    contactPhone: Optional[str] = None
    prototypeUrl: Optional[str] = None
    status: Optional[str] = "Submitted"

class SolutionUpdateRequest(BaseModel):
    status: Optional[str] = None # Submitted, In Review, Prototype, Field Testing, Implemented, Rejected
    description: Optional[str] = None

class TelemetryPayload(BaseModel):
    """Matches the Android AlertNetworkManager.kt WildlifeAlertPayload exactly."""
    alertId: Optional[str] = None
    target: Optional[str] = "ELEPHANT"
    count: Optional[int] = 1
    confidence: Optional[float] = 1.0
    riskScore: Optional[int] = 50
    threatLevel: Optional[str] = "CRITICAL"
    latitude: float
    longitude: float
    isGpsActive: Optional[bool] = True
    timestamp: Optional[str] = None
    sourceDeviceId: Optional[str] = "ANDROID-UNIT"
    sourceDeviceName: Optional[str] = "Patrol Device"
    notes: Optional[str] = ""
    isSos: Optional[bool] = False
