import 'package:flutter/material.dart';
import '../data/payment_repository.dart';
import '../models/payment_log.dart';
import '../models/spend_category.dart';
import '../theme/app_colors.dart';
import '../widgets/category_chip.dart';
import '../widgets/delete_confirm_dialog.dart';

class HistoryScreen extends StatelessWidget {
  final PaymentRepository repository;

  const HistoryScreen({super.key, required this.repository});

  void _confirmClear(BuildContext context) {
    showDialog(
      context: context,
      builder: (_) => DeleteConfirmDialog(
        title: 'Clear History',
        message: 'Are you sure you want to clear all transaction logs?',
        onConfirm: () => repository.clearLogs(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: repository,
      builder: (context, _) {
        final logs = repository.logs;

        final totalSpend =
            logs.fold(0.0, (acc, item) => acc + item.amount);
        final totalRewards =
            logs.fold(0.0, (acc, item) => acc + item.rewardEarned);
        final avgRate =
            totalSpend > 0 ? (totalRewards / totalSpend) * 100.0 : 0.0;

        return Scaffold(
          appBar: AppBar(
            title: const Text('Transaction History'),
            actions: [
              if (logs.isNotEmpty)
                IconButton(
                  tooltip: 'Clear History',
                  icon: const Icon(Icons.delete_sweep_outlined),
                  onPressed: () => _confirmClear(context),
                ),
            ],
          ),
          body: logs.isEmpty
              ? Container(
                  padding: const EdgeInsets.all(32),
                  child: const Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.history,
                            size: 48, color: AppColors.textMuted),
                        SizedBox(height: 12),
                        Text(
                          'No transactions logged yet',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textSecondary,
                          ),
                        ),
                        SizedBox(height: 8),
                        Text(
                          'Every time you route or log a payment, it will appear here with calculated rewards.',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                              color: AppColors.textMuted, fontSize: 13),
                        ),
                      ],
                    ),
                  ),
                )
              : ListView(
                  padding: const EdgeInsets.all(16),
                  children: [
                    // Overview Stats Card
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(18),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Reward Optimization Summary',
                              style: TextStyle(
                                fontSize: 13,
                                fontWeight: FontWeight.w600,
                                color: AppColors.textSecondary,
                              ),
                            ),
                            const SizedBox(height: 16),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                _buildStatItem('Total Spend',
                                    '₹${totalSpend.toStringAsFixed(0)}'),
                                _buildStatItem(
                                  'Rewards Earned',
                                  '₹${totalRewards.toStringAsFixed(2)}',
                                  valueColor: AppColors.success,
                                ),
                                _buildStatItem(
                                  'Return Rate',
                                  '${avgRate.toStringAsFixed(1)}%',
                                  valueColor: AppColors.primaryLight,
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ),

                    const SizedBox(height: 20),

                    const Text(
                      'Past Transactions',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 10),

                    ...logs.reversed.map((log) => _buildLogCard(log)),
                  ],
                ),
        );
      },
    );
  }

  Widget _buildStatItem(String label, String value, {Color? valueColor}) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(fontSize: 11, color: AppColors.textMuted),
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: valueColor ?? AppColors.textPrimary,
          ),
        ),
      ],
    );
  }

  Widget _buildLogCard(PaymentLog log) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Row(
            children: [
              CategoryChip(category: log.category),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      log.payeeName ?? log.category.displayName,
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 14,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      '${log.methodName} • ${log.date}',
                      style: const TextStyle(
                        fontSize: 11,
                        color: AppColors.textSecondary,
                      ),
                    ),
                    if (log.offerApplied != null) ...[
                      const SizedBox(height: 2),
                      Text(
                        'Offer: ${log.offerApplied}',
                        style: const TextStyle(
                          fontSize: 11,
                          color: AppColors.warning,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ],
                  ],
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    '₹${log.amount.toStringAsFixed(0)}',
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 15,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    '+₹${log.rewardEarned.toStringAsFixed(2)}',
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: AppColors.success,
                    ),
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
