@import hexlet.code.dto.UrlsPage

@param UrlsPage page

@template.layout.page(
page = page,
content = @`
    <div class="container mt-5">
        <h1 class="h2 mb-4">Сайты</h1>
        <table class="table table-bordered table-hover">
            <thead>
            <tr>
                <th>ID</th>
                <th>Имя</th>
                <th>Последняя проверка</th>
                <th>Код ответа</th>
            </tr>
            </thead>
            <tbody>
            @if(page.getUrls().isEmpty())
                <tr>
                    <td colspan="3" class="text-center">Пока нет добавленных страниц.</td>
                </tr>
            @else
            @for (var url : page.getUrls())
                <tr>
                    <td>${url.getId()}</td>
                    <td><a href="/urls/${url.getId()}">${url.getName()}</a></td>
                    <td></td>
                    <td></td>
                </tr>
            @endfor
            @endif
            </tbody>
        </table>
    </div>
    `)
