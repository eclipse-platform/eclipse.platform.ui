/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.internal.text.html.HTML2TextReader;


public class HTML2TextReaderTester extends TestCase {

	private static final boolean DEBUG= false;

	private static final String LD= System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	public HTML2TextReaderTester(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(HTML2TextReaderTester.class);
	}

	private void verify(String input, String expectedOutput) throws IOException{
		Reader reader= new StringReader(input);
		HTML2TextReader htmlReader= new HTML2TextReader(reader, null);
		String result= htmlReader.getString();
		if (DEBUG)
			System.out.println("<"+ result +"/>");
		assertEquals(expectedOutput, result);
	}

	public void test0() throws IOException{
		String string= "<code>3<5<code>";
		String expected= "3<5";
		verify(string, expected);
	}

	public void test1() throws IOException{
		String string= "<dl><dt>@author</dt><dd>Foo Bar</dd></dl>";
		String expected= LD+ "@author"+LD+"\tFoo Bar"+LD;
		verify(string, expected);
	}

	public void test2() throws IOException{
		String string= "<code>3>5<code>";
		String expected= "3>5";
		verify(string, expected);
	}

	public void test3() throws IOException{
		String string= "<a href= \"<p>this is only a string - not a tag<p>\">text</a>";
		String expected= "text";
		verify(string, expected);
	}

	public void test4() throws IOException{
		String string= 	"<html><body text=\"#000000\" bgcolor=\"#FFFF88\"><font size=-1><h5>void p.Bb.fes()</h5><p><dl><dt>Parameters:</dt><dd><b>i</b> fred or <code>null</code></dd></dl></font></body></html>";
		String expected= "void p.Bb.fes()"+ LD + LD + LD+ "Parameters:"+ LD + "\ti fred or null"+LD;
		verify(string, expected);
	}

	public void test5() throws IOException{
		String string= "<code>1<2<3<4</code>";
		String expected= "1<2<3<4";
		verify(string, expected);
	}

	public void test6() throws IOException{
		//test for bug 19070
		String string= "<p>Something.<p>Something more.";
		String expected= LD + "Something." + LD + "Something more.";
		verify(string, expected);
	}

	public void testComments() throws Exception {
		String string= "<!-- begin-user-doc -->no comment<!-- end-user-doc -->";
		String expected= "no comment";
		verify(string,expected);
	}

}

