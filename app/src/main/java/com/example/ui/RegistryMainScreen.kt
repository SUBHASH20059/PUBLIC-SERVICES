package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistryMainScreen(viewModel: RegistryViewModel, securityViewModel: SecurityViewModel, businessViewModel: BusinessViewModel, gstViewModel: GstViewModel, civilRegistryViewModel: CivilRegistryViewModel) {
    val allRecords by viewModel.allRecords.collectAsState()
    val allChangeRequests by viewModel.allChangeRequests.collectAsState()
    val allCourtOrders by viewModel.allCourtOrders.collectAsState()

    var showInfoAlert by remember { mutableStateOf(false) }

    val langHeaders = remember {
        mapOf(
            "English" to Triple("LEKHA CIVIL TRUST", "National Legal Ledger • Constitution Compliant", "Change Language"),
            "Hindi" to Triple("लेख नागरिक न्यास", "राष्ट्रीय कानूनी बहीखाता • भारतीय संविधान के अनुरूप", "भाषा बदलें"),
            "Telugu" to Triple("లేఖా సివిల్ ట్రస్ట్", "జాతీయ న్యాయ రిజిస్టర్ • భారత రాజ్యాంగబద్ధమైనది", "భాषా మార్చండి"),
            "Tamil" to Triple("லேகா சிவில் அறக்கட்டளை", "தேசிய சட்டப் பேரேடு • இந்திய அரசியலமைப்பிற்கு உட்பட்டது", "மொழியை மாற்று"),
            "Kannada" to Triple("ಲೇಖಾ ಸಿವಿಲ್ ಟ್ರಸ್ಟ್", "ರಾಷ್ಟ್ರೀಯ ಕಾನೂನು ರಿಜಿಸ್ಟರ್ • ಭಾರತೀಯ ಸಂವಿಧಾನಕ್ಕೆ ಬದ್ಧವಾಗಿದೆ", "ಭಾಷೆ ಬದಲಾಯಿಸಿ"),
            "Bengali" to Triple("লেখা সিভিল ট্রাস্ট", "জাতীয় আইনি খতিয়ান • ভারতীয় সংবিধানের অনুবর্তী", "ভাষা পরিবর্তন করুন"),
            "Marathi" to Triple("लेखा सिव्हिल ट्रस्ट", "राष्ट्रीय कायदेशीर बहीखाता • भारतीय संविधानाचे पालन करणारे", "भाषा बदला"),
            "Malayalam" to Triple("ലേഖാ സിവിൽ ട്രസ്റ്റ്", "ദേശീയ നിയമ രജിസ്റ്റർ • ഇന്ത്യൻ ഭരണഘടനാനുസൃതം", "ഭാഷ മാറ്റുക")
        )
    }

    val currentLangData = langHeaders[viewModel.currentLanguage] ?: Triple("LEKHA CIVIL TRUST", "National Legal Ledger • Constitution Compliant", "Change Language")
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = "Legal Registry logo",
                                tint = Saffron,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentLangData.first,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = Color.White
                            )
                        }
                        Text(
                            text = currentLangData.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedBlueText
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showLanguageDialog = true },
                        modifier = Modifier.testTag("lang_selector_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = currentLangData.third,
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { showInfoAlert = true },
                        modifier = Modifier.testTag("info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Constitutional Rules Info",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlue
                )
            )
        },
        bottomBar = {
            // Portal Tab selectors
            NavigationBar(
                containerColor = DarkBg,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = viewModel.activePortalTab == 0,
                    onClick = { viewModel.activePortalTab = 0 },
                    icon = { Icon(Icons.Default.Public, contentDescription = "Public") },
                    label = { Text("Public", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Saffron,
                        selectedTextColor = Saffron,
                        indicatorColor = DeepBlue,
                        unselectedIconColor = MutedBlueText,
                        unselectedTextColor = MutedBlueText
                    ),
                    modifier = Modifier.testTag("tab_public")
                )
                NavigationBarItem(
                    selected = viewModel.activePortalTab == 1,
                    onClick = { viewModel.activePortalTab = 1 },
                    icon = { Icon(Icons.Default.Badge, contentDescription = "Owner") },
                    label = { Text("Legal Owner", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Saffron,
                        selectedTextColor = Saffron,
                        indicatorColor = DeepBlue,
                        unselectedIconColor = MutedBlueText,
                        unselectedTextColor = MutedBlueText
                    ),
                    modifier = Modifier.testTag("tab_owner")
                )
                NavigationBarItem(
                    selected = viewModel.activePortalTab == 2,
                    onClick = { viewModel.activePortalTab = 2 },
                    icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Officer") },
                    label = { Text("Officer Chamber", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Saffron,
                        selectedTextColor = Saffron,
                        indicatorColor = DeepBlue,
                        unselectedIconColor = MutedBlueText,
                        unselectedTextColor = MutedBlueText
                    ),
                    modifier = Modifier.testTag("tab_officer")
                )
                NavigationBarItem(
                    selected = viewModel.activePortalTab == 3,
                    onClick = { viewModel.activePortalTab = 3 },
                    icon = { Icon(Icons.Default.Balance, contentDescription = "Judiciary") },
                    label = { Text("Court Orders", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Saffron,
                        selectedTextColor = Saffron,
                        indicatorColor = DeepBlue,
                        unselectedIconColor = MutedBlueText,
                        unselectedTextColor = MutedBlueText
                    ),
                    modifier = Modifier.testTag("tab_judiciary")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Contents based on selected tab
            when (viewModel.activePortalTab) {
                0 -> PublicPortalScreen(viewModel, allRecords)
                1 -> LegalOwnerPortalScreen(viewModel)
                2 -> OfficerChamberScreen(viewModel, allRecords)
                3 -> CourtDecreeScreen(viewModel, allCourtOrders, allRecords)
            }
        }
    }

    BiometricScannerDialog(viewModel)
    if (viewModel.selectedVaultItem != null) {
        SecureVaultDetailsDialog(
            viewModel = viewModel,
            item = viewModel.selectedVaultItem!!,
            onDismiss = {
                viewModel.selectedVaultItem = null
                viewModel.vaultDecryptionPin = ""
                viewModel.vaultDecryptionError = ""
                viewModel.vaultDecryptedDetails = null
            }
        )
    }
    if (viewModel.showAddVaultItemDialog) {
        AddVaultItemDialog(viewModel = viewModel)
    }

    if (showInfoAlert) {
        AlertDialog(
            onDismissRequest = { showInfoAlert = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Saffron)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Constitutional Safeguards", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Lekha Registry operates under the direct mandates of the Constitution of India:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• Article 300A: 'No person shall be deprived of his property save by authority of law.' Accordingly, land deed title shifts must satisfy judicial decrees and formal registration.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Article 21: Safeguards individual civil agreements and certificates including marriages as an inherent right to privacy and human dignity.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Registration Act, 1908: Renders complete legal compliance mandatory for high value assets, stamp duty check, and government appraisal limits.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Legal Charge Protection: Financial charges on loans cannot be altered, added, or erased by non-judicial entities, securing the banking system from corrupt audits.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoAlert = false }) {
                    Text("De Jure Cleared")
                }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = Saffron)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Sovereign Language", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val languages = listOf("English", "Hindi", "Telugu", "Tamil", "Kannada", "Bengali", "Marathi", "Malayalam")
                    items(languages) { lang ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.currentLanguage = lang
                                    viewModel.addAlert("Language switched to $lang successfully.")
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 4.dp),
                            border = if (viewModel.currentLanguage == lang) BorderStroke(1.5.dp, Saffron) else null,
                            colors = CardDefaults.cardColors(
                                containerColor = if (viewModel.currentLanguage == lang) DeepBlue else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = lang,
                                    fontWeight = if (viewModel.currentLanguage == lang) FontWeight.Bold else FontWeight.Normal,
                                    color = if (viewModel.currentLanguage == lang) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (viewModel.currentLanguage == lang) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Saffron)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// ------------------ PORTAL 0: PUBLIC PORTAL ------------------

@Composable
fun PublicPortalScreen(viewModel: RegistryViewModel, allRecords: List<RegistryRecord>) {
    val blockchainBlocks by viewModel.allBlockchainBlocks.collectAsState(initial = emptyList())
    val valuationsList by viewModel.allValuations.collectAsState(initial = emptyList())
    var userTabSelection by remember { mutableStateOf(0) }
    var generatedNoticeLog by remember { mutableStateOf("") }

    val currentMfa = viewModel.currentMfaAccount

    if (currentMfa == null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Image generated dynamically
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.justice_banner_1781851630588),
                        contentDescription = "Scales of Justice Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            "Unified Democratic Legal Vault",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            "Securing ownership, marriages, and business integrity for 1.4 Billion citizens.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Secure Portal Access Barrier
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, DeepBlue.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Saffron, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Federal Identity Gate",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepBlue
                            )
                        }

                        Text(
                            text = "To access secure deeds, initial filings, or pending administrative procedures, you must authenticate your Aadhaar Unique Identification (UID). To protect privacy, generic public browsing of private property databases is blocked.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        // Selector for login / enroll
                        val isLoginForm = viewModel.mfaActiveFormMode == "LOGIN"
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.mfaActiveFormMode = "LOGIN" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLoginForm) DeepBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isLoginForm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text("Secure Login", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = { viewModel.mfaActiveFormMode = "SIGN_UP" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isLoginForm) DeepBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (!isLoginForm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text("Enroll MFA ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (viewModel.mfaCurrentStep == "OTP") {
                            // SMS OTP block inside Public portal
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Smartphone, contentDescription = null, tint = Saffron, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SMS OTP Verification Required", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DeepBlue)
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Saffron.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, Saffron.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Sms, contentDescription = null, tint = Saffron, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Sovereign OTP SMS Broadcast", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = DeepBlue)
                                        Text("Federal OTP TOKEN: ${viewModel.generatedOtpCode}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = viewModel.enteredOtpCode,
                                onValueChange = { viewModel.enteredOtpCode = it },
                                label = { Text("6-Digit OTP Code") },
                                modifier = Modifier.fillMaxWidth().testTag("mfa_otp_public_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.enteredOtpCode = viewModel.generatedOtpCode },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Auto-Fill OTP", fontSize = 11.sp) }
                                Button(
                                    onClick = { viewModel.verifyOtpAndSettleSession() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                                    modifier = Modifier.weight(1f).testTag("mfa_otp_verify_public_btn")
                                ) { Text("Verify SMS Code", fontSize = 11.sp, color = Color.White) }
                            }
                        } else if (isLoginForm) {
                            Text("Pre-seeded profiles (Click to check-in):", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                viewModel.registeredMfaAccounts.forEach { acc ->
                                    FilterChip(
                                        selected = viewModel.mfaLoginAadhaarOrPan == acc.aadhaar,
                                        onClick = {
                                            viewModel.mfaLoginAadhaarOrPan = acc.aadhaar
                                            viewModel.mfaLoginPin = acc.securityPin
                                            viewModel.addAlert("Preloaded parameters for ${acc.fullName}.")
                                        },
                                        label = { Text(acc.fullName, fontSize = 9.sp) }
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = viewModel.mfaLoginAadhaarOrPan,
                                onValueChange = { viewModel.mfaLoginAadhaarOrPan = it },
                                label = { Text("Aadhaar UID-xxxx-xxxx-xxxx") },
                                modifier = Modifier.fillMaxWidth().testTag("public_mfa_login_uid"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = viewModel.mfaLoginPin,
                                onValueChange = { viewModel.mfaLoginPin = it },
                                label = { Text("6-Digit PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("public_mfa_login_pin"),
                                singleLine = true
                            )
                            if (viewModel.mfaLoginError.isNotBlank()) {
                                Text(viewModel.mfaLoginError, color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.submitMfaLogin() },
                                modifier = Modifier.fillMaxWidth().testTag("public_mfa_login_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Verify Identity Gate", fontSize = 11.sp)
                                }
                            }
                        } else {
                            // SignUp Flow
                            OutlinedTextField(
                                value = viewModel.mfaSignUpName,
                                onValueChange = { viewModel.mfaSignUpName = it },
                                label = { Text("Full Legal Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = viewModel.mfaSignUpAadhaar,
                                onValueChange = { viewModel.mfaSignUpAadhaar = it },
                                label = { Text("Unique Aadhaar ID (UID-xxxx-xxxx-xxxx)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = viewModel.mfaSignUpPan,
                                onValueChange = { viewModel.mfaSignUpPan = it },
                                label = { Text("PAN Number (ABCDE1234F)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = viewModel.mfaSignUpPhone,
                                onValueChange = { viewModel.mfaSignUpPhone = it },
                                label = { Text("SMS Communication Phone") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = viewModel.mfaSignUpPin,
                                onValueChange = { viewModel.mfaSignUpPin = it },
                                label = { Text("6-Digit Security PIN") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            Button(
                                onClick = { viewModel.submitMfaSignUp() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                            ) {
                                Text("Enroll Citizen Credentials")
                            }
                        }
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Identity Banner for logged in user
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DharmaGreen.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DharmaGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = DharmaGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SECURE COGNITIVE WALLET", fontWeight = FontWeight.Bold, color = DharmaGreen, fontSize = 10.sp)
                        }
                        Text(
                            text = "Citizen: ${currentMfa.fullName}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepBlue
                        )
                        Text(
                            text = "Aadhaar: ${currentMfa.aadhaar} • PAN: ${currentMfa.pan}",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = { viewModel.logoutMfa() }) {
                        Icon(Icons.Default.LockOpen, contentDescription = "Sign Out", tint = DangerRed)
                    }
                }
            }
        }

        // Hero Image generated dynamically
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.justice_banner_1781851630588),
                    contentDescription = "Scales of Justice Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        "Unified Democratic Legal Vault",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        "Securing ownership, marriages, and business integrity for 1.4 Billion citizens.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Action Toggles
        item {
            ScrollableTabRow(
                selectedTabIndex = userTabSelection,
                containerColor = Color.Transparent,
                contentColor = Saffron,
                modifier = Modifier.testTag("public_tab_row"),
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = userTabSelection == 0,
                    onClick = { userTabSelection = 0 },
                    text = { Text("Registry Ledger", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = userTabSelection == 1,
                    onClick = { userTabSelection = 1 },
                    text = { Text("Search Bureau", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = userTabSelection == 2,
                    onClick = { userTabSelection = 2 },
                    text = { Text("Gov Service Hub", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = userTabSelection == 3,
                    onClick = { userTabSelection = 3 },
                    text = { Text("Civil Dispute Desk", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = userTabSelection == 4,
                    onClick = { userTabSelection = 4 },
                    text = { Text("Lawyer Network", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = userTabSelection == 5,
                    onClick = { userTabSelection = 5 },
                    text = { Text("PostgreSQL Audit Trail", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    modifier = Modifier.testTag("public_tab_audit")
                )
                Tab(
                    selected = userTabSelection == 6,
                    onClick = { userTabSelection = 6 },
                    text = { Text("Blockchain Vault", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    modifier = Modifier.testTag("public_tab_blockchain")
                )
            }
        }

        if (userTabSelection == 0) {
            // General Public Ledger
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Official National Deeds",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(
                        onClick = { viewModel.showRegisterDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                        modifier = Modifier.testTag("register_request_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Request", fontSize = 12.sp)
                    }
                }
            }

            val myAadhaar = viewModel.currentMfaAccount?.aadhaar ?: ""
            val approvedRecords = allRecords.filter { it.status == "APPROVED" && it.ownerUniqueId == myAadhaar }
            if (approvedRecords.isEmpty()) {
                item {
                    EmptyStatePlaceholder("You currently have no approved properties recorded under Aadhaar: $myAadhaar")
                }
            } else {
                items(approvedRecords) { record ->
                    RegistryCard(record = record, onActionClick = null)
                }
            }
        } else if (userTabSelection == 1) {
            // Search Registry by UID (Aadhaar or Government unique ID)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Secure Search Ledger",
                            fontWeight = FontWeight.Bold,
                            color = DeepBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Enter the individual's Government-issued Unique Identification Number (UID) to look up connected Land deeds, Marriage contracts, commercial covenants, or Loans.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = viewModel.publicSearchUid,
                            onValueChange = { viewModel.publicSearchUid = it },
                            label = { Text("Owner unique UID (e.g., UID-8942-1029-9912)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_uid_input"),
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.searchPublicByUid() },
                                    modifier = Modifier.testTag("search_action_btn")
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Search icon")
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Saffron
                            )
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Search Findings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            val myAadhaar = viewModel.currentMfaAccount?.aadhaar ?: ""
            val searchResults = viewModel.publicSearchResult?.filter { it.ownerUniqueId == myAadhaar }
            if (searchResults == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = BlueGrey)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Awaiting search parameters. Ensure correct UID format is entered.", fontSize = 12.sp, color = BlueGrey, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else if (searchResults.isEmpty()) {
                item {
                    EmptyStatePlaceholder("No matching records found for this Unique Identification Number.")
                }
            } else {
                items(searchResults) { record ->
                    RegistryCard(record = record, onActionClick = null)
                }
            }
        } else if (userTabSelection == 2) {
            // Unified Government Service Hub (Passport, PAN, DL progress tracking 1 to 8 steps)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Unified Goverment Service Hub",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Submit, verify and download essential national sovereign credentials under direct federal e-service mandates.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // New Application Form Card
            item {
                var citizenName by remember { mutableStateOf("") }
                var citizenUid by remember { mutableStateOf("") }
                var selectedService by remember { mutableStateOf("Passport") }
                val serviceTypes = listOf("Passport", "PAN Card", "Driving License", "Birth Certificate", "Death Certificate", "Income Tax Services")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.3f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Apply for New Sovereign Credential", fontWeight = FontWeight.Bold, color = DeepBlue)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = citizenName,
                            onValueChange = { citizenName = it },
                            label = { Text("Applicant Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = citizenUid,
                            onValueChange = { citizenUid = it },
                            label = { Text("Government unique UID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Select Sovereign Service Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val chunkedServices = serviceTypes.take(3)
                            chunkedServices.forEach { choice ->
                                FilterChip(
                                    selected = selectedService == choice,
                                    onClick = { selectedService = choice },
                                    label = { Text(choice, fontSize = 9.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val chunkedServices = serviceTypes.drop(3)
                            chunkedServices.forEach { choice ->
                                FilterChip(
                                    selected = selectedService == choice,
                                    onClick = { selectedService = choice },
                                    label = { Text(choice, fontSize = 9.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (citizenName.isNotBlank() && citizenUid.isNotBlank()) {
                                    viewModel.submitGovService(selectedService, citizenName, citizenUid)
                                    citizenName = ""
                                    citizenUid = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                        ) {
                            Text("Submit Sovereign File")
                        }
                    }
                }
            }

            item {
                Text("Ongoing Applications Progress List (Steps 1 to 8)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            val myAadhaar = viewModel.currentMfaAccount?.aadhaar ?: ""
            val myGovServiceApplications = viewModel.govServiceApplications.filter { it.citizenUid == myAadhaar }
            items(myGovServiceApplications) { app ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(containerColor = Saffron.copy(alpha = 0.15f)) {
                                Text(app.serviceName, color = DeepBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                            }
                            Text(text = "App ID: #${app.id}", fontSize = 10.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Applicant: ${app.citizenName} • UID: ${app.citizenUid}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(text = "Last verified: ${app.lastUpdate}", fontSize = 10.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))
                        // Progress Bar (1 to 8 Step rendering)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Step ${app.currentStep} of 8", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (app.status == "APPROVED") "APPROVED & ISSUED" else "VERIFYING IN RE-ROUTE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (app.status == "APPROVED") DharmaGreen else Saffron
                            )
                        }
                        LinearProgressIndicator(
                            progress = { app.currentStep / 8.0f },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = DeepBlue,
                            trackColor = Color.LightGray
                        )

                        // Progress milestone tags
                        Text(
                            text = when (app.currentStep) {
                                1 -> "Verification Milestone: [1/8] Citizen Record Pulled"
                                2 -> "Verification Milestone: [2/8] Automated Risk Analyzed"
                                3 -> "Verification Milestone: [3/8] UID Biometrics Cleared"
                                4 -> "Verification Milestone: [4/8] State Revenue Crosscheck"
                                5 -> "Verification Milestone: [5/8] Income Tax Audit Cleared"
                                6 -> "Verification Milestone: [6/8] Police Verification Complete"
                                7 -> "Verification Milestone: [7/8] IAS Magistrate Ledger Seal"
                                8 -> "Verification Milestone: [8/8] Certificate Dispatched & Saved to AES Vault"
                                else -> "Completed"
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else if (userTabSelection == 3) {
            // Civil Dispute Desk (Post-Marriage counseling & stage wise dispute tracking 1 to 5)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Constitutional Civil Marriage & Dispute Mediation Desk",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Providing end-to-end counselor assistance, mutual mediation routing, legal warnings, and formal court filing protocols in compliance with Article 21.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Submit Complaint Form
            item {
                var compReporterName by remember { mutableStateOf("") }
                var compReporterUid by remember { mutableStateOf("") }
                var compPartnerName by remember { mutableStateOf("") }
                var compDetails by remember { mutableStateOf("") }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Log Civil Dispute & Request Mediation Assistance", fontWeight = FontWeight.Bold, color = DeepBlue)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = compReporterName,
                            onValueChange = { compReporterName = it },
                            label = { Text("Reporter Spouse Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = compReporterUid,
                            onValueChange = { compReporterUid = it },
                            label = { Text("Reporter Unique UID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = compPartnerName,
                            onValueChange = { compPartnerName = it },
                            label = { Text("Partner Spouse Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = compDetails,
                            onValueChange = { compDetails = it },
                            label = { Text("Brief of Dispute (Property policy, marital division, counseling demand)") },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (compReporterName.isNotBlank() && compPartnerName.isNotBlank() && compDetails.isNotBlank()) {
                                    viewModel.registerMarriageComplaint(compReporterName, compReporterUid, compPartnerName, compDetails)
                                    compReporterName = ""
                                    compReporterUid = ""
                                    compPartnerName = ""
                                    compDetails = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                        ) {
                            Text("Register Dispute for Mediation")
                        }
                    }
                }
            }

            item {
                Text("Stage-wise Counseling & Conciliation Files", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            val myAadhaar = viewModel.currentMfaAccount?.aadhaar ?: ""
            val myMarriageComplaints = viewModel.marriageComplaintsList.filter { it.reporterUid == myAadhaar }
            items(myMarriageComplaints) { complaint ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Complaint Reference: ID #${complaint.id}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Saffron)
                            Badge(containerColor = if (complaint.status == "ACTIVE") DeepBlue.copy(alpha = 0.1f) else DharmaGreen.copy(alpha = 0.1f)) {
                                Text(
                                    text = complaint.status,
                                    color = if (complaint.status == "ACTIVE") DeepBlue else DharmaGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Reporter: ${complaint.reporterName} vs Partner: ${complaint.partnerName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(text = "Details entered: ${complaint.complaintDetails}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Text(text = "Dispute Timestamp: ${complaint.timestamp}", fontSize = 10.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render Stage Tracker 1 to 5
                        Text("Mediation Resolution Tracker:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth().background(DeepBlue.copy(alpha = 0.04f)).padding(8.dp)
                        ) {
                            for (i in 1..5) {
                                val active = complaint.stage >= i
                                val isCurrent = complaint.stage == i
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (active) Icons.Default.Check else Icons.Default.Add,
                                        contentDescription = null,
                                        tint = if (isCurrent) Saffron else if (active) DharmaGreen else Color.LightGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when(i) {
                                            1 -> "Stage 1: Counseling (Mutual Psychological Support)"
                                            2 -> "Stage 2: Mediation (Constitutional Arbiters)"
                                            3 -> "Stage 3: Legal Notice Issuance (Sovereign Warning)"
                                            4 -> "Stage 4: Lawyer Assignment (Verified Counsel Allocation)"
                                            5 -> "Stage 5: Court Filing Asssitance (Final Judicial Adjudication)"
                                            else -> ""
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) Saffron else if (active) MaterialTheme.colorScheme.onSurface else Color.LightGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { viewModel.advanceComplaintStage(complaint.id) }
                            ) {
                                Text(
                                    text = if (complaint.stage < 5) "Advance Mediation Stage" else "Settle Dispute Case",
                                    color = Saffron,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        } else if (userTabSelection == 4) {
            // Lawyer Network / Advocate Marketplace Lookups
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "National Advocate Network & Notary Marketplace",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Find and retain legal advisors. Search transparently by specialization, fee structure, and sovereign certification ratings.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // ________________ Official Sovereign Notice Generator Workspace ________________
            item {
                var showNoticeBuilder by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepBlue.copy(alpha = 0.04f)),
                    border = BorderStroke(1.dp, Saffron.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Balance, contentDescription = null, tint = Saffron)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lekha Civil Legal Notice Workstation", fontWeight = FontWeight.Bold, color = DeepBlue)
                            }
                            IconButton(onClick = { showNoticeBuilder = !showNoticeBuilder }) {
                                Icon(
                                    imageVector = if (showNoticeBuilder) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand notice drafting desk",
                                    tint = DeepBlue
                                )
                            }
                        }
                        Text(
                            "Draft, customize, and register certified legal civil notices with verified Bar Council advocates to demand compliance on property borders, marriage defaults, or loan charges.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        if (showNoticeBuilder) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = viewModel.noticeSenderName,
                                onValueChange = { viewModel.noticeSenderName = it },
                                label = { Text("Client / Sender Name (e.g. Ramesh Kumar)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = viewModel.noticeReceiverName,
                                onValueChange = { viewModel.noticeReceiverName = it },
                                label = { Text("Receiver / Counter Party (e.g. Sunil Mehta)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = viewModel.noticeLegalSubject,
                                onValueChange = { viewModel.noticeLegalSubject = it },
                                label = { Text("Disputed Property Title / Deed (e.g. Pune Survey Sector 3-A)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Select Notice Act & Jurisdiction:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val noticeTypes = listOf(
                                "Section 80 CPC (Sovereign Notice)",
                                "Section 106 Transfer of Property Act (Lease default)",
                                "Section 138 Negotiable Instruments Act (Cheque dispute)"
                            )
                            noticeTypes.forEach { nType ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.noticeTypeCPC = nType }.padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = viewModel.noticeTypeCPC == nType,
                                        onClick = { viewModel.noticeTypeCPC = nType }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(nType, fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Assign Verified Advocate:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val advocates = listOf("Adv. Menaka Guruswamy", "Adv. Harish Salve", "Adv. Indira Jaising", "Adv. KK Venugopal")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                advocates.forEach { advocate ->
                                    FilterChip(
                                        selected = viewModel.noticeLawyerName == advocate,
                                        onClick = { viewModel.noticeLawyerName = advocate },
                                        label = { Text(advocate, fontSize = 9.sp) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = viewModel.noticeGrievanceDetails,
                                onValueChange = { viewModel.noticeGrievanceDetails = it },
                                label = { Text("Civil Grievance Facts & Demands") },
                                modifier = Modifier.fillMaxWidth().height(90.dp),
                                maxLines = 4,
                                placeholder = { Text("Describe land encroachment bounds, matrimonial maintenance breach, or default parameters...") }
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.generateLegalNoticeDocument(viewModel.noticeLegalSubject) },
                                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Official Certified Notice")
                            }
                        }
                    }
                }
            }

            // Generated display panel
            if (viewModel.showNoticeBuilderResult && viewModel.activeNoticeDocumentText.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)), // Beautiful Parchment tone
                        border = BorderStroke(1.5.dp, Saffron)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = DharmaGreen)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SOVEREIGN COURT DESK DISPATCH", fontWeight = FontWeight.Bold, color = DharmaGreen, fontSize = 12.sp)
                                }
                                IconButton(onClick = { viewModel.showNoticeBuilderResult = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Dispatch Preview", tint = Color.Gray)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.activeNoticeDocumentText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color.DarkGray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .border(1.dp, Color.LightGray)
                                    .padding(12.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.addAlert("Sovereign certified stamp notice sent securely to national registrar via SHA-digest!") },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dispatch Notice", fontSize = 11.sp, color = Color.White)
                                }
                                Button(
                                    onClick = { viewModel.addAlert("Notice exported as dynamic legal PDF.") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Print notice PDF", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Retention & Automated Notice Generation feedback dialog helper
            if (generatedNoticeLog.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.5.dp, DharmaGreen),
                        colors = CardDefaults.cardColors(containerColor = DharmaGreen.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = DharmaGreen)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LEGAL NOTICE REGISTERED SECURELY", fontWeight = FontWeight.Bold, color = DharmaGreen)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = generatedNoticeLog, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { generatedNoticeLog = "" }) {
                                Text("Dismiss notice snapshot", fontSize = 11.sp, color = DharmaGreen)
                            }
                        }
                    }
                }
            }

            // Filter Chips selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Specialization:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    val specs = listOf("ALL", "Property", "Family", "Constitutional", "Loan")
                    specs.forEach { spec ->
                        FilterChip(
                            selected = viewModel.filteredLawyersSpecialization == spec,
                            onClick = { viewModel.filteredLawyersSpecialization = spec },
                            label = { Text(spec, fontSize = 10.sp) }
                        )
                    }
                }
            }

            val matchingLawyers = viewModel.lawyerNetworkList.filter {
                viewModel.filteredLawyersSpecialization == "ALL" || it.specialization.equals(viewModel.filteredLawyersSpecialization, ignoreCase = true)
            }

            items(matchingLawyers) { lawyer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = lawyer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = DeepBlue)
                                Text(
                                    text = "Sovereign Advocate Council Certified • Badge #IND-${(1000..9999).random()}",
                                    fontSize = 10.sp,
                                    color = DharmaGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Badge(containerColor = Saffron) {
                                Text(text = "★ ${lawyer.rating}", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Specialization: ${lawyer.specialization}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Standard Retainer: ${lawyer.fee}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DharmaGreen)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Saffron, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Availability: ${lawyer.availabilityStatus}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val generatedText = """
                                    LEGAL NOTICE SnapID: TS-${System.currentTimeMillis() / 1000}
                                    Pre-drafted by ${lawyer.name}
                                    Pursuant to Section 80 of Code of Civil Procedure, 1908.
                                    This demand notice mandates compliance on associated land survey boundaries / marital conditions.
                                    Immutable security digest logged with SHA-256 signature and registered to sovereign index.
                                """.trimIndent()
                                generatedNoticeLog = generatedText
                                viewModel.addAlert("Sovereign Legal Notice pre-drafted and verified by ${lawyer.name}.")
                                viewModel.logAuditAction(999, "LEGAL_NOTICE_GENERATED", "Auto-generated legal notice drafted by ${lawyer.name}.", "CITIZEN-901")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Draft Legal Notice & Appoint Counselor", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        } else if (userTabSelection == 5) {
            // Express Interceptor & PostgreSQL Audit Trail (Simulated Real-Time Backend Monitor)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, DharmaGreen.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(DharmaGreen, RoundedCornerShape(5.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Express Backend Interceptor Daemon",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Status: ACTIVE (Zero-Trust Intercept) • PostgreSQL Integrity Certified",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = DharmaGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This dashboard visualizes server-side log blocks captured by our Express Middleware. Every sensitive PUT, POST, or DELETE write action anywhere on Lekha Civil Trust is intercepted before reaching Postgres database storage to audit User credential claims, timestamps, physical IPs, and state differentials.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            val myAadhaar = viewModel.currentMfaAccount?.aadhaar ?: ""
            val myLogs = viewModel.middlewareAuditLogs.filter { it.userId == myAadhaar }

            if (myLogs.isEmpty()) {
                item {
                    EmptyStatePlaceholder("No sensitive write operations logged under your Aadhaar UID yet.")
                }
            } else {
                items(myLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBg),
                        border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Badge(containerColor = DeepBlue) {
                                        Text(
                                            log.method,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        log.endpoint,
                                        fontWeight = FontWeight.Bold,
                                        color = Saffron,
                                        fontSize = 12.sp,
                                        modifier = Modifier.testTag("audit_endpoint_${log.id}")
                                    )
                                }
                                Text(
                                    "#${log.id}",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BlueGrey.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Action and Sign details
                            Text(
                                "Intercepted Action: ${log.actionType}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("USER ID (UID)", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(log.userId, fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.SemiBold)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("CLIENT IP ADDRESS", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(log.clientIp, fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("TIMESTAMP", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(log.timestamp, fontSize = 11.sp, color = Color.LightGray)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("POSTGRESQL STATUS", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(log.dbStatus, fontSize = 10.sp, color = DharmaGreen, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .padding(8.dp)
                                    .border(0.5.dp, BlueGrey.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            ) {
                                Column {
                                    Text("STATE TRANSITION METRICS:", fontSize = 9.sp, color = Saffron, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "↩️ PREVIOUS: ${log.previousState}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.LightGray
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "🆕 CURRENT:  ${log.newState}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (userTabSelection == 6) {
            // ________________ Blockchain & Property Valuation Desk ________________
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Saffron.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = Saffron, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Blockchain & Valuation Vault",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Every property appraisal and ownership transition generates a mined block, cryptographically chained using SHA-256 integrity hashes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // SECTION 1: Blockchain Explorer
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Cryptographic Block Explorer (Active Chain)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Badge(containerColor = DharmaGreen) {
                        Text(
                            "Live Chain Synced",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            val myAadhaar = viewModel.currentMfaAccount?.aadhaar ?: ""
            val myBlocks = blockchainBlocks.filter { it.payload.contains(myAadhaar) || it.transactionType == "GENESIS" }

            if (myBlocks.isEmpty()) {
                item {
                    EmptyStatePlaceholder("No blockchain ledger hashes registered for your properties or events yet.")
                }
            } else {
                items(myBlocks) { block ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkBg),
                        border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (block.transactionType) {
                                            "GENESIS" -> Icons.Default.Home
                                            "DEED_GEN" -> Icons.Default.Description
                                            "TITLE_TRANS" -> Icons.Default.Refresh
                                            else -> Icons.Default.Link
                                        },
                                        contentDescription = null,
                                        tint = Saffron,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Block #${block.blockIndex} [${block.transactionType}]",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Saffron
                                    )
                                }
                                Text(
                                    text = "Nonce: ${block.nonce}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                text = "Hash: ${block.hash}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = DharmaGreen,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.testTag("block_hash_${block.id}")
                            )
                            Text(
                                text = "Prev Hash: ${block.previousHash}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = BlueGrey.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Block Payload (Immutable Log):", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(
                                text = block.payload,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .padding(6.dp)
                            )
                        }
                    }
                }
            }

            // SECTION 2: Valuation Vault
            item {
                Text(
                    "Sovereign Land Valuation Calculator Vault",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, Saffron.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Calculate government assessed guideline property value mapped to real-time development premiums.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = viewModel.valuationPropName,
                            onValueChange = { viewModel.valuationPropName = it },
                            label = { Text("Property Name / Survey Detail") },
                            modifier = Modifier.fillMaxWidth().testTag("valuation_prop_name_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = viewModel.valuationAreaInSqFt,
                                onValueChange = { viewModel.valuationAreaInSqFt = it },
                                label = { Text("Area (in Sq. Ft)") },
                                modifier = Modifier.weight(1f).testTag("valuation_area_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = viewModel.valuationMultiplier,
                                onValueChange = { viewModel.valuationMultiplier = it },
                                label = { Text("Development Multiplier") },
                                modifier = Modifier.weight(1f).testTag("valuation_multiplier_input"),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Select Revenue Mapped Zone:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("Premium Commercial", "High-Density Urban", "Semi-Urban", "Agricultural").forEach { zone ->
                                FilterChip(
                                    selected = viewModel.valuationZone == zone,
                                    onClick = { viewModel.valuationZone = zone },
                                    label = { Text(zone, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (viewModel.valuationError.isNotBlank()) {
                            Text(viewModel.valuationError, color = DangerRed, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }

                        Button(
                            onClick = { viewModel.calculatePropertyValuation() },
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                            modifier = Modifier.fillMaxWidth().testTag("calculate_valuation_btn")
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Calculate and Seal Value Certificate")
                        }
                    }
                }
            }

            // Valuation Certificate result card
            val resultCert = viewModel.valuationResult
            if (resultCert != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)), // Parchment
                        border = BorderStroke(2.dp, Saffron)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = DharmaGreen)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SOVEREIGN STATE APPRAISAL CERTIFICATE", fontWeight = FontWeight.Bold, color = DharmaGreen, fontSize = 11.sp)
                                }
                                Badge(containerColor = DharmaGreen) {
                                    Text("VALUED SECURE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text("Property: ${resultCert.propertyName}", fontWeight = FontWeight.Bold, color = DeepBlue)
                            Text("Zone Classification: ${resultCert.zoneClassification}", fontSize = 11.sp)
                            Text("Acreage/Area Metrics: ${resultCert.landAreaSqFt} sq ft", fontSize = 11.sp)
                            Text("Guideline Rate: ₹${resultCert.regionalGuidelineRate} / sq ft", fontSize = 11.sp)
                            Text("Developmental Factor: x${resultCert.developmentalPremiumMultiplier}", fontSize = 11.sp)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Saffron.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("TOTAL ASSESSED FAIR VALUATION:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(
                                "₹${String.format("%,.2f", resultCert.overallAssessedValue)} INR",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepBlue
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .border(1.dp, Color.LightGray)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("BLOCKCHAIN PROOF SEAL & DIGEST:", fontSize = 8.sp, color = Saffron, fontWeight = FontWeight.Bold)
                                    Text(
                                        resultCert.blockchainSealHash,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // HISTORIC VALUE CERTIFICATE LIST
            if (valuationsList.isNotEmpty()) {
                item {
                    Text(
                        "Historic Appraisal Archives",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                items(valuationsList) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, Color.LightGray)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(entry.propertyName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepBlue)
                                Text("Assessed: ₹${String.format("%,.0f", entry.overallAssessedValue)} (${entry.zoneClassification})", fontSize = 11.sp)
                                Text("Seal: ${entry.blockchainSealHash.take(24)}...", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.addAlert("Certificate PDF re-printed: Sealed reference hash valid.") }) {
                                Icon(Icons.Default.Print, contentDescription = "Reprint Certificate", tint = Saffron, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // New Request Dialog
    if (viewModel.showRegisterDialog) {
        Dialog(onDismissRequest = { viewModel.showRegisterDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "New Legal Deed Request",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepBlue
                        )
                        Text(
                            text = "Submissions are audited against constitution laws of India. Errors subject to Stamp Act penalties.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    item {
                        Text("Deed Classification", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("LAND", "MARRIAGE", "AGREEMENT", "LOAN").forEach { type ->
                                FilterChip(
                                    selected = viewModel.formType == type,
                                    onClick = { viewModel.formType = type },
                                    label = { Text(type, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formTitle,
                            onValueChange = { viewModel.formTitle = it },
                            label = { Text("Title (e.g., Agricultural Deed, Lease)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_title_input"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formOwnerName,
                            onValueChange = { viewModel.formOwnerName = it },
                            label = { Text("Primary Owner / Declarant Full Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_owner_name_input"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formOwnerUid,
                            onValueChange = { viewModel.formOwnerUid = it },
                            label = { Text("Owner unique UID (Aadhaar / Passport)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_owner_uid_input"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formDescription,
                            onValueChange = { viewModel.formDescription = it },
                            label = { Text("Deed description and terms of contract") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("form_desc_input"),
                            maxLines = 3
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formAdditionalParties,
                            onValueChange = { viewModel.formAdditionalParties = it },
                            label = { Text("Co-parties, Spouse, or Lender Name(s)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_parties_input"),
                            singleLine = true
                        )
                    }

                    if (viewModel.formType == "LOAN") {
                        item {
                            OutlinedTextField(
                                value = viewModel.formChargeValue,
                                onValueChange = { viewModel.formChargeValue = it },
                                label = { Text("Charge liability amount (INR)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_charge_input"),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, Saffron.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Saffron, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Lekha AI Fraud Shield Integration", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DeepBlue)
                                }
                                Text(
                                    "Your document deed and physical signature will be verified against India's digital biometric vault using real-time neural models to prevent duplicate claims. Select test scenario.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    mapOf(
                                        "LEGITIMATE" to "Green Pass",
                                        "FORGED_SIGNATURE" to "Forged Sign",
                                        "DUPLICATE_PARCEL" to "Duplicate Claim"
                                    ).forEach { (scenario, label) ->
                                        FilterChip(
                                            selected = viewModel.selectedDocumentScenario == scenario,
                                            onClick = { viewModel.selectedDocumentScenario = scenario },
                                            label = { Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = when(scenario) {
                                                    "LEGITIMATE" -> DharmaGreen
                                                    else -> DangerRed
                                                },
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Attachment: digital_deed_specimen_${viewModel.formType.lowercase()}_signed.pdf", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    if (viewModel.isCheckingFraud) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Lekha Backend Fraud Service Analyzer...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Saffron)
                                    Text("${(viewModel.fraudCheckProgress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { viewModel.fraudCheckProgress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Saffron,
                                    trackColor = Color.LightGray.copy(alpha = 0.2f),
                                )
                            }
                        }
                    }

                    if (viewModel.registerFormError.isNotBlank()) {
                        item {
                            Text(
                                text = viewModel.registerFormError,
                                color = DangerRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { viewModel.showRegisterDialog = false },
                                enabled = !viewModel.isCheckingFraud
                            ) {
                                Text("Cancel", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.submitNewRegistrationRequest() },
                                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                                modifier = Modifier.testTag("form_submit_btn"),
                                enabled = !viewModel.isCheckingFraud
                            ) {
                                Text(if (viewModel.isCheckingFraud) "Verifying..." else "Submit Request")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------ PORTAL 1: LEGAL OWNER PORTAL ------------------

@Composable
fun LegalOwnerPortalScreen(viewModel: RegistryViewModel) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Legal Owner Portal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Verify your constitutional properties/registries using your Unique Identification ID (UID). Request legal modifications or transfers securely.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedBlueText
                    )
                }
            }
        }

        item {
            if (viewModel.currentMfaAccount == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, DeepBlue.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Saffron, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Sovereign Multi-Factor Portal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepBlue
                            )
                        }

                        // Selector
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val loginSelected = viewModel.mfaActiveFormMode == "LOGIN"
                            Button(
                                onClick = { viewModel.mfaActiveFormMode = "LOGIN" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (loginSelected) DeepBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (loginSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Secure Login", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = { viewModel.mfaActiveFormMode = "SIGN_UP" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!loginSelected) DeepBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (!loginSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Enroll MFA Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (viewModel.mfaCurrentStep == "OTP") {
                            // Render OTP sub-step
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Smartphone, contentDescription = null, tint = Saffron, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Secure SMS Block: OTP Verification", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DeepBlue)
                                }
                                Text(
                                    text = "A dynamic security token has been generated and dispatched to: ${viewModel.mfaTempAccountForAuth?.phone}.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Saffron.copy(alpha = 0.12f)),
                                    border = BorderStroke(1.dp, Saffron.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Sms, contentDescription = null, tint = Saffron, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Sovereign OTP SMS Broadcast", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = DeepBlue)
                                            Text("Federal OTP: ${viewModel.generatedOtpCode}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = viewModel.enteredOtpCode,
                                    onValueChange = { viewModel.enteredOtpCode = it },
                                    label = { Text("6-Digit OTP Code") },
                                    modifier = Modifier.fillMaxWidth().testTag("mfa_otp_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.enteredOtpCode = viewModel.generatedOtpCode },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Auto-Fill OTP", fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.verifyOtpAndSettleSession() },
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                                        modifier = Modifier.weight(1f).testTag("mfa_otp_verify_btn")
                                    ) {
                                        Text("Verify SMS Factor", fontSize = 11.sp, color = Color.White)
                                    }
                                }

                                TextButton(
                                    onClick = { viewModel.logoutMfa() },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Abort Verification", color = DangerRed, fontSize = 11.sp)
                                }
                            }
                        } else {
                            // SELECT_FLOW / Forms
                            if (viewModel.mfaActiveFormMode == "LOGIN") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Sovereign Multi-Factor Sign-In", fontWeight = FontWeight.Bold, color = DeepBlue, fontSize = 13.sp)
                                    Text(
                                        text = "Supply registered identifier credentials and unlock 2-factor authentication layers.",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )

                                    // Preloaded accounts Filter chips
                                    Text("Pre-seeded profiles:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        viewModel.registeredMfaAccounts.forEach { acc ->
                                            FilterChip(
                                                selected = viewModel.mfaLoginAadhaarOrPan == acc.aadhaar,
                                                onClick = {
                                                    viewModel.mfaLoginAadhaarOrPan = acc.aadhaar
                                                    viewModel.mfaLoginPin = acc.securityPin
                                                    viewModel.addAlert("Preloaded parameters for ${acc.fullName}.")
                                                },
                                                label = { Text(acc.fullName, fontSize = 9.sp) }
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = viewModel.mfaLoginAadhaarOrPan,
                                        onValueChange = { viewModel.mfaLoginAadhaarOrPan = it },
                                        label = { Text("Aadhaar (UID-xxxx) or PAN") },
                                        modifier = Modifier.fillMaxWidth().testTag("mfa_login_uid"),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = viewModel.mfaLoginPin,
                                        onValueChange = { viewModel.mfaLoginPin = it },
                                        label = { Text("6-Digit Security PIN") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().testTag("mfa_login_pin"),
                                        singleLine = true
                                    )

                                    if (viewModel.mfaLoginError.isNotBlank()) {
                                        Text(viewModel.mfaLoginError, color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.submitMfaLogin() },
                                        modifier = Modifier.fillMaxWidth().testTag("mfa_login_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("Trigger Biometric Matching & OTP", fontSize = 11.sp)
                                        }
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Register Sovereign Multi-Factor Account", fontWeight = FontWeight.Bold, color = DeepBlue, fontSize = 13.sp)
                                    Text(
                                        text = "Align identifiers to establish multi-factor authentication (Aadhaar node scan + PAN tax match + SMS OTP).",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )

                                    OutlinedTextField(
                                        value = viewModel.mfaSignUpName,
                                        onValueChange = { viewModel.mfaSignUpName = it },
                                        label = { Text("Citizen Full Name") },
                                        modifier = Modifier.fillMaxWidth().testTag("mfa_signup_name"),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = viewModel.mfaSignUpAadhaar,
                                        onValueChange = { viewModel.mfaSignUpAadhaar = it },
                                        label = { Text("Aadhaar UID (e.g. UID-8942-1029-9912)") },
                                        modifier = Modifier.fillMaxWidth().testTag("mfa_signup_aadhaar"),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = viewModel.mfaSignUpPan,
                                        onValueChange = { viewModel.mfaSignUpPan = it },
                                        label = { Text("National PAN ID (e.g. PAN-CH67123A)") },
                                        modifier = Modifier.fillMaxWidth().testTag("mfa_signup_pan"),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = viewModel.mfaSignUpPhone,
                                        onValueChange = { viewModel.mfaSignUpPhone = it },
                                        label = { Text("Mobile Number (+91 XXXXX XXXXX)") },
                                        modifier = Modifier.fillMaxWidth().testTag("mfa_signup_phone"),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = viewModel.mfaSignUpPin,
                                        onValueChange = { viewModel.mfaSignUpPin = it },
                                        label = { Text("Sovereign Security PIN (6 digits)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().testTag("mfa_signup_pin"),
                                        singleLine = true
                                    )

                                    if (viewModel.mfaSignUpError.isNotBlank()) {
                                        Text(viewModel.mfaSignUpError, color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.submitMfaSignUp() },
                                        modifier = Modifier.fillMaxWidth().testTag("mfa_signup_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("Acknowledge & Register Factors", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Settle profile session badge
                val acc = viewModel.currentMfaAccount!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DharmaGreen.copy(alpha = 0.08f)),
                    border = BorderStroke(1.5.dp, DharmaGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = DharmaGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SECURE SOVEREIGN MFA ACTIVE", fontWeight = FontWeight.Bold, color = DharmaGreen, fontSize = 12.sp)
                            }
                            Badge(containerColor = DharmaGreen) {
                                Text("Tier-3 Active", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Authenticated Federal Profile Client:",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = acc.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Success badges
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(DharmaGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = DharmaGreen, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Aadhaar Verified", fontSize = 9.sp, color = DharmaGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(DharmaGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = DharmaGreen, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("PAN Validated", fontSize = 9.sp, color = DharmaGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(DharmaGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = DharmaGreen, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Biometrics Synced", fontSize = 9.sp, color = DharmaGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = DharmaGreen.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Details table
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Aadhaar UID:", fontSize = 11.sp, color = Color.Gray)
                                Text(acc.aadhaar, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("National PAN:", fontSize = 11.sp, color = Color.Gray)
                                Text(acc.pan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Mobile Device:", fontSize = 11.sp, color = Color.Gray)
                                Text(acc.phone, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Digital Signature Key:", fontSize = 11.sp, color = Color.Gray)
                                Text(viewModel.digitalSignatureHex, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- SECURE ENCLAVE DOCUMENT VAULT ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🛡️ Secure Citizen Enclave Vault",
                                fontWeight = FontWeight.Bold,
                                color = DeepBlue,
                                fontSize = 11.sp
                            )
                            TextButton(
                                onClick = { viewModel.showAddVaultItemDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("vault_seal_new_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Document", modifier = Modifier.size(12.dp), tint = DeepBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Seal Document", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Access, decrypt, and manage central agency federated records directly. All documents are encrypted with your security PIN.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val userVaultItems = viewModel.secureVaultItems.filter { it.citizenUid == acc.aadhaar }
                        if (userVaultItems.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.15f))
                            ) {
                                Box(modifier = Modifier.padding(12.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("No secure document references found in your Enclave Vault.", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                userVaultItems.forEach { valItem ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectedVaultItem = valItem }
                                            .testTag("vault_item_${valItem.id}"),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(
                                            width = 1.dp, 
                                            color = if (valItem.isDeclassified) DharmaGreen.copy(alpha = 0.5f) else BlueGrey.copy(alpha = 0.15f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.5f)) {
                                                Icon(
                                                    imageVector = if (valItem.isDeclassified) Icons.Default.LockOpen else Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = if (valItem.isDeclassified) DharmaGreen else Saffron,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(valItem.docTitle, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .background(
                                                                    if (valItem.isDeclassified) DharmaGreen.copy(alpha = 0.12f) 
                                                                    else DangerRed.copy(alpha = 0.08f)
                                                                )
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                .clip(RoundedCornerShape(3.dp))
                                                        ) {
                                                            Text(
                                                                text = if (valItem.isDeclassified) "DECRYPTED" else "AES LOCKED",
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (valItem.isDeclassified) DharmaGreen else DangerRed
                                                            )
                                                        }
                                                    }
                                                    if (valItem.isDeclassified) {
                                                        Text("UID: ${valItem.docNumber}", fontSize = 10.sp, color = DeepBlue, fontWeight = FontWeight.SemiBold)
                                                    } else {
                                                        Text("Ciphertext: " + valItem.encryptedPayloadHex.take(12) + "...", fontSize = 9.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                                    }
                                                }
                                            }
                                            Text(
                                                text = if (valItem.isDeclassified) "Show Plaintext" else "Unlock Card",
                                                fontSize = 9.sp,
                                                color = if (valItem.isDeclassified) DharmaGreen else Saffron,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.logoutMfa() },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            modifier = Modifier.fillMaxWidth().testTag("mfa_logout_btn")
                        ) {
                            Text("Terminate Secure MFA Session", color = Color.White)
                        }
                    }
                }
            }
        }

        val signedInRecords = viewModel.ownerSignedInRecords
        if (signedInRecords != null) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.FolderShared, contentDescription = null, tint = DharmaGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your Government Recorded Deeds (${signedInRecords.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (signedInRecords.isEmpty()) {
                item {
                    EmptyStatePlaceholder("No deeds are connected to this Government ID at this moment.")
                }
            } else {
                items(signedInRecords) { record ->
                    RegistryCard(
                        record = record,
                        onActionClick = {
                            viewModel.selectRecordForTransfer(record)
                        },
                        actionLabel = "Request Ownership Transfer"
                    )
                }
            }
        }
    }

    // Change Request Dialog
    if (viewModel.showChangeRequestDialog) {
        val record = viewModel.selectedRecordForChange
        if (record != null) {
            Dialog(onDismissRequest = { viewModel.showChangeRequestDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "Request Ownership Transfer",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepBlue
                            )
                            Text(
                                "Target Deed: ID - ${record.id} : ${record.title}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }

                        item {
                            Text(
                                text = "CONSTITUTIONAL PROPERTY LOCKED:\nUnder Article 300A, any registration change or charge creation/release can ONLY be applied through a directive Court Order issued by a civil court of jurisdiction.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DangerRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DangerRed.copy(alpha = 0.1f))
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = viewModel.changeNewOwnerName,
                                onValueChange = { viewModel.changeNewOwnerName = it },
                                label = { Text("New Proposed Owner Name") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("transfer_name_input"),
                                singleLine = true
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = viewModel.changeNewOwnerUid,
                                onValueChange = { viewModel.changeNewOwnerUid = it },
                                label = { Text("New Owner unique Gov ID (UID)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("transfer_uid_input"),
                                singleLine = true
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = viewModel.changeCourtOrderNo,
                                onValueChange = { viewModel.changeCourtOrderNo = it },
                                label = { Text("Mandatory Court Decree Ref No.") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("transfer_court_input"),
                                supportingText = { Text("e.g. HC-MUM-CIVIL-2026-6712") },
                                singleLine = true,
                                trailingIcon = {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Saffron)
                                }
                            )
                            Text(
                                "Tip: Enter a Court Decree issued in the 'Court Orders' tab (e.g. HC-MUM-CIVIL-2026-6712)",
                                fontSize = 11.sp,
                                color = Saffron,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = viewModel.changeReason,
                                onValueChange = { viewModel.changeReason = it },
                                label = { Text("Reason for ownership transition") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("transfer_reason_input"),
                                singleLine = true
                            )
                        }

                        if (viewModel.changeFormError.isNotBlank()) {
                            item {
                                Text(
                                    text = viewModel.changeFormError,
                                    color = DangerRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { viewModel.showChangeRequestDialog = false }) {
                                    Text("Dismiss", color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.executeOwnershipChange() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                                    modifier = Modifier.testTag("transfer_submit_btn")
                                ) {
                                    Text("Execute Change")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------ PORTAL 2: OFFICER CHAMBER ------------------

@Composable
fun OfficerChamberScreen(viewModel: RegistryViewModel, allRecords: List<RegistryRecord>) {
    var officerSignName by remember { mutableStateOf("SDM IAS Kumar") }
    var activeDepartment by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Official Verification Portal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Authorized multi-department sub-registrars chamber. Route legal tasks, verify biometrics, and approve records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    OutlinedTextField(
                        value = officerSignName,
                        onValueChange = { officerSignName = it },
                        label = { Text("Registrar Name / Office Symbol") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("officer_sign_input"),
                        singleLine = true,
                        trailingIcon = {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Saffron)
                        }
                    )
                }
            }
        }

        // WORKSTATION DEPARTMENT SWITCHER (Multi-department of work)
        item {
            ScrollableTabRow(
                selectedTabIndex = activeDepartment,
                containerColor = Color.Transparent,
                contentColor = Saffron,
                edgePadding = 0.dp,
                modifier = Modifier.testTag("officer_dep_tab_row")
            ) {
                Tab(
                    selected = activeDepartment == 0,
                    onClick = { activeDepartment = 0 },
                    text = { Text("🏢 Sub-Registrar", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeDepartment == 1,
                    onClick = { activeDepartment = 1 },
                    text = { Text("📋 IAS Magistrate", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeDepartment == 2,
                    onClick = { activeDepartment = 2 },
                    text = { Text("💰 CBDT Revenue", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeDepartment == 3,
                    onClick = { activeDepartment = 3 },
                    text = { Text("⚖️ Civil Dispute", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeDepartment == 4,
                    onClick = { activeDepartment = 4 },
                    text = { Text("⚙️ Adm Services", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (viewModel.officerStatusFeedback.isNotBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepBlue.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Saffron)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = viewModel.officerStatusFeedback,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepBlue
                        )
                    }
                }
            }
        }

        val pendingRecords = allRecords.filter { it.status == "PENDING" }

        if (activeDepartment == 0) {
            // 🏢 DEPARTMENT 0: SUB-REGISTRAR MAIN DESK
            item {
                Text(
                    text = "Deeds Awaiting Audit (${pendingRecords.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (pendingRecords.isEmpty()) {
                item {
                    EmptyStatePlaceholder("All submitted deed requests are currently audited and cleared.")
                }
            } else {
                items(pendingRecords) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Badge(
                                    containerColor = when (record.type) {
                                        "LAND" -> Saffron.copy(alpha = 0.15f)
                                        "MARRIAGE" -> DharmaGreen.copy(alpha = 0.15f)
                                        "AGREEMENT" -> DeepBlue.copy(alpha = 0.15f)
                                        "LOAN" -> DangerRed.copy(alpha = 0.15f)
                                        else -> Color.Gray.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = record.type,
                                        color = DeepBlue,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    "ID: ${record.id}",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = record.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Claimant: ${record.ownerName} • UID: ${record.ownerUniqueId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = record.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Indian Statute: ${record.constitutionStatutes}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Saffron
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            // Forensic scanning
                            Text(
                                text = "Lekha AI Integrity Forensics",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (record.signatureStatus == "FORGED_FLAGGED" || record.duplicateAttemptFound) {
                                        DangerRed.copy(alpha = 0.05f)
                                    } else {
                                        DharmaGreen.copy(alpha = 0.05f)
                                    }
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (record.signatureStatus == "FORGED_FLAGGED" || record.duplicateAttemptFound) {
                                        DangerRed.copy(alpha = 0.4f)
                                    } else {
                                        DharmaGreen.copy(alpha = 0.4f)
                                    }
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (record.signatureStatus == "FORGED_FLAGGED" || record.duplicateAttemptFound) {
                                                    Icons.Default.Warning
                                                } else {
                                                    Icons.Default.Shield
                                                },
                                                contentDescription = null,
                                                tint = if (record.signatureStatus == "FORGED_FLAGGED" || record.duplicateAttemptFound) {
                                                    DangerRed
                                                } else {
                                                    DharmaGreen
                                                },
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (record.signatureStatus == "FORGED_FLAGGED" || record.duplicateAttemptFound) {
                                                    "FRAUD ALERT: VERIFICATION FAILURE"
                                                } else if (record.signatureStatus == "VERIFIED_AUTHENTIC") {
                                                    "INTEGRITY SCANNED: PASS"
                                                } else {
                                                    "AWAITING DEED SECURITY ANALYSIS"
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (record.signatureStatus == "FORGED_FLAGGED" || record.duplicateAttemptFound) {
                                                    DangerRed
                                                } else {
                                                    DharmaGreen
                                                }
                                            )
                                        }

                                        if (record.signatureStatus != "NOT_SCANNED") {
                                            Badge(
                                                containerColor = if (record.signatureStatus == "FORGED_FLAGGED") DangerRed else DharmaGreen
                                            ) {
                                                Text(
                                                    "Sig Match: ${record.signatureMatchRate}%",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (record.scanLog.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = record.scanLog,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.04f))
                                                .padding(6.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.runOfficerManualFraudRecheck(record.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Re-Run Backend AI Scanner", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            // Clearance checklist static check markers
                            Text(
                                "Department Clearance Checklist Ledger",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepBlue
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (record.iasClearance) DharmaGreen.copy(alpha = 0.08f) else DangerRed.copy(alpha = 0.05f)
                                    )
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (record.iasClearance) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (record.iasClearance) DharmaGreen else DangerRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("IAS Clearance", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (record.incomeTaxClearance) DharmaGreen.copy(alpha = 0.08f) else DangerRed.copy(alpha = 0.05f)
                                    )
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (record.incomeTaxClearance) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (record.incomeTaxClearance) DharmaGreen else DangerRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("CBDT Tax Clear", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { viewModel.rejectRegistryRecord(record, officerSignName) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .testTag("officer_reject_btn_${record.id}"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Reject", fontSize = 11.sp)
                                }

                                val allCleared = record.iasClearance && record.incomeTaxClearance
                                Button(
                                    onClick = { viewModel.approveRegistryRecord(record, officerSignName) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (allCleared) DharmaGreen else Color.Gray
                                    ),
                                    modifier = Modifier.testTag("officer_approve_btn_${record.id}"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve & Seal", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeDepartment == 1) {
            // 📋 DEPARTMENT 1: IAS MAGISTRATES OVERWATCH OFFICE
            item {
                Text(
                    text = "Deeds Awaiting Executive Magistrate Clearance (${pendingRecords.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (pendingRecords.isEmpty()) {
                item {
                    EmptyStatePlaceholder("All submitted deed requests are currently audited and cleared.")
                }
            } else {
                items(pendingRecords) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = record.title, fontWeight = FontWeight.Bold, color = DeepBlue)
                            Text(text = "Claimant: ${record.ownerName} • Type: ${record.type}", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (record.iasClearance) DharmaGreen.copy(alpha = 0.08f)
                                        else DangerRed.copy(alpha = 0.06f)
                                    )
                                    .clickable { viewModel.toggleIasClearance(record) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (record.iasClearance) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (record.iasClearance) DharmaGreen else DangerRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Grant IAS Divisional Clearance Seal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                }
                                Text(
                                    text = if (record.iasClearance) "AUTHORIZED" else "STANDBY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.iasClearance) DharmaGreen else DangerRed
                                )
                            }
                        }
                    }
                }
            }
        } else if (activeDepartment == 2) {
            // 💰 DEPARTMENT 2: CBDT INCOME TAX ASSESSMENT DIVISION
            item {
                Text(
                    text = "Deeds Awaiting Revenue Clearance (${pendingRecords.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (pendingRecords.isEmpty()) {
                item {
                    EmptyStatePlaceholder("All submitted deed requests are currently audited and cleared.")
                }
            } else {
                items(pendingRecords) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = record.title, fontWeight = FontWeight.Bold, color = DeepBlue)
                            Text(text = "Claimant: ${record.ownerName} • Type: ${record.type}", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (record.incomeTaxClearance) DharmaGreen.copy(alpha = 0.08f)
                                        else DangerRed.copy(alpha = 0.06f)
                                    )
                                    .clickable { viewModel.toggleIncomeTaxClearance(record) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (record.incomeTaxClearance) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (record.incomeTaxClearance) DharmaGreen else DangerRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Grant Income Tax Clearance (CBDT Seal)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                }
                                Text(
                                    text = if (record.incomeTaxClearance) "AUTHORIZED" else "STANDBY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.incomeTaxClearance) DharmaGreen else DangerRed
                                )
                            }
                        }
                    }
                }
            }
        } else if (activeDepartment == 3) {
            // ⚖️ DEPARTMENT 3: CIVIL DISPUTES MEDIATION OFFICE
            item {
                Text(
                    text = "Awaiting Dispute Counseling Mediation (${viewModel.marriageComplaintsList.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (viewModel.marriageComplaintsList.isEmpty()) {
                item {
                    EmptyStatePlaceholder("No pending family or land dispute counseling files logged.")
                }
            } else {
                items(viewModel.marriageComplaintsList) { complaint ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Dispute ID: #${complaint.id}", fontWeight = FontWeight.Bold, color = DangerRed)
                                Badge(containerColor = if (complaint.status == "ACTIVE") Saffron.copy(alpha = 0.15f) else DharmaGreen.copy(alpha = 0.15f)) {
                                    Text(complaint.status, color = DeepBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Filer: ${complaint.reporterName} (UID: ${complaint.reporterUid})", fontWeight = FontWeight.SemiBold)
                            Text(text = "Details: ${complaint.complaintDetails}", fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Mediation Stage: Stage ${complaint.stage} (${when(complaint.stage) {
                                1 -> "Initial Counseling"
                                2 -> "Executive Conciliation Meeting"
                                3 -> "Bilateral Settlement Settlement Proposals"
                                4 -> "Legally Settled & Sealed Case"
                                else -> "Completed"
                            }})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepBlue)

                            LinearProgressIndicator(
                                progress = { complaint.stage / 4.0f },
                                color = DharmaGreen,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                            )

                            if (complaint.status == "ACTIVE") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { viewModel.advanceComplaintStage(complaint.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                                        modifier = Modifier.testTag("mediation_advance_btn_${complaint.id}")
                                    ) {
                                        Text(if (complaint.stage < 3) "Advance Mediation File" else "Close Dispute as Settled", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeDepartment == 4) {
            // ⚙️ DEPARTMENT 4: SOVEREIGN ADMIN SERVICES HUB
            item {
                Text(
                    text = "Ongoing Citizens Document Registrations (${viewModel.govServiceApplications.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (viewModel.govServiceApplications.isEmpty()) {
                item {
                    EmptyStatePlaceholder("No pending administrative citizen files in procedure.")
                }
            } else {
                items(viewModel.govServiceApplications) { app ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Badge(containerColor = Saffron.copy(alpha = 0.15f)) {
                                    Text(app.serviceName, color = DeepBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                }
                                Text("ID: #${app.id}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Applicant: ${app.citizenName} • Aadhaar: ${app.citizenUid}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Procedure Stage: ${app.currentStep} of 8 (Status: ${app.status})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            LinearProgressIndicator(
                                progress = { app.currentStep / 8.0f },
                                color = DeepBlue,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(5.dp).clip(RoundedCornerShape(2.dp))
                            )

                            if (app.status == "PENDING") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { viewModel.advanceServiceStep(app.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                                        modifier = Modifier.testTag("official_service_advance_btn_${app.id}")
                                    ) {
                                        Text(if (app.currentStep < 7) "Advance Procedure Stage" else "Approve & Clear File", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------ PORTAL 3: COURT DECREES / JUDICIARY CENTER ------------------

@Composable
fun CourtDecreeScreen(
    viewModel: RegistryViewModel,
    allOrders: List<CourtOrder>,
    allRecords: List<RegistryRecord>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Saffron.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Saffron.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Judicial Decree Center",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Under the Constitution of India, registries can only be aliened, modified or charged through direct Court Orders. Issue and view judicial decrees here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.showAddCourtOrderDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_court_order_btn")
                    ) {
                        Icon(Icons.Default.AddModerator, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Issue New Judicial Decree")
                    }
                }
            }
        }

        item {
            Text(
                text = "Registered Judicial Mandates (${allOrders.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (allOrders.isEmpty()) {
            item {
                EmptyStatePlaceholder("No live judicial decrees exist in the registry databases.")
            }
        } else {
            items(allOrders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = order.orderNumber,
                                fontWeight = FontWeight.Bold,
                                color = Saffron,
                                fontSize = 14.sp,
                                modifier = Modifier.testTag("court_order_no_${order.id}")
                            )
                            Badge(
                                containerColor = if (order.isExecuted) DharmaGreen.copy(alpha = 0.15f)
                                else PendingYellow.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (order.isExecuted) "EXECUTED" else "PENDING EXECUTION",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = if (order.isExecuted) DharmaGreen else PendingYellow,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = order.courtName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Decree Mandate:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBlue
                        )
                        Text(
                            text = order.details,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Display target record info
                        val targetRec = allRecords.find { it.id == order.recordId }
                        if (targetRec != null) {
                            Text(
                                text = "Target Deed ID: ${order.recordId} • ${targetRec.title}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Target Deed Record ID: ${order.recordId}",
                                fontSize = 11.sp,
                                color = DangerRed
                            )
                        }

                        if (order.mandatedNewOwnerName != null) {
                            Text(
                                text = "Mandated Transferee: ${order.mandatedNewOwnerName} (${order.mandatedNewOwnerUniqueId})",
                                fontSize = 11.sp,
                                color = DharmaGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (order.mandatedCharge != null) {
                            Text(
                                text = "Mandated Charge Limit: ₹${order.mandatedCharge}",
                                fontSize = 11.sp,
                                color = DeepBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    if (viewModel.showAddCourtOrderDialog) {
        Dialog(onDismissRequest = { viewModel.showAddCourtOrderDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Issue Judicial Order",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepBlue
                        )
                        Text(
                            text = "Under Art. 142, only valid judge declarations alter registry values and charge locks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        HorizontalDivider()
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.courtOrderNo,
                            onValueChange = { viewModel.courtOrderNo = it },
                            label = { Text("Court Decree Ref No. (e.g. HC-DEL-2026-09)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("court_form_number"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.courtName,
                            onValueChange = { viewModel.courtName = it },
                            label = { Text("Issuing Court of Jurisdiction") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("court_form_name"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.courtTargetRecordId,
                            onValueChange = { viewModel.courtTargetRecordId = it },
                            label = { Text("Target Deed record ID") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("court_form_target"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.courtDecreeDetails,
                            onValueChange = { viewModel.courtDecreeDetails = it },
                            label = { Text("Mandated adjustments or decree text") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .testTag("court_form_details")
                        )
                    }

                    item {
                        Text("Mandated Adjustments (Optional)", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color.Gray)
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.courtNewOwnerName,
                            onValueChange = { viewModel.courtNewOwnerName = it },
                            label = { Text("Altered Owner Name (Optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("court_form_new_owner"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.courtNewOwnerUid,
                            onValueChange = { viewModel.courtNewOwnerUid = it },
                            label = { Text("Altered Owner Gov UID (Optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("court_form_new_uid"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.courtNewChargeAmount,
                            onValueChange = { viewModel.courtNewChargeAmount = it },
                            label = { Text("New Financial Charge (INR) (Optional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("court_form_new_charge"),
                            singleLine = true
                        )
                    }

                    if (viewModel.courtOrderError.isNotBlank()) {
                        item {
                            Text(viewModel.courtOrderError, color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { viewModel.showAddCourtOrderDialog = false }) {
                                Text("Dismiss", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.submitNewCourtOrder() },
                                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                                modifier = Modifier.testTag("court_form_submit")
                            ) {
                                Text("Seal Order")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------ HELPERS AND CARDS ------------------

@Composable
fun RegistryCard(
    record: RegistryRecord,
    onActionClick: (() -> Unit)?,
    actionLabel: String = ""
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BlueGrey.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(
                    containerColor = when (record.type) {
                        "LAND" -> Saffron.copy(alpha = 0.15f)
                        "MARRIAGE" -> DharmaGreen.copy(alpha = 0.15f)
                        "AGREEMENT" -> DeepBlue.copy(alpha = 0.15f)
                        "LOAN" -> DangerRed.copy(alpha = 0.15f)
                        else -> Color.Gray.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = record.type,
                        color = DeepBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "Deed ID: ${record.id}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = record.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Authorized Owner: ${record.ownerName} • UID: ${record.ownerUniqueId}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            if (record.additionalParties.isNotBlank()) {
                Text(
                    text = "Connected parties: ${record.additionalParties}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = record.description,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Constitutional statutory reference:",
                fontSize = 10.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = record.constitutionStatutes,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepBlue
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Charge Liability UI representing "can't be charged in any from other than court orders"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBlue.copy(alpha = 0.05f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = Saffron,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Adjudicated Charge / Liability:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = if (record.chargeAmount > 0.0) "₹${String.format(Locale.US, "%,.2f", record.chargeAmount)}" else "₹0.00 (No Lien)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (record.chargeAmount > 0.0) DangerRed else DharmaGreen
                )
            }

            if (record.courtOrderLinked != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = DharmaGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Linked Decree: ${record.courtOrderLinked}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DharmaGreen
                    )
                }
            }

            if (record.verifiedByOfficer != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Approved Ledger Sign: ${record.verifiedByOfficer}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlue
                    )
                }
            }

            if (onActionClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_action_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Gavel, contentDescription = null, modifier = Modifier.size(42.dp), tint = BlueGrey.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = BlueGrey,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BiometricScannerDialog(viewModel: RegistryViewModel) {
    if (viewModel.showBiometricScanner) {
        LaunchedEffect(key1 = viewModel.showBiometricScanner) {
            for (i in 1..100) {
                kotlinx.coroutines.delay(20)
                viewModel.faceLivenessPercentage = i
            }
            viewModel.completeBiometricScan()
        }

        Dialog(onDismissRequest = { viewModel.showBiometricScanner = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DeepBlue),
                border = BorderStroke(2.dp, Saffron),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aadhaar Biometric Scan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        IconButton(onClick = { viewModel.showBiometricScanner = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(80.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(BorderStroke(3.dp, Saffron), shape = RoundedCornerShape(80.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Face",
                            tint = Saffron,
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Liveness Authenticator: active",
                            color = DharmaGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Matching Facial Nodes: ${viewModel.faceLivenessPercentage}%",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { viewModel.faceLivenessPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Saffron,
                            trackColor = Color.DarkGray
                        )
                    }

                    Text(
                        text = "SECURE PROTOCOL: UIDAI Central Server session encrypted. Capturing biometric liveness vectors. Keep device eye-level.",
                        color = MutedBlueText,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SecureVaultDetailsDialog(viewModel: RegistryViewModel, item: SecureVaultItem, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DeepBlue),
            border = BorderStroke(1.5.dp, Saffron),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (item.isDeclassified) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Citizen Enclave Vault",
                            fontWeight = FontWeight.Bold,
                            color = Saffron,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = "Document Seal",
                    tint = Saffron,
                    modifier = Modifier.size(56.dp)
                )

                Text(
                    text = item.docTitle,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Sovereign SHA-256 Digest Certificate", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = DharmaGreen)
                        Text(text = "Certificate Reference: ${item.certificateRefNo}", fontSize = 10.sp, color = Color.LightGray)
                        Text(text = "Status: " + (if (item.isDeclassified) "ACTIVE & DECLASSIFIED" else "AES-256 VAULT LOCKED"), fontSize = 10.sp, color = if (item.isDeclassified) DharmaGreen else Saffron)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.DarkGray)
                        
                        if (item.isDeclassified) {
                            Text(text = "Decrypted Document ID:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.LightGray)
                            Text(text = item.docNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Official Metadata & Authority:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.LightGray)
                            Text(text = item.docMetadata, fontSize = 11.sp, color = Color.LightGray)
                        } else {
                            Text(text = "Encrypted Block Ciphertext:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.LightGray)
                            Text(
                                text = item.encryptedPayloadHex,
                                fontSize = 9.sp,
                                color = Color.LightGray.copy(alpha = 0.6f),
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (!item.isDeclassified) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Enter your 6-Digit security PIN to derive the AES decryption key:",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                        OutlinedTextField(
                            value = viewModel.vaultDecryptionPin,
                            onValueChange = { viewModel.vaultDecryptionPin = it },
                            label = { Text("6-Digit Security PIN", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Saffron,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Saffron,
                                unfocusedLabelColor = Color.LightGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("vault_item_pin_input")
                        )

                        if (viewModel.vaultDecryptionError.isNotBlank()) {
                            Text(
                                text = viewModel.vaultDecryptionError,
                                color = DangerRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { 
                                viewModel.decryptVaultItem(item, viewModel.vaultDecryptionPin)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                            modifier = Modifier.fillMaxWidth().testTag("vault_item_decrypt_btn")
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Derive AES Key & Decrypt Payload", fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                viewModel.lockVaultItem(item)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Lock Back Payload", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Download Copy (.pdf)", fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Sovereignty Purge action
                TextButton(
                    onClick = {
                        viewModel.deleteVaultItem(item)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("vault_item_purge_btn")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Purge Card", modifier = Modifier.size(12.dp), tint = DangerRed)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Purge Reference from Sovereign Enclave", color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddVaultItemDialog(viewModel: RegistryViewModel) {
    Dialog(onDismissRequest = { viewModel.showAddVaultItemDialog = false }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, DeepBlue.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Encrypt & Seal New Reference",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepBlue
                        )
                        IconButton(onClick = { viewModel.showAddVaultItemDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Text(
                        "All documents references are dynamically AES-128 encrypted using your unique passcode PIN derived hash.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                item {
                    Text("Select Document Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Aadhaar", "PAN", "Passport", "Land Deed", "Custom").forEach { cat ->
                            FilterChip(
                                selected = viewModel.vaultNewType == cat,
                                onClick = { viewModel.vaultNewType = cat },
                                label = { Text(cat, fontSize = 9.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = viewModel.vaultNewTitle,
                        onValueChange = { viewModel.vaultNewTitle = it },
                        label = { Text("Document Description / Title") },
                        placeholder = { Text("e.g. My Primary Voters ID Card") },
                        modifier = Modifier.fillMaxWidth().testTag("vault_new_title"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = viewModel.vaultNewNumber,
                        onValueChange = { viewModel.vaultNewNumber = it },
                        label = { Text("Government ID / Reference Number [UID]") },
                        placeholder = { Text("e.g. VID-9081-3428-11") },
                        modifier = Modifier.fillMaxWidth().testTag("vault_new_number"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = viewModel.vaultNewMetadata,
                        onValueChange = { viewModel.vaultNewMetadata = it },
                        label = { Text("Issuing Agency & Details") },
                        placeholder = { Text("e.g. Election Commission of India") },
                        modifier = Modifier.fillMaxWidth().testTag("vault_new_metadata"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = viewModel.vaultConfirmPin,
                        onValueChange = { viewModel.vaultConfirmPin = it },
                        label = { Text("Confirm security 6-digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("vault_confirm_pin"),
                        singleLine = true
                    )
                }

                item {
                    if (viewModel.vaultFormError.isNotBlank()) {
                        Text(
                            text = viewModel.vaultFormError,
                            color = DangerRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.addDocumentToVault() },
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                        modifier = Modifier.fillMaxWidth().testTag("vault_submit_btn")
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = "Seal")
                            Text("Encrypt & Vault-Lock Reference")
                        }
                    }
                }
            }
        }
    }
}
