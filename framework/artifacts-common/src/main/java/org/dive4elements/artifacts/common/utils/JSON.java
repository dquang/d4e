package org.dive4elements.artifacts.common.utils;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public final class JSON
{
    private JSON() {
    }

    private static final boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    public static final boolean isWhitespace(int c) {
        return c == ' '  || c == '\n' || c == '\r'
            || c == '\t' || c == '\f';
    }

    private static final void match(int c, int x) throws IOException {
        if (c != x) {
            throw new IOException(
                "Expecting '" + (char)c + "' found '" + (char)x + "'");
        }
    }

    private static final int eof(InputStream in)
    throws IOException
    {
        int c = in.read();
        if (c == -1) {
            throw new IOException("EOF unexpected.");
        }
        return c;
    }

    private static final int whitespace(InputStream in)
    throws IOException
    {
        int c;
        while (isWhitespace(c = eof(in)));
        return c;
    }

    private static final int parseHex(String hex) throws IOException {
        try {
            return Integer.parseInt(hex, 16);
        }
        catch (NumberFormatException nfe) {
            throw new IOException("'" + hex + "' is not a hex string.");
        }
    }

    public static final String jsonString(String string) {
        StringBuilder sb = new StringBuilder(string.length()+2);

        sb.append('"');

        for (int i = 0, N = string.length(); i < N; ++i) {
            char c = string.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\t': sb.append("\\t"); break;
                case '\r': sb.append("\\r"); break;
                case '\n': sb.append("\\n"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default: sb.append(c);
            }
        }

        sb.append('"');

        return sb.toString();
    }

    public static String toJSONString(Map<String, Object> map) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        write(pw, map);
        pw.flush();
        return sw.toString();
    }


    public static void write(PrintWriter out, Map<String, Object> map) {
        writeObject(out, map);
    }

    private static void writeValue(PrintWriter out, Object value) {
        if (value instanceof Map) {
            writeObject(out, (Map)value);
        }
        else if (value instanceof List) {
            writeList(out, (List)value);
        }
        else if (value instanceof Number) {
            out.print(value);
        }
        else if (value instanceof Boolean) {
            out.print(((Boolean)value) ? "true" : "false");
        }
        else if (value == null) {
            out.print("null");
        }
        else {
            out.print(jsonString(value.toString()));
        }
    }

    private static void writeObject(PrintWriter out, Map map) {

        out.print('{');
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            out.print(jsonString(entry.getKey().toString()));
            out.print(':');
            writeValue(out, entry.getValue());
            if (iter.hasNext()) {
                out.print(',');
            }
        }
        out.print('}');
    }

    private static void writeList(PrintWriter out, List list) {
        out.print('[');
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            writeValue(out, iter.next());
            if (iter.hasNext()) {
                out.print(',');
            }
        }
        out.print(']');
    }

    public static Map<String, Object> parse(String in)
    throws IOException
    {
        return parse(asInputStream(in));
    }

    private static InputStream asInputStream(String in) {
        byte [] bytes;
        try {
            bytes = in.getBytes(Charset.forName("US-ASCII"));
        }
        catch (UnsupportedCharsetException uce) {
            // Should not happen.
            bytes = in.getBytes();
        }
        return new ByteArrayInputStream(bytes);
    }

    public static Map<String, Object> parse(InputStream in)
    throws IOException
    {
        return parseObject(new PushbackInputStream(in, 1));
    }

    public static Map<String, Object> parse(PushbackInputStream in)
    throws IOException
    {
        return parseObject(in);
    }

    private static final String parseString(
        PushbackInputStream in
    )
    throws IOException
    {
        StringBuilder sb = new StringBuilder();

        int mode = 0;

        char [] hex = new char[4];

        match('"', eof(in));

        OUT: for (int c = eof(in);; c = eof(in)) {

            switch (mode) {
                case 0:
                    if (c == '"') {
                        break OUT;
                    }
                    if (c == '\\') {
                        mode = 1;
                    }
                    else {
                        sb.append((char)c);
                    }
                    break;
                case 1:
                    switch (c) {
                        case 'u':
                            mode = 2;
                            continue;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        default:
                            sb.append((char)c);
                    }
                    mode = 0;
                    break;
                case 2:
                    hex[0] = (char)c;
                    mode = 3;
                    break;
                case 3:
                    hex[1] = (char)c;
                    mode = 4;
                    break;
                case 4:
                    hex[2] = (char)c;
                    mode = 5;
                    break;
                case 5:
                    hex[3] = (char)c;
                    sb.append((char)parseHex(new String(hex)));
                    mode = 0;
                    break;
            }
        }
        return sb.toString();
    }

    private static final Boolean parseTrue(InputStream in)
    throws IOException
    {
        match('t', eof(in));
        match('r', eof(in));
        match('u', eof(in));
        match('e', eof(in));
        return Boolean.TRUE;
    }

    private static final Boolean parseFalse(InputStream in)
    throws IOException
    {
        match('f', eof(in));
        match('a', eof(in));
        match('l', eof(in));
        match('s', eof(in));
        match('e', eof(in));
        return Boolean.FALSE;
    }

    private static final Object parseNull(InputStream in)
    throws IOException
    {
        match('n', eof(in));
        match('u', eof(in));
        match('l', eof(in));
        match('l', eof(in));
        return null;
    }

    private static final Number parseNumber(PushbackInputStream in)
    throws IOException
    {
        StringBuilder sb = new StringBuilder();

        boolean isInteger = true;

        int c;
        OUT: for (;;) {
            switch (c = eof(in)) {
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                case '-': case '+':
                    sb.append((char)c);
                    break;
                case '.': case 'e': case 'E':
                    isInteger = false;
                    sb.append((char)c);
                    break;
                default:
                    in.unread(c);
                    break OUT;
            }
        }

        try {
            if (isInteger) {
                return sb.length() > 9
                    ? (Number)Long   .valueOf(sb.toString())
                    : (Number)Integer.valueOf(sb.toString());
            }
            return (Number)Double.valueOf(sb.toString());
        }
        catch (NumberFormatException nfe) {
            throw new IOException("Not a number '" + sb + "'");
        }
    }

    private static List<Object> parseList(PushbackInputStream in)
    throws IOException
    {
        List<Object> list = new ArrayList<Object>();
        match('[', whitespace(in));
        int c = whitespace(in);
        if (c == ']') {
            return list;
        }

        for (;; c = whitespace(in)) {
            Object value;
            in.unread(c);
            switch (c) {
                case '{':
                    value = parseObject(in);
                    break;
                case '[':
                    value = parseList(in);
                    break;
                case '"':
                    value = parseString(in);
                    break;
                case 't':
                    value = parseTrue(in);
                    break;
                case 'f':
                    value = parseFalse(in);
                    break;
                case 'n':
                    value = parseNull(in);
                    break;
                default:
                    value = parseNumber(in);
            }
            list.add(value);

            if ((c = whitespace(in)) == ']') break;
            match(',', c);
        }
        return list;
    }

    private static void parsePair(
        PushbackInputStream in,
        Map<String, Object> pairs
    )
    throws IOException
    {
        in.unread(whitespace(in));
        String string = parseString(in);
        match(':', whitespace(in));

        Object value;

        int c = whitespace(in);
        in.unread(c);
        switch (c) {
            case '{':
                value = parseObject(in);
                break;
            case '[':
                value = parseList(in);
                break;
            case '"':
                value = parseString(in);
                break;
            case 't':
                value = parseTrue(in);
                break;
            case 'f':
                value = parseFalse(in);
                break;
            case 'n':
                value = parseNull(in);
                break;
            default:
                value = parseNumber(in);
        }
        pairs.put(string, value);
    }

    private static Map<String, Object> parseObject(PushbackInputStream in)
    throws IOException
    {
        Map<String, Object> pairs = new LinkedHashMap<String, Object>();

        int c = whitespace(in);
        match('{', c);

        if ((c = whitespace(in)) == '}') {
            return pairs;
        }

        in.unread(c);

        for (;;) {
            parsePair(in, pairs);

            if ((c = whitespace(in)) == '}') {
                break;
            }

            if (c == '}') break;
            match(',', c);
        }

        return pairs;
    }
}
