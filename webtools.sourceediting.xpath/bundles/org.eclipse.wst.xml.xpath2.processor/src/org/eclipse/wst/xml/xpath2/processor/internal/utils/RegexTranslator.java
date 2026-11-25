/*******************************************************************************
 * Copyright (c) 2026 Michele Locati and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.utils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Translates XPath 2.0 regular expressions (see "Regular Expression Syntax" in
 * XQuery 1.0 and XPath 2.0 Functions and Operators) into java.util.regex ones.
 */
public final class RegexTranslator {

	private static final String CATEGORIES = " L Lu Ll Lt Lm Lo M Mn Mc Me N Nd Nl No P Pc Pd Ps Pe Pi Pf Po Z Zs Zl Zp S Sm Sc Sk So C Cc Cf Co Cn ";

	/*
	 * Since Java 15, CASE_INSENSITIVE applies to these categories too, but in
	 * XPath \p{Lu} must not match lower-case letters.
	 */
	private static final String CASE_SENSITIVE_CATEGORIES = " Lu Ll Lt ";

	private static final String WHITESPACE = "\\x{20}\\t\\n\\r";

	private static final String NOT_WORD = "\\p{P}\\p{Z}\\p{C}";

	// NameStartChar and NameChar of XML 1.0 Fifth Edition
	private static final String NAME_START_CHAR = ":A-Z_a-z\\x{C0}-\\x{D6}\\x{D8}-\\x{F6}\\x{F8}-\\x{2FF}\\x{370}-\\x{37D}"
			+ "\\x{37F}-\\x{1FFF}\\x{200C}-\\x{200D}\\x{2070}-\\x{218F}\\x{2C00}-\\x{2FEF}\\x{3001}-\\x{D7FF}"
			+ "\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFFD}\\x{10000}-\\x{EFFFF}";

	private static final String NAME_CHAR = NAME_START_CHAR + "\\-.0-9\\x{B7}\\x{300}-\\x{36F}\\x{203F}-\\x{2040}";

	private static final String ANY_CHAR = "[\\x{0}-\\x{10FFFF}]";

	// The block names of XML Schema Part 2, F.1.1 (Unicode 3.1)
	private static final String[] BLOCKS = {
		"BasicLatin=0000-007F",
		"Latin-1Supplement=0080-00FF",
		"LatinExtended-A=0100-017F",
		"LatinExtended-B=0180-024F",
		"IPAExtensions=0250-02AF",
		"SpacingModifierLetters=02B0-02FF",
		"CombiningDiacriticalMarks=0300-036F",
		"Greek=0370-03FF",
		"Cyrillic=0400-04FF",
		"Armenian=0530-058F",
		"Hebrew=0590-05FF",
		"Arabic=0600-06FF",
		"Syriac=0700-074F",
		"Thaana=0780-07BF",
		"Devanagari=0900-097F",
		"Bengali=0980-09FF",
		"Gurmukhi=0A00-0A7F",
		"Gujarati=0A80-0AFF",
		"Oriya=0B00-0B7F",
		"Tamil=0B80-0BFF",
		"Telugu=0C00-0C7F",
		"Kannada=0C80-0CFF",
		"Malayalam=0D00-0D7F",
		"Sinhala=0D80-0DFF",
		"Thai=0E00-0E7F",
		"Lao=0E80-0EFF",
		"Tibetan=0F00-0FFF",
		"Myanmar=1000-109F",
		"Georgian=10A0-10FF",
		"HangulJamo=1100-11FF",
		"Ethiopic=1200-137F",
		"Cherokee=13A0-13FF",
		"UnifiedCanadianAboriginalSyllabics=1400-167F",
		"Ogham=1680-169F",
		"Runic=16A0-16FF",
		"Khmer=1780-17FF",
		"Mongolian=1800-18AF",
		"LatinExtendedAdditional=1E00-1EFF",
		"GreekExtended=1F00-1FFF",
		"GeneralPunctuation=2000-206F",
		"SuperscriptsandSubscripts=2070-209F",
		"CurrencySymbols=20A0-20CF",
		"CombiningMarksforSymbols=20D0-20FF",
		"LetterlikeSymbols=2100-214F",
		"NumberForms=2150-218F",
		"Arrows=2190-21FF",
		"MathematicalOperators=2200-22FF",
		"MiscellaneousTechnical=2300-23FF",
		"ControlPictures=2400-243F",
		"OpticalCharacterRecognition=2440-245F",
		"EnclosedAlphanumerics=2460-24FF",
		"BoxDrawing=2500-257F",
		"BlockElements=2580-259F",
		"GeometricShapes=25A0-25FF",
		"MiscellaneousSymbols=2600-26FF",
		"Dingbats=2700-27BF",
		"BraillePatterns=2800-28FF",
		"CJKRadicalsSupplement=2E80-2EFF",
		"KangxiRadicals=2F00-2FDF",
		"IdeographicDescriptionCharacters=2FF0-2FFF",
		"CJKSymbolsandPunctuation=3000-303F",
		"Hiragana=3040-309F",
		"Katakana=30A0-30FF",
		"Bopomofo=3100-312F",
		"HangulCompatibilityJamo=3130-318F",
		"Kanbun=3190-319F",
		"BopomofoExtended=31A0-31BF",
		"EnclosedCJKLettersandMonths=3200-32FF",
		"CJKCompatibility=3300-33FF",
		"CJKUnifiedIdeographsExtensionA=3400-4DB5",
		"CJKUnifiedIdeographs=4E00-9FFF",
		"YiSyllables=A000-A48F",
		"YiRadicals=A490-A4CF",
		"HangulSyllables=AC00-D7A3",
		"PrivateUse=E000-F8FF,F0000-FFFFD,100000-10FFFD",
		"CJKCompatibilityIdeographs=F900-FAFF",
		"AlphabeticPresentationForms=FB00-FB4F",
		"ArabicPresentationForms-A=FB50-FDFF",
		"CombiningHalfMarks=FE20-FE2F",
		"CJKCompatibilityForms=FE30-FE4F",
		"SmallFormVariants=FE50-FE6F",
		"ArabicPresentationForms-B=FE70-FEFE",
		"Specials=FEFF-FEFF,FFF0-FFFD",
		"HalfwidthandFullwidthForms=FF00-FFEF",
		"OldItalic=10300-1032F",
		"Gothic=10330-1034F",
		"Deseret=10400-1044F",
		"ByzantineMusicalSymbols=1D000-1D0FF",
		"MusicalSymbols=1D100-1D1FF",
		"MathematicalAlphanumericSymbols=1D400-1D7FF",
		"CJKUnifiedIdeographsExtensionB=20000-2A6D6",
		"CJKCompatibilityIdeographsSupplement=2F800-2FA1F",
		"Tags=E0000-E007F"
	};

	private static Map blockRanges = null;

	private final String regex;

	private final boolean dotAll;

	private final boolean multiLine;

	private final boolean caseInsensitive;

	private final StringBuilder out = new StringBuilder();

	private final BitSet closedGroups = new BitSet();

	private int pos = 0;

	private int groupCount = 0;

	private RegexTranslator(String regex, String flags) {
		this.regex = flags.indexOf('x') >= 0 ? removeWhitespace(regex) : regex;
		this.dotAll = flags.indexOf('s') >= 0;
		this.multiLine = flags.indexOf('m') >= 0;
		this.caseInsensitive = flags.indexOf('i') >= 0;
	}

	/**
	 * Compiles an XPath 2.0 regular expression.
	 *
	 * @param regex the XPath 2.0 regular expression
	 * @param flags the XPath 2.0 flags (assumed to contain only s, m, i, x)
	 * @return the compiled pattern
	 * @throws PatternSyntaxException if the regular expression is not valid
	 */
	public static Pattern compile(String regex, String flags) throws PatternSyntaxException {
		if (flags == null) {
			flags = "";
		}
		RegexTranslator translator = new RegexTranslator(regex, flags);
		String javaRegex = translator.translate();
		// UNIX_LINES: for XPath, the only line separator is \n
		int javaFlags = Pattern.UNIX_LINES;
		if (translator.dotAll) {
			javaFlags |= Pattern.DOTALL;
		}
		if (translator.multiLine) {
			javaFlags |= Pattern.MULTILINE;
		}
		if (translator.caseInsensitive) {
			javaFlags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		}
		return Pattern.compile(javaRegex, javaFlags);
	}

	/**
	 * Translates an XPath 2.0 regular expression into a java.util.regex one.
	 *
	 * @param regex the XPath 2.0 regular expression
	 * @param flags the XPath 2.0 flags (assumed to contain only s, m, i, x)
	 * @return the java.util.regex regular expression (to be used with the flags set by {@link #compile(String, String)})
	 * @throws PatternSyntaxException if the regular expression is not valid
	 */
	public static String translate(String regex, String flags) throws PatternSyntaxException {
		return new RegexTranslator(regex, flags == null ? "" : flags).translate();
	}

	// With the x flag, whitespace is removed, except within character class expressions
	private static String removeWhitespace(String regex) {
		StringBuilder sb = new StringBuilder(regex.length());
		int depth = 0;
		boolean escaped = false;
		for (int i = 0; i < regex.length(); i++) {
			char c = regex.charAt(i);
			if (depth == 0 && (c == ' ' || c == '\t' || c == '\n' || c == '\r')) {
				continue;
			}
			sb.append(c);
			if (escaped) {
				escaped = false;
			} else if (c == '\\') {
				escaped = true;
			} else if (c == '[') {
				depth++;
			} else if (c == ']' && depth > 0) {
				depth--;
			}
		}
		return sb.toString();
	}

	private String translate() {
		parseRegExp();
		if (pos < regex.length()) {
			throw error("Unmatched ')'");
		}
		return out.toString();
	}

	private PatternSyntaxException error(String description) {
		return new PatternSyntaxException(description, regex, pos);
	}

	private boolean atEnd() {
		return pos >= regex.length();
	}

	private int peek() {
		return regex.codePointAt(pos);
	}

	private int peek(int offset) {
		int i = pos;
		for (int k = 0; k < offset && i < regex.length(); k++) {
			i += Character.charCount(regex.codePointAt(i));
		}
		return i < regex.length() ? regex.codePointAt(i) : -1;
	}

	private int next() {
		if (atEnd()) {
			throw error("Unexpected end of the regular expression");
		}
		int c = regex.codePointAt(pos);
		pos += Character.charCount(c);
		return c;
	}

	private void parseRegExp() {
		parseBranch();
		while (!atEnd() && peek() == '|') {
			pos++;
			out.append('|');
			parseBranch();
		}
	}

	private void parseBranch() {
		while (!atEnd() && peek() != '|' && peek() != ')') {
			parseAtom();
			parseQuantifier();
		}
	}

	private void parseAtom() {
		int c = next();
		switch (c) {
		case '(':
			int group = ++groupCount;
			out.append('(');
			parseRegExp();
			if (atEnd()) {
				throw error("Unmatched '('");
			}
			pos++;
			out.append(')');
			closedGroups.set(group);
			break;
		case '[':
			out.append(parseCharClassExpr());
			break;
		case '.':
			out.append(dotAll ? "." : "[^\\n\\r]");
			break;
		case '^':
			out.append("(?:^)");
			break;
		case '$':
			// without the m flag, $ matches only at the very end of the string
			out.append(multiLine ? "(?:$)" : "(?:\\z)");
			break;
		case '\\':
			parseEscape();
			break;
		case '*':
		case '+':
		case '?':
		case '{':
			throw error("Quantifier without an atom");
		case '}':
		case ']':
			throw error("Unescaped '" + (char) c + "'");
		default:
			out.append(literal(c));
		}
	}

	private void parseQuantifier() {
		if (atEnd()) {
			return;
		}
		int c = peek();
		if (c == '*' || c == '+' || c == '?') {
			pos++;
			out.append((char) c);
		} else if (c == '{') {
			pos++;
			int min = parseNumber();
			int max = min;
			if (!atEnd() && peek() == ',') {
				pos++;
				max = atEnd() || peek() == '}' ? -1 : parseNumber();
			}
			if (atEnd() || peek() != '}') {
				throw error("Invalid quantifier");
			}
			pos++;
			if (max != -1 && max < min) {
				throw error("Invalid quantifier range");
			}
			out.append('{').append(min);
			if (max != min) {
				out.append(',');
				if (max != -1) {
					out.append(max);
				}
			}
			out.append('}');
		} else {
			return;
		}
		if (!atEnd() && peek() == '?') {
			pos++;
			out.append('?');
		}
		if (!atEnd() && (peek() == '*' || peek() == '+' || peek() == '?' || peek() == '{')) {
			throw error("Invalid quantifier");
		}
	}

	private int parseNumber() {
		int start = pos;
		while (!atEnd() && peek() >= '0' && peek() <= '9') {
			pos++;
		}
		if (pos == start) {
			throw error("Invalid quantifier");
		}
		try {
			return Integer.parseInt(regex.substring(start, pos));
		} catch (NumberFormatException e) {
			throw error("Quantifier too large");
		}
	}

	private void parseEscape() {
		if (atEnd()) {
			throw error("Unexpected end of the regular expression");
		}
		int c = peek();
		if (c >= '1' && c <= '9') {
			pos++;
			int group = c - '0';
			while (!atEnd() && peek() >= '0' && peek() <= '9' && group * 10 + (peek() - '0') <= groupCount) {
				group = group * 10 + (next() - '0');
			}
			if (!closedGroups.get(group)) {
				throw error("Back-reference to a group that does not exist or is not closed");
			}
			// the group stops java.util.regex from taking the following digits
			out.append("(?:\\").append(group).append(')');
			return;
		}
		CharClass cc = new CharClass();
		int single = parseClassEscape(cc);
		if (single != -1) {
			out.append(literal(single));
		} else {
			out.append(cc.toJava(false));
		}
	}

	/*
	 * Parses an escape (after the backslash). Returns the character for single
	 * character escapes, or adds the multi-character escape to cc and returns -1.
	 */
	private int parseClassEscape(CharClass cc) {
		int c = next();
		switch (c) {
		case 'n':
			return '\n';
		case 'r':
			return '\r';
		case 't':
			return '\t';
		case '\\':
		case '|':
		case '.':
		case '?':
		case '*':
		case '+':
		case '(':
		case ')':
		case '{':
		case '}':
		case '-':
		case '[':
		case ']':
		case '^':
		case '$':
			return c;
		case 's':
			cc.add(WHITESPACE);
			return -1;
		case 'S':
			cc.addNested("[^" + WHITESPACE + "]");
			return -1;
		case 'd':
			cc.add("\\p{Nd}");
			return -1;
		case 'D':
			cc.add("\\P{Nd}");
			return -1;
		case 'w':
			cc.addNested("[^" + NOT_WORD + "]");
			return -1;
		case 'W':
			cc.add(NOT_WORD);
			return -1;
		case 'i':
			cc.add(NAME_START_CHAR);
			return -1;
		case 'I':
			cc.addNested("[^" + NAME_START_CHAR + "]");
			return -1;
		case 'c':
			cc.add(NAME_CHAR);
			return -1;
		case 'C':
			cc.addNested("[^" + NAME_CHAR + "]");
			return -1;
		case 'p':
		case 'P':
			parseCategoryEscape(cc, c == 'P');
			return -1;
		default:
			throw error("Invalid escape '\\" + new String(Character.toChars(c)) + "'");
		}
	}

	private void parseCategoryEscape(CharClass cc, boolean negated) {
		if (atEnd() || peek() != '{') {
			throw error("Missing '{' after \\p or \\P");
		}
		pos++;
		int end = regex.indexOf('}', pos);
		if (end == -1) {
			throw error("Missing '}' after \\p or \\P");
		}
		String name = regex.substring(pos, end);
		pos = end + 1;
		if (name.startsWith("Is")) {
			String ranges = getBlockRanges(name.substring(2));
			if (ranges == null) {
				throw error("Unknown block name '" + name.substring(2) + "'");
			}
			if (negated) {
				cc.addNested("[^" + ranges + "]");
			} else {
				cc.add(ranges);
			}
			return;
		}
		if (name.length() == 0 || CATEGORIES.indexOf(" " + name + " ") == -1) {
			throw error("Unknown character category '" + name + "'");
		}
		String property = (negated ? "\\P{" : "\\p{") + name + "}";
		if (caseInsensitive && CASE_SENSITIVE_CATEGORIES.indexOf(" " + name + " ") != -1) {
			cc.addCaseSensitive(property);
		} else {
			cc.add(property);
		}
	}

	private static synchronized String getBlockRanges(String name) {
		if (blockRanges == null) {
			Map map = new HashMap();
			for (int i = 0; i < BLOCKS.length; i++) {
				int eq = BLOCKS[i].indexOf('=');
				StringBuilder sb = new StringBuilder();
				String[] ranges = BLOCKS[i].substring(eq + 1).split(",");
				for (int k = 0; k < ranges.length; k++) {
					int dash = ranges[k].indexOf('-');
					sb.append("\\x{").append(ranges[k].substring(0, dash)).append("}-\\x{").append(ranges[k].substring(dash + 1)).append('}');
				}
				map.put(BLOCKS[i].substring(0, eq), sb.toString());
			}
			blockRanges = map;
		}
		String ranges = (String) blockRanges.get(name);
		if (ranges == null) {
			// blocks added to Unicode after version 3.1
			try {
				Character.UnicodeBlock.forName(name);
				ranges = "\\p{In" + name + "}";
			} catch (IllegalArgumentException e) {
			}
		}
		return ranges;
	}

	// Parses a character class expression (after the opening bracket)
	private String parseCharClassExpr() {
		boolean negated = false;
		if (!atEnd() && peek() == '^') {
			pos++;
			negated = true;
		}
		CharClass cc = new CharClass();
		boolean first = true;
		String subtracted = null;
		for (;;) {
			if (atEnd()) {
				throw error("Unmatched '['");
			}
			int c = peek();
			if (c == ']') {
				if (first) {
					throw error("Empty character class");
				}
				pos++;
				break;
			}
			if (c == '[') {
				throw error("Unescaped '[' in a character class");
			}
			if (c == '-' && !first) {
				if (peek(1) == '[') {
					pos += 2;
					subtracted = parseCharClassExpr();
					if (atEnd() || peek() != ']') {
						throw error("A subtraction must be the last part of a character class");
					}
					pos++;
					break;
				}
				if (peek(1) != ']') {
					throw error("Unescaped '-' in a character class");
				}
			}
			first = false;
			int start;
			if (c == '\\') {
				pos++;
				start = parseClassEscape(cc);
				if (start == -1) {
					if (!atEnd() && peek() == '-' && peek(1) != ']' && peek(1) != '[') {
						throw error("Invalid character range");
					}
					continue;
				}
			} else {
				start = next();
			}
			if (!atEnd() && peek() == '-' && peek(1) != ']' && peek(1) != '[') {
				pos++;
				int end;
				int e = next();
				if (e == '\\') {
					end = parseClassEscape(cc);
					if (end == -1) {
						throw error("Invalid character range");
					}
				} else if (e == '[' || e == ']' || e == '-') {
					throw error("Invalid character range");
				} else {
					end = e;
				}
				if (end < start) {
					throw error("Invalid character range");
				}
				cc.add(literal(start) + "-" + literal(end));
			} else {
				cc.add(literal(start));
			}
		}
		String java = cc.toJava(negated);
		if (subtracted != null) {
			java = "(?:(?!" + subtracted + ")" + java + ")";
		}
		return java;
	}

	private static String literal(int c) {
		if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
			return String.valueOf((char) c);
		}
		return "\\x{" + Integer.toHexString(c) + "}";
	}

	/*
	 * The items of a character class, as java.util.regex fragments. Negated
	 * classes containing nested classes are translated with a negative
	 * lookahead, since Java 8 handles [^a[b]] as (not a) or b.
	 */
	private final class CharClass {

		private final StringBuilder items = new StringBuilder();

		private final List caseSensitiveItems = new ArrayList();

		private boolean nested = false;

		void add(String item) {
			items.append(item);
		}

		void addNested(String item) {
			items.append(item);
			nested = true;
		}

		void addCaseSensitive(String item) {
			caseSensitiveItems.add(item);
		}

		String toJava(boolean negated) {
			if (caseSensitiveItems.isEmpty()) {
				if (!negated) {
					return "[" + items + "]";
				}
				if (!nested) {
					return "[^" + items + "]";
				}
			}
			StringBuilder sb = new StringBuilder("(?:");
			if (items.length() > 0) {
				sb.append('[').append(items).append(']');
			}
			for (int i = 0; i < caseSensitiveItems.size(); i++) {
				if (i > 0 || items.length() > 0) {
					sb.append('|');
				}
				sb.append("(?-i:").append(caseSensitiveItems.get(i)).append(')');
			}
			sb.append(')');
			if (negated) {
				return "(?:(?!" + sb + ")" + ANY_CHAR + ")";
			}
			return sb.toString();
		}
	}
}
