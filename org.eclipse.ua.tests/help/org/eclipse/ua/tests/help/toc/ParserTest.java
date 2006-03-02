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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.internal.toc.Toc;
import org.eclipse.help.internal.toc.TocBuilder;
import org.eclipse.help.internal.toc.TocFile;
import org.eclipse.help.ui.internal.HelpUIPlugin;
import org.eclipse.ua.tests.help.util.TocModelSerializer;
import org.eclipse.ua.tests.help.util.TocModelSerializerTest;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;
import org.osgi.framework.Bundle;

public class ParserTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(ParserTest.class);
	}
	
	/*
	 * Ensure that org.eclipse.help.ui is started. It contributes extra content
	 * filtering that is used by this test. See UIContentFilterProcessor.
	 */
	protected void setUp() throws Exception {
		HelpUIPlugin.getDefault();
	}

	public void testParser() {
		Collection tocFiles = TocModelSerializerTest.getTocFiles();
		TocBuilder builder = new TocBuilder();
		builder.build(tocFiles);
		Collection builtTocs = builder.getBuiltTocs();
		
		Iterator iter = builtTocs.iterator();
		while (iter.hasNext()) {
			Toc toc = (Toc)iter.next();
			TocFile file = toc.getTocFile();

			Bundle bundle = UserAssistanceTestPlugin.getDefault().getBundle();
			String pluginRoot = bundle.getLocation().substring("update@".length());
			String relativePath = file.getHref();
			
			/*
			 * Only test what's in the toc test data folder.
			 */
			if (relativePath.startsWith("data/help/toc/")) {
				String absolutePath = pluginRoot + relativePath;
				String resultFile = FileUtil.getResultFile(absolutePath); 
	
				try {
					String expected = FileUtil.getContents(bundle, FileUtil.getResultFile(relativePath, true));
					String actual = TocModelSerializer.serialize(toc);
					Assert.assertEquals("Serialized toc model for " + relativePath + "did not match the expected result: " + FileUtil.getResultFile(relativePath), expected, actual);
				}
				catch(IOException e) {
					Assert.fail("An error occured while loading expected result file for TOC at: " + resultFile + ": " + e);
				}
			}
		}
	}
}
