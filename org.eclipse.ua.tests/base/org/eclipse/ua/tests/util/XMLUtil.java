/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.Assert;

import org.eclipse.help.internal.dynamic.DOMProcessorHandler;
import org.eclipse.help.internal.dynamic.XMLProcessor;

/**
 * A utility class for working with XML.
 */
public class XMLUtil extends Assert {
	
	public static void assertXMLEquals(String msg, String s1, String s2) throws Exception {
		InputStream in1 = new ByteArrayInputStream(s1.getBytes("UTF-8"));
		InputStream in2 = new ByteArrayInputStream(s2.getBytes("UTF-8"));
		assertXMLEquals(msg, in1, in2);
	}
	
	public static void assertXMLEquals(String msg, InputStream in1, InputStream in2) throws Exception {
		XMLProcessor processor = new XMLProcessor(new DOMProcessorHandler[0]);
		String s1 = FileUtil.readString(processor.process(in1, null));
		String s2 = FileUtil.readString(processor.process(in2, null));
		assertEquals(msg, s1, s2);
	}
}
