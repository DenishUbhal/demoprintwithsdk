import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';

import 'package:flutter/services.dart';
import 'dart:async';

//import 'package:safeconnectionsapp/shared_preferences.dart';
//import 'package:vaishnav_vanik_matrimony/Common/Constants.dart';

class Splash extends StatefulWidget {
  @override
  _SplashState createState() => _SplashState();
}

class _SplashState extends State<Splash> {
  String MemberId = "";

  @override
  void initState() {
    //checkLoginStatus();
    // TODO: implement initState
    super.initState();
    Timer(Duration(seconds: 5), ()=> Navigator.pushReplacementNamed(context, '/Login'));
  }

  checkLoginStatus() async {
    /* SharedPreferences prefs = await SharedPreferences.getInstance();
    String StdId = prefs.getString(Session.LoginId);
    MemberId=StdId;
    if(StdId!=null && StdId!=""){
      Timer(Duration(seconds: 2), ()=> Navigator.pushReplacementNamed(context, '/Dashboard'));
    }else{
      Timer(Duration(seconds: 2), ()=> Navigator.pushReplacementNamed(context, '/Login'));
    }*/
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
        body: Container(
      child: Stack(
        children: <Widget>[
          Image.asset(
            'images/logini.png',
            height: MediaQuery.of(context).size.height,
            fit: BoxFit.cover,
          ),
          Container(
            width: MediaQuery.of(context).size.width,
            //color: Color.fromRGBO(255, 255, 255, 1),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Image.asset(
                  "images/logo.png",
                  width: 300.0,
                  height: 300.0,
                  fit: BoxFit.contain,
                ),
                SpinKitCubeGrid(
                  size: 20,
                  itemBuilder: (_, int index) {
                    return DecoratedBox(
                      decoration: BoxDecoration(
                        //color: index.isEven ? Colors.black : Colors.black,
                        color: Color.fromRGBO(128, 44, 44, 1),
                      ),
                    );
                  },
                )
              ],
            ),
          )
        ],
      ),
    ));
  }
}