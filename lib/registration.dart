import 'dart:io';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:demoprintwithsdk/Common/Constants.dart';
import 'package:flutter/material.dart';
import 'package:demoprintwithsdk/Common/Services.dart';
import 'Common/ClassList.dart';
import 'package:toast/toast.dart';
import 'package:demoprintwithsdk/Components/LoadinComponent.dart';
import 'package:demoprintwithsdk/Components/NoDataComponent.dart';
import 'package:intl/intl.dart';
import 'package:demoprintwithsdk/Components/VisitorListComponents.dart';

class registration extends StatefulWidget {
  @override
  _registrationState createState() => _registrationState();
}

class _registrationState extends State<registration> {

  static const String _channel = 'test_activity';
  static const String _channe2 = 'printScan';
  static const platform = const MethodChannel(_channel);
  static const platform2 = const MethodChannel(_channe2);

  TextEditingController edName=new TextEditingController();
  TextEditingController edMobile=new TextEditingController();
  TextEditingController edCmpName=new TextEditingController();
  TextEditingController edEmail=new TextEditingController();

  LoginClass UserData = new LoginClass(memberid: '0',name: '', chapterid:'',chapter:'');


  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    getLocalData();
  }

  getLocalData() async{
    SharedPreferences prefs = await SharedPreferences.getInstance();
    String memberid= prefs.getString(Session.memberid);
    String name= prefs.getString(Session.name);
    String chapterid= prefs.getString(Session.chapterid);
    String chapter= prefs.getString(Session.chapter);

    if (memberid != null) {
      setState(() {
        UserData = new LoginClass(memberid: memberid,name: name,chapterid: chapterid,chapter: chapter);
      });
    }
  }

  var isLoading = false;

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

  showMsg(String name,String categoryName,String inviteBy,String mobileno) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        // return object of type Dialog
        return AlertDialog(
          title: new Text("You want to print this."),
          //content: new Text(msg),
          actions: <Widget>[
            // usually buttons at the bottom of the dialog
            new FlatButton(
              child: new Text("Okay"),
              onPressed: () {
                List<String> name1 = name.split(" ");

                _printScanResult(name1[0],UserData.chapter,categoryName,inviteBy,mobileno);
                Navigator.of(context).pop();
              },
            ),

            new FlatButton(
              child: new Text("Cancel"),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  sendData() async{
    try {
      String name = edName.text.toString().trim();
      String mobile = edMobile.text.toString().trim();
      String cmpName = edCmpName.text.toString().trim();
      String email = edEmail.text.toString().trim();

      final result = await InternetAddress.lookup('google.com');
      if (result.isNotEmpty && result[0].rawAddress.isNotEmpty) {
        if(name!="" && name!=null){
          if(mobile!=null && mobile!="" && mobile.length==10){
            if(cmpName!="" && cmpName!=null){
              if(email!="" && email!=null){
                setState(() {
                  isLoading = true;
                });

                var data = {
                  'type': 'visitor',
                  'userid': UserData.memberid,
                  'Visitor': name,
                  'Company': cmpName,
                  'Mobile': mobile,
                  'Email': email,
                  'chapterid': UserData.chapterid,
                };
                print(data);

                Services.UpdateProfile(data).then((data) async {
                  if (data != null && data.ERROR_STATUS == false) {
                    Toast.show("Data Saved", context,
                        backgroundColor: Colors.green, gravity: Toast.TOP);
                    showMsg(edName.text,edCmpName.text,edEmail.text,edMobile.text);
                    //_printScanResult(name);
                    edName.clear();
                    edMobile.clear();
                    edCmpName.clear();
                    edEmail.clear();
                    setState(() {
                      isLoading = false;
                    });
                    //showMsg();
                    //Navigator.pushReplacementNamed(context, '/Dashboard');
                  } else {
                    Toast.show("Data Not Saved" + data.MESSAGE, context,
                        backgroundColor: Colors.red,
                        gravity: Toast.TOP,
                        duration: Toast.LENGTH_LONG);
                    setState(() {
                      isLoading = false;
                    });
                  }
                }, onError: (e) {
                  setState(() {
                    isLoading = false;
                  });
                  Toast.show("Data Not Saved" + e.toString(), context,
                      backgroundColor: Colors.red);
                });

              }else{
                showInternetMsg("Please Enter Invite By.");
              }
            }else{
              showInternetMsg("Please Enter Category Name.");
            }
          }else{
            if(mobile=="" || mobile==null){
              showInternetMsg("Please Enter Mobile No.");
            }else{
              showInternetMsg("Please Enter Valid Mobile No.");
            }
          }
        }else{
          showInternetMsg("Please Enter Name.");
        }
      }
    } on SocketException catch (_) {
      showInternetMsg("No Internet Connection.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Print Label'),
        actions: <Widget>[
          Padding(
            padding: const EdgeInsets.only(right: 10),
            child: GestureDetector(
                onTap: (){
                  _SelectPrinter();
                },
                child: Icon(Icons.local_printshop,color: Colors.white,size: 30)
            ),
          )
        ],
      ),
      floatingActionButton: FloatingActionButton(
        backgroundColor: Color.fromRGBO(128, 44, 44, 1),
        onPressed: (){
          Navigator.pushNamed(context, '/VisitorList');
        },
        child: Icon(Icons.view_list),
      ),
      body: Container(
        width: MediaQuery.of(context).size.width,
        child: Column(
          //mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Padding(padding: EdgeInsets.only(top: 20)),
            Container(
              padding: EdgeInsets.symmetric(horizontal: 0),
              decoration: BoxDecoration(
                  color: Color.fromRGBO(255, 255, 255, 0.5),
                  border: new Border.all(width: 1),
                  borderRadius: BorderRadius.all(
                      Radius.circular(0))),
              child: TextFormField(
                controller: edName,
                decoration: InputDecoration(
                    border: InputBorder.none,
                    prefixIcon: Icon(Icons.account_circle),
                    hintText: "Name"),
                keyboardType: TextInputType.text,
                style: TextStyle(color: Colors.black),
              ),
              width: MediaQuery
                  .of(context)
                  .size
                  .width - 60,
            ),
            Padding(padding: EdgeInsets.only(top: 10)),
            Container(
              padding: EdgeInsets.symmetric(horizontal: 0),
              decoration: BoxDecoration(
                  color: Color.fromRGBO(255, 255, 255, 0.5),
                  border: new Border.all(width: 1),
                  borderRadius: BorderRadius.all(
                      Radius.circular(0))),
              child: TextFormField(
                controller: edMobile,
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
            Container(
              padding: EdgeInsets.symmetric(horizontal: 0),
              decoration: BoxDecoration(
                  color: Color.fromRGBO(255, 255, 255, 0.5),
                  border: new Border.all(width: 1),
                  borderRadius: BorderRadius.all(
                      Radius.circular(0))),
              child: TextFormField(
                controller: edCmpName,
                decoration: InputDecoration(
                    border: InputBorder.none,
                    prefixIcon: Icon(Icons.supervised_user_circle),
                    hintText: "Whom to meet"),
                keyboardType: TextInputType.text,
                style: TextStyle(color: Colors.black),
              ),
              width: MediaQuery
                  .of(context)
                  .size
                  .width - 60,
            ),
            Padding(padding: EdgeInsets.only(top: 10)),
            Container(
              padding: EdgeInsets.symmetric(horizontal: 0),
              decoration: BoxDecoration(
                  color: Color.fromRGBO(255, 255, 255, 0.5),
                  border: new Border.all(width: 1),
                  borderRadius: BorderRadius.all(
                      Radius.circular(0))),
              child: TextFormField(
                controller: edEmail,
                decoration: InputDecoration(
                    border: InputBorder.none,
                    counterText: "",
                    prefixIcon: Icon(Icons.email),
                    hintText: "Purpose of meeting"),
                keyboardType: TextInputType.text,

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
                sendData();
                //_printScanResult("","","","","");
              },
              child: setUpButtonChild(),
            ),
          ],
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
          color: Colors.white,
          border: new Border.all(color: Color.fromRGBO(128, 44, 44, 1), width: 2.0),
          borderRadius: new BorderRadius.circular(0.0),
        ),
        child: new Center(child: new Text('Save', style: new TextStyle(fontSize: 16.0, color: Colors.black),),),
      );
    } else {
      return CircularProgressIndicator(
        valueColor: AlwaysStoppedAnimation<Color>(Colors.black),
      );
    }
  }

  _SelectPrinter() async {
    String response = "";
    try {
      final String result =  await platform.invokeMethod('startNewActivity');
    } on PlatformException catch (e) {
      print(e.message);
    }
  }

  _printScanResult(String username,String chapter,String CategoryName,String InviteBy,String mobileno) async {
    String response = "";
    try {
      final String result =  await platform2.invokeMethod('printName',{"text":"$username","chapter":"$chapter","categoryName":"$CategoryName","InviteBy":"$InviteBy","mobileno":"$mobileno"},);
    } on PlatformException catch (e) {
      print(e.message);
    }
  }
}