/*******************************************************************************
 * Copyright (c) 2026 Michele Locati and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.test;

import junit.framework.TestCase;

import org.eclipse.wst.xml.xpath2.processor.JFlexCupParser;
import org.eclipse.wst.xml.xpath2.processor.XPathParserException;

/**
 * Bug 388504: XPath scanner does not detect non-ASCII names.
 */
public class XPathScannerTest extends TestCase {

	private static void assertParses(String xpath) {
		try {
			new JFlexCupParser().parse(xpath);
		} catch (XPathParserException e) {
			fail("'" + xpath + "' should be parsed: " + e.getMessage());
		}
	}

	public void testAsciiNames() {
		assertParses("/abc/d-e.f_g");
	}

	public void testNonAsciiNames() {
		// the example of the bug report
		assertParses("//k\u00F8kkenm\u00F8dding[@\u00E6ggeskaller = $schl\u00FC\u00DFel]");
		assertParses("/citt\u00E0");
		assertParses("/\u3042\u3044");
	}

	public void testSupplementaryCharactersInNames() {
		// U+10000 LINEAR B SYLLABLE B008 A
		assertParses("/a\uD800\uDC00b");
		assertParses("/\uD800\uDC00");
	}
}
