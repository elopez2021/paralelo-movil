from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, status, Request
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session

from backend.app.database.session import get_db
from backend.app.models.user import User
from backend.app.schemas.user import UserCreate, UserResponse, LoginRequest
from backend.app.schemas.token import Token
from backend.app.core.security import get_password_hash, verify_password, create_access_token, decode_access_token

router = APIRouter(prefix="", tags=["Autenticación"])
security = HTTPBearer(auto_error=False)


def get_current_user(
    request: Request,
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(security),
    db: Session = Depends(get_db)
) -> User:
    token: Optional[str] = None

    # 1. Si la peticion viene a traves de Google Cloud API Gateway, el token original
    # del cliente es trasladado al encabezado 'X-Forwarded-Authorization'
    forwarded_auth = request.headers.get("x-forwarded-authorization") or request.headers.get("X-Forwarded-Authorization")
    if forwarded_auth:
        parts = forwarded_auth.strip().split(" ", 1)
        if len(parts) == 2 and parts[0].lower() == "bearer":
            token = parts[1]
        elif len(parts) == 1:
            token = parts[0]

    # 2. Si no viene en el encabezado reenviado, leer de las credenciales de Authorization
    if not token and credentials:
        token = credentials.credentials
    elif not token:
        auth_header = request.headers.get("authorization") or request.headers.get("Authorization")
        if auth_header:
            parts = auth_header.strip().split(" ", 1)
            if len(parts) == 2 and parts[0].lower() == "bearer":
                token = parts[1]

    if not token:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Credenciales de autenticacion no suministradas",
            headers={"WWW-Authenticate": "Bearer"},
        )

    payload = decode_access_token(token)
    # Si fallo con el token de X-Forwarded-Authorization (o si este contenia basura),
    # intentar decodificar el token directo si existia
    if (not payload or "sub" not in payload) and credentials and credentials.credentials and credentials.credentials != token:
        payload = decode_access_token(credentials.credentials)

    if not payload or "sub" not in payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token invalido o expirado",
            headers={"WWW-Authenticate": "Bearer"},
        )
    user_id = payload.get("sub")
    user = db.query(User).filter(User.id == int(user_id)).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Usuario no encontrado",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return user


@router.post("/register", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
def register(user_in: UserCreate, db: Session = Depends(get_db)):
    existing = db.query(User).filter(User.email == user_in.email).first()
    if existing:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El correo electrónico ya se encuentra registrado"
        )
    hashed_password = get_password_hash(user_in.password)
    new_user = User(
        nombre=user_in.nombre,
        apellido=user_in.apellido,
        email=user_in.email,
        password=hashed_password,
        foto=user_in.foto,
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return new_user


@router.post("/login", response_model=Token)
def login(login_data: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == login_data.email).first()
    if not user or not verify_password(login_data.password, user.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Credenciales incorrectas"
        )
    
    token = create_access_token(data={"sub": str(user.id), "email": user.email})
    return {
        "access_token": token,
        "token_type": "bearer",
        "user": user
    }
