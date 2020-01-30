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

class VisitorList extends StatefulWidget {
  @override
  _VisitorListState createState() => _VisitorListState();
}

class _VisitorListState extends State<VisitorList> {

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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Visitor List'),
      ),
      body: FutureBuilder<List<VisitorListClass>>(
        future: Services.getVisitorList(UserData.chapterid),
        builder: (BuildContext context, AsyncSnapshot snapshot) {
          return snapshot.connectionState == ConnectionState.done
              ? snapshot.hasData
              ? ListView.builder(
            padding: EdgeInsets.all(0),
            //reverse: true,
            itemCount: snapshot.data.length,
            itemBuilder: (BuildContext context, int index) {
              return VisitorListComponents(snapshot.data[index],index);
            },
          )
              : NoDataComponent()
              : LoadinComponent();
        },
      ),
    );
  }
}
