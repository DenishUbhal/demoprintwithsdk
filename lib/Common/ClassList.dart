class LoginClassData {
  String MESSAGE;
  String ORIGINAL_ERROR;
  bool ERROR_STATUS;
  bool RECORDS;

  List<LoginClass> Data;

  LoginClassData({
    this.MESSAGE,
    this.ORIGINAL_ERROR,
    this.ERROR_STATUS,
    this.RECORDS,
    this.Data,
  });

  factory LoginClassData.fromJson(Map<String, dynamic> json) {
    return LoginClassData(
        MESSAGE: json['MESSAGE'] as String,
        ORIGINAL_ERROR: json['ORIGINAL_ERROR'] as String,
        ERROR_STATUS: json['ERROR_STATUS'] as bool,
        RECORDS: json['RECORDS'] as bool,
        Data: json['Data']
            .map<LoginClass>((json) => LoginClass.fromJson(json))
            .toList());
  }
}

class LoginClass {
  String memberid;
  String name;
  String chapterid;
  String chapter;

  LoginClass({
    this.memberid,
    this.name,
    this.chapterid,
    this.chapter,
  });

  factory LoginClass.fromJson(Map<String, dynamic> json) {
    return LoginClass(
      memberid: json["memberid"] as String,
      name: json['name'] as String,
      chapterid: json['chapterid'] as String,
      chapter: json['chapter'] as String,
    );
  }
}


class SaveDataClass {
  String MESSAGE;
  String ORIGINAL_ERROR;
  bool ERROR_STATUS;
  bool RECORDS;

  SaveDataClass(
      {this.MESSAGE, this.ORIGINAL_ERROR, this.ERROR_STATUS, this.RECORDS});

  factory SaveDataClass.fromJson(Map<String, dynamic> json) {
    return SaveDataClass(
        MESSAGE: json['MESSAGE'] as String,
        ORIGINAL_ERROR: json['ORIGINAL_ERROR'] as String,
        ERROR_STATUS: json['ERROR_STATUS'] as bool,
        RECORDS: json['RECORDS'] as bool);
  }
}



class VisitorListClassData {
  String MESSAGE;
  String ORIGINAL_ERROR;
  bool ERROR_STATUS;
  bool RECORDS;

  List<VisitorListClass> Data;

  VisitorListClassData({
    this.MESSAGE,
    this.ORIGINAL_ERROR,
    this.ERROR_STATUS,
    this.RECORDS,
    this.Data,
  });

  factory VisitorListClassData.fromJson(Map<String, dynamic> json) {
    return VisitorListClassData(
        MESSAGE: json['MESSAGE'] as String,
        ORIGINAL_ERROR: json['ORIGINAL_ERROR'] as String,
        ERROR_STATUS: json['ERROR_STATUS'] as bool,
        RECORDS: json['RECORDS'] as bool,
        Data: json['Data']
            .map<VisitorListClass>((json) => VisitorListClass.fromJson(json))
            .toList());
  }
}

class VisitorListClass {
  String id;
  String Name;
  String CompanyName;
  String Mobile;
  String Email;

  VisitorListClass({
    this.id,
    this.Name,
    this.CompanyName,
    this.Mobile,
    this.Email,
  });

  factory VisitorListClass.fromJson(Map<String, dynamic> json) {
    return VisitorListClass(
      id: json["id"] as String,
      Name: json['Name'] as String,
      CompanyName: json['CompanyName'] as String,
      Mobile: json['Mobile'] as String,
      Email: json['Email'] as String,
    );
  }
}