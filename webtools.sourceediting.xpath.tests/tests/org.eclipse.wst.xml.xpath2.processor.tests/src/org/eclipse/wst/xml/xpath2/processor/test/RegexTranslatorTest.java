/*******************************************************************************
 * Copyright (c) 2026 Michele Locati and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.test;

import java.util.regex.PatternSyntaxException;

import junit.framework.TestCase;

import org.eclipse.wst.xml.xpath2.processor.internal.utils.RegexTranslator;

public class RegexTranslatorTest extends TestCase {

	private static boolean matches(String input, String regex, String flags) {
		return RegexTranslator.compile(regex, flags).matcher(input).find();
	}

	private static void assertMatches(String input, String regex, String flags) {
		assertTrue("'" + regex + "' (flags: '" + flags + "') should match '" + input + "'", matches(input, regex, flags));
	}

	private static void assertNotMatches(String input, String regex, String flags) {
		assertFalse("'" + regex + "' (flags: '" + flags + "') should not match '" + input + "'", matches(input, regex, flags));
	}

	private static void assertInvalid(String regex) {
		try {
			RegexTranslator.compile(regex, "");
			fail("'" + regex + "' should be invalid");
		} catch (PatternSyntaxException e) {
		}
	}

	public void testAnchors() {
		assertMatches("abc", "^abc$", "");
		// without the m flag, $ matches only at the end of the string
		assertNotMatches("abc\n", "^abc$", "");
		assertMatches("x\nabc\ny", "^abc$", "m");
		assertNotMatches("x\r\nabc\r\ny", "^abc$", "m");
	}

	public void testDot() {
		assertNotMatches("\n", ".", "");
		assertNotMatches("\r", ".", "");
		assertMatches("\n", ".", "s");
		assertMatches("\r", ".", "s");
		assertMatches("\uD800\uDC00", "^.$", "");
	}

	public void testWhitespaceFlag() {
		assertMatches("helloworld", "hello world", "x");
		assertNotMatches("hello world", "hello world", "x");
		// whitespace is kept within character class expressions
		assertNotMatches("123", "[0 - 9]{3}", "x");
		assertMatches(" ", "[ ]", "x");
		// the examples of the specification
		assertNotMatches("helloworld", "hello[ ]world", "x");
		assertMatches("hello world", "hello\\ sworld", "x");
	}

	public void testQuantifiers() {
		assertMatches("aaa", "^a{3}$", "");
		assertMatches("aaa", "^a{2,}$", "");
		assertMatches("aaa", "^a{1,3}$", "");
		assertNotMatches("aaaa", "^a{1,3}$", "");
		assertEquals("aa", RegexTranslator.compile("a+?", "").matcher("aaa").replaceFirst(""));
	}

	public void testBackReferences() {
		assertMatches("abab", "^(ab)\\1$", "");
		// there is only one group: \10 is \1 followed by 0
		assertMatches("aa0", "^(a)\\10$", "");
		assertMatches("abcdefghijj", "^(a)(b)(c)(d)(e)(f)(g)(h)(i)(j)\\10$", "");
		assertInvalid("\\1(a)");
		assertInvalid("(a\\1)");
	}

	public void testMultiCharEscapes() {
		assertMatches(" \t\n\r", "^\\s+$", "");
		assertNotMatches("\u00A0", "\\s", "");
		assertMatches("\u0663", "\\d", "");
		assertNotMatches("a", "\\d", "");
		assertMatches("a", "\\w", "");
		assertNotMatches("-", "\\w", "");
		assertMatches("-", "\\W", "");
		assertMatches("_", "\\i", "");
		assertNotMatches("-", "\\i", "");
		assertMatches("-", "\\c", "");
		assertNotMatches(" ", "\\c", "");
		assertMatches("-", "\\I", "");
		assertMatches(" ", "\\C", "");
	}

	public void testNegatedClassWithMultiCharEscapes() {
		assertMatches("-", "[^\\w]", "");
		assertNotMatches("a", "[^\\w]", "");
		assertMatches(" ", "[^\\S]", "");
		assertNotMatches("a", "[^\\S]", "");
		assertNotMatches("a", "[^\\wb]", "");
		assertNotMatches("b", "[^\\wb]", "");
		assertMatches("-", "[^\\wb]", "");
	}

	public void testCategories() {
		assertMatches("A", "\\p{Lu}", "");
		assertNotMatches("a", "\\p{Lu}", "");
		assertMatches("a", "\\P{Lu}", "");
		assertMatches("a", "\\p{L}", "");
		assertMatches("\u2028", "\\p{Zl}", "");
		assertInvalid("\\p{Foo}");
		assertInvalid("\\p{}");
		assertInvalid("\\p{IsLu}");
	}

	public void testCaseSensitiveCategories() {
		assertMatches("M", "\\p{Lu}", "i");
		assertNotMatches("m", "\\p{Lu}", "i");
		assertNotMatches("m", "[\\p{Lu}]", "i");
		assertMatches("m", "[\\p{Lu}m]", "i");
		assertMatches("m", "[\\p{Lu}M]", "i");
		assertMatches("m", "[^\\p{Lu}]", "i");
		assertNotMatches("M", "[^\\p{Lu}]", "i");
		assertNotMatches("M", "\\p{Ll}", "i");
		assertMatches("m", "\\P{Lu}", "i");
	}

	public void testCaseInsensitive() {
		assertMatches("ABC", "abc", "i");
		assertMatches("Q", "[a-z]", "i");
		assertMatches("\u00C9", "\u00E9", "i");
		assertMatches("aA", "^(a)\\1$", "i");
	}

	public void testBlocks() {
		assertMatches("a", "\\p{IsBasicLatin}", "");
		assertNotMatches("\u00E9", "\\p{IsBasicLatin}", "");
		assertMatches("\u00E9", "\\p{IsLatin-1Supplement}", "");
		assertMatches("a", "\\P{IsLatin-1Supplement}", "");
		assertMatches("\u0627", "\\p{IsArabic}", "");
		assertMatches("\uE000", "\\p{IsPrivateUse}", "");
		assertMatches("\uDB80\uDC00", "\\p{IsPrivateUse}", "");
		assertMatches("\uD840\uDC00", "\\p{IsCJKUnifiedIdeographsExtensionB}", "");
		assertMatches("\uFEFF", "\\p{IsSpecials}", "");
		assertMatches("\u00E9", "[^\\P{IsLatin-1Supplement}]", "");
		assertNotMatches("a", "[^\\P{IsLatin-1Supplement}]", "");
		assertInvalid("\\p{IsFoo}");
	}

	public void testCharacterClasses() {
		assertMatches("-", "[-a]", "");
		assertMatches("-", "[a-]", "");
		assertNotMatches("-", "[^-a]", "");
		assertMatches("^", "[a^]", "");
		assertMatches("]", "[\\]]", "");
		assertMatches("\uD800\uDC01", "[\uD800\uDC00-\uD800\uDC02]", "");
		assertInvalid("[]");
		assertInvalid("[a");
		assertInvalid("[a[b]]");
		assertInvalid("[z-a]");
		assertInvalid("[a-\\d]");
		assertInvalid("[\\d-z]");
		assertInvalid("[a-c-e]");
	}

	public void testSubtraction() {
		assertMatches("b", "[a-z-[aeiou]]", "");
		assertNotMatches("a", "[a-z-[aeiou]]", "");
		assertMatches("a", "^[a-z-[b-y-[c]]]$", "");
		assertMatches("c", "^[a-z-[b-y-[c]]]$", "");
		assertNotMatches("d", "^[a-z-[b-y-[c]]]$", "");
		assertNotMatches("A", "[^a-z-[A]]", "");
		assertMatches("B", "[^a-z-[A]]", "");
		assertMatches("b", "[\\w-[a]]", "");
		assertNotMatches("a", "[\\w-[a]]", "");
		assertInvalid("[a-z-[aeiou]b]");
	}

	public void testInvalidExpressions() {
		assertInvalid("(");
		assertInvalid(")");
		assertInvalid("a**");
		assertInvalid("a{2,1}");
		assertInvalid("a{,1}");
		assertInvalid("*a");
		assertInvalid("{1}");
		assertInvalid("(?:a)");
		assertInvalid("]");
		assertInvalid("}");
		assertInvalid("\\b");
		assertInvalid("\\0");
		assertInvalid("\\");
	}
}
