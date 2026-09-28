from typing import List
from pydantic import BaseModel


class SystemStats(BaseModel):
    total_users: int
    total_files: int
    system_status: str
    active_threads: int
    memory_usage_mb: float


class NotificationItem(BaseModel):
    id: int
    title: str
    message: str
    timestamp: str
    type: str  # info, warning, success
