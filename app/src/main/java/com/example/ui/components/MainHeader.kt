package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.constants.BrandConstants
import com.example.model.DomainPreset

@Composable
fun MainHeader(
    selectedDomain: DomainPreset,
    onDomainSelected: (DomainPreset) -> Unit,
    blueprintCount: Int = 0,
    historyCount: Int = 0,
    hasApiKey: Boolean = false,
    onOpenBlueprints: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenApiKey: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val brand = BrandConstants.forDomain(selectedDomain)
    val isTactical = brand.isTactical
    var isDomainDropdownExpanded by remember { mutableStateOf(false) }

    val animatedHeaderBg by animateColorAsState(
        targetValue = brand.headerBg,
        animationSpec = tween(durationMillis = 350),
        label = "headerBg"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(animatedHeaderBg)
            .testTag("main_header_container")
    ) {
        // TOP NAVIGATION BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // BRAND LOGO & HEADLINE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .testTag("brand_header_info")
            ) {
                // Logo Icon Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isTactical) Color(0xFF0F172A) else Color(0xFF107C41)
                        )
                        .border(
                            width = if (isTactical) 1.5.dp else 1.dp,
                            color = if (isTactical) Color(0xFF38BDF8) else Color(0xFF22C55E).copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = brand.logoIcon,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = brand.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White,
                            letterSpacing = if (isTactical) 0.5.sp else 0.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Category Tag
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isTactical) Color(0xFF1E293B) else Color(0xFF15803D),
                            border = BorderStroke(
                                1.dp,
                                if (isTactical) Color(0xFF38BDF8) else Color(0xFF86EFAC).copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = brand.categoryTag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 8.5.sp,
                                    letterSpacing = 0.75.sp,
                                    fontFamily = if (isTactical) FontFamily.Monospace else FontFamily.Default
                                ),
                                color = if (isTactical) Color(0xFF38BDF8) else Color(0xFF86EFAC),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = brand.subtitle,
                        fontSize = 10.sp,
                        color = if (isTactical) Color(0xFF94A3B8) else Color(0xFFD1E7DD),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ACTION ICONS
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Blueprint Library Button
                IconButton(
                    onClick = onOpenBlueprints,
                    modifier = Modifier.size(38.dp).testTag("blueprint_library_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (blueprintCount > 0) {
                                Badge(
                                    containerColor = if (isTactical) Color(0xFF38BDF8) else Color(0xFF22C55E)
                                ) {
                                    Text(
                                        "$blueprintCount",
                                        color = if (isTactical) Color(0xFF020617) else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmarks,
                            contentDescription = "Blueprint Library",
                            tint = Color.White
                        )
                    }
                }

                // History Button
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.size(38.dp).testTag("history_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (historyCount > 0) {
                                Badge(
                                    containerColor = if (isTactical) Color(0xFFF59E0B) else Color(0xFF21A366)
                                ) {
                                    Text(
                                        "$historyCount",
                                        color = if (isTactical) Color(0xFF020617) else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = Color.White
                        )
                    }
                }

                // API Key Settings
                IconButton(
                    onClick = onOpenApiKey,
                    modifier = Modifier.size(38.dp).testTag("api_key_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "API Settings",
                        tint = if (hasApiKey) {
                            if (isTactical) Color(0xFF38BDF8) else Color(0xFF86EFAC)
                        } else Color.White
                    )
                }
            }
        }

        // SUB-BAR: DOMAIN SELECTOR DROPDOWN & ACTIVE PORTAL INDICATOR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isTactical) Color(0xFF0F172A) else Color(0xFF0A3D21))
                .border(
                    width = 1.dp,
                    color = if (isTactical) Color(0xFF334155) else Color(0xFF107C41).copy(alpha = 0.4f)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // DOMAIN SELECTOR DROPDOWN BUTTON
            Box {
                Surface(
                    onClick = { isDomainDropdownExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isTactical) Color(0xFF1E293B) else Color(0xFF15803D),
                    border = BorderStroke(
                        1.dp,
                        if (isTactical) Color(0xFF38BDF8) else Color(0xFF22C55E).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("domain_selector_dropdown_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedDomain.icon,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedDomain.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Domain",
                            tint = if (isTactical) Color(0xFF38BDF8) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // DROPDOWN MENU
                DropdownMenu(
                    expanded = isDomainDropdownExpanded,
                    onDismissRequest = { isDomainDropdownExpanded = false },
                    modifier = Modifier
                        .background(if (isTactical) Color(0xFF0F172A) else Color.White)
                        .border(
                            1.dp,
                            if (isTactical) Color(0xFF334155) else Color(0xFFCBD5E1),
                            RoundedCornerShape(8.dp)
                        )
                        .testTag("domain_selector_dropdown_menu")
                ) {
                    DomainPreset.values().forEach { domain ->
                        val isSelected = domain == selectedDomain
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = domain.icon,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = domain.title,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isTactical) {
                                                    if (isSelected) Color(0xFF38BDF8) else Color(0xFFF8FAFC)
                                                } else {
                                                    if (isSelected) Color(0xFF107C41) else Color(0xFF0F172A)
                                                },
                                                fontSize = 13.5.sp
                                            )
                                            if (domain == DomainPreset.MILITARY) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(3.dp),
                                                    color = Color(0xFF450A0A)
                                                ) {
                                                    Text(
                                                        text = "TACTICAL",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFFCA5A5),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = if (domain == DomainPreset.MILITARY) {
                                                "Switches to TacticalGrid tactical dark slate portal"
                                            } else {
                                                domain.description
                                            },
                                            fontSize = 10.sp,
                                            color = if (isTactical) Color(0xFF94A3B8) else Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = if (isTactical) Color(0xFF38BDF8) else Color(0xFF107C41),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onDomainSelected(domain)
                                isDomainDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("dropdown_item_${domain.id}")
                        )
                    }
                }
            }

            // QUICK DOMAIN SWITCH HINT / STATUS SUMMARY
            Text(
                text = if (isTactical) "MODE: TACTICAL OPS" else "MODE: ENTERPRISE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp,
                    fontFamily = if (isTactical) FontFamily.Monospace else FontFamily.Default
                ),
                color = if (isTactical) Color(0xFF38BDF8) else Color(0xFF86EFAC)
            )
        }

        // HIGH-CONTRAST STATUS BADGES ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(if (isTactical) Color(0xFF020617) else Color(0xFF072B17))
                .padding(horizontal = 14.dp, vertical = 5.dp)
                .testTag("status_badges_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            brand.statusBadges.forEach { badge ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badge.bg,
                    border = BorderStroke(
                        1.dp,
                        badge.border
                    ),
                    modifier = Modifier.testTag("status_badge_${badge.code}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(badge.border)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = badge.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp,
                                letterSpacing = 0.5.sp,
                                fontFamily = if (isTactical) FontFamily.Monospace else FontFamily.Default
                            ),
                            color = badge.text
                        )
                    }
                }
            }
        }
    }
}
