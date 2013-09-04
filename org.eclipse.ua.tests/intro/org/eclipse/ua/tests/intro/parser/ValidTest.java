/*******************************************************************************
 *  Copyright (c) 2005, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.intro.parser;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Assert;
import org.osgi.framework.Bundle;

import org.eclipse.help.ui.internal.HelpUIPlugin;
import org.eclipse.ua.tests.intro.util.IntroModelSerializer;
import org.eclipse.ua.tests.intro.util.IntroModelSerializerTest;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.ui.internal.intro.impl.model.IntroModelRoot;
import org.eclipse.ui.internal.intro.impl.model.loader.ExtensionPointManager;

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

	public void testDynamicXHTML() throws IOException {
		singleConfigTest("org.eclipse.ua.tests.intro.config.dynamicXHTML");
	}

	public void testDynamicXML() throws IOException {
		singleConfigTest("org.eclipse.ua.tests.intro.config.dynamicXML");
	}

	public void testAnchors() throws IOException {
		singleConfigTest("org.eclipse.ua.tests.intro.config.anchors");
	}

	public void testMixed() throws IOException {
		singleConfigTest("org.eclipse.ua.tests.intro.config.mixed");
	}
	
	public void testStatic() throws IOException {
		singleConfigTest("org.eclipse.ua.tests.intro.config.static");
	}
	
	/*
	 * Test valid intro content. This goes through the test intro content
	 * (xml files and xhtml files) and serializes it using the
	 * IntroModelSerializer, then compares the result of the serialization
	 * with the expected result (the _expected.txt files).
	 */
	private void singleConfigTest(String configId) throws IOException {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.intro.config");
		for (int i=0;i<elements.length;++i) {
			/*
			 * Only use the ones from this test plugin.
			 */
			if (elements[i].getDeclaringExtension().getContributor().getName().equals(UserAssistanceTestPlugin.getDefault().getBundle().getSymbolicName())) {
				String content = elements[i].getAttribute("content");
				String id = elements[i].getAttribute("id");
				if (id.equals(configId)) {
					for (int x = 0; x < 10; x++) {
						 // Perform 10 times to better detect intermittent ordering bugs
						Bundle bundle = UserAssistanceTestPlugin.getDefault().getBundle();
						
						IntroModelRoot model = ExtensionPointManager.getInst().getModel(id);
						IntroModelSerializer serializer = new IntroModelSerializer(model);
	
						String expected = FileUtil.getContents(bundle, FileUtil.getResultFile(content));
						String actual = serializer.toString();
						Assert.assertEquals("The model parsed for intro did not match the expected result for: " + id, expected, actual);
						
						Map map = IntroModelSerializerTest.getXHTMLFiles(model);
						Iterator iter = map.entrySet().iterator();
						while (iter.hasNext()) {
							Map.Entry entry = (Map.Entry)iter.next();
							String relativePath = (String)entry.getKey();
							
							expected = FileUtil.getContents(bundle, FileUtil.getResultFile(relativePath));
							actual = (String)entry.getValue();
							Assert.assertEquals("The XHTML generated for intro did not match the expected result for: " + relativePath, expected, actual);
						}
					}
					return;
				}				
			}
		}
		fail("Config extension not found");
	}
}
