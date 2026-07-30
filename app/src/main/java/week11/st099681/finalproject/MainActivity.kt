package week11.st099681.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import week11.st099681.finalproject.screens.AddVehicleScreen
import week11.st099681.finalproject.screens.DashboardScreen
import week11.st099681.finalproject.screens.ForgotPasswordScreen
import week11.st099681.finalproject.screens.HistoryScreen
import week11.st099681.finalproject.screens.LoginScreen
import week11.st099681.finalproject.screens.MyVehiclesScreen
import week11.st099681.finalproject.screens.OcrResultsScreen
import week11.st099681.finalproject.screens.ReceiptPreviewScreen
import week11.st099681.finalproject.screens.RegisterScreen
import week11.st099681.finalproject.screens.RemindersScreen
import week11.st099681.finalproject.screens.ScanningScreen
import week11.st099681.finalproject.screens.ServiceDetailsScreen
import week11.st099681.finalproject.screens.SplashScreen
import week11.st099681.finalproject.screens.UploadReceiptScreen
import week11.st099681.finalproject.ui.AppBottomBar
import week11.st099681.finalproject.ui.bottomTabs
import week11.st099681.finalproject.ui.theme.MotorVaultTheme

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT = "forgot"
    const val VEHICLES = "vehicles"
    const val ADD_VEHICLE = "add_vehicle"
    const val DASHBOARD = "dashboard"
    const val UPLOAD = "upload"
    const val PREVIEW = "preview"
    const val SCANNING = "scanning"
    const val RESULTS = "results"
    const val HISTORY = "history"
    const val REMINDERS = "reminders"
    const val DETAILS = "details/{category}"
    fun details(category: String) = "details/$category"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotorVaultTheme {
                MotorVaultApp()
            }
        }
    }
}

@Composable
fun MotorVaultApp() {
    val navController = rememberNavController()
    val vm: AppViewModel = viewModel()

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = bottomTabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.VEHICLES) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { padding ->
        AppNavHost(navController, vm, Modifier.padding(padding))
    }
}

@Composable
fun AppNavHost(navController: NavHostController, vm: AppViewModel, modifier: Modifier) {
    NavHost(navController = navController, startDestination = Routes.SPLASH, modifier = modifier) {

        composable(Routes.SPLASH) {
            SplashScreen {
                if (vm.isLoggedIn) {
                    vm.startListening()
                    navController.navigate(Routes.VEHICLES) { popUpTo(Routes.SPLASH) { inclusive = true } }
                } else {
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } }
                }
            }
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    vm.startListening()
                    navController.navigate(Routes.VEHICLES) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onCreateAccount = { navController.navigate(Routes.REGISTER) },
                onForgotPassword = { navController.navigate(Routes.FORGOT) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    vm.startListening()
                    navController.navigate(Routes.VEHICLES) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onLogIn = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT) {
            ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() })
        }

        composable(Routes.VEHICLES) {
            MyVehiclesScreen(
                vm = vm,
                onAddVehicle = { navController.navigate(Routes.ADD_VEHICLE) },
                onViewDashboard = { vehicle ->
                    vm.selectedVehicleId = vehicle.id
                    navController.navigate(Routes.DASHBOARD)
                },
                onSignOut = {
                    vm.signOut()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Routes.ADD_VEHICLE) {
            AddVehicleScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                vm = vm,
                onUploadReceipt = { navController.navigate(Routes.UPLOAD) },
                onCategoryClick = { category -> navController.navigate(Routes.details(category)) }
            )
        }

        composable(Routes.UPLOAD) {
            UploadReceiptScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onImageReady = { navController.navigate(Routes.PREVIEW) }
            )
        }

        composable(Routes.PREVIEW) {
            ReceiptPreviewScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onScan = { navController.navigate(Routes.SCANNING) }
            )
        }

        composable(Routes.SCANNING) {
            ScanningScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.navigate(Routes.RESULTS) {
                        popUpTo(Routes.SCANNING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.RESULTS) {
            OcrResultsScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate(Routes.HISTORY) {
                        popUpTo(Routes.DASHBOARD)
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                vm = vm,
                onViewDetails = { category -> navController.navigate(Routes.details(category)) }
            )
        }

        composable(Routes.REMINDERS) {
            RemindersScreen(vm = vm)
        }

        composable(Routes.DETAILS) { entry ->
            val category = entry.arguments?.getString("category") ?: "Oil Change"
            ServiceDetailsScreen(
                vm = vm,
                categoryName = category,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
