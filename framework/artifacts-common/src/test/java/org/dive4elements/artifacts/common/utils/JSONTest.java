package org.dive4elements.artifacts.common.utils;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class JSONTest {
    @Test
    public void toJSONString() {
        final Map<String, Object> json = new HashMap<>();
        json.put("number", 0);
        json.put("boolean", true);
        json.put("string", "test");
        final Map<String, Object> object = new HashMap<>();
        object.put("test", "test");
        json.put("object", object);

        assertEquals("{"
            + "\"number\":0,"
            + "\"boolean\":true,"
            + "\"string\":\"test\","
            + "\"object\":{\"test\":\"test\"}"
            + "}",
            JSON.toJSONString(json));
    }

    @Test
    public void toNonASCIIJSONString() {
        final Map<String, Object> json = new HashMap<>();
        json.put("täst", "täst");

        assertEquals("{\"täst\":\"täst\"}", JSON.toJSONString(json));
    }
}
