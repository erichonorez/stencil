package io.h5z.stencil;

import static io.h5z.stencil.DSL.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Showcase {
    
    public static void main(String[] args) throws IOException {
        
        List<Element> page = 
            html5(
                head(
                    title("This is a showcase of Stencil"),
                    meta(charset("utf8")),
                    meta(name("viewport"), content("width=device-width, initial-scale=1")),
                    link(rel("stylesheet"), href("https://cdn.jsdelivr.net/npm/bulma@0.9.3/css/bulma.min.css"))),
                body(
                    section(
                        attrs(classes("section")),
                        div(
                            attrs(classes("container")),
                            h1(
                               attrs(classes("title")),
                               "Hello Stencil!"),
                            p(
                              attrs(classes("subtitle")),
                              "You are a super cool HTML DSL for java."),
                              table(
                                    attrs(classes("table")),
                                    thead(
                                        tr(
                                            th("Position"),
                                            th("Played"),
                                            th("Won"),
                                            th("Drawn"))),
                                    tbody(
                                        IntStream.range(1, 10).mapToObj(i -> {
                                            return tr(
                                                td("1"),
                                                td(a(attrs(href("https://en.wikipedia.org/wiki/Leicester_City_F.C."), attr("title", "Leicester City F.C.")), "Leicester City")),
                                                td("2"),
                                                td("3"));
                                        }).collect(Collectors.toList())))))));

        try(BufferedWriter writer = new BufferedWriter(new FileWriter("index.html"))) {
            writer.write(render(page));
        }

    }

}
