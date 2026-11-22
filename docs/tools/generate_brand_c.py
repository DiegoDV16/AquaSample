"""Genera el logo de AquaSample inspirado en la opción C: planilla, lupa, valva y gota."""

import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

BASE = Path(r"C:\Users\diego\OneDrive\Documents\GitHub\AquaSample")
OUT = BASE / "docs" / "logos"
RES = BASE / "app" / "src" / "main" / "res"
OUT.mkdir(parents=True, exist_ok=True)

S = 2048
OCEAN = (10, 87, 122)
DEEP = (6, 50, 70)
CYAN = (42, 167, 201)
LIGHT_CYAN = (111, 201, 238)
KELP = (31, 122, 87)
MINT = (127, 211, 172)
SAND = (255, 212, 138)
WHITE = (255, 255, 255)
PAPER = (247, 251, 253)
NAVY = (8, 38, 53)

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


def cubic(p0, p1, p2, p3, steps=80):
    points = []
    for index in range(1, steps + 1):
        t = index / steps
        mt = 1 - t
        x = mt**3 * p0[0] + 3 * mt**2 * t * p1[0] + 3 * mt * t**2 * p2[0] + t**3 * p3[0]
        y = mt**3 * p0[1] + 3 * mt**2 * t * p1[1] + 3 * mt * t**2 * p2[1] + t**3 * p3[1]
        points.append((x, y))
    return points


def shell_points(cx, cy, width, height, angle=-18):
    local = cubic((0, -0.50), (0.40, -0.18), (0.34, 0.28), (0, 0.50))
    local += cubic((0, 0.50), (-0.34, 0.28), (-0.40, -0.18), (0, -0.50))
    rad = math.radians(angle)
    result = []
    for x, y in local:
        px, py = x * width, y * height
        result.append(
            (
                (cx + px * math.cos(rad) - py * math.sin(rad)) * S,
                (cy + px * math.sin(rad) + py * math.cos(rad)) * S,
            )
        )
    return result


def gradient_shape(polygon, top, bottom):
    layer = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    grad = gradient(S, top, bottom)
    mask = Image.new("L", (S, S), 0)
    ImageDraw.Draw(mask).polygon(polygon, fill=255)
    layer.paste(grad, (0, 0), mask)
    return layer


def draw_clipboard(draw):
    draw.rounded_rectangle(
        [S * 0.185, S * 0.175, S * 0.705, S * 0.865],
        radius=S * 0.050,
        fill=(0, 0, 0, 55),
    )
    draw.rounded_rectangle(
        [S * 0.170, S * 0.155, S * 0.690, S * 0.845],
        radius=S * 0.050,
        fill=(*PAPER, 255),
        outline=(*NAVY, 255),
        width=int(S * 0.018),
    )

    # Sujetador superior
    draw.rounded_rectangle(
        [S * 0.345, S * 0.095, S * 0.515, S * 0.170],
        radius=S * 0.018,
        fill=(*OCEAN, 255),
        outline=(*NAVY, 255),
        width=int(S * 0.010),
    )
    draw.rounded_rectangle(
        [S * 0.405, S * 0.112, S * 0.455, S * 0.142],
        radius=S * 0.010,
        fill=(*MINT, 255),
    )

    # Líneas de planilla
    draw.rounded_rectangle(
        [S * 0.225, S * 0.225, S * 0.365, S * 0.238], radius=S * 0.006,
        fill=(*LIGHT_CYAN, 255),
    )
    draw.rounded_rectangle(
        [S * 0.225, S * 0.268, S * 0.315, S * 0.280], radius=S * 0.006,
        fill=(*MINT, 255),
    )
    for index in range(3):
        y = 0.330 + index * 0.030
        draw.rounded_rectangle(
            [S * 0.225, S * y, S * 0.245, S * (y + 0.014)],
            radius=S * 0.004, fill=(*CYAN, 255),
        )


def draw_shell(img, draw):
    shell = shell_points(0.405, 0.520, 0.300, 0.390, angle=-18)
    img.alpha_composite(gradient_shape(shell, LIGHT_CYAN, OCEAN))
    draw.line(shell + [shell[0]], fill=(*NAVY, 255), width=int(S * 0.012), joint="curve")

    for factor in (0.72, 0.48):
        inner = shell_points(0.405, 0.520, 0.300 * factor, 0.390 * factor, angle=-18)
        draw.line(inner + [inner[0]], fill=(*NAVY, 130), width=int(S * 0.006), joint="curve")


def draw_lens(draw):
    # Mango
    draw.line(
        [(S * 0.700, S * 0.635), (S * 0.850, S * 0.805)],
        fill=(*NAVY, 255), width=int(S * 0.055),
    )
    draw.ellipse(
        (S * 0.840, S * 0.795, S * 0.885, S * 0.840), fill=(*NAVY, 255)
    )
    draw.ellipse(
        (S * 0.690, S * 0.625, S * 0.735, S * 0.670), fill=(*NAVY, 255)
    )

    cx, cy, radius = 0.600, 0.520, 0.165
    draw.ellipse(
        (S * (cx - radius), S * (cy - radius), S * (cx + radius), S * (cy + radius)),
        fill=(176, 226, 240, 70),
    )
    draw.ellipse(
        (S * (cx - radius), S * (cy - radius), S * (cx + radius), S * (cy + radius)),
        outline=(*NAVY, 255), width=int(S * 0.030),
    )
    draw.arc(
        (S * (cx - radius * 0.78), S * (cy - radius * 0.78),
         S * (cx + radius * 0.78), S * (cy + radius * 0.78)),
        start=200, end=300, fill=(255, 255, 255, 190), width=int(S * 0.012),
    )


def draw_drop(img, draw):
    cx, cy = 0.800, 0.265
    top = (cx, cy - 0.090)
    side_right = cubic(top, (cx + 0.020, cy - 0.020), (cx + 0.060, cy + 0.015), (cx + 0.060, cy + 0.040))
    side_left = cubic((cx - 0.060, cy + 0.040), (cx - 0.060, cy + 0.015), (cx - 0.020, cy - 0.020), top)
    arc = [
        (cx + 0.060 * math.cos(math.radians(a)), cy + 0.040 + 0.060 * math.sin(math.radians(a)))
        for a in range(0, 181, 10)
    ]
    drop = [(top[0] * S, top[1] * S)] + [
        (x * S, y * S) for x, y in side_right + arc + side_left
    ]
    img.alpha_composite(gradient_shape(drop, (180, 235, 250), CYAN))
    draw.line(drop + [drop[0]], fill=(*NAVY, 255), width=int(S * 0.010), joint="curve")
    draw.ellipse(
        (S * 0.772, S * 0.245, S * 0.790, S * 0.275), fill=(255, 255, 255, 210)
    )


def elements_canvas(scale=1.0):
    layer = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    draw = ImageDraw.Draw(layer, "RGBA")
    draw_clipboard(draw)
    draw_shell(layer, draw)
    draw_lens(draw)
    draw_drop(layer, draw)
    if scale != 1.0:
        content = layer.crop(layer.getbbox())
        target = int(S * scale)
        content = content.resize((target, target), Image.LANCZOS)
        layer = Image.new("RGBA", (S, S), (0, 0, 0, 0))
        layer.paste(content, ((S - target) // 2, (S - target) // 2), content)
    return layer


def rounded(img, radius_ratio=0.22):
    mask = Image.new("L", (S, S), 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        [0, 0, S - 1, S - 1], radius=int(S * radius_ratio), fill=255
    )
    img.putalpha(mask)
    return img


def build_icon():
    background = gradient(S, (14, 110, 150), DEEP)
    icon = Image.alpha_composite(background, elements_canvas())
    return rounded(icon)


def build_lockup(icon):
    width, height = 1700, 460
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    size = 360
    resized = icon.resize((size, size), Image.LANCZOS)
    img.paste(resized, (60, 50), resized)
    draw = ImageDraw.Draw(img)
    draw.text((480, 100), "Aqua", font=font(132), fill=OCEAN)
    draw.text((790, 100), "Sample", font=font(132), fill=CYAN)
    draw.text((486, 262), "Muestreo de choritos", font=font(38, bold=False), fill=KELP)
    img.save(OUT / "opcion-c-lockup.png")


def main():
    icon = build_icon()
    icon.resize((1024, 1024), Image.LANCZOS).save(OUT / "opcion-c-icon.png")
    build_lockup(icon.resize((1024, 1024), Image.LANCZOS))

    # Login: icono con esquinas redondeadas sobre fondo transparente
    logo = icon.resize((1024, 1024), Image.LANCZOS)
    (RES / "drawable-nodpi").mkdir(parents=True, exist_ok=True)
    logo.save(RES / "drawable-nodpi" / "ic_aquasample_logo.png")

    # Adaptive foreground: solo elementos, escalados al área segura
    foreground = elements_canvas(scale=0.64).resize((1024, 1024), Image.LANCZOS)
    foreground.save(RES / "drawable-nodpi" / "ic_launcher_foreground.png")

    # Iconos legacy y round
    square = Image.alpha_composite(gradient(S, (14, 110, 150), DEEP), elements_canvas())
    square = square.resize((1024, 1024), Image.LANCZOS)
    for density, px in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)):
        folder = RES / f"mipmap-{density}"
        folder.mkdir(parents=True, exist_ok=True)
        square.resize((px, px), Image.LANCZOS).save(folder / "ic_launcher.webp", "WEBP", quality=95)
        round_mask = Image.new("L", (1024, 1024), 0)
        ImageDraw.Draw(round_mask).ellipse((0, 0, 1023, 1023), fill=255)
        rounded_icon = square.copy()
        rounded_icon.putalpha(round_mask)
        rounded_icon.resize((px, px), Image.LANCZOS).save(
            folder / "ic_launcher_round.webp", "WEBP", quality=95
        )

    print("OK")


if __name__ == "__main__":
    main()
