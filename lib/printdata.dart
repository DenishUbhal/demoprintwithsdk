import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class printdata extends StatefulWidget {
  @override
  _printdataState createState() => _printdataState();
}

class _printdataState extends State<printdata> {
  static const String _channel = 'test_activity';
  static const String _channe2 = 'printScan';
  static const platform = const MethodChannel(_channel);
  static const platform2 = const MethodChannel(_channe2);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(''),
        actions: <Widget>[
          Padding(
            padding: const EdgeInsets.only(right: 10),
            child: GestureDetector(
                onTap: () {
                  _SelectPrinter();
                },
                child:
                    Icon(Icons.local_printshop, color: Colors.white, size: 30)),
          )
        ],
      ),
      body: Container(
        child: Column(
          children: <Widget>[
            GestureDetector(
              onTap: (){
                _printScanResult('Ubhal');
              },
              child: Center(
                  child: Text('Print Data')
              ),
            ),
          ],
        ),
      ),
    );
  }

  _SelectPrinter() async {
    String response = "";
    try {
      final String result = await platform.invokeMethod('startNewActivity');
      //_printScanResult("");
    } on PlatformException catch (e) {
      print(e.message);
    }
  }

  _printScanResult(String username) async {
    String response = "";
    try {
      final String result =  await platform2.invokeMethod('printName',{"text":"$username"});
    } on PlatformException catch (e) {
      print(e.message);
    }
  }


}
