/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts.common.utils;

import java.io.UnsupportedEncodingException;

import java.util.UUID;

import org.apache.commons.codec.DecoderException;

import org.apache.commons.codec.binary.Hex;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Commonly used string functions.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public final class StringUtils
{
    private static Logger logger = LogManager.getLogger(StringUtils.class);

    /**
     * Generated a random UUIDv4 in form of a string.
     * @return the UUID
     */
    public static final String newUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Checks if a given string is a valid UUID.
     * @param uuid The string to test.
     * @return true if the string is a valid UUID else false.
     */
    public static final boolean checkUUID(String uuid) {
        try {
            UUID.fromString(uuid);
        }
        catch (IllegalArgumentException iae) {
            logger.warn(iae.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * Returns the UTF-8 byte array representation of a given string.
     * @param s The string to be transformed.
     * @return The byte array representation.
     */
    public static final byte [] getUTF8Bytes(String s) {
        try {
            return s.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException usee) {
            logger.error(usee.getLocalizedMessage(), usee);
            return s.getBytes();
        }
    }

    /**
     * Tries to convert a Base64 encoded string into the
     * corresponing byte array.
     * @param s The Base64 encoded string
     * @return The byte array representation or null if
     * an decoding error occurs.
     */
    public static final byte [] decodeHex(String s) {
        try {
            return Hex.decodeHex(s.toCharArray());
        }
        catch (DecoderException de) {
            return null;
        }
    }

    public static final String repeat(String s, int count, String sep) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s);
        for (--count; count > 0; --count) {
            sb.append(sep).append(s);
        }
        return sb.toString();
    }

    public static final String repeat(char c, int count, char sep) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(2*count-1).append(c);
        for (--count; count > 0; --count) {
            sb.append(sep).append(c);
        }
        return sb.toString();
    }

    public static final String [] toUpperCase(String [] s) {
        if (s == null) {
            return null;
        }
        String [] d = new String[s.length];
        for (int i = 0; i < s.length; ++i) {
            if (s[i] != null) {
                d[i] = s[i].toUpperCase();
            }
        }
        return d;
    }

    public static String join(String sep, String [] strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; ++i) {
            if (i > 0) sb.append(sep);
            sb.append(strings[i]);
        }
        return sb.toString();
    }

    public static final String [] join(String [] a, String [] b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        String [] dst = new String[a.length + b.length];
        System.arraycopy(a, 0, dst, 0, a.length);
        System.arraycopy(b, 0, dst, a.length, b.length);
        return dst;
    }

    public static final int indexOf(String needle, String [] haystack) {
        for (int i = 0; i < haystack.length; ++i) {
            if (needle.equals(haystack[i])) {
                return i;
            }
        }
        return -1;
    }

    public static final boolean contains(String needle, String [] haystack) {
        return indexOf(needle, haystack) != -1;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
