import 'package:flutter_test/flutter_test.dart';
import 'package:smart_payment_router/models/parsed_upi.dart';
import 'package:smart_payment_router/models/saved_merchant.dart';
import 'package:smart_payment_router/models/spend_category.dart';
import 'package:smart_payment_router/services/upi_parser.dart';

void main() {
  group('UpiParser', () {
    const sampleSavedMerchants = [
      SavedMerchant(
        name: 'Swiggy',
        normalizedName: 'swiggy',
        vpa: 'swiggy@icici',
        category: SpendCategory.dining,
        mcc: '5812',
      ),
      SavedMerchant(
        name: 'HPCL Fuel Station',
        normalizedName: 'hpclfuelstation',
        vpa: 'hpcl@upi',
        category: SpendCategory.fuel,
        mcc: '5541',
      ),
    ];

    test('valid UPI QR code with amount and MCC', () {
      const qrString =
          'upi://pay?pa=restaurant@okaxis&pn=Green+Bistro&am=650.00&cu=INR&mc=5812&tn=Lunch+Table+4';
      final result =
          UpiParser.parse(qrString, savedMerchants: sampleSavedMerchants);

      expect(result.isValidUpi, isTrue);
      expect(result.payeeAddress, equals('restaurant@okaxis'));
      expect(result.payeeName, equals('Green Bistro'));
      expect(result.amount, equals(650.0));
      expect(result.mcc, equals('5812'));
      expect(result.suggestedCategory, equals(SpendCategory.dining));
      expect(result.categorySource, equals(CategorySource.mcc));
    });

    test('valid UPI QR code with missing amount prompts user', () {
      const qrString =
          'upi://pay?pa=retailer@upi&pn=City+Shop&cu=INR&mc=5411';
      final result =
          UpiParser.parse(qrString, savedMerchants: sampleSavedMerchants);

      expect(result.isValidUpi, isTrue);
      expect(result.payeeAddress, equals('retailer@upi'));
      expect(result.amount, isNull);
      expect(result.suggestedCategory, equals(SpendCategory.groceries));
      expect(result.categorySource, equals(CategorySource.mcc));
    });

    test('non-UPI website link parses domain and treats as non-UPI', () {
      const nonUpiUrl = 'https://www.croma.com/checkout/order-9912';
      final result =
          UpiParser.parse(nonUpiUrl, savedMerchants: sampleSavedMerchants);

      expect(result.isValidUpi, isFalse);
      expect(result.amount, isNull);
      expect(result.payeeName, equals('croma.com'));
    });

    test('merchant history memory priority over default', () {
      const qrWithoutMcc =
          'upi://pay?pa=swiggy@icici&pn=Swiggy+Delivery&am=450.00&cu=INR';
      final result =
          UpiParser.parse(qrWithoutMcc, savedMerchants: sampleSavedMerchants);

      expect(result.isValidUpi, isTrue);
      expect(result.suggestedCategory, equals(SpendCategory.dining));
      expect(result.categorySource, equals(CategorySource.merchantHistory));
    });

    test('MCC mappings for all fixed categories', () {
      // Fuel (5541)
      final fuelQR = UpiParser.parse('upi://pay?pa=petrol@paytm&am=1000&mc=5541');
      expect(fuelQR.suggestedCategory, equals(SpendCategory.fuel));

      // Groceries (5411)
      final groceryQR =
          UpiParser.parse('upi://pay?pa=mart@hdfc&am=500&mc=5411');
      expect(groceryQR.suggestedCategory, equals(SpendCategory.groceries));

      // Utilities (4900)
      final utilityQR =
          UpiParser.parse('upi://pay?pa=power@axis&am=2000&mc=4900');
      expect(utilityQR.suggestedCategory, equals(SpendCategory.utilities));

      // Entertainment (7832)
      final movieQR =
          UpiParser.parse('upi://pay?pa=cinemas@icici&am=800&mc=7832');
      expect(movieQR.suggestedCategory, equals(SpendCategory.entertainment));
    });

    test('buildUpiUri formats correctly', () {
      final uri = UpiParser.buildUpiUri(
        payeeAddress: 'merchant@upi',
        payeeName: 'Test Merchant',
        amount: 500.0,
        category: SpendCategory.dining,
        mcc: '5812',
      );

      expect(uri.scheme, equals('upi'));
      expect(uri.host, equals('pay'));
      expect(uri.queryParameters['pa'], equals('merchant@upi'));
      expect(uri.queryParameters['am'], equals('500.00'));
      expect(uri.queryParameters['mc'], equals('5812'));
    });
  });
}
