"""Prepara los logos entregados: lockup exacto y tile del icono para el launcher."""

from pathlib import Path

from PIL import Image, ImageDraw

BASE = Path(r"C:\Users\diego\OneDrive\Documents\GitHub\AquaSample")
OUT = BASE / "docs" / "logos"
RES = BASE / "app" / "src" / "main" / "res"


def squarify(image: Image.Image, size: int = 1024) -> Image.Image:
    side = max(image.width, image.height)
    canvas = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    canvas.paste(image, ((side - image.width) // 2, (side - image.height) // 2))
    return canvas.resize((size, size), Image.LANCZOS)


def rounded_mask(image: Image.Image, radius_ratio: float = 0.18) -> Image.Image:
    mask = Image.new("L", image.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        [0, 0, image.width - 1, image.height - 1],
        radius=int(image.width * radius_ratio),
        fill=255,
    )
    result = image.convert("RGBA")
    result.putalpha(mask)
    return result


def main():
    lockup = Image.open(OUT / "fuente-lockup.png").convert("RGB")
    icon = Image.open(OUT / "fuente-icono.jpg").convert("RGB")

    # Lockup exacto: se recorta el margen gris sobrante, se conserva su fondo.
    lockup_crop = lockup.crop((92, 227, 593, 743))
    lockup_crop.save(OUT / "aquasample-lockup.png")

    # Icono: se recorta el tile blanco completo y se redondean las esquinas.
    tile = icon.crop((214, 207, 820, 819))
    icon_tile = rounded_mask(squarify(tile), 0.19)
    icon_tile.save(OUT / "aquasample-icon.png")

    (RES / "drawable-nodpi").mkdir(parents=True, exist_ok=True)
    lockup_crop.save(RES / "drawable-nodpi" / "ic_aquasample_lockup.png")
    icon_tile.save(RES / "drawable-nodpi" / "ic_launcher_foreground.png")

    for density, px in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)):
        folder = RES / f"mipmap-{density}"
        folder.mkdir(parents=True, exist_ok=True)
        icon_tile.resize((px, px), Image.LANCZOS).save(folder / "ic_launcher.webp", "WEBP", quality=95)

        round_icon = Image.new("RGBA", (1024, 1024), (0, 0, 0, 0))
        mask = Image.new("L", (1024, 1024), 0)
        ImageDraw.Draw(mask).ellipse((0, 0, 1023, 1023), fill=255)
        round_icon.paste(icon_tile, (0, 0), mask)
        round_icon.resize((px, px), Image.LANCZOS).save(
            folder / "ic_launcher_round.webp", "WEBP", quality=95
        )

    # Preview sobre fondo claro
    preview = Image.new("RGBA", (1200, 1200), (246, 250, 252, 255))
    preview.alpha_composite(icon_tile.resize((1024, 1024)), (88, 88))
    preview.convert("RGB").save(OUT / "preview-icon.png")
    print("lockup", lockup_crop.size, "icon", icon_tile.size)


if __name__ == "__main__":
    main()
