import xml.etree.ElementTree as ET

root = ET.parse(r"app/build/reports/coverage/test/debug/report.xml").getroot()


def pct(counter):
    if counter is None:
        return "n/a"
    missed = int(counter.get("missed"))
    covered = int(counter.get("covered"))
    total = covered + missed
    return f"{covered}/{total} ({100 * covered / total:.1f}%)"


print("TOTAL lineas:", pct(root.find("counter[@type='LINE']")))
print("TOTAL instrucciones:", pct(root.find("counter[@type='INSTRUCTION']")))

targets = [
    "SampleValidator",
    "HistoryFilter",
    "LoginValidator",
    "SampleUiState",
    "SampleSummary",
    "MuestraRepository",
    "SampleStatus",
    "CountMark",
]
for cls in root.iter("class"):
    name = cls.get("name", "")
    if any(target in name for target in targets):
        line_counter = cls.find("counter[@type='LINE']")
        print(f"{name}: {pct(line_counter)}")
