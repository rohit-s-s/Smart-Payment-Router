import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'data/payment_repository.dart';
import 'screens/cards_screen.dart';
import 'screens/history_screen.dart';
import 'screens/manual_entry_screen.dart';
import 'screens/offers_screen.dart';
import 'screens/scan_qr_screen.dart';
import 'theme/app_colors.dart';
import 'theme/app_theme.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Dark status bar and navigation bar styling
  SystemChrome.setSystemUIOverlayStyle(
    const SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      statusBarIconBrightness: Brightness.light,
      systemNavigationBarColor: AppColors.surface,
      systemNavigationBarIconBrightness: Brightness.light,
    ),
  );

  final repository = PaymentRepository();
  await repository.init();

  runApp(SmartPaymentRouterApp(repository: repository));
}

class SmartPaymentRouterApp extends StatelessWidget {
  final PaymentRepository repository;

  const SmartPaymentRouterApp({super.key, required this.repository});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Smart Payment Router',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.darkTheme,
      home: MainShell(repository: repository),
    );
  }
}

class MainShell extends StatefulWidget {
  final PaymentRepository repository;

  const MainShell({super.key, required this.repository});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  int _currentIndex = 0;

  late final List<Widget> _screens;

  @override
  void initState() {
    super.initState();
    _screens = [
      ScanQrScreen(repository: widget.repository),
      ManualEntryScreen(repository: widget.repository),
      CardsScreen(repository: widget.repository),
      OffersScreen(repository: widget.repository),
      HistoryScreen(repository: widget.repository),
    ];
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _currentIndex,
        children: _screens,
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (idx) => setState(() => _currentIndex = idx),
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.qr_code_scanner_outlined),
            selectedIcon: Icon(Icons.qr_code_scanner),
            label: 'Scan',
          ),
          NavigationDestination(
            icon: Icon(Icons.edit_note_outlined),
            selectedIcon: Icon(Icons.edit_note),
            label: 'Manual',
          ),
          NavigationDestination(
            icon: Icon(Icons.credit_card_outlined),
            selectedIcon: Icon(Icons.credit_card),
            label: 'Cards',
          ),
          NavigationDestination(
            icon: Icon(Icons.local_offer_outlined),
            selectedIcon: Icon(Icons.local_offer),
            label: 'Offers',
          ),
          NavigationDestination(
            icon: Icon(Icons.history_outlined),
            selectedIcon: Icon(Icons.history),
            label: 'History',
          ),
        ],
      ),
    );
  }
}
