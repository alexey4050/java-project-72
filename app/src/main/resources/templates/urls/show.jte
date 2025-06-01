@import hexlet.code.dto.UrlPage
@import hexlet.code.model.Url
@import java.time.format.DateTimeFormatter

@param UrlPage page
@param Url url

@template.layout.page(
page = page,
content = @`
    <div class="container mt-5">
        <h1 class="h1 mb-1">Сайт: ${url.getName()}</h1>
        <table class="table table-bordered">
            <tr>
                <th>ID</th>
                <td>${url.getCreatedAt()!= null ?
    url.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) :
    "N/A"}</td>
            </tr>
            <tr>
                <th>Имя</th>
                <td>${url.getName()}</td>
            </tr>
            <tr>
                <th>Дата создания</th>
                <td>${url.getId()}</td>
            </tr>
        </table>

        <h2 class="h2 mb-1">Проверки</h2>
        <button class="btn btn-primary mb-3" onclick="startCheck(${url.getId()})">Запустить проверку</button>

        <table class="table table-bordered">
            <thead>
            <tr>
                <th>ID</th>
                <th>Код ответа</th>
                <th>title</th>
                <th>description</th>
                <th>Дата проверки</th>
            </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>
`
)
