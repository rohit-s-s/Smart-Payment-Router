import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:uuid/uuid.dart';
import '../models/payment_method.dart';
import '../models/spend_category.dart';
import '../theme/app_colors.dart';

class EditCardDialog extends StatefulWidget {
  final PaymentMethod? initialMethod;
  final ValueChanged<PaymentMethod> onSave;

  const EditCardDialog({
    super.key,
    this.initialMethod,
    required this.onSave,
  });

  @override
  State<EditCardDialog> createState() => _EditCardDialogState();
}

class _EditCardDialogState extends State<EditCardDialog> {
  final _formKey = GlobalKey<FormState>();

  late TextEditingController _nameController;
  late TextEditingController _issuerController;
  late TextEditingController _baseRateController;
  late TextEditingController _monthlyCapController;
  late TextEditingController _cardLast4Controller;
  late TextEditingController _upiVpaController;

  PaymentMethodType _selectedType = PaymentMethodType.creditCard;
  String _selectedNetwork = 'Visa';
  final Set<SpendCategory> _exclusions = {};
  final List<RewardRule> _rules = [];
  bool _isActive = true;

  final List<String> _networks = [
    'Visa',
    'Mastercard',
    'RuPay',
    'American Express',
    'Diners Club',
    'UPI',
  ];

  @override
  void initState() {
    super.initState();
    final m = widget.initialMethod;
    _nameController = TextEditingController(text: m?.name ?? '');
    _issuerController = TextEditingController(text: m?.issuer ?? '');
    _baseRateController =
        TextEditingController(text: m != null ? m.baseRate.toString() : '1.0');
    _monthlyCapController = TextEditingController(
        text: m?.monthlyCap != null ? m!.monthlyCap!.toStringAsFixed(0) : '');
    _cardLast4Controller = TextEditingController(text: m?.cardLast4 ?? '');
    _upiVpaController = TextEditingController(text: m?.upiVpa ?? '');

    if (m != null) {
      _selectedType = m.type;
      _selectedNetwork = m.network;
      _exclusions.addAll(m.exclusions);
      _rules.addAll(m.rules);
      _isActive = m.isActive;
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _issuerController.dispose();
    _baseRateController.dispose();
    _monthlyCapController.dispose();
    _cardLast4Controller.dispose();
    _upiVpaController.dispose();
    super.dispose();
  }

  void _addBonusRule() {
    SpendCategory selectedCat = SpendCategory.dining;
    final rateController = TextEditingController(text: '5.0');

    showDialog(
      context: context,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            return AlertDialog(
              title: const Text('Add Category Bonus'),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Select Category:',
                      style: TextStyle(fontSize: 13, color: AppColors.textSecondary)),
                  const SizedBox(height: 6),
                  DropdownButtonFormField<SpendCategory>(
                    value: selectedCat,
                    items: SpendCategory.values.map((c) {
                      return DropdownMenuItem(
                        value: c,
                        child: Text(c.displayName),
                      );
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) {
                        setDialogState(() => selectedCat = val);
                      }
                    },
                  ),
                  const SizedBox(height: 14),
                  const Text('Bonus Rate (%):',
                      style: TextStyle(fontSize: 13, color: AppColors.textSecondary)),
                  const SizedBox(height: 6),
                  TextField(
                    controller: rateController,
                    keyboardType:
                        const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(
                      hintText: 'e.g. 5.0',
                      suffixText: '%',
                    ),
                  ),
                ],
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.of(ctx).pop(),
                  child: const Text('Cancel'),
                ),
                ElevatedButton(
                  onPressed: () {
                    final rate = double.tryParse(rateController.text.trim()) ?? 0.0;
                    if (rate > 0) {
                      setState(() {
                        _rules.removeWhere((r) => r.category == selectedCat);
                        _rules.add(RewardRule(
                          category: selectedCat,
                          rate: rate,
                        ));
                      });
                      Navigator.of(ctx).pop();
                    }
                  },
                  child: const Text('Add Rule'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  void _save() {
    if (!_formKey.currentState!.validate()) return;

    final name = _nameController.text.trim();
    final issuer = _issuerController.text.trim();
    final baseRate = double.tryParse(_baseRateController.text.trim()) ?? 1.0;
    final capText = _monthlyCapController.text.trim();
    final monthlyCap = capText.isNotEmpty ? double.tryParse(capText) : null;
    final last4 = _cardLast4Controller.text.trim();
    final vpa = _upiVpaController.text.trim();
    final todayStr = DateFormat('yyyy-MM-dd').format(DateTime.now());

    final updated = PaymentMethod(
      id: widget.initialMethod?.id ?? const Uuid().v4(),
      name: name,
      type: _selectedType,
      issuer: issuer,
      network: _selectedNetwork,
      baseRate: baseRate,
      rules: _rules,
      exclusions: _exclusions.toList(),
      monthlyCap: monthlyCap,
      currentMonthRewardUsed:
          widget.initialMethod?.currentMonthRewardUsed ?? 0.0,
      ruleLastConfirmed: todayStr,
      isActive: _isActive,
      cardLast4: last4.isNotEmpty ? last4 : null,
      upiVpa: vpa.isNotEmpty ? vpa : null,
    );

    widget.onSave(updated);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.initialMethod != null;

    return Dialog(
      insetPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 24),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 550, maxHeight: 720),
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      isEditing ? 'Edit Payment Method' : 'Add Payment Method',
                      style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close, size: 20),
                      onPressed: () => Navigator.of(context).pop(),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Expanded(
                  child: SingleChildScrollView(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Method Name
                        TextFormField(
                          controller: _nameController,
                          decoration: const InputDecoration(
                            labelText: 'Method / Card Name *',
                            hintText: 'e.g. HDFC Millennia Credit Card',
                          ),
                          validator: (val) => val == null || val.trim().isEmpty
                              ? 'Please enter method name'
                              : null,
                        ),
                        const SizedBox(height: 14),

                        // Type & Issuer row
                        Row(
                          children: [
                            Expanded(
                              child: DropdownButtonFormField<PaymentMethodType>(
                                value: _selectedType,
                                decoration: const InputDecoration(labelText: 'Type'),
                                items: PaymentMethodType.values.map((t) {
                                  return DropdownMenuItem(
                                    value: t,
                                    child: Text(t.displayName,
                                        style: const TextStyle(fontSize: 13)),
                                  );
                                }).toList(),
                                onChanged: (val) {
                                  if (val != null) {
                                    setState(() => _selectedType = val);
                                  }
                                },
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: TextFormField(
                                controller: _issuerController,
                                decoration: const InputDecoration(
                                  labelText: 'Issuer / Bank',
                                  hintText: 'e.g. HDFC Bank',
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),

                        // Network & Base Rate
                        Row(
                          children: [
                            Expanded(
                              child: DropdownButtonFormField<String>(
                                value: _selectedNetwork,
                                decoration:
                                    const InputDecoration(labelText: 'Network'),
                                items: _networks.map((n) {
                                  return DropdownMenuItem(
                                    value: n,
                                    child: Text(n,
                                        style: const TextStyle(fontSize: 13)),
                                  );
                                }).toList(),
                                onChanged: (val) {
                                  if (val != null) {
                                    setState(() => _selectedNetwork = val);
                                  }
                                },
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: TextFormField(
                                controller: _baseRateController,
                                keyboardType: const TextInputType.numberWithOptions(
                                    decimal: true),
                                decoration: const InputDecoration(
                                  labelText: 'Base Rate (%) *',
                                  suffixText: '%',
                                ),
                                validator: (val) {
                                  if (val == null || val.trim().isEmpty) {
                                    return 'Required';
                                  }
                                  final num = double.tryParse(val.trim());
                                  if (num == null || num < 0) return 'Invalid';
                                  return null;
                                },
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),

                        // Monthly Cap & Last 4 / UPI VPA
                        Row(
                          children: [
                            Expanded(
                              child: TextFormField(
                                controller: _monthlyCapController,
                                keyboardType: TextInputType.number,
                                decoration: const InputDecoration(
                                  labelText: 'Monthly Reward Cap',
                                  hintText: 'e.g. 1000 (₹)',
                                  prefixText: '₹ ',
                                ),
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: TextFormField(
                                controller: _selectedType == PaymentMethodType.upi
                                    ? _upiVpaController
                                    : _cardLast4Controller,
                                decoration: InputDecoration(
                                  labelText:
                                      _selectedType == PaymentMethodType.upi
                                          ? 'UPI ID / VPA'
                                          : 'Card Last 4 Digits',
                                  hintText:
                                      _selectedType == PaymentMethodType.upi
                                          ? 'e.g. user@okhdfcbank'
                                          : 'e.g. 4321',
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 20),

                        // Category Bonus Rules
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text(
                              'Category Bonus Rules',
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                fontSize: 14,
                                color: AppColors.textPrimary,
                              ),
                            ),
                            TextButton.icon(
                              onPressed: _addBonusRule,
                              icon: const Icon(Icons.add, size: 16),
                              label: const Text('Add Category'),
                            ),
                          ],
                        ),
                        const SizedBox(height: 6),
                        if (_rules.isEmpty)
                          Container(
                            width: double.infinity,
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              color: AppColors.surfaceVariant.withOpacity(0.3),
                              borderRadius: BorderRadius.circular(10),
                            ),
                            child: const Text(
                              'No bonus categories added yet. Card will earn base rate on all purchases.',
                              style: TextStyle(
                                color: AppColors.textMuted,
                                fontSize: 12,
                              ),
                            ),
                          )
                        else
                          Wrap(
                            spacing: 8,
                            runSpacing: 8,
                            children: _rules.map((r) {
                              final catLabel =
                                  r.category?.displayName ?? 'All Categories';
                              return Chip(
                                label: Text(
                                  '$catLabel: ${r.rate}%',
                                  style: const TextStyle(
                                    fontSize: 12,
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                                deleteIcon:
                                    const Icon(Icons.close, size: 14),
                                onDeleted: () {
                                  setState(() => _rules.remove(r));
                                },
                                backgroundColor:
                                    AppColors.primary.withOpacity(0.2),
                                side: BorderSide(
                                    color: AppColors.primaryLight.withOpacity(0.5)),
                              );
                            }).toList(),
                          ),

                        const SizedBox(height: 20),

                        // Excluded Categories (US-04)
                        const Text(
                          'Excluded Categories (Earn 0% Rewards)',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 14,
                            color: AppColors.textPrimary,
                          ),
                        ),
                        const SizedBox(height: 6),
                        const Text(
                          'Select categories excluded by your bank (e.g. Fuel, Utilities, Rent):',
                          style:
                              TextStyle(color: AppColors.textSecondary, fontSize: 12),
                        ),
                        const SizedBox(height: 8),
                        Wrap(
                          spacing: 8,
                          runSpacing: 8,
                          children: SpendCategory.values.map((cat) {
                            final isExcluded = _exclusions.contains(cat);
                            return FilterChip(
                              label: Text(cat.displayName,
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: isExcluded
                                        ? AppColors.error
                                        : AppColors.textSecondary,
                                  )),
                              selected: isExcluded,
                              selectedColor: AppColors.error.withOpacity(0.2),
                              checkmarkColor: AppColors.error,
                              side: BorderSide(
                                color: isExcluded
                                    ? AppColors.error
                                    : AppColors.border,
                              ),
                              onSelected: (selected) {
                                setState(() {
                                  if (selected) {
                                    _exclusions.add(cat);
                                  } else {
                                    _exclusions.remove(cat);
                                  }
                                });
                              },
                            );
                          }).toList(),
                        ),

                        const SizedBox(height: 16),

                        // Active toggle
                        SwitchListTile(
                          contentPadding: EdgeInsets.zero,
                          title: const Text('Active for Recommendations',
                              style: TextStyle(fontSize: 14)),
                          subtitle: const Text(
                              'When disabled, this card is not suggested in routing.',
                              style: TextStyle(
                                  fontSize: 12, color: AppColors.textMuted)),
                          value: _isActive,
                          activeColor: AppColors.primary,
                          onChanged: (val) => setState(() => _isActive = val),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    TextButton(
                      onPressed: () => Navigator.of(context).pop(),
                      child: const Text('Cancel'),
                    ),
                    const SizedBox(width: 8),
                    ElevatedButton(
                      onPressed: _save,
                      child: Text(isEditing ? 'Save Changes' : 'Add Card'),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
