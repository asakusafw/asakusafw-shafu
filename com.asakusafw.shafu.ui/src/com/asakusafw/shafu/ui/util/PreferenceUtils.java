/**
 *
 */
package com.asakusafw.shafu.ui.util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utilities for preferences.
 */
public final class PreferenceUtils {

    private PreferenceUtils() {
        return;
    }

    private static final char ESCAPE = '\\';

    private static final char KEY_DELIMITER = '%';

    private static final char VALUE_DELIMITER = '$';

    /**
     * Encodes a path into a flat character string.
     * @param file the target path
     * @return the encoded string
     */
    public static String encodeFile(File file) {
        if (file == null) {
            return ""; //$NON-NLS-1$
        }
        return file.getPath();
    }

    /**
     * Decodes the encoded flat character string into file path.
     * @param encoded the encoded string
     * @return the decoded path
     */
    public static File decodeFile(String encoded) {
        if (encoded.isEmpty()) {
            return null;
        }
        return new File(encoded);
    }

    /**
     * Encodes pairs into a flat character string.
     * @param pairs the target pairs
     * @return the encoded string
     */
    public static String encodeMap(Map<String, String> pairs) {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> pair : pairs.entrySet()) {
            encodeFieldTo(pair.getKey(), buf);
            buf.append(KEY_DELIMITER);
            encodeFieldTo(pair.getValue(), buf);
            buf.append(VALUE_DELIMITER);
        }
        return buf.toString();
    }

    private static void encodeFieldTo(String string, StringBuilder buf) {
        for (int i = 0, n = string.length(); i < n; i++) {
            char c = string.charAt(i);
            if (c == ESCAPE || c == KEY_DELIMITER || c == VALUE_DELIMITER) {
                buf.append(ESCAPE);
            }
            buf.append(c);
        }
    }

    /**
     * Decodes the encoded flat character string into pairs.
     * @param encoded the encoded string
     * @return the decoded pairs
     */
    public static Map<String, String> decodeToMap(String encoded) {
        boolean sawEscape = false;
        String key = null;
        StringBuilder buf = new StringBuilder();
        Map<String, String> results = new LinkedHashMap<String, String>();
        for (int i = 0, n = encoded.length(); i < n; i++) {
            char c = encoded.charAt(i);
            if (sawEscape) {
                buf.append(c);
                sawEscape = false;
            } else {
                switch (c) {
                case ESCAPE:
                    sawEscape = true;
                    break;
                case KEY_DELIMITER:
                    key = buf.toString();
                    buf.setLength(0);
                    break;
                case VALUE_DELIMITER:
                    if (key != null) {
                        String value = buf.toString();
                        buf.setLength(0);
                        results.put(key, value);
                        key = null;
                    }
                    break;
                default:
                    buf.append(c);
                    break;
                }
            }
        }
        return results;
    }
}
