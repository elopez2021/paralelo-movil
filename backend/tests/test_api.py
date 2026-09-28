from fastapi.testclient import TestClient
from backend.app.main import app

client = TestClient(app)

def test_full_api_flow():
    # 1. Health check
    r = client.get("/health")
    assert r.status_code == 200

    # 2. Login with default seeded user
    r = client.post("/login", json={"email": "admin@paralelo.com", "password": "admin123"})
    assert r.status_code == 200
    token = r.json()["access_token"]
    headers = {"Authorization": f"Bearer {token}"}

    # 3. Create a test user
    r = client.post(
        "/users",
        json={"nombre": "Test", "apellido": "User", "email": "testuser@paralelo.com", "password": "password123"},
        headers=headers
    )
    assert r.status_code == 201
    user_id = r.json()["id"]

    # 4. List users
    r = client.get("/users", headers=headers)
    assert r.status_code == 200

    # 5. Dashboard endpoints
    assert client.get("/dashboard/profile", headers=headers).status_code == 200
    assert client.get("/dashboard/stats", headers=headers).status_code == 200
    assert client.get("/dashboard/notifications", headers=headers).status_code == 200

    # 6. Upload file
    files = {"file": ("demo.txt", b"Prueba de archivo para Paralelo Movil", "text/plain")}
    r_up = client.post("/upload", files=files, headers=headers)
    assert r_up.status_code == 201
    file_id = r_up.json()["id"]

    # 7. Cleanup
    assert client.delete(f"/upload/{file_id}", headers=headers).status_code == 200
    assert client.delete(f"/users/{user_id}", headers=headers).status_code == 200
