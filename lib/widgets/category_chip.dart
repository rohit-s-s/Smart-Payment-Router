import 'package:flutter/material.dart';
import '../models/parsed_upi.dart';
import '../models/spend_category.dart';
import '../theme/app_colors.dart';

class CategoryChip extends StatelessWidget {
  final SpendCategory category;
  final bool isSelected;
  final VoidCallback? onTap;

  const CategoryChip({
    super.key,
    required this.category,
    this.isSelected = false,
    this.onTap,
  });

  IconData _getIcon(SpendCategory cat) {
    switch (cat) {
      case SpendCategory.dining:
        return Icons.restaurant;
      case SpendCategory.groceries:
        return Icons.shopping_basket;
      case SpendCategory.fuel:
        return Icons.local_gas_station;
      case SpendCategory.travel:
        return Icons.flight_takeoff;
      case SpendCategory.utilities:
        return Icons.bolt;
      case SpendCategory.shopping:
        return Icons.shopping_bag;
      case SpendCategory.entertainment:
        return Icons.movie;
      case SpendCategory.other:
        return Icons.more_horiz;
    }
  }

  @override
  Widget build(BuildContext context) {
    final bg = isSelected
        ? AppColors.primary
        : AppColors.surfaceVariant.withOpacity(0.6);
    final fg = isSelected ? Colors.white : AppColors.textSecondary;
    final borderColor = isSelected ? AppColors.primaryLight : AppColors.border;

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(20),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        decoration: BoxDecoration(
          color: bg,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: borderColor, width: isSelected ? 1.5 : 1),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(_getIcon(category), size: 16, color: fg),
            const SizedBox(width: 6),
            Text(
              category.displayName,
              style: TextStyle(
                color: fg,
                fontSize: 13,
                fontWeight: isSelected ? FontWeight.w600 : FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class CategorySourceBadge extends StatelessWidget {
  final CategorySource source;
  final String? mccCode;

  const CategorySourceBadge({
    super.key,
    required this.source,
    this.mccCode,
  });

  @override
  Widget build(BuildContext context) {
    Color color;
    IconData icon;
    String text = source.label;

    switch (source) {
      case CategorySource.mcc:
        color = AppColors.secondary;
        icon = Icons.qr_code_2;
        if (mccCode != null && mccCode!.isNotEmpty) {
          text = 'MCC $mccCode';
        }
        break;
      case CategorySource.merchantHistory:
        color = AppColors.success;
        icon = Icons.history;
        break;
      case CategorySource.manual:
        color = AppColors.warning;
        icon = Icons.edit;
        break;
      case CategorySource.defaultValue:
        color = AppColors.textMuted;
        icon = Icons.auto_awesome;
        break;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withOpacity(0.15),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: color.withOpacity(0.4)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 12, color: color),
          const SizedBox(width: 4),
          Text(
            text,
            style: TextStyle(
              color: color,
              fontSize: 11,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}
