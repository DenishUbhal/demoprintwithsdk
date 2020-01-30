import 'dart:io';
import 'package:flutter/material.dart';
import 'package:demoprintwithsdk/Common/Services.dart';
import 'package:demoprintwithsdk/Common/Constants.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Login extends StatefulWidget {
  @override
  _LoginState createState() => _LoginState();
}

class _LoginState extends State<Login> {

  var isLoading = false;
  TextEditingController edMobileNo=new TextEditingController();

  showMsg(String msg) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        // return object of type Dialog
        return AlertDialog(
          title: new Text("BNI"),
          content: new Text(msg),
          actions: <Widget>[
            // usually buttons at the bottom of the dialog
            new FlatButton(
              child: new Text("Close"),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }


  checkLogin() async{
    try {
      final result = await InternetAddress.lookup('google.com');
      if (result.isNotEmpty && result[0].rawAddress.isNotEmpty) {
        if(edMobileNo.text!=null && edMobileNo.text.length==10 && edMobileNo.text!=""){
          setState(() { isLoading = true; });
          Future res=Services.checkLogin(edMobileNo.text);
          res.then((data) async{
            SharedPreferences prefs = await SharedPreferences.getInstance();
            setState(() { isLoading = false; });
            if(data!=null && data.length>0){
              await prefs.setString(Session.memberid, data[0].memberid);
              await prefs.setString(Session.name, data[0].name);
              await prefs.setString(Session.chapterid, data[0].chapterid);
              await prefs.setString(Session.chapter, data[0].chapter);
              Navigator.pushReplacementNamed(context, "/registration");
            }else{
              showMsg("Invalid login details");
            }
          },onError: (e) {
            print("Error : on Login Call");
            showMsg("$e");
            setState(() { isLoading = false; });
          });
        }else{
          if(edMobileNo.text!=null && edMobileNo.text!=""){
            if(edMobileNo.text.length!=10){
              showInternetMsg("Please Enter Valid Mobile No");
            }
          }else
          showInternetMsg("Please Enter Mobile No.");
        }
      }
    } on SocketException catch (_) {
      showInternetMsg("No Internet Connection.");
    }
  }

  showInternetMsg(String msg) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        // return object of type Dialog
        return AlertDialog(
          title: new Text(msg),
          actions: <Widget>[
            // usually buttons at the bottom of the dialog
            new FlatButton(
              child: new Text("Okay"),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }



  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(20.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Padding(
                padding: const EdgeInsets.only(right: 30),
                child: Image.asset(
                  'images/logo.png',
                  height: 250,
                  width: 250,
                ),
              ),
              Container(
                padding: EdgeInsets.symmetric(horizontal: 0),
                decoration: BoxDecoration(
                    color: Color.fromRGBO(255, 255, 255, 0.5),
                    border: new Border.all(width: 1),
                    borderRadius: BorderRadius.all(
                        Radius.circular(0))),
                child: TextFormField(
                  controller: edMobileNo,
                  maxLength: 10,
                  decoration: InputDecoration(
                      border: InputBorder.none,
                      counterText: "",
                      prefixIcon: Icon(Icons.phone_android),
                      hintText: "Mobile No"),
                  keyboardType: TextInputType.number,
                  style: TextStyle(color: Colors.black),
                ),
                width: MediaQuery
                    .of(context)
                    .size
                    .width - 60,
              ),
              Padding(padding: EdgeInsets.only(top: 10)),
              new InkWell(
                onTap: (){
                  checkLogin();
                },
                child: setUpButtonChild(),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget setUpButtonChild() {
    if (isLoading == false) {
      return new Container(
        height: 45.0,
        width: MediaQuery.of(context).size.width - 60,
        decoration: new BoxDecoration(
          //color: Colors.red,
          border: new Border.all(color: Color.fromRGBO(128, 44, 44, 1), width: 2.0),
          borderRadius: new BorderRadius.circular(0.0),
        ),
        child: new Center(child: new Text('SIGN IN', style: new TextStyle(fontSize: 16.0, color: Colors.black),),),
      );
    } else {
      return CircularProgressIndicator(
        valueColor: AlwaysStoppedAnimation<Color>(Colors.black),
      );
    }
  }
}
