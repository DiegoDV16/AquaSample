"""Propuestas de logo con motivo de chorito (Mytilus chilensis)."""

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

S = 2048
FONT_BOLD = "C:/Windows/Fonts/segoeuib.ttf"
FONT_REG = "C:/Windows/Fonts/segoeui.ttf"


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


def cubic(p0, p1, p2, p3, steps=90):
    points = []
    for index in range(1, steps + 1):
        t = index / steps
        mt = 1 - t
        x = mt**3 * p0[0] + 3 * mt**2 * t * p1[0] + 3 * mt * t**2 * p2[0] + t**3 * p3[0]
        y = mt**3 * p0[1] + 3 * mt**2 * t * p1[1] + 3 * mt * t**2 * p2[1] + t**3 * p3[1]
        points.append((x, y))
    return points


def rotate_norm(x, y, angle_deg, center=(0.5, 0.55)):
    rad = math.radians(angle_deg)
    dx, dy = x - center[0], y - center[1]
    return (
        center[0] + dx * math.cos(rad) - dy * math.sin(rad),
        center[1] + dx * math.sin(rad) + dy * math.cos(rad),
    )


def chorito_points(size, scale=1.0, offset=(0.0, 0.0), angle=-22):
    hinge = (0.46, 0.18)

    def p(x, y):
        nx = hinge[0] + (x - hinge[0]) * scale + offset[0]
        ny = hinge[1] + (y - hinge[1]) * scale + offset[1]
        rx, ry = rotate_norm(nx, ny, angle)
        return (rx * size, ry * size)

    umbo = p(0.46, 0.18)
    seg1 = cubic(umbo, p(0.60, 0.19), p(0.76, 0.40), p(0.73, 0.60))
    seg2 = cubic(seg1[-1], p(0.71, 0.75), p(0.62, 0.86), p(0.49, 0.89))
    seg3 = cubic(seg2[-1], p(0.35, 0.91), p(0.24, 0.79), p(0.23, 0.62))
    seg4 = cubic(seg3[-1], p(0.22, 0.44), p(0.33, 0.23), umbo)
    return [umbo] + seg1 + seg2 + seg3 + seg4


def wave_points(y, amplitude, phase, size=S):
    return [
        (x, y + amplitude * math.sin(x / size * 2 * math.pi * 2 + phase))
        for x in range(0, size + 1, 8)
    ]


def draw_waves(draw, size, color, width, base_y, amplitude, count=3, gap=0.06):
    for index in range(count):
        draw.line(
            wave_points(size * (base_y + index * gap), size * amplitude, index * 1.1, size),
            fill=color,
            width=width,
            joint="curve",
        )


def rounded_icon(img, size, radius_ratio=0.22):
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        [0, 0, size - 1, size - 1], radius=int(size * radius_ratio), fill=255
    )
    img.putalpha(mask)
    return img


def base_icon():
    img = gradient(S, (14, 110, 150), DEEP)
    draw = ImageDraw.Draw(img, "RGBA")
    draw_waves(draw, S, (*MINT, 80), int(S * 0.009), 0.88, 0.010, count=3)
    draw_waves(draw, S, (*WHITE, 40), int(S * 0.006), 0.92, 0.008, count=2)
    return img


def concept_chorito():
    img = base_icon()
    draw = ImageDraw.Draw(img, "RGBA")

    shadow = chorito_points(S, scale=0.98, offset=(0.012, 0.014))
    draw.polygon(shadow, fill=(*DEEP, 200))

    shell = chorito_points(S)
    draw.polygon(shell, fill=(*WHITE, 255))

    for factor in (0.80, 0.62, 0.44):
        inner = chorito_points(S, scale=factor)
        draw.line(inner + [inner[0]], fill=(*OCEAN, 190), width=int(S * 0.006), joint="curve")

    # Umbo: pequeño nudo en el extremo del ligamento
    hx, hy = rotate_norm(0.46, 0.18, -22)
    draw.ellipse(
        (hx * S - S * 0.028, hy * S - S * 0.028, hx * S + S * 0.028, hy * S + S * 0.028),
        fill=(*KELP, 255),
    )
    return rounded_icon(img, S)


def concept_chorito_grid():
    img = base_icon()
    draw = ImageDraw.Draw(img, "RGBA")
    shell = chorito_points(S)
    mask = Image.new("L", (S, S), 0)
    ImageDraw.Draw(mask).polygon(shell, fill=255)

    grid = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    grid_draw = ImageDraw.Draw(grid)
    dot = S * 0.013
    spacing = S * 0.07
    start_x = S * 0.34
    start_y = S * 0.32
    for row in range(6):
        for col in range(5):
            x = start_x + col * spacing
            y = start_y + row * spacing
            grid_draw.ellipse((x - dot, y - dot, x + dot, y + dot), fill=(*MINT, 255))
    grid.putalpha(ImageChops.multiply(grid.getchannel("A"), mask))
    img = Image.alpha_composite(img, grid)

    draw = ImageDraw.Draw(img, "RGBA")
    draw.line(shell + [shell[0]], fill=(*WHITE, 230), width=int(S * 0.010), joint="curve")
    hx, hy = rotate_norm(0.46, 0.18, -22)
    draw.ellipse(
        (hx * S - S * 0.024, hy * S - S * 0.024, hx * S + S * 0.024, hy * S + S * 0.024),
        fill=(*SAND, 255),
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
        "Propuestas con motivo de chorito · valva alargada, líneas de crecimiento y biso",
        font=font(28, bold=False),
        fill=MUTED,
    )

    labels = [
        ("1B · Chorito", "Valva alargada con líneas de crecimiento y biso. La más específica."),
        ("2 · Monograma A", "A geométrica con gota. Alternativa corporativa."),
        ("3 · Onda y muestra", "Oleaje y punto de muestreo. Minimalista."),
        ("4B · Chorito retícula", "Matriz de calibre dentro de la valva del chorito."),
    ]
    positions = [(80, 240), (940, 240), (80, 1180), (940, 1180)]
    card_w, card_h = 780, 860
    for index, (icon, (title, description)) in enumerate(zip(icons, labels)):
        x, y = positions[index]
        draw.rounded_rectangle(
            [x, y, x + card_w, y + card_h], radius=34, fill=WHITE,
            outline=(220, 232, 239), width=2,
        )
        icon_size = 560
        resized = icon.resize((icon_size, icon_size), Image.LANCZOS)
        sheet.paste(resized, (x + (card_w - icon_size) // 2, y + 48), resized)
        draw.text((x + 40, y + 650), title, font=font(38), fill=INK)
        draw.text((x + 40, y + 706), description, font=font(25, bold=False), fill=MUTED)

    draw.text(
        (80, 2100),
        "Paleta: océano #0A577A · kelp #1F7A57 · arena #FFD48A · fondo #F6FAFC",
        font=font(24, bold=False),
        fill=MUTED,
    )
    sheet.save(OUT / "propuestas-v2.png")


def make_lockup(icon):
    width, height = 1700, 460
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    icon_size = 360
    resized = icon.resize((icon_size, icon_size), Image.LANCZOS)
    img.paste(resized, (60, 50), resized)
    draw = ImageDraw.Draw(img)
    draw.text((480, 105), "AquaSample", font=font(132), fill=OCEAN)
    draw.text((486, 260), "Muestreo de choritos", font=font(38, bold=False), fill=KELP)
    img.save(OUT / "lockup-chorito.png")


def main():
    icons = [
        resize_icon(concept_chorito(), "propuesta-1b-chorito"),
        resize_icon(concept_chorito(), "propuesta-2-chorito"),
        resize_icon(concept_waves(), "propuesta-3-onda"),
        resize_icon(concept_chorito_grid(), "propuesta-4b-chorito-reticula"),
    ]
    # La segunda tarjeta usa el monograma anterior para comparar.
    from generate_logo_proposals import concept_monogram as monogram
    icons[1] = resize_icon(monogram(), "propuesta-2-monograma")
    make_sheet(icons)
    make_lockup(icons[0])
    print("OK:", OUT)


if __name__ == "__main__":
    main()
