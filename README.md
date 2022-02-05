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

## Form elements

| Element       |  Implemented ?    |
|---------------|-------------------|
| `<form>`        |         ✅         |
| `<input>`       |         ✅         |
| `<button>`      |         ✅         |
| `<datalist>`    |                   |
| `<fieldset>`    |                   |
| `<label>`       |         ✅         |
| `<legend>`      |                   |
| `<meter>`       |                   |
| `<optgroup>`    |                   |
| `<option>`      |                   |
| `<output>`      |         ✅         |
| `<progress>`    |                   |
| `<select>`      |         ✅         |
| `<textarea>`    |         ✅         |

## Table elements

Example : 

```java
Element table = 

    table(
        thead(
            tr(
                th("First name"), 
                th("Last name"))),
        tbody(
            tr(
                td("John"), 
                td("Doe")),
            tr(
                td("Jane"), 
                td("Doe"))));

table.toString(); // <table><thead><tr><th>First name</th> ... </table>
```

| Element       |  Implemented ?    |
|---------------|-------------------|
| `<table>`        |         ✅         |
| `<thead>`       |         ✅         |
| `<tr>`      |         ✅         |
| `<th>`    |            ✅        |
| `<tbody>`    |         ✅           |
| `<td>`       |         ✅         |
| `<tfoot>`      |                   |
| `<caption>`       |                   |
| `<col>`    |                   |
| `<colgroup>`      |                   |

## Text elements

Example:

```java
Element div =
        
    div(
        p("this is a paragraph"),
        ul(
            li("list item"),
            li("list item")),
        ol(
            li("list item"),
            li("list item")),
        dl(
            dt("DSL"),
            dd("Domain Specific Language")));
        
div.toString(); // <div><p>this is a paragraph</p><ul><li>list item</li>... </div>
```

| Element       |  Implemented ?    |
|---------------|-------------------|
| `<div>`        |         ✅         |
| `<p>`       |         ✅         |
| `<ul>`      |         ✅         |
| `<ol>`    |            ✅        |
| `<li>`    |         ✅           |
| `<dl>`    |         ✅           |
| `<dt>`    |         ✅           |
| `<dd>`    |         ✅           |