import 'dart:async';
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:demoprintwithsdk/Common/ClassList.dart';
import 'package:demoprintwithsdk/Common/Constants.dart';
import 'package:intl/intl.dart';

class Services {

  static Future<List<LoginClass>> checkLogin(String mobileno) async {
    String url =
        APIURL.API_URL + 'Member_login_by_Mobile?type=mobilelogin&Mobile_Number=$mobileno';
    print("checkLogin URL: " + url);
    try {
      final response = await http.get(url);
      if (response.statusCode == 200) {
        List<LoginClass> list = [];
        print("MemberLogin Response: " + response.body);

        final jsonResponse = json.decode(response.body);
        LoginClassData memberDataClass =
        new LoginClassData.fromJson(jsonResponse);

        if (memberDataClass.ERROR_STATUS == false)
          list = memberDataClass.Data;
        else
          list = [];

        return list;
      } else {
        throw Exception(MESSAGES.INTERNET_ERROR);
      }
    } catch (e) {
      print("Check Login Erorr : " + e.toString());
      throw Exception(e);
    }
  }

  static Future<SaveDataClass> UpdateProfile(data) async {
    String url = APIURL.API_URL + 'AddVisitorEntry';
    print("UpdateProfile URL: " + url);
    final response = await http.post(url, body: data);
    try {
      if (response.statusCode == 200) {
        SaveDataClass data;
        final jsonResponse = json.decode(response.body);
        SaveDataClass saveDataClass = new SaveDataClass.fromJson(jsonResponse);
        return saveDataClass;
        return data;
      } else {
        throw Exception(MESSAGES.INTERNET_ERROR);
      }
    } catch (e) {
      print("UpdateProfile Erorr : " + e.toString());
      throw Exception(MESSAGES.INTERNET_ERROR);
    }
  }

  static Future<List<VisitorListClass>> getVisitorList(String chapterid) async {

    var now = new DateTime.now();
    var formatter = new DateFormat('yyyy-MM-dd');
    String formatted = formatter.format(now);

    String url =
        APIURL.API_URL + 'GetVisitorList?type=visitorlist&chapterid=$chapterid&currentdate=$formatted';
    print("getVisitor URL: " + url);
    try {
      final response = await http.get(url);
      if (response.statusCode == 200) {
        List<VisitorListClass> list = [];
        print("MemberLogin Response: " + response.body);

        final jsonResponse = json.decode(response.body);
        VisitorListClassData memberDataClass =
        new VisitorListClassData.fromJson(jsonResponse);

        if (memberDataClass.ERROR_STATUS == false&& memberDataClass.Data.length>0)
          list = memberDataClass.Data;
        else
          list = null;
        return list;
      } else {
        throw Exception(MESSAGES.INTERNET_ERROR);
      }
    } catch (e) {
      print("Check Login Erorr : " + e.toString());
      throw Exception(e);
    }
  }


  /*String url =
      APIURL.API_URL + 'LoginClient?type=login&dat=$sendtype&username=$username&password=$password';*/
}