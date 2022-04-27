import 'package:get/get.dart';
import 'entity.dart';
import '../../common/utils/base_provider.dart';

abstract class @nameInterfaceProvider {
  Future<Response<@nameState>> getInfo();
}

class @nameProvider extends BaseProvider implements @nameInterfaceProvider {

  // @override
  // Future<Response<@nameState>> getInfo() => get("/news");
  @override
  Future<Response<@nameState>> getInfo() async {
    var response = await get("/news");
    var data = @nameState.fromJson(response.body);
    return Response(
      statusCode: response.statusCode,
      statusText: response.statusText,
      body: data,
    );
  }
}
