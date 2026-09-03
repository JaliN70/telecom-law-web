# 從 MOJ HTML 解析法條並產生 js/data.js
$ErrorActionPreference = "Stop"
$Root = Split-Path (Split-Path $MyInvocation.MyCommand.Path -Parent) -Parent
$Src = Join-Path $Root "scripts\law-K0060040-source.html"
$Out = Join-Path $Root "js\data.js"

$html = Get-Content $Src -Raw -Encoding UTF8

function Escape-Js([string]$s) {
    if (-null -eq $s) { return "" }
    return ($s -replace '\\', '\\\\' -replace "'", "\'" -replace "`r", "" -replace "`n", "\n")
}

function Short-Label([string]$t) {
    $t = ($t -replace '\s+', ' ').Trim()
    if ($t.Length -le 32) { return $t }
    return $t.Substring(0, 32) + "…"
}

$articles = @()
$rowPat = [regex]'flno=(\d+)"[^>]*>第 \d+ 條</a></div><div class="col-data"><div class="law-article">\s*(.*?)</div>\s*</div></div>'
foreach ($m in $rowPat.Matches($html)) {
    $flno = $m.Groups[1].Value
    $body = $m.Groups[2].Value -replace '<[^>]+>', '' -replace '\s+', ' '
    $body = $body.Trim()
    $articles += [pscustomobject]@{ art = $flno; label = (Short-Label $body); body = $body }
}

$files = @()
$filePat = [regex]'title="下載：([^"]+)" href="LawGetFile\.ashx\?([^"]+)"'
foreach ($m in $filePat.Matches($html)) {
    $name = $m.Groups[1].Value
    if ($name -notmatch '\.PDF$') { continue }
    $files += [pscustomobject]@{
        label = ($name -replace '\.PDF$', '')
        url   = "https://law.moj.gov.tw/LawClass/LawGetFile.ashx?$($m.Groups[2].Value)"
    }
}

$artMap = @{}
foreach ($a in $articles) { $artMap[$a.art] = $a }

function Art([string]$n) {
    $a = $artMap[$n]
    if (-not $a) { throw "Missing article $n" }
    return $a
}

$categories = @(
    @{
        id = 'general'; icon = '📋'; title = '通則'; subtitle = '第 1–3 條'
        desc = '立法依據、適用範圍及用詞定義'
        articles = @('1','2','3') | ForEach-Object { Art $_ }
    },
    @{
        id = 'duty'; icon = '📡'; title = '設置義務與責任分界'; subtitle = '第 4–7 條'
        desc = '屋內外電信設備設置義務、責任分界點及維護責任'
        articles = @('4','5','6','7') | ForEach-Object { Art $_ }
    },
    @{
        id = 'equipment'; icon = '🏢'; title = '電信設備與電信室'; subtitle = '第 8–9 條'
        desc = '應設置之電信設備、光纜引進及電信室設置條件'
        articles = @('8','9') | ForEach-Object { Art $_ }
    },
    @{
        id = 'design'; icon = '📐'; title = '設計與檢測'; subtitle = '第 10 條'
        desc = '工程技術規範、設計簽證及屋外管線'
        articles = @('10') | ForEach-Object { Art $_ }
    },
    @{
        id = 'review'; icon = '📝'; title = '洽辦審查審驗'; subtitle = '第 11–13 條'
        desc = '洽辦、設計審查、竣工審驗及電信服務申請'
        articles = @('11','12','13') | ForEach-Object { Art $_ }
    },
    @{
        id = 'operation'; icon = '⚙️'; title = '使用維護與管理'; subtitle = '第 14–21 條'
        desc = '資料保存、施工維護、集線室及竣工圖移交'
        articles = @('14','15','16','17','18','19','20','21') | ForEach-Object { Art $_ }
    },
    @{
        id = 'supplement'; icon = '📎'; title = '附則'; subtitle = '第 22 條'
        desc = '施行日期及過渡規定'
        articles = @('22') | ForEach-Object { Art $_ }
    }
)

$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine("/**")
[void]$sb.AppendLine(" * 建築物電信設備及空間設置使用管理規則 — 導覽資料")
[void]$sb.AppendLine(" * pcode: K0060040")
[void]$sb.AppendLine(" */")
[void]$sb.AppendLine("const LAW = {")
[void]$sb.AppendLine("  pcode: 'K0060040',")
[void]$sb.AppendLine("  name: '建築物電信設備及空間設置使用管理規則',")
[void]$sb.AppendLine("  shortName: '屋內外電信設備規則',")
[void]$sb.AppendLine("  amended: '民國 104 年 08 月 05 日',")
[void]$sb.AppendLine("  fullUrl: 'https://law.moj.gov.tw/LawClass/LawAll.aspx?pcode=K0060040',")
[void]$sb.AppendLine("  updated: '2026/09/03',")
[void]$sb.AppendLine("  author: 'AJ',")
[void]$sb.AppendLine("};")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("function mojArticleUrl(article) {")
[void]$sb.AppendLine("  const flno = String(article).replace(/\s+/g, '');")
[void]$sb.AppendLine("  return `https://law.moj.gov.tw/LawClass/LawSingle.aspx?pcode=${LAW.pcode}&flno=${encodeURIComponent(flno)}`;")
[void]$sb.AppendLine("}")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("const ATTACHMENTS = [")
for ($i = 0; $i -lt $files.Count; $i++) {
    $f = $files[$i]
    $comma = if ($i -lt $files.Count - 1) { ',' } else { '' }
    [void]$sb.AppendLine("  { label: '$((Escape-Js $f.label))', url: '$($f.url)' }$comma")
}
[void]$sb.AppendLine("];")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("const CATEGORIES = [")

for ($ci = 0; $ci -lt $categories.Count; $ci++) {
    $cat = $categories[$ci]
    [void]$sb.AppendLine("  {")
    [void]$sb.AppendLine("    id: '$($cat.id)',")
    [void]$sb.AppendLine("    icon: '$($cat.icon)',")
    [void]$sb.AppendLine("    title: '$((Escape-Js $cat.title))',")
    [void]$sb.AppendLine("    subtitle: '$((Escape-Js $cat.subtitle))',")
    [void]$sb.AppendLine("    desc: '$((Escape-Js $cat.desc))',")
    [void]$sb.AppendLine("    articles: [")
    for ($ai = 0; $ai -lt $cat.articles.Count; $ai++) {
        $a = $cat.articles[$ai]
        $note = ""
        if ($a.art -eq '6') { $note = ", note: '含附圖一～四'" }
        elseif ($a.art -eq '9') { $note = ", note: '含附件一'" }
        elseif ($a.art -in @('11','12')) { $note = ", note: '含附件'" }
        $comma = if ($ai -lt $cat.articles.Count - 1) { ',' } else { '' }
        [void]$sb.AppendLine("      { art: '$($a.art)', label: '$((Escape-Js $a.label))'$note }$comma")
    }
    [void]$sb.AppendLine("    ],")
    [void]$sb.AppendLine("  }$(if ($ci -lt $categories.Count - 1) { ',' } else { '' })")
}
[void]$sb.AppendLine("];")

New-Item -ItemType Directory -Force -Path (Split-Path $Out) | Out-Null
[System.IO.File]::WriteAllText($Out, $sb.ToString(), [System.Text.UTF8Encoding]::new($false))
Write-Host "Wrote $Out ($($articles.Count) articles, $($files.Count) attachments)"
