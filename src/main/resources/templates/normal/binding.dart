import 'package:get/get.dart';
import 'logic.dart';
import 'provider.dart';
import 'repository.dart';

class @nameBinding implements Bindings {
  @override
  void dependencies() {
    Get.lazyPut<@nameInterfaceProvider>(() => @nameProvider());
    Get.lazyPut<@nameInterfaceRepository>(() => @nameRepository(provider: Get.find()));
    Get.lazyPut(() => @nameController(repository: Get.find()));
  }
}