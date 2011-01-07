/*******************************************************************************
 *  Copyright (c) 2005, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.intro.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.eclipse.ua.tests.util.FileUtil;
import org.eclipse.ua.tests.util.XHTMLUtil;
import org.eclipse.ui.internal.intro.impl.model.AbstractIntroPage;
import org.eclipse.ui.internal.intro.impl.model.IntroHomePage;
import org.eclipse.ui.internal.intro.impl.model.IntroModelRoot;
import org.eclipse.ui.internal.intro.impl.model.IntroPage;
import org.eclipse.ui.internal.intro.impl.model.loader.ExtensionPointManager;
import org.eclipse.ui.internal.intro.impl.presentations.BrowserIntroPartImplementation;
import org.eclipse.ui.intro.config.IIntroContentProvider;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;

/*
 * A utility for regenerating the _expected.txt files that contain the expected
 * output for the intro model when serialized. This reads all the intro content from
 * the plugin manifest (for this test plugin only), constructs the intro model, then
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
public class IntroModelSerializerTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(IntroModelSerializerTest.class);
	}
	
	/*
	 * Ensure that org.eclipse.help.ui is started. It contributes extra
	 * content filtering that is used by this test. See
	 * UIContentFilterProcessor.
	 */
	protected void setUp() throws Exception {
		HelpUIPlugin.getDefault();
	}
	
	public void testRunSerializer() throws FileNotFoundException {
		/*
		 * Serialize the test intros.
		 */
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.intro.config");
		for (int i=0;i<elements.length;++i) {
			/*
			 * Only use the ones from this test plugin.
			 */
			if (elements[i].getDeclaringExtension().getContributor().getName().equals(UserAssistanceTestPlugin.getDefault().getBundle().getSymbolicName())) {
				String pluginRoot = UserAssistanceTestPlugin.getDefault().getBundle().getLocation().substring("update@".length());
				String content = elements[i].getAttribute("content");
				String id = elements[i].getAttribute("id");

				/*
				 * First do the intro XML files.
				 */
				IntroModelRoot model = ExtensionPointManager.getInst().getModel(id);
				IntroModelSerializer serializer = new IntroModelSerializer(model);
				
				String file = FileUtil.getResultFile(pluginRoot + content);
				PrintWriter out = new PrintWriter(new FileOutputStream(file));
				out.print(serializer.toString());
				out.close();
				
				/*
				 * Now do the intro XHTML files. Find all the XHTML files
				 * referenced from the model.
				 */
				Map map = getXHTMLFiles(model);
				Iterator iter = map.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry)iter.next();
					file = FileUtil.getResultFile(pluginRoot + entry.getKey());
					out = new PrintWriter(new FileOutputStream(file));
					out.print((String)entry.getValue());
					out.close();
				}
			}
		}
	}

	/*
	 * Search through the given model and find all XHTML files referred to by
	 * the model. Also loads the contents of the XHTML files. The result is a
	 * mapping of filenames relative to the test plugin to Strings, the
	 * contents of the XHTML files.
	 */
	public static Map getXHTMLFiles(IntroModelRoot model) {
		Map map = new HashMap();
		Collection<AbstractIntroPage> pages = new ArrayList<AbstractIntroPage>();
		IntroHomePage home = model.getRootPage();
		if (home.isXHTMLPage()) {
			pages.add(home);
		}
		IntroPage[] otherPages = model.getPages();
		for (int i=0;i<otherPages.length;++i) {
			if (otherPages[i].isXHTMLPage()) {
				pages.add(otherPages[i]);
			}
		}
		Iterator<AbstractIntroPage> iter = pages.iterator();
		while (iter.hasNext()) {
			AbstractIntroPage page = iter.next();
			BrowserIntroPartImplementation impl = new BrowserIntroPartImplementation();
			String xhtml = impl.generateXHTMLPage(page, new IIntroContentProviderSite() {
				public void reflow(IIntroContentProvider provider, boolean incremental) {
					// dummy site
				}
			});
			xhtml = XHTMLUtil.removeEnvironmentSpecificContent(xhtml);
			// filter windows-specific newline
			xhtml = xhtml.replaceAll("\r", "");
			// ignore all beginning and ending whitespace
			xhtml = xhtml.trim();
			map.put(page.getInitialBase() + page.getRawContent(), xhtml);
		}
		return map;
	}
}
