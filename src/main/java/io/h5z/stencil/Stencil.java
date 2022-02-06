package io.h5z.stencil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class Stencil {
    
    private Stencil() {
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
        public T visit(HTMLPage page);
        public T visit(Text text);
    }

    public static class HTMLElement extends Element {
        private String name;
        private final Map<String, String> attributes;
        private final List<? extends Element> nodes;

        public HTMLElement(String name, Map<String, String> attributes, List<? extends Element> nodes) {
            this.name = name;
            this.attributes = attributes;
            this.nodes = nodes;
        }

        public String name() { return this.name; }
        public Map<String, String> attributes() { return this.attributes; }
        public List<? extends Element> nodes() { return this.nodes;  }

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

    public enum DocType {
        HTML5("<!DOCTYPE html>");

        private final String value;

        private DocType(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    public static class HTMLPage extends Element {

        private final DocType docType;
        private final HTMLElement element;

        public HTMLPage(DocType docType, HTMLElement element) {
            this.docType = docType;
            this.element = element;
        }

        public DocType docType() {
            return this.docType;
        }

        public HTMLElement element() {
            return this.element;
        }

        @Override
        public <T> T accept(ElementVisitor<T> visitor) {
            return visitor.visit(this);
        }

    }


    public static String render(Element element) {
        return element.accept(new ElementVisitor<String>() {

            @Override
            public String visit(HTMLElement element) {
                return renderHTMLElement(element);
            }

            @Override
            public String visit(HTMLPage page) {
                return new StringBuilder(page.docType().value())
                    .append(renderHTMLElement(page.element()))
                    .toString();

            }

            @Override
            public String visit(Text text) {
                return text.content;
            }

        });
    }

    public static String renderHTMLElement(HTMLElement element) {
        StringBuilder elementBuilder = new StringBuilder()
                .append(
                    openingTag(
                        element.name(),
                        tagAttrs(element.attributes())));
        
        if (element.nodes().size() < 1) {
            switch (element.name()) {
                case "link":
                case "meta":
                case "input":
                    return elementBuilder.toString();
                default:
                    return elementBuilder.append(" />")
                        .toString();
            }
        } else {
            return elementBuilder.append(
                    element.nodes().stream()
                        .map(e -> render(e))
                        .reduce("", (a, b) -> a + b))
                .append(closingTag(element.name()))
                .toString();
        }
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

    private static String tagAttrs(Map<String, String> attrs) {
        return attrs.entrySet()
            .stream()
            .map(kv -> 
                null == kv.getValue() 
                    ? kv.getKey()
                    : String.format("%s=\"%s\"", kv.getKey(), kv.getValue()))
            .reduce("", (a, b) -> String.format("%s %s", a, b));
    }

    public static class Html extends HTMLElement {

        private final String docType;

        public Html(String docType, Map<String, String> attributes, List<? extends Element> nodes) {
            super("html", attributes, nodes);
            this.docType = docType;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                .append(this.docType)
                .append(super.toString())
                .toString();
        }

    }

    public static Element html5(Element... elems) {
        return new Html("<!DOCTYPE html>", Collections.emptyMap(), Arrays.asList(elems));
    }

    public static Element html5(Map<String, String> attrs, Element... elems) {
        return new Html("<!DOCTYPE html>", attrs, Arrays.asList(elems));
    }

    public static class Head extends HTMLElement {

        public Head(List<Element> elems) {
            super("head", Collections.emptyMap(), elems);
        }

    }

    public static Element head(Element... elems) {
        return new Head(Arrays.asList(elems));
    }

    public static class Meta extends HTMLElement {

        public Meta(Map<String, String> attributes) {
            super("meta", attributes, Collections.emptyList());
        }

        @Override
        public String toString() {
            return new StringBuilder()
                .append("<")
                .append(this.name())
                .append(
                    this.attributes().entrySet()
                        .stream()
                        .map(kv -> 
                            null == kv.getValue() 
                                ? kv.getKey()
                                : String.format("%s=\"%s\"", kv.getKey(), kv.getValue()))
                        .reduce("", (a, b) -> String.format("%s %s", a, b))
                )   
                .append(">")
                .toString();
        }

    }

    @SafeVarargs
    public static Element meta(Entry<String, String>... attrs) {
        return new Meta(new Hashtable() {{
            Arrays.stream(attrs)
                .forEach(kv -> put(kv.getKey(), kv.getValue()));
        }});
    }

    public static class Title extends HTMLElement {

        public Title(String content) {
            super("title", Collections.emptyMap(), Arrays.asList(new Text(content)));
        }

    }

    public static Element title(String title) {
        return new Title(title);
    }

    public static class Link extends HTMLElement {

        public Link(Map<String, String> attributes) {
            super("link", attributes, Collections.emptyList());
        }

        @Override
        public String toString() {
            return new StringBuilder()
                .append("<")
                .append(this.name())
                .append(
                    this.attributes().entrySet()
                        .stream()
                        .map(kv -> 
                            null == kv.getValue() 
                                ? kv.getKey()
                                : String.format("%s=\"%s\"", kv.getKey(), kv.getValue()))
                        .reduce("", (a, b) -> String.format("%s %s", a, b))
                )   
                .append(">")
                .toString();
        }

    }

    @SafeVarargs
    public static Element link(Entry<String, String>... attrs) {
        return new Meta(new Hashtable() {{
            Arrays.stream(attrs)
                .forEach(kv -> put(kv.getKey(), kv.getValue()));
        }});
    }

    public static class Script extends HTMLElement {

        public Script(Map<String, String> attributes, String content) {
            super("script", attributes, Arrays.asList(__u(content)));
        }

    }

    public static Element script(Map<String, String> attributes, String content) {
        return new Script(attributes, content);
    }

    public static Element script(String content) {
        return script(Collections.emptyMap(), content);
    }

    public static Element script(Map<String, String> attributes) {
        return script(attributes, "");
    }

    public static class Body extends HTMLElement {

        public Body(Map<String, String> attributes, List<? extends Element> nodes) {
            super("body", attributes, nodes);
        }

    }

    public static Element body(Map<String, String> attrs, Element... elems) {
        return new Body(attrs, Arrays.asList(elems));
    }

    public static Element body(Element... elems) {
        return body(Collections.emptyMap(), elems);
    }

    // ----------------------------------------------------------------------------------
    // Content sectionning elements
    // ----------------------------------------------------------------------------------

    public static HTMLElement section(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("section", Collections.emptyMap(), es);
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

    public static HTMLElement aside(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("aside", Collections.emptyMap(), es);
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

    public static HTMLElement footer(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("aside", Collections.emptyMap(), es);
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

    public static HTMLElement header(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("header", Collections.emptyMap(), es);
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

    public static HTMLElement main(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("main", Collections.emptyMap(), es);
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

    public static HTMLElement nav(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("nav", Collections.emptyMap(), es);
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

    public static HTMLElement article(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("article", Collections.emptyMap(), es);
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

    public static HTMLElement h1(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("h1", Collections.emptyMap(), es);
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

    public static HTMLElement h2(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("h2", Collections.emptyMap(), es);
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

    public static HTMLElement h3(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("h3", Collections.emptyMap(), es);
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

    public static HTMLElement h4(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("h4", Collections.emptyMap(), es);
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

    public static HTMLElement h5(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("h5", Collections.emptyMap(), es);
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

    public static HTMLElement h6(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("h6", Collections.emptyMap(), es);
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

    // ----------------------------------------------------------------------------------
    // Text elements
    // ----------------------------------------------------------------------------------


    public static HTMLElement div(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("div", Collections.emptyMap(), es);
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

    public static HTMLElement p(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("p", Collections.emptyMap(), es);
    }

    public static HTMLElement p(Map<String, String> attrs, Element... es) {
        return p(attrs, Arrays.asList(es));
    }

    public static HTMLElement p(List<Element> es) {
        return p(Collections.emptyMap(), es);
    }

    public static HTMLElement p(Element... es) {
        return p(Collections.emptyMap(), Arrays.asList(es));
    }

    public static HTMLElement p(Map<String, String> attrs, String content) {
        return p(attrs, __(content));
    }

    public static HTMLElement p(String content) {
        return p(__(content));
    }

    public static HTMLElement ul(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("ul", Collections.emptyMap(), es);
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

    public static HTMLElement li(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("li", Collections.emptyMap(), es);
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

    public static HTMLElement ol(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("ol", Collections.emptyMap(), es);
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

    public static HTMLElement dl(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("dl", Collections.emptyMap(), es);
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

    public static HTMLElement dt(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("dt", Collections.emptyMap(), es);
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

    public static HTMLElement dd(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("dd", Collections.emptyMap(), es);
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

    // ----------------------------------------------------------------------------------
    // Form elements
    // ----------------------------------------------------------------------------------

    /**
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/form">The form element</a>
     */
    public static class Form extends HTMLElement {

        public Form(Map<String, String> attributes, List<? extends Element> nodes) {
            super("form", attributes, nodes);
        }

    }

    public static Element form(Map<String, String> attrs, List<Element> es) {
        return new Form(attrs, es); 
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

    /**
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">The Input element</a>
     */
    public static class Input extends HTMLElement {

        public Input(Map<String, String> attributes) {
            super("input", attributes, Collections.emptyList());
        }

        @Override
        public String toString() {
            return new StringBuilder()
                .append("<")
                .append(this.name())
                .append(
                    this.attributes().entrySet()
                        .stream()
                        .map(kv -> 
                            null == kv.getValue() 
                                ? kv.getKey()
                                : String.format("%s=\"%s\"", kv.getKey(), kv.getValue()))
                        .reduce("", (a, b) -> String.format("%s %s", a, b))
                )   
                .append(" />")
                .toString();
        }

    }

    public static Element input(Map<String, String> attrs) {
        return new Input(attrs);
    }

    /**
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/label">The Label element</a>
     */
    public static class Label extends HTMLElement {

        public Label(Map<String, String> attributes, List<? extends Element> nodes) {
            super("label", attributes, nodes);
        }

    }

    public static Element label(Map<String, String> attrs, String label, List<Element> es) {
        List<Element> xs = new ArrayList<>();
        xs.add(__(label));
        xs.addAll(es);
        return new Label(attrs, xs);
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

    /**
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/button">The button element</a>
     */
    public static class Button extends HTMLElement {

        public Button(Map<String, String> attributes, List<? extends Element> nodes) {
            super("button", attributes, nodes);
        }

    }

    public static Element button(Map<String, String> attrs, Element... es) {
        return button(attrs, Arrays.asList(es));
    }

    public static Element button(Map<String, String> attrs, List<Element> es) {
        return new Button(attrs, es);
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

    /**
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/select">The HTML Select element</a>
     */
    public static class Select extends HTMLElement {

        public Select(Map<String, String> attributes, List<? extends Element> nodes) {
            super("select", attributes, nodes);
        }

    }

    public static Element select(Map<String, String> attrs, List<Element> es) {
        return new Select(attrs, es);
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

    /**
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/option">The HTML Option element</a>
     */
    public static class Option extends HTMLElement {

        public Option(Map<String, String> attributes, List<? extends Element> nodes) {
            super("option", attributes, nodes);
        }

    }

    public static Element option(Map<String, String> attrs, String content) {
        return new Option(attrs, Arrays.asList(__(content)));
    }

    public static Element option(String content) {
        return option(Collections.emptyMap(), content);
    }

    /**
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/textarea">The Textarea element</a>
     */
    public static class Textarea extends HTMLElement {

        public Textarea(Map<String, String> attributes, List<? extends Element> nodes) {
            super("textarea", attributes, nodes);
        }

    }

    public static Element textarea(Map<String, String> attrs, String content) {
        return new Textarea(attrs, Arrays.asList(__(content)));
    }

    public static Element textarea(String content) {
        return textarea(Collections.emptyMap(), content);
    }

    // ----------------------------------------------------------------------------------
    // Table elements
    // ----------------------------------------------------------------------------------

    /**
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/table">The Table element</a>
     */
    public static class Table extends HTMLElement {

        public Table(Map<String, String> attributes, List<? extends Element> nodes) {
            super("table", attributes, nodes);
        }

    }

    public static Element table(Map<String, String> attrs, List<Element> es) {
        return new Table(attrs, es);
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

    /**
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/thead">The Table Head element</a>
     */
    public static class THead extends HTMLElement {

        public THead(Map<String, String> attributes, List<? extends Element> nodes) {
            super("thead", attributes, nodes);
        }

    }

    public static Element thead(Map<String, String> attrs, List<Element> es) {
        return new THead(attrs, es);
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

    /**
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/tbody">The Table Body element</a>
     */
    public static class TBody extends HTMLElement {

        public TBody(Map<String, String> attributes, List<? extends Element> nodes) {
            super("tbody", attributes, nodes);
        }

    }

    public static Element tbody(Map<String, String> attrs, List<Element> es) {
        return new TBody(attrs, es);
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


    /**
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/tr">The Table Row element</a>
     */
    public static class TR extends HTMLElement {

        public TR(Map<String, String> attributes, List<? extends Element> nodes) {
            super("tr", attributes, nodes);
        }

    }

    public static Element tr(Map<String, String> attrs, List<Element> es) {
        return new TR(attrs, es);
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

    /**
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/th">The Table Header element</a>
     */
    public static class TH extends HTMLElement {

        public TH(Map<String, String> attributes, List<? extends Element> nodes) {
            super("th", attributes, nodes);
        }

    }

    public static Element th(Map<String, String> attrs, List<Element> es) {
        return new TH(attrs, es);
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

    /**
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/td">The Table Data Cell element</a>
     */
    public static class TD extends HTMLElement {

        public TD(Map<String, String> attributes, List<? extends Element> nodes) {
            super("td", attributes, nodes);
        }

    }

    public static Element td(Map<String, String> attrs, List<Element> es) {
        return new TD(attrs, es);
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

    // ----------------------------------------------------------------------------------
    // Inline HTML elements
    // ----------------------------------------------------------------------------------


    public static HTMLElement span(Map<String, String> attrs, List<Element> es) {
        return new HTMLElement("span", Collections.emptyMap(), es);
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

    public static HTMLElement a(Map<String, String> attrs, Element e) {
        return new HTMLElement("a", Collections.emptyMap(), Arrays.asList(e));
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
        return new HTMLElement("i", Collections.emptyMap(), es);
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
        return span(attrs, __(content));
    }
    
    public static Element i(String content) {
        return span(__(content));
    }

    public static Element br(String content) {
        return new HTMLElement("br", Collections.emptyMap(), Collections.emptyList());
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
                .reduce("", (a, b) -> String.format("%s %s", a, b))); 
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

    public static Entry<String, String> rows(int i) {
        return attr("rows", Integer.toString(i));
    }

    public static Entry<String, String> cols(int j) {
        return attr("cols", Integer.toString(j));
    }

    // ----------------------------------------------------------------------------------
    // Helper classes and static methods
    // ----------------------------------------------------------------------------------

    public static String escapeHTML(String str) {
        return str.codePoints().mapToObj(c -> c > 127 || "\"'<>&".indexOf(c) != -1 ?
                "&#" + c + ";" : new String(Character.toChars(c)))
           .collect(Collectors.joining());
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
                            "Login :",
                            input(
                                attrs(
                                    attr("type", "text"),
                                    attr("name", "login"),
                                    placeholder("toto@example.com"),
                                    required()))),
                        label(attrs(attr("for", "password")), "password :"),
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
        // System.out.println(page);
        System.out.println(button(attrs(type("submit")), "Submit").toString());

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
