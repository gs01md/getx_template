
/// 分页 response
class @nameState {
  int? counts;
  int? pagesize;
  int? pages;
  int? page;
  List<@nameStateItem>? items;

  @nameState({
    this.counts,
    this.pagesize,
    this.pages,
    this.page,
    this.items,
  });

  factory @nameState.fromJson(Map<String, dynamic> json) =>
      @nameState(
        counts: json["counts"],
        pagesize: json["pagesize"],
        pages: json["pages"],
        page: json["page"],
        items:
            List<@nameStateItem>.from(json["items"].map((x) => @nameStateItem.fromJson(x))),
      );

  Map<String, dynamic> toJson() => {
        "counts": counts,
        "pagesize": pagesize,
        "pages": pages,
        "page": page,
        "items": items != null
            ? List<dynamic>.from(items!.map((x) => x.toJson()))
            : [],
      };
}

class @nameStateItem {
  String id;
  String title;
  String category;
  String thumbnail;
  String author;
  DateTime addtime;
  String url;

  @nameStateItem ({
    required this.id,
    required this.title,
    required this.category,
    required this.thumbnail,
    required this.author,
    required this.addtime,
    required this.url,
  });

  factory @nameStateItem .fromJson(Map<String, dynamic> json) => @nameStateItem (
        id: json["id"],
        title: json["title"],
        category: json["category"],
        thumbnail: json["thumbnail"],
        author: json["author"],
        addtime: DateTime.parse(json["addtime"]),
        url: json["url"],
      );

  Map<String, dynamic> toJson() => {
        "id": id,
        "title": title,
        "category": category,
        "thumbnail": thumbnail,
        "author": author,
        "addtime": addtime.toIso8601String(),
        "url": url,
      };
}
