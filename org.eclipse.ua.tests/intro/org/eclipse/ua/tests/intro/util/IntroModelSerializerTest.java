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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.ui.internal.HelpUIPlugin;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.ResourceFinder;
import org.eclipse.ui.internal.intro.impl.model.AbstractIntroPage;
import org.eclipse.ui.internal.intro.impl.model.IntroHomePage;
import org.eclipse.ui.internal.intro.impl.model.IntroModelRoot;
import org.eclipse.ui.internal.intro.impl.model.IntroPage;
import org.eclipse.ui.internal.intro.impl.model.loader.ExtensionPointManager;
import org.eclipse.ui.internal.intro.impl.presentations.BrowserIntroPartImplementation;
import org.eclipse.ui.intro.config.IIntroContentProvider;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;

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
 * 
 * Note: Some of the files have os, ws, and arch appended, for example
 * <original_name>_serialized_linux_gtk_x86.txt. These are filtering tests that have
 * filters by os/ws/arch so the result is different on each combination. This test will
 * only generate the _serialized file and will be the one for the current platform. You
 * need to make one copy for each combination and edit the files manually to have the
 * correct content (or generate on each platform).
 */
public class IntroModelSerializerTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(IntroModelSerializerTest.class);
	}
	
	/*
	 * Ensure that org.eclipse.help.ui is started. It contributes extra content
	 * filtering that is used by this test. See UIContentFilterProcessor.
	 */
	protected void setUp() throws Exception {
		HelpUIPlugin.getDefault();
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
		 * Serialize the test intros.
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

				/*
				 * First do the intro XML files.
				 */
				IntroModelRoot model = ExtensionPointManager.getInst().getModel(id);
				IntroModelSerializer serializer = new IntroModelSerializer(model);
				
				try {
					String file = getResultFile(pluginRoot + content);
					PrintWriter out = new PrintWriter(new FileOutputStream(file));
					out.print(serializer.toString());
					out.close();
				}
				catch(FileNotFoundException e) {
					e.printStackTrace();
				}
				
				/*
				 * Now do the intro XHTML files. Find all the XHTML files referenced
				 * from the model.
				 */
				Map map = getXHTMLFiles(model);
				Iterator iter = map.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry)iter.next();
					try {
						String file = getResultFile(pluginRoot + entry.getKey());
						PrintWriter out = new PrintWriter(new FileOutputStream(file));
						out.print((String)entry.getValue());
						out.close();
					}
					catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/*
	 * Generates a filename with path to the result file that will be generated
	 * for the intro xml referred to by the string.
	 */
	public static String getResultFile(String in) {
		return getResultFile(in, false);
	}

	/*
	 * Same as above, but gives the option of appending os, ws, and arch. For example,
	 * myfile_serialized_macosx_carbon_ppc.txt.
	 */
	public static String getResultFile(String in, boolean env) {
		StringBuffer buf = new StringBuffer();
		buf.append(in.substring(0, in.lastIndexOf('.')) + "_serialized");
		if (env) {
			buf.append('_');
			buf.append(Platform.getOS());
			buf.append('_');
			buf.append(Platform.getWS());
			buf.append('_');
			buf.append(Platform.getOSArch());
		}
		buf.append(".txt");
		return buf.toString();
	}

	/*
	 * Search through the given model and find all XHTML files referred to by the model.
	 * Also loads the contents of the XHTML files. The result is a mapping of filenames relative
	 * to the test plugin to Strings, the contents of the XHTML files.
	 */
	public static Map getXHTMLFiles(IntroModelRoot model) {
		Map map = new HashMap();
		Collection pages = new ArrayList();
		IntroHomePage home = model.getHomePage();
		if (home.isXHTMLPage()) {
			pages.add(home);
		}
		IntroPage[] otherPages = model.getPages();
		for (int i=0;i<otherPages.length;++i) {
			if (otherPages[i].isXHTMLPage()) {
				pages.add(otherPages[i]);
			}
		}
		Iterator iter = pages.iterator();
		while (iter.hasNext()) {
			AbstractIntroPage page = (AbstractIntroPage)iter.next();
			BrowserIntroPartImplementation impl = new BrowserIntroPartImplementation();
			String xhtml = impl.generateXHTMLPage(page, new IIntroContentProviderSite() {
				public void reflow(IIntroContentProvider provider, boolean incremental) {
					// dummy site
				}
			});
			xhtml = removeEnvironmentSpecificContent(xhtml);
			map.put(page.getInitialBase() + page.getRawContent(), xhtml);
		}
		return map;
	}
	
	/*
	 * Some of the XHTML content is environment-specific. This means it changes depending on
	 * the test machine, location on filesystem, etc. This content is not important for this
	 * test so just strip it out before comparing the serializations.
	 */
	private static String removeEnvironmentSpecificContent(String xhtml) {
		/*
		 * The base tag is added before showing in browser. It contains an absolute path
		 * in filesystem.
		 */
		xhtml = xhtml.replaceAll("<base href=\".*\" />", "");

		/*
		 * The order of the params for the meta tag comes out differently on different platforms.
		 * I'm not sure why, and why just this tag. We don't care about this one for our tests anyway,
		 * so just strip it.
		 */
		xhtml = xhtml.replaceAll("<meta .*/>", "");
		return xhtml;
	}
}
