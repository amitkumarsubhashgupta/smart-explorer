package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

interface LocalizedStrings {
    val appName: String
    val home: String
    val favorites: String
    val settings: String
    val profile: String
    val search: String
    val offlineMode: String
    val editProfile: String
    val referralCode: String
    val copyCode: String
    val points: String
    val switchLanguage: String
    val languageChanged: String
    val logout: String
    val save: String
    val cancel: String
    val welcomeBack: String
    val offlineWarning: String
}

object EnglishStrings : LocalizedStrings {
    override val appName = "Smart Explorer"
    override val home = "Home"
    override val favorites = "Favorites"
    override val settings = "Settings"
    override val profile = "Profile"
    override val search = "Search"
    override val offlineMode = "Offline Mode"
    override val editProfile = "Edit Profile"
    override val referralCode = "Referral Code"
    override val copyCode = "Copy Code"
    override val points = "Points"
    override val switchLanguage = "Switch Language"
    override val languageChanged = "Language changed successfully"
    override val logout = "Sign Out"
    override val save = "Save"
    override val cancel = "Cancel"
    override val welcomeBack = "Welcome back, Explorer!"
    override val offlineWarning = "Some search, news, and cloud features are limited in offline mode."
}

object SpanishStrings : LocalizedStrings {
    override val appName = "Smart Explorer"
    override val home = "Inicio"
    override val favorites = "Favoritos"
    override val settings = "Ajustes"
    override val profile = "Perfil"
    override val search = "Buscar"
    override val offlineMode = "Modo sin conexión"
    override val editProfile = "Editar Perfil"
    override val referralCode = "Código de Referido"
    override val copyCode = "Copiar Código"
    override val points = "Puntos"
    override val switchLanguage = "Cambiar Idioma"
    override val languageChanged = "Idioma cambiado con éxito"
    override val logout = "Cerrar sesión"
    override val save = "Guardar"
    override val cancel = "Cancelar"
    override val welcomeBack = "¡Bienvenido de nuevo, Explorador!"
    override val offlineWarning = "Algunas funciones de búsqueda, noticias y nube están limitadas sin conexión."
}

object FrenchStrings : LocalizedStrings {
    override val appName = "Smart Explorer"
    override val home = "Accueil"
    override val favorites = "Favoris"
    override val settings = "Paramètres"
    override val profile = "Profil"
    override val search = "Rechercher"
    override val offlineMode = "Mode hors ligne"
    override val editProfile = "Modifier le profil"
    override val referralCode = "Code de parrainage"
    override val copyCode = "Copier le code"
    override val points = "Points"
    override val switchLanguage = "Changer de langue"
    override val languageChanged = "Langue changée avec succès"
    override val logout = "Se déconnecter"
    override val save = "Enregistrer"
    override val cancel = "Annuler"
    override val welcomeBack = "Bon retour, Explorateur !"
    override val offlineWarning = "Certaines fonctionnalités de recherche, d'actualités et de cloud sont limitées hors ligne."
}

object GermanStrings : LocalizedStrings {
    override val appName = "Smart Explorer"
    override val home = "Startseite"
    override val favorites = "Favoriten"
    override val settings = "Einstellungen"
    override val profile = "Profil"
    override val search = "Suche"
    override val offlineMode = "Offline-Modus"
    override val editProfile = "Profil bearbeiten"
    override val referralCode = "Empfehlungscode"
    override val copyCode = "Code kopieren"
    override val points = "Punkte"
    override val switchLanguage = "Sprache wechseln"
    override val languageChanged = "Sprache erfolgreich geändert"
    override val logout = "Abmelden"
    override val save = "Speichern"
    override val cancel = "Abbrechen"
    override val welcomeBack = "Willkommen zurück, Explorer!"
    override val offlineWarning = "Einige Such-, Nachrichten- und Cloud-Funktionen sind im Offline-Modus eingeschränkt."
}

object JapaneseStrings : LocalizedStrings {
    override val appName = "スマートエクスプローラー"
    override val home = "ホーム"
    override val favorites = "お気に入り"
    override val settings = "設定"
    override val profile = "プロフィール"
    override val search = "検索"
    override val offlineMode = "オフラインモード"
    override val editProfile = "プロフィール編集"
    override val referralCode = "紹介コード"
    override val copyCode = "コードをコピー"
    override val points = "ポイント"
    override val switchLanguage = "言語切り替え"
    override val languageChanged = "言語が正常に変更されました"
    override val logout = "サインアウト"
    override val save = "保存"
    override val cancel = "キャンセル"
    override val welcomeBack = "お帰りなさい、エクスプローラー！"
    override val offlineWarning = "オフラインモードでは、一部の検索、ニュース、およびクラウド機能が制限されます。"
}

object HindiStrings : LocalizedStrings {
    override val appName = "स्मार्ट एक्सप्लोरर"
    override val home = "होम"
    override val favorites = "पसंदीदा"
    override val settings = "सेटिंग्स"
    override val profile = "प्रोफ़ाइल"
    override val search = "खोजें"
    override val offlineMode = "ऑफ़लाइन मोड"
    override val editProfile = "प्रोफ़ाइल संपादित करें"
    override val referralCode = "रेफरल कोड"
    override val copyCode = "कोड कॉपी करें"
    override val points = "अंक"
    override val switchLanguage = "भाषा बदलें"
    override val languageChanged = "भाषा सफलतापूर्वक बदली गई"
    override val logout = "साइन आउट"
    override val save = "सहेजें"
    override val cancel = "रद्द करें"
    override val welcomeBack = "वापसी पर स्वागत है, एक्सप्लोरर!"
    override val offlineWarning = "ऑफ़लाइन मोड में कुछ खोज, समाचार और क्लाउड सुविधाएँ सीमित हैं।"
}

val LocalAppStrings = staticCompositionLocalOf<LocalizedStrings> { EnglishStrings }

object AppStrings {
    val current: LocalizedStrings
        @Composable
        @ReadOnlyComposable
        get() = LocalAppStrings.current
}
