param(
    [Parameter(Mandatory = $false)]
    [string] $StudentTicket = "1$([char]0x0411)$([char]0x0410)$([char]0x0421)25001",

    [string] $OutDir = "certs/smarthome-chain",

    [string] $P12Name = "mtuci-rpbo-api-chain.p12",

    [string] $P12Password = "change-me-strong",

    [string] $KeyAlias = "mtuci-rpbo-leaf-key"
)

$ErrorActionPreference = "Stop"

function Resolve-OpenSsl {
    $candidates = @(
        "openssl",
        "C:\Program Files\Git\usr\bin\openssl.exe",
        "C:\Program Files (x86)\Git\usr\bin\openssl.exe"
    )
    foreach ($c in $candidates) {
        if ($c -eq "openssl") {
            $cmd = Get-Command openssl -ErrorAction SilentlyContinue
            if ($cmd) { return $cmd.Source }
        } elseif (Test-Path $c) {
            return $c
        }
    }
    throw "OpenSSL not found. Install Git for Windows (includes openssl) or add openssl to PATH."
}

function Get-LatinSlugForCn([string] $raw) {
    $pairs = @(
        @(0x0410, 'A'), @(0x0411, 'B'), @(0x0412, 'V'), @(0x0413, 'G'), @(0x0414, 'D'), @(0x0415, 'E'), @(0x0401, 'E')
        @(0x0416, 'Zh'), @(0x0417, 'Z'), @(0x0418, 'I'), @(0x0419, 'I'), @(0x041A, 'K'), @(0x041B, 'L'), @(0x041C, 'M')
        @(0x041D, 'N'), @(0x041E, 'O'), @(0x041F, 'P'), @(0x0420, 'R'), @(0x0421, 'S'), @(0x0422, 'T'), @(0x0423, 'U')
        @(0x0424, 'F'), @(0x0425, 'Kh'), @(0x0426, 'Ts'), @(0x0427, 'Ch'), @(0x0428, 'Sh'), @(0x0429, 'Shch')
        @(0x042B, 'Y'), @(0x042D, 'E'), @(0x042E, 'Yu'), @(0x042F, 'Ya')
    )
    $map = @{}
    foreach ($p in $pairs) {
        $map[[char][int]$p[0]] = [string]$p[1]
    }
    $sb = [System.Text.StringBuilder]::new()
    foreach ($ch in $raw.ToCharArray()) {
        if ($map.ContainsKey($ch)) {
            [void]$sb.Append($map[$ch])
        } elseif ($ch -ge 'a' -and $ch -le 'z') {
            [void]$sb.Append($ch)
        } elseif ($ch -ge 'A' -and $ch -le 'Z') {
            [void]$sb.Append($ch)
        } elseif ($ch -ge '0' -and $ch -le '9') {
            [void]$sb.Append($ch)
        } elseif ($ch -eq '-') {
            [void]$sb.Append('-')
        }
    }
    $out = $sb.ToString()
    if ([string]::IsNullOrWhiteSpace($out)) {
        throw "Cannot build Latin CN suffix from ticket. Use digits/Latin or Cyrillic letters from student ID."
    }
    return $out
}

function Write-Utf8NoBom([string] $Path, [string] $Content) {
    $enc = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($Path, $Content, $enc)
}

function New-RootCnfMerged([string] $path, [string] $cn, [string] $ouValue, [string] $extPath) {
    $ext = [System.IO.File]::ReadAllText($extPath)
    $lines = @(
        "[ req ]",
        "distinguished_name = req_dn",
        "prompt = no",
        "utf8 = yes",
        "x509_extensions = v3_root",
        "",
        "[ req_dn ]",
        "CN = $cn",
        "O = MTUCI",
        "OU = $ouValue",
        "ST = Moscow",
        "L = Moscow",
        "C = RU",
        "",
        $ext.TrimEnd()
    )
    Write-Utf8NoBom $path ($lines -join "`n")
}

function New-ReqCnf([string] $path, [string] $cn, [string] $ouValue) {
    $lines = @(
        "[ req ]",
        "distinguished_name = req_dn",
        "prompt = no",
        "utf8 = yes",
        "",
        "[ req_dn ]",
        "CN = $cn",
        "O = MTUCI",
        "OU = $ouValue",
        "ST = Moscow",
        "L = Moscow",
        "C = RU",
        ""
    )
    Write-Utf8NoBom $path ($lines -join "`n")
}

function New-SignExtCnf([string] $path, [string] $extPath) {
    $ext = [System.IO.File]::ReadAllText($extPath)
    Write-Utf8NoBom $path $ext.TrimEnd()
}

$openssl = Resolve-OpenSsl
Write-Host "Using OpenSSL: $openssl"

$displayTicket = $StudentTicket.Trim()
if ([string]::IsNullOrWhiteSpace($displayTicket)) {
    throw "StudentTicket is empty."
}

$cnSlug = Get-LatinSlugForCn $displayTicket
$ouValue = "StudentTicket-$displayTicket"

$rootCn = "MTUCI-RPBO-TrustAnchor-$cnSlug"
$midCn = "MTUCI-RPBO-SigningBridge-$cnSlug"
$leafCn = "MTUCI-RPBO-EndpointFacet-$cnSlug"

$base = Join-Path (Get-Location) $OutDir
New-Item -ItemType Directory -Force -Path $base | Out-Null

$rootKey = Join-Path $base "01-root.key"
$rootCrt = Join-Path $base "01-root.crt"
$rootCnfMerged = Join-Path $base "01-root-full.cnf"
$midKey = Join-Path $base "02-intermediate.key"
$midCsr = Join-Path $base "02-intermediate.csr"
$midCrt = Join-Path $base "02-intermediate.crt"
$midCnf = Join-Path $base "02-intermediate-req.cnf"
$midSignExt = Join-Path $base "02-intermediate-sign.ext.cnf"
$leafKey = Join-Path $base "03-server.key"
$leafCsr = Join-Path $base "03-server.csr"
$leafCrt = Join-Path $base "03-server.crt"
$leafCnf = Join-Path $base "03-server-req.cnf"
$leafSignExt = Join-Path $base "03-server-sign.ext.cnf"
$chainPem = Join-Path $base "chain-intermediate-root.pem"
$p12Path = Join-Path $base $P12Name

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$extRoot = Join-Path $scriptDir "tls\root-anchor.ext"
$extMid = Join-Path $scriptDir "tls\intermediate-bridge.ext"
$extLeaf = Join-Path $scriptDir "tls\api-leaf.ext"

Write-Host "OU in all certs (UTF-8): $ouValue"
Write-Host "CN use Latin slug: $cnSlug (from ticket: $displayTicket)"
Write-Host "Output directory: $base"

New-RootCnfMerged $rootCnfMerged $rootCn $ouValue $extRoot
New-ReqCnf $midCnf $midCn $ouValue
New-ReqCnf $leafCnf $leafCn $ouValue
New-SignExtCnf $midSignExt $extMid
New-SignExtCnf $leafSignExt $extLeaf

Write-Host "Generating 3-level chain (Root -> Intermediate -> Server)..."

& $openssl genrsa -out $rootKey 4096 2>$null
& $openssl req -x509 -new -nodes -key $rootKey -sha384 -days 3650 -out $rootCrt -config $rootCnfMerged
if (-not (Test-Path $rootCrt)) {
    throw "Root certificate was not created. Check OpenSSL output above."
}

& $openssl genrsa -out $midKey 4096 2>$null
& $openssl req -new -key $midKey -out $midCsr -config $midCnf
& $openssl x509 -req -in $midCsr -CA $rootCrt -CAkey $rootKey -CAcreateserial -out $midCrt -days 1825 -sha384 -extensions v3_intermediate -extfile $midSignExt
if (-not (Test-Path $midCrt)) {
    throw "Intermediate certificate was not created."
}

& $openssl genrsa -out $leafKey 2048 2>$null
& $openssl req -new -key $leafKey -out $leafCsr -config $leafCnf
& $openssl x509 -req -in $leafCsr -CA $midCrt -CAkey $midKey -CAcreateserial -out $leafCrt -days 825 -sha384 -extensions v3_server -extfile $leafSignExt
if (-not (Test-Path $leafCrt)) {
    throw "Server certificate was not created."
}

Get-Content $midCrt | Set-Content -Path $chainPem -Encoding ascii
Get-Content $rootCrt | Add-Content -Path $chainPem -Encoding ascii

& $openssl pkcs12 -export -out $p12Path -inkey $leafKey -in $leafCrt -certfile $chainPem -name $KeyAlias -password pass:$P12Password

Write-Host ""
Write-Host "Done."
Write-Host "Artifacts: $base"
Write-Host "PKCS12: $p12Path"
Write-Host ""
Write-Host "Environment (new terminal after setx):"
$rel = $OutDir.Replace("\", "/") + "/" + $P12Name
Write-Host ('  setx SSL_KEY_STORE "file:./{0}"' -f $rel)
Write-Host ('  setx SSL_KEY_STORE_PASSWORD "{0}"' -f $P12Password)
Write-Host ('  setx SSL_KEY_ALIAS "{0}"' -f $KeyAlias)
Write-Host ""
Write-Host "Trust root for green lock: import 01-root.crt -> Trusted Root Certification Authorities (local machine demo only)."
Write-Host "Browser: https://localhost:8443 -> lock -> Certificate -> Certification path (3 levels)."
