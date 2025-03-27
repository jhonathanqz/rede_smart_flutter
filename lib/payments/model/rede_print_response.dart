// ignore_for_file: public_member_api_docs, sort_constructors_first
import 'dart:convert';

class RedePrintResponse {
  final String status;
  final String method;

  RedePrintResponse({
    required this.status,
    required this.method,
  });

  RedePrintResponse copyWith({
    String? status,
    String? method,
  }) {
    return RedePrintResponse(
      status: status ?? this.status,
      method: method ?? this.method,
    );
  }

  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'status': status,
      'method': method,
    };
  }

  factory RedePrintResponse.fromMap(Map<String, dynamic> map) {
    return RedePrintResponse(
      status: map['status'] as String,
      method: map['method'] as String,
    );
  }

  String toJson() => json.encode(toMap());

  factory RedePrintResponse.fromJson(String source) => RedePrintResponse.fromMap(json.decode(source) as Map<String, dynamic>);

  @override
  String toString() => 'RedePrintResponse(status: $status, method: $method)';

  @override
  bool operator ==(covariant RedePrintResponse other) {
    if (identical(this, other)) return true;

    return other.status == status && other.method == method;
  }

  @override
  int get hashCode => status.hashCode ^ method.hashCode;
}
