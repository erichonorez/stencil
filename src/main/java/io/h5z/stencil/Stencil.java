package io.h5z.stencil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class Stencil {
    public Stencil() {
        throw new IllegalAccessError();
    }

    private static <T1, T2> Tuple2<T1, T2> attr(T1 t1, T2 t2) {
        return new Tuple2<>(t1, t2);
    }

    public interface HtmlEl {}

    public static class Tag implements HtmlEl {
        private String name;
        private final Map<String, String> attributes;
        private final List<? extends HtmlEl> nodes;

        public Tag(String name, Map<String, String> attributes, List<? extends HtmlEl> nodes) {
            this.name = name;
            this.attributes = attributes;
            this.nodes = nodes;
        }

        public String name() { return this.name; }
        public Map<String, String> attributes() { return this.attributes; }
        public List<? extends HtmlEl> node() { return this.nodes;  }

        @Override
        public String toString() { 
            return new StringBuilder()
                .append("<")
                .append(this.name)
                .append(
                    this.attributes.entrySet()
                        .stream()
                        .map(kv -> 
                            null == kv.getValue() 
                                ? kv.getKey()
                                : String.format("%s=\"%s\"", kv.getKey(), kv.getValue()))
                        .reduce("", (a, b) -> String.format("%s %s", a, b))
                )   
                .append(">")
                .append(
                    this.nodes.stream()
                        .map(HtmlEl::toString)
                        .reduce("", (a, b) -> a + b))
                .append("</")
                .append(this.name)
                .append(">")
                .toString();
        }

        protected String renderContent() {
            return "";
        }

    }

    public static class Html extends Tag {

        private final String docType;

        public Html(String docType, Map<String, String> attributes, List<? extends HtmlEl> nodes) {
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

    public static HtmlEl html5(HtmlEl... elems) {
        return new Html("<!DOCTYPE html>", Collections.emptyMap(), Arrays.asList(elems));
    }

    public static HtmlEl html5(Map<String, String> attrs, HtmlEl... elems) {
        return new Html("<!DOCTYPE html>", attrs, Arrays.asList(elems));
    }

    public static class Head extends Tag {

        public Head(List<HtmlEl> elems) {
            super("head", Collections.emptyMap(), elems);
        }

    }

    public static HtmlEl head(HtmlEl... elems) {
        return new Head(Arrays.asList(elems));
    }

    public static class Meta extends Tag {

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
    public static HtmlEl meta(Entry<String, String>... attrs) {
        return new Meta(new Hashtable<>() {{
            Arrays.stream(attrs)
                .forEach(kv -> put(kv.getKey(), kv.getValue()));
        }});
    }

    public static class Title extends Tag {

        public Title(String content) {
            super("title", Collections.emptyMap(), Arrays.asList(new Text(content)));
        }

    }

    public static HtmlEl title(String title) {
        return new Title(title);
    }

    public static class Link extends Tag {

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
    public static HtmlEl link(Entry<String, String>... attrs) {
        return new Meta(new Hashtable<>() {{
            Arrays.stream(attrs)
                .forEach(kv -> put(kv.getKey(), kv.getValue()));
        }});
    }

    public static class Script extends Tag {

        public Script(Map<String, String> attributes, String content) {
            super("script", attributes, Arrays.asList(new UnsafeText(content)));
        }

    }

    public static HtmlEl script(Map<String, String> attributes, String content) {
        return new Script(attributes, content);
    }

    public static HtmlEl script(String content) {
        return script(Collections.emptyMap(), content);
    }

    public static HtmlEl script(Map<String, String> attributes) {
        return script(attributes, "");
    }

    public static class Body extends Tag {

        public Body(Map<String, String> attributes, List<? extends HtmlEl> nodes) {
            super("body", attributes, nodes);
        }

    }

    public static HtmlEl body(Map<String, String> attrs, HtmlEl... elems) {
        return new Body(attrs, Arrays.asList(elems));
    }

    public static HtmlEl body(HtmlEl... elems) {
        return body(Collections.emptyMap(), elems);
    }

    public static class H1 extends Tag {

        public H1(Map<String, String> attributes, List<? extends HtmlEl> nodes) {
            super("h1", attributes, nodes);
        }

    }

    public static HtmlEl h1(Map<String, String> attrs, HtmlEl... elems) {
        return new H1(attrs, Arrays.asList(elems));
    }

    public static HtmlEl h1(String content) {
        return h1(__(content));
    }

    public static HtmlEl h1(Map<String, String> attrs, String content) {
        return h1(attrs, __(content));    
    }

    public static HtmlEl h1(HtmlEl... elems) {
        return h1(Collections.emptyMap(), elems);
    }

    public static class H2 extends Tag {

        public H2(Map<String, String> attributes, List<? extends HtmlEl> nodes) {
            super("h2", attributes, nodes);
        }

    }

    public static HtmlEl h2(Map<String, String> attrs, HtmlEl... elems) {
        return new H2(attrs, Arrays.asList(elems));
    }

    public static HtmlEl h2(HtmlEl... elems) {
        return h2(Collections.emptyMap(), elems);
    }

    public static HtmlEl h2(String content) {
        return h2(__(content));
    }

    public static HtmlEl h2(Map<String, String> attrs, String content) {
        return h2(attrs, __(content));    
    }

    public static class H3 extends Tag {

        public H3(Map<String, String> attributes, List<? extends HtmlEl> nodes) {
            super("h3", attributes, nodes);
        }

    }

    public static HtmlEl h3(Map<String, String> attrs, HtmlEl... elems) {
        return new H3(attrs, Arrays.asList(elems));
    }

    public static HtmlEl h3(HtmlEl... elems) {
        return h3(Collections.emptyMap(), elems);
    }

    public static HtmlEl h3(String content) {
        return h3(__(content));
    }

    public static HtmlEl h3(Map<String, String> attrs, String content) {
        return h3(attrs, __(content));    
    }

    public static class H4 extends Tag {

        public H4(Map<String, String> attributes, List<? extends HtmlEl> nodes) {
            super("h4", attributes, nodes);
        }

    }

    public static HtmlEl h4(Map<String, String> attrs, HtmlEl... elems) {
        return new H4(attrs, Arrays.asList(elems));
    }

    public static HtmlEl h4(HtmlEl... elems) {
        return h4(Collections.emptyMap(), elems);
    }

    public static HtmlEl h4(String content) {
        return h4(__(content));
    }

    public static HtmlEl h4(Map<String, String> attrs, String content) {
        return h4(attrs, __(content));    
    }

    public static class H5 extends Tag {

        public H5(Map<String, String> attributes, List<? extends HtmlEl> nodes) {
            super("h5", attributes, nodes);
        }

    }

    public static HtmlEl h5(Map<String, String> attrs, HtmlEl... elems) {
        return new H5(attrs, Arrays.asList(elems));
    }

    public static HtmlEl h5(HtmlEl... elems) {
        return h5(Collections.emptyMap(), elems);
    }

    public static HtmlEl h5(String content) {
        return h5(__(content));
    }

    public static HtmlEl h5(Map<String, String> attrs, String content) {
        return h5(attrs, __(content));    
    }

    public static class H6 extends Tag {

        public H6(Map<String, String> attributes, List<? extends HtmlEl> nodes) {
            super("h6", attributes, nodes);
        }

    }

    public static HtmlEl h6(Map<String, String> attrs, HtmlEl... elems) {
        return new H6(attrs, Arrays.asList(elems));
    }

    public static HtmlEl h6(HtmlEl... elems) {
        return h6(Collections.emptyMap(), elems);
    }

    public static HtmlEl h6(String content) {
        return h6(__(content));
    }

    public static HtmlEl h6(Map<String, String> attrs, String content) {
        return h6(attrs, __(content));    
    }

    public static class P extends Tag {

        public P(Map<String, String> attributes, List<HtmlEl> nodes) {
            super("p", attributes, nodes);
        }

    }

    public static class Span extends Tag {

        public Span(Map<String, String> attributes, List<Text> nodes) {
            super("span", attributes, nodes);
        }

    }

    public static class Text implements HtmlEl {

        private final String content;

        public Text(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return escapeHTML(this.content);
        }

    }

    public static HtmlEl __(String content) {
        return new Text(content);
    }

    public static class UnsafeText implements HtmlEl {

        private final String content;

        public UnsafeText(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return this.content;
        }

    }

    public static HtmlEl __u(String content) {
        return new UnsafeText(content);
    }

    public static Tag div(HtmlEl... nodes) {
        return new Tag("div", Collections.emptyMap(), Arrays.asList(nodes));
    }

    public static Tag div(Map<String, String> attributes, HtmlEl... nodes) {
        return new Tag("div", attributes, Arrays.asList(nodes));
    }

    public static Tag p(String content) {
        return new P(Collections.emptyMap(), Arrays.asList(new Text(content)));
    }

    // ----------------------------------------------------------------------------------
    // Form elements
    // ----------------------------------------------------------------------------------

    /**
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/form">The form element</a>
     */
    public static class Form extends Tag {

        public Form(Map<String, String> attributes, List<? extends HtmlEl> nodes) {
            super("form", attributes, nodes);
        }

    }

    public static HtmlEl form(Map<String, String> attrs, List<HtmlEl> es) {
        return new Form(attrs, es); 
    }

    public static HtmlEl form(Map<String, String> attrs, HtmlEl... es) {
        return form(attrs, Arrays.asList(es));
    }

    public static HtmlEl form(List<HtmlEl> es) {
        return form(Collections.emptyMap(), es);
    }

    public static HtmlEl form(HtmlEl... es) {
        return form(Collections.emptyMap(), Arrays.asList(es));
    }

    /**
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">The Input element</a>
     */
    public static class Input extends Tag {

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

    public static HtmlEl input(Map<String, String> attrs) {
        return new Input(attrs);
    }

    /**
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/label">The Label element</a>
     */
    public static class Label extends Tag {

        public Label(Map<String, String> attributes, List<? extends HtmlEl> nodes) {
            super("label", attributes, nodes);
        }

    }

    public static HtmlEl label(Map<String, String> attrs, List<HtmlEl> es) {
        return new Label(attrs, es);
    }

    public static HtmlEl label(Map<String, String> attrs, HtmlEl... es) {
        return label(attrs, Arrays.asList(es));
    }

    public static HtmlEl label(Map<String, String> attrs, String content) {
        return label(attrs, Arrays.asList(__(content)));
    }

    public static HtmlEl label(List<HtmlEl> es) {
        return label(Collections.emptyMap(), es);
    }

    public static HtmlEl label(HtmlEl... es) {
        return label(Arrays.asList(es));
    }

    public static HtmlEl label(String content) {
        return label(Collections.emptyMap(), Arrays.asList(__(content)));
    }

    /**
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/button">The button element</a>
     */
    public static class Button extends Tag {

        public Button(Map<String, String> attributes, List<? extends HtmlEl> nodes) {
            super("button", attributes, nodes);
        }

    }

    public static HtmlEl button(Map<String, String> attrs, HtmlEl... es) {
        return form(attrs, Arrays.asList(es));
    }

    public static HtmlEl button(Map<String, String> attrs, List<HtmlEl> es) {
        return new Form(attrs, es);
    }

    public static HtmlEl button(HtmlEl... es) {
        return form(Collections.emptyMap(), Arrays.asList(es));
    }

    public static HtmlEl button(List<HtmlEl> es) {
        return form(Collections.emptyMap(), es);
    }

    public static HtmlEl button(Map<String, String> attrs, String content) {
        return form(attrs, Arrays.asList(__(content)));
    }

    public static HtmlEl button(String content) {
        return form(Collections.emptyMap(), Arrays.asList(__(content)));
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
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
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
                .reduce("", (a, b) -> String.format("%s `%s", a, b))); 
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
        return attr("name", method);
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
                h1(attrs(id("main-tite"), classes("h1")), "This is a title h1"),
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
                    button("Submit")),
                script(
                    attrs(
                        attr("src", "https://h5z.io/script.js"))))).toString();
    }
}
