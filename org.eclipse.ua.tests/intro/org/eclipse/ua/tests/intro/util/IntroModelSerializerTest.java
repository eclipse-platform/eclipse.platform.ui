/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.intro.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.ResourceFinder;
import org.eclipse.ui.internal.intro.impl.model.IntroModelRoot;
import org.eclipse.ui.internal.intro.impl.model.loader.ExtensionPointManager;

/*
 * A utility for regenerating the _serialized.txt files that contain the expected
 * output for the intro content when serialized. This reads all the intro content from
 * the plugin manifest (for this test plugin only), constructs the intro model, then
 * serializes the model to a text file, which is stored in the same directory as the
 * intro xml file, as <original_name>_serialized.txt.
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
public class IntroModelSerializerTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(IntroModelSerializerTest.class);
	}
	
	public void testRunSerializer() {
		/*
		 * Serialize the SDK's intro.
		 */
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.intro.config");
		for (int i=0;i<elements.length;++i) {
			String id = elements[i].getAttribute("id");
			if ("org.eclipse.platform.introConfig".equals(id)) {
				String pluginRoot = ResourceFinder.findFile(UserAssistanceTestPlugin.getDefault(), "/").toString().substring("file:".length());

				IntroModelRoot model = ExtensionPointManager.getInst().getModel(id);
				IntroModelSerializer serializer = new IntroModelSerializer(model);
				
				try {
					PrintWriter out = new PrintWriter(new FileOutputStream(pluginRoot + "data/intro/platform/serialized.txt"));
					out.print(serializer.toString());
					out.close();
				}
				catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				break;
			}
		}

		/*
		 * Serialize the test intro.
		 */
		elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.intro.config");
		for (int i=0;i<elements.length;++i) {
			/*
			 * Only use the ones from this test plugin.
			 */
			if (elements[i].getDeclaringExtension().getNamespace().equals(UserAssistanceTestPlugin.getDefault().getBundle().getSymbolicName())) {
				String pluginRoot = UserAssistanceTestPlugin.getDefault().getBundle().getLocation().substring("update@".length());
				String content = elements[i].getAttribute("content");
				String id = elements[i].getAttribute("id");

				IntroModelRoot model = ExtensionPointManager.getInst().getModel(id);
				IntroModelSerializer serializer = new IntroModelSerializer(model);
				
				try {
					PrintWriter out = new PrintWriter(new FileOutputStream(getResultFile(pluginRoot + content)));
					out.print(serializer.toString());
					out.close();
				}
				catch(FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/*
	 * Generates a filename with path to the result file that will be generated
	 * for the intro xml referred to by the string.
	 */
	public static String getResultFile(String in) {
		return in.substring(0, in.lastIndexOf('.')) + "_serialized.txt";
	}
}
