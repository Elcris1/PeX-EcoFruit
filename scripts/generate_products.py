import json
import os
import random
import time
from typing import Any, Dict, List, Optional

# Dependencias requeridas:
# pip install firebase-admin faker

try:
    import firebase_admin
    from firebase_admin import credentials, firestore, storage
    from faker import Faker
except ImportError:
    print("Por favor, instala las dependencias necesarias ejecutando en la terminal:")
    print("pip install firebase-admin faker")
    exit(1)

fake = Faker("es_ES")
STORAGE_BUCKET_OVERRIDE = "ecofruit-2517b.firebasestorage.app"


def init_firebase() -> None:
    """
    Inicializa Firebase Admin SDK.
    Si existe serviceAccountKey.json en scripts/, se usa para credenciales.
    Si no, intenta obtener project_id desde app/google-services.json y usa ADC.
    """
    storage_bucket = None
    project_id = None

    print("Buscando google-services.json en la carpeta app/ para obtener el project_id...")
    try:
        gs_path = os.path.join(os.path.dirname(__file__), "..", "app", "google-services.json")
        if os.path.exists(gs_path):
            with open(gs_path, "r") as f:
                gs_data = json.load(f)
                project_id = gs_data["project_info"]["project_id"]
                storage_bucket = gs_data["project_info"].get("storage_bucket")
                print(f"Project ID encontrado: {project_id}")
    except Exception as e:
        print(f"No se pudo extraer project_id de google-services.json: {e}")

    if not storage_bucket and STORAGE_BUCKET_OVERRIDE:
        storage_bucket = STORAGE_BUCKET_OVERRIDE

    if os.path.exists("serviceAccountKey.json"):
        try:
            cred = credentials.Certificate("serviceAccountKey.json")
            options = None
            if storage_bucket or project_id:
                if not storage_bucket and project_id:
                    storage_bucket = f"{project_id}.appspot.com"
                options = {"storageBucket": storage_bucket}
            firebase_admin.initialize_app(cred, options=options)
            print("Firebase inicializado con serviceAccountKey.json.")
            return
        except Exception as e:
            print(f"Error al inicializar con serviceAccountKey.json: {e}")

    try:
        if project_id:
            if not storage_bucket:
                storage_bucket = f"{project_id}.appspot.com"
            firebase_admin.initialize_app(
                options={"projectId": project_id, "storageBucket": storage_bucket}
            )
        else:
            firebase_admin.initialize_app()
        print("Firebase inicializado con Application Default Credentials.")
    except Exception as e:
        print(f"\nError de autenticacion: {e}")
        print("\nPara que este script funcione correctamente necesitas permisos de Administrador de Firebase.")
        print("Sigue estos pasos:")
        print("1. Ve a Firebase Console -> Configuracion del proyecto -> Cuentas de servicio")
        print("2. Clica en 'Generar nueva clave privada' y descargala.")
        print("3. Renombra el archivo a 'serviceAccountKey.json' y guardalo en la carpeta 'scripts/'.")
        print("4. Vuelve a ejecutar este script.")
        exit(1)


def pick_product_name(product_type: str) -> str:
    fruits = ["Manzana", "Pera", "Naranja", "Mandarina", "Uva", "Melocoton", "Fresa", "Sandia"]
    vegetables = ["Tomate", "Lechuga", "Zanahoria", "Cebolla", "Pimiento", "Calabacin", "Berenjena"]
    cereal = ["Trigo", "Maiz", "Avena", "Arroz", "Cebada"]
    traditional = ["Mermelada", "Pan", "Aceite", "Queso", "Miel"]
    plants = ["Albahaca", "Romero", "Menta", "Perejil", "Aloe vera"]
    from_animal = ["Huevos", "Leche", "Yogur", "Queso", "Miel"]
    other = ["Frutos secos", "Setas", "Conservas", "Harina", "Especias"]

    pools = {
        "FRUITS": fruits,
        "VEGETABLES": vegetables,
        "CEREAL": cereal,
        "TRADITIONAL": traditional,
        "PLANTS": plants,
        "FROM_ANIMAL": from_animal,
        "OTHER": other,
    }

    return random.choice(pools.get(product_type, other))


def list_local_images(images_dir: str) -> List[str]:
    if not os.path.isdir(images_dir):
        return []
    candidates: List[str] = []
    for name in os.listdir(images_dir):
        lower = name.lower()
        if lower.endswith(".jpg") or lower.endswith(".jpeg") or lower.endswith(".png"):
            candidates.append(os.path.join(images_dir, name))
    return candidates


def upload_image(bucket: Any, file_path: str, destination_path: str) -> str:
    """Sube una imagen al bucket y devuelve su URL publica."""
    blob = bucket.blob(destination_path)
    blob.upload_from_filename(file_path, content_type="image/jpeg")
    blob.make_public()
    return blob.public_url


def build_product_data(
    producer: Dict[str, Any],
    producer_id: str,
    images_url: List[str],
    product_type: str,
    unit: str,
    name: str,
) -> Dict[str, Any]:
    now_millis = int(time.time())
    price = round(random.uniform(1.0, 8.0), 2)
    user_name = producer.get("name", "")
    user_avatar = producer.get("profileImageUrl", "")

    return {
        "id": "",
        "name": name,
        "description": fake.sentence(),
        "createdAt": now_millis,
        "imagesUrl": images_url,
        "location": producer.get("location"),
        "price": price,
        "unit": unit,
        "isOrganic": random.choice([True, False]),
        "type": product_type,
        "userId": producer_id,
        "userName": user_name,
        "userAvatar": user_avatar,
        "favouritesList": [],
        "rating": 0.0,
        "reviewCount": 0,
        "recommendationScore": 0.0,
    }


def create_products(
    products_per_producer: int,
    images_dir: Optional[str],
    use_storage: bool,
) -> None:
    db = firestore.client()
    producers = db.collection("users").where("isProducer", "==", True).stream()

    bucket = None
    if use_storage:
        try:
            bucket = storage.bucket()
        except Exception as e:
            print(f"No se pudo inicializar Storage: {e}")
            print("Continuando sin subida de imagenes.")
            bucket = None

    product_types = ["FRUITS", "VEGETABLES", "CEREAL", "TRADITIONAL", "OTHER", "PLANTS", "FROM_ANIMAL"]
    product_units = ["KG", "UNIT", "LITER"]

    created = 0
    for producer_doc in producers:
        producer = producer_doc.to_dict()
        producer_id = producer.get("id") or producer_doc.id
        for _ in range(products_per_producer):
            product_type = random.choice(product_types)
            name = pick_product_name(product_type)
            if name in {"Leche", "Aceite"}:
                unit = "LITER"
            else:
                unit = random.choice(product_units[:1])

            images_url: List[str] = []
            type_name_dir = (
                os.path.join(images_dir, product_type, name) if images_dir else None
            )
            local_images = list_local_images(type_name_dir) if type_name_dir else []

            if local_images and bucket:
                image_path = random.choice(local_images)
                dest = f"products/{producer.get('id', 'unknown')}/{os.path.basename(image_path)}"
                try:
                    images_url.append(upload_image(bucket, image_path, dest))
                except Exception as e:
                    print(f"No se pudo subir la imagen {image_path}: {e}")
            elif local_images:
                # Sin Storage configurado, dejamos las rutas locales como referencia.
                images_url.append(os.path.abspath(random.choice(local_images)))

            product_data = build_product_data(
                producer, producer_id, images_url, product_type, unit, name
            )
            doc_ref = db.collection("products").document()
            product_data["id"] = doc_ref.id
            doc_ref.set(product_data)
            created += 1

            print(
                f"Producto creado: {product_data['name']} (prod: {producer.get('name', 'N/A')})"
            )

    print(f"\nTotal de productos creados: {created}")


if __name__ == "__main__":
    init_firebase()

    # Configuracion basica
    PRODUCTS_PER_PRODUCER = 5
    IMAGES_DIR = os.path.join(os.path.dirname(__file__), "images")
    USE_STORAGE = True

    create_products(PRODUCTS_PER_PRODUCER, IMAGES_DIR, USE_STORAGE)
    print("\nProceso finalizado.")
