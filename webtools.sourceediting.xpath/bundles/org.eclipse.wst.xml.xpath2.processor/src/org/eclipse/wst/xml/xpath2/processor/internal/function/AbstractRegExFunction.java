/*******************************************************************************
 * Copyright (c) 2009, 2010 Standards for Technology in Automotive Retail and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Carver - bug 262765 - initial API and implementation
 *     Mukul Gandhi - bug 280798 - PsychoPath XPath 2.0 support for JDK 1.4
 *     Mukul Gandhi - Fixes for XercesJ bug https://issues.apache.org/jira/browse/XERCESJ-1732
 *     Michele Locati - use java.util.regex, through RegexTranslator
 *******************************************************************************/
package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.regex.Matcher;

import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.utils.RegexTranslator;


public abstract class AbstractRegExFunction extends Function {
	private static final String validflags = "smix";

	public AbstractRegExFunction(QName name, int arity) {
		super(name, arity);
	}
	
	public AbstractRegExFunction(QName name, int min_arity, int max_arity) {
		super(name, min_arity, max_arity);
	}
	
	protected static Matcher regex(String pattern, String flags, String src) {
		return RegexTranslator.compile(pattern, flags).matcher(src);
	}
	
	protected static boolean isFlagStrValid(String flags) {
       boolean flagStrValid = true;
       
       if (flags.length() > 0) {
    	  for (int idx = 0; idx < flags.length(); idx++) {
    		 if (validflags.indexOf(flags.charAt(idx)) == -1) {
    			flagStrValid = false;
    			break;
    		 }
    	  }
       }
       
       return flagStrValid; 
	}

}
