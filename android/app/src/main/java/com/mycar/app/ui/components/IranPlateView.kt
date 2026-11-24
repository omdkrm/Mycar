package com.mycar.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Plate categories supported by the Iranian national license plate design.
 * Defaults to PERSONAL (white background).
 */
enum class PlateType(
    val title: String,
    val backgroundColor: Color,
    val textColor: Color,
    val borderColor: Color
) {
    PERSONAL(
        title = "شخصی",
        backgroundColor = Color.White,
        textColor = Color(0xFF1E293B),
        borderColor = Color(0xFF0F172A)
    ),
    TAXI(
        title = "تاکسی / عمومی",
        backgroundColor = Color(0xFFFFD54F),
        textColor = Color(0xFF1E293B),
        borderColor = Color(0xFF0F172A)
    ),
    GOVERNMENT(
        title = "دولتی",
        backgroundColor = Color(0xFFDC2626),
        textColor = Color.White,
        borderColor = Color(0xFF7F1D1D)
    ),
    POLICE(
        title = "پلیس / فراجا",
        backgroundColor = Color(0xFF15803D),
        textColor = Color.White,
        borderColor = Color(0xFF14532D)
    )
}

/**
 * Model representing the 4 components of an Iranian national license plate:
 * Physical order: [BLUE BAND] [2 DIGITS] [LETTER] [3 DIGITS] [PROVINCE CODE]
 */
data class IranPlate(
    val twoDigits: String = "",     // 2 digits BEFORE the letter (e.g. "12")
    val letter: String = "ب",       // 1 valid Persian plate letter
    val threeDigits: String = "",   // 3 digits AFTER the letter (e.g. "365")
    val provinceCode: String = "",  // 2-digit province code in right box (e.g. "33")
    val type: PlateType = PlateType.PERSONAL
) {
    val isValid: Boolean
        get() = twoDigits.length == 2 &&
                twoDigits.all { it.isDigit() } &&
                letter in VALID_LETTERS &&
                threeDigits.length == 3 &&
                threeDigits.all { it.isDigit() } &&
                provinceCode.length == 2 &&
                provinceCode.all { it.isDigit() }

    val isPartiallyFilled: Boolean
        get() = twoDigits.isNotEmpty() || threeDigits.isNotEmpty() || provinceCode.isNotEmpty()

    /**
     * Formats the plate into the standard Iranian national plate representation
     * (physical order, left to right): 2 digits | letter | 3 digits | province code
     * e.g. "۱۲ | ب | ۳۶۵ | ۳۳"
     */
    fun toNormalizedString(): String {
        if (!isValid) return ""
        val p1 = IranPlateParser.toPersianDigits(twoDigits)
        val p2 = IranPlateParser.toPersianDigits(threeDigits)
        val prov = IranPlateParser.toPersianDigits(provinceCode)
        return "$p1 | $letter | $p2 | $prov"
    }

    companion object {
        // Standard Persian letters for Iranian personal vehicle license plates
        val VALID_LETTERS = listOf(
            "ب", "ج", "د", "س", "ص", "ط", "ق", "ل", "م", "ن", "و", "ه", "ی"
        )
    }
}

/**
 * Utility for parsing and normalizing Iranian vehicle license plates.
 */
object IranPlateParser {
    private val PERSIAN_TO_ENGLISH_MAP = mapOf(
        '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
        '۵' to '5', '۶' to '6', '۷' to '7', '۸' to '8', '۹' to '9',
        '٠' to '0', '١' to '1', '٢' to '2', '٣' to '3', '٤' to '4',
        '٥' to '5', '٦' to '6', '٧' to '7', '٨' to '8', '٩' to '9'
    )

    private val ENGLISH_TO_PERSIAN_MAP = mapOf(
        '0' to '۰', '1' to '۱', '2' to '۲', '3' to '۳', '4' to '۴',
        '5' to '۵', '6' to '۶', '7' to '۷', '8' to '۸', '9' to '۹'
    )

    fun toEnglishDigits(input: String): String {
        return input.map { PERSIAN_TO_ENGLISH_MAP[it] ?: it }.joinToString("")
    }

    fun toPersianDigits(input: String): String {
        return input.map { ENGLISH_TO_PERSIAN_MAP[it] ?: it }.joinToString("")
    }

    private const val LETTERS = "بجدسصطقلمنوهی"

    // Standard: 2 digits, letter, 3 digits, province
    private val FORMAT_STANDARD = Regex("(\\d{2})\\s*([$LETTERS])\\s*(\\d{3})\\s*(\\d{2})")
    // Legacy: 3 digits, letter, 2 digits, province
    private val FORMAT_LEGACY = Regex("(\\d{3})\\s*([$LETTERS])\\s*(\\d{2})\\s*(\\d{2})")
    // Province first: province, 2 digits, letter, 3 digits
    private val FORMAT_PROVINCE_FIRST = Regex("(\\d{2})\\s*(\\d{2})\\s*([$LETTERS])\\s*(\\d{3})")
    // Province first (legacy): province, 3 digits, letter, 2 digits
    private val FORMAT_PROVINCE_FIRST_LEGACY = Regex("(\\d{2})\\s*(\\d{3})\\s*([$LETTERS])\\s*(\\d{2})")

    private val SEPARATORS = Regex("[\\s|\\-\u2013\u060C,/._]+")

    /**
     * Parses an Iranian license plate from a raw stored string.
     * Understands the current standard and every older format that the app stored before:
     * - Standard: "۱۲ | ب | ۳۶۵ | ۳۳"
     * - Previous format: "12 ب 365 ایران 33" / "۱۲ ب ۳۶۵ ایران ۳۳"
     * - Old format: "365 ب 12 ایران 33"
     * - Compact formats: "12ب365-33" or "365ب12-33"
     * - Province-first formats: "ایران 33 12 ب 365"
     * Returns null when the text is not a recognizable plate.
     */
    fun parse(raw: String): IranPlate? {
        if (raw.isBlank()) return null

        val text = toEnglishDigits(raw.trim())
            .replace("ایران", " ")
            .replace(SEPARATORS, " ")
            .trim()
        if (text.isEmpty()) return null

        // Reject anything that contains characters other than digits, plate letters and spaces
        if (text.any { it != ' ' && it !in '0'..'9' && it !in LETTERS }) return null

        FORMAT_STANDARD.matchEntire(text)?.let { m ->
            val (two, letter, three, prov) = m.destructured
            return IranPlate(twoDigits = two, letter = letter, threeDigits = three, provinceCode = prov)
        }
        FORMAT_LEGACY.matchEntire(text)?.let { m ->
            val (three, letter, two, prov) = m.destructured
            return IranPlate(twoDigits = two, letter = letter, threeDigits = three, provinceCode = prov)
        }
        FORMAT_PROVINCE_FIRST.matchEntire(text)?.let { m ->
            val (prov, two, letter, three) = m.destructured
            return IranPlate(twoDigits = two, letter = letter, threeDigits = three, provinceCode = prov)
        }
        FORMAT_PROVINCE_FIRST_LEGACY.matchEntire(text)?.let { m ->
            val (prov, three, letter, two) = m.destructured
            return IranPlate(twoDigits = two, letter = letter, threeDigits = three, provinceCode = prov)
        }
        return null
    }
}

/**
 * Interactive Iranian National Vehicle License Plate Input Component.
 * Formatted physically from left to right as:
 * [Blue Strip] | [2 Digits] | [Letter] | [3 Digits] | [Province Code]
 * E.g.: BLUE | ۱۲ | ب | ۳۶۵ | ۳۳
 */
@Composable
fun IranPlateInput(
    plate: IranPlate,
    onPlateChange: (IranPlate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLetterPicker by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val twoDigitsRequester = remember { FocusRequester() }
    val threeDigitsRequester = remember { FocusRequester() }
    val provinceRequester = remember { FocusRequester() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label above plate
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "پلاک ملی خودرو:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "نمونه: ۱۲ | ب | ۳۶۵ | ۳۳",
                style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Ltr),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Plate Frame (Uses LTR explicitly for authentic physical Iranian plate layout)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = plate.type.backgroundColor),
                border = BorderStroke(2.dp, plate.type.borderColor)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Blue Strip (Leftmost: I.R. IRAN + Flag)
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF003399)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Mini Flag
                            Column(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(10.dp)
                                    .border(0.5.dp, Color.White.copy(alpha = 0.6f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(Color(0xFF229954))
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(Color.White)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(Color(0xFFC0392B))
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "I.R.",
                                color = Color.White,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 8.sp
                            )
                            Text(
                                text = "IRAN",
                                color = Color.White,
                                fontSize = 6.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 7.sp
                            )
                        }
                    }

                    // 2. Two Digits Section (e.g. 12 / ۱۲ before the letter)
                    Box(
                        modifier = Modifier
                            .weight(1.05f)
                            .fillMaxHeight()
                            .clickable { twoDigitsRequester.requestFocus() },
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = IranPlateParser.toPersianDigits(plate.twoDigits),
                            onValueChange = { input ->
                                val english = IranPlateParser.toEnglishDigits(input).filter { it.isDigit() }
                                if (english.length <= 2) {
                                    onPlateChange(plate.copy(twoDigits = english))
                                    if (english.length == 2) {
                                        threeDigitsRequester.requestFocus()
                                    }
                                }
                            },
                            textStyle = TextStyle(
                                color = plate.type.textColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                textDirection = TextDirection.Ltr
                            ),
                            cursorBrush = SolidColor(plate.type.textColor),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { threeDigitsRequester.requestFocus() }
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(twoDigitsRequester)
                        )

                        if (plate.twoDigits.isEmpty()) {
                            Text(
                                text = "۱۲",
                                style = TextStyle(
                                    color = Color.LightGray,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }

                    // Divider between 2 digits and letter
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    // 3. Persian Letter Section (e.g. ب)
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .fillMaxHeight()
                            .clickable { showLetterPicker = true }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = plate.letter,
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "انتخاب حرف",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Divider between letter and 3 digits
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    // 4. Three Digits Section (e.g. 365 / ۳۶۵ after the letter)
                    Box(
                        modifier = Modifier
                            .weight(1.35f)
                            .fillMaxHeight()
                            .clickable { threeDigitsRequester.requestFocus() },
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = IranPlateParser.toPersianDigits(plate.threeDigits),
                            onValueChange = { input ->
                                val english = IranPlateParser.toEnglishDigits(input).filter { it.isDigit() }
                                if (english.length <= 3) {
                                    onPlateChange(plate.copy(threeDigits = english))
                                    if (english.length == 3) {
                                        provinceRequester.requestFocus()
                                    }
                                }
                            },
                            textStyle = TextStyle(
                                color = plate.type.textColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                textDirection = TextDirection.Ltr
                            ),
                            cursorBrush = SolidColor(plate.type.textColor),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { provinceRequester.requestFocus() }
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(threeDigitsRequester)
                        )

                        if (plate.threeDigits.isEmpty()) {
                            Text(
                                text = "۳۶۵",
                                style = TextStyle(
                                    color = Color.LightGray,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }

                    // Province Divider Line
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(plate.type.borderColor)
                    )

                    // 5. Province Code Box (Rightmost: 2-digit province code)
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .fillMaxHeight()
                            .clickable { provinceRequester.requestFocus() },
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = IranPlateParser.toPersianDigits(plate.provinceCode),
                            onValueChange = { input ->
                                val english = IranPlateParser.toEnglishDigits(input).filter { it.isDigit() }
                                if (english.length <= 2) {
                                    onPlateChange(plate.copy(provinceCode = english))
                                    if (english.length == 2) {
                                        focusManager.clearFocus()
                                    }
                                }
                            },
                            textStyle = TextStyle(
                                color = plate.type.textColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                textDirection = TextDirection.Ltr
                            ),
                            cursorBrush = SolidColor(plate.type.textColor),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(provinceRequester)
                        )

                        if (plate.provinceCode.isEmpty()) {
                            Text(
                                text = "۳۳",
                                style = TextStyle(
                                    color = Color.LightGray,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to select one of the 13 valid personal-vehicle letters
    if (showLetterPicker) {
        SelectPlateLetterDialog(
            currentLetter = plate.letter,
            onLetterSelected = { letter ->
                onPlateChange(plate.copy(letter = letter))
                showLetterPicker = false
                threeDigitsRequester.requestFocus()
            },
            onDismiss = { showLetterPicker = false }
        )
    }
}

/**
 * Dialog for choosing a valid Iranian personal vehicle plate letter.
 */
@Composable
fun SelectPlateLetterDialog(
    currentLetter: String,
    onLetterSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        title = "انتخاب حرف پلاک",
        onDismiss = onDismiss
    ) {
        Text(
            text = "حروف مجاز خودروهای شخصی ایران:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Letter Grid (13 letters)
        val rows = IranPlate.VALID_LETTERS.chunked(4)

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            rows.forEach { rowLetters ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowLetters.forEach { l ->
                        val isSelected = l == currentLetter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onLetterSelected(l) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = l,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // Fill empty slots in the last row if needed
                    if (rowLetters.size < 4) {
                        repeat(4 - rowLetters.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Iranian National License Plate Badge for displaying vehicle plates in cards.
 */
@Composable
fun IranPlateBadge(
    plateNumber: String,
    modifier: Modifier = Modifier
) {
    if (plateNumber.isBlank()) return

    val parsed = remember(plateNumber) { IranPlateParser.parse(plateNumber) }

    if (parsed != null && parsed.isValid) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Surface(
                modifier = modifier
                    .width(136.dp)
                    .height(30.dp),
                shape = RoundedCornerShape(4.dp),
                color = parsed.type.backgroundColor,
                border = BorderStroke(1.2.dp, parsed.type.borderColor),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Blue Strip
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF003399)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Mini Flag
                            Column(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(Color(0xFF229954))
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(Color.White)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(Color(0xFFC0392B))
                                )
                            }
                            Text(
                                text = "IRAN",
                                color = Color.White,
                                fontSize = 4.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 4.sp
                            )
                        }
                    }

                    // 2 Digits (before the letter)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = IranPlateParser.toPersianDigits(parsed.twoDigits),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = parsed.type.textColor
                        )
                    }

                    // Letter
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = parsed.letter,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = parsed.type.textColor
                        )
                    }

                    // 3 Digits (after the letter)
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = IranPlateParser.toPersianDigits(parsed.threeDigits),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = parsed.type.textColor
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(parsed.type.borderColor)
                    )

                    // Province Code Box
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = IranPlateParser.toPersianDigits(parsed.provinceCode),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = parsed.type.textColor
                        )
                    }
                }
            }
        }
    } else {
        // Fallback for non-standard / old plate string
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Text(
                text = "پلاک: $plateNumber",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
