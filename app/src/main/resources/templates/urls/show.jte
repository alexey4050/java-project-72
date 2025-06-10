@import hexlet.code.dto.UrlPage
@import hexlet.code.util.NamedRoutes
@import java.time.format.DateTimeFormatter

@param UrlPage page

@template.layout.page(
page = page,
content = @`
    <div class="container mt-4">
        <h1 class="h1 mb-1">Сайт: ${page.getUrl().getName()}</h1>
        <table class="table table-bordered">
            <tr>
                <th>ID</th>
                <td>${page.getUrl().getId()}</td>
            </tr>
            <tr>
                <th>Имя</th>
                <td>${page.getUrl().getName()}</td>
            </tr>
            <tr>
                <th>Дата создания</th>
                <td>${page.getUrl().getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}</td>
            </tr>
        </table>
        <h2 class="h2 mb-1">Проверки</h2>
        <form action="${NamedRoutes.urlChecksPath(page.getUrl().getId())}" method="post">
            <button class="btn btn-primary mb-3">Запустить проверку</button>
        </form>

        <table class="table table-bordered">
            <thead>
            <tr>
                <th>ID</th>
                <th>Код ответа</th>
                <th>Title</th>
                <th>H1</th>
                <th>Description</th>
                <th>Дата проверки</th>
            </tr>
            </thead>
            <tbody>
                @if(page.getUrlChecks().isEmpty())
                    <tr>
                        <td colspan="6" class="text-center">Пока нет проверок.</td>
                    </tr>
                @else
                    @for (var check : page.getUrlChecks())
                        <tr>
                            <td>${check.getId()}</td>
                            <td>${check.getStatusCode()}</td>
                            <td>${check.getTitle() != null ? check.getTitle() : "N/A"}</td>
                            <td>${check.getH1() != null ? check.getH1() : "N/A"}</td>
                            <td>@if(check.getDescription() != null && !check.getDescription().isEmpty())
                                ${check.getDescription().length() > 50 ?
                                            check.getDescription().substring(0, 50) + "..." :
                                            check.getDescription()}
                                @else
                                    N/A
                                @endif</td>
                            <td>${check.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}</td>
                        </tr>
                    @endfor
                @endif
            </tbody>
        </table>
    </div>
`
)
