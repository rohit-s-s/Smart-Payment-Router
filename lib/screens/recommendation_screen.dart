import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:url_launcher/url_launcher.dart';
import '../data/payment_repository.dart';
import '../models/parsed_upi.dart';
import '../models/payment_log.dart';
import '../models/recommendation.dart';
import '../models/saved_merchant.dart';
import '../models/spend_category.dart';
import '../services/recommendation_engine.dart';
import '../services/upi_parser.dart';
import '../theme/app_colors.dart';
import '../widgets/category_chip.dart';
import '../widgets/offer_activation_dialog.dart';

class RecommendationScreen extends StatefulWidget {
  final PaymentRepository repository;
  final double amount;
  final SpendCategory category;
  final CategorySource categorySource;
  final String? payeeName;
  final String? payeeAddress;
  final String? mcc;

  const RecommendationScreen({
    super.key,
    required this.repository,
    required this.amount,
    required this.category,
    required this.categorySource,
    this.payeeName,
    this.payeeAddress,
    this.mcc,
  });

  @override
  State<RecommendationScreen> createState() => _RecommendationScreenState();
}

class _RecommendationScreenState extends State<RecommendationScreen> {
  late List<Recommendation> _recommendations;
  late int _calculationTimeMs;
  bool _isLogged = false;

  @override
  void initState() {
    super.initState();
    _computeRecommendations();
  }

  void _computeRecommendations() {
    final stopwatch = Stopwatch()..start();
    _recommendations = RecommendationEngine.calculate(
      amount: widget.amount,
      category: widget.category,
      methods: widget.repository.methods,
      offers: widget.repository.offers,
    );
    stopwatch.stop();
    _calculationTimeMs = stopwatch.elapsedMilliseconds;
  }

  Future<void> _payViaUpi(Recommendation topPick) async {
    if (widget.payeeAddress == null || widget.payeeAddress!.isEmpty) {
      _showPaymentLogged(topPick);
      return;
    }

    final upiUri = UpiParser.buildUpiUri(
      payeeAddress: widget.payeeAddress!,
      payeeName: widget.payeeName,
      amount: widget.amount,
      category: widget.category,
      mcc: widget.mcc,
    );

    try {
      final launched = await launchUrl(
        upiUri,
        mode: LaunchMode.externalNonBrowserApplication,
      );
      if (!launched) {
        // Fallback: copy VPA to clipboard
        await Clipboard.setData(ClipboardData(text: widget.payeeAddress!));
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                  'No UPI app could be launched. Copied VPA "${widget.payeeAddress}" to clipboard.'),
              backgroundColor: AppColors.warning,
            ),
          );
        }
      }
    } catch (_) {
      await Clipboard.setData(ClipboardData(text: widget.payeeAddress!));
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
                'Copied VPA "${widget.payeeAddress}" to clipboard. Open your UPI app to complete payment.'),
            backgroundColor: AppColors.secondary,
          ),
        );
      }
    }

    _showPaymentLogged(topPick);
  }

  void _showPaymentLogged(Recommendation rec) {
    if (_isLogged) return;

    final dateStr = DateFormat('yyyy-MM-dd HH:mm').format(DateTime.now());
    final log = PaymentLog(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      timestamp: dateStr,
      amount: widget.amount,
      category: widget.category,
      merchantName: widget.payeeName ?? widget.category.displayName,
      payeeAddress: widget.payeeAddress ?? '',
      source: widget.payeeAddress != null ? 'scan' : 'manual',
      recommendedMethodId: rec.method.id,
      recommendedMethodName: rec.method.name,
      chosenMethodId: rec.method.id,
      chosenMethodName: rec.method.name,
      expectedReward: rec.effectiveReward,
      actualReward: rec.effectiveReward,
      offerApplied: rec.breakdown.appliedOffer?.title,
    );

    widget.repository.addLog(log);

    // Save merchant history
    if (widget.payeeName != null && widget.payeeName!.trim().isNotEmpty) {
      widget.repository.saveMerchant(SavedMerchant(
        name: widget.payeeName!.trim(),
        normalizedName: widget.payeeName!
            .toLowerCase()
            .replaceAll(RegExp(r'[^a-z0-9]'), '')
            .trim(),
        vpa: widget.payeeAddress,
        category: widget.category,
        mcc: widget.mcc,
        lastUsedAt: DateTime.now().toIso8601String(),
      ));
    }

    setState(() => _isLogged = true);

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
            'Payment logged! Earned ₹${rec.effectiveReward} on ${rec.method.name}'),
        backgroundColor: AppColors.success,
      ),
    );
  }

  void _showOfferActivation(Recommendation rec) {
    final offer = rec.breakdown.appliedOffer;
    if (offer == null) return;

    showDialog(
      context: context,
      builder: (_) => OfferActivationDialog(
        offer: offer,
        onActivated: () {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Offer confirmed active! Ready to pay.'),
              backgroundColor: AppColors.success,
            ),
          );
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final currencyFormat = NumberFormat('#,##0.00', 'en_IN');

    return Scaffold(
      appBar: AppBar(
        title: const Text('Best Payment Option'),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 16),
            child: Center(
              child: Text(
                '${_calculationTimeMs}ms',
                style: const TextStyle(
                  fontSize: 11,
                  color: AppColors.success,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ),
        ],
      ),
      body: _recommendations.isEmpty
          ? const Center(
              child: Text('No active payment methods configured.'),
            )
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Payment Summary Header
                  _buildPaymentSummaryHeader(),

                  const SizedBox(height: 16),

                  // Top Pick Card (Rank #1)
                  _buildTopPickCard(_recommendations.first, currencyFormat),

                  const SizedBox(height: 24),

                  // Runner-Up Options Header
                  if (_recommendations.length > 1) ...[
                    const Text(
                      'Other Payment Methods',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 10),
                    ListView.separated(
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      itemCount: _recommendations.length - 1,
                      separatorBuilder: (_, __) => const SizedBox(height: 10),
                      itemBuilder: (context, index) {
                        final rec = _recommendations[index + 1];
                        return _buildRunnerUpCard(
                            rec, index + 2, currencyFormat);
                      },
                    ),
                  ],
                ],
              ),
            ),
      bottomNavigationBar: _recommendations.isEmpty
          ? null
          : Container(
              padding: const EdgeInsets.all(16),
              decoration: const BoxDecoration(
                color: AppColors.surface,
                border: Border(top: BorderSide(color: AppColors.border)),
              ),
              child: SafeArea(
                child: Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: _isLogged
                            ? null
                            : () => _showPaymentLogged(_recommendations.first),
                        icon: Icon(
                          _isLogged ? Icons.check : Icons.bookmark_border,
                          size: 18,
                        ),
                        label: Text(_isLogged ? 'Logged' : 'Log Only'),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      flex: 2,
                      child: ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppColors.success,
                          foregroundColor: Colors.white,
                        ),
                        onPressed: () => _payViaUpi(_recommendations.first),
                        icon: const Icon(Icons.payment, size: 20),
                        label: Text(
                          widget.payeeAddress != null
                              ? 'Pay via UPI'
                              : 'Confirm & Save',
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
    );
  }

  Widget _buildPaymentSummaryHeader() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surfaceVariant.withOpacity(0.4),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                widget.payeeName ?? 'Payment',
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 4),
              Row(
                children: [
                  CategoryChip(category: widget.category),
                  const SizedBox(width: 8),
                  CategorySourceBadge(
                    source: widget.categorySource,
                    mccCode: widget.mcc,
                  ),
                ],
              ),
            ],
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              const Text(
                'Total Spend',
                style: TextStyle(fontSize: 11, color: AppColors.textMuted),
              ),
              Text(
                '₹${widget.amount.toStringAsFixed(0)}',
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: AppColors.textPrimary,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildTopPickCard(
      Recommendation rec, NumberFormat currencyFormat) {
    final b = rec.breakdown;

    return Container(
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.success, width: 2),
        boxShadow: [
          BoxShadow(
            color: AppColors.success.withOpacity(0.12),
            blurRadius: 16,
            spreadRadius: 2,
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Top Pick Pill & Card Name
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.success.withOpacity(0.15),
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(color: AppColors.success),
                  ),
                  child: const Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.star, color: AppColors.success, size: 14),
                      SizedBox(width: 4),
                      Text(
                        'TOP RECOMMENDATION',
                        style: TextStyle(
                          color: AppColors.success,
                          fontWeight: FontWeight.bold,
                          fontSize: 11,
                          letterSpacing: 0.5,
                        ),
                      ),
                    ],
                  ),
                ),
                Text(
                  rec.method.issuer,
                  style: const TextStyle(
                    color: AppColors.textSecondary,
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Text(
              rec.method.name,
              style: const TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              rec.reason,
              style: const TextStyle(
                color: AppColors.primaryLight,
                fontSize: 13,
                fontWeight: FontWeight.w500,
              ),
            ),
            const SizedBox(height: 16),

            // Big Reward Display
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              decoration: BoxDecoration(
                color: AppColors.successBg.withOpacity(0.4),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: AppColors.success.withOpacity(0.3)),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Net Rewards Earned',
                    style: TextStyle(
                      color: AppColors.textPrimary,
                      fontWeight: FontWeight.w600,
                      fontSize: 14,
                    ),
                  ),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.baseline,
                    textBaseline: TextBaseline.alphabetic,
                    children: [
                      Text(
                        '+₹${rec.effectiveReward.toStringAsFixed(2)}',
                        style: const TextStyle(
                          fontSize: 22,
                          fontWeight: FontWeight.bold,
                          color: AppColors.success,
                        ),
                      ),
                      const SizedBox(width: 6),
                      Text(
                        '(${rec.effectiveRate}%)',
                        style: const TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                          color: AppColors.textSecondary,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),

            const SizedBox(height: 16),
            const Divider(color: AppColors.border),
            const SizedBox(height: 12),

            // Breakdown grid
            const Text(
              'Reward Breakdown',
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w600,
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: 8),

            if (b.categoryBonus > 0)
              _buildBreakdownRow(
                'Category Bonus',
                '₹${b.categoryBonus.toStringAsFixed(2)}',
                color: AppColors.primaryLight,
              ),
            if (b.excessAtBaseRate > 0)
              _buildBreakdownRow(
                'Spend Excess at Base Rate',
                '₹${b.excessAtBaseRate.toStringAsFixed(2)}',
              ),
            if (b.baseReward > 0)
              _buildBreakdownRow(
                'Base Reward',
                '₹${b.baseReward.toStringAsFixed(2)}',
              ),
            if (b.offerReward > 0)
              _buildBreakdownRow(
                'Offer Savings (${b.appliedOffer?.title ?? ""})',
                '₹${b.offerReward.toStringAsFixed(2)}',
                color: AppColors.warning,
              ),

            // Monthly Cap Alert
            if (b.isCapReached) ...[
              const SizedBox(height: 8),
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: AppColors.warningBg.withOpacity(0.5),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: AppColors.warning.withOpacity(0.4)),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.info, color: AppColors.warning, size: 16),
                    SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'Monthly category bonus cap reached! Additional spend drops to base reward rate.',
                        style: TextStyle(
                            color: AppColors.warning, fontSize: 11),
                      ),
                    ),
                  ],
                ),
              ),
            ],

            // Stale Rules Warning (FR-11)
            if (rec.ruleWarning != null) ...[
              const SizedBox(height: 8),
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: AppColors.warningBg.withOpacity(0.5),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: AppColors.warning.withOpacity(0.4)),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.timer_outlined,
                        color: AppColors.warning, size: 16),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        rec.ruleWarning!,
                        style: const TextStyle(
                            color: AppColors.warning, fontSize: 11),
                      ),
                    ),
                  ],
                ),
              ),
            ],

            // Requires Activation Action (Offer)
            if (b.appliedOffer != null &&
                b.appliedOffer!.requiresActivation) ...[
              const SizedBox(height: 12),
              OutlinedButton.icon(
                style: OutlinedButton.styleFrom(
                  side: const BorderSide(color: AppColors.warning),
                ),
                onPressed: () => _showOfferActivation(rec),
                icon: const Icon(Icons.bolt,
                    color: AppColors.warning, size: 16),
                label: const Text(
                  'Offer Requires Bank Activation - Tap to Review',
                  style: TextStyle(
                      color: AppColors.warning,
                      fontSize: 12,
                      fontWeight: FontWeight.w600),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildRunnerUpCard(
      Recommendation rec, int rank, NumberFormat currencyFormat) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(
          children: [
            CircleAvatar(
              radius: 14,
              backgroundColor: AppColors.surfaceVariant,
              child: Text(
                '#$rank',
                style: const TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.bold,
                  color: AppColors.textSecondary,
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    rec.method.name,
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 14,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    rec.reason,
                    style: const TextStyle(
                      color: AppColors.textSecondary,
                      fontSize: 12,
                    ),
                  ),
                ],
              ),
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  '+₹${rec.effectiveReward.toStringAsFixed(2)}',
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 15,
                    color: AppColors.textPrimary,
                  ),
                ),
                Text(
                  '${rec.effectiveRate}%',
                  style: const TextStyle(
                    color: AppColors.textMuted,
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildBreakdownRow(String label, String value, {Color? color}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 3),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: const TextStyle(
              fontSize: 12,
              color: AppColors.textSecondary,
            ),
          ),
          Text(
            value,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: color ?? AppColors.textPrimary,
            ),
          ),
        ],
      ),
    );
  }
}
