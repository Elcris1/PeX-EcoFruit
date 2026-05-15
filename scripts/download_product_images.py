import argparse
import os
import re
import time
from typing import Dict, List
from concurrent.futures import ThreadPoolExecutor, as_completed
import requests


PIXABAY_API_URL = "https://pixabay.com/api/"
DEFAULT_IMAGE_DIR = os.path.join(os.path.dirname(__file__), "images")


PRODUCT_NAMES_BY_TYPE: Dict[str, List[str]] = {
    "FRUITS": ["Manzana", "Pera", "Naranja", "Mandarina", "Uva", "Melocoton", "Fresa", "Sandia"],
    "VEGETABLES": ["Tomate", "Lechuga", "Zanahoria", "Cebolla", "Pimiento", "Calabacin", "Berenjena"],
    "CEREAL": ["Trigo", "Maiz", "Avena", "Arroz", "Cebada"],
    "TRADITIONAL": ["Mermelada", "Pan", "Aceite", "Queso", "Miel"],
    "PLANTS": ["Albahaca", "Romero", "Menta", "Perejil", "Aloe vera"],
    "FROM_ANIMAL": ["Huevos", "Leche", "Yogur", "Queso", "Miel"],
    "OTHER": ["Frutos secos", "Setas", "Conservas", "Harina", "Especias"],
}

PRODUCT_NAME_TRANSLATIONS: Dict[str, Dict[str, str]] = {
    "FRUITS": {
        "Manzana": "apple",
        "Pera": "pear",
        "Naranja": "orange",
        "Mandarina": "mandarin",
        "Uva": "grape",
        "Melocoton": "peach",
        "Fresa": "strawberry",
        "Sandia": "watermelon",
    },
    "VEGETABLES": {
        "Tomate": "tomato",
        "Lechuga": "lettuce",
        "Zanahoria": "carrot",
        "Cebolla": "onion",
        "Pimiento": "pepper",
        "Calabacin": "zucchini",
        "Berenjena": "eggplant",
    },
    "CEREAL": {
        "Trigo": "wheat",
        "Maiz": "corn",
        "Avena": "oats",
        "Arroz": "rice",
        "Cebada": "barley",
    },
    "TRADITIONAL": {
        "Mermelada": "jam",
        "Pan": "bread",
        "Aceite": "olive oil",
        "Queso": "cheese",
        "Miel": "honey",
    },
    "PLANTS": {
        "Albahaca": "basil",
        "Romero": "rosemary",
        "Menta": "mint",
        "Perejil": "parsley",
        "Aloe vera": "aloe vera",
    },
    "FROM_ANIMAL": {
        "Huevos": "eggs",
        "Leche": "milk",
        "Yogur": "yogurt",
        "Queso": "cheese",
        "Miel": "honey",
    },
    "OTHER": {
        "Frutos secos": "nuts",
        "Setas": "mushrooms",
        "Conservas": "preserves",
        "Harina": "flour",
        "Especias": "spices",
    },
}




def slugify(value: str) -> str:
    value = value.strip().lower()
    value = re.sub(r"\s+", "-", value)
    value = re.sub(r"[^a-z0-9\-]", "", value)
    return value or "item"


def build_query(product_type: str, name: str) -> str:
    # Add a generic agricultural context to improve results quality.
    english_name = PRODUCT_NAME_TRANSLATIONS.get(product_type, {}).get(name, "")
    if english_name:
        return f"{name} {english_name} "
    return f"{name} "


def download_image(url: str, dest_path: str, timeout: int = 20) -> bool:
    try:
        response = requests.get(url, timeout=timeout)
        response.raise_for_status()
    except requests.RequestException as exc:
        print(f"No se pudo descargar {url}: {exc}")
        return False

    with open(dest_path, "wb") as handle:
        handle.write(response.content)
    return True


def fetch_pixabay_images(
    api_key: str,
    query: str,
    per_page: int,
    image_type: str = "photo",
    orientation: str = "horizontal",
) -> List[Dict[str, str]]:
    params = {
        "key": api_key,
        "q": query,
        "per_page": 5,
        "language": "es",
    }

    response = requests.get(PIXABAY_API_URL, params=params, timeout=20)
    response.raise_for_status()
    data = response.json()
    return data.get("hits", [])


def ensure_dir(path: str) -> None:
    os.makedirs(path, exist_ok=True)


def download_images_for_product(
    api_key: str,
    product_type: str,
    name: str,
    out_dir: str,
    images_per_product: int,
    delay_seconds: float,
) -> int:
    print(f"Descargando imágenes para {product_type}/{name}...")
    query = build_query(product_type, name)
    hits = fetch_pixabay_images(api_key, query, per_page=images_per_product)

    if not hits:
        print(f"Sin resultados para {product_type}/{name}")
        return 0

    product_dir = os.path.join(out_dir, product_type, slugify(name))
    ensure_dir(product_dir)

    downloaded = 0
    for hit in hits:
        image_url = hit.get("largeImageURL") or hit.get("webformatURL")
        if not image_url:
            continue

        filename = f"{slugify(name)}-{hit.get('id', downloaded)}.jpg"
        dest_path = os.path.join(product_dir, filename)
        if os.path.exists(dest_path):
            continue

        if download_image(image_url, dest_path):
            downloaded += 1
            time.sleep(delay_seconds)

        if downloaded >= images_per_product:
            break
    return downloaded

def main() -> None:
    from dotenv import load_dotenv
    load_dotenv()
    parser = argparse.ArgumentParser(
        description="Descarga imagenes de productos agricolas y las organiza por tipo/nombre."
    )
    parser.add_argument(
        "--api-key",
        default=os.getenv("PIXABAY_API_KEY"),
        help="API key de Pixabay (o usar variable PIXABAY_API_KEY)",
    )
    parser.add_argument(
        "--out-dir",
        default=DEFAULT_IMAGE_DIR,
        help="Carpeta base para guardar imagenes",
    )
    parser.add_argument(
        "--images-per-product",
        type=int,
        default=3,
        help="Numero de imagenes por producto",
    )
    parser.add_argument(
        "--delay-seconds",
        type=float,
        default=0.5,
        help="Pausa entre descargas para evitar rate limits",
    )
    parser.add_argument(
        "--max-workers",
        type=int,
        default=5,
        help="Numero maximo de hilos concurrentes",
    )
    parser.add_argument(
        "--only-type",
        default=None,
        help="Descargar solo un tipo (ej: FRUITS)",
    )
    parser.add_argument(
        "--only-name",
        default=None,
        help="Descargar solo un nombre (ej: Manzana)",
    )
    args = parser.parse_args()

    if not args.api_key:
        print("Falta API key. Define PIXABAY_API_KEY o usa --api-key.")
        raise SystemExit(1)

    out_dir = os.path.abspath(args.out_dir)
    ensure_dir(out_dir)

    tasks = []
    with ThreadPoolExecutor(max_workers=args.max_workers) as executor:
        for product_type, names in PRODUCT_NAMES_BY_TYPE.items():
            if args.only_type and args.only_type != product_type:
                continue
            for name in names:
                if args.only_name and args.only_name != name:
                    continue
                tasks.append(
                    executor.submit(
                        download_images_for_product,
                        args.api_key,
                        product_type,
                        name,
                        out_dir,
                        args.images_per_product,
                        args.delay_seconds,
                    )
                )

        for task in as_completed(tasks):
            task.result()





if __name__ == "__main__":
    main()
