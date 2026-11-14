import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';
import '../models/offer.dart';
import '../models/payment_method.dart';
import '../models/spend_category.dart';
import '../theme/app_colors.dart';

class EditOfferDialog extends StatefulWidget {
  final Offer? initialOffer;
  final List<PaymentMethod> methods;
  final ValueChanged<Offer> onSave;

  const EditOfferDialog({
    super.key,
    this.initialOffer,
    required this.methods,
    required this.onSave,
  });

  @override
  State<EditOfferDialog> createState() => _EditOfferDialogState();
}

class _EditOfferDialogState extends State<EditOfferDialog> {
  final _formKey = GlobalKey<FormState>();

  late String _selectedMethodId;
  late TextEditingController _titleController;
  late TextEditingController _descriptionController;
  late TextEditingController _minAmountController;
  late TextEditingController _discountValueController;
  late TextEditingController _maxDiscountController;
  late TextEditingController _usageCapController;

  bool _isPercentage = false;
  final Set<SpendCategory> _selectedCategories = {};
  bool _stacks = true;
  bool _requiresActivation = false;
  bool _isActive = true;

  @override
  void initState() {
    super.initState();
    final o = widget.initialOffer;

    _selectedMethodId = o?.methodId ??
        (widget.methods.isNotEmpty ? widget.methods.first.id : '');

    _titleController = TextEditingController(text: o?.title ?? '');
    _descriptionController = TextEditingController(text: o?.description ?? '');
    _minAmountController = TextEditingController(
        text: o != null && o.minAmount > 0 ? o.minAmount.toStringAsFixed(0) : '0');

    if (o != null) {
      if (o.discountPercent != null && o.discountPercent! > 0) {
        _isPercentage = true;
        _discountValueController =
            TextEditingController(text: o.discountPercent!.toString());
      } else {
        _isPercentage = false;
        _discountValueController = TextEditingController(
            text: o.discountFlat != null ? o.discountFlat!.toString() : '50');
      }
      _maxDiscountController = TextEditingController(
          text: o.maxDiscount != null ? o.maxDiscount!.toStringAsFixed(0) : '');
      _usageCapController =
          TextEditingController(text: o.usageCap.toString());
      _selectedCategories.addAll(o.categories);
      _stacks = o.stacks;
      _requiresActivation = o.requiresActivation;
      _isActive = o.isActive;
    } else {
      _discountValueController = TextEditingController(text: '50');
      _maxDiscountController = TextEditingController();
      _usageCapController = TextEditingController(text: '5');
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    _minAmountController.dispose();
    _discountValueController.dispose();
    _maxDiscountController.dispose();
    _usageCapController.dispose();
    super.dispose();
  }

  void _save() {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedMethodId.isEmpty) return;

    final title = _titleController.text.trim();
    final desc = _descriptionController.text.trim();
    final minAmount = double.tryParse(_minAmountController.text.trim()) ?? 0.0;
    final discountVal =
        double.tryParse(_discountValueController.text.trim()) ?? 0.0;
    final maxDiscText = _maxDiscountController.text.trim();
    final maxDiscount =
        maxDiscText.isNotEmpty ? double.tryParse(maxDiscText) : null;
    final usageCap = int.tryParse(_usageCapController.text.trim()) ?? 10;

    final updated = Offer(
      id: widget.initialOffer?.id ?? const Uuid().v4(),
      methodId: _selectedMethodId,
      title: title,
      description: desc,
      categories: _selectedCategories.toList(),
      minAmount: minAmount,
      discountPercent: _isPercentage ? discountVal : null,
      discountFlat: !_isPercentage ? discountVal : null,
      maxDiscount: maxDiscount,
      usageCap: usageCap,
      usageCount: widget.initialOffer?.usageCount ?? 0,
      stacks: _stacks,
      requiresActivation: _requiresActivation,
      isActive: _isActive,
    );

    widget.onSave(updated);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.initialOffer != null;

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
                      isEditing ? 'Edit Offer' : 'Add New Offer',
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
                        // Target Card Selector
                        DropdownButtonFormField<String>(
                          value: _selectedMethodId,
                          decoration:
                              const InputDecoration(labelText: 'Linked Card / Method *'),
                          items: widget.methods.map((m) {
                            return DropdownMenuItem(
                              value: m.id,
                              child: Text(m.name,
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(fontSize: 13)),
                            );
                          }).toList(),
                          onChanged: (val) {
                            if (val != null) {
                              setState(() => _selectedMethodId = val);
                            }
                          },
                        ),
                        const SizedBox(height: 14),

                        // Title
                        TextFormField(
                          controller: _titleController,
                          decoration: const InputDecoration(
                            labelText: 'Offer Title *',
                            hintText: 'e.g. Swiggy ₹100 Flat Off',
                          ),
                          validator: (val) => val == null || val.trim().isEmpty
                              ? 'Please enter offer title'
                              : null,
                        ),
                        const SizedBox(height: 14),

                        // Description
                        TextFormField(
                          controller: _descriptionController,
                          decoration: const InputDecoration(
                            labelText: 'Description / Terms',
                            hintText: 'e.g. Valid on minimum order ₹499',
                          ),
                        ),
                        const SizedBox(height: 14),

                        // Discount Type Toggle
                        Row(
                          children: [
                            const Text('Discount Type: ',
                                style: TextStyle(
                                    fontSize: 13,
                                    color: AppColors.textSecondary)),
                            const SizedBox(width: 8),
                            ChoiceChip(
                              label: const Text('Flat (₹)'),
                              selected: !_isPercentage,
                              onSelected: (val) =>
                                  setState(() => _isPercentage = false),
                            ),
                            const SizedBox(width: 8),
                            ChoiceChip(
                              label: const Text('Percent (%)'),
                              selected: _isPercentage,
                              onSelected: (val) =>
                                  setState(() => _isPercentage = true),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),

                        // Discount Value & Min Amount
                        Row(
                          children: [
                            Expanded(
                              child: TextFormField(
                                controller: _discountValueController,
                                keyboardType: const TextInputType.numberWithOptions(
                                    decimal: true),
                                decoration: InputDecoration(
                                  labelText: _isPercentage
                                      ? 'Discount (%) *'
                                      : 'Flat Discount (₹) *',
                                  prefixText: _isPercentage ? null : '₹ ',
                                  suffixText: _isPercentage ? '%' : null,
                                ),
                                validator: (val) {
                                  if (val == null || val.trim().isEmpty) {
                                    return 'Required';
                                  }
                                  final num = double.tryParse(val.trim());
                                  if (num == null || num <= 0) return 'Invalid';
                                  return null;
                                },
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: TextFormField(
                                controller: _minAmountController,
                                keyboardType: TextInputType.number,
                                decoration: const InputDecoration(
                                  labelText: 'Min Spend (₹)',
                                  prefixText: '₹ ',
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),

                        // Max Discount (if %) & Usage Cap
                        Row(
                          children: [
                            if (_isPercentage) ...[
                              Expanded(
                                child: TextFormField(
                                  controller: _maxDiscountController,
                                  keyboardType: TextInputType.number,
                                  decoration: const InputDecoration(
                                    labelText: 'Max Discount (₹)',
                                    prefixText: '₹ ',
                                  ),
                                ),
                              ),
                              const SizedBox(width: 12),
                            ],
                            Expanded(
                              child: TextFormField(
                                controller: _usageCapController,
                                keyboardType: TextInputType.number,
                                decoration: const InputDecoration(
                                  labelText: 'Usage Cap (Times)',
                                  hintText: 'e.g. 5 (0 for unlimited)',
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 20),

                        // Applicable Categories
                        const Text(
                          'Applicable Categories',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 14,
                            color: AppColors.textPrimary,
                          ),
                        ),
                        const SizedBox(height: 6),
                        const Text(
                          'Leave empty to apply to all categories, or select specific ones:',
                          style:
                              TextStyle(color: AppColors.textSecondary, fontSize: 12),
                        ),
                        const SizedBox(height: 8),
                        Wrap(
                          spacing: 8,
                          runSpacing: 8,
                          children: SpendCategory.values.map((cat) {
                            final isSelected =
                                _selectedCategories.contains(cat);
                            return FilterChip(
                              label: Text(cat.displayName,
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: isSelected
                                        ? AppColors.primaryLight
                                        : AppColors.textSecondary,
                                  )),
                              selected: isSelected,
                              selectedColor: AppColors.primary.withOpacity(0.2),
                              side: BorderSide(
                                color: isSelected
                                    ? AppColors.primaryLight
                                    : AppColors.border,
                              ),
                              onSelected: (selected) {
                                setState(() {
                                  if (selected) {
                                    _selectedCategories.add(cat);
                                  } else {
                                    _selectedCategories.remove(cat);
                                  }
                                });
                              },
                            );
                          }).toList(),
                        ),
                        const SizedBox(height: 16),

                        // Stacks Checkbox
                        CheckboxListTile(
                          contentPadding: EdgeInsets.zero,
                          title: const Text('Stacks with Card Cashback',
                              style: TextStyle(fontSize: 14)),
                          subtitle: const Text(
                              'When checked, this offer reward is added on top of the card cashback.',
                              style: TextStyle(
                                  fontSize: 12, color: AppColors.textMuted)),
                          value: _stacks,
                          activeColor: AppColors.primary,
                          onChanged: (val) =>
                              setState(() => _stacks = val ?? true),
                        ),

                        // Requires Activation Checkbox
                        CheckboxListTile(
                          contentPadding: EdgeInsets.zero,
                          title: const Text('Requires Activation in Bank App',
                              style: TextStyle(fontSize: 14)),
                          subtitle: const Text(
                              'Warns user to activate offer in banking app before paying.',
                              style: TextStyle(
                                  fontSize: 12, color: AppColors.textMuted)),
                          value: _requiresActivation,
                          activeColor: AppColors.warning,
                          onChanged: (val) =>
                              setState(() => _requiresActivation = val ?? false),
                        ),

                        // Active Toggle
                        SwitchListTile(
                          contentPadding: EdgeInsets.zero,
                          title: const Text('Offer is Active',
                              style: TextStyle(fontSize: 14)),
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
                      child: Text(isEditing ? 'Save Offer' : 'Add Offer'),
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
