// ignore_for_file: public_member_api_docs, sort_constructors_first
import 'dart:convert';

class RedeAuthResponse {
  final String status;
  final String method;

  RedeAuthResponse({
    required this.status,
    required this.method,
  });

  RedeAuthResponse copyWith({
    String? status,
    String? method,
  }) {
    return RedeAuthResponse(
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

  factory RedeAuthResponse.fromMap(Map<String, dynamic> map) {
    return RedeAuthResponse(
      status: map['status'] as String,
      method: map['method'] as String,
    );
  }

  String toJson() => json.encode(toMap());

  factory RedeAuthResponse.fromJson(String source) => RedeAuthResponse.fromMap(json.decode(source) as Map<String, dynamic>);

  @override
  String toString() => 'RedeAuthResponse(status: $status, method: $method)';

  @override
  bool operator ==(covariant RedeAuthResponse other) {
    if (identical(this, other)) return true;

    return other.status == status && other.method == method;
  }

  @override
  int get hashCode => status.hashCode ^ method.hashCode;
}
