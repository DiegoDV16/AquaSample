param(
    [Parameter(Mandatory = $true)][string]$Key,
    [Parameter(Mandatory = $true)][string]$Token,
    [string]$BoardName = "AquaSample · Semana 16",
    [string]$CsvPath = "docs/trello/tarjetas.csv"
)

$ErrorActionPreference = "Stop"
$base = "https://api.trello.com/1"
$auth = "key=$Key&token=$Token"

if (-not (Test-Path $CsvPath)) {
    throw "No existe $CsvPath"
}

$board = Invoke-RestMethod -Method Post `
    -Uri "$base/boards/?name=$([uri]::EscapeDataString($BoardName))&defaultLists=false&$auth"

$listIds = @{}
foreach ($listName in @("Backlog", "En curso", "En revisión", "Hecho")) {
    $list = Invoke-RestMethod -Method Post `
        -Uri "$base/lists?name=$([uri]::EscapeDataString($listName))&idBoard=$($board.id)&$auth"
    $listIds[$listName] = $list.id
}

$cards = Import-Csv -Path $CsvPath -Delimiter ","
$created = 0
foreach ($card in $cards) {
    $listId = $listIds[$card.Lista]
    if (-not $listId) { $listId = $listIds["Backlog"] }

    $description = "Etiqueta: $($card.Etiqueta)`nResponsable: $($card.Responsable)"
    $item = Invoke-RestMethod -Method Post `
        -Uri "$base/cards?idList=$listId&name=$([uri]::EscapeDataString($card.Tarjeta))&desc=$([uri]::EscapeDataString($description))&$auth"

    $checklistItems = $card.Checklist -split ";" | Where-Object { $_.Trim() -ne "" }
    if ($checklistItems.Count -gt 0) {
        $checklist = Invoke-RestMethod -Method Post `
            -Uri "$base/checklists?idCard=$($item.id)&name=Checklist&$auth"
        foreach ($entry in $checklistItems) {
            Invoke-RestMethod -Method Post `
                -Uri "$base/checklists/$($checklist.id)/checkItems?name=$([uri]::EscapeDataString($entry.Trim()))&$auth" | Out-Null
        }
    }
    $created++
}

Write-Output "Tablero creado: $($board.url)"
Write-Output "Tarjetas creadas: $created"
