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
package org.eclipse.ua.tests.help.util;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;
import org.eclipse.ua.tests.util.XHTMLUtil;

/*
 * A utility for regenerating the _expected.txt files that contain the
 * expected result for the producer content model when serialized.
 * This reads all the TOC content from data/help/producer, passes it
 * through the dynamic content producer, then stores the result in a
 * text file, which is stored in the same directory as the original
 * xhtml file, as <original_name>_expected.txt.
 * 
 * These files are used by the JUnit tests to compare the result with the expected
 * result.
 * 
 * Usage:
 * 
 * 1. Run this test as a JUnit plug-in test.
 * 2. Right-click in "Package Explorer -> Refresh".
 * 
 * The new files should appear.
 */
public class ProducerSerializerTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(ProducerSerializerTest.class);
	}

	public void testGenerateOutput() throws Exception {
		IToc[] tocs = HelpSystem.getTocs();
		for (int i=0;i<tocs.length;++i) {
			// only look for content in data/help/producer
			if (tocs[i].getHref().indexOf("data/help/producer/") != -1) {
				ITopic[] topics = tocs[i].getTopics();
				// only goes one level deep - don't need subtopics here
				for (int j=0;j<topics.length;++j) {
					String href = topics[j].getHref();
					String pluginRoot = UserAssistanceTestPlugin.getDefault().getBundle().getLocation().substring("update@".length());
					String relativePath = href.substring(href.indexOf('/', 1));
					String absolutePath = pluginRoot + relativePath;
					String resultFile = FileUtil.getResultFile(absolutePath);
					
					PrintWriter out = new PrintWriter(new FileOutputStream(resultFile));
					String output = FileUtil.readString(HelpSystem.getHelpContent(href));
					output = XHTMLUtil.removeEnvironmentSpecificContent(output);
					out.print(output);
					out.close();
				}
			}
		}
	}
}
