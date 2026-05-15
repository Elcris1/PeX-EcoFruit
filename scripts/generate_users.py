import json
import time
import os
from typing import Dict, Any

# Dependencias requeridas:
# pip install firebase-admin faker

try:
    import firebase_admin
    from firebase_admin import credentials, auth, firestore
    from faker import Faker
except ImportError:
    print("Por favor, instala las dependencias necesarias ejecutando en la terminal:")
    print("pip install firebase-admin faker")
    exit(1)

# Inicializar Faker
fake = Faker('es_ES')

def init_firebase():
    """
    Inicializa Firebase Admin SDK.
    Nota sobre `google-services.json`: Este archivo es para clientes Android.
    El SDK de Admin (usado en Python) requiere una Service Account Key (clave privada) 
    o autenticación a través de Google Cloud CLI.
    """
    # 1. Intentar con Service Account Key si existe en la misma carpeta
    if os.path.exists('serviceAccountKey.json'):
        try:
            cred = credentials.Certificate('serviceAccountKey.json')
            firebase_admin.initialize_app(cred)
            print("Firebase inicializado correctamente con serviceAccountKey.json.")
            return
        except Exception as e:
            print(f"Error al inicializar con serviceAccountKey.json: {e}")

    # 2. Intentar usar Default Credentials obteniendo el project_id de google-services.json
    print("Buscando google-services.json en la carpeta app/ para obtener el project_id...")
    project_id = None
    try:
        # Asumiendo que el script se corre desde /scripts
        gs_path = os.path.join(os.path.dirname(__file__), '..', 'app', 'google-services.json')
        if os.path.exists(gs_path):
            with open(gs_path, 'r') as f:
                gs_data = json.load(f)
                project_id = gs_data['project_info']['project_id']
                print(f"Project ID encontrado: {project_id}")
    except Exception as e:
        print(f"No se pudo extraer project_id de google-services.json: {e}")

    try:
        if project_id:
            firebase_admin.initialize_app(options={'projectId': project_id})
        else:
            firebase_admin.initialize_app()
        print("Firebase inicializado con Application Default Credentials.")
    except Exception as e:
        print(f"\nError de autenticación: {e}")
        print("\nPara que este script funcione correctamente necesitas permisos de Administrador de Firebase.")
        print("Sigue estos pasos:")
        print("1. Ve a Firebase Console -> Configuración del proyecto -> Cuentas de servicio")
        print("2. Clica en 'Generar nueva clave privada' y descárgala.")
        print("3. Renombra el archivo a 'serviceAccountKey.json' y guárdalo en la carpeta 'scripts/'.")
        print("4. Vuelve a ejecutar este script.")
        exit(1)

def generate_user_data(uid: str, is_producer: bool) -> Dict[str, Any]:
    """Genera datos aleatorios que coinciden exactamente con el modelo de datos User.kt y LocationData.kt"""
    now_millis = int(time.time())

    name = fake.name()
    email = fake.unique.email()
    
    location = {
        "latitude": float(fake.latitude()),
        "longitude": float(fake.longitude()),
        "address": fake.street_address(),
        "city": fake.city(),
        "country": "España"
    }
    
    user_data = {
        "id": uid,
        "name": name,
        "email": email,
        "createdAt": now_millis,
        "profileImageUrl": "",
        "bio": fake.text(max_nb_chars=120) if is_producer else fake.sentence(),
        "location": location,
        "isProducer": is_producer,
        "following": [],
        "followers": 0,
        "reviewCount": 0,
        "rating": 0.0
    }
    
    return user_data

def create_users(num_normal: int, num_producers: int):
    """Crea usuarios en Firebase Auth y guarda su modelo de datos en Firestore."""
    db = firestore.client()
    
    total = num_normal + num_producers
    print(f"\nComenzando la creación de {total} usuarios...")
    print(f"- {num_normal} Usuarios Normales")
    print(f"- {num_producers} Usuarios Productores\n")
    
    for i in range(total):
        is_producer = i >= num_normal
        user_type = "Productor" if is_producer else "Normal"
        
        # Generar un usuario dummy
        dummy_data = generate_user_data("temp_id", is_producer)
        password = "Password123!" # Contraseña por defecto para pruebas
        
        try:
            # 1. Crear usuario en Firebase Auth
            user_record = auth.create_user(
                email=dummy_data['email'],
                password=password,
                display_name=dummy_data['name']
            )
            uid = user_record.uid
            
            # 2. Actualizar el ID en los datos
            dummy_data['id'] = uid
            
            # 3. Guardar el documento en la colección 'users' de Firestore
            db.collection('users').document(uid).set(dummy_data)
            
            print(f"[{i+1}/{total}] ✅ {user_type} creado exitosamente: {dummy_data['email']} (UID: {uid})")
            
        except Exception as e:
            print(f"[{i+1}/{total}] ❌ Error al crear usuario {dummy_data['email']}: {e}")

if __name__ == "__main__":
    init_firebase()
    
    # Puedes ajustar la cantidad de usuarios a generar aquí
    CANTIDAD_NORMALES = 5
    CANTIDAD_PRODUCTORES = 5
    
    create_users(CANTIDAD_NORMALES, CANTIDAD_PRODUCTORES)
    print("\n🎉 Proceso finalizado.")
