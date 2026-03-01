package com.bountybugger.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.bountybugger.domain.model.PortResult
import com.bountybugger.domain.model.PortState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import kotlin.math.min

/**
 * Network Scanner - Nmap-style port scanning implementation
 * Provides port scanning, service detection, and network mapping
 */
class NetworkScanner(private val context: Context? = null) {

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _scanResults = MutableStateFlow<List<PortResult>>(emptyList())
    val scanResults: StateFlow<List<PortResult>> = _scanResults.asStateFlow()

    private var isScanning = false
    private var scanJob: Job? = null

    /**
     * Get current device's IP address and network info
     */
    fun getCurrentNetworkInfo(): NetworkInfo? {
        return try {
            context?.let { ctx ->
                var ipAddress: String? = null
                var networkName: String? = null
                var gateway: String? = null
                
                // Try WiFi first
                try {
                    val wifiManager = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    if (wifiInfo != null) {
                        val ip = wifiInfo.ipAddress
                        if (ip != 0) {
                            ipAddress = String.format("%d.%d.%d.%d", 
                                ip and 0xff,
                                ip shr 8 and 0xff,
                                ip shr 16 and 0xff,
                                ip shr 24 and 0xff)
                            
                            networkName = wifiInfo.ssid?.replace("\"", "") ?: "Unknown Network"
                            
                            val gatewayInt = wifiManager.dhcpInfo?.gateway ?: 0
                            if (gatewayInt != 0) {
                                gateway = String.format("%d.%d.%d.%d", 
                                    gatewayInt and 0xff,
                                    gatewayInt shr 8 and 0xff,
                                    gatewayInt shr 16 and 0xff,
                                    gatewayInt shr 24 and 0xff)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // WiFi not available
                }
                
                // If no WiFi IP, try to get from network interfaces
                if (ipAddress == null) {
                    ipAddress = getLocalIpAddress()
                }
                
                // If still no IP, try from active network
                if (ipAddress == null) {
                    try {
                        val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val network = connectivityManager.activeNetwork
                        val capabilities = connectivityManager.getNetworkCapabilities(network)
                        if (capabilities != null) {
                            // Try to get IP from link properties
                            val linkProperties = connectivityManager.getActiveNetwork()?.let { connectivityManager.getNetworkCapabilities(it) }
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
                
                if (ipAddress != null) {
                    val subnet = ipAddress.substringBeforeLast(".")
                    if (gateway == null) {
                        gateway = "$subnet.1"
                    }
                    if (networkName == null) {
                        networkName = "Local Network"
                    }
                    NetworkInfo(
                        ipAddress = ipAddress,
                        networkName = networkName,
                        gateway = gateway,
                        subnet = subnet
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get the local IP address (alternative method)
     */
    fun getLocalIpAddress(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces()?.toList()?.flatMap { networkInterface ->
                networkInterface.inetAddresses.toList()
                    .filter { !it.isLoopbackAddress && it.hostAddress?.contains(":") == false }
                    .map { it.hostAddress }
            }?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            // Try alternative method
            try {
                val process = Runtime.getRuntime().exec("ip addr show wlan0")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val line = reader.readLines().firstOrNull { it.contains("inet ") }
                reader.close()
                line?.let {
                    val match = Regex("inet (\\d+\\.\\d+\\.\\d+\\.\\d+)").find(it)
                    match?.groupValues?.get(1)
                }
            } catch (ex: Exception) {
                null
            }
        }
    }

    data class NetworkInfo(
        val ipAddress: String,
        val networkName: String,
        val gateway: String,
        val subnet: String
    )

    // Common port to service mapping
    private val commonPorts = mapOf(
        20 to ("ftp-data" to "FTP Data"),
        21 to ("ftp" to "FTP"),
        22 to ("ssh" to "SSH"),
        23 to ("telnet" to "Telnet"),
        25 to ("smtp" to "SMTP"),
        53 to ("dns" to "DNS"),
        67 to ("dhcp" to "DHCP Server"),
        68 to ("dhcp" to "DHCP Client"),
        69 to ("tftp" to "TFTP"),
        80 to ("http" to "HTTP"),
        110 to ("pop3" to "POP3"),
        119 to ("nntp" to "NNTP"),
        123 to ("ntp" to "NTP"),
        135 to ("msrpc" to "MS RPC"),
        137 to ("netbios-ns" to "NetBIOS Name"),
        138 to ("netbios-dgm" to "NetBIOS Datagram"),
        139 to ("netbios-ssn" to "NetBIOS Session"),
        143 to ("imap" to "IMAP"),
        161 to ("snmp" to "SNMP"),
        162 to ("snmptrap" to "SNMP Trap"),
        389 to ("ldap" to "LDAP"),
        443 to ("https" to "HTTPS"),
        445 to ("microsoft-ds" to "Microsoft DS"),
        465 to ("smtps" to "SMTPS"),
        514 to ("syslog" to "Syslog"),
        587 to ("submission" to "Mail Submission"),
        636 to ("ldaps" to "LDAPS"),
        993 to ("imaps" to "IMAPS"),
        995 to ("pop3s" to "POP3S"),
        1080 to ("socks" to "SOCKS"),
        1433 to ("mssql" to "MS SQL"),
        1434 to ("mssql UDP" to "MS SQL Monitor"),
        1521 to ("oracle" to "Oracle"),
        1723 to ("pptp" to "PPTP"),
        2049 to ("nfs" to "NFS"),
        2082 to ("cpanel" to "cPanel"),
        2083 to ("cpanel-ssl" to "cPanel SSL"),
        2181 to ("zookeeper" to "ZooKeeper"),
        3306 to ("mysql" to "MySQL"),
        3389 to ("rdp" to "RDP"),
        3690 to ("svn" to "SVN"),
        4000 to ("dev" to "Development"),
        4369 to ("epmd" to "Erlang Port Mapper"),
        5000 to ("upnp" to "UPnP"),
        5060 to ("sip" to "SIP"),
        5432 to ("postgresql" to "PostgreSQL"),
        5672 to ("amqp" to "AMQP"),
        5900 to ("vnc" to "VNC"),
        5985 to ("winrm" to "WinRM HTTP"),
        5986 to ("winrm" to "WinRM HTTPS"),
        6379 to ("redis" to "Redis"),
        6443 to ("k8s" to "Kubernetes API"),
        6667 to ("irc" to "IRC"),
        8000 to ("http-alt" to "HTTP Alt"),
        8080 to ("http-proxy" to "HTTP Proxy"),
        8443 to ("https-alt" to "HTTPS Alt"),
        8888 to ("http-alt" to "HTTP Alt"),
        9000 to ("sonarqube" to "SonarQube"),
        9090 to ("prometheus" to "Prometheus"),
        9200 to ("elasticsearch" to "Elasticsearch"),
        9300 to ("elasticsearch" to "Elasticsearch"),
        11211 to ("memcache" to "Memcached"),
        27017 to ("mongodb" to "MongoDB"),
        27018 to ("mongodb" to "MongoDB"),
        50000 to ("sap" to "SAP")
    )

    /**
     * Common ports list for quick scanning
     */
    private val commonPortsList = listOf(
        21, 22, 23, 25, 53, 80, 110, 135, 139, 143, 443, 445, 993, 995,
        1433, 1521, 3306, 3389, 5432, 5900, 6379, 8080, 8443, 27017
    )

    /**
     * Scan ports on a target host
     * @param target Hostname or IP address
     * @param startPort Starting port
     * @param endPort Ending port
     * @param timeout Connection timeout in milliseconds
     * @param coroutineScope Scope for coroutine execution
     * @param onProgress Callback for progress updates
     */
    suspend fun scanPorts(
        target: String,
        startPort: Int = 1,
        endPort: Int = 1024,
        timeout: Int = 1000,
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        onProgress: ((Float, List<PortResult>) -> Unit)? = null
    ): List<PortResult> = withContext(Dispatchers.IO) {
        if (isScanning) {
            return@withContext emptyList()
        }

        isScanning = true
        _scanResults.value = emptyList()
        _scanProgress.value = 0f

        val results = mutableListOf<PortResult>()
        val totalPorts = endPort - startPort + 1
        var scannedPorts = 0

        try {
            // Resolve hostname first
            val address = withContext(Dispatchers.IO) {
                InetAddress.getByName(target)
            }

            val hostAddress = address.hostAddress ?: target

            // Scan ports in batches for better performance
            val batchSize = 50
            val ports = (startPort..endPort).toList()

            for (batch in ports.chunked(batchSize)) {
                if (!isScanning) break

                val batchResults = batch.map { port ->
                    async {
                        scanPort(hostAddress, port, timeout)
                    }
                }.awaitAll()

                results.addAll(batchResults.filterNotNull())
                scannedPorts += batch.size
                _scanProgress.value = scannedPorts.toFloat() / totalPorts
                _scanResults.value = results.toList()
                onProgress?.invoke(_scanProgress.value, results)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isScanning = false
            _scanProgress.value = 1f
        }

        results.sortedBy { it.port }
    }

    /**
     * Quick scan using common ports
     */
    suspend fun quickScan(
        target: String,
        timeout: Int = 1500,
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): List<PortResult> = withContext(Dispatchers.IO) {
        scanPorts(target, 1, 1, timeout, coroutineScope) // This won't work, fix below
        scanCommonPorts(target, timeout, coroutineScope)
    }

    /**
     * Scan common ports only
     */
    private suspend fun scanCommonPorts(
        target: String,
        timeout: Int,
        coroutineScope: CoroutineScope
    ): List<PortResult> = withContext(Dispatchers.IO) {
        isScanning = true
        _scanResults.value = emptyList()
        val results = mutableListOf<PortResult>()
        val totalPorts = commonPortsList.size
        var scannedPorts = 0

        try {
            val address = withContext(Dispatchers.IO) {
                InetAddress.getByName(target)
            }
            val hostAddress = address.hostAddress ?: target

            val batchResults = commonPortsList.map { port ->
                async {
                    scanPort(hostAddress, port, timeout)
                }
            }.awaitAll()

            results.addAll(batchResults.filterNotNull())
            scannedPorts = totalPorts
            _scanProgress.value = 1f
            _scanResults.value = results
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isScanning = false
        }

        results.sortedBy { it.port }
    }

    /**
     * Scan a single port
     */
    private fun scanPort(host: String, port: Int, timeout: Int): PortResult? {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeout)

                // Try to grab banner
                val banner = try {
                    socket.getInputStream().bufferedReader().readLine()
                } catch (e: Exception) {
                    null
                }

                val (service, serviceDesc) = commonPorts[port] ?: ("unknown" to "Unknown")
                val version = banner?.take(50)

                PortResult(
                    port = port,
                    protocol = "tcp",
                    state = PortState.OPEN,
                    service = service,
                    serviceVersion = version,
                    banner = banner
                )
            }
        } catch (e: Exception) {
            // Port is closed or filtered
            null
        }
    }

    /**
     * Detect service version based on banner
     */
    fun detectServiceVersion(banner: String?): String? {
        if (banner == null) return null

        // Simple pattern matching for common services
        return when {
            banner.contains("SSH", ignoreCase = true) -> "SSH"
            banner.contains("FTP", ignoreCase = true) -> "FTP"
            banner.contains("HTTP", ignoreCase = true) -> "HTTP"
            banner.contains("MySQL", ignoreCase = true) -> "MySQL"
            banner.contains("PostgreSQL", ignoreCase = true) -> "PostgreSQL"
            banner.contains("MongoDB", ignoreCase = true) -> "MongoDB"
            banner.contains("Redis", ignoreCase = true) -> "Redis"
            banner.contains("Apache", ignoreCase = true) -> "Apache"
            banner.contains("Nginx", ignoreCase = true) -> "Nginx"
            banner.contains("Microsoft", ignoreCase = true) -> "Microsoft"
            banner.contains("OpenSSH", ignoreCase = true) -> "OpenSSH"
            else -> banner.take(30)
        }
    }

    /**
     * Check if host is alive (ICMP-like ping)
     */
    suspend fun pingHost(target: String, timeout: Int = 3000): Boolean = withContext(Dispatchers.IO) {
        try {
            val address = InetAddress.getByName(target)
            address.isReachable(timeout)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get service name for port
     */
    fun getServiceName(port: Int): Pair<String, String>? = commonPorts[port]

    /**
     * Cancel ongoing scan
     */
    fun cancelScan() {
        isScanning = false
        scanJob?.cancel()
    }

    /**
     * Check if currently scanning
     */
    fun isCurrentlyScanning(): Boolean = isScanning

    /**
     * Scan local subnet for live hosts (common gateway/router IPs)
     */
    suspend fun scanLocalSubnet(timeout: Int = 2000): List<String> = withContext(Dispatchers.IO) {
        val localIp = getLocalIpAddress() ?: getCurrentNetworkInfo()?.ipAddress
        if (localIp == null) {
            return@withContext emptyList()
        }
        
        val subnet = localIp.substringBeforeLast(".")
        
        // Common IP addresses to scan on local networks
        val commonIps = listOf(
            "$subnet.1",   // Common gateway
            "$subnet.254", // Common gateway
            "$subnet.100", // Common gateway
            "$subnet.2",   // Common gateway
            "$subnet.3",
            "$subnet.10",
            "$subnet.50",
            "$subnet.99",
            "$subnet.101",
            "$subnet.110",
            "$subnet.150",
            "$subnet.200",
            "$subnet.253", // Common gateway
            // Also scan the device's own IP
            localIp
        ).distinct()
        
        val liveHosts = mutableListOf<String>()
        
        commonIps.forEach { ip ->
            try {
                val address = InetAddress.getByName(ip)
                if (address.isReachable(timeout)) {
                    liveHosts.add(ip)
                }
            } catch (e: Exception) {
                // Host not reachable
            }
        }
        
        liveHosts
    }
}
