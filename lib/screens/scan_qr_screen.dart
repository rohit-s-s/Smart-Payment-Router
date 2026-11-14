import 'package:flutter/material.dart';
import 'package:google_mlkit_barcode_scanning/google_mlkit_barcode_scanning.dart'
    as mlkit;
import 'package:image_picker/image_picker.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import '../data/payment_repository.dart';
import '../models/parsed_upi.dart';
import '../models/spend_category.dart';
import '../services/upi_parser.dart';
import '../theme/app_colors.dart';
import '../widgets/quick_test_qr_dialog.dart';
import 'manual_entry_screen.dart';
import 'recommendation_screen.dart';

class ScanQrScreen extends StatefulWidget {
  final PaymentRepository repository;

  const ScanQrScreen({super.key, required this.repository});

  @override
  State<ScanQrScreen> createState() => _ScanQrScreenState();
}

class _ScanQrScreenState extends State<ScanQrScreen> {
  final MobileScannerController _scannerController = MobileScannerController(
    detectionSpeed: DetectionSpeed.noDuplicates,
    facing: CameraFacing.back,
    torchEnabled: false,
  );

  bool _isProcessing = false;
  bool _torchOn = false;

  @override
  void dispose() {
    _scannerController.dispose();
    super.dispose();
  }

  void _handleQrCode(String rawPayload) {
    if (_isProcessing) return;
    setState(() => _isProcessing = true);

    try {
      final parsed = UpiParser.parse(
        rawPayload,
        savedMerchants: widget.repository.merchants,
      );

      if (parsed.amount != null && parsed.amount! > 0) {
        // Amount is available, navigate directly to Recommendation Screen
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => RecommendationScreen(
              repository: widget.repository,
              amount: parsed.amount!,
              category: parsed.suggestedCategory,
              categorySource: parsed.categorySource,
              payeeName: parsed.payeeName,
              payeeAddress: parsed.payeeAddress,
              mcc: parsed.mcc,
            ),
          ),
        ).then((_) {
          if (mounted) setState(() => _isProcessing = false);
        });
      } else {
        // Amount missing, prompt user or push to manual entry prefilled
        _promptForAmount(parsed);
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Failed to process QR code: $e')),
      );
      setState(() => _isProcessing = false);
    }
  }

  void _promptForAmount(ParsedUpi parsed) {
    final amountController = TextEditingController();
    SpendCategory selectedCategory = parsed.suggestedCategory;

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            return AlertDialog(
              title: const Text('Enter Payment Amount'),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  if (parsed.payeeName != null &&
                      parsed.payeeName!.isNotEmpty) ...[
                    Text(
                      'Payee: ${parsed.payeeName}',
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        color: AppColors.primaryLight,
                      ),
                    ),
                    const SizedBox(height: 8),
                  ],
                  Text(
                    'Category detected: ${selectedCategory.displayName} (${parsed.categorySource.label})',
                    style: const TextStyle(
                      fontSize: 12,
                      color: AppColors.textSecondary,
                    ),
                  ),
                  const SizedBox(height: 14),
                  TextField(
                    controller: amountController,
                    autofocus: true,
                    keyboardType:
                        const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(
                      labelText: 'Amount (₹)',
                      prefixText: '₹ ',
                      hintText: 'e.g. 500',
                    ),
                  ),
                ],
              ),
              actions: [
                TextButton(
                  onPressed: () {
                    Navigator.of(ctx).pop();
                    setState(() => _isProcessing = false);
                  },
                  child: const Text('Cancel'),
                ),
                ElevatedButton(
                  onPressed: () {
                    final enteredAmount =
                        double.tryParse(amountController.text.trim());
                    if (enteredAmount != null && enteredAmount > 0) {
                      Navigator.of(ctx).pop();
                      Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) => RecommendationScreen(
                            repository: widget.repository,
                            amount: enteredAmount,
                            category: selectedCategory,
                            categorySource: parsed.categorySource,
                            payeeName: parsed.payeeName,
                            payeeAddress: parsed.payeeAddress,
                            mcc: parsed.mcc,
                          ),
                        ),
                      ).then((_) {
                        if (mounted) setState(() => _isProcessing = false);
                      });
                    }
                  },
                  child: const Text('Route Payment'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  Future<void> _pickImageFromGallery() async {
    try {
      final picker = ImagePicker();
      final pickedFile = await picker.pickImage(source: ImageSource.gallery);
      if (pickedFile == null) return;

      final inputImage = mlkit.InputImage.fromFilePath(pickedFile.path);
      final barcodeScanner = mlkit.BarcodeScanner(
        formats: [mlkit.BarcodeFormat.qrCode],
      );

      final barcodes = await barcodeScanner.processImage(inputImage);
      await barcodeScanner.close();

      if (barcodes.isNotEmpty && barcodes.first.rawValue != null) {
        _handleQrCode(barcodes.first.rawValue!);
      } else {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('No QR code found in selected image.')),
          );
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error reading image: $e')),
        );
      }
    }
  }

  void _showQuickTestDialog() {
    showDialog(
      context: context,
      builder: (_) => QuickTestQrDialog(
        onSelected: (payload) => _handleQrCode(payload),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Scan & Route'),
        actions: [
          IconButton(
            tooltip: 'Quick Test QRs',
            icon: const Icon(Icons.flash_on, color: AppColors.warning),
            onPressed: _showQuickTestDialog,
          ),
          IconButton(
            tooltip: 'Toggle Flashlight',
            icon: Icon(
              _torchOn ? Icons.flash_on : Icons.flash_off,
              color: _torchOn ? AppColors.warning : AppColors.textSecondary,
            ),
            onPressed: () {
              _scannerController.toggleTorch();
              setState(() => _torchOn = !_torchOn);
            },
          ),
          IconButton(
            tooltip: 'Switch Camera',
            icon: const Icon(Icons.flip_camera_android),
            onPressed: () => _scannerController.switchCamera(),
          ),
        ],
      ),
      body: Stack(
        children: [
          // Mobile Scanner Camera View
          MobileScanner(
            controller: _scannerController,
            onDetect: (capture) {
              final barcodes = capture.barcodes;
              for (final barcode in barcodes) {
                if (barcode.rawValue != null) {
                  _handleQrCode(barcode.rawValue!);
                  break;
                }
              }
            },
          ),

          // Viewfinder Overlay
          Center(
            child: Container(
              width: 260,
              height: 260,
              decoration: BoxDecoration(
                border: Border.all(color: AppColors.primaryLight, width: 3),
                borderRadius: BorderRadius.circular(24),
                boxShadow: [
                  BoxShadow(
                    color: AppColors.primary.withOpacity(0.2),
                    blurRadius: 20,
                    spreadRadius: 4,
                  ),
                ],
              ),
            ),
          ),

          // Top instructions
          Positioned(
            top: 24,
            left: 20,
            right: 20,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
              decoration: BoxDecoration(
                color: Colors.black.withOpacity(0.7),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Text(
                'Align merchant QR code within the frame to optimize payment rewards',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 13,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ),

          // Bottom Action Bar
          Positioned(
            bottom: 30,
            left: 20,
            right: 20,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.surface.withOpacity(0.9),
                        foregroundColor: Colors.white,
                        side: const BorderSide(color: AppColors.border),
                      ),
                      onPressed: _pickImageFromGallery,
                      icon: const Icon(Icons.photo_library, size: 18),
                      label: const Text('Pick Image'),
                    ),
                    ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primaryDark,
                        foregroundColor: Colors.white,
                      ),
                      onPressed: _showQuickTestDialog,
                      icon: const Icon(Icons.science, size: 18),
                      label: const Text('Preset Test'),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                TextButton(
                  onPressed: () {
                    Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (_) => ManualEntryScreen(
                          repository: widget.repository,
                        ),
                      ),
                    );
                  },
                  child: const Text(
                    'No QR? Tap here for Manual Entry →',
                    style: TextStyle(
                      color: AppColors.primaryLight,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
          ),

          if (_isProcessing)
            Container(
              color: Colors.black54,
              child: const Center(
                child: CircularProgressIndicator(),
              ),
            ),
        ],
      ),
    );
  }
}
