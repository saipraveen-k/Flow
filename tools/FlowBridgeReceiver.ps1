param(
    [Parameter(Mandatory = $true)] [string]$Token,
    [int]$Port = 43910,
    [string]$SaveDirectory = "$env:USERPROFILE\Downloads\FlowBridge"
)

$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Force -Path $SaveDirectory | Out-Null
$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Any, $Port)
$listener.Start()
Write-Host "Flow Bridge receiver ready on port $Port. Saving to: $SaveDirectory"

function Read-Exact($stream, [int]$count) {
    $buffer = New-Object byte[] $count; $offset = 0
    while ($offset -lt $count) { $read = $stream.Read($buffer, $offset, $count - $offset); if ($read -le 0) { throw 'Connection closed.' }; $offset += $read }
    return ,$buffer
}
function Read-Int32($stream) { $bytes = Read-Exact $stream 4; [Array]::Reverse($bytes); return [BitConverter]::ToInt32($bytes, 0) }
function Read-Int64($stream) { $bytes = Read-Exact $stream 8; [Array]::Reverse($bytes); return [BitConverter]::ToInt64($bytes, 0) }
function Write-Response($stream, [bool]$success, [string]$message) {
    $messageBytes = [Text.Encoding]::UTF8.GetBytes($message)
    $stream.WriteByte($(if ($success) { 1 } else { 0 }))
    $length = [BitConverter]::GetBytes([System.Net.IPAddress]::HostToNetworkOrder($messageBytes.Length)); $stream.Write($length, 0, 4); $stream.Write($messageBytes, 0, $messageBytes.Length); $stream.Flush()
}

while ($true) {
    $client = $listener.AcceptTcpClient(); $stream = $client.GetStream()
    try {
        $magic = [Text.Encoding]::ASCII.GetString((Read-Exact $stream 8))
        if ($magic -ne "FLOWOS1`0") { throw 'Unsupported Flow Bridge request.' }
        $incomingToken = [Text.Encoding]::UTF8.GetString((Read-Exact $stream (Read-Int32 $stream)))
        if ($incomingToken -cne $Token) { Write-Response $stream $false 'Authentication failed.'; continue }
        $name = [Text.Encoding]::UTF8.GetString((Read-Exact $stream (Read-Int32 $stream)))
        $length = Read-Int64 $stream
        if ([string]::IsNullOrWhiteSpace($name) -and $length -eq 0) { Write-Response $stream $true 'Connected.'; continue }
        if ($length -lt 0 -or $length -gt 2147483648) { throw 'Invalid file size.' }
        $safeName = [IO.Path]::GetFileName($name); $destination = Join-Path $SaveDirectory $safeName
        $output = [IO.File]::Open($destination, [IO.FileMode]::Create)
        try { $remaining = $length; while ($remaining -gt 0) { $chunk = Read-Exact $stream ([Math]::Min(65536, $remaining)); $output.Write($chunk, 0, $chunk.Length); $remaining -= $chunk.Length } } finally { $output.Dispose() }
        Write-Response $stream $true "Received $safeName"
    } catch { try { Write-Response $stream $false $_.Exception.Message } catch {} } finally { $stream.Dispose(); $client.Dispose() }
}
