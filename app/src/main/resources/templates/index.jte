@import hexlet.code.dto.BasePage
@import hexlet.code.util.NamedRoutes
@param BasePage page = null

@template.layout.page(
page = page,
content = @`
    <div class="text-bg-dark p-4">
        <div class="container">
            <h1 class="mb-3 display-4">Анализатор страниц</h1>
            <p class="lead mb-4">Бесплатно проверяйте сайты на SEO пригодность</p>

            <form action="${NamedRoutes.urlsPath()}" method="post">
                <div class="d-flex align-items-center gap-3 mb-3" style="max-width: 1200px;">
                    <div class="form-floating flex-grow-1">
                        <input type="text" name="url" id="urlInput" class="form-control" placeholder="Ссылка">
                        <label for="urlInput">Ссылка</label>
                    </div>
                    <button class="btn btn-primary btn-lg px-5" type="submit">Проверить</button>
                </div>
            </form>
            <p class="mt-2">Пример: https://www.example.com</p>
        </div>
    </div>
`
)
