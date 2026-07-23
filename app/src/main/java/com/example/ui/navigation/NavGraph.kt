package com.example.ui.navigation

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import com.example.ui.screens.*
import com.example.ui.screens.tools.*
import com.example.ui.screens.profile.*
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ExplorerViewModel
import com.example.ui.viewmodel.ReferralViewModel
import com.example.SmartExplorerApplication
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

enum class Screen(val route: String) {
    SPLASH("splash"),
    ONBOARDING("onboarding"),
    LOGIN("login"),
    VERIFY_EMAIL("verify_email"),
    HOME("home"),
    SEARCH("search"),
    AI("ai"),
    FAVORITES("favorites"),
    PROFILE("profile"),
    SETTINGS("settings"),
    WEATHER("weather"),
    COUNTRIES("countries"),
    NEWS("news"),
    JOKES("jokes"),
    CURRENCY("currency"),
    TRANSLATOR("translator"),
    DICTIONARY("dictionary"),
    QR_GEN("qr_gen"),
    QR_SCAN("qr_scan"),
    CALCULATOR("calculator"),
    WORLD_CLOCK("world_clock"),
    INTERNET_SPEED("internet_speed"),
    EXPLORER_MAP("explorer_map"),
    VAULT_NOTES("vault_notes"),
    CRYPTO("crypto_ticker"),
    COMMODITY("commodity_prices"),
    FACTS("random_facts"),
    EMERGENCY("emergency_desk"),
    ABOUT_US("about_us"),
    CONTACT_US("contact_us"),
    PRIVACY_POLICY("privacy_policy"),
    TERMS_CONDITIONS("terms_conditions"),
    PROFILE_SETTINGS("profile_settings"),
    PROFILE_PRIVACY("profile_privacy"),
    PROFILE_NOTIFICATIONS("profile_notifications"),
    PROFILE_LANGUAGE("profile_language"),
    PROFILE_APPEARANCE("profile_appearance"),
    PROFILE_SUPPORT("profile_support"),
    PROFILE_ABOUT("profile_about"),
    PROFILE_DELETE_ACCOUNT("profile_delete_account"),
    REFER("refer"),
    ADMIN_DASHBOARD("admin_dashboard"),
    HELP_SUPPORT("help_support"),
    FAQ("faq"),
    APP_VERSION("app_version"),
    OPEN_SOURCE_LICENSES("open_source_licenses")
}

@Composable
fun SmartExplorerNavGraph(
    authViewModel: AuthViewModel,
    explorerViewModel: ExplorerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appContainer = remember(context) { (context.applicationContext as SmartExplorerApplication).container }
    val referralViewModel: ReferralViewModel = viewModel(
        factory = ReferralViewModel.provideFactory(appContainer.referralRepository, appContainer.authRepository)
    )

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if bottom bar should be displayed
    val displayBottomBar = remember(currentRoute) {
        currentRoute in listOf(
            Screen.HOME.route,
            Screen.SEARCH.route,
            Screen.AI.route,
            Screen.FAVORITES.route,
            Screen.PROFILE.route
        )
    }

    val userProfile by authViewModel.currentUser.collectAsState()
    val isBiometricLockPrefEnabled by explorerViewModel.biometricLockState.collectAsState()
    var isAppUnlocked by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userProfile) {
        if (userProfile == null) {
            isAppUnlocked = false
        }
    }

    val shouldShowLockScreen = userProfile != null && 
            isBiometricLockPrefEnabled && 
            !isAppUnlocked && 
            currentRoute != null && 
            currentRoute != Screen.SPLASH.route && 
            currentRoute != Screen.ONBOARDING.route && 
            currentRoute != Screen.LOGIN.route && 
            currentRoute != Screen.VERIFY_EMAIL.route

    LaunchedEffect(shouldShowLockScreen) {
        if (shouldShowLockScreen) {
            triggerBiometricAuth(context) { success ->
                if (success) {
                    isAppUnlocked = true
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Smart Explorer",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Application Modules",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Cloud, contentDescription = null) },
                    label = { Text("Weather Forecast") },
                    selected = currentRoute == Screen.WEATHER.route,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Screen.WEATHER.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Feed, contentDescription = null) },
                    label = { Text("News Feed") },
                    selected = currentRoute == Screen.NEWS.route,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Screen.NEWS.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Translate, contentDescription = null) },
                    label = { Text("AI Translator") },
                    selected = currentRoute == Screen.TRANSLATOR.route,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Screen.TRANSLATOR.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Public, contentDescription = null) },
                    label = { Text("Country Facts") },
                    selected = currentRoute == Screen.COUNTRIES.route,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Screen.COUNTRIES.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (displayBottomBar) {
                NavigationBar(
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Screen.HOME.route,
                        onClick = {
                            navController.navigate(Screen.HOME.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = currentRoute == Screen.SEARCH.route,
                        onClick = {
                            navController.navigate(Screen.SEARCH.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Explore, contentDescription = "Explore") },
                        label = { Text("Explore") },
                        modifier = Modifier.testTag("nav_item_search")
                    )

                    NavigationBarItem(
                        selected = currentRoute == Screen.AI.route,
                        onClick = {
                            navController.navigate(Screen.AI.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Core") },
                        label = { Text("AI") },
                        modifier = Modifier.testTag("nav_item_ai")
                    )

                    NavigationBarItem(
                        selected = currentRoute == Screen.FAVORITES.route,
                        onClick = {
                            navController.navigate(Screen.FAVORITES.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Saved") },
                        label = { Text("Saved") },
                        modifier = Modifier.testTag("nav_item_favorites")
                    )

                    NavigationBarItem(
                        selected = currentRoute == Screen.PROFILE.route,
                        onClick = {
                            navController.navigate(Screen.PROFILE.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        modifier = Modifier.testTag("nav_item_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.SPLASH.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // SPLASH SCREEN
            composable(Screen.SPLASH.route) {
                LaunchedEffect(Unit) { explorerViewModel.logScreenView("Splash") }
                SplashScreen(
                    onSplashFinished = {
                        val user = authViewModel.currentUser.value
                        if (user != null) {
                            if (user.isGuest || user.loginProvider == "google" || user.emailVerified) {
                                navController.navigate(Screen.HOME.route) {
                                    popUpTo(Screen.SPLASH.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.VERIFY_EMAIL.route) {
                                    popUpTo(Screen.SPLASH.route) { inclusive = true }
                                }
                            }
                        } else {
                            navController.navigate(Screen.ONBOARDING.route) {
                                popUpTo(Screen.SPLASH.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // ONBOARDING
            composable(Screen.ONBOARDING.route) {
                LaunchedEffect(Unit) { explorerViewModel.logScreenView("Onboarding") }
                OnboardingScreen(
                    onFinished = {
                        explorerViewModel.logOnboardingComplete()
                        navController.navigate(Screen.LOGIN.route) {
                            popUpTo(Screen.ONBOARDING.route) { inclusive = true }
                        }
                    }
                )
            }

            // LOGIN
            composable(Screen.LOGIN.route) {
                LaunchedEffect(Unit) { explorerViewModel.logScreenView("Login") }
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        val user = authViewModel.currentUser.value
                        if (user != null && (user.isGuest || user.loginProvider == "google" || user.emailVerified)) {
                            navController.navigate(Screen.HOME.route) {
                                popUpTo(Screen.LOGIN.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.VERIFY_EMAIL.route) {
                                popUpTo(Screen.LOGIN.route) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToPrivacyPolicy = { navController.navigate(Screen.PRIVACY_POLICY.route) },
                    onNavigateToTermsConditions = { navController.navigate(Screen.TERMS_CONDITIONS.route) },
                    onNavigateToAppVersion = { navController.navigate(Screen.APP_VERSION.route) }
                )
            }

            // VERIFY EMAIL
            composable(Screen.VERIFY_EMAIL.route) {
                com.example.ui.screens.VerifyEmailScreen(
                    viewModel = authViewModel,
                    onVerificationSuccess = {
                        navController.navigate(Screen.HOME.route) {
                            popUpTo(Screen.VERIFY_EMAIL.route) { inclusive = true }
                        }
                    },
                    onNavigateBackToLogin = {
                        navController.navigate(Screen.LOGIN.route) {
                            popUpTo(Screen.VERIFY_EMAIL.route) { inclusive = true }
                        }
                    }
                )
            }

            // HOME
            composable(
                route = Screen.HOME.route,
                deepLinks = listOf(
                    navDeepLink { uriPattern = "smartexplorer://home" },
                    navDeepLink { uriPattern = "smartexplorer://features" }
                )
            ) {
                LaunchedEffect(Unit) { explorerViewModel.logScreenView("Home") }
                HomeScreen(
                    authViewModel = authViewModel,
                    explorerViewModel = explorerViewModel,
                    onFeatureSelected = { route ->
                        explorerViewModel.logFeatureCardUsage(route)
                        navController.navigate(route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.SETTINGS.route)
                    },
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    }
                )
            }

            // SEARCH
            composable(Screen.SEARCH.route) {
                LaunchedEffect(Unit) { explorerViewModel.logScreenView("Search") }
                SearchScreen(
                    viewModel = explorerViewModel
                )
            }

            // AI
            composable(Screen.AI.route) {
                AIScreen()
            }

            // FAVORITES
            composable(Screen.FAVORITES.route) {
                LaunchedEffect(Unit) { explorerViewModel.logScreenView("Favorites") }
                FavoritesScreen(
                    viewModel = explorerViewModel
                )
            }

            // PROFILE
            composable(
                route = Screen.PROFILE.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) }
            ) {
                LaunchedEffect(Unit) { explorerViewModel.logScreenView("Profile") }
                ProfileScreen(
                    authViewModel = authViewModel,
                    explorerViewModel = explorerViewModel,
                    referralViewModel = referralViewModel,
                    onSignOutComplete = {
                        navController.navigate(Screen.LOGIN.route) {
                            popUpTo(Screen.HOME.route) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = { navController.navigate(Screen.PROFILE_SETTINGS.route) },
                    onNavigateToPrivacy = { navController.navigate(Screen.PROFILE_PRIVACY.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.PROFILE_NOTIFICATIONS.route) },
                    onNavigateToLanguage = { navController.navigate(Screen.PROFILE_LANGUAGE.route) },
                    onNavigateToAppearance = { navController.navigate(Screen.PROFILE_APPEARANCE.route) },
                    onNavigateToSupport = { navController.navigate(Screen.PROFILE_SUPPORT.route) },
                    onNavigateToAbout = { navController.navigate(Screen.PROFILE_ABOUT.route) },
                    onNavigateToDeleteAccount = { navController.navigate(Screen.PROFILE_DELETE_ACCOUNT.route) },
                    onNavigateToRefer = { navController.navigate(Screen.REFER.route) }
                )
            }

            // PROFILE DETAIL SCREENS WITH PREMIUM SLIDE ANIMATIONS
            composable(
                route = Screen.PROFILE_SETTINGS.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) }
            ) {
                ProfileSettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PROFILE_PRIVACY.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) }
            ) {
                ProfilePrivacyScreen(
                    explorerViewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PROFILE_NOTIFICATIONS.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) }
            ) {
                ProfileNotificationsScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PROFILE_LANGUAGE.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) }
            ) {
                ProfileLanguageScreen(
                    explorerViewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PROFILE_APPEARANCE.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) }
            ) {
                ProfileAppearanceScreen(
                    explorerViewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PROFILE_SUPPORT.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) }
            ) {
                ProfileSupportScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PROFILE_ABOUT.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) }
            ) {
                ProfileAboutScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PROFILE_DELETE_ACCOUNT.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) }
            ) {
                ProfileDeleteAccountScreen(
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() },
                    onDeletedSuccess = {
                        navController.navigate(Screen.LOGIN.route) {
                            popUpTo(Screen.HOME.route) { inclusive = true }
                        }
                    }
                )
            }

            // SETTINGS
            composable(Screen.SETTINGS.route) {
                LaunchedEffect(Unit) { explorerViewModel.logScreenView("Settings") }
                SettingsScreen(
                    viewModel = explorerViewModel,
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToAboutUs = { navController.navigate(Screen.ABOUT_US.route) },
                    onNavigateToContactUs = { navController.navigate(Screen.CONTACT_US.route) },
                    onNavigateToPrivacyPolicy = { navController.navigate(Screen.PRIVACY_POLICY.route) },
                    onNavigateToTermsConditions = { navController.navigate(Screen.TERMS_CONDITIONS.route) },
                    onNavigateToAdminDashboard = { navController.navigate(Screen.ADMIN_DASHBOARD.route) }
                )
            }

            // ADMIN DASHBOARD
            composable(Screen.ADMIN_DASHBOARD.route) {
                LaunchedEffect(Unit) { explorerViewModel.logScreenView("AdminDashboard") }
                com.example.ui.screens.AdminFeedbackDashboardScreen(
                    viewModel = explorerViewModel,
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // WEATHER TOOL
            composable(
                route = Screen.WEATHER.route,
                deepLinks = listOf(navDeepLink { uriPattern = "smartexplorer://weather" })
            ) {
                WeatherScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    }
                )
            }

            // COUNTRY TOOL
            composable(Screen.COUNTRIES.route) {
                CountryScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    }
                )
            }

            // NEWS TOOL
            composable(
                route = Screen.NEWS.route,
                deepLinks = listOf(navDeepLink { uriPattern = "smartexplorer://news" })
            ) {
                NewsScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    }
                )
            }

            // JOKES TOOL
            composable(Screen.JOKES.route) {
                JokeScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // CURRENCY TOOL
            composable(Screen.CURRENCY.route) {
                CurrencyScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // TRANSLATOR TOOL
            composable(Screen.TRANSLATOR.route) {
                TranslatorScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    }
                )
            }

            // DICTIONARY TOOL
            composable(Screen.DICTIONARY.route) {
                DictionaryScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // QR GENERATOR TOOL
            composable(Screen.QR_GEN.route) {
                QrGenScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // QR SCANNER TOOL
            composable(Screen.QR_SCAN.route) {
                QrScanScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // CALCULATOR TOOL
            composable(Screen.CALCULATOR.route) {
                CalculatorScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // WORLD CLOCK TOOL
            composable(Screen.WORLD_CLOCK.route) {
                WorldClockScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // INTERNET SPEED TOOL
            composable(Screen.INTERNET_SPEED.route) {
                InternetSpeedScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // EXPLORER MAP TOOL
            composable(Screen.EXPLORER_MAP.route) {
                MapScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // VAULT NOTES TOOL
            composable(Screen.VAULT_NOTES.route) {
                VaultNotesScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // CRYPTO TOOL
            composable(Screen.CRYPTO.route) {
                CryptoScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // COMMODITY TOOL
            composable(Screen.COMMODITY.route) {
                CommodityPricesScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // FACTS TOOL
            composable(Screen.FACTS.route) {
                RandomFactsScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // EMERGENCY TOOL
            composable(Screen.EMERGENCY.route) {
                EmergencyDeskScreen(
                    viewModel = explorerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // ABOUT US
            composable(Screen.ABOUT_US.route) {
                AboutUsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToLicenses = { navController.navigate(Screen.OPEN_SOURCE_LICENSES.route) }
                )
            }

            // CONTACT US
            composable(Screen.CONTACT_US.route) {
                ContactUsScreen(onBack = { navController.popBackStack() })
            }

            // PRIVACY POLICY
            composable(Screen.PRIVACY_POLICY.route) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }

            // TERMS & CONDITIONS
            composable(Screen.TERMS_CONDITIONS.route) {
                TermsConditionsScreen(onBack = { navController.popBackStack() })
            }

            // HELP & SUPPORT
            composable(Screen.HELP_SUPPORT.route) {
                HelpSupportScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToFAQ = { navController.navigate(Screen.FAQ.route) },
                    onNavigateToContact = { navController.navigate(Screen.CONTACT_US.route) },
                    onNavigateToAppVersion = { navController.navigate(Screen.APP_VERSION.route) },
                    onNavigateToLicenses = { navController.navigate(Screen.OPEN_SOURCE_LICENSES.route) }
                )
            }

            // FAQ
            composable(Screen.FAQ.route) {
                FAQScreen(onBack = { navController.popBackStack() })
            }

            // APP VERSION
            composable(Screen.APP_VERSION.route) {
                AppVersionScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToLicenses = { navController.navigate(Screen.OPEN_SOURCE_LICENSES.route) }
                )
            }

            // OPEN SOURCE LICENSES
            composable(Screen.OPEN_SOURCE_LICENSES.route) {
                OpenSourceLicensesScreen(onBack = { navController.popBackStack() })
            }

            // REFER FRIENDS PREMIUM SCREEN
            composable(
                route = Screen.REFER.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) }
            ) {
                ReferScreen(
                    viewModel = referralViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

    if (shouldShowLockScreen) {
        BiometricLockScreen(
            onUnlockClick = {
                triggerBiometricAuth(context) { success ->
                    if (success) {
                        isAppUnlocked = true
                    }
                }
            },
            onSignOutClick = {
                authViewModel.signOut {
                    navController.navigate(Screen.LOGIN.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        )
    }
}
}

fun triggerBiometricAuth(context: android.content.Context, onResult: (Boolean) -> Unit) {
    val activity = context as? androidx.fragment.app.FragmentActivity
    if (activity == null) {
        onResult(true) // Fallback bypass if not in a FragmentActivity
        return
    }
    
    val biometricManager = BiometricManager.from(context)
    val canAuthenticate = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )
    
    if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
        // If biometrics are not enrolled or available, bypass to prevent user lockouts
        onResult(true)
        return
    }

    val executor = ContextCompat.getMainExecutor(context)
    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onResult(false)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResult(true)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onResult(false)
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Smart Explorer Security")
        .setSubtitle("Authenticate to access your workspace")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .build()

    try {
        biometricPrompt.authenticate(promptInfo)
    } catch (e: Exception) {
        onResult(true) // Bypass on unexpected API failures
    }
}

@Composable
fun BiometricLockScreen(
    onUnlockClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF020617)  // Slate 950
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Glowing fingerprint icon inside a smooth circular card
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.03f), CircleShape)
                    .border(1.dp, Color(0xFF4F8CFF).copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Security Lock",
                    tint = Color(0xFF4F8CFF),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Smart Explorer Secure",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "App access is locked. Please authenticate using your biometrics to proceed.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onUnlockClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F8CFF)),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unlock with Biometrics", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onSignOutClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Logout,
                    contentDescription = null,
                    tint = Color(0xFFFF5252).copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Switch Account / Sign Out",
                    color = Color(0xFFFF5252).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
