@import hexlet.code.dto.BasePage
@import hexlet.code.util.NamedRoutes
@param BasePage page = null

@template.layout.page(
page = page,
content = @`
    <div class="container mt-5 text-center">
        <h1 class="display-4">Анализатор страниц</h1>
        <p class="lead">Бесплатно проверяйте сайты на SEO пригодность</p>

        <form action="${NamedRoutes.urlsPath()}" method="post" class="d-flex justify-content-center mt-4">
            <div class="input-group" style="max-width: 600px;">
                <input type="text" name="url" class="form-control form-control-lg" placeholder="Ссылки" required>
                <button type="submit" class="btn btn-primary btn-lg">Проверить</button>
            </div>
        </form>
        <p class="mt-2 text-muted">Пример: https://www.example.com</p>
    </div>
`
)
