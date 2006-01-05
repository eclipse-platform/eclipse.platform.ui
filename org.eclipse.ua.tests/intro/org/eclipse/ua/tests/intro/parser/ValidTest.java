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

import java.util.StringTokenizer;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ua.tests.intro.util.IntroModelSerializer;
import org.eclipse.ua.tests.intro.util.IntroModelSerializerTest;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;
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
	 * Test valid intro content.
	 */
	public void testParserValid() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.intro.config");
		for (int i=0;i<elements.length;++i) {
			/*
			 * Only use the ones from this test plugin.
			 */
			if (elements[i].getDeclaringExtension().getNamespace().equals(UserAssistanceTestPlugin.getDefault().getBundle().getSymbolicName())) {
				String pluginRoot = UserAssistanceTestPlugin.getDefault().getBundle().getLocation().substring("update@".length());
				String content = elements[i].getAttribute("content");
				String id = elements[i].getAttribute("id");
				String resultFile = IntroModelSerializerTest.getResultFile(pluginRoot + content);

				IntroModelRoot model = ExtensionPointManager.getInst().getModel(id);
				IntroModelSerializer serializer = new IntroModelSerializer(model);
				
				try {
					String expected = FileUtil.getContents(resultFile);
					String actual = serializer.toString();
					
					StringTokenizer tok1 = new StringTokenizer(expected, "\n");
					StringTokenizer tok2 = new StringTokenizer(actual, "\n");
					
					/*
					 * Report the line number and line text where it didn't match,
					 * as well as the extension id and expected results file.
					 */
					int lineNumber = 0;
					while (tok1.hasMoreTokens() && tok2.hasMoreTokens()) {
						String a = tok1.nextToken();
						String b = tok2.nextToken();
						Assert.assertEquals("Serialized intro content model text for \"" + id + "\" did not match expected result (" + IntroModelSerializerTest.getResultFile(content) + "). First difference occured on line " + lineNumber + ".", a, b);
						++lineNumber;
					}
				}
				catch(Exception e) {
					Assert.fail("An error occured while loading expected result file for intro at: " + resultFile);
				}
			}
		}
	}
}
