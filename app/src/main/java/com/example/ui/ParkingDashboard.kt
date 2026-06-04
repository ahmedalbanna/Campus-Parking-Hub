package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingDashboard(viewModel: ParkingViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val selectedZoneId by viewModel.selectedZoneId.collectAsStateWithLifecycle()

    val zones by viewModel.zones.collectAsStateWithLifecycle()
    val slots by viewModel.slots.collectAsStateWithLifecycle()
    val reservations by viewModel.reservations.collectAsStateWithLifecycle()
    val violations by viewModel.violations.collectAsStateWithLifecycle()
    val permits by viewModel.permits.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()

    val currentUserVehicles by viewModel.currentUserVehicles.collectAsStateWithLifecycle()
    val currentUserReservations by viewModel.currentUserReservations.collectAsStateWithLifecycle()
    val currentUserPermits by viewModel.currentUserPermits.collectAsStateWithLifecycle()
    val currentUserPayments by viewModel.currentUserPayments.collectAsStateWithLifecycle()

    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()
    val arUiMessage by viewModel.arUiMessage.collectAsStateWithLifecycle()

    // Dialog flags
    var reservationSlotTarget by remember { mutableStateOf<ParkingSlot?>(null) }
    var extensionTarget by remember { mutableStateOf<Reservation?>(null) }
    var paymentTarget by remember { mutableStateOf<Pair<Int, String>?>(null) } // pair(id, "RESERVATION" or "VIOLATION")
    var paymentAmount by remember { mutableStateOf(0.0) }

    // RTL handling
    val layoutDirection = if (currentLang == AppLanguage.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

    // Formatters
    val timeFormatter = remember { SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()) }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = Localization.get("app_title", currentLang),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = Localization.get("app_subtitle", currentLang),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    },
                    actions = {
                        // Language switcher
                        Button(
                            onClick = {
                                viewModel.setLanguage(if (currentLang == AppLanguage.EN) AppLanguage.AR else AppLanguage.EN)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("btn_language_switch")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language toggle",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Localization.get("select_lang", currentLang),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Role switcher slider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TabButton(
                            label = Localization.get("role_user", currentLang),
                            icon = Icons.Default.DirectionsCar,
                            isSelected = currentRole == UserRole.USER,
                            onClick = { viewModel.setRole(UserRole.USER) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_role_user")
                        )
                        TabButton(
                            label = Localization.get("role_security", currentLang),
                            icon = Icons.Default.Security,
                            isSelected = currentRole == UserRole.SECURITY,
                            onClick = { viewModel.setRole(UserRole.SECURITY) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_role_security")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Alert Messages banner
                    val activeMsg = if (currentLang == AppLanguage.AR) arUiMessage else uiMessage
                    if (activeMsg != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("ui_alert_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "info"
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = activeMsg,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.clearMessage() },
                                    modifier = Modifier.testTag("btn_clear_alert")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close"
                                    )
                                }
                            }
                        }
                    }

                    // Main View depending on selected Role
                    Crossfade(targetState = currentRole, label = "RoleTransition") { role ->
                        when (role) {
                            UserRole.USER -> {
                                StudentStaffPortal(
                                    currentUserId = currentUserId,
                                    currentLang = currentLang,
                                    selectedZoneId = selectedZoneId,
                                    zones = zones,
                                    slots = slots,
                                    vehicles = vehicles,
                                    users = users,
                                    currentUserVehicles = currentUserVehicles,
                                    currentUserReservations = currentUserReservations,
                                    currentUserPermits = currentUserPermits,
                                    currentUserPayments = currentUserPayments,
                                    violations = violations,
                                    viewModel = viewModel,
                                    onReserveRequested = { reservationSlotTarget = it },
                                    onExtendRequested = { extensionTarget = it },
                                    onPaymentRequested = { referenceId, type, amount ->
                                        paymentTarget = Pair(referenceId, type)
                                        paymentAmount = amount
                                    },
                                    timeFormatter = timeFormatter
                                )
                            }
                            UserRole.SECURITY -> {
                                SecurityPersonnelPortal(
                                    currentLang = currentLang,
                                    zones = zones,
                                    slots = slots,
                                    violations = violations,
                                    permits = permits,
                                    payments = payments,
                                    reports = reports,
                                    viewModel = viewModel,
                                    timeFormatter = timeFormatter
                                )
                            }
                        }
                    }
                }

                // Reservation Dialog
                if (reservationSlotTarget != null) {
                    val target = reservationSlotTarget!!
                    ReservationDialog(
                        slot = target,
                        currentLang = currentLang,
                        userVehicles = currentUserVehicles,
                        onDismiss = { reservationSlotTarget = null },
                        onConfirm = { plate, hours ->
                            viewModel.reserveSlot(target.id, target.zoneId, plate, hours)
                            reservationSlotTarget = null
                        }
                    )
                }

                // Extension Dialog
                if (extensionTarget != null) {
                    val target = extensionTarget!!
                    ExtensionDialog(
                        reservation = target,
                        currentLang = currentLang,
                        onDismiss = { extensionTarget = null },
                        onConfirm = { hours ->
                            viewModel.extendParking(target.id, hours)
                            extensionTarget = null
                        }
                    )
                }

                // Payment Dialog
                if (paymentTarget != null) {
                    val target = paymentTarget!!
                    PaymentDialog(
                        amount = paymentAmount,
                        paymentType = target.second,
                        currentLang = currentLang,
                        onDismiss = { paymentTarget = null },
                        onConfirm = { method ->
                            viewModel.payFee(
                                referenceId = target.first,
                                type = target.second,
                                amount = paymentAmount,
                                method = method
                            )
                            paymentTarget = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TabButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val elevation = if (isSelected) 4.dp else 0.dp

    Row(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        )
    }
}

// ==========================================
// Students / Staff User Role Portal
// ==========================================
@Composable
fun StudentStaffPortal(
    currentUserId: String,
    currentLang: AppLanguage,
    selectedZoneId: String,
    zones: List<Zone>,
    slots: List<ParkingSlot>,
    vehicles: List<Vehicle>,
    users: List<User>,
    currentUserVehicles: List<Vehicle>,
    currentUserReservations: List<Reservation>,
    currentUserPermits: List<Permit>,
    currentUserPayments: List<Payment>,
    violations: List<Violation>,
    viewModel: ParkingViewModel,
    onReserveRequested: (ParkingSlot) -> Unit,
    onExtendRequested: (Reservation) -> Unit,
    onPaymentRequested: (Int, String, Double) -> Unit,
    timeFormatter: SimpleDateFormat
) {
    var userTabState by remember { mutableStateOf(0) } // 0: Live Map, 1: Bookings, 2: Registration, 3: Permits & History

    // Active session Countdown ticker
    val activeRes = currentUserReservations.find { it.status == "ACTIVE" && it.endTime > System.currentTimeMillis() }
    var tickerTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(key1 = activeRes) {
        if (activeRes != null) {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                tickerTime = System.currentTimeMillis()
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Driver Profile selector (To show student vs staff workflows)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = Localization.get("active_user", currentLang),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeDriverName = users.find { it.id == currentUserId }?.name ?: "Unknown"
                    val activeDriverRole = users.find { it.id == currentUserId }?.role ?: "STUDENT"
                    val activeDriverEmail = users.find { it.id == currentUserId }?.email ?: ""
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeDriverName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$activeDriverRole | $activeDriverEmail",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    // Quick switcher
                    Row {
                        AssistChip(
                            onClick = { viewModel.switchUserSimulation("U101") },
                            label = { Text("Student (Ahmed)", fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (currentUserId == "U101") MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                            ),
                            modifier = Modifier.testTag("switch_to_student")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AssistChip(
                            onClick = { viewModel.switchUserSimulation("U202") },
                            label = { Text("Staff (Sarah)", fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (currentUserId == "U202") MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                            ),
                            modifier = Modifier.testTag("switch_to_staff")
                        )
                    }
                }
            }
        }

        // Active Session high density dashboard block
        activeRes?.let { res ->
            val remainingTimeMs = res.endTime - tickerTime
            if (remainingTimeMs > 0) {
                val hours = (remainingTimeMs / (1000 * 60 * 60)) % 24
                val minutes = (remainingTimeMs / (1000 * 60)) % 60
                val seconds = (remainingTimeMs / 1000) % 60
                val countdownStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF001D36),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("active_session_card"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (currentLang == AppLanguage.AR) "الجلسة النشطة · الموقف ${res.slotId}" else "Current Session · Slot ${res.slotId}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (currentLang == AppLanguage.AR) "ينتهي في $countdownStr" else "Ends in $countdownStr",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = Color.White
                            )
                        }
                        Button(
                            onClick = { onExtendRequested(res) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD6E3FF),
                                contentColor = Color(0xFF001D36)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("session_extend_btn")
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.AR) "تمديد" else "EXTEND",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Sub-tabs
        ScrollableTabRow(
            selectedTabIndex = userTabState,
            edgePadding = 0.dp,
            divider = {},
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Tab(
                selected = userTabState == 0,
                onClick = { userTabState = 0 },
                text = { Text(if (currentLang == AppLanguage.AR) "مخطط المواقف" else "Live Slot Map") },
                icon = { Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("user_subtab_map")
            )
            Tab(
                selected = userTabState == 1,
                onClick = { userTabState = 1 },
                text = { Text(if (currentLang == AppLanguage.AR) "حجوزاتي" else "Reservations") },
                icon = { Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("user_subtab_bookings")
            )
            Tab(
                selected = userTabState == 2,
                onClick = { userTabState = 2 },
                text = { Text(if (currentLang == AppLanguage.AR) "تسجيل تصريح" else "Permit Form") },
                icon = { Icon(Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("user_subtab_permits")
            )
            Tab(
                selected = userTabState == 3,
                onClick = { userTabState = 3 },
                text = { Text(if (currentLang == AppLanguage.AR) "المحفظة والمخالفات" else "Wallet & Fines") },
                icon = { Icon(Icons.Default.Wallet, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("user_subtab_wallet")
            )
        }

        // Selected Sub-Tab Screens
        when (userTabState) {
            0 -> LiveMapScreen(
                currentLang = currentLang,
                selectedZoneId = selectedZoneId,
                zones = zones,
                slots = slots,
                currentUserReservations = currentUserReservations,
                currentUserPermits = currentUserPermits,
                violations = violations,
                viewModel = viewModel,
                onReserveRequested = onReserveRequested,
                onExtendRequested = onExtendRequested,
                onPaymentRequested = onPaymentRequested
            )
            1 -> UserReservationsScreen(
                currentLang = currentLang,
                reservations = currentUserReservations,
                onExtendRequested = onExtendRequested,
                onPaymentRequested = onPaymentRequested,
                timeFormatter = timeFormatter
            )
            2 -> AddVehiclePermitScreen(
                currentLang = currentLang,
                currentUserPermits = currentUserPermits,
                currentUserVehicles = currentUserVehicles,
                viewModel = viewModel,
                timeFormatter = timeFormatter
            )
            3 -> WalletFinesScreen(
                currentLang = currentLang,
                payments = currentUserPayments,
                violations = violations,
                vehicles = currentUserVehicles,
                onPaymentRequested = onPaymentRequested,
                timeFormatter = timeFormatter
            )
        }
    }
}

@Composable
fun LiveMapScreen(
    currentLang: AppLanguage,
    selectedZoneId: String,
    zones: List<Zone>,
    slots: List<ParkingSlot>,
    currentUserReservations: List<Reservation>,
    currentUserPermits: List<Permit>,
    violations: List<Violation>,
    viewModel: ParkingViewModel,
    onReserveRequested: (ParkingSlot) -> Unit,
    onExtendRequested: (Reservation) -> Unit,
    onPaymentRequested: (Int, String, Double) -> Unit
) {
    val zone = zones.find { it.id == selectedZoneId } ?: zones.firstOrNull()
    val zoneSlots = slots.filter { it.zoneId == selectedZoneId }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Zone Selector Chips
        Text(
            text = Localization.get("current_zone", currentLang),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            zones.forEach { z ->
                ElevatedFilterChip(
                    selected = selectedZoneId == z.id,
                    onClick = { viewModel.selectZone(z.id) },
                    label = {
                        Text(if (currentLang == AppLanguage.AR) z.nameAr else z.name, fontSize = 12.sp)
                    },
                    modifier = Modifier.testTag("zone_chip_${z.id}")
                )
            }
        }

        if (zone != null) {
            val occupiedInZone = zoneSlots.count { it.isOccupied }
            val freeInZone = zone.totalSlots - occupiedInZone
            val occPercent = if (zone.totalSlots > 0) (occupiedInZone.toFloat() / zone.totalSlots.toFloat() * 100).toInt() else 0

            // High Density Row of 3 Cards Side-by-Side (Height ~ 20vh represented nicely as 80.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1: Available Slots (Lavender BG #D6E3FF, text #001D36)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD6E3FF),
                        contentColor = Color(0xFF001D36)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.AR) "الشواغر" else "AVAILABLE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001D36).copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$freeInZone",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001D36)
                        )
                    }
                }

                // Card 2: Occupancy (White BG, border #C4C6D0, text #44474E)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF44474E)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .border(1.dp, Color(0xFFC4C6D0), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.AR) "نسبة الإشغال" else "OCCUPANCY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF44474E).copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$occPercent%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF44474E)
                        )
                    }
                }

                // Card 3: My Permit Status (Mint BG #BAF3DB, text #002114)
                val activePermit = currentUserPermits.firstOrNull { it.isValid }
                val permitText = if (activePermit != null) {
                    if (currentLang == AppLanguage.AR) "صالح · ${if (activePermit.permitType.contains("STAFF")) "موظف" else "طالب"}" 
                    else "VALID · ${activePermit.permitType.replace("_", " ").split(" ").firstOrNull() ?: ""}"
                } else {
                    if (currentLang == AppLanguage.AR) "لا يوجد تصريح" else "NO PERMIT"
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFBAF3DB),
                        contentColor = Color(0xFF002114)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.AR) "تصريحي" else "MY PERMIT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF002114).copy(alpha = 0.7f)
                        )
                        Text(
                            text = permitText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF002114)
                        )
                    }
                }
            }

            // High Density Zone Label header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (currentLang == AppLanguage.AR) zone.nameAr else zone.name} · ${zone.type}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
            }

            // Slots Grid - Styled as 4-column custom card list in a rounded-3xl container border
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFC4C6D0), RoundedCornerShape(24.dp))
                    .padding(8.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .testTag("slots_grid")
                ) {
                    items(zoneSlots) { slot ->
                        // Determine custom high density design states/styles
                        val belongsToActiveUser = currentUserReservations.any {
                            it.slotId == slot.id && it.status == "ACTIVE" && slot.isOccupied && slot.occupiedByVehiclePlate == it.vehiclePlate
                        }

                        val hasViolation = slot.isOccupied && violations.any {
                            it.vehiclePlate == slot.occupiedByVehiclePlate && !it.isPaid
                        }

                        // Colors and borders per mockup
                        val (cardBg, borderStroke, textColor, tagText) = when {
                            belongsToActiveUser -> Quadruple(
                                Color(0xFFBAF3DB), // Mint
                                BorderStroke(2.dp, Color(0xFF006C4C)), // ring outline
                                Color(0xFF002114),
                                if (currentLang == AppLanguage.AR) "حجزي" else "MINE"
                            )
                            hasViolation -> Quadruple(
                                Color(0xFFFFDAD6), // Rose
                                null,
                                Color(0xFF410002),
                                if (currentLang == AppLanguage.AR) "مخالف" else "VIOL"
                            )
                            slot.isOccupied -> Quadruple(
                                Color(0xFF0061A4), // Royal brand blue
                                null,
                                Color.White,
                                ""
                            )
                            slot.type == "STAFF" -> Quadruple(
                                Color(0xFF1A1C1E), // Slate dark
                                null,
                                Color.White,
                                if (currentLang == AppLanguage.AR) "خاص" else "RES"
                            )
                            else -> Quadruple(
                                Color(0xFFF1F3FB), // Light grey periwinkle
                                BorderStroke(2.dp, Color(0xFFC4C6D0)), // Dashed boundary equiv
                                Color(0xFF44474E),
                                ""
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(10.dp)) else Modifier)
                                .clickable {
                                    if (belongsToActiveUser) {
                                        val myRes = currentUserReservations.find { it.slotId == slot.id && it.status == "ACTIVE" }
                                        if (myRes != null) {
                                            onExtendRequested(myRes)
                                        }
                                    } else if (!slot.isOccupied) {
                                        onReserveRequested(slot)
                                    }
                                }
                                .testTag("slot_card_${slot.id}")
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = slot.id,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                if (tagText.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tagText,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textColor,
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontStyle = if (slot.type == "STAFF" && !belongsToActiveUser) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simple quad holder
data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun UserReservationsScreen(
    currentLang: AppLanguage,
    reservations: List<Reservation>,
    onExtendRequested: (Reservation) -> Unit,
    onPaymentRequested: (Int, String, Double) -> Unit,
    timeFormatter: SimpleDateFormat
) {
    Text(
        text = Localization.get("active_reservations_label", currentLang),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (reservations.isEmpty()) {
        EmptyStateView(
            message = Localization.get("empty_reservations", currentLang),
            icon = Icons.Default.CalendarToday
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .testTag("user_reservations_list")
        ) {
            items(reservations) { res ->
                val remainingTimeMs = res.endTime - System.currentTimeMillis()
                val isExpired = remainingTimeMs <= 0

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = res.slotId,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${Localization.get("current_zone", currentLang)}: ${res.zoneId.replace("ZONE_", "Zone ")}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Plate: ${res.vehiclePlate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Status badge
                            val badgeBg = if (res.status == "ACTIVE" && !isExpired) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                            val badgeText = if (res.status == "ACTIVE") {
                                if (isExpired) Localization.get("EXPIRED", currentLang) else Localization.get("ACTIVE", currentLang)
                            } else {
                                Localization.get(res.status, currentLang)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = Localization.get("duration", currentLang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${timeFormatter.format(Date(res.startTime))} \n-> ${timeFormatter.format(Date(res.endTime))}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Localization.get("amount", currentLang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$${res.feeAmount}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Payment Status Action
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (res.isPaid) Icons.Default.CheckCircle else Icons.Outlined.Payment,
                                    contentDescription = null,
                                    tint = if (res.isPaid) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (res.isPaid) {
                                        Localization.get("is_paid", currentLang)
                                    } else {
                                        Localization.get("unpaid", currentLang)
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (res.isPaid) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.clickable(enabled = !res.isPaid) {
                                        onPaymentRequested(res.id, "RESERVATION", res.feeAmount)
                                    }
                                )
                            }

                            if (res.status == "ACTIVE" && !isExpired) {
                                Button(
                                    onClick = { onExtendRequested(res) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier.testTag("btn_extend_res_${res.id}")
                                ) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Localization.get("extend_time", currentLang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddVehiclePermitScreen(
    currentLang: AppLanguage,
    currentUserPermits: List<Permit>,
    currentUserVehicles: List<Vehicle>,
    viewModel: ParkingViewModel,
    timeFormatter: SimpleDateFormat
) {
    var plate by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var selectedPermitType by remember { mutableStateOf("STUDENT_SEMESTER") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = Localization.get("apply_permit", currentLang),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text(Localization.get("input_vehicle_plate", currentLang)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_permit_plate"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text(Localization.get("vehicle_model", currentLang)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_permit_model"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = Localization.get("permit_type", currentLang),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Radio options
                listOf("STUDENT_SEMESTER", "STAFF_ANNUAL", "VISITOR_DAILY").forEach { type ->
                    val labelAndPrice = when (type) {
                        "STUDENT_SEMESTER" -> Localization.get("permit_student", currentLang)
                        "STAFF_ANNUAL" -> Localization.get("permit_staff", currentLang)
                        else -> Localization.get("permit_visitor", currentLang)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPermitType = type }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPermitType == type,
                            onClick = { selectedPermitType = type },
                            modifier = Modifier.testTag("radio_permit_$type")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = labelAndPrice, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.addVehicleAndApplyPermit(plate, model, selectedPermitType)
                        plate = ""
                        model = ""
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_submit_permit_app")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Localization.get("register_and_apply", currentLang), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Existing Permits list
        Text(
            text = Localization.get("permit_status", currentLang),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (currentUserPermits.isEmpty()) {
            EmptyStateView(
                message = Localization.get("empty_permits", currentLang),
                icon = Icons.Default.Badge
            )
        } else {
            currentUserPermits.forEach { permit ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Plate: ${permit.licensePlate}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Type: ${Localization.get(permit.permitType, currentLang)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Expires: ${timeFormatter.format(Date(permit.expiryDate))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified valid",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WalletFinesScreen(
    currentLang: AppLanguage,
    payments: List<Payment>,
    violations: List<Violation>,
    vehicles: List<Vehicle>,
    onPaymentRequested: (Int, String, Double) -> Unit,
    timeFormatter: SimpleDateFormat
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Pending Violations
        Text(
            text = if (currentLang == AppLanguage.AR) "مخالفات السير المترتبة عليك" else "Your Traffic Violations",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val plateList = vehicles.map { it.licensePlate }
        val userFines = violations.filter { plateList.contains(it.vehiclePlate) }

        if (userFines.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (currentLang == AppLanguage.AR) "سجل نظيف! لا توجد مخالفات مسجلة على مركبتك." else "Amazing! No registered traffic violations on your license plate.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        } else {
            userFines.forEach { violation ->
                val cardBg = if (violation.isPaid) Color(0xFFBAF3DB).copy(alpha = 0.25f) else Color(0xFFFFDAD6)
                val strokeColor = if (violation.isPaid) Color(0xFF006C4C) else Color(0xFFBA1A1A)
                val contentColor = if (violation.isPaid) Color(0xFF002114) else Color(0xFF410002)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .border(
                            if (violation.isPaid) 1.dp else 2.dp,
                            strokeColor,
                            RoundedCornerShape(14.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = Localization.get(violation.violationType, currentLang),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = contentColor
                                )
                                Text(
                                    text = "${Localization.get("location", currentLang)}: ${violation.location}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = contentColor.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = "$${violation.fineAmount}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = strokeColor
                                )
                            )
                        }

                        if (violation.notes != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = violation.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (violation.isPaid) Color(0xFF006C4C) else Color(0xFFBA1A1A),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = timeFormatter.format(Date(violation.timestamp)),
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.7f)
                            )

                            if (!violation.isPaid) {
                                Button(
                                    onClick = { onPaymentRequested(violation.id, "VIOLATION", violation.fineAmount) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_pay_violation_${violation.id}")
                                ) {
                                    Text(Localization.get("pay_now", currentLang), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(Localization.get("is_paid", currentLang), color = Color(0xFF006C4C)) },
                                    leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF006C4C), modifier = Modifier.size(12.dp)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // History list
        Text(
            text = Localization.get("history_label", currentLang),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (payments.isEmpty()) {
            EmptyStateView(
                message = "No payments generated yet.",
                icon = Icons.Default.Receipt
            )
        } else {
            payments.forEach { pay ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Paid reservation / violation",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "via: ${Localization.get(pay.paymentMethod, currentLang)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = timeFormatter.format(Date(pay.timestamp)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "-$${pay.amount}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// Security Personnel Role Portal
// ==========================================
@Composable
fun SecurityPersonnelPortal(
    currentLang: AppLanguage,
    zones: List<Zone>,
    slots: List<ParkingSlot>,
    violations: List<Violation>,
    permits: List<Permit>,
    payments: List<Payment>,
    reports: List<ReportSnapshot>,
    viewModel: ParkingViewModel,
    timeFormatter: SimpleDateFormat
) {
    var secTabState by remember { mutableStateOf(0) } // 0: Live Monitoring, 1: Gate Registrar, 2: Cites/Violations, 3: Reports

    Column(modifier = Modifier.fillMaxWidth()) {
        // Operational stats summary row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val totalCap = slots.size
            val currentOcc = slots.count { it.isOccupied }
            val occPercent = if (totalCap > 0) (currentOcc.toDouble() / totalCap.toDouble()) * 100.0 else 0.0

            SecurityStatCard(
                title = Localization.get("occupancy_rate", currentLang),
                value = "${"%.1f".format(occPercent)}%",
                icon = Icons.Default.Analytics,
                modifier = Modifier.weight(1f)
            )

            val revenue = payments.sumOf { it.amount }
            SecurityStatCard(
                title = Localization.get("total_revenue", currentLang),
                value = "$${revenue}",
                icon = Icons.Default.MonetizationOn,
                modifier = Modifier.weight(1f)
            )
        }

        // Subtabs
        ScrollableTabRow(
            selectedTabIndex = secTabState,
            edgePadding = 0.dp,
            divider = {},
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Tab(
                selected = secTabState == 0,
                onClick = { secTabState = 0 },
                text = { Text(if (currentLang == AppLanguage.AR) "إشغال المواقف" else "Monitor") },
                icon = { Icon(Icons.Default.DynamicFeed, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("sec_subtab_monitor")
            )
            Tab(
                selected = secTabState == 1,
                onClick = { secTabState = 1 },
                text = { Text(if (currentLang == AppLanguage.AR) "تسجيل الدخول/الخروج" else "Check In/Out") },
                icon = { Icon(Icons.Default.ImportExport, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("sec_subtab_gate")
            )
            Tab(
                selected = secTabState == 2,
                onClick = { secTabState = 2 },
                text = { Text(if (currentLang == AppLanguage.AR) "المخالفات" else "Violations") },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("sec_subtab_violations")
            )
            Tab(
                selected = secTabState == 3,
                onClick = { secTabState = 3 },
                text = { Text(if (currentLang == AppLanguage.AR) "التقارير الحية" else "Reports") },
                icon = { Icon(Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("sec_subtab_reports")
            )
        }

        when (secTabState) {
            0 -> SecurityLiveMappingScreen(
                currentLang = currentLang,
                zones = zones,
                slots = slots,
                permits = permits,
                viewModel = viewModel
            )
            1 -> GateRegistrarScreen(
                currentLang = currentLang,
                slots = slots,
                viewModel = viewModel
            )
            2 -> SecurityViolationsScreen(
                currentLang = currentLang,
                violations = violations,
                viewModel = viewModel,
                timeFormatter = timeFormatter
            )
            3 -> ReportsScreen(
                currentLang = currentLang,
                reports = reports,
                viewModel = viewModel,
                timeFormatter = timeFormatter
            )
        }
    }
}

@Composable
fun SecurityStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
            }
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun SecurityLiveMappingScreen(
    currentLang: AppLanguage,
    zones: List<Zone>,
    slots: List<ParkingSlot>,
    permits: List<Permit>,
    viewModel: ParkingViewModel
) {
    val selectedZoneId by viewModel.selectedZoneId.collectAsStateWithLifecycle()
    val zoneSlots = slots.filter { it.zoneId == selectedZoneId }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.get("security_monitoring", currentLang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )

            // Switch Zone Box
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                zones.forEach { z ->
                    ElevatedFilterChip(
                        selected = selectedZoneId == z.id,
                        onClick = { viewModel.selectZone(z.id) },
                        label = { Text(if (currentLang == AppLanguage.AR) z.nameAr else z.name, fontSize = 9.sp) },
                        modifier = Modifier.testTag("sec_zone_chip_${z.id}")
                    )
                }
            }
        }

        // Horizontal legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LegendItem(color = Color(0xFF4CAF50), label = if (currentLang == AppLanguage.AR) "متاح" else "Available")
            LegendItem(color = Color(0xFFF44336), label = if (currentLang == AppLanguage.AR) "مشغول" else "Occupied")
        }

        // Big Grid map
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            items(zoneSlots) { slot ->
                val slotColor = if (slot.isOccupied) Color(0xFFF44336) else Color(0xFF4CAF50)

                Card(
                    colors = CardDefaults.cardColors(containerColor = slotColor.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, slotColor, RoundedCornerShape(10.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = slot.id,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = slotColor
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val plate = slot.occupiedByVehiclePlate
                        if (plate != null) {
                            Text(
                                text = plate.replace("(", "\n").replace(")", ""),
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2
                            )
                        } else {
                            Text(text = "Empty", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GateRegistrarScreen(
    currentLang: AppLanguage,
    slots: List<ParkingSlot>,
    viewModel: ParkingViewModel
) {
    var checkInSlotId by remember { mutableStateOf("") }
    var checkInPlate by remember { mutableStateOf("") }
    var checkOutSlotId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = Localization.get("check_in_out", currentLang),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Row containing both panels
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Check In Section
                Text(
                    text = Localization.get("check_in_btn", currentLang),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Select Slot
                OutlinedTextField(
                    value = checkInSlotId,
                    onValueChange = { checkInSlotId = it },
                    label = { Text(if (currentLang == AppLanguage.AR) "موقف (مثال: A01)" else "Slot ID (e.g. A01)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_gate_check_in_slot"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = checkInPlate,
                    onValueChange = { checkInPlate = it },
                    label = { Text(Localization.get("license_plate_label", currentLang)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_gate_check_in_plate"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.securityCheckIn(checkInSlotId.uppercase(), checkInPlate)
                        checkInSlotId = ""
                        checkInPlate = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_register_check_in")
                ) {
                    Text(Localization.get("check_in_btn", currentLang), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(18.dp))

                // Check Out Section
                Text(
                    text = Localization.get("check_out_btn", currentLang),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = checkOutSlotId,
                    onValueChange = { checkOutSlotId = it },
                    label = { Text(if (currentLang == AppLanguage.AR) "رقم الموقف التعديلي" else "Slot ID (to free up)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_gate_check_out_slot"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.securityCheckOut(checkOutSlotId.uppercase())
                        checkOutSlotId = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_register_check_out")
                ) {
                    Text(Localization.get("check_out_btn", currentLang), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SecurityViolationsScreen(
    currentLang: AppLanguage,
    violations: List<Violation>,
    viewModel: ParkingViewModel,
    timeFormatter: SimpleDateFormat
) {
    var plate by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("100") }
    var location by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("NO_PERMIT") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = Localization.get("issue_violation", currentLang),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text(Localization.get("license_plate_label", currentLang)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_violation_plate"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(Localization.get("location", currentLang)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_violation_location"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(Localization.get("fine_amount", currentLang)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_violation_amount"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = Localization.get("violation_type", currentLang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                listOf("NO_PERMIT", "OVERTIME", "WRONG_ZONE").forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Localization.get(type, currentLang), fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(Localization.get("notes", currentLang)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_violation_notes"),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val fine = amountStr.toDoubleOrNull() ?: 100.0
                        viewModel.issueViolation(plate, selectedType, fine, location, notes)
                        plate = ""
                        location = ""
                        notes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_issue_violation")
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Localization.get("issue_citation_btn", currentLang), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History list
        Text(
            text = Localization.get("violation_history", currentLang),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (violations.isEmpty()) {
            EmptyStateView(
                message = Localization.get("empty_violations", currentLang),
                icon = Icons.Default.Done
            )
        } else {
            violations.forEach { citation ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = citation.vehiclePlate,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${Localization.get("violation_type", currentLang)}: ${Localization.get(citation.violationType, currentLang)}",
                                    fontSize = 13.sp
                                )
                            }

                            val paidBg = if (citation.isPaid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            val paidText = if (citation.isPaid) Localization.get("is_paid", currentLang) else Localization.get("unpaid", currentLang)
                            val paidContentColor = if (citation.isPaid) Color(0xFF2E7D32) else Color(0xFFC62828)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(paidBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = paidText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = paidContentColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${Localization.get("location", currentLang)}: ${citation.location} | Fine: $${citation.fineAmount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (citation.notes != null) {
                            Text(
                                text = "Notes: ${citation.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(
    currentLang: AppLanguage,
    reports: List<ReportSnapshot>,
    viewModel: ParkingViewModel,
    timeFormatter: SimpleDateFormat
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = { viewModel.generateOccupancyReport() },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_generate_report")
        ) {
            Icon(Icons.Default.Summarize, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(Localization.get("generate_report_btn", currentLang), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Localization.get("reports_history_label", currentLang),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (reports.isEmpty()) {
            EmptyStateView(
                message = Localization.get("empty_reports", currentLang),
                icon = Icons.Default.Analytics
            )
        } else {
            reports.forEach { r ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Report Snapshot #${r.id}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${Localization.get("time", currentLang)}: ${timeFormatter.format(Date(r.timestamp))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Occupancy: ${"%.1f".format(r.totalOccupancyPercent)}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${Localization.get("active_res_count", currentLang)}: ${r.activeReservationsCount}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${Localization.get("active_vio_count", currentLang)}: ${r.activeViolationsCount}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${Localization.get("total_revenue", currentLang)}: $${r.totalRevenue}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Officer: ${r.generatedBy}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Dialogs & Helper Screens
// ==========================================
@Composable
fun ReservationDialog(
    slot: ParkingSlot,
    currentLang: AppLanguage,
    userVehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var selectedPlate by remember { mutableStateOf(userVehicles.firstOrNull()?.licensePlate ?: "") }
    var customPlateInput by remember { mutableStateOf("") }
    var isCustomPlateExpanded by remember { mutableStateOf(userVehicles.isEmpty()) }
    var durationHours by remember { mutableStateOf(2) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${Localization.get("reservation_title", currentLang)} (${slot.id})",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (userVehicles.isNotEmpty()) {
                    Text(
                        text = Localization.get("select_vehicle", currentLang),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    userVehicles.forEach { vehicle ->
                        val isSelected = selectedPlate == vehicle.licensePlate && !isCustomPlateExpanded
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPlate = vehicle.licensePlate
                                    isCustomPlateExpanded = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedPlate = vehicle.licensePlate
                                    isCustomPlateExpanded = false
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${vehicle.licensePlate} (${vehicle.model})")
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCustomPlateExpanded = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isCustomPlateExpanded, onClick = { isCustomPlateExpanded = true })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (currentLang == AppLanguage.AR) "رقم لوحة آخر مخصص" else "Other custom plate")
                    }
                }

                if (isCustomPlateExpanded || userVehicles.isEmpty()) {
                    OutlinedTextField(
                        value = customPlateInput,
                        onValueChange = { customPlateInput = it },
                        label = { Text(Localization.get("input_vehicle_plate", currentLang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_reservation_custom_plate"),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${Localization.get("duration", currentLang)}: $durationHours ${Localization.get("hours", currentLang)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = durationHours.toFloat(),
                    onValueChange = { durationHours = it.toInt() },
                    valueRange = 1f..12f,
                    steps = 10,
                    modifier = Modifier.testTag("slider_reservation_duration")
                )

                // Cost summary Card
                val price = durationHours * 5.0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (currentLang == AppLanguage.AR) "التكلفة الكلية المقدرة" else "Total Estimated Fee:", fontWeight = FontWeight.Bold)
                        Text(text = "$$price", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("btn_reserve_cancel")) {
                        Text(Localization.get("cancel", currentLang))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val plate = if (isCustomPlateExpanded) customPlateInput else selectedPlate
                            if (plate.isNotBlank()) {
                                onConfirm(plate, durationHours)
                            }
                        },
                        modifier = Modifier.testTag("btn_reserve_confirm")
                    ) {
                        Text(Localization.get("confirm_reservation", currentLang))
                    }
                }
            }
        }
    }
}

@Composable
fun ExtensionDialog(
    reservation: Reservation,
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var extensionHours by remember { mutableStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Localization.get("extend_time", currentLang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = if (currentLang == AppLanguage.AR) "تمديد حجز الموقف رقم: ${reservation.slotId}" else "Extend parking reservation for: ${reservation.slotId}", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${Localization.get("duration", currentLang)}: +$extensionHours ${Localization.get("hours", currentLang)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = extensionHours.toFloat(),
                    onValueChange = { extensionHours = it.toInt() },
                    valueRange = 1f..6f,
                    steps = 4,
                    modifier = Modifier.testTag("slider_extension")
                )

                val cost = extensionHours * 5.0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (currentLang == AppLanguage.AR) "الرسوم الإضافية" else "Additional Fee:", fontWeight = FontWeight.Bold)
                        Text(text = "$$cost", fontWeight = FontWeight.Black)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("btn_extend_cancel")) {
                        Text(Localization.get("cancel", currentLang))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(extensionHours) },
                        modifier = Modifier.testTag("btn_extend_confirm")
                    ) {
                        Text(if (currentLang == AppLanguage.AR) "أكد التمديد" else "Confirm Extension")
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentDialog(
    amount: Double,
    paymentType: String,
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("APPLE_PAY") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Localization.get("pay_now", currentLang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Completing transaction for $paymentType", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "${Localization.get("amount", currentLang)}: $$amount", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                Spacer(modifier = Modifier.height(14.dp))

                Text(text = if (currentLang == AppLanguage.AR) "اختر وسيلة الدفع الآمنة المعتمدة" else "Select Authorized Payment Method", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                listOf("APPLE_PAY", "CREDIT_CARD", "UNIV_ACCOUNT").forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMethod = method }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            modifier = Modifier.testTag("radio_pay_$method")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Localization.get(method, currentLang))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("btn_payment_cancel")) {
                        Text(Localization.get("cancel", currentLang))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedMethod) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.testTag("btn_payment_confirm")
                    ) {
                        Text(if (currentLang == AppLanguage.AR) "سداد الآن" else "Authorize & Pay")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun rememberScrollState(): androidx.compose.foundation.ScrollState {
    return androidx.compose.foundation.rememberScrollState()
}
