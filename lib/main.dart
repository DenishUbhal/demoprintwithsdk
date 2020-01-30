import 'package:flutter/material.dart';
import 'package:demoprintwithsdk/printdata.dart';
import 'package:demoprintwithsdk/registration.dart';
import 'package:demoprintwithsdk/Login.dart';
import 'package:demoprintwithsdk/Splash.dart';
import 'package:demoprintwithsdk/VisitorList.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  static Map<int, Color> color = {
    50: Color.fromRGBO(128, 44, 44, .1),
    100: Color.fromRGBO(128, 44, 44, .2),
    200: Color.fromRGBO(128, 44, 44, .3),
    300: Color.fromRGBO(128, 44, 44, .4),
    400: Color.fromRGBO(128, 44, 44, .5),
    500: Color.fromRGBO(128, 44, 44, .6),
    600: Color.fromRGBO(128, 44, 44, .7),
    700: Color.fromRGBO(128, 44, 44, .8),
    800: Color.fromRGBO(128, 44, 44, .9),
    900: Color.fromRGBO(128, 44, 44, 1),
  };

  MaterialColor colorCustom = MaterialColor(0xFF802C2C, color);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Demo Print',
      initialRoute: '/registration',
      routes: {
        '/': (context) => Splash(),
        '/Login': (context) => Login(),
        '/registration': (context) => registration(),
        '/VisitorList': (context) => VisitorList(),
      },
      theme: ThemeData(
        primarySwatch: colorCustom,
      ),
    );
  }
}