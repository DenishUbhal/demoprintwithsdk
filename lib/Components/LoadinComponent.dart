import 'package:flutter/material.dart';

class LoadinComponent extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Container(
        color: Color.fromRGBO(0, 0, 0, 0),
        padding: EdgeInsets.all(10),
        child: CircularProgressIndicator(
            strokeWidth: 5,
            valueColor: AlwaysStoppedAnimation<Color>(
                Colors.black)),
      ),
    );
  }
}