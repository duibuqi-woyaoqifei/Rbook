# generate-keystore.ps1
# 用途：本地生成一个可用于 CI 签名的 Android keystore，并输出三个可直接
#       粘贴进 GitHub Actions Secrets 的值（SIGNING_KEY / KEY_STORE_PASSWORD / KEY_PASSWORD）。
# 用法：  powershell -ExecutionPolicy Bypass -File scripts\generate-keystore.ps1
# 注意： 本脚本只在本机生成/打印信息，不会把 keystore 或密码提交到仓库。

param(
    [string]$Alias = "key0",
    [int]$ValidityDays = 10000
)

$ErrorActionPreference = "Stop"

# 1) 收集密码（输入时隐藏回显）
$storePass = Read-Host -AsSecureString "请输入 Keystore 密码 (Store Password)"
$keyPass   = Read-Host -AsSecureString "请输入 Key 密码 (Key Password, 可与 Store 密码相同)"
$storePassPlain = [Runtime.InteropServices.Marshal]::PtrToStringBSTR(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($storePass))
$keyPassPlain   = [Runtime.InteropServices.Marshal]::PtrToStringBSTR(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($keyPass))
if ([string]::IsNullOrEmpty($storePassPlain) -or [string]::IsNullOrEmpty($keyPassPlain)) {
    throw "密码不能为空！"
}

# 2) 生成 keystore 文件
$ksFile = Join-Path $PSScriptRoot "release.jks"
if (Test-Path $ksFile) { Remove-Item $ksFile -Force }
& keytool -genkeypair -v `
    -keystore $ksFile `
    -alias $Alias `
    -keyalg RSA -keysize 2048 -validity $ValidityDays `
    -storepass $storePassPlain `
    -keypass $keyPassPlain `
    -dname "CN=Rbook, OU=Rbook, O=Rbook, L=City, ST=State, C=CN"
if ($LASTEXITCODE -ne 0) { throw "keytool 生成 keystore 失败 (exit=$LASTEXITCODE)" }

# 3) 生成单行 base64（供 SIGNING_KEY 使用）
$base64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($ksFile))

Write-Host ""
Write-Host "==================== 请完整复制以下内容到 GitHub Secrets ====================" -ForegroundColor Cyan
Write-Host ""
Write-Host "SIGNING_KEY:" -ForegroundColor Yellow
Write-Host $base64
Write-Host ""
Write-Host "KEY_STORE_PASSWORD:" -ForegroundColor Yellow
Write-Host $storePassPlain
Write-Host ""
Write-Host "KEY_PASSWORD:" -ForegroundColor Yellow
Write-Host $keyPassPlain
Write-Host ""
Write-Host "keytool alias (若不同于默认 key0，请在 release.yml 的 --ks-key-alias 中同步):" -ForegroundColor Yellow
Write-Host $Alias
Write-Host ""
Write-Host "==================== 保存后请妥善保管，勿提交到仓库 ====================" -ForegroundColor Cyan

# 清理内存中的明文密码
$storePassPlain = $null
$keyPassPlain = $null
