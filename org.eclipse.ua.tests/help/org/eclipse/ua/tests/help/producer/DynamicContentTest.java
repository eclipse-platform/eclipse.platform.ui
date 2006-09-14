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
package org.eclipse.ua.tests.help.producer;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.xhtml.DynamicXHTMLProcessor;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;
import org.eclipse.ua.tests.util.XHTMLUtil;
import org.osgi.framework.Bundle;

/*
 * Tests the intro parser on valid intro content.
 */
public class DynamicContentTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(DynamicContentTest.class);
	}
	
	public void testDynamicContent() throws Exception {
		IToc[] tocs = HelpSystem.getTocs();
		for (int i=0;i<tocs.length;++i) {
			// only look for content in data/help/producer
			if (tocs[i].getHref().indexOf("data/help/producer/") != -1) {
				ITopic[] topics = tocs[i].getTopics();
				// only goes one level deep - don't need subtopics here
				for (int j=0;j<topics.length;++j) {
					String href = topics[j].getHref();
					String relativePath = href.substring(href.indexOf('/', 1) + 1);
					Bundle bundle = UserAssistanceTestPlugin.getDefault().getBundle();
					
					try {
						String expected = FileUtil.getContents(bundle, FileUtil.getResultFile(relativePath));
						InputStream in = HelpSystem.getHelpContent(href);
						in = DynamicXHTMLProcessor.process(href, in, Platform.getNL(), true);
						String actual = FileUtil.readString(in);
						actual = XHTMLUtil.removeEnvironmentSpecificContent(actual);
						Assert.assertEquals("Processed file " + relativePath + " did not match the expected result", expected, actual);
					}
					catch(IOException e) {
						Assert.fail("An error occured while loading expected result file for producer test at: " + relativePath + ": " + e);
					}
				}
			}
		}
	}
}
