"""Genera propuestas visuales de logo para AquaSample (icono + lockup)."""

import math
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFont

BASE = Path(r"C:\Users\diego\OneDrive\Documents\GitHub\AquaSample")
OUT = BASE / "docs" / "logos"
OUT.mkdir(parents=True, exist_ok=True)

OCEAN = (10, 87, 122)
DEEP = (6, 50, 70)
KELP = (31, 122, 87)
MINT = (127, 211, 172)
SAND = (255, 212, 138)
LIGHT = (246, 250, 252)
WHITE = (255, 255, 255)
INK = (11, 27, 35)
MUTED = (91, 107, 122)

FONT_BOLD = "C:/Windows/Fonts/segoeuib.ttf"
FONT_REG = "C:/Windows/Fonts/segoeui.ttf"

S = 2048


def font(size, bold=True):
    return ImageFont.truetype(FONT_BOLD if bold else FONT_REG, size)


def gradient(size, top, bottom):
    img = Image.new("RGB", (size, size))
    draw = ImageDraw.Draw(img)
    for y in range(size):
        t = y / (size - 1)
        color = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
        draw.line([(0, y), (size, y)], fill=color)
    return img.convert("RGBA")


def wave_points(y, amplitude, phase, size=S):
    points = []
    for x in range(0, size + 1, 8):
        points.append((x, y + amplitude * math.sin(x / size * 2 * math.pi * 2 + phase)))
    return points


def draw_waves(draw, size, color, width, base_y, amplitude, count=3, gap=0.06):
    for index in range(count):
        draw.line(
            wave_points(size * (base_y + index * gap), size * amplitude, index * 1.1, size),
            fill=color,
            width=width,
            joint="curve",
        )


def shell_polygon(size, base_y=0.77, radius=0.36):
    base = (size * 0.5, size * base_y)
    points = [base]
    for angle in range(200, 341, 5):
        rad = math.radians(angle)
        points.append((base[0] + size * radius * math.cos(rad), base[1] + size * radius * math.sin(rad)))
    return base, points


def rounded_icon(img, size, radius_ratio=0.22):
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        [0, 0, size - 1, size - 1], radius=int(size * radius_ratio), fill=255
    )
    img.putalpha(mask)
    return img


def concept_shell():
    img = gradient(S, (14, 110, 150), DEEP)
    draw = ImageDraw.Draw(img, "RGBA")
    draw_waves(draw, S, (*MINT, 90), int(S * 0.010), 0.86, 0.012, count=3)
    draw_waves(draw, S, (*WHITE, 45), int(S * 0.006), 0.90, 0.010, count=2)

    base, poly = shell_polygon(S)
    draw.polygon(poly, fill=(*WHITE, 255))
    for step in range(7):
        angle = math.radians(200 + step * (140 / 6))
        end = (base[0] + S * 0.36 * math.cos(angle), base[1] + S * 0.36 * math.sin(angle))
        draw.line([base, end], fill=(*OCEAN, 255), width=int(S * 0.011))
    bbox = (base[0] - S * 0.36, base[1] - S * 0.36, base[0] + S * 0.36, base[1] + S * 0.36)
    draw.arc(bbox, 200, 340, fill=(*MINT, 255), width=int(S * 0.012))
    draw.ellipse(
        (base[0] - S * 0.055, base[1] - S * 0.03, base[0] + S * 0.055, base[1] + S * 0.05),
        fill=(*KELP, 255),
    )
    return rounded_icon(img, S)


def concept_monogram():
    img = gradient(S, (14, 110, 150), DEEP)
    draw = ImageDraw.Draw(img, "RGBA")
    draw_waves(draw, S, (*MINT, 70), int(S * 0.009), 0.88, 0.010, count=2)

    apex = (S * 0.5, S * 0.24)
    left = (S * 0.27, S * 0.77)
    right = (S * 0.73, S * 0.77)
    width = int(S * 0.125)
    draw.line([apex, left], fill=WHITE, width=width, joint="curve")
    draw.line([apex, right], fill=WHITE, width=width, joint="curve")
    draw.line([(S * 0.34, S * 0.62), (S * 0.66, S * 0.62)], fill=WHITE, width=int(S * 0.10))
    for point in (apex, left, right):
        r = width / 2
        draw.ellipse((point[0] - r, point[1] - r, point[0] + r, point[1] + r), fill=WHITE)

    draw.ellipse(
        (S * 0.5 - S * 0.052, S * 0.47 - S * 0.052, S * 0.5 + S * 0.052, S * 0.47 + S * 0.052),
        fill=(*KELP, 255),
    )
    draw.ellipse(
        (S * 0.5 - S * 0.022, S * 0.47 - S * 0.022, S * 0.5 + S * 0.022, S * 0.47 + S * 0.022),
        fill=(*MINT, 255),
    )
    return rounded_icon(img, S)


def concept_waves():
    img = gradient(S, (14, 110, 150), DEEP)
    draw = ImageDraw.Draw(img, "RGBA")
    draw_waves(draw, S, (*WHITE, 230), int(S * 0.016), 0.40, 0.035, count=1)
    draw_waves(draw, S, (*MINT, 235), int(S * 0.018), 0.51, 0.045, count=1)
    draw_waves(draw, S, (*WHITE, 210), int(S * 0.014), 0.62, 0.035, count=1)

    cx, cy, r = S * 0.5, S * 0.505, S * 0.075
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=(*SAND, 255))
    draw.ellipse((cx - r * 0.52, cy - r * 0.52, cx + r * 0.52, cy + r * 0.52), fill=(*OCEAN, 255))
    return rounded_icon(img, S)


def concept_grid():
    img = gradient(S, (14, 110, 150), DEEP)
    draw = ImageDraw.Draw(img, "RGBA")

    base, poly = shell_polygon(S, base_y=0.78, radius=0.34)
    mask = Image.new("L", (S, S), 0)
    ImageDraw.Draw(mask).polygon(poly, fill=255)

    grid = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    grid_draw = ImageDraw.Draw(grid)
    dot = S * 0.014
    spacing = S * 0.078
    start_x = S * 0.32
    start_y = S * 0.34
    for row in range(6):
        for col in range(5):
            x = start_x + col * spacing
            y = start_y + row * spacing
            grid_draw.ellipse((x - dot, y - dot, x + dot, y + dot), fill=(*MINT, 255))

    grid.putalpha(ImageChops.multiply(grid.getchannel("A"), mask))
    img = Image.alpha_composite(img, grid)
    draw = ImageDraw.Draw(img, "RGBA")

    bbox = (base[0] - S * 0.34, base[1] - S * 0.34, base[0] + S * 0.34, base[1] + S * 0.34)
    draw.arc(bbox, 200, 340, fill=(*WHITE, 220), width=int(S * 0.012))
    draw.line([poly[0], poly[-1]], fill=(*WHITE, 220), width=int(S * 0.012))
    draw.line([poly[0], poly[1]], fill=(*WHITE, 220), width=int(S * 0.012))
    draw.ellipse(
        (base[0] - S * 0.055, base[1] - S * 0.03, base[0] + S * 0.055, base[1] + S * 0.05),
        fill=(*SAND, 255),
    )
    return rounded_icon(img, S)


def resize_icon(img, name):
    img = img.resize((1024, 1024), Image.LANCZOS)
    img.save(OUT / f"{name}.png")
    return img


def make_sheet(icons):
    width, height = 1800, 2180
    sheet = Image.new("RGB", (width, height), LIGHT)
    draw = ImageDraw.Draw(sheet)

    draw.text((80, 64), "AquaSample", font=font(78), fill=OCEAN)
    draw.text(
        (82, 158),
        "Propuestas de logo · elige una para reemplazar ALDEMAR en el login y el icono de la app",
        font=font(28, bold=False),
        fill=MUTED,
    )

    labels = [
        ("1 · Valva y ola", "Concha de chorito con oleaje. Lectura inmediata en el icono."),
        ("2 · Monograma A", "A geométrica con gota de agua. Más corporativo y sobrio."),
        ("3 · Onda y muestra", "Oleaje y punto de muestreo. Minimalista y moderno."),
        ("4 · Retícula", "Matriz de calibre dentro de la valva. Conecta con la app."),
    ]

    positions = [(80, 240), (940, 240), (80, 1180), (940, 1180)]
    card_w, card_h = 780, 860
    for index, (icon, (title, description)) in enumerate(zip(icons, labels)):
        x, y = positions[index]
        draw.rounded_rectangle(
            [x, y, x + card_w, y + card_h],
            radius=34,
            fill=WHITE,
            outline=(220, 232, 239),
            width=2,
        )
        icon_size = 560
        sheet.paste(
            icon.resize((icon_size, icon_size), Image.LANCZOS),
            (x + (card_w - icon_size) // 2, y + 48),
            icon.resize((icon_size, icon_size), Image.LANCZOS),
        )
        draw.text((x + 40, y + 650), title, font=font(38), fill=INK)
        draw.text((x + 40, y + 706), description, font=font(25, bold=False), fill=MUTED)

    draw.text(
        (80, 2100),
        "Paleta: océano #0A577A · kelp #1F7A57 · arena #FFD48A · fondo #F6FAFC",
        font=font(24, bold=False),
        fill=MUTED,
    )
    sheet.save(OUT / "propuestas.png")


def make_lockup(icon):
    width, height = 1700, 460
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    icon_size = 360
    img.paste(
        icon.resize((icon_size, icon_size), Image.LANCZOS),
        (60, 50),
        icon.resize((icon_size, icon_size), Image.LANCZOS),
    )
    draw = ImageDraw.Draw(img)
    draw.text((480, 105), "AquaSample", font=font(132), fill=OCEAN)
    draw.text((486, 260), "Control de muestreo de moluscos", font=font(38, bold=False), fill=KELP)
    img.save(OUT / "lockup-valva.png")


def main():
    icons = [
        resize_icon(concept_shell(), "propuesta-1-valva"),
        resize_icon(concept_monogram(), "propuesta-2-monograma"),
        resize_icon(concept_waves(), "propuesta-3-onda"),
        resize_icon(concept_grid(), "propuesta-4-reticula"),
    ]
    make_sheet(icons)
    make_lockup(icons[0])
    print("OK:", OUT)


if __name__ == "__main__":
    main()
