"""Variante de chorito abierto (dos valvas) para el logo."""

from pathlib import Path

from PIL import Image, ImageDraw

from generate_logo_chorito import (
    BASE, OUT, S, OCEAN, DEEP, KELP, MINT, SAND, WHITE, font, gradient,
    draw_waves, cubic, rounded_icon, resize_icon, concept_chorito_grid,
)

from generate_logo_proposals import concept_monogram as monogram_icon
from generate_logo_chorito import concept_waves


def valve_points(size, right=True, scale=1.0):
    hinge = (0.50, 0.20)
    sign = 1 if right else -1

    def p(x, y):
        return (
            (0.50 + sign * (x - 0.50) * scale) * size,
            (0.20 + (y - 0.20) * scale) * size,
        )

    start = p(0.50, 0.20)
    tip = p(0.24, 0.60)
    bottom = p(0.48, 0.86)
    seg1 = cubic(start, p(0.34, 0.24), p(0.22, 0.44), tip)
    seg2 = cubic(tip, p(0.26, 0.76), p(0.36, 0.87), bottom)
    seg3 = cubic(bottom, p(0.45, 0.70), p(0.48, 0.40), start)
    return [start] + seg1 + seg2 + seg3


def concept_pair():
    img = gradient(S, (14, 110, 150), DEEP)
    draw = ImageDraw.Draw(img, "RGBA")
    draw_waves(draw, S, (*MINT, 75), int(S * 0.009), 0.90, 0.010, count=3)

    left = valve_points(S, right=False)
    right = valve_points(S, right=True)
    draw.polygon(left, fill=(220, 240, 247, 255))
    draw.line(left + [left[0]], fill=(*OCEAN, 160), width=int(S * 0.006), joint="curve")
    draw.polygon(right, fill=(*WHITE, 255))
    draw.line(right + [right[0]], fill=(*OCEAN, 200), width=int(S * 0.007), joint="curve")

    for factor in (0.78, 0.58):
        for right_side in (False, True):
            inner = valve_points(S, right=right_side, scale=factor)
            draw.line(inner + [inner[0]], fill=(*OCEAN, 120), width=int(S * 0.005), joint="curve")

    draw.ellipse((S * 0.465, S * 0.175, S * 0.535, S * 0.245), fill=(*KELP, 255))
    draw.line([(S * 0.44, S * 0.21), (S * 0.56, S * 0.21)], fill=(*SAND, 255), width=int(S * 0.012))

    for start_x, mid_x, end_x in ((0.44, 0.39, 0.37), (0.50, 0.49, 0.48), (0.56, 0.58, 0.60)):
        draw.line(
            [(S * start_x, S * 0.86), (S * mid_x, S * 0.93), (S * end_x, S * 0.98)],
            fill=(*SAND, 255),
            width=int(S * 0.009),
            joint="curve",
        )

    return rounded_icon(img, S)


def make_sheet(icons):
    width, height = 1800, 2180
    sheet = Image.new("RGB", (width, height), (246, 250, 252))
    draw = ImageDraw.Draw(sheet)
    draw.text((80, 64), "AquaSample", font=font(78), fill=OCEAN)
    draw.text(
        (82, 158),
        "Propuestas con motivo de chorito · valvas, umbo y biso",
        font=font(28, bold=False),
        fill=(91, 107, 122),
    )

    labels = [
        ("1C · Chorito abierto", "Dos valvas con umbo y biso. La lectura de chorito más clara."),
        ("2 · Monograma A", "A geométrica con gota. Alternativa corporativa."),
        ("3 · Onda y muestra", "Oleaje y punto de muestreo. Minimalista."),
        ("4B · Chorito retícula", "Matriz de calibre dentro de la valva."),
    ]
    positions = [(80, 240), (940, 240), (80, 1180), (940, 1180)]
    card_w, card_h = 780, 860
    for index, (icon, (title, description)) in enumerate(zip(icons, labels)):
        x, y = positions[index]
        draw.rounded_rectangle(
            [x, y, x + card_w, y + card_h], radius=34, fill=(255, 255, 255),
            outline=(220, 232, 239), width=2,
        )
        icon_size = 560
        resized = icon.resize((icon_size, icon_size), Image.LANCZOS)
        sheet.paste(resized, (x + (card_w - icon_size) // 2, y + 48), resized)
        draw.text((x + 40, y + 650), title, font=font(38), fill=(11, 27, 35))
        draw.text((x + 40, y + 706), description, font=font(25, bold=False), fill=(91, 107, 122))

    draw.text(
        (80, 2100),
        "Paleta: océano #0A577A · kelp #1F7A57 · arena #FFD48A · fondo #F6FAFC",
        font=font(24, bold=False), fill=(91, 107, 122),
    )
    sheet.save(OUT / "propuestas-v3.png")


def make_lockup(icon):
    width, height = 1700, 460
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    size = 360
    resized = icon.resize((size, size), Image.LANCZOS)
    img.paste(resized, (60, 50), resized)
    draw = ImageDraw.Draw(img)
    draw.text((480, 105), "AquaSample", font=font(132), fill=OCEAN)
    draw.text((486, 260), "Muestreo de choritos", font=font(38, bold=False), fill=KELP)
    img.save(OUT / "lockup-chorito.png")


def main():
    pair = resize_icon(concept_pair(), "propuesta-1c-chorito-abierto")
    monogram = resize_icon(monogram_icon(), "propuesta-2-monograma")
    waves = resize_icon(concept_waves(), "propuesta-3-onda")
    grid = resize_icon(concept_chorito_grid(), "propuesta-4b-chorito-reticula")
    make_sheet([pair, monogram, waves, grid])
    make_lockup(pair)
    print("OK:", OUT)


if __name__ == "__main__":
    main()
