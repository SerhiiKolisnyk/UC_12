package com.softserve.sk.myapplication;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class providing a collection of static helper methods for common string manipulations and operations.
 *
 * <p>This class is designed to assist in handling and modifying strings in various ways, such as abbreviating,
 * swapping case, wrapping text, and more. It provides efficient solutions for common problems faced in string processing.</p>
 *
 * <p><strong>Usage Examples:</strong></p>
 *
 * <pre>
 *   Utils.abbreviate("Hello World", 5, 10, "...")    -> "Hello..."
 *   Utils.swapCase("Hello World")                    -> "hELLO wORLD"
 *   Utils.wrap("Hello World", 7)                    -> "Hello\nWorld"
 * </pre>
 *
 * <p><strong>Note:</strong> All methods are static</p>
 */
public class Utils {

    private static final int INDEX_NOT_FOUND = -1;
    private static final String EMPTY = "";

    /**
     * Abbreviates a given string between specified lower and upper bounds and appends the provided suffix if the string is truncated.
     *
     * <p>This method attempts to find a space character after the specified lower bound and abbreviates the string there. If no space character is found,
     * it abbreviates the string at the upper bound. If the string gets truncated, the provided suffix (appendToEnd) is added to the result.</p>
     *
     * <h3>Usage Examples:</h3>
     *
     * <pre>
     *   Utils.abbreviate("Hello world", 5, 10, "...")   -> "Hello..."
     *   Utils.abbreviate("Hello world", 5, 7, "...")    -> "Hello..."
     *   Utils.abbreviate("Hello", 5, 10, "...")         -> "Hello"
     *   Utils.abbreviate("Hello world", 20, 25, "...")  -> "Hello world"
     *   Utils.abbreviate("Hello", -1, 7, "...")         -> "Hello"
     * </pre>
     *
     * @param str         the string to abbreviate, may be null
     * @param lower       the lower bound index, after which the string will try to be truncated at the first space character.
     *                    If the lower value is greater than the length of the string, it defaults to the string's length.
     * @param upper       the upper bound index at which the string will be truncated if no space character is found after the lower bound.
     *                    If the upper value is -1 or greater than the length of the string, it defaults to the string's length.
     * @param appendToEnd the string to append at the end of the abbreviated string. If the original string is not truncated, this suffix will not be added.
     *                    If provided as null, it defaults to an empty string.
     *
     * @return the abbreviated string with the appendToEnd suffix if applicable.
     * @throws IllegalArgumentException if the upper value is less than -1 or if the upper value is less than the lower value.
     **/
    public static String abbreviate(final String str, int lower, int upper, final String appendToEnd) {
        isTrue(upper >= -1, "upper value cannot be less than -1");
        isTrue(upper >= lower || upper == -1, "upper value is less than lower value");
        if (isEmpty(str)) {
            return str;
        }

        if (lower > str.length()) {
            lower = str.length();
        }

        if (upper == -1 || upper > str.length()) {
            upper = str.length();
        }

        final StringBuilder result = new StringBuilder();
        final int index = indexOf(str, " ", lower);
        if (index == -1) {
            result.append(str, 0, upper);
            if (upper != str.length()) {
                result.append(defaultString(appendToEnd));
            }
        } else {
            result.append(str, 0, Math.min(index, upper));
            result.append(defaultString(appendToEnd));
        }

        return result.toString();
    }

    /**
     * Extracts the initials from the given string using the specified delimiters. If no delimiters are provided,
     * whitespace characters will be used as the default delimiter. Initials are extracted based on the delimiters,
     * meaning each character that comes after a delimiter will be considered an initial.
     *
     * <h3>Usage Examples:</h3>
     *
     * <pre>
     *   Utils.initials("John F. Kennedy")           -> "JFK"
     *   Utils.initials("John F. Kennedy", '.')      -> "JF"
     *   Utils.initials("Madonna", ' ')              -> "M"
     *   Utils.initials(" ", ' ')                    -> ""
     * </pre>
     *
     * @param str        the string from which to extract initials, may be null
     * @param delimiters the characters to use as delimiters, null or empty array means whitespace
     * @return the initials of the string, or the original string if it's empty
     */
    public static String initials(final String str, final char... delimiters) {
        if (isEmpty(str)) {
            return str;
        }
        if (delimiters != null && delimiters.length == 0) {
            return EMPTY;
        }
        final Set<Integer> delimiterSet = generateDelimiterSet(delimiters);
        final int strLen = str.length();
        final int[] newCodePoints = new int[strLen / 2 + 1];
        int count = 0;
        boolean lastWasGap = true;
        for (int i = 0; i < strLen; ) {
            final int codePoint = str.codePointAt(i);

            if (delimiterSet.contains(codePoint) || delimiters == null && Character.isWhitespace(codePoint)) {
                lastWasGap = true;
            } else if (lastWasGap) {
                newCodePoints[count++] = codePoint;
                lastWasGap = false;
            }

            i += Character.charCount(codePoint);
        }
        return new String(newCodePoints, 0, count);
    }

    /**
     * Swaps the case of each character in the given string. Upper-case characters will be converted to lower-case, and
     * lower-case characters will be converted to upper-case. Additionally, the method turns the first lower-case character
     * after a whitespace into a title-case character.
     *
     * <p>Note: This method takes into account title-case characters (which are usually not used in most languages).</p>
     *
     * <h3>Usage Examples:</h3>
     *
     * <pre>
     *   StringUtils.swapCase("Hello World")    -> "hELLO wORLD"
     *   StringUtils.swapCase("HELLO WORLD")    -> "hello world"
     *   StringUtils.swapCase("hello world")    -> "Hello World"
     *   StringUtils.swapCase("Hello wORLD")    -> "hELLO World"
     *   StringUtils.swapCase("12345")          -> "12345"
     *   StringUtils.swapCase("")               -> ""
     * </pre>
     *
     * @param str the string to swap case, may be null
     * @return the swapped case string, or the original string if it's empty or null
     */
    public static String swapCase(final String str) {
        if (isEmpty(str)) {
            return str;
        }
        final int strLen = str.length();
        final int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        boolean whitespace = true;
        for (int index = 0; index < strLen; ) {
            final int oldCodepoint = str.codePointAt(index);
            final int newCodePoint;
            if (Character.isUpperCase(oldCodepoint) || Character.isTitleCase(oldCodepoint)) {
                newCodePoint = Character.toLowerCase(oldCodepoint);
                whitespace = false;
            } else if (Character.isLowerCase(oldCodepoint)) {
                if (whitespace) {
                    newCodePoint = Character.toTitleCase(oldCodepoint);
                    whitespace = false;
                } else {
                    newCodePoint = Character.toUpperCase(oldCodepoint);
                }
            } else {
                whitespace = Character.isWhitespace(oldCodepoint);
                newCodePoint = oldCodepoint;
            }
            newCodePoints[outOffset++] = newCodePoint;
            index += Character.charCount(newCodePoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

    /**
     * Wraps the given string at the specified wrap length. It also supports custom line breaks, wrap characters, and the option to wrap long words.
     *
     * <p>This method will attempt to wrap the input string at the specified wrap length or at the custom wrap character.
     * If a string is shorter than the wrap length, it remains as is. When wrapping at a specific character, if that character
     * is not found in the string, the string remains unchanged.</p>
     *
     * <h3>Usage Examples:</h3>
     *
     * <pre>
     *   StringUtils.wrap("Hello World", 7, null, false, " ")    -> "Hello\nWorld"
     *   StringUtils.wrap("Hello World", 7, "*", false, " ")     -> "Hello*World"
     *   StringUtils.wrap("abcdefg", 3, null, true, " ")         -> "abc\ndef\ng"
     *   StringUtils.wrap("abcdefg", 3, null, false, " ")        -> "abcdefg"
     *   StringUtils.wrap("abcdefg", 3, null, true, "-")         -> "abcdefg"
     * </pre>
     *
     * @param str           the string to wrap, may be null
     * @param wrapLength    the column to wrap the words at, must be greater than 0
     * @param newLineStr    the string that represents a new line, defaults to the system's line separator if null
     * @param wrapLongWords flag to control if words longer than wrapLength should be wrapped
     * @param wrapOn        the string on which to wrap, defaults to space if empty or null
     * @return the wrapped string, or the original string if it doesn't need wrapping or if it's null
     */
    public static String wrap(final String str,
                              int wrapLength,
                              String newLineStr,
                              final boolean wrapLongWords,
                              String wrapOn) {
        if (str == null) {
            return null;
        }
        if (newLineStr == null) {
            newLineStr = System.lineSeparator();
        }
        if (wrapLength < 1) {
            wrapLength = 1;
        }
        if (isBlank(wrapOn)) {
            wrapOn = " ";
        }
        final Pattern patternToWrapOn = Pattern.compile(wrapOn);
        final int inputLineLength = str.length();
        int offset = 0;
        final StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);
        int matcherSize = -1;

        while (offset < inputLineLength) {
            int spaceToWrapAt = -1;
            Matcher matcher = patternToWrapOn.matcher(str.substring(offset,
                    Math.min((int) Math.min(Integer.MAX_VALUE, offset + wrapLength + 1L), inputLineLength)));
            if (matcher.find()) {
                if (matcher.start() == 0) {
                    matcherSize = matcher.end();
                    if (matcherSize != 0) {
                        offset += matcher.end();
                        continue;
                    }
                    offset += 1;
                }
                spaceToWrapAt = matcher.start() + offset;
            }

            if (inputLineLength - offset <= wrapLength) {
                break;
            }

            while (matcher.find()) {
                spaceToWrapAt = matcher.start() + offset;
            }

            if (spaceToWrapAt >= offset) {
                wrappedLine.append(str, offset, spaceToWrapAt);
                wrappedLine.append(newLineStr);
                offset = spaceToWrapAt + 1;
            } else if (wrapLongWords) {
                if (matcherSize == 0) {
                    offset--;
                }
                wrappedLine.append(str, offset, wrapLength + offset);
                wrappedLine.append(newLineStr);
                offset += wrapLength;
                matcherSize = -1;
            } else {
                matcher = patternToWrapOn.matcher(str.substring(offset + wrapLength));
                if (matcher.find()) {
                    matcherSize = matcher.end() - matcher.start();
                    spaceToWrapAt = matcher.start() + offset + wrapLength;
                }

                if (spaceToWrapAt >= 0) {
                    if (matcherSize == 0 && offset != 0) {
                        offset--;
                    }
                    wrappedLine.append(str, offset, spaceToWrapAt);
                    wrappedLine.append(newLineStr);
                    offset = spaceToWrapAt + 1;
                } else {
                    if (matcherSize == 0 && offset != 0) {
                        offset--;
                    }
                    wrappedLine.append(str, offset, str.length());
                    offset = inputLineLength;
                    matcherSize = -1;
                }
            }
        }

        if (matcherSize == 0 && offset < inputLineLength) {
            offset--;
        }

        wrappedLine.append(str, offset, str.length());

        return wrappedLine.toString();
    }

    private static Set<Integer> generateDelimiterSet(final char[] delimiters) {
        final Set<Integer> delimiterHashSet = new HashSet<>();
        if (delimiters == null || delimiters.length == 0) {
            if (delimiters == null) {
                delimiterHashSet.add(Character.codePointAt(new char[]{' '}, 0));
            }

            return delimiterHashSet;
        }

        for (int index = 0; index < delimiters.length; index++) {
            delimiterHashSet.add(Character.codePointAt(delimiters, index));
        }
        return delimiterHashSet;
    }

    private static void isTrue(final boolean expression, final String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    private static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    private static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private static boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static String defaultString(final String str) {
        return Objects.toString(str, EMPTY);
    }

    private static int indexOf(final CharSequence seq, final CharSequence searchSeq, final int startPos) {
        if (seq == null || searchSeq == null) {
            return INDEX_NOT_FOUND;
        }
        if (seq instanceof String) {
            return ((String) seq).indexOf(searchSeq.toString(), startPos);
        }
        if (seq instanceof StringBuilder) {
            return ((StringBuilder) seq).indexOf(searchSeq.toString(), startPos);
        }
        if (seq instanceof StringBuffer) {
            return ((StringBuffer) seq).indexOf(searchSeq.toString(), startPos);
        }
        return seq.toString().indexOf(searchSeq.toString(), startPos);
    }
}