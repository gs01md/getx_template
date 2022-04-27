import 'package:get/get.dart';
import 'entity.dart';
import 'repository.dart';

class @nameLogic extends SuperController<@nameState> {
  @nameLogic({required this.repository});

  final @nameInterfaceRepository repository;

  @override
  void onInit() {
    super.onInit();

    //Loading, Success, Error handle with 1 line of code
    // append(() => repository.getNews);
  }

  // 拉取信息
  Future<void> getInfo() async {
    append(() => repository.getInfo);
  }

  @override
  void onReady() {
  //  print('The build method is done. '
  //      'Your controller is ready to call dialogs and snackbars');
    super.onReady();
  }

  @override
  void onClose() {
  //  print('onClose called');
    super.onClose();
  }

    @override
    void onDetached() {}

    @override
    void onInactive() {}

    @override
    void onPaused() {}

    @override
    void onResumed() {}

  // @override void didChangeMetrics() {super.didChangeMetrics();}
  // @override void didChangePlatformBrightness() {super.didChangePlatformBrightness();}
  // @override Future<bool> didPushRoute(String route) {return super.didPushRoute(route);}
  // @override Future<bool> didPopRoute() {return super.didPopRoute();}

}