package io.h5z.stencil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class DSL {
    
    private DSL() {
        throw new IllegalAccessError();
    }

    public static abstract class Element {

        public abstract <T> T accept(ElementVisitor<T> visitor);

        @Override
        public String toString() {
            return render(this);
        }

    }

    public static interface ElementVisitor<T> {
        public T visit(HTMLElement element);
        public T visit(DocType page);
        public T visit(Text text);
    }

    public static class HTMLElement extends Element {
        private String name;
        private final Map<String, String> attributes;
        private final List<? extends Element> nodes;
        private final boolean isVoidElement;

        public HTMLElement(String name, Map<String, String> attributes, List<? extends Element> nodes, boolean isVoidElement) {
            this.name = name;
            this.attributes = attributes;
            this.nodes = nodes;
            this.isVoidElement = isVoidElement;
        }

        public HTMLElement(String name, Map<String, String> attributes, List<? extends Element> nodes) {
            this.name = name;
            this.attributes = attributes;
            this.nodes = nodes;
            this.isVoidElement = false;
        }

        public String name() { return this.name; }
        public Map<String, String> attributes() { return this.attributes; }
        public List<? extends Element> nodes() { return this.nodes;  }
        public boolean isVoidElement() { return this.isVoidElement; }

        @Override
        public <T> T accept(ElementVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
            return result;
        }
    }

    public static class Text extends Element {

        private final String content;

        public Text(String content) {
            this.content = content;
        }

        public String content() {
            return this.content;
        }

        @Override
        public <T> T accept(ElementVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((content == null) ? 0 : content.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Text other = (Text) obj;
            if (content == null) {
                if (other.content != null)
                    return false;
            } else if (!content.equals(other.content))
                return false;
            return true;
        }

    }

    public static Element __(String content) {
        return new Text(escapeHTML(content));
    }

    public static Element __u(String content) {
        return new Text(content);
    }

    public enum DocTypeValue {
        HTML5("<!DOCTYPE html>");

        private final String value;

        private DocTypeValue(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    public static class DocType extends Element {

        private final DocTypeValue docType;

        public DocType(DocTypeValue docType) {
            this.docType = docType;
        }

        public DocTypeValue docType() {
            return this.docType;
        }

        @Override
        public <T> T accept(ElementVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((docType == null) ? 0 : docType.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DocType other = (DocType) obj;
            if (docType != other.docType)
                return false;
            return true;
        }

    }

    public static String render(List<Element> elements) {
        return elements.stream()
            .map(DSL::renderElement)
            .reduce("", (a, b) -> a + b);
    }

    public static String render(Element... elements) {
        return render(Arrays.asList(elements));
    }

    private static String renderElement(Element element) {
        return element.accept(new ElementVisitor<String>() {

            @Override
            public String visit(HTMLElement element) {
                return renderHTMLElement(element);
            }

            @Override
            public String visit(DocType page) {
                return page.docType().value();
            }

            @Override
            public String visit(Text text) {
                return text.content();
            }
        });
    }

    private static String renderHTMLElement(HTMLElement element) {
        StringBuilder elementBuilder = new StringBuilder()
                .append(
                    openingTag(
                        element.name(),
                        renderTagAttrs(element.attributes())));
        
        if (element.isVoidElement()) {
            return elementBuilder.toString();
        }

        return elementBuilder.append(
                    element.nodes().stream()
                        .map(e -> render(e))
                        .reduce("", (a, b) -> a + b))
                .append(closingTag(element.name()))
                .toString();
    }

    private static String openingTag(String name, String attrs) {
        return new StringBuilder()
                .append("<")
                .append(name)
                .append(" ")
                .append(attrs)
                .append(">")
                .toString();
    }

    private static String closingTag(String name) {
        return new StringBuilder()
            .append("</")
            .append(name)
            .append(">")
            .toString();
    }

    private static String renderTagAttrs(Map<String, String> attrs) {
        return attrs.entrySet()
            .stream()
            .map(kv -> 
                null == kv.getValue() 
                    ? kv.getKey()
                    : String.format("%s=\"%s\"", kv.getKey(), kv.getValue()))
            .reduce("", (a, b) -> String.format("%s %s", a, b))
            .trim();
    }

    public static Element html(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("html", attrs, es);
    }
    
    public static Element html(Map<String, String> attrs, Element... es) {
        return html(attrs, Arrays.asList(es));
    }
    
    public static Element html(List<Element> es) {
        return section(Collections.emptyMap(), es);
    }
    
    public static Element html(Element... es) {
        return html(Collections.emptyMap(), Arrays.asList(es));
    }
    
    public static List<Element> html5(Map<String, String> attrs, List<Element> es) {
        return Arrays.asList(
            new DocType(DocTypeValue.HTML5),
            html(attrs, es));
    }
    
    public static List<Element> html5(Map<String, String> attrs, Element... es) {
        return Arrays.asList(
            new DocType(DocTypeValue.HTML5),
            html(attrs, es));
    }
    
    public static List<Element> html5(List<Element> es) {
        return Arrays.asList(
            new DocType(DocTypeValue.HTML5),
            html(es));
    }
    
    public static List<Element> html5(Element... es) {
        return Arrays.asList(
            new DocType(DocTypeValue.HTML5),
            html(es));
    }

    public static Element head(List<Element> es) {
        return new HTMLElement("head", Collections.emptyMap(), es);
    }
    
    public static Element head(Element... es) {
        return head(Arrays.asList(es));
    }

    public static Element meta(Map<String, String> attrs) {
        return new HTMLElement("meta", attrs, Collections.emptyList(), true);
    }

    @SafeVarargs
    public static Element meta(Entry<String, String>... attrs) {
        return meta(new HashMap<String, String>() {{
            Arrays.stream(attrs)
                .forEach(kv -> put(kv.getKey(), kv.getValue()));
        }});
    }

    public static Element title(Element title) {
        return new HTMLElement("title", Collections.emptyMap(), Arrays.asList(title));
    }

    public static Element title(String title) {
        return new HTMLElement("title", Collections.emptyMap(), Arrays.asList(__(title)));
    }

    public static Element link(Map<String, String> attrs) {
        return new HTMLElement("link", attrs, Collections.emptyList(), true);
    }

    @SafeVarargs
    public static Element link(Entry<String, String>... attrs) {
        return link(new HashMap<String, String>() {{
            Arrays.stream(attrs)
                .forEach(kv -> put(kv.getKey(), kv.getValue()));
        }});
    }
    
    public static Element script(Map<String, String> attributes, String content) {
        return new HTMLElement("script", attributes, Arrays.asList(__u(content)));
    }

    public static Element script(String content) {
        return script(Collections.emptyMap(), content);
    }

    public static Element script(Map<String, String> attributes) {
        return script(attributes, "");
    }

    public static Element body(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("body", attrs, es);
    }
    
    public static Element body(Map<String, String> attrs, Element... es) {
        return body(attrs, Arrays.asList(es));
    }
    
    public static Element body(List<Element> es) {
        return body(Collections.emptyMap(), es);
    }
    
    public static Element body(Element... es) {
        return body(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element body(String idAndClasses, List<Element> elements) {
        return body(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element body(String idAndClasses, Element... elements) {
        return body(parseIdAndClasses(idAndClasses), elements);
    }

    // ----------------------------------------------------------------------------------
    // Content sectionning elements
    // ----------------------------------------------------------------------------------

    public static HTMLElement section(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("section", attrs, es);
    }
    
    public static Element section(Map<String, String> attrs, Element... es) {
        return section(attrs, Arrays.asList(es));
    }
    
    public static Element section(List<Element> es) {
        return section(Collections.emptyMap(), es);
    }
    
    public static Element section(Element... es) {
        return section(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element section(String idAndClasses, List<Element> elements) {
        return section(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element section(String idAndClasses, Element... elements) {
        return section(parseIdAndClasses(idAndClasses), elements);
    }

    public static HTMLElement aside(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("aside", attrs, es);
    }
    
    public static Element aside(Map<String, String> attrs, Element... es) {
        return aside(attrs, Arrays.asList(es));
    }
    
    public static Element aside(List<Element> es) {
        return aside(Collections.emptyMap(), es);
    }
    
    public static Element aside(Element... es) {
        return aside(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element aside(String idAndClasses, List<Element> elements) {
        return aside(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element aside(String idAndClasses, Element... elements) {
        return aside(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element footer(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("aside", attrs, es);
    }
    
    public static Element footer(Map<String, String> attrs, Element... es) {
        return footer(attrs, Arrays.asList(es));
    }
    
    public static Element footer(List<Element> es) {
        return footer(Collections.emptyMap(), es);
    }
    
    public static Element footer(Element... es) {
        return footer(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element footer(String idAndClasses, List<Element> elements) {
        return footer(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element footer(String idAndClasses, Element... elements) {
        return footer(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element header(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("header", attrs, es);
    }
    
    public static Element header(Map<String, String> attrs, Element... es) {
        return header(attrs, Arrays.asList(es));
    }
    
    public static Element header(List<Element> es) {
        return header(Collections.emptyMap(), es);
    }
    
    public static Element header(Element... es) {
        return header(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element header(String idAndClasses, List<Element> elements) {
        return header(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element header(String idAndClasses, Element... elements) {
        return header(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element main(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("main", attrs, es);
    }
    
    public static Element main(Map<String, String> attrs, Element... es) {
        return main(attrs, Arrays.asList(es));
    }
    
    public static Element main(List<Element> es) {
        return main(Collections.emptyMap(), es);
    }
    
    public static Element main(Element... es) {
        return main(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element main(String idAndClasses, List<Element> elements) {
        return main(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element main(String idAndClasses, Element... elements) {
        return main(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element nav(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("nav", attrs, es);
    }
    
    public static Element nav(Map<String, String> attrs, Element... es) {
        return nav(attrs, Arrays.asList(es));
    }
    
    public static Element nav(List<Element> es) {
        return nav(Collections.emptyMap(), es);
    }
    
    public static Element nav(Element... es) {
        return nav(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element nav(String idAndClasses, List<Element> elements) {
        return nav(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element nav(String idAndClasses, Element... elements) {
        return nav(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element article(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("article", attrs, es);
    }
    
    public static Element article(Map<String, String> attrs, Element... es) {
        return article(attrs, Arrays.asList(es));
    }
    
    public static Element article(List<Element> es) {
        return article(Collections.emptyMap(), es);
    }
    
    public static Element article(Element... es) {
        return article(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element article(String idAndClasses, List<Element> elements) {
        return article(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element article(String idAndClasses, Element... elements) {
        return article(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h1(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("h1", attrs, es);
    }

    public static Element h1(Map<String, String> attrs, Element... es) {
        return h1(attrs, Arrays.asList(es));
    }

    public static Element h1(List<Element> es) {
        return h1(Collections.emptyMap(), es);
    }

    public static Element h1(Element... es) {
        return h1(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element h1(Map<String, String> attrs, String content) {
        return h1(attrs, __(content));
    }

    public static Element h1(String content) {
        return h1(__(content));
    }

    public static Element h1(String idAndClasses, List<Element> elements) {
        return h1(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h1(String idAndClasses, Element... elements) {
        return h1(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h1(String idAndClasses, String content) {
        return h1(parseIdAndClasses(idAndClasses), content);
    }

    public static Element h2(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("h2", attrs, es);
    }

    public static Element h2(Map<String, String> attrs, Element... es) {
        return h2(attrs, Arrays.asList(es));
    }

    public static Element h2(List<Element> es) {
        return h2(Collections.emptyMap(), es);
    }

    public static Element h2(Element... es) {
        return h2(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element h2(Map<String, String> attrs, String content) {
        return h2(attrs, __(content));
    }

    public static Element h2(String content) {
        return h2(__(content));
    }

    public static Element h2(String idAndClasses, List<Element> elements) {
        return h2(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h2(String idAndClasses, Element... elements) {
        return h2(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h2(String idAndClasses, String content) {
        return h2(parseIdAndClasses(idAndClasses), content);
    }

    public static Element h3(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("h3", attrs, es);
    }

    public static Element h3(Map<String, String> attrs, Element... es) {
        return h3(attrs, Arrays.asList(es));
    }

    public static Element h3(List<Element> es) {
        return h3(Collections.emptyMap(), es);
    }

    public static Element h3(Element... es) {
        return h3(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element h3(Map<String, String> attrs, String content) {
        return h3(attrs, __(content));
    }

    public static Element h3(String content) {
        return h3(__(content));
    }

    public static Element h3(String idAndClasses, List<Element> elements) {
        return h3(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h3(String idAndClasses, Element... elements) {
        return h3(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h3(String idAndClasses, String content) {
        return h3(parseIdAndClasses(idAndClasses), content);
    }

    public static Element h4(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("h4", attrs, es);
    }
    
    public static Element h4(Map<String, String> attrs, Element... es) {
        return h4(attrs, Arrays.asList(es));
    }
    
    public static Element h4(List<Element> es) {
        return h4(Collections.emptyMap(), es);
    }
    
    public static Element h4(Element... es) {
        return h4(Collections.emptyMap(), Arrays.asList(es));
    }
    
    public static Element h4(Map<String, String> attrs, String content) {
        return h4(attrs, __(content));
    }
    
    public static Element h4(String content) {
        return h4(__(content));
    }

    public static Element h4(String idAndClasses, List<Element> elements) {
        return h4(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h4(String idAndClasses, Element... elements) {
        return h4(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h4(String idAndClasses, String content) {
        return h4(parseIdAndClasses(idAndClasses), content);
    }

    public static Element h5(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("h5", attrs, es);
    }
    
    public static Element h5(Map<String, String> attrs, Element... es) {
        return h5(attrs, Arrays.asList(es));
    }
    
    public static Element h5(List<Element> es) {
        return h5(Collections.emptyMap(), es);
    }
    
    public static Element h5(Element... es) {
        return h5(Collections.emptyMap(), Arrays.asList(es));
    }
    
    public static Element h5(Map<String, String> attrs, String content) {
        return h5(attrs, __(content));
    }
    
    public static Element h5(String content) {
        return h5(__(content));
    }

    public static Element h5(String idAndClasses, List<Element> elements) {
        return h5(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h5(String idAndClasses, Element... elements) {
        return h5(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h5(String idAndClasses, String content) {
        return h5(parseIdAndClasses(idAndClasses), content);
    }

    public static Element h6(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("h6", attrs, es);
    }
    
    public static Element h6(Map<String, String> attrs, Element... es) {
        return h6(attrs, Arrays.asList(es));
    }
    
    public static Element h6(List<Element> es) {
        return h6(Collections.emptyMap(), es);
    }
    
    public static Element h6(Element... es) {
        return h6(Collections.emptyMap(), Arrays.asList(es));
    }
    
    public static Element h6(Map<String, String> attrs, String content) {
        return h6(attrs, __(content));
    }
    
    public static Element h6(String content) {
        return h6(__(content));
    }

    public static Element h6(String idAndClasses, List<Element> elements) {
        return h6(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h6(String idAndClasses, Element... elements) {
        return h6(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element h6(String idAndClasses, String content) {
        return h6(parseIdAndClasses(idAndClasses), content);
    }

    // ----------------------------------------------------------------------------------
    // Text elements
    // ----------------------------------------------------------------------------------


    public static Element div(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("div", attrs, es);
    }

    public static Element div(Map<String, String> attrs, Element... es) {
        return div(attrs, Arrays.asList(es));
    }

    public static Element div(List<Element> es) {
        return div(Collections.emptyMap(), es);
    }

    public static Element div(Element... es) {
        return div(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element div(String idAndClasses, List<Element> elements) {
        return div(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element div(String idAndClasses, Element... elements) {
        return div(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element p(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("p", attrs, es);
    }

    public static Element p(Map<String, String> attrs, Element... es) {
        return p(attrs, Arrays.asList(es));
    }

    public static Element p(List<Element> es) {
        return p(Collections.emptyMap(), es);
    }

    public static Element p(Element... es) {
        return p(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element p(Map<String, String> attrs, String content) {
        return p(attrs, __(content));
    }

    public static Element p(String content) {
        return p(__(content));
    }

    public static Element p(String idAndClasses, List<Element> elements) {
        return p(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element p(String idAndClasses, Element... elements) {
        return p(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element ul(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("ul", attrs, es);
    }

    public static Element ul(Map<String, String> attrs, Element... es) {
        return ul(attrs, Arrays.asList(es));
    }

    public static Element ul(List<Element> es) {
        return ul(Collections.emptyMap(), es);
    }

    public static Element ul(Element... es) {
        return ul(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element ul(String idAndClasses, List<Element> elements) {
        return ul(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element ul(String idAndClasses, Element... elements) {
        return ul(parseIdAndClasses(idAndClasses), elements);
    }

    public static HTMLElement li(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("li", attrs, es);
    }

    public static Element li(Map<String, String> attrs, Element... es) {
        return li(attrs, Arrays.asList(es));
    }

    public static Element li(List<Element> es) {
        return li(Collections.emptyMap(), es);
    }

    public static Element li(Element... es) {
        return li(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element li(Map<String, String> attrs, String content) {
        return li(attrs, __(content));
    }

    public static Element li(String content) {
        return li(__(content));
    }

    public static Element li(String idAndClasses, List<Element> elements) {
        return li(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element li(String idAndClasses, Element... elements) {
        return li(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element li(String idAndClasses, String content) {
        return li(parseIdAndClasses(idAndClasses), content);
    }

    public static HTMLElement ol(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("ol", attrs, es);
    }

    public static Element ol(Map<String, String> attrs, Element... es) {
        return ol(attrs, Arrays.asList(es));
    }

    public static Element ol(List<Element> es) {
        return ol(Collections.emptyMap(), es);
    }

    public static Element ol(Element... es) {
        return ol(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element ol(String idAndClasses, List<Element> elements) {
        return ol(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element ol(String idAndClasses, Element... elements) {
        return ol(parseIdAndClasses(idAndClasses), elements);
    }

    public static HTMLElement dl(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("dl", attrs, es);
    }

    public static Element dl(Map<String, String> attrs, Element... es) {
        return dl(attrs, Arrays.asList(es));
    }

    public static Element dl(List<Element> es) {
        return dl(Collections.emptyMap(), es);
    }

    public static Element dl(Element... es) {
        return dl(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element dl(String idAndClasses, List<Element> elements) {
        return dl(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element dl(String idAndClasses, Element... elements) {
        return dl(parseIdAndClasses(idAndClasses), elements);
    }

    public static HTMLElement dt(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("dt", attrs, es);
    }

    public static Element dt(Map<String, String> attrs, Element... es) {
        return dt(attrs, Arrays.asList(es));
    }

    public static Element dt(List<Element> es) {
        return dt(Collections.emptyMap(), es);
    }

    public static Element dt(Element... es) {
        return dt(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element dt(Map<String, String> attrs, String content) {
        return dt(attrs, __(content));
    }

    public static Element dt(String content) {
        return dt(__(content));
    }

    public static Element dt(String idAndClasses, List<Element> elements) {
        return dt(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element dt(String idAndClasses, Element... elements) {
        return dt(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element dt(String idAndClasses, String content) {
        return dt(parseIdAndClasses(idAndClasses), content);
    }

    public static HTMLElement dd(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("dd", attrs, es);
    }

    public static Element dd(Map<String, String> attrs, Element... es) {
        return dd(attrs, Arrays.asList(es));
    }

    public static Element dd(List<Element> es) {
        return dd(Collections.emptyMap(), es);
    }

    public static Element dd(Element... es) {
        return dd(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element dd(Map<String, String> attrs, String content) {
        return dd(attrs, __(content));
    }

    public static Element dd(String content) {
        return dd(__(content));
    }

    public static Element dd(String idAndClasses, List<Element> elements) {
        return dd(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element dd(String idAndClasses, Element... elements) {
        return dd(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element dd(String idAndClasses, String content) {
        return dd(parseIdAndClasses(idAndClasses), content);
    }

    // ----------------------------------------------------------------------------------
    // Form elements
    // ----------------------------------------------------------------------------------

    public static Element form(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("form", attrs, es); 
    }

    public static Element form(Map<String, String> attrs, Element... es) {
        return form(attrs, Arrays.asList(es));
    }

    public static Element form(List<Element> es) {
        return form(Collections.emptyMap(), es);
    }

    public static Element form(Element... es) {
        return form(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element input(Map<String, String> attrs) {
        return new HTMLElement("input", attrs, Collections.emptyList(), true);
    }

    public static Element label(Map<String, String> attrs, String label, List<Element> es) {
        List<Element> xs = new ArrayList<>();
        xs.add(__(label));
        xs.addAll(es);
        return new HTMLElement("label", attrs, xs);
    }

    public static Element label(Map<String, String> attrs, String label, Element... es) {
        return label(attrs, label, Arrays.asList(es));
    }

    public static Element label(Map<String, String> attrs, String label) {
        return label(attrs, label, Collections.emptyList());
    }

    public static Element label(String label) {
        return label(Collections.emptyMap(), label, Collections.emptyList());
    }

    public static Element label(String label, List<Element> es) {
        return label(Collections.emptyMap(), label, es);
    }

    public static Element label(String label, Element... es) {
        return label(Collections.emptyMap(), label, Arrays.asList(es));
    }

    public static Element button(Map<String, String> attrs, Element... es) {
        return button(attrs, Arrays.asList(es));
    }

    public static Element button(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("button", attrs, es);
    }

    public static Element button(Element... es) {
        return button(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element button(List<Element> es) {
        return button(Collections.emptyMap(), es);
    }

    public static Element button(Map<String, String> attrs, String content) {
        return button(attrs, Arrays.asList(__(content)));
    }

    public static Element button(String content) {
        return button(Collections.emptyMap(), Arrays.asList(__(content)));
    }

    public static Element select(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("select", attrs, es);
    }

    public static Element select(Map<String, String> attrs, Element... es) {
        return select(attrs, Arrays.asList(es));
    }

    public static Element select(List<Element> es) {
        return select(Collections.emptyMap(), es);
    }

    public static Element select(Element... es) {
        return select(Collections.emptyMap(), Arrays.asList(es));
    }

    public static Element option(Map<String, String> attrs, String content) {
        return new HTMLElement("option", attrs, Arrays.asList(__(content)));
    }

    public static Element option(String content) {
        return option(Collections.emptyMap(), content);
    }

    public static Element textarea(Map<String, String> attrs, String content) {
        return new HTMLElement("textarea", attrs, Arrays.asList(__(content)));
    }

    public static Element textarea(String content) {
        return textarea(Collections.emptyMap(), content);
    }

    // ----------------------------------------------------------------------------------
    // Table elements
    // ----------------------------------------------------------------------------------

    public static Element table(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("table", attrs, es);
    }

    public static Element table(Map<String, String> attrs, Element... es) {
        return table(attrs, Arrays.asList(es));
    }

    public static Element table(List<Element> es) {
        return table(Collections.emptyMap(), es);
    }

    public static Element table(Element... es) {
        return table(Arrays.asList(es));
    }

    public static Element table(String idAndClasses, List<Element> elements) {
        return table(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element table(String idAndClasses, Element... elements) {
        return table(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element thead(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("thead", attrs, es);
    }

    public static Element thead(Map<String, String> attrs, Element... es) {
        return thead(attrs, Arrays.asList(es));
    }

    public static Element thead(List<Element> es) {
        return thead(Collections.emptyMap(), es);
    }

    public static Element thead(Element... es) {
        return thead(Arrays.asList(es));
    }

    public static Element thead(String idAndClasses, List<Element> elements) {
        return thead(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element thead(String idAndClasses, Element... elements) {
        return thead(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element tbody(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("tbody", attrs, es);
    }

    public static Element tbody(Map<String, String> attrs, Element... es) {
        return tbody(attrs, Arrays.asList(es));
    }

    public static Element tbody(List<Element> es) {
        return tbody(Collections.emptyMap(), es);
    }

    public static Element tbody(Element... es) {
        return tbody(Arrays.asList(es));
    }

    public static Element tbody(String idAndClasses, List<Element> elements) {
        return tbody(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element tbody(String idAndClasses, Element... elements) {
        return tbody(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element tr(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("tr", attrs, es);
    }

    public static Element tr(Map<String, String> attrs, Element... es) {
        return tr(attrs, Arrays.asList(es));
    }

    public static Element tr(List<Element> es) {
        return tr(Collections.emptyMap(), es);
    }

    public static Element tr(Element... es) {
        return tr(Arrays.asList(es));
    }

    public static Element tr(String idAndClasses, List<Element> elements) {
        return tr(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element tr(String idAndClasses, Element... elements) {
        return tr(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element th(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("th", attrs, es);
    }

    public static Element th(Map<String, String> attrs, Element... es) {
        return th(attrs, Arrays.asList(es));
    }

    public static Element th(List<Element> es) {
        return th(Collections.emptyMap(), es);
    }

    public static Element th(Element... es) {
        return th(Arrays.asList(es));
    }

    public static Element th(Map<String, String> attrs, String content) {
        return th(attrs, Arrays.asList(__(content)));
    }

    public static Element th(String content) {
        return th(Collections.emptyMap(), content);
    }

    public static Element th(String idAndClasses, List<Element> elements) {
        return th(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element th(String idAndClasses, Element... elements) {
        return th(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element th(String idAndClasses, String content) {
        return th(parseIdAndClasses(idAndClasses), content);
    }

    public static Element td(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("td", attrs, es);
    }

    public static Element td(Map<String, String> attrs, Element... es) {
        return td(attrs, Arrays.asList(es));
    }

    public static Element td(List<Element> es) {
        return td(Collections.emptyMap(), es);
    }

    public static Element td(Element... es) {
        return td(Collections.emptyMap(), es);
    }

    public static Element td(Map<String, String> attrs, String content) {
        return td(attrs, Arrays.asList(__(content)));
    }

    public static Element td(String content) {
        return td(Collections.emptyMap(), content);
    }

    public static Element td(String idAndClasses, List<Element> elements) {
        return td(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element td(String idAndClasses, Element... elements) {
        return td(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element td(String idAndClasses, String content) {
        return td(parseIdAndClasses(idAndClasses), content);
    }

    // ----------------------------------------------------------------------------------
    // Inline HTML elements
    // ----------------------------------------------------------------------------------


    public static HTMLElement span(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("span", attrs, es);
    }
    
    public static Element span(Map<String, String> attrs, Element... es) {
        return span(attrs, Arrays.asList(es));
    }
    
    public static Element span(List<Element> es) {
        return span(Collections.emptyMap(), es);
    }
    
    public static Element span(Element... es) {
        return span(Collections.emptyMap(), Arrays.asList(es));
    }
    
    public static Element span(Map<String, String> attrs, String content) {
        return span(attrs, __(content));
    }
    
    public static Element span(String content) {
        return span(__(content));
    }

    public static Element span(String idAndClasses, List<Element> elements) {
        return span(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element span(String idAndClasses, Element... elements) {
        return span(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element span(String idAndClasses, String content) {
        return span(parseIdAndClasses(idAndClasses), content);
    }

    public static HTMLElement a(Map<String, String> attrs, Element e) {
        return new HTMLElement("a", attrs, Arrays.asList(e));
    }
    
    public static Element a(Element e) {
        return a(Collections.emptyMap(), e);
    }
    
    public static Element a(Map<String, String> attrs, String content) {
        return a(attrs, __(content));
    }
    
    public static Element a(String content) {
        return a(__(content));
    }

    public static HTMLElement i(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("i", attrs, es);
    }
    
    public static Element i(Map<String, String> attrs, Element... es) {
        return i(attrs, Arrays.asList(es));
    }
    
    public static Element i(List<Element> es) {
        return i(Collections.emptyMap(), es);
    }
    
    public static Element i(Element... es) {
        return i(Collections.emptyMap(), Arrays.asList(es));
    }
    
    public static Element i(Map<String, String> attrs, String content) {
        return i(attrs, __(content));
    }
    
    public static Element i(String content) {
        return i(__(content));
    }

    public static Element i(String idAndClasses, List<Element> elements) {
        return i(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element i(String idAndClasses, Element... elements) {
        return i(parseIdAndClasses(idAndClasses), elements);
    }

    public static Element i(String idAndClasses, String content) {
        return i(parseIdAndClasses(idAndClasses), content);
    }

    public static Element br(String content) {
        return new HTMLElement("br", Collections.emptyMap(), Collections.emptyList(), true);
    }

    // ----------------------------------------------------------------------------------
    // Attribute static factories
    // ----------------------------------------------------------------------------------

    @SafeVarargs
    public static Map<String, String> attrs(Entry<String, String>... attrs) {
        return attrs(Arrays.asList(attrs));
    }

    public static Map<String, String> attrs(List<Entry<String, String>> attrs) {
        return attrs.stream()
            .collect(HashMap::new, (m,v)->m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }

    public static <T1, T2> Tuple2<T1, T2> attr(T1 t1, T2 t2) {
        return new Tuple2<>(t1, t2);
    }

    public static Entry<String, String> lang(String lang) {
        return attr("lang", lang);
    }

    public static Entry<String, String> charset(String charset) {
        return attr("charset", charset);
    }

    public static Entry<String, String> httpEquiv(String string) {
        return attr("http-equiv", string);
    }

    public static Entry<String, String> id(String id) {
        return attr("id", id);
    }

    public static Entry<String, String> classes(String... classes) {
        return classes(Arrays.asList(classes));
    }

    public static Entry<String, String> classes(List<String> classes) {
        return attr(
            "class",
            classes.stream() 
                .reduce("", (a, b) -> String.format("%s %s", a, b))
                .trim()); 
    }

    public static Entry<String, String> type(String type) {
        return attr("type", type);
    }

    public static Entry<String, String> name(String name) {
        return attr("name", name);
    }

    public static Entry<String, String> action(String action) {
        return attr("action", action);
    }

    public static Entry<String, String> method(String method) {
        return attr("method", method);
    }

    public static Entry<String, String> placeholder(String placeholder) {
        return attr("placeholder", placeholder);
    }

    public static Entry<String, String> required() {
        return attr("required", null);
    }

    public static Entry<String, String> value(String value) {
        return attr("value", value);
    }

    public static Entry<String, String> _for(String _for) {
        return attr("for", _for);
    }

    public static Entry<String, String> selected() {
        return attr("selected", null);
    }

    public static Entry<String, String> checked() {
        return attr("checked", null);
    }

    public static Entry<String, String> rows(int i) {
        return attr("rows", Integer.toString(i));
    }

    public static Entry<String, String> cols(int j) {
        return attr("cols", Integer.toString(j));
    }

    public static Entry<String, String> rel(String rel) {
        return attr("rel", rel);
    }

    public static Entry<String, String> href(String href) {
        return attr("href", href);
    }

    public static Entry<String, String> content(String content) {
        return attr("content", content);
    }

    public static Entry<String, String> shape(String string) {
        return attr("shape", string);
    }

    public static Entry<String, String> width(String string) {
        return attr("width", string);
    }

    public static Entry<String, String> height(String string) {
        return attr("height", string);
    }

    public static Entry<String, String> rowspan(String string) {
        return attr("rowspan", string);
    }

    public static Entry<String, String> colspan(String string) {
        return attr("colspan", string);
    }

    public static Entry<String, String> src(String string) {
        return attr("src", string);
    }

    public static Entry<String, String> async(String string) {
        return attr("async", string);
    }

    public static Entry<String, String> data(String data, String string) {
        return attr("data-" + data, string);
    }

    public static Entry<String, String> aria(String aria, String string) {
        return attr("aria-" + aria, string);
    }

    public static Entry<String, String> role(String string) {
        return attr("role", string);
    }

    public static Entry<String, String> alt(String string) {
        return attr("alt", string);
    }

    // ----------------------------------------------------------------------------------
    // Helper classes and static methods
    // ----------------------------------------------------------------------------------

    public static String escapeHTML(String str) {
        return str.codePoints().mapToObj(c -> c > 127 || "\"'<>&".indexOf(c) != -1 ?
                "&#" + c + ";" : new String(Character.toChars(c)))
           .collect(Collectors.joining());
    }

    public static Map<String, String> parseIdAndClasses(String idAndClasses) {
        return _parseIdAndClasses(
            new LinkedList<>(Arrays.asList(idAndClasses.split("\\."))), 
            new HashMap<String, String>() {{
                put("class", "");
                put("id", "");
            }});
    }

    private static Map<String, String> _parseIdAndClasses(LinkedList<String> parts, HashMap<String, String> acc) {
        if (parts.size() < 1) { return acc; }
        String current = parts.get(0);
        if (current.startsWith("#")) {
            acc.put("id", current.substring(1));
        } else {
            acc.put("class", acc.get("class") + " " + current);
        }
        parts.remove();
        return _parseIdAndClasses(parts, acc);
    }

    private static class Tuple2<T1, T2> implements Entry<T1, T2> {

        private final T1 _1;
        private final T2 _2;

        public Tuple2(T1 t1, T2 t2) {
            this._1 = t1;
            this._2 = t2;
        }

        @Override
        public T1 getKey() {
            return this._1;
        }

        @Override
        public T2 getValue() {
            return this._2;
        }

        @Override
        public T2 setValue(T2 value) {
            throw new UnsupportedOperationException();
        }

    }

    public static void main(String[] args) {
        String page = 

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
                    h1("#main-tite.big-title", "This is a title h1"),
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
                            "Login :",
                            input(
                                attrs(
                                    attr("type", "text"),
                                    attr("name", "login"),
                                    placeholder("toto@example.com"),
                                    required()))),
                        label(attrs(_for("password")), "password :"),
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

        System.out.println(page);

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

        Element sectioning =

            main(
                header(
                    h1("title 1"),
                    h2("title 2"),
                    h3("title 3"),
                    h4("title 4"),
                    h5("title 5"),
                    h6("title 6")),
                nav(
                    ol(
                        li("a"),
                        li("b"),
                        li("c"))),
                aside(
                    ul(
                        li("suggestion 1"),
                        li("suggestion 2"))),
                section(
                    article(
                        p("an article"))),
                footer(
                    p("a footer")));
        
        sectioning.toString();
                
    }
}
