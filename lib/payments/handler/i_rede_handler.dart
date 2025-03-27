abstract interface class IRedeHandler {
  void onError(String message);

  void onMessage(String message);

  void onChanged(String message);

  void onFinishedResponse(String message);

  void onAuthProgress(String message);
}
