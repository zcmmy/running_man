package com.example.campusrunner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilterBar(
    modifier: Modifier = Modifier
) {
    EnhancedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 位置筛选
            EnhancedFilterDropdown(
                items = listOf(
                    FilterItem("位置", Icons.Filled.LocationOn),
                    FilterItem("韵苑学生公寓", Icons.Filled.LocationOn),
                    FilterItem("紫菘学生公寓", Icons.Filled.LocationOn),
                    FilterItem("沁苑学生公寓", Icons.Filled.LocationOn),
                    FilterItem("西十二教学楼", Icons.Filled.LocationOn),
                    FilterItem("东九教学楼", Icons.Filled.LocationOn),
                    FilterItem("图书馆", Icons.Filled.LocationOn),
                    FilterItem("西区食堂", Icons.Filled.LocationOn),
                    FilterItem("东区食堂", Icons.Filled.LocationOn),
                    FilterItem("校大门", Icons.Filled.LocationOn)
                ),
                selectedIndex = 0,
                modifier = Modifier.weight(1f)
            )

            // 时间筛选
            EnhancedFilterDropdown(
                items = listOf(
                    FilterItem("时间", Icons.Filled.AccessTime),
                    FilterItem("1小时内", Icons.Filled.AccessTime),
                    FilterItem("2小时内", Icons.Filled.AccessTime),
                    FilterItem("不限", Icons.Filled.AccessTime)
                ),
                selectedIndex = 0,
                modifier = Modifier.weight(1f)
            )

            // 类型筛选
            EnhancedFilterDropdown(
                items = listOf(
                    FilterItem("类型", Icons.Filled.FilterList),
                    FilterItem("外卖", Icons.Filled.Restaurant),
                    FilterItem("快递", Icons.Filled.LocalShipping),
                    FilterItem("打印", Icons.Filled.Print),
                    FilterItem("购物", Icons.Filled.ShoppingCart),
                    FilterItem("其他", Icons.Filled.FilterList)
                ),
                selectedIndex = 0,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

data class FilterItem(
    val text: String,
    val icon: ImageVector
)

@Composable
fun EnhancedFilterDropdown(
    items: List<FilterItem>,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(items[selectedIndex]) }

    Box(
        modifier = modifier
    ) {
        // 自定义下拉触发器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = selectedItem.icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = selectedItem.text,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "展开菜单",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 下拉菜单
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ),
            offset = DpOffset(0.dp, 4.dp)
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.text,
                                modifier = Modifier.padding(start = 6.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp
                                ),
                                maxLines = 1
                            )
                        }
                    },
                    onClick = {
                        selectedItem = item
                        expanded = false
                    }
                )
            }
        }
    }
}