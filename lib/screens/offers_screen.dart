import 'package:flutter/material.dart';
import '../data/payment_repository.dart';
import '../models/offer.dart';
import '../theme/app_colors.dart';
import '../widgets/delete_confirm_dialog.dart';
import '../widgets/edit_offer_dialog.dart';

class OffersScreen extends StatelessWidget {
  final PaymentRepository repository;

  const OffersScreen({super.key, required this.repository});

  void _openAddOfferDialog(BuildContext context) {
    if (repository.methods.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please add a payment method before creating offers.')),
      );
      return;
    }

    showDialog(
      context: context,
      builder: (_) => EditOfferDialog(
        methods: repository.methods,
        onSave: (newOffer) => repository.addOffer(newOffer),
      ),
    );
  }

  void _openEditOfferDialog(BuildContext context, Offer offer) {
    showDialog(
      context: context,
      builder: (_) => EditOfferDialog(
        initialOffer: offer,
        methods: repository.methods,
        onSave: (updated) => repository.updateOffer(updated),
      ),
    );
  }

  void _confirmDelete(BuildContext context, Offer offer) {
    showDialog(
      context: context,
      builder: (_) => DeleteConfirmDialog(
        title: 'Delete Offer',
        message: 'Are you sure you want to delete "${offer.title}"?',
        onConfirm: () => repository.deleteOffer(offer.id),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: repository,
      builder: (context, _) {
        final offers = repository.offers;

        return Scaffold(
          appBar: AppBar(
            title: const Text('Offers & Boosters'),
            actions: [
              IconButton(
                tooltip: 'Add Offer',
                icon: const Icon(Icons.add),
                onPressed: () => _openAddOfferDialog(context),
              ),
            ],
          ),
          body: offers.isEmpty
              ? Container(
                  padding: const EdgeInsets.all(32),
                  child: Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(Icons.local_offer_outlined,
                            size: 48, color: AppColors.textMuted),
                        const SizedBox(height: 12),
                        const Text(
                          'No active offers added yet',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textSecondary,
                          ),
                        ),
                        const SizedBox(height: 8),
                        const Text(
                          'Add temporary discount vouchers, merchant promos, and seasonal boosts to stack on your cards.',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                              color: AppColors.textMuted, fontSize: 13),
                        ),
                        const SizedBox(height: 16),
                        ElevatedButton.icon(
                          onPressed: () => _openAddOfferDialog(context),
                          icon: const Icon(Icons.add),
                          label: const Text('Add First Offer'),
                        ),
                      ],
                    ),
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: offers.length,
                  itemBuilder: (context, index) {
                    final offer = offers[index];
                    return _buildOfferCard(context, offer);
                  },
                ),
          floatingActionButton: FloatingActionButton(
            backgroundColor: AppColors.primary,
            foregroundColor: Colors.white,
            onPressed: () => _openAddOfferDialog(context),
            child: const Icon(Icons.add),
          ),
        );
      },
    );
  }

  Widget _buildOfferCard(BuildContext context, Offer offer) {
    // Find linked payment method name
    final linkedMethod = repository.methods.cast<dynamic>().firstWhere(
      (m) => m.id == offer.methodId,
      orElse: () => null,
    );
    final methodName = linkedMethod?.name ?? 'Unknown Card';

    final isExhausted = offer.isExhausted;

    String discountText = '';
    if (offer.discountFlat != null && offer.discountFlat! > 0) {
      discountText = '₹${offer.discountFlat!.toInt()} Flat Off';
    } else if (offer.discountPercent != null && offer.discountPercent! > 0) {
      discountText = '${offer.discountPercent}% Off';
      if (offer.maxDiscount != null && offer.maxDiscount! > 0) {
        discountText += ' (Max ₹${offer.maxDiscount!.toInt()})';
      }
    }

    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: Card(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(
            color: isExhausted
                ? AppColors.error.withOpacity(0.4)
                : AppColors.border,
            width: 1,
          ),
        ),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header: Title & Switch
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          offer.title,
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 16,
                            color: AppColors.textPrimary,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          'On $methodName',
                          style: const TextStyle(
                            fontSize: 12,
                            color: AppColors.primaryLight,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ),
                  Switch(
                    value: offer.isActive,
                    activeColor: AppColors.primary,
                    onChanged: (val) {
                      repository.updateOffer(offer.copyWith(isActive: val));
                    },
                  ),
                ],
              ),

              const SizedBox(height: 10),

              // Description if present
              if (offer.description.isNotEmpty) ...[
                Text(
                  offer.description,
                  style: const TextStyle(
                    fontSize: 12,
                    color: AppColors.textSecondary,
                  ),
                ),
                const SizedBox(height: 10),
              ],

              // Badges row: Discount value, min spend, stacking, activation
              Wrap(
                spacing: 8,
                runSpacing: 6,
                children: [
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.successBg,
                      borderRadius: BorderRadius.circular(6),
                      border: Border.all(color: AppColors.success.withOpacity(0.4)),
                    ),
                    child: Text(
                      discountText,
                      style: const TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.bold,
                        color: AppColors.success,
                      ),
                    ),
                  ),
                  if (offer.minAmount > 0)
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: AppColors.surfaceVariant,
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(
                        'Min Spend ₹${offer.minAmount.toInt()}',
                        style: const TextStyle(
                          fontSize: 11,
                          color: AppColors.textSecondary,
                        ),
                      ),
                    ),
                  if (offer.stacks)
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: AppColors.primary.withOpacity(0.15),
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: const Text(
                        'Stacks with Card',
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w500,
                          color: AppColors.primaryLight,
                        ),
                      ),
                    ),
                  if (offer.requiresActivation)
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: AppColors.warning.withOpacity(0.15),
                        borderRadius: BorderRadius.circular(6),
                        border:
                            Border.all(color: AppColors.warning.withOpacity(0.4)),
                      ),
                      child: const Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(Icons.bolt, size: 12, color: AppColors.warning),
                          SizedBox(width: 4),
                          Text(
                            'Needs Activation',
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                              color: AppColors.warning,
                            ),
                          ),
                        ],
                      ),
                    ),
                ],
              ),

              const SizedBox(height: 12),

              // Categories if specific
              if (offer.categories.isNotEmpty) ...[
                Row(
                  children: [
                    const Text(
                      'Categories: ',
                      style: TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.bold,
                        color: AppColors.textMuted,
                      ),
                    ),
                    Expanded(
                      child: Text(
                        offer.categories.map((c) => c.displayName).join(', '),
                        style: const TextStyle(
                          fontSize: 11,
                          color: AppColors.textSecondary,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
              ],

              // Usage Tracker (Epic 2 US-07)
              if (offer.usageCap > 0) ...[
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      isExhausted ? 'Usage Cap Reached' : 'Usage Tracker',
                      style: TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.w600,
                        color: isExhausted
                            ? AppColors.error
                            : AppColors.textMuted,
                      ),
                    ),
                    Text(
                      '${offer.usageCount} / ${offer.usageCap} used',
                      style: TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.bold,
                        color: isExhausted
                            ? AppColors.error
                            : AppColors.textSecondary,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                ClipRRect(
                  borderRadius: BorderRadius.circular(4),
                  child: LinearProgressIndicator(
                    value: (offer.usageCount / offer.usageCap).clamp(0.0, 1.0),
                    backgroundColor: AppColors.surfaceVariant,
                    color: isExhausted ? AppColors.error : AppColors.secondary,
                    minHeight: 6,
                  ),
                ),
                const SizedBox(height: 10),
              ],

              const Divider(color: AppColors.border),
              const SizedBox(height: 6),

              // Actions: Mark Used (+1), Edit, Delete
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  if (!isExhausted)
                    TextButton.icon(
                      style: TextButton.styleFrom(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 10, vertical: 4),
                        minimumSize: Size.zero,
                        tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                      ),
                      onPressed: () {
                        repository.incrementOfferUsage(offer.id);
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(
                                'Marked "${offer.title}" used (+1). Total: ${offer.usageCount + 1}/${offer.usageCap}'),
                            duration: const Duration(seconds: 2),
                          ),
                        );
                      },
                      icon: const Icon(Icons.add_task, size: 14),
                      label: const Text('Mark as Used (+1)',
                          style: TextStyle(fontSize: 12)),
                    )
                  else
                    const Text(
                      'Offer Exhausted',
                      style: TextStyle(
                        fontSize: 11,
                        color: AppColors.error,
                        fontWeight: FontWeight.bold,
                      ),
                    ),

                  Row(
                    children: [
                      IconButton(
                        icon: const Icon(Icons.edit_outlined, size: 18),
                        onPressed: () => _openEditOfferDialog(context, offer),
                        tooltip: 'Edit Offer',
                      ),
                      IconButton(
                        icon: const Icon(Icons.delete_outline,
                            size: 18, color: AppColors.error),
                        onPressed: () => _confirmDelete(context, offer),
                        tooltip: 'Delete Offer',
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
