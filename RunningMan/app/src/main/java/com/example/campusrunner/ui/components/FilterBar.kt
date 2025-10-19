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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilterBar(
    modifier: Modifier = Modifier
) {
    // 完全按照demo.html的样式：白色背景，圆角，阴影，精确的padding
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                clip = true
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp) // 精确控制内边距
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 位置筛选
            EnhancedFilterDropdown(
                items = listOf(
                    FilterItem("位置", Icons.Filled.LocationOn),
                    FilterItem("校门口", Icons.Filled.LocationOn),
                    FilterItem("图书馆", Icons.Filled.LocationOn),
                    FilterItem("教学楼", Icons.Filled.LocationOn),
                    FilterItem("宿舍区", Icons.Filled.LocationOn)
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
        // 筛选项样式 - 精确按照demo设计
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .background(
                    color = Color(0xFFf5f5f5), // 使用demo中的背景色
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 8.dp) // 精确控制内边距
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selectedItem.text,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp // 精确字体大小
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
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
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