import 'package:flutter/material.dart';
import '../data/payment_repository.dart';
import '../models/parsed_upi.dart';
import '../models/spend_category.dart';
import '../theme/app_colors.dart';
import '../widgets/category_chip.dart';
import 'recommendation_screen.dart';

class ManualEntryScreen extends StatefulWidget {
  final PaymentRepository repository;
  final double? initialAmount;
  final SpendCategory? initialCategory;
  final String? initialMerchant;

  const ManualEntryScreen({
    super.key,
    required this.repository,
    this.initialAmount,
    this.initialCategory,
    this.initialMerchant,
  });

  @override
  State<ManualEntryScreen> createState() => _ManualEntryScreenState();
}

class _ManualEntryScreenState extends State<ManualEntryScreen> {
  final _amountController = TextEditingController();
  final _merchantController = TextEditingController();
  late SpendCategory _selectedCategory;
  CategorySource _categorySource = CategorySource.manual;
  String? _matchedMerchantHistoryInfo;

  @override
  void initState() {
    super.initState();
    _selectedCategory = widget.initialCategory ?? SpendCategory.dining;
    if (widget.initialAmount != null && widget.initialAmount! > 0) {
      _amountController.text = widget.initialAmount!.toStringAsFixed(0);
    }
    if (widget.initialMerchant != null) {
      _merchantController.text = widget.initialMerchant!;
    }

    _merchantController.addListener(_onMerchantChanged);
  }

  @override
  void dispose() {
    _amountController.dispose();
    _merchantController.removeListener(_onMerchantChanged);
    _merchantController.dispose();
    super.dispose();
  }

  void _onMerchantChanged() {
    final query = _merchantController.text.trim().toLowerCase();
    if (query.isEmpty) {
      setState(() {
        _matchedMerchantHistoryInfo = null;
      });
      return;
    }

    final normalized = query.replaceAll(RegExp(r'[^a-z0-9]'), '');
    final match = widget.repository.merchants.cast<dynamic>().firstWhere(
      (m) =>
          m.normalizedName == normalized ||
          m.normalizedName.contains(normalized) ||
          normalized.contains(m.normalizedName),
      orElse: () => null,
    );

    if (match != null) {
      setState(() {
        _selectedCategory = match.category;
        _categorySource = CategorySource.merchantHistory;
        _matchedMerchantHistoryInfo =
            'Matched saved merchant "${match.name}" (${match.category.displayName})';
      });
    } else {
      setState(() {
        _matchedMerchantHistoryInfo = null;
      });
    }
  }

  void _addAmount(double add) {
    final current = double.tryParse(_amountController.text.trim()) ?? 0.0;
    final updated = current + add;
    _amountController.text = updated.toStringAsFixed(0);
  }

  void _calculate() {
    final amount = double.tryParse(_amountController.text.trim());
    if (amount == null || amount <= 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter a valid amount greater than ₹0')),
      );
      return;
    }

    final merchantName = _merchantController.text.trim();

    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => RecommendationScreen(
          repository: widget.repository,
          amount: amount,
          category: _selectedCategory,
          categorySource: _categorySource,
          payeeName: merchantName.isNotEmpty ? merchantName : null,
          payeeAddress: null,
          mcc: null,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Manual Entry'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Amount Input Card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Transaction Amount',
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                        color: AppColors.textSecondary,
                      ),
                    ),
                    const SizedBox(height: 10),
                    TextField(
                      controller: _amountController,
                      keyboardType:
                          const TextInputType.numberWithOptions(decimal: true),
                      style: const TextStyle(
                        fontSize: 32,
                        fontWeight: FontWeight.bold,
                        color: AppColors.textPrimary,
                      ),
                      decoration: InputDecoration(
                        prefixIcon: const Padding(
                          padding: EdgeInsets.only(left: 12, top: 4),
                          child: Text(
                            '₹',
                            style: TextStyle(
                              fontSize: 32,
                              fontWeight: FontWeight.bold,
                              color: AppColors.primaryLight,
                            ),
                          ),
                        ),
                        prefixIconConstraints:
                            const BoxConstraints(minWidth: 40, minHeight: 0),
                        hintText: '0',
                        hintStyle: TextStyle(
                          fontSize: 32,
                          color: AppColors.textMuted.withOpacity(0.5),
                        ),
                      ),
                    ),
                    const SizedBox(height: 14),
                    // Quick Increment Chips
                    SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      child: Row(
                        children: [
                          _buildQuickAmountChip(100),
                          const SizedBox(width: 8),
                          _buildQuickAmountChip(500),
                          const SizedBox(width: 8),
                          _buildQuickAmountChip(1000),
                          const SizedBox(width: 8),
                          _buildQuickAmountChip(2000),
                          const SizedBox(width: 8),
                          _buildQuickAmountChip(5000),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Merchant Name Input Card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Merchant / Payee Name (Optional)',
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                        color: AppColors.textSecondary,
                      ),
                    ),
                    const SizedBox(height: 10),
                    TextField(
                      controller: _merchantController,
                      decoration: const InputDecoration(
                        hintText: 'e.g. Starbucks, Swiggy, IndianOil, Amazon',
                        prefixIcon: Icon(Icons.storefront, size: 20),
                      ),
                    ),
                    if (_matchedMerchantHistoryInfo != null) ...[
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          const Icon(Icons.check_circle,
                              color: AppColors.success, size: 14),
                          const SizedBox(width: 6),
                          Expanded(
                            child: Text(
                              _matchedMerchantHistoryInfo!,
                              style: const TextStyle(
                                color: AppColors.success,
                                fontSize: 12,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Category Selection Card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Spend Category',
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w600,
                            color: AppColors.textSecondary,
                          ),
                        ),
                        CategorySourceBadge(source: _categorySource),
                      ],
                    ),
                    const SizedBox(height: 14),
                    Wrap(
                      spacing: 8,
                      runSpacing: 10,
                      children: SpendCategory.values.map((cat) {
                        final isSelected = _selectedCategory == cat;
                        return CategoryChip(
                          category: cat,
                          isSelected: isSelected,
                          onTap: () {
                            setState(() {
                              _selectedCategory = cat;
                              _categorySource = CategorySource.manual;
                              _matchedMerchantHistoryInfo = null;
                            });
                          },
                        );
                      }).toList(),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 28),

            // Submit Button
            SizedBox(
              width: double.infinity,
              height: 52,
              child: ElevatedButton.icon(
                onPressed: _calculate,
                icon: const Icon(Icons.auto_awesome),
                label: const Text(
                  'Route & Maximize Rewards',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildQuickAmountChip(double amount) {
    return InkWell(
      onTap: () => _addAmount(amount),
      borderRadius: BorderRadius.circular(8),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          color: AppColors.surfaceVariant.withOpacity(0.5),
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: AppColors.border),
        ),
        child: Text(
          '+₹${amount.toInt()}',
          style: const TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w600,
            color: AppColors.primaryLight,
          ),
        ),
      ),
    );
  }
}
