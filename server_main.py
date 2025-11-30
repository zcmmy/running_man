import uvicorn
from fastapi import FastAPI, Body, Query, Path, HTTPException, Depends
from pydantic import BaseModel, Field
from typing import List, Optional, Any, TypeVar, Generic
from enum import Enum
import datetime
import uuid

import databases

from passlib.context import CryptContext
from jose import JWTError, jwt
from datetime import datetime, timedelta
from fastapi.security import OAuth2PasswordBearer
from fastapi.staticfiles import StaticFiles
from fastapi.responses import HTMLResponse

from zoneinfo import ZoneInfo
from pydantic import BaseModel, validator

DATABASE_URL = "mysql+asyncmy://running_man_app:760924@localhost/running_man"

SECRET_KEY = "371ad45fc8505442159f9c2081946079b00ede1026266f2e1e899373ceaef7c4"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24 * 7 # Token 有效期: 7 天

SERVER_TZ = ZoneInfo("Asia/Shanghai")

# 数据库实例
database = databases.Database(DATABASE_URL)

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="auth/login")

# Pydantic 数据模型
class TaskType(str, Enum):
    FOOD_DELIVERY = "FOOD_DELIVERY"
    EXPRESS_DELIVERY = "EXPRESS_DELIVERY"
    OTHER = "OTHER"

class MessageType(str, Enum):
    CHAT = "CHAT"
    SYSTEM = "SYSTEM"

class OrderStatus(str, Enum):
    PENDING = "PENDING"      # 待接单
    IN_PROGRESS = "IN_PROGRESS"  # 进行中
    COMPLETED = "COMPLETED"    # 已完成
    CANCELLED = "CANCELLED"    # 已取消

class UserCreate(BaseModel):
    """用于 /auth/register 的请求体"""
    studentId: str
    password: str
    name: str
    phone: Optional[str] = None
    email: Optional[str] = None

    class Config:
        orm_mode = True # 允许从数据库对象映射

class UserProfile(BaseModel):
    """用户公开信息 (对应 'users' 表)"""
    id: str
    studentId: str
    name: str
    avatar: Optional[str] = None
    phone: Optional[str] = None
    email: Optional[str] = None
    creditScore: Optional[float] = None
    totalOrders: Optional[int] = None
    balance: Optional[float] = None
    createdAt: datetime

    @validator("createdAt")
    def attach_timezone_to_created_at(cls, v: datetime):
        """
        验证 createdAt 字段:
        如果从数据库读出的是一个“朴素” (naive) datetime (没有时区),
        则自动附加上服务器的本地时区 (SERVER_TZ)。
        """
        if v.tzinfo is None:
            # v 是朴素的, 附加时区
            return v.replace(tzinfo=SERVER_TZ)
            # (pytz 语法): return SERVER_TZ.localize(v)
        
        # v 已经包含时区, 直接返回
        return v

    class Config:
        orm_mode = True

class LoginRequest(BaseModel):
    studentId: str
    password: str

class LoginResponse(BaseModel):
    token: str
    user: UserProfile

class TokenData(BaseModel):
    """JWT Token 内部存储的数据模型"""
    sub: str = None  # "sub" (Subject) 是 JWT 的标准字段, 我们用它存储 user_id

# 通用 API 响应
T = TypeVar('T')
class ApiResponse(BaseModel, Generic[T]):
    code: int
    message: str
    data: Optional[T] = None

# 任务/订单模型，对应orders表
class OrderBase(BaseModel):
    # 同时对 'Task' 和 'Order' 模型使用
    id: int
    title: str
    description: Optional[str] = None
    price: float
    type: TaskType
    status: OrderStatus
    location: str
    destination: str
    estimatedTime: Optional[int] = None
    contactPhone: Optional[str] = None
    specialRequirements: Optional[str] = None
    publisherId: str
    publisherName: str
    runnerId: Optional[str] = None
    runnerName: Optional[str] = None
    createdAt: datetime
    updatedAt: datetime
    
    @validator("createdAt")
    def attach_timezone_to_created_at(cls, v: datetime):
        if v.tzinfo is None:
            return v.replace(tzinfo=SERVER_TZ)
        return v
    
    @validator("updatedAt")
    def attach_timezone_to_updated_at(cls, v: datetime):
        if v.tzinfo is None:
            return v.replace(tzinfo=SERVER_TZ)
        return v
    
    class Config:
        orm_mode = True


class TaskRequest(BaseModel):
    # POST /tasks 的请求体
    id: Optional[int] = None
    title: str
    description: Optional[str] = None
    price: float
    type: TaskType
    status: Optional[OrderStatus] = None
    location: str
    destination: str
    estimatedTime: Optional[int] = None
    contactPhone: Optional[str] = None
    specialRequirements: Optional[str] = None
    publisherId: Optional[str] = None
    publisherName: Optional[str] = None
    runnerId: Optional[str] = None
    runnerName: Optional[str] = None
    createdAt: Optional[datetime] = None
    updatedAt: Optional[datetime] = None

    @validator("createdAt")
    def attach_timezone_to_created_at(cls, v: datetime):
        if v.tzinfo is None:
            return v.replace(tzinfo=SERVER_TZ)
        return v

    @validator("updatedAt")
    def attach_timezone_to_updated_at(cls, v: datetime):
        if v.tzinfo is None:
            return v.replace(tzinfo=SERVER_TZ)
        return v

    class Config:
        orm_mode = True

class OrderListResponse(BaseModel):
    orders: List[TaskRequest]
    totalCount: int
    page: int
    pageSize: int

class OrderStats(BaseModel):
    totalPublished: int
    totalAccepted: int
    totalCompleted: int
    totalIncome: float

    class Config:
        orm_mode = True

# 聊天/消息模型
class ChatMessage(BaseModel):
    # 对应chat_messages表
    id: int
    orderId: int
    senderId: str
    content: str
    messageType: MessageType
    timestamp: datetime
    isRead: bool

    @validator("timestamp")
    def attach_timezone_to_timestamp(cls, v: datetime):
        if v.tzinfo is None:
            return v.replace(tzinfo=SERVER_TZ)
        return v

    class Config:
        orm_mode = True

class MessageRequest(BaseModel):
    # POST /chats/{orderId}/messages 的请求体
    content: str
    type: MessageType = MessageType.CHAT

class ChatSession(BaseModel):
    # GET /chats/sessions 的响应模型
    orderId: int
    orderTitle: str
    orderStatus: OrderStatus
    participantId: Optional[str] = None
    participantName: Optional[str] = None
    lastMessage: Optional[str] = None
    lastMessageTime: Optional[datetime] = None
    unreadCount: int

    @validator("lastMessageTime")
    def attach_timezone_to_lastUpdated(cls, v: datetime):
        if v.tzinfo is None:
            return v.replace(tzinfo=SERVER_TZ)
        return v

    class Config:
        orm_mode = True

# 实时订单/跟踪模型
class Location(BaseModel):
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    address: Optional[str] = None

    class Config:
        orm_mode = True

class LiveOrder(BaseModel):
    # GET /orders/current 的响应模型
    id: int # 订单 ID
    orderId: int # 重复订单 ID (以匹配 ApiService.kt)
    orderTitle: str
    runnerId: Optional[str] = None
    runnerName: Optional[str] = None
    status: OrderStatus
    currentLocation: Location # 组合 runner_latitude 和 runner_longitude
    pickupLocation: Location
    deliveryLocation: Location
    lastUpdated: datetime

    @validator("lastUpdated")
    def attach_timezone_to_lastUpdated(cls, v: datetime):
        if v.tzinfo is None:
            return v.replace(tzinfo=SERVER_TZ)
        return v

    class Config:
        orm_mode = True

# 搜索模型
class SearchHistory(BaseModel):
    # 对应search_history表
    id: int
    userId: str
    keyword: str
    searchCount: int
    lastSearchedAt: datetime
    createdAt: datetime

    @validator("lastSearchedAt")
    def attach_timezone_to_lastSearched_at(cls, v: datetime):
        if v.tzinfo is None:
            return v.replace(tzinfo=SERVER_TZ)
        return v
    
    @validator("createdAt")
    def attach_timezone_to_created_at(cls, v: datetime):
        if v.tzinfo is None:
            return v.replace(tzinfo=SERVER_TZ)
        return v

    class Config:
        orm_mode = True

class SearchHistoryRequest(BaseModel):
    # POST /search/history 的请求体
    keyword: str
    # userId将从 token 获取, 此处不需要

class SearchHistoryResponse(BaseModel):
    histories: List[SearchHistory]
    total: int

    class Config:
        orm_mode = True

class AddBalanceRequest(BaseModel):
    userId: str
    amount: float

    class Config:
        orm_mode = True

class SubtractBalanceRequest(BaseModel):
    amount: float

    class Config:
        orm_mode = True


# 辅助函数

def verify_password(plain_password: str, hashed_password: str) -> bool:
    """验证明文密码是否与哈希密码匹配"""
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password: str) -> str:
    """生成密码的哈希值"""
    return pwd_context.hash(password)

def create_access_token(data: dict, expires_delta: timedelta = None) -> str:
    """创建 JWT"""
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        # 默认 1 小时
        expire = datetime.utcnow() + timedelta(minutes=60)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

async def get_current_user_id(token: str = Depends(oauth2_scheme)) -> str:
    """
    (关键) FastAPI 依赖项:
    自动验证 'Authorization: Bearer <token>' 请求头, 
    如果有效, 返回 user_id, 否则抛出 401 错误。
    """
    credentials_exception = HTTPException(
        status_code=401,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: str = payload.get("sub")
        if user_id is None:
            raise credentials_exception
        
        # 验证 user_id (从 TokenData Pydantic 模型)
        # (我们信任 token, 不再二次查询数据库, 以提高性能)
        return user_id
        
    except (JWTError, Exception):
        # 捕获包括 ExpiredSignatureError (过期) 在内的所有错误
        raise credentials_exception

async def get_current_user(current_user_id: str = Depends(get_current_user_id)) -> UserProfile:
    """
    在 get_current_user_id 的基础上, 进一步从数据库获取完整的 UserProfile
    """
    user = await database.fetch_one("SELECT * FROM users WHERE id = :id", {"id": current_user_id})
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return UserProfile(**user)

# FastAPI应用创建

app = FastAPI(
    title="Campus Runner API",
    description="这是对 ApiService.kt 的 Python FastAPI 实现, 包含 JWT 认证和 MySQL",
    version="1.0.0"
)

@app.on_event("startup")
async def startup_db_client():
    # FastAPI 启动时, 连接到数据库
    try:
        await database.connect()
        print(f"成功连接到数据库: {DATABASE_URL}")
    except Exception as e:
        print(f"!!! 数据库连接失败: {e}")

@app.on_event("shutdown")
async def shutdown_db_client():
    # FastAPI 关闭时, 断开数据库连接
    await database.disconnect()
    print("已断开与数据库的连接")


# API 实现
# 注册
@app.post("/auth/register", response_model=ApiResponse[UserProfile], tags=["Auth"])
async def register_user(user_in: UserCreate = Body(...)):
    existing_user = await database.fetch_one(
        "SELECT id FROM users WHERE studentId = :studentId", 
        {"studentId": user_in.studentId}
    )
    if existing_user:
        raise HTTPException(status_code=400, detail="Student ID already registered")
        
    hashed_password = get_password_hash(user_in.password)
    new_user_id = str(uuid.uuid4()) # 使用 UUID 作为主键
    
    query = """
        INSERT INTO users (id, studentId, password_hash, name, phone, email, createdAt)
        VALUES (:id, :studentId, :password_hash, :name, :phone, :email, :createdAt)
    """
    values = {
        "id": new_user_id,
        "studentId": user_in.studentId,
        "password_hash": hashed_password,
        "name": user_in.name,
        "phone": user_in.phone,
        "email": user_in.email,
        "createdAt": datetime.now()
    }
    
    try:
        await database.execute(query, values)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error on user creation: {e}")
        
    new_user_profile = await database.fetch_one(
        "SELECT * FROM users WHERE id = :id", {"id": new_user_id}
    )
    
    return ApiResponse(
        code=201, 
        message="User registered successfully", 
        data=UserProfile(**new_user_profile)
    )

# 登录
@app.post("/auth/login", response_model=ApiResponse[LoginResponse], tags=["Auth"])
async def login(request: LoginRequest = Body(...)):
    user = await database.fetch_one("SELECT * FROM users WHERE studentId = :studentId", {"studentId": request.studentId})
    
    if user is None or not verify_password(request.password, user["password_hash"]):
        return ApiResponse(code=401, message="学号或密码错误", data=None)
    
    expires_delta = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    token = create_access_token(
        data={"sub": user["id"]},
        expires_delta=expires_delta
    )
    
    user_profile = UserProfile(**user)
    login_data = LoginResponse(token=token, user=user_profile)
    return ApiResponse(code=200, message="登录成功", data=login_data)

# 获取用户信息
@app.get("/user/profile", response_model=UserProfile, tags=["User"])
async def get_my_user_profile(
    current_user: UserProfile = Depends(get_current_user)
):
    return current_user

# 更新用户信息
@app.put("/user/profile", response_model=ApiResponse[str], tags=["User"])
async def update_my_user_profile(
    profile_update: UserProfile = Body(...),
    current_user_id: str = Depends(get_current_user_id)
):
    query = """
        UPDATE users
        SET name = :name, avatar = :avatar, phone = :phone, email = :email
        WHERE id = :id
    """
    values = {
        "name": profile_update.name,
        "avatar": profile_update.avatar,
        "phone": profile_update.phone,
        "email": profile_update.email,
        "id": current_user_id  # 【安全】强制使用 token 中的 user_id
    }
    
    rows_affected = await database.execute(query, values)
    if rows_affected == 0:
        raise HTTPException(status_code=404, detail="User not found to update")
        
    return ApiResponse(code=200, message="个人信息更新成功", data=None)

# 获取订单信息
@app.get("/tasks", response_model=List[TaskRequest], tags=["Tasks"])
async def get_tasks(
    page: int = 1,
    limit: int = 20,
    type: Optional[str] = Query(None),
    location: Optional[str] = Query(None),
    search: Optional[str] = Query(None)
):
    # 获取公开的任务列表 (仅限 PENDING 状态)
    query = "SELECT * FROM orders WHERE status = :status"
    values = {"status": OrderStatus.PENDING.value}
    
    if type:
        query += " AND type = :type"
        values["type"] = type
    if location:
        query += " AND location LIKE :location"
        values["location"] = f"%{location}%"
    if search:
        query += " AND (title LIKE :search OR description LIKE :search)"
        values["search"] = f"%{search}%"
        
    offset = (page - 1) * limit
    query += " ORDER BY createdAt DESC LIMIT :limit OFFSET :offset"
    values["limit"] = limit
    values["offset"] = offset

    tasks = await database.fetch_all(query=query, values=values)
    tasks_list = [TaskRequest(**t) for t in tasks]
    return tasks_list

# 通过id获取单个任务
@app.get("/tasks/{id}", response_model=TaskRequest, tags=["Tasks"])
async def get_task_detail(id: int = Path(..., description="任务ID")):
    query = "SELECT * FROM orders WHERE id = :id"
    task = await database.fetch_one(query=query, values={"id": id})
    
    if task is None:
        raise HTTPException(status_code=404, detail="Task not found")
    return task

# 接单
@app.post("/tasks/{id}/accept", response_model=ApiResponse[str], tags=["Tasks"])
async def accept_task(
    id: int = Path(..., description="任务ID"),
    current_user_id: str = Depends(get_current_user_id)
):
    async with database.transaction():
        task = await database.fetch_one(
            "SELECT status, publisherId FROM orders WHERE id = :id FOR UPDATE", 
            {"id": id}
        )
        
        if task is None:
            raise HTTPException(status_code=404, detail="Task not found")
        if task["status"] != OrderStatus.PENDING.value:
            raise HTTPException(status_code=400, detail="Task is not available")
        if task["publisherId"] == current_user_id:
            raise HTTPException(status_code=403, detail="You cannot accept your own task")

        query = """
            UPDATE orders 
            SET status = :new_status, runnerId = :runnerId 
            WHERE id = :id AND status = :old_status
        """
        values = {
            "id": id,
            "new_status": OrderStatus.IN_PROGRESS.value,
            "runnerId": current_user_id,
            "old_status": OrderStatus.PENDING.value
        }
        
        rows_affected = await database.execute(query=query, values=values)
        
        if rows_affected == 0:
            raise HTTPException(status_code=409, detail="Task was already accepted (concurrency issue)")

    return ApiResponse(code=200, message="接单成功", data="订单已接受")

# 发布订单
@app.post("/tasks", response_model=ApiResponse[str], tags=["Tasks"])
async def create_task(
    task_request: TaskRequest = Body(...),
    current_user_id: str = Depends(get_current_user_id)
):
    query_to_name = "SELECT name FROM users WHERE id=:userId"
    try:
        user_name = await database.fetch_val(query_to_name, {"userId": current_user_id})
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
    
    query = """
        INSERT INTO orders (title, description, price, type, location, destination, 
                           estimatedTime, contactPhone, specialRequirements, 
                           status, publisherId, createdAt, updatedAt, publisherName)
        VALUES (:title, :description, :price, :type, :location, :destination, 
                :estimatedTime, :contactPhone, :specialRequirements, 
                :status, :publisherId, :createdAt, :updatedAt, :publisherName)
    """
    values = task_request.dict()
    values.update({
        "type": task_request.type.value,
        "status": OrderStatus.PENDING.value,
        "publisherId": current_user_id,
        "createdAt": datetime.now(),
        "updatedAt": datetime.now(),
        "publisherName": str(user_name)
    })
    del values['runnerId']
    del values['runnerName']
    del values['id']
    print(values)
    
    try:
        new_task_id = await database.execute(query=query, values=values)
        return ApiResponse(code=200, message="任务发布成功", data=f"任务ID：{new_task_id}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")

# 获取聊天信息
@app.get("/chats/sessions", response_model=List[ChatSession], tags=["Chat"])
async def get_chat_sessions(
    current_user_id: str = Depends(get_current_user_id)
):
    query = """
        SELECT
            o.id AS orderId,
            o.title AS orderTitle,
            o.status AS orderStatus,
            IF(o.publisherId = :user_id, o.runnerId, o.publisherId) AS participantId,
            u.name AS participantName,
            (SELECT content FROM chat_messages cm 
             WHERE cm.orderId = o.id 
             ORDER BY cm.timestamp DESC LIMIT 1) AS lastMessage,
            (SELECT timestamp FROM chat_messages cm 
             WHERE cm.orderId = o.id 
             ORDER BY cm.timestamp DESC LIMIT 1) AS lastMessageTime,
            (SELECT COUNT(*) FROM chat_messages cm 
             WHERE cm.orderId = o.id AND cm.isRead = FALSE AND cm.senderId != :user_id) AS unreadCount
        FROM
            orders o
        LEFT JOIN
            users u ON u.id = IF(o.publisherId = :user_id, o.runnerId, o.publisherId)
        WHERE
            (o.publisherId = :user_id OR o.runnerId = :user_id)
            AND o.status IN ('IN_PROGRESS', 'PENDING')
        GROUP BY
            o.id, u.name
        ORDER BY
            lastMessageTime DESC;
    """
    sessions = await database.fetch_all(query, {"user_id": current_user_id})
    sessions_list = []
    for s in sessions:
        s_dict = dict(s)
        s_dict['id'] = 'no id'
        if s_dict['lastMessage'] is None:
            s_dict['lastMessage'] = '可以开始对话了'
        if s_dict['lastMessageTime'] is None:
            s_dict['lastMessageTime'] = datetime.now()
        sessions_list.append(ChatSession(**s_dict))

    return sessions_list

# 获取某订单的聊天信息
@app.get("/chats/{orderId}/messages", response_model=List[ChatMessage], tags=["Chat"])
async def get_chat_messages(
    orderId: int = Path(...),
    current_user_id: str = Depends(get_current_user_id) # 【受保护】
):
    order_check = await database.fetch_one(
        "SELECT id FROM orders WHERE id = :orderId AND (publisherId = :user_id OR runnerId = :user_id)",
        {"orderId": orderId, "user_id": current_user_id}
    )
    if order_check is None:
        raise HTTPException(status_code=403, detail="Not authorized to view these messages")

    query = "SELECT * FROM chat_messages WHERE orderId = :orderId ORDER BY timestamp ASC"
    messages = await database.fetch_all(query, {"orderId": orderId})
    messages_list = [ChatMessage(**m) for m in messages]
    return messages_list

# 发送聊天消息
@app.post("/chats/{orderId}/messages", response_model=ApiResponse[str], tags=["Chat"])
async def send_message(
    orderId: int = Path(...),
    message_request: MessageRequest = Body(...),
    current_user_id: str = Depends(get_current_user_id)
):
    order_check = await database.fetch_one(
        "SELECT id, status FROM orders WHERE id = :orderId AND (publisherId = :user_id OR runnerId = :user_id)",
        {"orderId": orderId, "user_id": current_user_id}
    )
    if order_check is None:
        raise HTTPException(status_code=403, detail="Not authorized to send messages to this order")
    if order_check["status"] not in [OrderStatus.IN_PROGRESS.value, OrderStatus.PENDING.value]:
        raise HTTPException(status_code=400, detail="Cannot send messages to a completed or cancelled order")

    query = """
        INSERT INTO chat_messages (orderId, senderId, content, messageType, timestamp, isRead)
        VALUES (:orderId, :senderId, :content, :messageType, :timestamp, :isRead)
    """
    values = {
        "orderId": orderId,
        "senderId": current_user_id,
        "content": message_request.content,
        "messageType": message_request.type,
        "timestamp": datetime.now(),
        "isRead": False
    }
    new_msg_id = await database.execute(query, values)
    return ApiResponse(code=200, message="消息发送成功", data=f"消息ID：{new_msg_id}")

# 获取系统消息
@app.get("/messages/system", response_model=List[Any], tags=["Chat"])
async def get_system_messages(
    current_user_id: str = Depends(get_current_user_id)
):
    query = "SELECT * FROM system_messages WHERE userId = :user_id ORDER BY createdAt DESC"
    messages = await database.fetch_all(query, {"user_id": current_user_id})
    return messages

# 获取用户进行中订单
@app.get("/orders/current", response_model=List[LiveOrder], tags=["Live Order"])
async def get_current_orders(
    current_user_id: str = Depends(get_current_user_id)
):
    query = """
        SELECT 
            o.id AS id,
            o.id AS orderId,
            o.location AS location,
            o.destination AS destination,
            o.title AS orderTitle,
            o.runnerId AS runnerId,
            u_runner.name AS runnerName,
            o.status AS status,
            o.runner_latitude,
            o.runner_longitude
        FROM orders o
        LEFT JOIN users u_runner ON o.runnerId = u_runner.id
        WHERE 
            (o.publisherId = :user_id OR o.runnerId = :user_id)
            AND o.status = 'IN_PROGRESS'
    """
    orders = await database.fetch_all(query, {"user_id": current_user_id})
    
    # 手动组装 Pydantic 模型
    live_orders_list = []
    for order in orders:
        order_dict = dict(order)
        order_dict["currentLocation"] = Location(
            latitude=order_dict["runner_latitude"], 
            longitude=order_dict["runner_longitude"]
        )
        order_dict["pickupLocation"] = Location(
            latitude=order_dict["runner_latitude"], 
            longitude=order_dict["runner_longitude"],
            address=order_dict["location"]
        )
        order_dict["deliveryLocation"] = Location(
            latitude=order_dict["runner_latitude"], 
            longitude=order_dict["runner_longitude"],
            address=order_dict["destination"]
        )
        order_dict["lastUpdated"] = datetime.now()
        print(order_dict)
        live_orders_list.append(LiveOrder(**order_dict))
        
    return live_orders_list

# 获取实时订单跟踪消息
@app.get("/orders/{orderId}/tracking", response_model=LiveOrder, tags=["Live Order"])
async def get_order_tracking(
    orderId: int = Path(...),
    current_user_id: str = Depends(get_current_user_id)
):
    query = """
        SELECT 
            o.id AS id, o.id AS orderId, o.title AS orderTitle,
            o.runnerId AS runnerId, u_runner.name AS runnerName,
            o.status AS status, o.runner_latitude, o.runner_longitude
        FROM orders o
        LEFT JOIN users u_runner ON o.runnerId = u_runner.id
        WHERE 
            o.id = :orderId
            AND (o.publisherId = :user_id OR o.runnerId = :user_id)
    """
    values = {"orderId": orderId, "user_id": current_user_id}
    order = await database.fetch_one(query, values)

    if order is None:
        raise HTTPException(status_code=404, detail="Live order tracking not found or not authorized")
    
    order_dict = dict(order)
    order_dict["currentLocation"] = Location(
        latitude=order.get("runner_latitude"), 
        longitude=order.get("runner_longitude")
    )
    return LiveOrder(**order_dict)

# 获取搜索历史记录
@app.get("/search/history", response_model=SearchHistoryResponse, tags=["Search"])
async def get_search_history(
    limit: int = 10,
    current_user_id: str = Depends(get_current_user_id) # 【受保护】
):
    query = "SELECT * FROM search_history WHERE userId = :userId ORDER BY lastSearchedAt DESC LIMIT :limit"
    histories = await database.fetch_all(query, {"userId": current_user_id, "limit": limit})
    histories_list = [SearchHistory(**h) for h in histories]

    count_query = "SELECT COUNT(*) FROM search_history WHERE userId = :userId"
    total_count = await database.fetch_val(count_query, {"userId": current_user_id})
    
    return SearchHistoryResponse(histories=histories_list, total=total_count)

# 添加搜索历史记录
@app.post("/search/history", response_model=ApiResponse[str], tags=["Search"])
async def add_search_history(
    request: SearchHistoryRequest = Body(...),
    current_user_id: str = Depends(get_current_user_id) # 【受保护】
):
    query = """
        INSERT INTO search_history (userId, keyword, searchCount, lastSearchedAt, createdAt)
        VALUES (:userId, :keyword, 1, :now, :now)
        ON DUPLICATE KEY UPDATE
        searchCount = searchCount + 1,
        lastSearchedAt = :now
    """
    values = {
        "userId": current_user_id,
        "keyword": request.keyword,
        "now": datetime.now()
    }
    await database.execute(query, values)
    return ApiResponse(code=200, message="搜索历史添加成功", data=None)

# 删除搜索历史记录
@app.delete("/search/history/{id}", response_model=ApiResponse[str], tags=["Search"])
async def delete_search_history(
    id: int = Path(...),
    current_user_id: str = Depends(get_current_user_id)
):
    query = "DELETE FROM search_history WHERE id = :id AND userId = :userId"
    await database.execute(query, {"id": id, "userId": current_user_id})
    return ApiResponse(code=200, message="删除成功", data=None)

# 清空搜索历史记录
@app.delete("/search/history", response_model=ApiResponse[str], tags=["Search"])
async def clear_search_history(
    current_user_id: str = Depends(get_current_user_id)
):
    query = "DELETE FROM search_history WHERE userId = :userId"
    await database.execute(query, {"userId": current_user_id})
    return ApiResponse(code=200, message="搜索历史已清空", data=None)

# 获取用户发布的订单历史列表
@app.get("/orders/published", response_model=OrderListResponse, tags=["Order History"])
async def get_published_orders(
    page: int = 1,
    pageSize: int = 20,
    status: Optional[str] = Query(None),
    current_user_id: str = Depends(get_current_user_id)
):
    query = "SELECT * FROM orders WHERE publisher_id = :user_id"
    values = {"user_id": current_user_id}
    
    count_query = "SELECT COUNT(*) FROM orders WHERE publisher_id = :user_id"
    
    if status:
        query += " AND status = :status"
        count_query += " AND status = :status"
        values["status"] = status
        
    offset = (page - 1) * pageSize
    query += " ORDER BY createdAt DESC LIMIT :pageSize OFFSET :offset"
    values["pageSize"] = pageSize
    values["offset"] = offset
    
    orders = await database.fetch_all(query, values)
    orders_list = [TaskRequest(**o) for o in orders]

    total_count = await database.fetch_val(count_query, values)

    return OrderListResponse(orders=orders_list, totalCount=total_count, page=page, pageSize=pageSize)

# 获取用户接单的订单历史列表
@app.get("/orders/accepted", response_model=OrderListResponse, tags=["Order History"])
async def get_accepted_orders(
    page: int = 1,
    pageSize: int = 20,
    status: Optional[str] = Query(None),
    current_user_id: str = Depends(get_current_user_id) # 【受保护】
):
    query = "SELECT * FROM orders WHERE runner_id = :user_id"
    values = {"user_id": current_user_id}
    
    count_query = "SELECT COUNT(*) FROM orders WHERE runner_id = :user_id"
    
    if status:
        query += " AND status = :status"
        count_query += " AND status = :status"
        values["status"] = status
        
    offset = (page - 1) * pageSize
    query += " ORDER BY createdAt DESC LIMIT :pageSize OFFSET :offset"
    values["pageSize"] = pageSize
    values["offset"] = offset
    
    orders = await database.fetch_all(query, values)
    orders_list = [TaskRequest(**o) for o in orders]

    total_count = await database.fetch_val(count_query, values)

    return OrderListResponse(orders=orders_list, totalCount=total_count, page=page, pageSize=pageSize)

# 获取历史订单详情
@app.get("/orders/{orderId}", response_model=TaskRequest, tags=["Order History"])
async def get_order_detail(
    orderId: int = Path(...),
    current_user_id: str = Depends(get_current_user_id)
):
    query = "SELECT * FROM orders WHERE id = :orderId AND (publisher_id = :user_id OR runner_id = :user_id)"
    order = await database.fetch_one(query, {"orderId": orderId, "user_id": current_user_id})
    if order is None:
        raise HTTPException(status_code=404, detail="Order not found or not authorized")
    return order

# 获取用户历史订单统计信息
@app.get("/orders/stats", response_model=OrderStats, tags=["Order History"])
async def get_order_stats(
    current_user_id: str = Depends(get_current_user_id) # 【受保护】
):
    query = """
        SELECT
            (SELECT COUNT(*) FROM orders WHERE publisher_id = :user_id) AS totalPublished,
            (SELECT COUNT(*) FROM orders WHERE runner_id = :user_id) AS totalAccepted,
            (SELECT COUNT(*) FROM orders WHERE runner_id = :user_id AND status = 'COMPLETED') AS totalCompleted,
            COALESCE((SELECT SUM(price) FROM orders WHERE runner_id = :user_id AND status = 'COMPLETED'), 0) AS totalIncome
    """
    stats = await database.fetch_one(query, {"user_id": current_user_id})
    return stats

# 接单
@app.post("/orders/{orderId}/accept", response_model=ApiResponse[None], tags=["Order History"])
async def accept_order(
    orderId: int = Path(...),
    current_user_id: str = Depends(get_current_user_id) # 【受保护】
):
    # 此 API 与 /tasks/{id}/accept 功能完全重叠
    try:
        response = await accept_task(id=orderId, current_user_id=current_user_id)
        return ApiResponse(code=response.code, message=response.message, data=None)
    except HTTPException as e:
        raise e

# 完成订单
@app.post("/orders/{orderId}/complete", response_model=ApiResponse[None], tags=["Order History"])
async def complete_order(
    orderId: int = Path(...),
    current_user_id: str = Depends(get_current_user_id)
):
    query = """
        UPDATE orders 
        SET status = :new_status 
        WHERE id = :id AND runnerId = :user_id AND status = :old_status
    """
    values = {
        "new_status": OrderStatus.COMPLETED.value,
        "id": orderId,
        "user_id": current_user_id,
        "old_status": OrderStatus.IN_PROGRESS.value
    }
    print(values)
    rows_affected = await database.execute(query, values)
    
    if rows_affected == 0:
        raise HTTPException(status_code=403, detail="Order cannot be completed. (Not found, not in progress, or not runner)")

    return ApiResponse(code=200, message="订单已完成", data=None)

# 取消订单
@app.post("/orders/{orderId}/cancel", response_model=ApiResponse[None], tags=["Order History"])
async def cancel_order(
    orderId: int = Path(...),
    current_user_id: str = Depends(get_current_user_id)
):
    query = """
        UPDATE orders 
        SET status = :new_status 
        WHERE id = :id AND publisher_id = :user_id AND status = :old_status
    """
    values = {
        "new_status": OrderStatus.CANCELLED.value,
        "id": orderId,
        "user_id": current_user_id,
        "old_status": OrderStatus.PENDING.value
    }
    rows_affected = await database.execute(query, values)
    
    if rows_affected == 0:
        raise HTTPException(status_code=403, detail="Order cannot be cancelled. (Not found, already accepted, or not publisher)")

    return ApiResponse(code=200, message="订单已取消", data=None)

# 给用户增加余额
@app.post("/user/addBalance", response_model=ApiResponse[str], tags=["User"])
async def add_balance(
    request: AddBalanceRequest = Body(...),
):
    if request.amount <= 0:
        raise HTTPException(status_code=400, detail="增加的金额必须为正数")
    
    query = """
        UPDATE users
        SET balance = balance + :amount
        WHERE id = :id
    """
    values = {
        "amount": request.amount,
        "id": request.userId
    }
    
    rows_affected = await database.execute(query, values)
    if rows_affected == 0:
        raise HTTPException(status_code=404, detail="User not found")
        
    # 获取新余额
    new_balance = await database.fetch_val(
        "SELECT balance FROM users WHERE id = :id", 
        {"id": request.userId}
    )
    
    return ApiResponse(
        code=200, 
        message="余额更新成功", 
        data=f"新的余额: {new_balance}"
    )

# 扣除用户余额
@app.post("/user/subtractBalance", response_model=ApiResponse[str], tags=["User"])
async def subtract_balance(
    request: SubtractBalanceRequest = Body(...),
    current_user_id: str = Depends(get_current_user_id)
):
    if request.amount <= 0:
        raise HTTPException(status_code=400, detail="扣除的金额必须为正数")

    # 使用事务确保数据一致性
    async with database.transaction():
        # 1. 检查当前余额 (并锁定行)
        current_balance = await database.fetch_val(
            "SELECT balance FROM users WHERE id = :id FOR UPDATE", 
            {"id": current_user_id}
        )
        
        if current_balance is None:
            raise HTTPException(status_code=404, detail="User not found")
        
        if current_balance < request.amount:
            raise HTTPException(status_code=400, detail="余额不足")
            
        # 2. 扣除余额
        new_balance = current_balance - request.amount
        query = """
            UPDATE users
            SET balance = :new_balance
            WHERE id = :id
        """
        values = {
            "new_balance": new_balance,
            "id": current_user_id
        }
        
        await database.execute(query, values)
    
    return ApiResponse(
        code=200, 
        message="扣款成功", 
        data=f"新的余额: {new_balance}"
    )


@app.get("/message", response_class=HTMLResponse)
async def read_error_log():
    with open("/root/workspace/running_man_service/www/message.html", "r", encoding="utf-8") as f:
        html_content = f.read()
    return html_content

@app.get("/index", response_class=HTMLResponse)
async def read_error_log():
    with open("/root/workspace/running_man_service/www/index.html", "r", encoding="utf-8") as f:
        html_content = f.read()
    return html_content


# 运行服务器

if __name__ == "__main__":
    print("--- 启动 FastAPI (Campus Runner) 服务器 ---")
    print(f"安全密钥 (SECRET_KEY) 已配置: {SECRET_KEY != 'PLEASE_REPLACE_THIS_WITH_YOUR_OWN_32_BYTE_HEX_SECRET_KEY'}")
    print(f"数据库 (DATABASE_URL) 已配置: {not 'a_strong_password_here' in DATABASE_URL}")
    print("访问 http://127.0.0.1:8000/docs 查看 API 文档")
    
    # 监听 0.0.0.0 允许来自局域网的访问
    uvicorn.run(app, host="0.0.0.0", port=80, log_config='./log_config.json')