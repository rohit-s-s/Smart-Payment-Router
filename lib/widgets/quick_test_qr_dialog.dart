import 'package:flutter/material.dart';
import '../theme/app_colors.dart';

class QuickTestQrDialog extends StatefulWidget {
  final ValueChanged<String> onSelected;

  const QuickTestQrDialog({
    super.key,
    required this.onSelected,
  });

  @override
  State<QuickTestQrDialog> createState() => _QuickTestQrDialogState();
}

class _QuickTestQrDialogState extends State<QuickTestQrDialog> {
  final TextEditingController _customController = TextEditingController();

  static const List<_PresetQr> _presets = [
    _PresetQr(
      title: 'Starbucks Coffee',
      subtitle: 'Dining • ₹450 • MCC 5812',
      icon: Icons.coffee,
      payload:
          'upi://pay?pa=starbucks@hdfcbank&pn=Starbucks%20Coffee&am=450.00&mc=5812&cu=INR',
    ),
    _PresetQr(
      title: 'HPCL Petrol Pump',
      subtitle: 'Fuel • ₹2,000 • MCC 5541',
      icon: Icons.local_gas_station,
      payload:
          'upi://pay?pa=hpcl.outlet@icici&pn=HPCL%20Petrol%20Pump&am=2000.00&mc=5541&cu=INR',
    ),
    _PresetQr(
      title: 'Zepto Groceries',
      subtitle: 'Groceries • ₹890 • MCC 5411',
      icon: Icons.shopping_basket,
      payload:
          'upi://pay?pa=zepto@axisbank&pn=Zepto%20Quick%20Commerce&am=890.00&mc=5411&cu=INR',
    ),
    _PresetQr(
      title: 'Uber India',
      subtitle: 'Travel • ₹350 • MCC 4121',
      icon: Icons.directions_car,
      payload:
          'upi://pay?pa=uber.india@yesbank&pn=Uber%20Rides&am=350.00&mc=4121&cu=INR',
    ),
    _PresetQr(
      title: 'BESCOM Electricity Bill',
      subtitle: 'Utilities • ₹1,500 • MCC 4900',
      icon: Icons.bolt,
      payload:
          'upi://pay?pa=bescom@sbi&pn=BESCOM%20Electricity&am=1500.00&mc=4900&cu=INR',
    ),
    _PresetQr(
      title: 'Amazon Shopping',
      subtitle: 'Shopping • ₹3,200 • MCC 5311',
      icon: Icons.shopping_bag,
      payload:
          'upi://pay?pa=amazonpay@apl&pn=Amazon%20India&am=3200.00&mc=5311&cu=INR',
    ),
  ];

  @override
  void dispose() {
    _customController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 450, maxHeight: 600),
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Quick Test QR Codes',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.close, size: 20),
                    onPressed: () => Navigator.of(context).pop(),
                  ),
                ],
              ),
              const SizedBox(height: 6),
              const Text(
                'Select a preset transaction to test routing on emulator or device:',
                style: TextStyle(color: AppColors.textSecondary, fontSize: 13),
              ),
              const SizedBox(height: 16),
              Expanded(
                child: ListView.separated(
                  shrinkWrap: true,
                  itemCount: _presets.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 8),
                  itemBuilder: (context, index) {
                    final item = _presets[index];
                    return InkWell(
                      borderRadius: BorderRadius.circular(12),
                      onTap: () {
                        Navigator.of(context).pop();
                        widget.onSelected(item.payload);
                      },
                      child: Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: AppColors.surfaceVariant.withOpacity(0.4),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(color: AppColors.border),
                        ),
                        child: Row(
                          children: [
                            Container(
                              padding: const EdgeInsets.all(10),
                              decoration: BoxDecoration(
                                color: AppColors.primary.withOpacity(0.15),
                                borderRadius: BorderRadius.circular(10),
                              ),
                              child: Icon(item.icon,
                                  color: AppColors.primaryLight, size: 20),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    item.title,
                                    style: const TextStyle(
                                      fontWeight: FontWeight.w600,
                                      color: AppColors.textPrimary,
                                      fontSize: 14,
                                    ),
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    item.subtitle,
                                    style: const TextStyle(
                                      color: AppColors.textSecondary,
                                      fontSize: 12,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            const Icon(Icons.chevron_right,
                                color: AppColors.textMuted, size: 18),
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ),
              const SizedBox(height: 14),
              const Divider(color: AppColors.border),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _customController,
                      decoration: const InputDecoration(
                        hintText: 'Or enter custom upi://pay link...',
                        isDense: true,
                      ),
                      style: const TextStyle(fontSize: 13),
                    ),
                  ),
                  const SizedBox(width: 8),
                  ElevatedButton(
                    onPressed: () {
                      final text = _customController.text.trim();
                      if (text.isNotEmpty) {
                        Navigator.of(context).pop();
                        widget.onSelected(text);
                      }
                    },
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 14, vertical: 12),
                    ),
                    child: const Text('Use'),
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

class _PresetQr {
  final String title;
  final String subtitle;
  final IconData icon;
  final String payload;

  const _PresetQr({
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.payload,
  });
}
