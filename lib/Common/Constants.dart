import 'package:flutter/material.dart';

class APIURL {
  static const String API_URL = "http://crm.buyinbni.in/BuyInBNIAppService.asmx/";
}

class Session{
  //static const String Session_Login = "Login_Data";
  //static const String EmployeeId = "EmployeeId";
  static const String memberid = "memberid";
  static const String name = "name";
  static const String chapterid = "chapterid";
  static const String chapter = "chapter";

}

class MESSAGES {
  static const String INTERNET_ERROR = "No Internet Connection";
  static const String INTERNET_ERROR_RETRY =
      "No Internet Connection.\nPlease Retry";
}

class COLORS {
  // App Colors //
  static const Color DRAWER_BG_COLOR = Colors.lightGreen;
  static const Color APP_THEME_COLOR = Colors.green;
}