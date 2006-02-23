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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.toc.Toc;
import org.eclipse.help.internal.toc.TocBuilder;
import org.eclipse.help.internal.toc.TocFile;
import org.eclipse.help.ui.internal.HelpUIPlugin;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;

/*
 * A utility for regenerating the _serialized.txt files that contain the expected
 * output for the TOC content when serialized. This reads all the TOC content from
 * the plugin manifest (for this test plugin only), constructs the model, then
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
 * 
 * Note: Some of the files have os, ws, and arch appended, for example
 * <original_name>_serialized_linux_gtk_x86.txt. These are filtering tests that have
 * filters by os/ws/arch so the result is different on each combination. This test will
 * only generate the _serialized file and will be the one for the current platform. You
 * need to make one copy for each combination and edit the files manually to have the
 * correct content (or generate on each platform).
 */
public class TocModelSerializerTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(TocModelSerializerTest.class);
	}

	/*
	 * Ensure that org.eclipse.help.ui is started. It contributes extra content
	 * filtering that is used by this test. See UIContentFilterProcessor.
	 */
	protected void setUp() throws Exception {
		HelpUIPlugin.getDefault();
	}
	
	public void testRunSerializer() {
		Collection tocFiles = getTocFiles();
		TocBuilder builder = new TocBuilder();
		builder.build(tocFiles);
		Collection builtTocs = builder.getBuiltTocs();
		
		Iterator iter = builtTocs.iterator();
		while (iter.hasNext()) {
			Toc toc = (Toc)iter.next();
			TocFile file = toc.getTocFile();

			String pluginRoot = UserAssistanceTestPlugin.getDefault().getBundle().getLocation().substring("update@".length());
			String relativePath = file.getHref();
			String absolutePath = pluginRoot + relativePath;
			String resultFile = FileUtil.getResultFile(absolutePath); 
			
			try {
				PrintWriter out = new PrintWriter(new FileOutputStream(resultFile));
				out.print(TocModelSerializer.serialize(toc));
				out.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Collection getTocFiles() {
		Collection tocFiles = new ArrayList();
		IExtensionPoint xpt = Platform.getExtensionRegistry().getExtensionPoint(HelpPlugin.PLUGIN_ID, "toc");
		IExtension[] extensions = xpt.getExtensions();
		for (int i=0;i<extensions.length;i++) {
			String pluginId = extensions[i].getContributor().getName();
			if (pluginId.equals("org.eclipse.ua.tests")) {
				IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
				for (int j=0;j<configElements.length;j++) {
					if (configElements[j].getName().equals("toc")) {
						String href = configElements[j].getAttribute("file"); //$NON-NLS-1$
						boolean isPrimary = "true".equals(configElements[j].getAttribute("primary")); //$NON-NLS-1$
						String extraDir = configElements[j].getAttribute("extradir"); //$NON-NLS-1$
						String categoryId = configElements[j].getAttribute("category"); //$NON-NLS-1$
						tocFiles.add(new TocFile(pluginId, href, isPrimary, Platform.getNL(), extraDir, categoryId));
					}
				}
			}
		}
		return tocFiles;
	}
}
