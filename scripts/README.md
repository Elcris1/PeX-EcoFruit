# MOCK DATA GENERATOR

Aprofitant-me de les facilitats de la IA i amb una mica del meu toc personal, he vibecoded principalment tot el que hi ha a `scripts/`.

## Comptes de prova

Els comptes es poden accedir amb la password: `Password123!`

- consumidor: gloria98@example.net
- consumidor: fajardoerasmo@example.net
- consumidor: zaida36@example.org
- consumidor: eligioarnaiz@example.com
- consumidor: anunciacion98@example.org
- productor: adolfopedro@example.net
- productor: luis-miguel01@example.net
- productor: angelicamelero@example.com
- productor: esterprado@example.com
- productor: miguela56@example.net

## Productes generats

- Setas (prod: Juan Pablo de Ibarra)
- Calabacin (prod: Juan Pablo de Ibarra)
- Especias (prod: Juan Pablo de Ibarra)
- Miel (prod: Juan Pablo de Ibarra)
- Manzana (prod: Juan Pablo de Ibarra)
- Queso (prod: RealGProducer)
- Aloe vera (prod: RealGProducer)
- Aceite (prod: RealGProducer)
- Harina (prod: RealGProducer)
- Avena (prod: RealGProducer)
- Calabacin (prod: Patricia del Guerrero)
- Berenjena (prod: Patricia del Guerrero)
- Especias (prod: Patricia del Guerrero)
- Miel (prod: Patricia del Guerrero)
- Avena (prod: Patricia del Guerrero)
- Pera (prod: prueba)
- Queso (prod: prueba)
- Aceite (prod: prueba)
- Uva (prod: prueba)
- Perejil (prod: prueba)
- Berenjena (prod: Manu Higueras-Perales)
- Arroz (prod: Manu Higueras-Perales)
- Calabacin (prod: Manu Higueras-Perales)
- Queso (prod: Manu Higueras-Perales)
- Cebolla (prod: Manu Higueras-Perales)
- Harina (prod: Azeneth Castello Cabanas)
- Especias (prod: Azeneth Castello Cabanas)
- Berenjena (prod: Azeneth Castello Cabanas)
- Conservas (prod: Azeneth Castello Cabanas)
- Queso (prod: Azeneth Castello Cabanas)
- Especias (prod: Yolanda de Zaragoza)
- Queso (prod: Yolanda de Zaragoza)
- Pera (prod: Yolanda de Zaragoza)
- Calabacin (prod: Yolanda de Zaragoza)
- Fresa (prod: Yolanda de Zaragoza)
- Maiz (prod: newTEst)
- Aceite (prod: newTEst)
- Pera (prod: newTEst)
- Conservas (prod: newTEst)
- Pimiento (prod: newTEst)

## Com executar

1. Afegeix la clau privada a `scripts/serviceAccountKey.json` (si tens `serviceAccount.json`, renombra-la).
2. Copia `scripts/.env.example` a `scripts/.env` i reemplassa la API key.
3. Instal.la dependencias:

```bash
uv sync
```

4. Executa els scripts:

```bash
uv run python3 download_product_images.py
uv run python3 generate_users.py
uv run python3 generate_products.py
uv run python3 cleanup_users_products.py --dry-run
uv run python3 cleanup_users_products.py
```
