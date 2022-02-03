# Stencil

Example : 

```java

html5(
    head(
        meta(attr("charset", "utf8")),
        meta(attr("property", "og:image"),
            attr("content", "https://developer.mozilla.org/static/img/opengraph-logo.png")),
        title("hello, world"),
        link(attr("rel", "icon"),
            attr("href", "favicon.icon"),
            attr("type", "image/x-icon"))),
    body(
        h1(attrs(id("main-tite"), classes("big-title")), "This is a title h1"),
        h2("This is a title h2"),
        h3("This is a title h3"),
        h4("This is a title h4"),
        h5("This is a title h5"),
        h6("This is a title h6"),
        div(
            attrs(
                id("super"),
                classes("class", "my-class")),
            p("hello, world")),
        form(
            attrs(
                attr("method", "POST"),
                action("/authenticate")),
            label(
                __("Login :"),
                input(
                    attrs(
                        attr("type", "text"),
                        attr("name", "login"),
                        placeholder("toto@example.com"),
                        required()))),
            label(attrs(attr("for", "password : "))),
            input(
                attrs(
                    type("password"),
                    name("password"),
                    attr("required", null))),
            select(
                attrs(
                    name("role")),
                option(
                    attrs(
                        value("USER"),
                        selected()),
                        "User"),
                option(
                    attrs(
                        value("ADMIN")),
                        "Admin")),
            textarea(attrs(id("description"), name("description")),
                "this is a content"),
            button("Submit")),
        script(
            attrs(
                attr("src", "https://h5z.io/script.js"))))).toString();
```

| Element       |  Implemented ?    |
|---------------|-------------------|
| <form>        |         ✅         |
| <input>       |         ✅         |
| <button>      |         ✅         |
| <datalist>    |                   |
| <fieldset>    |                   |
| <label>       |         ✅         |
| <legend>      |                   |
| <meter>       |                   |
| <optgroup>    |                   |
| <option>      |                   |
| <output>      |         ✅         |
| <progress>    |                   |
| <select>      |         ✅         |
| <textarea>    |         ✅         |