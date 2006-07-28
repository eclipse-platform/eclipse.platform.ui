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
package org.eclipse.ua.tests.help.toc;

import java.io.ByteArrayInputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.internal.base.remote.RemoteTocParser;
import org.eclipse.help.internal.toc.TocFileProvider;
import org.eclipse.help.internal.webapp.servlet.TocSerializer;

public class RemoteTocParserTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(RemoteTocParserTest.class);
	}

	public void testProvider() throws Exception {
		// first serialize the tocs
		String locale = Platform.getNL();
		
		TocFileProvider provider = new TocFileProvider();
		ITocContribution[] contributions = provider.getTocContributions(locale);
		String expected = TocSerializer.serialize(contributions, locale);

		// parse in the serialization
		ByteArrayInputStream in = new ByteArrayInputStream(expected.getBytes("UTF-8"));
		RemoteTocParser parser = new RemoteTocParser();
		ITocContribution[] parsedContributions = parser.parse(in);
		
		// serialize the parsed data again and compare with original
		String actual = TocSerializer.serialize(parsedContributions, locale);
		
		assertEquals("Did not get the same result when parsing then re-serializing toc contributions", expected, actual);
	}
}
