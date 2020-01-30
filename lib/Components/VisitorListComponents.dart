import 'package:flutter/material.dart';
import 'package:demoprintwithsdk/Common/ClassList.dart';
import 'package:url_launcher/url_launcher.dart';

class VisitorListComponents extends StatefulWidget {
  VisitorListClass _visitorListClass;
  int index1;

  VisitorListComponents(this._visitorListClass,this.index1);

  @override
  _VisitorListComponentsState createState() => _VisitorListComponentsState();
}

class _VisitorListComponentsState extends State<VisitorListComponents> {
  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      //margin: EdgeInsets.all(0),
      child: Container(
        width: MediaQuery.of(context).size.width,
        padding: EdgeInsets.all(5),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: <Widget>[
            Container(
              width: MediaQuery.of(context).size.width - 60,
              //color: Colors.red,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Row(
                    children: <Widget>[
                      Icon(
                        Icons.account_circle,
                        size: 20,
                      ),
                      Text(
                        '  ${widget._visitorListClass.Name}',
                        style: TextStyle(
                            fontSize: 18,
                            color: Colors.black,
                            letterSpacing: 1),
                      ),
                    ],
                  ),
                  Row(
                    children: <Widget>[

                      Icon(
                        Icons.supervised_user_circle,
                        size: 20,
                      ),
                      Text(
                        '  ${widget._visitorListClass.CompanyName}',
                        style: TextStyle(
                            fontSize: 15,
                            color: Colors.black,
                            letterSpacing: 1),
                      ),
                    ],
                  ),
                  Row(
                    children: <Widget>[
                      Icon(
                        Icons.email,
                        size: 20,
                      ),
                      Text(
                        '  ${widget._visitorListClass.Email}',
                        style: TextStyle(
                            fontSize: 15,
                            color: Colors.black,
                            letterSpacing: 1),
                      ),
                    ],
                  )
                ],
              ),
            ),
            GestureDetector(
              onTap: (){
                _launchURL(widget._visitorListClass.Mobile);
              },
                child: Icon(
              Icons.phone_in_talk,
              color: Colors.green,
              size: 30,
            )),
          ],
        ),
      ),
    );
  }

  _launchURL(String mobile) async {
    String url = 'tel: $mobile';
    if (await canLaunch(url)) {
      await launch(url);
    } else {
      throw 'Could not launch $url';
    }
  }
}
