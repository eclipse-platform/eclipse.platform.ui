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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ua.tests.intro.util.IntroModelSerializer;
import org.eclipse.ua.tests.intro.util.IntroModelSerializerTest;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;
import org.eclipse.ua.tests.util.ResourceFinder;
import org.eclipse.ui.internal.intro.impl.model.IntroModelRoot;
import org.eclipse.ui.internal.intro.impl.model.loader.ExtensionPointManager;
import org.osgi.framework.Bundle;

/*
 * Tests the intro parser on valid intro content.
 */
public class PlatformTest extends TestCase {
	
	private static final String SERIALIZED_PATH = "data/intro/platform/serialized.txt";
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(PlatformTest.class);
	}
	
	/*
	 * Test the platform's parsed intro content.
	 */
	public void testModel() {
		final String INTRO_CONFIG_ID = "intro";		
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.intro.config");
		IConfigurationElement element = null;
		for (int i=0;i<elements.length;++i) {
			if (elements[i] != null) {
				IExtension ext = elements[i].getDeclaringExtension();
				if (INTRO_CONFIG_ID.equals(ext.getSimpleIdentifier())) {
					element = elements[i];
				}
			}
		}
		Assert.assertNotNull("Could not find the \"org.eclipse.ui.intro.config\" extension with the id \"" + INTRO_CONFIG_ID + "\".", element);
		
		String pluginRoot = ResourceFinder.findFile(UserAssistanceTestPlugin.getDefault(), "/").toString().substring("file:".length());
		String content = element.getAttribute("content");
		String id = element.getAttribute("id");
		String resultFile = pluginRoot + SERIALIZED_PATH;

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
			int tokenNumber = 0;
			while (tok1.hasMoreTokens() && tok2.hasMoreTokens()) {
				String a = tok1.nextToken();
				String b = tok2.nextToken();
				Assert.assertEquals("Serialized intro content model text for \"" + id + "\" did not match expected result (" + IntroModelSerializerTest.getResultFile(content) + "). First difference occured on token " + tokenNumber + ".", a, b);
				++tokenNumber;
			}
		}
		catch(Exception e) {
			Assert.fail("An error occured while loading expected result file for intro at: " + resultFile);
		}
	}
	
	/*
	 * Some extensions run samples that involve executing code. Check to make sure
	 * that the classes exist and can be instantiated.
	 */
	public void testClasses() {
		String pluginRoot = ResourceFinder.findFile(UserAssistanceTestPlugin.getDefault(), "/").toString().substring("file:".length());
		String resultFile = pluginRoot + SERIALIZED_PATH;
		
		try {
			String contents = FileUtil.getContents(resultFile);
			StringTokenizer tok = new StringTokenizer(contents);
			while (tok.hasMoreTokens()) {
				String next = tok.nextToken();
				if (next.startsWith("http://org.eclipse.ui.intro/runAction?")) {
					Map map = createMap(next.substring("http://org.eclipse.ui.intro/runAction?".length()));
					Assert.assertTrue("The runAction was missing the class attribute: " + next, map.containsKey("class"));
					Assert.assertTrue("The runAction was missing the pluginId attribute: " + next, map.containsKey("pluginId"));
					
					String clazz = (String)map.get("class");
					String pluginId = (String)map.get("pluginId");
					
					Bundle bundle = Platform.getBundle(pluginId);
					Assert.assertNotNull("The plugin referenced in one of the platform's intro runAction URLs (" + next + ") was not found: " + pluginId, bundle);
					
					try {
						Class c = bundle.loadClass(clazz);
						c.newInstance();
					}
					catch (ClassNotFoundException e) {
						Assert.fail("One of the classes in the platform's intro runActions URLs was not found: " + clazz + " in plugin: " + pluginId);
					}
					catch (InstantiationException e) {
						Assert.fail("One of the classes in the platform's intro runActions URLs could not be instantiated: " + clazz + " in plugin: " + pluginId);
					}
					catch (IllegalAccessException e) {
						Assert.fail("One of the classes in the platform's intro runActions URLs could not be accessed (is it public?): " + clazz + " in plugin: " + pluginId);
					}
				}
			}
		}
		catch (IOException e) {
			Assert.fail("An IOException occured while reading platform's serialized.txt file");
		}
	}
	
	/*
	 * Generates a map from the given string of the form:
	 * "key1=value1&key2=value2&..."  (i.e. URL parameters)
	 */
	private static Map createMap(String args) {
		Map map = new HashMap();
		StringTokenizer tok2 = new StringTokenizer(args, "&");
		while (tok2.hasMoreTokens()) {
			String arg = tok2.nextToken();
			int separator = arg.indexOf('=');
			map.put(arg.substring(0, separator), arg.substring(separator + 1));
		}
		return map;
	}
}
