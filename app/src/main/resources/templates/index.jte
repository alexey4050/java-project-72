@param String title = "URL Shortener"

<!DOCTYPE html>
    <html lang="ru">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${title}</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.6/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-4Q6Gf2aSP4eDXB8Miphtr37CMZZQ5oXLH2yaXMJ2w8e2ZtHTl7GptT4jmndRuHDT" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.6/dist/js/bootstrap.bundle.min.js" integrity="sha384-j1CDi7MgGQ12Z7Qab0qlWQ/Qqz24Gc6BM0thvEMVjHnfYGF0rmFCozFSxQBxwHKO" crossorigin="anonymous"></script>
    </head>
    <body class="bg-light">
        <div class="container mt-5">
            <div class="row justify-content-center">
                <div class="col-md-8">
                    <div class="card shadow">
                        <div class="card-header bg-primary text-white">
                            <h1 class="h4 mb-0">${title}</h1>
                        </div>
                        <div class="card-body">
                            <form action="/urls" method="post" class="mb-4">
                                <div class="input-group">
                                    <input type="text" name="url" class="form-control" placeholder="Enter URL" required>
                                    <button type="submit" class="btn btn-primary">Shorten</button>
                                </div>
                            </form>

                            <div class="list-group">
                                <div class="list-group-item">
                                    <div class="d-flex justify-content-between">
                                        <a href="https://example.com" target="_blank">https://example.com</a>
                                        <span class="badge bg-secondary">2023-07-20</span>
                                    </div>
                                    <small class="text-muted">Short URL: <a href="/abc123">/abc123</a></small>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.6/dist/js/bootstrap.bundle.min.js" integrity="sha384-j1CDi7MgGQ12Z7Qab0qlWQ/Qqz24Gc6BM0thvEMVjHnfYGF0rmFCozFSxQBxwHKO" crossorigin="anonymous"></script>
    </body>
    </html>