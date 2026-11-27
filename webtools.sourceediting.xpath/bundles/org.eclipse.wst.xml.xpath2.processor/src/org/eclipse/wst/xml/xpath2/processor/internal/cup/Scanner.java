/*
 * The runtime of CUP 0.10k (package java_cup.runtime), included so that the
 * processor doesn't depend on it. It is distributed under this license:
 *
 * CUP Parser Generator Copyright Notice, License, and Disclaimer
 *
 * Copyright 1996-1999 by Scott Hudson, Frank Flannery, C. Scott Ananian
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose and without fee is hereby granted, provided
 * that the above copyright notice appear in all copies and that both
 * the copyright notice and this permission notice and warranty disclaimer
 * appear in supporting documentation, and that the names of the authors
 * or their employers not be used in advertising or publicity pertaining
 * to distribution of the software without specific, written prior permission.
 *
 * The authors and their employers disclaim all warranties with regard to
 * this software, including all implied warranties of merchantability
 * and fitness. In no event shall the authors or their employers be liable
 * for any special, indirect or consequential damages or any damages
 * whatsoever resulting from loss of use, data or profits, whether in an action
 * of contract, negligence or other tortious action, arising out of or
 * in connection with the use or performance of this software.
 */

package org.eclipse.wst.xml.xpath2.processor.internal.cup;

/**
 * Defines the Scanner interface, which CUP uses in the default
 * implementation of <code>lr_parser.scan()</code>.  Integration
 * of scanners implementing <code>Scanner</code> is facilitated.
 *
 * @version last updated 23-Jul-1999
 * @author David MacMahon &lt;davidm@smartsc.com&gt;
 */

/* *************************************************
  Interface Scanner
  
  Declares the next_token() method that should be
  implemented by scanners.  This method is typically
  called by lr_parser.scan().  End-of-file can be
  indicated either by returning
  <code>new Symbol(lr_parser.EOF_sym())</code> or
  <code>null</code>.
 ***************************************************/
public interface Scanner {
    /** Return the next token, or <code>null</code> on end-of-file. */
    public Symbol next_token() throws java.lang.Exception;
}
