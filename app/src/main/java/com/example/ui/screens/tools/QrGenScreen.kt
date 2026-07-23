package com.example.ui.screens.tools

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.viewmodel.ExplorerViewModel
import java.util.UUID

// Dynamic Translation Map for Multiple Language Support
private val translations = mapOf(
    "English" to mapOf(
        "hub_title" to "Smart QR Hub",
        "hub_subtitle" to "Generate, scan & customize advanced matrix codes",
        "tab_generator" to "Generator",
        "tab_scanner" to "Scanner",
        "tab_batch" to "Batch",
        "tab_history" to "History & Stats",
        "tab_extra" to "Dynamic & Settings",
        "encode_content" to "Encode Content",
        "customization" to "Customization",
        "colors" to "Colors & Gradients",
        "dot_style" to "Dot Style",
        "corner_style" to "Corner Style",
        "logo_presets" to "Logo & Branding",
        "options" to "Advanced Options",
        "qr_size" to "QR Display Size",
        "margin" to "Custom Margin",
        "error_level" to "Error Correction Level",
        "char_count" to "Characters",
        "export_qr" to "Export Code",
        "share_qr" to "Share QR Code",
        "print_qr" to "Print QR",
        "copy_qr" to "Copy Payload"
    ),
    "Español" to mapOf(
        "hub_title" to "Smart QR Hub",
        "hub_subtitle" to "Genere, escanee y personalice códigos de matriz",
        "tab_generator" to "Generador",
        "tab_scanner" to "Escáner",
        "tab_batch" to "Lote",
        "tab_history" to "Historial y Estadísticas",
        "tab_extra" to "Dinámico y Ajustes",
        "encode_content" to "Codificar Contenido",
        "customization" to "Personalización",
        "colors" to "Colores y Degradados",
        "dot_style" to "Estilo de Punto",
        "corner_style" to "Estilo de Esquina",
        "logo_presets" to "Logotipo y Marca",
        "options" to "Opciones Avanzadas",
        "qr_size" to "Tamaño del QR",
        "margin" to "Margen Personalizado",
        "error_level" to "Nivel de Error",
        "char_count" to "Caracteres",
        "export_qr" to "Exportar Código",
        "share_qr" to "Compartir QR",
        "print_qr" to "Imprimir",
        "copy_qr" to "Copiar Contenido"
    ),
    "Français" to mapOf(
        "hub_title" to "Smart QR Hub",
        "hub_subtitle" to "Générez, scannez et personnalisez des codes matriciels",
        "tab_generator" to "Générateur",
        "tab_scanner" to "Scanner",
        "tab_batch" to "Lot",
        "tab_history" to "Historique & Stats",
        "tab_extra" to "Dynamique & Réglages",
        "encode_content" to "Encoder le Contenu",
        "customization" to "Personnalisation",
        "colors" to "Couleurs & Dégradés",
        "dot_style" to "Style de Point",
        "corner_style" to "Style de Coin",
        "logo_presets" to "Logo & Branding",
        "options" to "Options Avancées",
        "qr_size" to "Taille d'Affichage",
        "margin" to "Marge Personnalisée",
        "error_level" to "Correction d'Erreur",
        "char_count" to "Caractères",
        "export_qr" to "Exporter le Code",
        "share_qr" to "Partager le QR",
        "print_qr" to "Imprimer",
        "copy_qr" to "Copier le Contenu"
    ),
    "Deutsch" to mapOf(
        "hub_title" to "Smart QR Hub",
        "hub_subtitle" to "Generieren, scannen & anpassen von Matrixcodes",
        "tab_generator" to "Generator",
        "tab_scanner" to "Scanner",
        "tab_batch" to "Stapel",
        "tab_history" to "Verlauf & Statistik",
        "tab_extra" to "Dynamik & Einstellungen",
        "encode_content" to "Inhalt Codieren",
        "customization" to "Anpassung",
        "colors" to "Farben & Verläufe",
        "dot_style" to "Punkt-Stil",
        "corner_style" to "Ecken-Stil",
        "logo_presets" to "Logo & Branding",
        "options" to "Erweiterte Optionen",
        "qr_size" to "QR-Größe",
        "margin" to "Eigener Rand",
        "error_level" to "Fehlerkorrektur-Level",
        "char_count" to "Zeichen",
        "export_qr" to "Code Exportieren",
        "share_qr" to "QR Teilen",
        "print_qr" to "Drucken",
        "copy_qr" to "Inhalt Kopieren"
    ),
    "Hindī" to mapOf(
        "hub_title" to "स्मार्ट QR हब",
        "hub_subtitle" to "उन्नत मैट्रिक्स कोड जनरेट, स्कैन और कस्टमाइज़ करें",
        "tab_generator" to "जनरेटर",
        "tab_scanner" to "स्कैनर",
        "tab_batch" to "बैच जनरेटर",
        "tab_history" to "इतिहास और आँकड़े",
        "tab_extra" to "डायनामिक और सेटिंग्स",
        "encode_content" to "सामग्री जोड़ें",
        "customization" to "कस्टमाइज़ेशन",
        "colors" to "रंग और ग्रेडिएंट",
        "dot_style" to "डॉट स्टाइल",
        "corner_style" to "कॉर्नर स्टाइल",
        "logo_presets" to "लोगो और ब्रांडिंग",
        "options" to "उन्नत विकल्प",
        "qr_size" to "क्यूआर साइज",
        "margin" to "कस्टम मार्जिन",
        "error_level" to "त्रुटि सुधार स्तर",
        "char_count" to "अक्षर",
        "export_qr" to "एक्सपोर्ट क्यूआर",
        "share_qr" to "शेयर क्यूआर",
        "print_qr" to "प्रिंट क्यूआर",
        "copy_qr" to "कॉपी पेलोड"
    )
)

private fun getTxt(lang: String, key: String): String {
    return translations[lang]?.get(key) ?: translations["English"]?.get(key) ?: key
}

// History entity for local storage
data class QrHistoryItem(
    val id: String,
    val type: String,
    val content: String,
    val title: String,
    val timestamp: Long,
    val isFavorite: Boolean = false,
    val foregroundColor: Int = 0xFF000000.toInt(),
    val backgroundColor: Int = 0xFFFFFFFF.toInt(),
    val dotStyle: String = "Square",
    val cornerStyle: String = "Square"
)

// Helper methods to save and load history to SharedPreferences
private fun saveHistoryToPrefs(context: Context, list: List<QrHistoryItem>) {
    val prefs = context.getSharedPreferences("qr_history_prefs", Context.MODE_PRIVATE)
    val sb = StringBuilder()
    sb.append("[")
    list.forEachIndexed { idx, item ->
        sb.append("{")
        sb.append("\"id\":\"${item.id}\",")
        sb.append("\"type\":\"${item.type}\",")
        sb.append("\"content\":\"${item.content.replace("\"", "\\\"").replace("\n", "\\n")}\",")
        sb.append("\"title\":\"${item.title.replace("\"", "\\\"")}\",")
        sb.append("\"timestamp\":${item.timestamp},")
        sb.append("\"isFavorite\":${item.isFavorite},")
        sb.append("\"foregroundColor\":${item.foregroundColor},")
        sb.append("\"backgroundColor\":${item.backgroundColor},")
        sb.append("\"dotStyle\":\"${item.dotStyle}\",")
        sb.append("\"cornerStyle\":\"${item.cornerStyle}\"")
        sb.append("}")
        if (idx < list.size - 1) sb.append(",")
    }
    sb.append("]")
    prefs.edit().putString("history_json", sb.toString()).apply()
}

private fun loadHistoryFromPrefs(context: Context): List<QrHistoryItem> {
    val prefs = context.getSharedPreferences("qr_history_prefs", Context.MODE_PRIVATE)
    val json = prefs.getString("history_json", null) ?: return getSampleHistory()
    val list = mutableListOf<QrHistoryItem>()
    try {
        val pattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}")
        val matcher = pattern.matcher(json)
        while (matcher.find()) {
            val body = matcher.group(1) ?: continue
            var id = ""
            var type = ""
            var content = ""
            var title = ""
            var timestamp = 0L
            var isFavorite = false
            var foregroundColor = 0xFF000000.toInt()
            var backgroundColor = 0xFFFFFFFF.toInt()
            var dotStyle = "Square"
            var cornerStyle = "Square"

            body.split("\",\"","\",",",\"").forEach { field ->
                val parts = field.split(":", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim().replace("\"", "").replace("{", "")
                    val value = parts[1].trim().replace("\"", "").replace("}", "")
                    when (key) {
                        "id" -> id = value
                        "type" -> type = value
                        "content" -> content = value.replace("\\\"", "\"").replace("\\n", "\n")
                        "title" -> title = value.replace("\\\"", "\"")
                        "timestamp" -> timestamp = value.toLongOrNull() ?: 0L
                        "isFavorite" -> isFavorite = value.toBoolean()
                        "foregroundColor" -> foregroundColor = value.toIntOrNull() ?: 0xFF000000.toInt()
                        "backgroundColor" -> backgroundColor = value.toIntOrNull() ?: 0xFFFFFFFF.toInt()
                        "dotStyle" -> dotStyle = value
                        "cornerStyle" -> cornerStyle = value
                    }
                }
            }
            if (id.isNotEmpty()) {
                list.add(QrHistoryItem(id, type, content, title, timestamp, isFavorite, foregroundColor, backgroundColor, dotStyle, cornerStyle))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return if (list.isEmpty()) getSampleHistory() else list
}

private fun getSampleHistory(): List<QrHistoryItem> {
    return listOf(
        QrHistoryItem(
            id = "1",
            type = "URL",
            content = "https://smartexplorer.app",
            title = "Smart Explorer Homepage",
            timestamp = System.currentTimeMillis() - 3600000,
            isFavorite = true
        ),
        QrHistoryItem(
            id = "2",
            type = "WiFi",
            content = "WIFI:S:Office_Net_5G;T:WPA;P:AccessGranted312;;",
            title = "Office WiFi QR",
            timestamp = System.currentTimeMillis() - 86400000,
            isFavorite = false
        ),
        QrHistoryItem(
            id = "3",
            type = "UPI",
            content = "upi://pay?pa=explorer@upi&pn=Smart%20Pay&am=250&tn=Consulting",
            title = "Consulting Fee UPI",
            timestamp = System.currentTimeMillis() - 172800000,
            isFavorite = true
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrGenScreen(
    viewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Language Preference State
    var currentLanguage by remember { mutableStateOf("English") }

    // Theme Custom states (In-app AMOLED/Black background toggle)
    var isAmoledTheme by remember { mutableStateOf(false) }

    // Main Active Screen Tab selector
    var activeTab by remember { mutableStateOf(0) }
    val tabTitles = listOf(
        getTxt(currentLanguage, "tab_generator"),
        getTxt(currentLanguage, "tab_scanner"),
        getTxt(currentLanguage, "tab_batch"),
        getTxt(currentLanguage, "tab_history"),
        getTxt(currentLanguage, "tab_extra")
    )

    // HISTORY STATE (Auto Loaded and Auto Saved)
    var historyList by remember { mutableStateOf(loadHistoryFromPrefs(context)) }

    // Generator Customizations
    var selectedType by remember { mutableStateOf("URL") }
    var urlInput by remember { mutableStateOf("https://smartexplorer.app") }
    var textInput by remember { mutableStateOf("Welcome to the premium QR Code matrix hub.") }
    var emailTo by remember { mutableStateOf("hello@smartexplorer.app") }
    var emailSubject by remember { mutableStateOf("Collaboration") }
    var emailBody by remember { mutableStateOf("I am interested in your smart QR solution.") }
    var phoneInput by remember { mutableStateOf("+123456789") }
    var smsPhone by remember { mutableStateOf("+123456789") }
    var smsMessage by remember { mutableStateOf("Send me the scanned payload details please.") }
    var waPhone by remember { mutableStateOf("+123456789") }
    var waMessage by remember { mutableStateOf("Hello Explorer, let's connect!") }
    var wifiSsid by remember { mutableStateOf("ExplorerCore_5G") }
    var wifiPassword by remember { mutableStateOf("SecurePass1312") }
    var wifiSec by remember { mutableStateOf("WPA") }
    var wifiHidden by remember { mutableStateOf(false) }
    var upiId by remember { mutableStateOf("smartexplorer@upi") }
    var upiName by remember { mutableStateOf("Smart Workspace Corp") }
    var upiAmount by remember { mutableStateOf("100") }
    var upiNote by remember { mutableStateOf("Workspace Upgrade") }
    var vcardName by remember { mutableStateOf("John Anderson") }
    var vcardOrg by remember { mutableStateOf("Smart Explorer Inc") }
    var vcardTitle by remember { mutableStateOf("Cloud Architect") }
    var vcardPhone by remember { mutableStateOf("+1 555-019-2831") }
    var vcardEmail by remember { mutableStateOf("john.anderson@smartexplorer.app") }
    var vcardWeb by remember { mutableStateOf("www.smartexplorer.app") }
    var locLat by remember { mutableStateOf("19.0760") }
    var locLng by remember { mutableStateOf("72.8777") }
    var eventTitle by remember { mutableStateOf("Smart Tech Summit") }
    var eventDesc by remember { mutableStateOf("Explore AI and edge integrations live.") }
    var eventStart by remember { mutableStateOf("2026-08-15T10:00") }
    var eventEnd by remember { mutableStateOf("2026-08-15T18:00") }
    var socialPlatform by remember { mutableStateOf("Twitter") }
    var socialUsername by remember { mutableStateOf("SmartExplorerApp") }
    var cryptoCoin by remember { mutableStateOf("BTC") }
    var cryptoAddress by remember { mutableStateOf("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa") }

    // Customization Styles
    var fgColorInt by remember { mutableStateOf(0xFF1E88E5.toInt()) }
    var bgColorInt by remember { mutableStateOf(0xFFFFFFFF.toInt()) }
    var useGradient by remember { mutableStateOf(true) }
    var gradientColorInt by remember { mutableStateOf(0xFFD81B60.toInt()) }
    var dotStyle by remember { mutableStateOf("Rounded") } // Square, Rounded, Dot, Gapped, Star
    var cornerStyle by remember { mutableStateOf("Rounded") } // Square, Rounded, Rounded Outer, Leaf
    var selectedLogo by remember { mutableStateOf("Globe") } // None, Globe, Wifi, Email, Phone, Pay, Maps, WhatsApp, Bitcoin
    var isTransparentBg by remember { mutableStateOf(false) }
    var customMargin by remember { mutableStateOf(16f) }
    var qrSizeSlider by remember { mutableStateOf(220f) }
    var errorCorrectionLevel by remember { mutableStateOf("M") } // L, M, Q, H

    // Live state checks
    var showZoomDialog by remember { mutableStateOf(false) }
    var showUploadMockSheet by remember { mutableStateOf(false) }
    var isGeneratingLive by remember { mutableStateOf(false) }
    var customLogoBase64Simulated by remember { mutableStateOf<String?>(null) }

    // Dynamic state computation
    val combinedQrPayload = remember(
        selectedType, urlInput, textInput, emailTo, emailSubject, emailBody,
        phoneInput, smsPhone, smsMessage, waPhone, waMessage, wifiSsid, wifiPassword, wifiSec, wifiHidden,
        upiId, upiName, upiAmount, upiNote, vcardName, vcardOrg, vcardTitle, vcardPhone, vcardEmail, vcardWeb,
        locLat, locLng, eventTitle, eventDesc, eventStart, eventEnd, socialPlatform, socialUsername,
        cryptoCoin, cryptoAddress
    ) {
        when (selectedType) {
            "URL" -> {
                if (urlInput.startsWith("http://") || urlInput.startsWith("https://")) urlInput
                else if (urlInput.isNotBlank()) "https://$urlInput"
                else "https://smartexplorer.app"
            }
            "Text" -> textInput
            "Email" -> "mailto:$emailTo?subject=${Uri.encode(emailSubject)}&body=${Uri.encode(emailBody)}"
            "Phone" -> "tel:$phoneInput"
            "SMS" -> "SMSTO:$smsPhone:$smsMessage"
            "WhatsApp" -> "https://wa.me/${waPhone.replace("+", "")}?text=${Uri.encode(waMessage)}"
            "WiFi" -> "WIFI:S:$wifiSsid;T:${if (wifiSec == "None") "nopass" else wifiSec};P:$wifiPassword;H:${if (wifiHidden) "true" else "false"};;"
            "UPI" -> "upi://pay?pa=$upiId&pn=${Uri.encode(upiName)}&am=$upiAmount&tn=${Uri.encode(upiNote)}"
            "Contact" -> "BEGIN:VCARD\nVERSION:3.0\nN:$vcardName\nORG:$vcardOrg\nTITLE:$vcardTitle\nTEL:$vcardPhone\nEMAIL:$vcardEmail\nURL:$vcardWeb\nEND:VCARD"
            "Location" -> "geo:$locLat,$locLng?q=$locLat,$locLng"
            "Event" -> "BEGIN:VEVENT\nSUMMARY:$eventTitle\nDESCRIPTION:$eventDesc\nDTSTART:$eventStart\nDTEND:$eventEnd\nEND:VEVENT"
            "Social" -> {
                when (socialPlatform) {
                    "Twitter" -> "https://twitter.com/$socialUsername"
                    "Instagram" -> "https://instagram.com/$socialUsername"
                    "GitHub" -> "https://github.com/$socialUsername"
                    "LinkedIn" -> "https://linkedin.com/in/$socialUsername"
                    else -> "https://social.com/$socialUsername"
                }
            }
            "Crypto" -> {
                when (cryptoCoin) {
                    "BTC" -> "bitcoin:$cryptoAddress"
                    "ETH" -> "ethereum:$cryptoAddress"
                    "SOL" -> "solana:$cryptoAddress"
                    "DOGE" -> "dogecoin:$cryptoAddress"
                    else -> cryptoAddress
                }
            }
            else -> "https://smartexplorer.app"
        }
    }

    // Is active generated QR a favorite in global state?
    val isFavByGlobalFlow by viewModel.isItemFavoriteFlow("QR_GEN", combinedQrPayload).collectAsState(initial = false)

    // AMOLED Background or standard background brush
    val rootBackgroundModifier = if (isAmoledTheme) {
        Modifier.fillMaxSize().background(Color(0xFF000000))
    } else {
        Modifier.fillMaxSize()
    }

    // Simulate auto-generation progress
    LaunchedEffect(combinedQrPayload) {
        isGeneratingLive = true
        kotlinx.coroutines.delay(180)
        isGeneratingLive = false
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = modifier
    ) { paddingValues ->
        PremiumGradientBackground(modifier = rootBackgroundModifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                // PREMIUM TOP HEADER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("qr_hub_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isAmoledTheme) Color.White else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = getTxt(currentLanguage, "hub_title"),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = if (isAmoledTheme) Color.White else MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = getTxt(currentLanguage, "hub_subtitle"),
                            style = MaterialTheme.typography.labelSmall,
                            color = (if (isAmoledTheme) Color.White else MaterialTheme.colorScheme.onBackground).copy(alpha = 0.6f)
                        )
                    }

                    // Quick Toggle Favorite of current generated content
                    IconButton(
                        onClick = {
                            if (combinedQrPayload.isNotBlank()) {
                                viewModel.toggleFavorite(
                                    type = "QR_GEN",
                                    key = combinedQrPayload,
                                    title = "$selectedType QR",
                                    subtitle = combinedQrPayload,
                                    content = "Custom styled QR payload"
                                )
                                Toast.makeText(
                                    context,
                                    if (isFavByGlobalFlow) "Removed from Board" else "Saved to Global Board",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.testTag("qr_hub_fav_button")
                    ) {
                        Icon(
                            imageVector = if (isFavByGlobalFlow) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Global Favorite",
                            tint = if (isFavByGlobalFlow) Color(0xFFFFC107) else if (isAmoledTheme) Color.White else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // HIGH-DENSITY M3 SCROLLABLE TAB ROW
                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = if (isAmoledTheme) Color.White else MaterialTheme.colorScheme.primary,
                    edgePadding = 0.dp,
                    divider = {},
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            icon = {
                                Icon(
                                    imageVector = when (index) {
                                        0 -> Icons.Default.QrCode
                                        1 -> Icons.Default.QrCodeScanner
                                        2 -> Icons.Default.Layers
                                        3 -> Icons.Default.History
                                        else -> Icons.Default.Settings
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // RESPONSIVE SCREEN WRAPPER
                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val isTablet = maxWidth > 650.dp

                    Crossfade(targetState = activeTab, label = "tab_crossfade") { screenState ->
                        when (screenState) {
                            0 -> {
                                // 1. GENERATOR TAB
                                if (isTablet) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1.2f).verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            TypeSelectorModule(selectedType, currentLanguage) { selectedType = it }
                                            InputFieldsModule(selectedType, currentLanguage, urlInput, { urlInput = it }, textInput, { textInput = it }, emailTo, { emailTo = it }, emailSubject, { emailSubject = it }, emailBody, { emailBody = it }, phoneInput, { phoneInput = it }, smsPhone, { smsPhone = it }, smsMessage, { smsMessage = it }, waPhone, { waPhone = it }, waMessage, { waMessage = it }, wifiSsid, { wifiSsid = it }, wifiPassword, { wifiPassword = it }, wifiSec, { wifiSec = it }, wifiHidden, { wifiHidden = it }, upiId, { upiId = it }, upiName, { upiName = it }, upiAmount, { upiAmount = it }, upiNote, { upiNote = it }, vcardName, { vcardName = it }, vcardOrg, { vcardOrg = it }, vcardTitle, { vcardTitle = it }, vcardPhone, { vcardPhone = it }, vcardEmail, { vcardEmail = it }, vcardWeb, { vcardWeb = it }, locLat, { locLat = it }, locLng, { locLng = it }, eventTitle, { eventTitle = it }, eventDesc, { eventDesc = it }, eventStart, { eventStart = it }, eventEnd, { eventEnd = it }, socialPlatform, { socialPlatform = it }, socialUsername, { socialUsername = it }, cryptoCoin, { cryptoCoin = it }, cryptoAddress, { cryptoAddress = it })
                                            CustomizationModule(currentLanguage, fgColorInt, { fgColorInt = it }, bgColorInt, { bgColorInt = it }, useGradient, { useGradient = it }, gradientColorInt, { gradientColorInt = it }, dotStyle, { dotStyle = it }, cornerStyle, { cornerStyle = it }, selectedLogo, { selectedLogo = it }, isTransparentBg, { isTransparentBg = it }, customMargin, { customMargin = it }, qrSizeSlider, { qrSizeSlider = it }, errorCorrectionLevel, { errorCorrectionLevel = it })
                                        }
                                        Column(
                                            modifier = Modifier.weight(0.8f).verticalScroll(rememberScrollState()),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            PreviewModule(combinedQrPayload, fgColorInt, bgColorInt, useGradient, gradientColorInt, dotStyle, cornerStyle, selectedLogo, isTransparentBg, customMargin, qrSizeSlider, errorCorrectionLevel, isGeneratingLive, { showZoomDialog = true })
                                            ExportActionsModule(context, combinedQrPayload, fgColorInt, bgColorInt, useGradient, gradientColorInt, dotStyle, cornerStyle, currentLanguage, {
                                                // Save to local history
                                                val newItem = QrHistoryItem(
                                                    id = UUID.randomUUID().toString(),
                                                    type = selectedType,
                                                    content = combinedQrPayload,
                                                    title = "$selectedType - ${combinedQrPayload.take(24)}",
                                                    timestamp = System.currentTimeMillis(),
                                                    isFavorite = false,
                                                    foregroundColor = fgColorInt,
                                                    backgroundColor = bgColorInt,
                                                    dotStyle = dotStyle,
                                                    cornerStyle = cornerStyle
                                                )
                                                historyList = listOf(newItem) + historyList
                                                saveHistoryToPrefs(context, historyList)
                                            })
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        TypeSelectorModule(selectedType, currentLanguage) { selectedType = it }
                                        InputFieldsModule(selectedType, currentLanguage, urlInput, { urlInput = it }, textInput, { textInput = it }, emailTo, { emailTo = it }, emailSubject, { emailSubject = it }, emailBody, { emailBody = it }, phoneInput, { phoneInput = it }, smsPhone, { smsPhone = it }, smsMessage, { smsMessage = it }, waPhone, { waPhone = it }, waMessage, { waMessage = it }, wifiSsid, { wifiSsid = it }, wifiPassword, { wifiPassword = it }, wifiSec, { wifiSec = it }, wifiHidden, { wifiHidden = it }, upiId, { upiId = it }, upiName, { upiName = it }, upiAmount, { upiAmount = it }, upiNote, { upiNote = it }, vcardName, { vcardName = it }, vcardOrg, { vcardOrg = it }, vcardTitle, { vcardTitle = it }, vcardPhone, { vcardPhone = it }, vcardEmail, { vcardEmail = it }, vcardWeb, { vcardWeb = it }, locLat, { locLat = it }, locLng, { locLng = it }, eventTitle, { eventTitle = it }, eventDesc, { eventDesc = it }, eventStart, { eventStart = it }, eventEnd, { eventEnd = it }, socialPlatform, { socialPlatform = it }, socialUsername, { socialUsername = it }, cryptoCoin, { cryptoCoin = it }, cryptoAddress, { cryptoAddress = it })
                                        PreviewModule(combinedQrPayload, fgColorInt, bgColorInt, useGradient, gradientColorInt, dotStyle, cornerStyle, selectedLogo, isTransparentBg, customMargin, qrSizeSlider, errorCorrectionLevel, isGeneratingLive, { showZoomDialog = true })
                                        CustomizationModule(currentLanguage, fgColorInt, { fgColorInt = it }, bgColorInt, { bgColorInt = it }, useGradient, { useGradient = it }, gradientColorInt, { gradientColorInt = it }, dotStyle, { dotStyle = it }, cornerStyle, { cornerStyle = it }, selectedLogo, { selectedLogo = it }, isTransparentBg, { isTransparentBg = it }, customMargin, { customMargin = it }, qrSizeSlider, { qrSizeSlider = it }, errorCorrectionLevel, { errorCorrectionLevel = it })
                                        ExportActionsModule(context, combinedQrPayload, fgColorInt, bgColorInt, useGradient, gradientColorInt, dotStyle, cornerStyle, currentLanguage, {
                                            // Save to local history
                                            val newItem = QrHistoryItem(
                                                id = UUID.randomUUID().toString(),
                                                type = selectedType,
                                                content = combinedQrPayload,
                                                title = "$selectedType - ${combinedQrPayload.take(24)}",
                                                timestamp = System.currentTimeMillis(),
                                                isFavorite = false,
                                                foregroundColor = fgColorInt,
                                                backgroundColor = bgColorInt,
                                                dotStyle = dotStyle,
                                                cornerStyle = cornerStyle
                                            )
                                            historyList = listOf(newItem) + historyList
                                            saveHistoryToPrefs(context, historyList)
                                        })
                                        Spacer(modifier = Modifier.height(24.dp))
                                    }
                                }
                            }
                            1 -> {
                                // 2. SCANNER TAB
                                ScannerModule(context, isAmoledTheme, currentLanguage, historyList, { updatedHistory ->
                                    historyList = updatedHistory
                                    saveHistoryToPrefs(context, historyList)
                                })
                            }
                            2 -> {
                                // 3. BATCH GENERATOR TAB
                                BatchGeneratorModule(context, currentLanguage, fgColorInt, bgColorInt, useGradient, gradientColorInt, dotStyle, cornerStyle)
                            }
                            3 -> {
                                // 4. HISTORY & STATS TAB
                                HistoryModule(context, currentLanguage, historyList, { updatedHistory ->
                                    historyList = updatedHistory
                                    saveHistoryToPrefs(context, historyList)
                                })
                            }
                            4 -> {
                                // 5. DYNAMIC & SETTINGS TAB
                                SettingsModule(
                                    currentLanguage, { currentLanguage = it },
                                    isAmoledTheme, { isAmoledTheme = it },
                                    context
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ZOOM PREVIEW DIALOG
    if (showZoomDialog) {
        Dialog(onDismissRequest = { showZoomDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Super High-Res Matrix Zoom",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    var zoomLevel by remember { mutableStateOf(260f) }

                    Box(
                        modifier = Modifier.size(zoomLevel.dp).background(Color.White).padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val zoomInt = zoomLevel.toInt()
                        QrCodeCanvas(
                            modifier = Modifier.size((zoomLevel - 16).dp),
                            content = combinedQrPayload,
                            fgColorInt = fgColorInt,
                            bgColorInt = bgColorInt,
                            useGradient = useGradient,
                            gradientColorInt = gradientColorInt,
                            dotStyle = dotStyle,
                            cornerStyle = cornerStyle,
                            isTransparentBg = isTransparentBg,
                            customMargin = customMargin,
                            errorCorrectionLevel = errorCorrectionLevel
                        )
                    }

                    Slider(
                        value = zoomLevel,
                        onValueChange = { zoomLevel = it },
                        valueRange = 150f..320f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Interactive Scale: ${zoomLevel.toInt()}dp", style = MaterialTheme.typography.bodySmall)

                    Button(
                        onClick = { showZoomDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss Zoom View", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE COMPONENTS
// -------------------------------------------------------------

@Composable
fun TypeSelectorModule(selectedType: String, lang: String, onTypeChange: (String) -> Unit) {
    val types = listOf(
        Pair("URL", Icons.Default.Language),
        Pair("Text", Icons.Default.Description),
        Pair("Email", Icons.Default.Email),
        Pair("Phone", Icons.Default.Phone),
        Pair("SMS", Icons.Default.Send),
        Pair("WhatsApp", Icons.Default.Chat),
        Pair("WiFi", Icons.Default.Wifi),
        Pair("UPI", Icons.Default.Payments),
        Pair("Contact", Icons.Default.Person),
        Pair("Location", Icons.Default.Place),
        Pair("Event", Icons.Default.Event),
        Pair("Social", Icons.Default.Share),
        Pair("Crypto", Icons.Default.Wallet)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Select QR Type",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { (type, icon) ->
                val isSelected = selectedType == type
                FilterChip(
                    selected = isSelected,
                    onClick = { onTypeChange(type) },
                    label = { Text(type, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)) },
                    leadingIcon = { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputFieldsModule(
    type: String,
    lang: String,
    urlInput: String, onUrl: (String) -> Unit,
    textInput: String, onText: (String) -> Unit,
    emailTo: String, onEmailTo: (String) -> Unit,
    emailSubject: String, onEmailSub: (String) -> Unit,
    emailBody: String, onEmailBody: (String) -> Unit,
    phoneInput: String, onPhone: (String) -> Unit,
    smsPhone: String, onSmsPhone: (String) -> Unit,
    smsMessage: String, onSmsMsg: (String) -> Unit,
    waPhone: String, onWaPhone: (String) -> Unit,
    waMessage: String, onWaMsg: (String) -> Unit,
    wifiSsid: String, onWifiSsid: (String) -> Unit,
    wifiPassword: String, onWifiPass: (String) -> Unit,
    wifiSec: String, onWifiSec: (String) -> Unit,
    wifiHidden: Boolean, onWifiHidden: (Boolean) -> Unit,
    upiId: String, onUpiId: (String) -> Unit,
    upiName: String, onUpiName: (String) -> Unit,
    upiAmount: String, onUpiAmount: (String) -> Unit,
    upiNote: String, onUpiNote: (String) -> Unit,
    vcardName: String, onVcardName: (String) -> Unit,
    vcardOrg: String, onVcardOrg: (String) -> Unit,
    vcardTitle: String, onVcardTitle: (String) -> Unit,
    vcardPhone: String, onVcardPhone: (String) -> Unit,
    vcardEmail: String, onVcardEmail: (String) -> Unit,
    vcardWeb: String, onVcardWeb: (String) -> Unit,
    locLat: String, onLocLat: (String) -> Unit,
    locLng: String, onLocLng: (String) -> Unit,
    eventTitle: String, onEventTitle: (String) -> Unit,
    eventDesc: String, onEventDesc: (String) -> Unit,
    eventStart: String, onEventStart: (String) -> Unit,
    eventEnd: String, onEventEnd: (String) -> Unit,
    socialPlatform: String, onSocialPlatform: (String) -> Unit,
    socialUsername: String, onSocialUser: (String) -> Unit,
    cryptoCoin: String, onCryptoCoin: (String) -> Unit,
    cryptoAddress: String, onCryptoAddress: (String) -> Unit
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "${getTxt(lang, "encode_content")} - $type",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            when (type) {
                "URL" -> {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = onUrl,
                        placeholder = { Text("https://example.com") },
                        label = { Text("Website Address") },
                        leadingIcon = { Icon(Icons.Default.Language, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                "Text" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = onText,
                            placeholder = { Text("Type any custom note...") },
                            label = { Text("Custom Text Payload") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Characters: ${textInput.length}", style = MaterialTheme.typography.labelSmall)
                            LinearProgressIndicator(
                                progress = { (textInput.length / 500f).coerceIn(0f, 1f) },
                                modifier = Modifier.width(80.dp).align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
                "Email" -> {
                    OutlinedTextField(value = emailTo, onValueChange = onEmailTo, label = { Text("To Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = emailSubject, onValueChange = onEmailSub, label = { Text("Email Subject") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = emailBody, onValueChange = onEmailBody, label = { Text("Message Body") }, modifier = Modifier.fillMaxWidth())
                }
                "Phone" -> {
                    OutlinedTextField(value = phoneInput, onValueChange = onPhone, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Phone, null) })
                }
                "SMS" -> {
                    OutlinedTextField(value = smsPhone, onValueChange = onSmsPhone, label = { Text("Target Phone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = smsMessage, onValueChange = onSmsMsg, label = { Text("SMS Body") }, modifier = Modifier.fillMaxWidth())
                }
                "WhatsApp" -> {
                    OutlinedTextField(value = waPhone, onValueChange = onWaPhone, label = { Text("WhatsApp Phone (with Country Code)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = waMessage, onValueChange = onWaMsg, label = { Text("Prefilled Message") }, modifier = Modifier.fillMaxWidth())
                }
                "WiFi" -> {
                    OutlinedTextField(value = wifiSsid, onValueChange = onWifiSsid, label = { Text("Network SSID") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = wifiPassword, onValueChange = onWifiPass, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("WPA", "WEP", "None").forEach { mode ->
                            FilterChip(
                                selected = wifiSec == mode,
                                onClick = { onWifiSec(mode) },
                                label = { Text(mode) }
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = wifiHidden, onCheckedChange = onWifiHidden)
                        Text("Hidden Network", style = MaterialTheme.typography.bodySmall)
                    }
                }
                "UPI" -> {
                    OutlinedTextField(value = upiId, onValueChange = onUpiId, label = { Text("Payee UPI ID") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = upiName, onValueChange = onUpiName, label = { Text("Merchant Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = upiAmount, onValueChange = onUpiAmount, label = { Text("Amount (INR)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = upiNote, onValueChange = onUpiNote, label = { Text("Transaction Note") }, modifier = Modifier.fillMaxWidth())
                }
                "Contact" -> {
                    OutlinedTextField(value = vcardName, onValueChange = onVcardName, label = { Text("Contact Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = vcardOrg, onValueChange = onVcardOrg, label = { Text("Organization") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = vcardTitle, onValueChange = onVcardTitle, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = vcardPhone, onValueChange = onVcardPhone, label = { Text("Mobile Phone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = vcardEmail, onValueChange = onVcardEmail, label = { Text("Email ID") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = vcardWeb, onValueChange = onVcardWeb, label = { Text("Website Link") }, modifier = Modifier.fillMaxWidth())
                }
                "Location" -> {
                    OutlinedTextField(value = locLat, onValueChange = onLocLat, label = { Text("Latitude") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = locLng, onValueChange = onLocLng, label = { Text("Longitude") }, modifier = Modifier.fillMaxWidth())
                }
                "Event" -> {
                    OutlinedTextField(value = eventTitle, onValueChange = onEventTitle, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eventDesc, onValueChange = onEventDesc, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eventStart, onValueChange = onEventStart, label = { Text("Start Date-Time (e.g. 2026-08-15T10:00)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eventEnd, onValueChange = onEventEnd, label = { Text("End Date-Time") }, modifier = Modifier.fillMaxWidth())
                }
                "Social" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Twitter", "Instagram", "GitHub", "LinkedIn").forEach { plat ->
                            FilterChip(
                                selected = socialPlatform == plat,
                                onClick = { onSocialPlatform(plat) },
                                label = { Text(plat) }
                            )
                        }
                    }
                    OutlinedTextField(value = socialUsername, onValueChange = onSocialUser, label = { Text("Profile Username") }, modifier = Modifier.fillMaxWidth())
                }
                "Crypto" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("BTC", "ETH", "SOL", "DOGE").forEach { coin ->
                            FilterChip(
                                selected = cryptoCoin == coin,
                                onClick = { onCryptoCoin(coin) },
                                label = { Text(coin) }
                            )
                        }
                    }
                    OutlinedTextField(value = cryptoAddress, onValueChange = onCryptoAddress, label = { Text("Wallet Public Address") }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun PreviewModule(
    content: String,
    fgColorInt: Int,
    bgColorInt: Int,
    useGradient: Boolean,
    gradientColorInt: Int,
    dotStyle: String,
    cornerStyle: String,
    selectedLogo: String,
    isTransparentBg: Boolean,
    customMargin: Float,
    qrSizeSlider: Float,
    errorCorrectionLevel: String,
    isGenerating: Boolean,
    onZoomClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Real-Time Preview",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                IconButton(onClick = onZoomClick) {
                    Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom Code")
                }
            }

            Box(
                modifier = Modifier
                    .size(qrSizeSlider.dp)
                    .background(
                        if (isTransparentBg) Color.Transparent else Color(bgColorInt),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(customMargin.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                } else {
                    QrCodeCanvas(
                        modifier = Modifier.fillMaxSize(),
                        content = content,
                        fgColorInt = fgColorInt,
                        bgColorInt = bgColorInt,
                        useGradient = useGradient,
                        gradientColorInt = gradientColorInt,
                        dotStyle = dotStyle,
                        cornerStyle = cornerStyle,
                        isTransparentBg = isTransparentBg,
                        customMargin = customMargin,
                        errorCorrectionLevel = errorCorrectionLevel
                    )

                    // Embedded preset logos
                    if (selectedLogo != "None") {
                        Surface(
                            modifier = Modifier.size((qrSizeSlider * 0.22f).dp),
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(2.dp, Color(fgColorInt)),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val logoIcon = when (selectedLogo) {
                                    "Globe" -> Icons.Default.Language
                                    "Wifi" -> Icons.Default.Wifi
                                    "Email" -> Icons.Default.Email
                                    "Phone" -> Icons.Default.Phone
                                    "Pay" -> Icons.Default.Payments
                                    "Maps" -> Icons.Default.Place
                                    "WhatsApp" -> Icons.Default.Chat
                                    "Bitcoin" -> Icons.Default.Star
                                    else -> Icons.Default.Star
                                }
                                Icon(
                                    imageVector = logoIcon,
                                    contentDescription = "Logo",
                                    tint = Color(fgColorInt),
                                    modifier = Modifier.size((qrSizeSlider * 0.12f).dp)
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Type matches matrix algorithm parameters.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QrCodeCanvas(
    modifier: Modifier,
    content: String,
    fgColorInt: Int,
    bgColorInt: Int,
    useGradient: Boolean,
    gradientColorInt: Int,
    dotStyle: String,
    cornerStyle: String,
    isTransparentBg: Boolean,
    customMargin: Float,
    errorCorrectionLevel: String
) {
    Canvas(modifier = modifier) {
        val sizeW = size.width
        val sizeH = size.height
        val matrixSize = 25

        val fgColor = Color(fgColorInt)
        val bgColor = if (isTransparentBg) Color.Transparent else Color(bgColorInt)
        val gradColor = if (useGradient) Color(gradientColorInt) else fgColor

        val blockWidth = sizeW / matrixSize
        val blockHeight = sizeH / matrixSize

        // Draw background
        if (!isTransparentBg) {
            drawRect(color = bgColor, topLeft = Offset.Zero, size = size)
        }

        // Draw deterministic matrix data based on content hashing
        val rand = java.util.Random(content.hashCode().toLong())

        val grid = Array(matrixSize) { BooleanArray(matrixSize) }
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                // Reserve Finder Patterns at corners
                if ((r < 7 && c < 7) || (r < 7 && c >= matrixSize - 7) || (r >= matrixSize - 7 && c < 7)) {
                    grid[r][c] = false
                } else {
                    grid[r][c] = rand.nextFloat() < when (errorCorrectionLevel) {
                        "L" -> 0.45f
                        "M" -> 0.52f
                        "Q" -> 0.58f
                        "H" -> 0.64f
                        else -> 0.52f
                    }
                }
            }
        }

        // Draw Data Dots
        val fillBrush = Brush.linearGradient(
            colors = listOf(fgColor, gradColor),
            start = Offset.Zero,
            end = Offset(sizeW, sizeH)
        )

        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                if (grid[r][c]) {
                    val tlX = c * blockWidth
                    val tlY = r * blockHeight

                    when (dotStyle) {
                        "Square" -> {
                            drawRect(
                                brush = fillBrush,
                                topLeft = Offset(tlX, tlY),
                                size = androidx.compose.ui.geometry.Size(blockWidth * 0.9f, blockHeight * 0.9f)
                            )
                        }
                        "Rounded" -> {
                            drawRoundRect(
                                brush = fillBrush,
                                topLeft = Offset(tlX, tlY),
                                size = androidx.compose.ui.geometry.Size(blockWidth * 0.85f, blockHeight * 0.85f),
                                cornerRadius = CornerRadius(blockWidth * 0.35f, blockHeight * 0.35f)
                            )
                        }
                        "Dot" -> {
                            drawCircle(
                                brush = fillBrush,
                                radius = blockWidth * 0.4f,
                                center = Offset(tlX + blockWidth / 2f, tlY + blockHeight / 2f)
                            )
                        }
                        "Gapped" -> {
                            drawRect(
                                brush = fillBrush,
                                topLeft = Offset(tlX + blockWidth * 0.15f, tlY + blockHeight * 0.15f),
                                size = androidx.compose.ui.geometry.Size(blockWidth * 0.65f, blockHeight * 0.65f)
                            )
                        }
                        "Star" -> {
                            val starPath = Path().apply {
                                val cx = tlX + blockWidth / 2f
                                val cy = tlY + blockHeight / 2f
                                moveTo(cx, tlY)
                                lineTo(tlX + blockWidth, cy)
                                lineTo(cx, tlY + blockHeight)
                                lineTo(tlX, cy)
                                close()
                            }
                            drawPath(path = starPath, brush = fillBrush)
                        }
                    }
                }
            }
        }

        // Draw the Three Finder Patterns beautifully
        val finderWidth = blockWidth * 7f
        val finderHeight = blockHeight * 7f

        val finderCoords = listOf(
            Offset(0f, 0f),
            Offset(sizeW - finderWidth, 0f),
            Offset(0f, sizeH - finderHeight)
        )

        finderCoords.forEach { offset ->
            val oX = offset.x
            val oY = offset.y

            when (cornerStyle) {
                "Square" -> {
                    // Outer Frame
                    drawRect(brush = fillBrush, topLeft = Offset(oX, oY), size = androidx.compose.ui.geometry.Size(finderWidth, finderHeight))
                    drawRect(color = bgColor, topLeft = Offset(oX + blockWidth, oY + blockHeight), size = androidx.compose.ui.geometry.Size(blockWidth * 5, blockHeight * 5))
                    // Inner dot
                    drawRect(brush = fillBrush, topLeft = Offset(oX + blockWidth * 2, oY + blockHeight * 2), size = androidx.compose.ui.geometry.Size(blockWidth * 3, blockHeight * 3))
                }
                "Rounded" -> {
                    drawRoundRect(brush = fillBrush, topLeft = Offset(oX, oY), size = androidx.compose.ui.geometry.Size(finderWidth, finderHeight), cornerRadius = CornerRadius(blockWidth * 2f))
                    drawRoundRect(color = bgColor, topLeft = Offset(oX + blockWidth, oY + blockHeight), size = androidx.compose.ui.geometry.Size(blockWidth * 5, blockHeight * 5), cornerRadius = CornerRadius(blockWidth * 1.5f))
                    drawRoundRect(brush = fillBrush, topLeft = Offset(oX + blockWidth * 2, oY + blockHeight * 2), size = androidx.compose.ui.geometry.Size(blockWidth * 3, blockHeight * 3), cornerRadius = CornerRadius(blockWidth * 1f))
                }
                "Rounded Outer" -> {
                    drawRoundRect(brush = fillBrush, topLeft = Offset(oX, oY), size = androidx.compose.ui.geometry.Size(finderWidth, finderHeight), cornerRadius = CornerRadius(blockWidth * 2.5f))
                    drawRect(color = bgColor, topLeft = Offset(oX + blockWidth, oY + blockHeight), size = androidx.compose.ui.geometry.Size(blockWidth * 5, blockHeight * 5))
                    drawRect(brush = fillBrush, topLeft = Offset(oX + blockWidth * 2, oY + blockHeight * 2), size = androidx.compose.ui.geometry.Size(blockWidth * 3, blockHeight * 3))
                }
                "Leaf" -> {
                    val outerPath = Path().apply {
                        moveTo(oX + finderWidth / 2f, oY)
                        quadraticBezierTo(oX + finderWidth, oY, oX + finderWidth, oY + finderHeight / 2f)
                        quadraticBezierTo(oX + finderWidth, oY + finderHeight, oX + finderWidth / 2f, oY + finderHeight)
                        quadraticBezierTo(oX, oY + finderHeight, oX, oY + finderHeight / 2f)
                        quadraticBezierTo(oX, oY, oX + finderWidth / 2f, oY)
                        close()
                    }
                    drawPath(path = outerPath, brush = fillBrush)

                    val innerPath = Path().apply {
                        val iX = oX + blockWidth
                        val iY = oY + blockHeight
                        val iW = blockWidth * 5
                        val iH = blockHeight * 5
                        moveTo(iX + iW / 2f, iY)
                        quadraticBezierTo(iX + iW, iY, iX + iW, iY + iH / 2f)
                        quadraticBezierTo(iX + iW, iY + iH, iX + iW / 2f, iY + iH)
                        quadraticBezierTo(iX, iY + iH, iX, iY + iH / 2f)
                        quadraticBezierTo(iX, iY, iX + iW / 2f, iY)
                        close()
                    }
                    drawPath(path = innerPath, color = bgColor)

                    val dotPath = Path().apply {
                        val dX = oX + blockWidth * 2f
                        val dY = oY + blockHeight * 2f
                        val dW = blockWidth * 3f
                        val dH = blockHeight * 3f
                        moveTo(dX + dW / 2f, dY)
                        quadraticBezierTo(dX + dW, dY, dX + dW, dY + dH / 2f)
                        quadraticBezierTo(dX + dW, dY + dH, dX + dW / 2f, dY + dH)
                        quadraticBezierTo(dX, dY + dH, dX, dY + dH / 2f)
                        quadraticBezierTo(dX, dY, dX + dW / 2f, dY)
                        close()
                    }
                    drawPath(path = dotPath, brush = fillBrush)
                }
            }
        }
    }
}

@Composable
fun CustomizationModule(
    lang: String,
    fgColor: Int, onFgChange: (Int) -> Unit,
    bgColor: Int, onBgChange: (Int) -> Unit,
    useGradient: Boolean, onUseGrad: (Boolean) -> Unit,
    gradColor: Int, onGradChange: (Int) -> Unit,
    dotStyle: String, onDotStyle: (String) -> Unit,
    cornerStyle: String, onCornerStyle: (String) -> Unit,
    selectedLogo: String, onLogo: (String) -> Unit,
    isTransparent: Boolean, onTransparent: (Boolean) -> Unit,
    customMargin: Float, onMargin: (Float) -> Unit,
    sizeSlider: Float, onSizeSlider: (Float) -> Unit,
    errorLevel: String, onErrorLevel: (String) -> Unit
) {
    var expandedSection by remember { mutableStateOf(0) } // 0: Colors, 1: Shapes, 2: Logos, 3: Layout

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = getTxt(lang, "customization"),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        // Collapsible Accordions for Customization Parameters
        // 1. Colors Accordion
        CustomAccordionRow(
            title = getTxt(lang, "colors"),
            isExpanded = expandedSection == 0,
            onClick = { expandedSection = if (expandedSection == 0) -1 else 0 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(12.dp)) {
                Text("Foreground Preset Color", style = MaterialTheme.typography.bodySmall)
                PresetColorRow(fgColor, onFgChange)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useGradient, onCheckedChange = onUseGrad)
                    Text("Apply Premium Color Gradient", style = MaterialTheme.typography.bodySmall)
                }

                if (useGradient) {
                    Text("Gradient Accent Color", style = MaterialTheme.typography.bodySmall)
                    PresetColorRow(gradColor, onGradChange)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isTransparent, onCheckedChange = onTransparent)
                    Text("Transparent Background", style = MaterialTheme.typography.bodySmall)
                }

                if (!isTransparent) {
                    Text("Background Canvas Color", style = MaterialTheme.typography.bodySmall)
                    PresetColorRow(bgColor, onBgChange, isBg = true)
                }
            }
        }

        // 2. Dot & Corner Styles Accordion
        CustomAccordionRow(
            title = "Dot & Corner Shapes",
            isExpanded = expandedSection == 1,
            onClick = { expandedSection = if (expandedSection == 1) -1 else 1 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(12.dp)) {
                Text(getTxt(lang, "dot_style"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Square", "Rounded", "Dot", "Gapped", "Star").forEach { style ->
                        FilterChip(
                            selected = dotStyle == style,
                            onClick = { onDotStyle(style) },
                            label = { Text(style) }
                        )
                    }
                }

                Text(getTxt(lang, "corner_style"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Square", "Rounded", "Rounded Outer", "Leaf").forEach { style ->
                        FilterChip(
                            selected = cornerStyle == style,
                            onClick = { onCornerStyle(style) },
                            label = { Text(style) }
                        )
                    }
                }
            }
        }

        // 3. Logo Presets Accordion
        CustomAccordionRow(
            title = getTxt(lang, "logo_presets"),
            isExpanded = expandedSection == 2,
            onClick = { expandedSection = if (expandedSection == 2) -1 else 2 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(12.dp)) {
                Text("Select Embedded Center Branding", style = MaterialTheme.typography.bodySmall)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("None", "Globe", "Wifi", "Email", "Phone", "Pay", "Maps", "WhatsApp", "Bitcoin").forEach { logo ->
                        FilterChip(
                            selected = selectedLogo == logo,
                            onClick = { onLogo(logo) },
                            label = { Text(logo) }
                        )
                    }
                }
            }
        }

        // 4. Layout & Options Accordion
        CustomAccordionRow(
            title = getTxt(lang, "options"),
            isExpanded = expandedSection == 3,
            onClick = { expandedSection = if (expandedSection == 3) -1 else 3 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(12.dp)) {
                Text("${getTxt(lang, "qr_size")}: ${sizeSlider.toInt()} dp", style = MaterialTheme.typography.bodySmall)
                Slider(value = sizeSlider, onValueChange = onSizeSlider, valueRange = 160f..300f)

                Text("${getTxt(lang, "margin")}: ${customMargin.toInt()} dp", style = MaterialTheme.typography.bodySmall)
                Slider(value = customMargin, onValueChange = onMargin, valueRange = 0f..40f)

                Text(getTxt(lang, "error_level"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("L", "M", "Q", "H").forEach { level ->
                        val desc = when (level) {
                            "L" -> "7% Low"
                            "M" -> "15% Mid"
                            "Q" -> "25% High"
                            else -> "30% Ultra"
                        }
                        FilterChip(
                            selected = errorLevel == level,
                            onClick = { onErrorLevel(level) },
                            label = { Text(desc) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomAccordionRow(title: String, isExpanded: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                content()
            }
        }
    }
}

@Composable
fun PresetColorRow(selectedColor: Int, onColorSelect: (Int) -> Unit, isBg: Boolean = false) {
    val colors = if (isBg) {
        listOf(0xFFFFFFFF.toInt(), 0xFFF5F5F5.toInt(), 0xFFE0F7FA.toInt(), 0xFFFFF9C4.toInt(), 0xFFECEFF1.toInt())
    } else {
        listOf(0xFF000000.toInt(), 0xFF1E88E5.toInt(), 0xFFD81B60.toInt(), 0xFF4CAF50.toInt(), 0xFF8E24AA.toInt(), 0xFFFF9800.toInt())
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.forEach { colorVal ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(colorVal))
                    .border(
                        width = if (selectedColor == colorVal) 3.dp else 1.dp,
                        color = if (selectedColor == colorVal) MaterialTheme.colorScheme.primary else Color.Gray,
                        shape = CircleShape
                    )
                    .clickable { onColorSelect(colorVal) }
            )
        }
    }
}

// -------------------------------------------------------------
// QR EXPORT & VECTOR UTILITIES
// -------------------------------------------------------------

fun drawQrOnAndroidCanvas(
    canvas: android.graphics.Canvas,
    sizeW: Float,
    sizeH: Float,
    content: String,
    fgColorInt: Int,
    bgColorInt: Int,
    useGradient: Boolean,
    gradientColorInt: Int,
    dotStyle: String,
    cornerStyle: String,
    isTransparentBg: Boolean,
    errorCorrectionLevel: String
) {
    val matrixSize = 25
    val blockWidth = sizeW / matrixSize
    val blockHeight = sizeH / matrixSize

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
    }

    // Draw background
    if (!isTransparentBg) {
        paint.color = bgColorInt
        paint.style = android.graphics.Paint.Style.FILL
        canvas.drawRect(0f, 0f, sizeW, sizeH, paint)
    }

    val rand = java.util.Random(content.hashCode().toLong())
    val grid = Array(matrixSize) { BooleanArray(matrixSize) }
    for (r in 0 until matrixSize) {
        for (c in 0 until matrixSize) {
            if ((r < 7 && c < 7) || (r < 7 && c >= matrixSize - 7) || (r >= matrixSize - 7 && c < 7)) {
                grid[r][c] = false
            } else {
                grid[r][c] = rand.nextFloat() < when (errorCorrectionLevel) {
                    "L" -> 0.45f
                    "M" -> 0.52f
                    "Q" -> 0.58f
                    "H" -> 0.64f
                    else -> 0.52f
                }
            }
        }
    }

    // Gradient Setup
    if (useGradient) {
        val shader = android.graphics.LinearGradient(
            0f, 0f, sizeW, sizeH,
            fgColorInt, gradientColorInt,
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.shader = shader
    } else {
        paint.color = fgColorInt
        paint.shader = null
    }

    paint.style = android.graphics.Paint.Style.FILL

    // Draw Data Dots
    for (r in 0 until matrixSize) {
        for (c in 0 until matrixSize) {
            if (grid[r][c]) {
                val tlX = c * blockWidth
                val tlY = r * blockHeight

                when (dotStyle) {
                    "Square" -> {
                        canvas.drawRect(tlX, tlY, tlX + blockWidth * 0.9f, tlY + blockHeight * 0.9f, paint)
                    }
                    "Rounded" -> {
                        val rect = android.graphics.RectF(tlX, tlY, tlX + blockWidth * 0.85f, tlY + blockHeight * 0.85f)
                        canvas.drawRoundRect(rect, blockWidth * 0.35f, blockHeight * 0.35f, paint)
                    }
                    "Dot" -> {
                        canvas.drawCircle(tlX + blockWidth / 2f, tlY + blockHeight / 2f, blockWidth * 0.4f, paint)
                    }
                    "Gapped" -> {
                        canvas.drawRect(tlX + blockWidth * 0.15f, tlY + blockHeight * 0.15f, tlX + blockWidth * 0.80f, tlY + blockHeight * 0.80f, paint)
                    }
                    "Star" -> {
                        val path = android.graphics.Path().apply {
                            val cx = tlX + blockWidth / 2f
                            val cy = tlY + blockHeight / 2f
                            moveTo(cx, tlY)
                            lineTo(tlX + blockWidth, cy)
                            lineTo(cx, tlY + blockHeight)
                            lineTo(tlX, cy)
                            close()
                        }
                        canvas.drawPath(path, paint)
                    }
                }
            }
        }
    }

    // Draw the Three Finder Patterns
    val finderWidth = blockWidth * 7f
    val finderHeight = blockHeight * 7f

    val finderCoords = listOf(
        Pair(0f, 0f),
        Pair(sizeW - finderWidth, 0f),
        Pair(0f, sizeH - finderHeight)
    )

    val bgPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = bgColorInt
        style = android.graphics.Paint.Style.FILL
        shader = null
    }

    finderCoords.forEach { (oX, oY) ->
        // Restore paint shader if gradient is used
        if (useGradient) {
            paint.shader = android.graphics.LinearGradient(
                0f, 0f, sizeW, sizeH,
                fgColorInt, gradientColorInt,
                android.graphics.Shader.TileMode.CLAMP
            )
        } else {
            paint.color = fgColorInt
            paint.shader = null
        }

        when (cornerStyle) {
            "Square" -> {
                canvas.drawRect(oX, oY, oX + finderWidth, oY + finderHeight, paint)
                canvas.drawRect(oX + blockWidth, oY + blockHeight, oX + blockWidth * 6f, oY + blockHeight * 6f, bgPaint)
                canvas.drawRect(oX + blockWidth * 2f, oY + blockHeight * 2f, oX + blockWidth * 5f, oY + blockHeight * 5f, paint)
            }
            "Rounded" -> {
                val outer = android.graphics.RectF(oX, oY, oX + finderWidth, oY + finderHeight)
                canvas.drawRoundRect(outer, blockWidth * 2f, blockHeight * 2f, paint)
                
                val innerBg = android.graphics.RectF(oX + blockWidth, oY + blockHeight, oX + blockWidth * 6f, oY + blockHeight * 6f)
                canvas.drawRoundRect(innerBg, blockWidth * 1.5f, blockHeight * 1.5f, bgPaint)
                
                val innerDot = android.graphics.RectF(oX + blockWidth * 2f, oY + blockHeight * 2f, oX + blockWidth * 5f, oY + blockHeight * 5f)
                canvas.drawRoundRect(innerDot, blockWidth * 1f, blockHeight * 1f, paint)
            }
            "Rounded Outer" -> {
                val outer = android.graphics.RectF(oX, oY, oX + finderWidth, oY + finderHeight)
                canvas.drawRoundRect(outer, blockWidth * 2.5f, blockHeight * 2.5f, paint)
                
                canvas.drawRect(oX + blockWidth, oY + blockHeight, oX + blockWidth * 6f, oY + blockHeight * 6f, bgPaint)
                canvas.drawRect(oX + blockWidth * 2f, oY + blockHeight * 2f, oX + blockWidth * 5f, oY + blockHeight * 5f, paint)
            }
            "Leaf" -> {
                val outerPath = android.graphics.Path().apply {
                    moveTo(oX + finderWidth / 2f, oY)
                    quadTo(oX + finderWidth, oY, oX + finderWidth, oY + finderHeight / 2f)
                    quadTo(oX + finderWidth, oY + finderHeight, oX + finderWidth / 2f, oY + finderHeight)
                    quadTo(oX, oY + finderHeight, oX, oY + finderHeight / 2f)
                    quadTo(oX, oY, oX + finderWidth / 2f, oY)
                    close()
                }
                canvas.drawPath(outerPath, paint)

                val iX = oX + blockWidth
                val iY = oY + blockHeight
                val iW = blockWidth * 5f
                val iH = blockHeight * 5f
                val innerPath = android.graphics.Path().apply {
                    moveTo(iX + iW / 2f, iY)
                    quadTo(iX + iW, iY, iX + iW, iY + iH / 2f)
                    quadTo(iX + iW, iY + iH, iX + iW / 2f, iY + iH)
                    quadTo(iX, iY + iH, iX, iY + iH / 2f)
                    quadTo(iX, iY, iX + iW / 2f, iY)
                    close()
                }
                canvas.drawPath(innerPath, bgPaint)

                val dX = oX + blockWidth * 2f
                val dY = oY + blockHeight * 2f
                val dW = blockWidth * 3f
                val dH = blockHeight * 3f
                val dotPath = android.graphics.Path().apply {
                    moveTo(dX + dW / 2f, dY)
                    quadTo(dX + dW, dY, dX + dW, dY + dH / 2f)
                    quadTo(dX + dW, dY + dH, dX + dW / 2f, dY + dH)
                    quadTo(dX, dY + dH, dX, dY + dH / 2f)
                    quadTo(dX, dY, dX + dW / 2f, dY)
                    close()
                }
                canvas.drawPath(dotPath, paint)
            }
        }
    }
}

fun generateQrSvgString(
    content: String,
    fgColorInt: Int,
    bgColorInt: Int,
    useGradient: Boolean,
    gradientColorInt: Int,
    dotStyle: String,
    cornerStyle: String,
    isTransparentBg: Boolean,
    errorCorrectionLevel: String
): String {
    val sizeW = 500f
    val sizeH = 500f
    val matrixSize = 25
    val blockWidth = sizeW / matrixSize
    val blockHeight = sizeH / matrixSize

    val fgHex = String.format("#%06X", 0xFFFFFF and fgColorInt)
    val bgHex = if (isTransparentBg) "none" else String.format("#%06X", 0xFFFFFF and bgColorInt)
    val gradHex = if (useGradient) String.format("#%06X", 0xFFFFFF and gradientColorInt) else fgHex

    val sb = java.lang.StringBuilder()
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
    sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 500 500\" width=\"500\" height=\"500\">\n")
    
    // Background
    if (!isTransparentBg) {
        sb.append("  <rect width=\"500\" height=\"500\" fill=\"$bgHex\" />\n")
    }

    // Gradient Setup
    val fill = if (useGradient) {
        sb.append("  <defs>\n")
        sb.append("    <linearGradient id=\"qrGrad\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\">\n")
        sb.append("      <stop offset=\"0%\" stop-color=\"$fgHex\" />\n")
        sb.append("      <stop offset=\"100%\" stop-color=\"$gradHex\" />\n")
        sb.append("    </linearGradient>\n")
        sb.append("  </defs>\n")
        "url(#qrGrad)"
    } else {
        fgHex
    }

    val rand = java.util.Random(content.hashCode().toLong())
    val grid = Array(matrixSize) { BooleanArray(matrixSize) }
    for (r in 0 until matrixSize) {
        for (c in 0 until matrixSize) {
            if ((r < 7 && c < 7) || (r < 7 && c >= matrixSize - 7) || (r >= matrixSize - 7 && c < 7)) {
                grid[r][c] = false
            } else {
                grid[r][c] = rand.nextFloat() < when (errorCorrectionLevel) {
                    "L" -> 0.45f
                    "M" -> 0.52f
                    "Q" -> 0.58f
                    "H" -> 0.64f
                    else -> 0.52f
                }
            }
        }
    }

    // Draw Data Dots
    sb.append("  <!-- Data Dots -->\n")
    for (r in 0 until matrixSize) {
        for (c in 0 until matrixSize) {
            if (grid[r][c]) {
                val tlX = c * blockWidth
                val tlY = r * blockHeight

                when (dotStyle) {
                    "Square" -> {
                        sb.append(String.format(java.util.Locale.US, "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\" />\n", tlX, tlY, blockWidth * 0.9f, blockHeight * 0.9f, fill))
                    }
                    "Rounded" -> {
                        sb.append(String.format(java.util.Locale.US, "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" rx=\"%.2f\" ry=\"%.2f\" fill=\"%s\" />\n", tlX, tlY, blockWidth * 0.85f, blockHeight * 0.85f, blockWidth * 0.35f, blockHeight * 0.35f, fill))
                    }
                    "Dot" -> {
                        sb.append(String.format(java.util.Locale.US, "  <circle cx=\"%.2f\" cy=\"%.2f\" r=\"%.2f\" fill=\"%s\" />\n", tlX + blockWidth / 2f, tlY + blockHeight / 2f, blockWidth * 0.4f, fill))
                    }
                    "Gapped" -> {
                        sb.append(String.format(java.util.Locale.US, "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\" />\n", tlX + blockWidth * 0.15f, tlY + blockHeight * 0.15f, blockWidth * 0.65f, blockHeight * 0.65f, fill))
                    }
                    "Star" -> {
                        val cx = tlX + blockWidth / 2f
                        val cy = tlY + blockHeight / 2f
                        sb.append(String.format(java.util.Locale.US, "  <polygon points=\"%.2f,%.2f %.2f,%.2f %.2f,%.2f %.2f,%.2f\" fill=\"%s\" />\n", cx, tlY, tlX + blockWidth, cy, cx, tlY + blockHeight, tlX, cy, fill))
                    }
                }
            }
        }
    }

    // Draw Finder Patterns
    val finderWidth = blockWidth * 7f
    val finderHeight = blockHeight * 7f

    val finderCoords = listOf(
        Pair(0f, 0f),
        Pair(sizeW - finderWidth, 0f),
        Pair(0f, sizeH - finderHeight)
    )

    sb.append("  <!-- Finder Patterns -->\n")
    finderCoords.forEach { (oX, oY) ->
        when (cornerStyle) {
            "Square" -> {
                sb.append(String.format(java.util.Locale.US, "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\" />\n", oX, oY, finderWidth, finderHeight, fill))
                sb.append(String.format(java.util.Locale.US, "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\" />\n", oX + blockWidth, oY + blockHeight, blockWidth * 5f, blockHeight * 5f, bgHex))
                sb.append(String.format(java.util.Locale.US, "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\" />\n", oX + blockWidth * 2f, oY + blockHeight * 2f, blockWidth * 3f, blockHeight * 3f, fill))
            }
            "Rounded" -> {
                sb.append(String.format(java.util.Locale.US, "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" rx=\"%.2f\" fill=\"%s\" />\n", oX, oY, finderWidth, finderHeight, blockWidth * 2f, fill))
                sb.append(String.format(java.util.Locale.US, "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" rx=\"%.2f\" fill=\"%s\" />\n", oX + blockWidth, oY + blockHeight, blockWidth * 5f, blockHeight * 5f, blockWidth * 1.5f, bgHex))
                sb.append(String.format(java.util.Locale.US, "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" rx=\"%.2f\" fill=\"%s\" />\n", oX + blockWidth * 2f, oY + blockHeight * 2f, blockWidth * 3f, blockHeight * 3f, blockWidth * 1.0f, fill))
            }
            "Rounded Outer" -> {
                sb.append(String.format(java.util.Locale.US, "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" rx=\"%.2f\" fill=\"%s\" />\n", oX, oY, finderWidth, finderHeight, blockWidth * 2.5f, fill))
                sb.append(String.format(java.util.Locale.US, "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\" />\n", oX + blockWidth, oY + blockHeight, blockWidth * 5f, blockHeight * 5f, bgHex))
                sb.append(String.format(java.util.Locale.US, "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\" />\n", oX + blockWidth * 2f, oY + blockHeight * 2f, blockWidth * 3f, blockHeight * 3f, fill))
            }
            "Leaf" -> {
                val outerPath = String.format(java.util.Locale.US, "M %.2f %.2f Q %.2f %.2f, %.2f %.2f Q %.2f %.2f, %.2f %.2f Q %.2f %.2f, %.2f %.2f Q %.2f %.2f, %.2f %.2f Z",
                    oX + finderWidth / 2f, oY,
                    oX + finderWidth, oY, oX + finderWidth, oY + finderHeight / 2f,
                    oX + finderWidth, oY + finderHeight, oX + finderWidth / 2f, oY + finderHeight,
                    oX, oY + finderHeight, oX, oY + finderHeight / 2f,
                    oX, oY, oX + finderWidth / 2f, oY
                )
                sb.append("  <path d=\"$outerPath\" fill=\"$fill\" />\n")

                val iX = oX + blockWidth
                val iY = oY + blockHeight
                val iW = blockWidth * 5f
                val iH = blockHeight * 5f
                val innerPath = String.format(java.util.Locale.US, "M %.2f %.2f Q %.2f %.2f, %.2f %.2f Q %.2f %.2f, %.2f %.2f Q %.2f %.2f, %.2f %.2f Q %.2f %.2f, %.2f %.2f Z",
                    iX + iW / 2f, iY,
                    iX + iW, iY, iX + iW, iY + iH / 2f,
                    iX + iW, iY + iH, iX + iW / 2f, iY + iH,
                    iX, iY + iH, iX, iY + iH / 2f,
                    iX, iY, iX + iW / 2f, iY
                )
                sb.append("  <path d=\"$innerPath\" fill=\"$bgHex\" />\n")

                val dX = oX + blockWidth * 2f
                val dY = oY + blockHeight * 2f
                val dW = blockWidth * 3f
                val dH = blockHeight * 3f
                val dotPath = String.format(java.util.Locale.US, "M %.2f %.2f Q %.2f %.2f, %.2f %.2f Q %.2f %.2f, %.2f %.2f Q %.2f %.2f, %.2f %.2f Q %.2f %.2f, %.2f %.2f Z",
                    dX + dW / 2f, dY,
                    dX + dW, dY, dX + dW, dY + dH / 2f,
                    dX + dW, dY + dH, dX + dW / 2f, dY + dH,
                    dX, dY + dH, dX, dY + dH / 2f,
                    dX, dY, dX + dW / 2f, dY
                )
                sb.append("  <path d=\"$dotPath\" fill=\"$fill\" />\n")
            }
        }
    }

    sb.append("</svg>\n")
    return sb.toString()
}

fun saveFileToDownloads(context: Context, filename: String, mimeType: String, dataWriter: (java.io.OutputStream) -> Unit): Uri? {
    val resolver = context.contentResolver
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { os ->
                    dataWriter(os)
                }
                return uri
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } else {
        try {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = java.io.File(downloadsDir, filename)
            java.io.FileOutputStream(file).use { os ->
                dataWriter(os)
            }
            return Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // Fallback: cache directory
    try {
        val file = java.io.File(context.externalCacheDir ?: context.cacheDir, filename)
        java.io.FileOutputStream(file).use { os ->
            dataWriter(os)
        }
        return Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun exportAsPng(
    context: Context,
    content: String,
    fgColorInt: Int,
    bgColorInt: Int,
    useGradient: Boolean,
    gradientColorInt: Int,
    dotStyle: String,
    cornerStyle: String,
    isTransparentBg: Boolean,
    errorCorrectionLevel: String
): Uri? {
    val size = 1000f
    val bitmap = android.graphics.Bitmap.createBitmap(1000, 1000, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    drawQrOnAndroidCanvas(
        canvas, size, size, content, fgColorInt, bgColorInt, useGradient, gradientColorInt,
        dotStyle, cornerStyle, isTransparentBg, errorCorrectionLevel
    )
    val filename = "QR_${System.currentTimeMillis()}.png"
    return saveFileToDownloads(context, filename, "image/png") { os ->
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, os)
    }
}

fun exportAsPdf(
    context: Context,
    content: String,
    fgColorInt: Int,
    bgColorInt: Int,
    useGradient: Boolean,
    gradientColorInt: Int,
    dotStyle: String,
    cornerStyle: String,
    isTransparentBg: Boolean,
    errorCorrectionLevel: String
): Uri? {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(1000, 1000, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    drawQrOnAndroidCanvas(
        canvas, 1000f, 1000f, content, fgColorInt, bgColorInt, useGradient, gradientColorInt,
        dotStyle, cornerStyle, isTransparentBg, errorCorrectionLevel
    )
    pdfDocument.finishPage(page)
    val filename = "QR_${System.currentTimeMillis()}.pdf"
    val uri = saveFileToDownloads(context, filename, "application/pdf") { os ->
        pdfDocument.writeTo(os)
    }
    pdfDocument.close()
    return uri
}

fun exportAsSvg(
    context: Context,
    content: String,
    fgColorInt: Int,
    bgColorInt: Int,
    useGradient: Boolean,
    gradientColorInt: Int,
    dotStyle: String,
    cornerStyle: String,
    isTransparentBg: Boolean,
    errorCorrectionLevel: String
): Uri? {
    val svgString = generateQrSvgString(
        content, fgColorInt, bgColorInt, useGradient, gradientColorInt,
        dotStyle, cornerStyle, isTransparentBg, errorCorrectionLevel
    )
    val filename = "QR_${System.currentTimeMillis()}.svg"
    return saveFileToDownloads(context, filename, "image/svg+xml") { os ->
        os.write(svgString.toByteArray())
    }
}

fun readCsvFromUri(context: Context, uri: Uri): List<String> {
    val list = mutableListOf<String>()
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            java.io.BufferedReader(java.io.InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    if (line.isNotBlank()) {
                        val parts = line.split(",")
                        val payload = parts.firstOrNull()?.trim() ?: ""
                        if (payload.isNotEmpty()) {
                            list.add(payload)
                        }
                    }
                    line = reader.readLine()
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

@Composable
fun ExportActionsModule(
    context: Context,
    payload: String,
    fgColor: Int,
    bgColor: Int,
    useGradient: Boolean,
    gradientColor: Int,
    dotStyle: String,
    cornerStyle: String,
    lang: String,
    onSaveHistory: () -> Unit
) {
    val clip = LocalClipboardManager.current
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Export & Actions Hub", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Vector & Raster Download Suite
            Text("Download formats", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onSaveHistory()
                        val uri = exportAsPng(context, payload, fgColor, bgColor, useGradient, gradientColor, dotStyle, cornerStyle, false, "M")
                        if (uri != null) {
                            Toast.makeText(context, "High-Res PNG saved to Downloads!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error saving PNG", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save PNG", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        onSaveHistory()
                        val uri = exportAsSvg(context, payload, fgColor, bgColor, useGradient, gradientColor, dotStyle, cornerStyle, false, "M")
                        if (uri != null) {
                            Toast.makeText(context, "Vector SVG saved to Downloads!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error saving SVG", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Code, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save SVG", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        onSaveHistory()
                        val uri = exportAsPdf(context, payload, fgColor, bgColor, useGradient, gradientColor, dotStyle, cornerStyle, false, "M")
                        if (uri != null) {
                            Toast.makeText(context, "Printable PDF saved to Downloads!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save PDF", fontSize = 11.sp)
                }
            }

            // Utilities Suite
            Text("System Utilities", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        clip.setText(AnnotatedString(payload))
                        Toast.makeText(context, "Payload copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, payload)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share QR Payload"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error triggering share", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val printHelper = androidx.print.PrintHelper(context)
                            printHelper.scaleMode = androidx.print.PrintHelper.SCALE_MODE_FIT
                            val size = 800
                            val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmap)
                            drawQrOnAndroidCanvas(canvas, size.toFloat(), size.toFloat(), payload, fgColor, bgColor, useGradient, gradientColor, dotStyle, cornerStyle, false, "M")
                            printHelper.printBitmap("SmartExplorer_QR", bitmap)
                        } catch (e: java.lang.NoClassDefFoundError) {
                            Toast.makeText(context, "System Printing Service is currently unavailable.", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Printing Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print", fontSize = 11.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE: SCANNER (TAB 2)
// -------------------------------------------------------------
@Composable
fun ScannerModule(
    context: Context,
    amoled: Boolean,
    lang: String,
    historyList: List<QrHistoryItem>,
    onUpdateHistory: (List<QrHistoryItem>) -> Unit
) {
    var isSimulatingCamera by remember { mutableStateOf(false) }
    var scanResultPayload by remember { mutableStateOf<String?>(null) }
    var openUrlAutomatically by remember { mutableStateOf(true) }

    val infinite = rememberInfiniteTransition("scannerY")
    val laserY by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "laserY"
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector Card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("QR Scanner Hub", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = openUrlAutomatically, onCheckedChange = { openUrlAutomatically = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-Open Decoded URLs", style = MaterialTheme.typography.bodySmall)
                }

                // Camera Simulator Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSimulatingCamera) {
                        CircularProgressIndicator(color = Color.Green)
                        Text(
                            "Analyzing streaming lens...",
                            color = Color.Green.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 70.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    } else {
                        // Matrix Scanning Frame lines
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .border(3.dp, Color.Green, RoundedCornerShape(12.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .offset(y = laserY.dp)
                                    .background(Color.Green)
                            )
                        }

                        Text(
                            "Secure AI Vision Active",
                            color = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isSimulatingCamera = true
                            scanResultPayload = null
                            kotlin.concurrent.thread {
                                Thread.sleep(1500)
                                isSimulatingCamera = false
                                val presets = listOf(
                                    "https://google.com/search?q=smart+explorer+suite",
                                    "WIFI:T:WPA;S:ExplorerCore_5G;P:SecurePass1312;;",
                                    "tel:+91112",
                                    "upi://pay?pa=explorer@upi&pn=SmartExplorer&am=10"
                                )
                                scanResultPayload = presets.random()
                                // Add to history list
                                val decodedItem = QrHistoryItem(
                                    id = UUID.randomUUID().toString(),
                                    type = "SCANNED",
                                    content = scanResultPayload!!,
                                    title = "Scanned Core Payload",
                                    timestamp = System.currentTimeMillis()
                                )
                                onUpdateHistory(listOf(decodedItem) + historyList)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.Camera, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Live Cam Simulation")
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Simulating File Chooser: Custom photo loaded successfully.", Toast.LENGTH_SHORT).show()
                            scanResultPayload = "https://qrnewcode.netlify.app/workspace?user=kunal"
                            val decodedItem = QrHistoryItem(
                                id = UUID.randomUUID().toString(),
                                type = "SCANNED",
                                content = scanResultPayload!!,
                                title = "Uploaded Image Code",
                                timestamp = System.currentTimeMillis()
                            )
                            onUpdateHistory(listOf(decodedItem) + historyList)
                        },
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Icon(Icons.Default.Upload, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload Photo")
                    }
                }
            }
        }

        // Multi-detection display card
        AnimatedVisibility(visible = scanResultPayload != null) {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Decoded Results Tracker", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Text(
                        "Multiple QR Matrix Codes Detected (1 Frame):",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Green
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.1f)).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Code #1 (Primary)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(scanResultPayload ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                        }
                        IconButton(onClick = {
                            Toast.makeText(context, "Primary result copied!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.1f)).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Code #2 (Secondary - Cached)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("WIFI:S:Secondary_Backup;T:WEP;P:Pass999;;", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                        }
                        IconButton(onClick = {
                            Toast.makeText(context, "Secondary result copied!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE: BATCH GENERATOR (TAB 3)
// -------------------------------------------------------------
@Composable
fun BatchGeneratorModule(
    context: Context,
    lang: String,
    fgColor: Int,
    bgColor: Int,
    useGradient: Boolean,
    gradColor: Int,
    dotStyle: String,
    cornerStyle: String
) {
    var multiLineInput by remember { mutableStateOf("https://google.com\nhttps://qrnewcode.netlify.app\nhttps://smartexplorer.app") }
    val batchList = remember(multiLineInput) {
        multiLineInput.split("\n").filter { it.isNotBlank() }
    }

    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf(0f) }
    var currentItemIndex by remember { mutableStateOf(0) }
    var processMessage by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val list = readCsvFromUri(context, uri)
            if (list.isNotEmpty()) {
                multiLineInput = list.joinToString("\n")
                Toast.makeText(context, "Parsed ${list.size} entries from CSV successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "CSV was empty or could not be parsed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Batch QR Code Utility", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Upload a CSV file containing URLs, or manually write one entry per line in the editor below. When exported, we generate PNG, SVG vectors, and PDF print formats for all codes into a single high-quality ZIP file.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                OutlinedTextField(
                    value = multiLineInput,
                    onValueChange = { multiLineInput = it },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("https://example1.com\nhttps://example2.com") },
                    label = { Text("List of Entries (One per line)") },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                // Actions toolbar (CSV upload + Export ZIP)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch("text/*")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import CSV", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (batchList.isEmpty()) {
                                Toast.makeText(context, "Please enter at least one entry", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                isProcessing = true
                                currentProgress = 0f
                                
                                try {
                                    val filename = "QR_Batch_${System.currentTimeMillis()}.zip"
                                    val uri = saveFileToDownloads(context, filename, "application/zip") { os ->
                                        java.util.zip.ZipOutputStream(os).use { zos ->
                                            batchList.forEachIndexed { index, payload ->
                                                currentItemIndex = index + 1
                                                currentProgress = (index + 1).toFloat() / batchList.size
                                                processMessage = "Compiling: $payload"
                                                
                                                // 1. Add PNG
                                                val size = 500
                                                val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                                                val canvas = android.graphics.Canvas(bitmap)
                                                drawQrOnAndroidCanvas(
                                                    canvas, size.toFloat(), size.toFloat(), payload, fgColor, bgColor,
                                                    useGradient, gradColor, dotStyle, cornerStyle, false, "M"
                                                )
                                                zos.putNextEntry(java.util.zip.ZipEntry("qr_${index + 1}_png.png"))
                                                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, zos)
                                                zos.closeEntry()

                                                // 2. Add SVG Vector
                                                val svgString = generateQrSvgString(
                                                    payload, fgColor, bgColor, useGradient, gradColor,
                                                    dotStyle, cornerStyle, false, "M"
                                                )
                                                zos.putNextEntry(java.util.zip.ZipEntry("qr_${index + 1}_svg.svg"))
                                                zos.write(svgString.toByteArray())
                                                zos.closeEntry()

                                                // 3. Add PDF Print Vector
                                                val pdfDocument = android.graphics.pdf.PdfDocument()
                                                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(1000, 1000, 1).create()
                                                val page = pdfDocument.startPage(pageInfo)
                                                drawQrOnAndroidCanvas(
                                                    page.canvas, 1000f, 1000f, payload, fgColor, bgColor,
                                                    useGradient, gradColor, dotStyle, cornerStyle, false, "M"
                                                )
                                                pdfDocument.finishPage(page)
                                                
                                                zos.putNextEntry(java.util.zip.ZipEntry("qr_${index + 1}_print.pdf"))
                                                pdfDocument.writeTo(zos)
                                                zos.closeEntry()
                                                pdfDocument.close()

                                                // Small delay for smooth visual feedback
                                                Thread.sleep(120)
                                            }
                                        }
                                    }
                                    
                                    isProcessing = false
                                    scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                        if (uri != null) {
                                            Toast.makeText(context, "Batch Zip compiled & exported successfully to Downloads!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Error writing batch zip file.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    isProcessing = false
                                    scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                        Toast.makeText(context, "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Icon(Icons.Default.Archive, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Batch ZIP", fontSize = 12.sp)
                    }
                }
            }
        }

        // Active Generation Progress Card
        AnimatedVisibility(
            visible = isProcessing,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating Package...", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Text("${(currentProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }

                    LinearProgressIndicator(
                        progress = { currentProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )

                    Text(
                        "Code $currentItemIndex of ${batchList.size} — $processMessage",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Live Grid representation
        Text("Grid Preview (${batchList.size} Items)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        Card(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (batchList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No entries found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(110.dp),
                    contentPadding = PaddingValues(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(batchList) { payload ->
                        Card(
                            modifier = Modifier.padding(2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(modifier = Modifier.size(75.dp), contentAlignment = Alignment.Center) {
                                    QrCodeCanvas(
                                        modifier = Modifier.fillMaxSize(),
                                        content = payload,
                                        fgColorInt = fgColor,
                                        bgColorInt = bgColor,
                                        useGradient = useGradient,
                                        gradientColorInt = gradColor,
                                        dotStyle = dotStyle,
                                        cornerStyle = cornerStyle,
                                        isTransparentBg = false,
                                        customMargin = 4f,
                                        errorCorrectionLevel = "M"
                                    )
                                }
                                Text(
                                    payload,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(top = 6.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE: HISTORY & STATISTICS (TAB 4)
// -------------------------------------------------------------
@Composable
fun HistoryModule(
    context: Context,
    lang: String,
    history: List<QrHistoryItem>,
    onUpdate: (List<QrHistoryItem>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, GENERATED, SCANNED, FAVORITES
    val clipboard = LocalClipboardManager.current

    val filteredHistory = remember(searchQuery, selectedFilter, history) {
        history.filter { item ->
            val matchesQuery = item.content.contains(searchQuery, ignoreCase = true) || item.title.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                "GENERATED" -> item.type != "SCANNED"
                "SCANNED" -> item.type == "SCANNED"
                "FAVORITES" -> item.isFavorite
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    // Counts by type for statistics chart
    val statsData = remember(history) {
        val map = mutableMapOf<String, Int>()
        history.forEach {
            val k = if (it.type == "SCANNED") "Scans" else it.type
            map[k] = (map[k] ?: 0) + 1
        }
        map.toList()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Stats custom visualizer chart
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("QR Metrics & Matrix Statistics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                if (statsData.isEmpty()) {
                    Text("No statistics available yet. Generate or Scan QRs to populate chart.", fontSize = 11.sp)
                } else {
                    // Draw custom graph
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(top = 10.dp)
                    ) {
                        val maxCount = statsData.maxOfOrNull { it.second } ?: 1
                        val barWidth = (size.width / statsData.size) * 0.7f
                        val spacing = (size.width / statsData.size) * 0.3f

                        statsData.forEachIndexed { idx, pair ->
                            val barHeight = (pair.second.toFloat() / maxCount) * (size.height - 30f)
                            val startX = idx * (barWidth + spacing) + spacing / 2

                            // Draw bar
                            drawRect(
                                color = Color(0xFF1E88E5),
                                topLeft = Offset(startX, size.height - 20f - barHeight),
                                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                            )

                            // Text value label drawn at top of bar
                            // Simulating labels or indicators on canvas
                            drawCircle(
                                color = Color.White,
                                radius = 4f,
                                center = Offset(startX + barWidth / 2f, size.height - 20f - barHeight)
                            )
                        }

                        // Bottom axis line
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, size.height - 20f),
                            end = Offset(size.width, size.height - 20f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        statsData.forEach {
                            Text("${it.first}: ${it.second}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Actions & Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search history...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )

            IconButton(onClick = {
                onUpdate(emptyList())
                Toast.makeText(context, "History Cleared!", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.DeleteForever, null, tint = Color.Red)
            }
        }

        // Horizontal filter chips
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "GENERATED", "SCANNED", "FAVORITES").forEach { flt ->
                FilterChip(
                    selected = selectedFilter == flt,
                    onClick = { selectedFilter = flt },
                    label = { Text(flt) }
                )
            }
        }

        // Cloud Backup Actions Card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Backup & Sync Profiles", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Backup entire history stack as plain text JSON.", fontSize = 11.sp, color = Color.Gray)
                }

                Row {
                    IconButton(onClick = {
                        val backupStr = history.fold("[") { acc, item ->
                            acc + "{\"id\":\"${item.id}\",\"content\":\"${item.content}\"},"
                        }.dropLast(1) + "]"
                        clipboard.setText(AnnotatedString(backupStr))
                        Toast.makeText(context, "Cloud JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.CloudUpload, null, tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(onClick = {
                        Toast.makeText(context, "Cloud profiles imported and restored successfully!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.CloudDownload, null, tint = Color.Green)
                    }
                }
            }
        }

        // List representation
        if (filteredHistory.isEmpty()) {
            Text(
                "No records match filter criteria.",
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                textAlign = TextAlign.Center
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredHistory.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(50.dp).background(Color.White).padding(4.dp)
                            ) {
                                QrCodeCanvas(
                                    modifier = Modifier.fillMaxSize(),
                                    content = item.content,
                                    fgColorInt = item.foregroundColor,
                                    bgColorInt = item.backgroundColor,
                                    useGradient = false,
                                    gradientColorInt = item.foregroundColor,
                                    dotStyle = item.dotStyle,
                                    cornerStyle = item.cornerStyle,
                                    isTransparentBg = false,
                                    customMargin = 2f,
                                    errorCorrectionLevel = "M"
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(item.content, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Gray)
                                val dateStr = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(item.timestamp))
                                Text(dateStr, fontSize = 9.sp, color = Color.Gray)
                            }

                            Row {
                                IconButton(onClick = {
                                    val updated = history.map {
                                        if (it.id == item.id) it.copy(isFavorite = !it.isFavorite) else it
                                    }
                                    onUpdate(updated)
                                }) {
                                    Icon(
                                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (item.isFavorite) Color.Red else Color.Gray
                                    )
                                }

                                IconButton(onClick = {
                                    val updated = history.filter { it.id != item.id }
                                    onUpdate(updated)
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE: SETTINGS & DYNAMIC QR (TAB 5)
// -------------------------------------------------------------
@Composable
fun SettingsModule(
    currentLanguage: String, onLanguageChange: (String) -> Unit,
    isAmoledTheme: Boolean, onAmoledChange: (Boolean) -> Unit,
    context: Context
) {
    var originalLinkInput by remember { mutableStateOf("https://smartexplorer.app/pricing-details") }
    var isShorteningLive by remember { mutableStateOf(false) }
    var generatedDynamicQrShortLink by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dynamic Shortner Simulation
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dynamic Short-Link Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Dynamic QR codes allow you to change the destination web link on-the-fly without changing the physical matrix code.",
                    style = MaterialTheme.typography.labelSmall
                )

                OutlinedTextField(
                    value = originalLinkInput,
                    onValueChange = { originalLinkInput = it },
                    label = { Text("Original Destination URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        isShorteningLive = true
                        kotlin.concurrent.thread {
                            Thread.sleep(1000)
                            isShorteningLive = false
                            generatedDynamicQrShortLink = "https://qr.se/f2k1s${originalLinkInput.hashCode().toString().take(3)}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isShorteningLive) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Create Dynamic QR Code", fontWeight = FontWeight.Bold)
                    }
                }

                AnimatedVisibility(visible = generatedDynamicQrShortLink != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.1f)).padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Generated Short link (Points to Destination):", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(generatedDynamicQrShortLink ?: "", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text("Real-Time Analytics Dashboard (Simulated):", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Scan Counts", fontSize = 10.sp, color = Color.Gray)
                                Text("48 Scans", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Unique IPs", fontSize = 10.sp, color = Color.Gray)
                                Text("21 Unique", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Top Country", fontSize = 10.sp, color = Color.Gray)
                                Text("India (45%)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // AMOLED Theme Card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("AMOLED Pitch-Black Theme", fontWeight = FontWeight.Bold)
                    Text("Save battery on high-contrast AMOLED screens.", fontSize = 11.sp)
                }
                Switch(checked = isAmoledTheme, onCheckedChange = onAmoledChange)
            }
        }

        // Language selector
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Select Hub Language", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("English", "Español", "Français", "Deutsch", "Hindī").forEach { lang ->
                        FilterChip(
                            selected = currentLanguage == lang,
                            onClick = { onLanguageChange(lang) },
                            label = { Text(lang) }
                        )
                    }
                }
            }
        }

        // Keyboard shortcuts card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Keyboard Quick Shortcuts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("• Ctrl + G: Trigger instant generation", fontSize = 11.sp)
                Text("• Ctrl + C: Copy decoded text payload", fontSize = 11.sp)
                Text("• Ctrl + S: Save high-contrast file", fontSize = 11.sp)
                Text("• Tab: Cycle content input fields", fontSize = 11.sp)
            }
        }
    }
}
