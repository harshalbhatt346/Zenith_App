from typing import Optional, Dict
from pydantic import BaseModel, Field


class EventOut(BaseModel):
    id: int
    title: str
    category: str
    description: Optional[str] = ""
    event_date: str
    event_time: str
    venue: str
    capacity: int
    fee: int = 0
    icon: Optional[str] = ""
    available_seats: int


# EventDetail uses the same shape as EventOut as specified
class EventDetail(EventOut):
    pass


class RegistrationIn(BaseModel):
    student_name: str = Field(..., description="Full name of the student")
    roll_number: str = Field(..., description="College roll number / student ID")
    email: str = Field(..., description="Valid student email address")
    phone: Optional[str] = Field(None, description="Optional phone number")


class RegistrationOut(BaseModel):
    id: int
    event_id: int
    student_name: str
    registered_at: str


class RegistrationListItem(BaseModel):
    id: int
    event_id: int
    student_name: str
    roll_number: str
    email: str
    phone: Optional[str] = None
    registered_at: str
    event_title: str


class StatsOut(BaseModel):
    total_events: int
    total_registrations: int
    total_capacity: int
    overall_utilization_pct: float
    upcoming_events_count: int
    events_by_category: Dict[str, int]


# Stretch goal model: Admin adding a new event
class EventIn(BaseModel):
    title: str = Field(..., description="Event name")
    category: str = Field(..., description="Technical, Cultural, Sports, Gaming, or Workshop")
    description: Optional[str] = Field("", description="Detailed event description")
    event_date: str = Field(..., description="ISO event date, e.g. 2026-11-14")
    event_time: str = Field(..., description="Display time, e.g. 10:00 AM")
    venue: str = Field(..., description="Campus venue location")
    capacity: int = Field(..., gt=0, description="Maximum seats capacity")
    fee: int = Field(0, ge=0, description="Registration fee in INR")
    icon: Optional[str] = Field("🎉", description="Emoji icon representation")
