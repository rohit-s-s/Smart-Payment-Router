import 'package:flutter/material.dart';
import '../models/offer.dart';
import '../theme/app_colors.dart';

class OfferActivationDialog extends StatelessWidget {
  final Offer offer;
  final VoidCallback onActivated;

  const OfferActivationDialog({
    super.key,
    required this.offer,
    required this.onActivated,
  });

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Row(
        children: [
          Icon(Icons.bolt, color: AppColors.warning, size: 24),
          SizedBox(width: 8),
          Expanded(child: Text('Activate Offer')),
        ],
      ),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            offer.title,
            style: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            offer.description.isNotEmpty
                ? offer.description
                : 'This offer requires activation in your bank or card app before payment.',
            style: const TextStyle(color: AppColors.textSecondary, fontSize: 13),
          ),
          const SizedBox(height: 14),
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: AppColors.warning.withOpacity(0.12),
              borderRadius: BorderRadius.circular(10),
              border: Border.all(color: AppColors.warning.withOpacity(0.3)),
            ),
            child: const Row(
              children: [
                Icon(Icons.info_outline, color: AppColors.warning, size: 18),
                SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Ensure this offer is toggled on in your issuer app so the cashback is credited.',
                    style: TextStyle(color: AppColors.warning, fontSize: 12),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        ElevatedButton(
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.warning,
            foregroundColor: Colors.black,
          ),
          onPressed: () {
            Navigator.of(context).pop();
            onActivated();
          },
          child: const Text("I've Activated It"),
        ),
      ],
    );
  }
}
