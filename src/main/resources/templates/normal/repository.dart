import 'entity.dart';
import 'provider.dart';

abstract class @nameInterfaceRepository {
  Future<@nameState> getInfo();
}

class @nameRepository implements @nameInterfaceRepository {
  @nameRepository({required this.provider});
  final @nameInterfaceProvider provider;

  @override
  Future<@nameState> getInfo() async {
    final response = await provider.getInfo();
    if (response.status.hasError) {
      return Future.error(response.statusText!);
    } else {
      return response.body!;
    }
  }
}

