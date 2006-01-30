/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.intro.parser;

import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.ui.internal.HelpUIPlugin;
import org.eclipse.ua.tests.intro.util.IntroModelSerializer;
import org.eclipse.ua.tests.intro.util.IntroModelSerializerTest;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;
import org.eclipse.ui.internal.intro.impl.model.IntroModelRoot;
import org.eclipse.ui.internal.intro.impl.model.loader.ExtensionPointManager;
import org.osgi.framework.Bundle;

/*
 * Tests the intro parser on valid intro content.
 */
public class ValidTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(ValidTest.class);
	}
	
	/*
	 * Ensure that org.eclipse.help.ui is started. It contributes extra content
	 * filtering that is used by this test. See UIContentFilterProcessor.
	 */
	protected void setUp() throws Exception {
		HelpUIPlugin.getDefault();
	}

	/*
	 * Test valid intro content. This goes through all the test intro content (xml files and
	 * xhtml files) and serializes them using the IntroModelSerializer, then compares the result
	 * of the serialization with the expected content (the _serialized.txt files).
	 */
	public void testParserValid() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.intro.config");
		for (int i=0;i<elements.length;++i) {
			/*
			 * Only use the ones from this test plugin.
			 */
			if (elements[i].getDeclaringExtension().getNamespace().equals(UserAssistanceTestPlugin.getDefault().getBundle().getSymbolicName())) {
				String content = elements[i].getAttribute("content");
				String id = elements[i].getAttribute("id");
				Bundle bundle = UserAssistanceTestPlugin.getDefault().getBundle();
				
				IntroModelRoot model = ExtensionPointManager.getInst().getModel(id);
				IntroModelSerializer serializer = new IntroModelSerializer(model);

				/*
				 * Try [filename]_serialized_os_ws_arch.txt. If it's not there, try
				 * [filename]_serialized.txt.
				 * 
				 * We use different files for os/ws/arch combinations in order to test dynamic content,
				 * specifically filtering. Some of the files have filters by os, ws, and arch so the
				 * result is different on each combination.
				 */
				String contents = null;
				try {
					contents = FileUtil.getContents(bundle, IntroModelSerializerTest.getResultFile(content, true));
				}
				catch(Exception e) {
					// didn't find the _serialized_os_ws_arch.txt file, try just _serialized.txt
				}
				if (contents == null) {
					try {
						contents = FileUtil.getContents(bundle, IntroModelSerializerTest.getResultFile(content));
					}
					catch(Exception e) {
						Assert.fail("An error occured while loading expected result file for intro XML for: " + content);
					}
				}
				/*
				 * Do a fuzzy match. Ignore all whitespace then compare. This is to avoid platform
				 * specific newlines, etc.
				 */
				String expected = contents.replaceAll("[ \t\n\r]", "");
				String actual = serializer.toString().replaceAll("[ \t\n\r]", "");;
				Assert.assertEquals("The serialization generated for intro did not match the expected result for: " + id, expected, actual);
				
				Map map = IntroModelSerializerTest.getXHTMLFiles(model);
				Iterator iter = map.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry)iter.next();
					String relativePath = (String)entry.getKey();
					
					/*
					 * Try [filename]_serialized_os_ws_arch.txt. If it's not there, try
					 * [filename]_serialized.txt.
					 * 
					 * We use different files for os/ws/arch combinations in order to test dynamic content,
					 * specifically filtering. Some of the files have filters by os, ws, and arch so the
					 * result is different on each combination.
					 */
					contents = null;
					try {
						contents = FileUtil.getContents(bundle, IntroModelSerializerTest.getResultFile(relativePath, true));
					}
					catch(Exception e) {
						// didn't find the _serialized_os_ws_arch.txt file, try just _serialized.txt
					}
					if (contents == null) {
						try {
							contents = FileUtil.getContents(bundle, IntroModelSerializerTest.getResultFile(relativePath));
						}
						catch(Exception e) {
							Assert.fail("An error occured while loading expected result file for intro XHTML for: " + relativePath);
						}
					}
					
					/*
					 * Do a fuzzy match. Ignore all whitespace then compare.. the XML transformers
					 * seem to add whitespace to the resulting XML string differently.
					 */
					expected = contents.replaceAll("[ \t\n\r]", "");
					actual = ((String)entry.getValue()).replaceAll("[ \t\n\r]", "");;
					Assert.assertEquals("The XHTML generated for intro did not match the expected result for: " + relativePath, expected, actual);
				}
			}
		}
	}
}
