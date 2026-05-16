import json
import os
from typing import List

# Dependencias requeridas:
# pip install firebase-admin

try:
    import firebase_admin
    from firebase_admin import auth, credentials, firestore
except ImportError:
    print("Por favor, instala las dependencias necesarias ejecutando en la terminal:")
    print("pip install firebase-admin")
    raise SystemExit(1)


def init_firebase() -> None:
    """
    Inicializa Firebase Admin SDK.
    Si existe serviceAccountKey.json en scripts/, se usa para credenciales.
    Si no, intenta obtener project_id desde app/google-services.json y usa ADC.
    """
    if os.path.exists("serviceAccountKey.json"):
        try:
            cred = credentials.Certificate("serviceAccountKey.json")
            firebase_admin.initialize_app(cred)
            print("Firebase inicializado con serviceAccountKey.json.")
            return
        except Exception as e:
            print(f"Error al inicializar con serviceAccountKey.json: {e}")

    print("Buscando google-services.json en la carpeta app/ para obtener el project_id...")
    project_id = None
    try:
        gs_path = os.path.join(os.path.dirname(__file__), "..", "app", "google-services.json")
        if os.path.exists(gs_path):
            with open(gs_path, "r") as f:
                gs_data = json.load(f)
                project_id = gs_data["project_info"]["project_id"]
                print(f"Project ID encontrado: {project_id}")
    except Exception as e:
        print(f"No se pudo extraer project_id de google-services.json: {e}")

    try:
        if project_id:
            firebase_admin.initialize_app(options={"projectId": project_id})
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
        raise SystemExit(1)


def list_users_to_delete(email_suffix: str) -> List[auth.UserRecord]:
    users_to_delete: List[auth.UserRecord] = []
    page = auth.list_users()
    while page:
        for user in page.users:
            if user.email and not user.email.endswith(email_suffix):
                users_to_delete.append(user)
        page = page.get_next_page()
    return users_to_delete


def delete_user_products(db: firestore.Client, user_id: str, dry_run: bool) -> int:
    deleted = 0
    while True:
        docs = (
            db.collection("products")
            .where("userId", "==", user_id)
            .limit(500)
            .stream()
        )
        batch = db.batch()
        count = 0
        for doc in docs:
            count += 1
            deleted += 1
            if not dry_run:
                batch.delete(doc.reference)
        if count == 0:
            break
        if not dry_run:
            batch.commit()
    return deleted


def delete_user_document(db: firestore.Client, user_id: str, dry_run: bool) -> None:
    if dry_run:
        return
    db.collection("users").document(user_id).delete()


def delete_auth_user(user_id: str, dry_run: bool) -> None:
    if dry_run:
        return
    auth.delete_user(user_id)


def main() -> None:
    import argparse

    parser = argparse.ArgumentParser(
        description="Elimina usuarios cuyo email no termina con un sufijo y borra sus productos."
    )
    parser.add_argument(
        "--email-suffix",
        default="@email.com",
        help="Sufijo permitido (se borran los usuarios que NO lo cumplan)",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Solo muestra lo que se eliminaria",
    )
    args = parser.parse_args()

    init_firebase()
    db = firestore.client()

    users = list_users_to_delete(args.email_suffix)
    if not users:
        print("No hay usuarios para eliminar.")
        return

    total_products = 0
    for user in users:
        print(f"Eliminando usuario {user.uid} ({user.email})")
        deleted = delete_user_products(db, user.uid, args.dry_run)
        total_products += deleted
        delete_user_document(db, user.uid, args.dry_run)
        delete_auth_user(user.uid, args.dry_run)

    print(f"Usuarios eliminados: {len(users)}")
    print(f"Productos eliminados: {total_products}")


if __name__ == "__main__":
    main()
