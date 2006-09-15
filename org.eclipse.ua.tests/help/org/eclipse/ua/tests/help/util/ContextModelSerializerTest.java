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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.context.ContextsBuilder;
import org.eclipse.help.internal.context.ContextsFile;
import org.eclipse.help.internal.context.PluginContexts;
import org.eclipse.help.ui.internal.HelpUIPlugin;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;

/*
 * A utility for regenerating the _expected.txt files that contain the expected
 * result for the context model when serialized. This reads all the context content
 * from the plugin manifest (for this test plugin only), constructs the model, then
 * serializes the model to a text file, which is stored in the same directory as the
 * intro xml file, as <original_name>_expected.txt.
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
public class ContextModelSerializerTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(ContextModelSerializerTest.class);
	}

	/*
	 * Ensure that org.eclipse.help.ui is started. It contributes extra content
	 * filtering that is used by this test. See UIContentFilterProcessor.
	 */
	protected void setUp() throws Exception {
		HelpUIPlugin.getDefault();
	}
	
	public void testRunSerializer() throws IOException {
		Iterator iter = getContextFiles().iterator();
		while (iter.hasNext()) {
			ContextsFile file = (ContextsFile)iter.next();
			PluginContexts contexts = new PluginContexts();
			ContextsBuilder builder = new ContextsBuilder(contexts);
			builder.build(file, Platform.getNL());
			
			String pluginRoot = UserAssistanceTestPlugin.getDefault().getBundle().getLocation().substring("update@".length());
			String relativePath = file.getHref();
			String absolutePath = pluginRoot + relativePath;
			String resultFile = FileUtil.getResultFile(absolutePath); 
			
			PrintWriter out = new PrintWriter(new FileOutputStream(resultFile));
			out.print(ContextModelSerializer.serialize(contexts));
			out.close();
		}
	}
	
	/**
	 * Gets all context files from the extension with id "tests.context". We only want
	 * to consider these for our tests.
	 * 
	 * @return all ContextsFile objects contributed for tests
	 */
	public static Collection getContextFiles() {
		Collection contextFiles = new ArrayList();
		IExtension extension = Platform.getExtensionRegistry().getExtension(UserAssistanceTestPlugin.getPluginId() + ".contextTest");
		IConfigurationElement[] configElements = extension.getConfigurationElements();
		for (int j=0;j<configElements.length;j++) {
			if (configElements[j].getName().equals("contexts")) {
				String href = configElements[j].getAttribute("file"); //$NON-NLS-1$
				contextFiles.add(new ContextsFile(UserAssistanceTestPlugin.getPluginId(), href, null));
			}
		}
		return contextFiles;
	}
}
