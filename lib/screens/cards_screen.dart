import 'package:flutter/material.dart';
import '../data/payment_repository.dart';
import '../models/payment_method.dart';
import '../theme/app_colors.dart';
import '../widgets/delete_confirm_dialog.dart';
import '../widgets/edit_card_dialog.dart';

class CardsScreen extends StatelessWidget {
  final PaymentRepository repository;

  const CardsScreen({super.key, required this.repository});

  void _openAddCardDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (_) => EditCardDialog(
        onSave: (newMethod) => repository.addMethod(newMethod),
      ),
    );
  }

  void _openEditCardDialog(BuildContext context, PaymentMethod method) {
    showDialog(
      context: context,
      builder: (_) => EditCardDialog(
        initialMethod: method,
        onSave: (updated) => repository.updateMethod(updated),
      ),
    );
  }

  void _confirmDelete(BuildContext context, PaymentMethod method) {
    showDialog(
      context: context,
      builder: (_) => DeleteConfirmDialog(
        title: 'Delete Payment Method',
        message:
            'Are you sure you want to delete "${method.name}"? Linked offers will also be removed.',
        onConfirm: () => repository.deleteMethod(method.id),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: repository,
      builder: (context, _) {
        final methods = repository.methods;
        final outdatedCount = methods.where((m) => m.isOutdated()).length;

        return Scaffold(
          appBar: AppBar(
            title: const Text('My Cards & Methods'),
            actions: [
              IconButton(
                tooltip: 'Add Method',
                icon: const Icon(Icons.add),
                onPressed: () => _openAddCardDialog(context),
              ),
            ],
          ),
          body: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              // 90-Day Review Alert Banner (Epic 2 US-05)
              if (outdatedCount > 0) ...[
                Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: AppColors.warningBg.withOpacity(0.6),
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(
                        color: AppColors.warning.withOpacity(0.4), width: 1.5),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.notification_important,
                          color: AppColors.warning, size: 24),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '$outdatedCount card(s) require 90-day review',
                              style: const TextStyle(
                                fontWeight: FontWeight.bold,
                                color: AppColors.warning,
                                fontSize: 13,
                              ),
                            ),
                            const SizedBox(height: 2),
                            const Text(
                              'Credit card rewards change frequently. Confirm your rates to keep optimization accurate.',
                              style: TextStyle(
                                  color: AppColors.textSecondary, fontSize: 11),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
              ],

              if (methods.isEmpty)
                Container(
                  padding: const EdgeInsets.all(32),
                  child: Center(
                    child: Column(
                      children: [
                        const Icon(Icons.credit_card_off,
                            size: 48, color: AppColors.textMuted),
                        const SizedBox(height: 12),
                        const Text(
                          'No payment methods added yet',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textSecondary,
                          ),
                        ),
                        const SizedBox(height: 8),
                        const Text(
                          'Tap + to add your credit cards, debit cards, or UPI accounts.',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                              color: AppColors.textMuted, fontSize: 13),
                        ),
                        const SizedBox(height: 16),
                        ElevatedButton.icon(
                          onPressed: () => _openAddCardDialog(context),
                          icon: const Icon(Icons.add),
                          label: const Text('Add First Card'),
                        ),
                      ],
                    ),
                  ),
                )
              else
                ...methods.map((method) => _buildMethodCard(context, method)),
            ],
          ),
          floatingActionButton: FloatingActionButton(
            backgroundColor: AppColors.primary,
            foregroundColor: Colors.white,
            onPressed: () => _openAddCardDialog(context),
            child: const Icon(Icons.add),
          ),
        );
      },
    );
  }

  Widget _buildMethodCard(BuildContext context, PaymentMethod m) {
    final isOutdated = m.isOutdated();
    final days = m.daysSinceConfirmed();

    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: Card(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(
            color: isOutdated
                ? AppColors.warning.withOpacity(0.5)
                : AppColors.border,
            width: isOutdated ? 1.5 : 1,
          ),
        ),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header: Name, Issuer, Type & Active switch
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Flexible(
                              child: Text(
                                m.name,
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 16,
                                  color: AppColors.textPrimary,
                                ),
                              ),
                            ),
                            if (m.cardLast4 != null && m.cardLast4!.isNotEmpty)
                              Padding(
                                padding: const EdgeInsets.only(left: 6),
                                child: Text(
                                  '•••• ${m.cardLast4}',
                                  style: const TextStyle(
                                    fontSize: 12,
                                    color: AppColors.textMuted,
                                    fontWeight: FontWeight.w500,
                                  ),
                                ),
                              ),
                          ],
                        ),
                        const SizedBox(height: 2),
                        Text(
                          '${m.issuer} • ${m.network} • ${m.type.displayName}',
                          style: const TextStyle(
                            fontSize: 12,
                            color: AppColors.textSecondary,
                          ),
                        ),
                      ],
                    ),
                  ),
                  Switch(
                    value: m.isActive,
                    activeColor: AppColors.primary,
                    onChanged: (val) {
                      repository.updateMethod(m.copyWith(isActive: val));
                    },
                  ),
                ],
              ),

              const SizedBox(height: 12),

              // Rates and Bonus rules
              Row(
                children: [
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.surfaceVariant,
                      borderRadius: BorderRadius.circular(6),
                    ),
                    child: Text(
                      'Base: ${m.baseRate}%',
                      style: const TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                        color: AppColors.textPrimary,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      child: Row(
                        children: m.rules.map((r) {
                          final catName =
                              r.category?.displayName ?? 'All Categories';
                          return Padding(
                            padding: const EdgeInsets.only(right: 6),
                            child: Container(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 8, vertical: 4),
                              decoration: BoxDecoration(
                                color: AppColors.primary.withOpacity(0.15),
                                borderRadius: BorderRadius.circular(6),
                                border: Border.all(
                                    color: AppColors.primary.withOpacity(0.3)),
                              ),
                              child: Text(
                                '$catName: ${r.rate}%',
                                style: const TextStyle(
                                  fontSize: 11,
                                  fontWeight: FontWeight.w600,
                                  color: AppColors.primaryLight,
                                ),
                              ),
                            ),
                          );
                        }).toList(),
                      ),
                    ),
                  ),
                ],
              ),

              // Exclusions list if any
              if (m.exclusions.isNotEmpty) ...[
                const SizedBox(height: 8),
                Row(
                  children: [
                    const Icon(Icons.block, size: 12, color: AppColors.error),
                    const SizedBox(width: 4),
                    const Text(
                      'Excluded: ',
                      style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.bold,
                          color: AppColors.error),
                    ),
                    Expanded(
                      child: Text(
                        m.exclusions.map((e) => e.displayName).join(', '),
                        style: const TextStyle(
                            fontSize: 11, color: AppColors.textSecondary),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
              ],

              // Monthly Cap Progress
              if (m.monthlyCap != null && m.monthlyCap! > 0) ...[
                const SizedBox(height: 10),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Monthly Reward Cap',
                          style: TextStyle(
                              fontSize: 11, color: AppColors.textMuted),
                        ),
                        Text(
                          '₹${m.currentMonthRewardUsed.toInt()} / ₹${m.monthlyCap!.toInt()}',
                          style: const TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.w600,
                            color: AppColors.textSecondary,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    ClipRRect(
                      borderRadius: BorderRadius.circular(4),
                      child: LinearProgressIndicator(
                        value: (m.currentMonthRewardUsed / m.monthlyCap!)
                            .clamp(0.0, 1.0),
                        backgroundColor: AppColors.surfaceVariant,
                        color: m.currentMonthRewardUsed >= m.monthlyCap!
                            ? AppColors.error
                            : AppColors.primary,
                        minHeight: 6,
                      ),
                    ),
                  ],
                ),
              ],

              const SizedBox(height: 12),
              const Divider(color: AppColors.border),
              const SizedBox(height: 6),

              // Footer: Stale Rules Status & Actions
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  // 90-day status
                  if (isOutdated)
                    InkWell(
                      onTap: () => repository.confirmRuleFreshness(m.id),
                      borderRadius: BorderRadius.circular(6),
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: AppColors.warning.withOpacity(0.15),
                          borderRadius: BorderRadius.circular(6),
                          border: Border.all(
                              color: AppColors.warning.withOpacity(0.4)),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            const Icon(Icons.timer_outlined,
                                color: AppColors.warning, size: 13),
                            const SizedBox(width: 4),
                            Text(
                              days >= 0 ? '$days days old • Tap to Confirm' : 'Unconfirmed',
                              style: const TextStyle(
                                color: AppColors.warning,
                                fontSize: 11,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                      ),
                    )
                  else
                    Row(
                      children: [
                        const Icon(Icons.check_circle,
                            color: AppColors.success, size: 13),
                        const SizedBox(width: 4),
                        Text(
                          'Verified ${days >= 0 ? "$days d ago" : "recently"}',
                          style: const TextStyle(
                            color: AppColors.textMuted,
                            fontSize: 11,
                          ),
                        ),
                      ],
                    ),

                  // Actions: Edit & Delete
                  Row(
                    children: [
                      IconButton(
                        icon: const Icon(Icons.edit_outlined, size: 18),
                        onPressed: () => _openEditCardDialog(context, m),
                        tooltip: 'Edit Card',
                      ),
                      IconButton(
                        icon: const Icon(Icons.delete_outline,
                            size: 18, color: AppColors.error),
                        onPressed: () => _confirmDelete(context, m),
                        tooltip: 'Delete Card',
                      ),
                    ],
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
