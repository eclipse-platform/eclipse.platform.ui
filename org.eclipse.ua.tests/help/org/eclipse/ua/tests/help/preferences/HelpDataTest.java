/*******************************************************************************
 *  Copyright (c) 2006, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.preferences;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.internal.HelpData;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;

/*
 * Tests the help data ordering of tocs and hiding tocs, indexes, etc.
 */
public class HelpDataTest extends TestCase {

	private static final String[][][] TEST_DATA = new String[][][] {
		{ { "data/help/preferences/helpData1.xml" }, { "toc1", "category1", "toc2", "toc3" }, { "toc4", "category2" }, { "index1", "index2" } },
		{ { "data/help/preferences/helpData2.xml" }, { }, { }, { } },
		{ { "data/help/preferences/helpData3.xml" }, { }, { }, { } },
	};
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(HelpDataTest.class);
	}

	private String baseTocsPreference;
	private String ignoredTocsPreference;
	private String ignoredIndexesPreference;
	

	protected void setUp() throws Exception {
		baseTocsPreference = Platform.getPreferencesService().getString
	        (HelpPlugin.PLUGIN_ID, HelpPlugin.BASE_TOCS_KEY, "", null);
		ignoredTocsPreference = Platform.getPreferencesService().getString
            (HelpPlugin.PLUGIN_ID, HelpPlugin.IGNORED_TOCS_KEY, "", null);
		ignoredIndexesPreference = Platform.getPreferencesService().getString
            (HelpPlugin.PLUGIN_ID, HelpPlugin.IGNORED_INDEXES_KEY, "", null);
		setBaseTocs("");
		setIgnoredTocs("");
		setIgnoredIndexes("");
	}
	
	protected void tearDown() throws Exception {
		setBaseTocs(baseTocsPreference);
		setIgnoredTocs(ignoredTocsPreference);
		setIgnoredIndexes(ignoredIndexesPreference);
	}

	private void setBaseTocs(String value) {
	    IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(HelpPlugin.PLUGIN_ID);
	    pref.put(HelpPlugin.BASE_TOCS_KEY, value);
	}
	
	private void setIgnoredTocs(String value) {
	    IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(HelpPlugin.PLUGIN_ID);
	    pref.put(HelpPlugin.IGNORED_TOCS_KEY, value);
	}
	
	private void setIgnoredIndexes(String value) {
	    IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(HelpPlugin.PLUGIN_ID);
	    pref.put(HelpPlugin.IGNORED_INDEXES_KEY, value);
	}

	public void testHelpData() {
		for (int i=0;i<TEST_DATA.length;++i) {
			String[][] entry = (String[][])TEST_DATA[i];
			String file = entry[0][0];
			List expectedTocOrder = Arrays.asList(entry[1]); 
			@SuppressWarnings("unchecked")
			Set expectedHiddenTocs = new HashSet(Arrays.asList(entry[2])); 
			@SuppressWarnings("unchecked")
			Set expectedHiddenIndexes = new HashSet(Arrays.asList(entry[3]));
			URL url = UserAssistanceTestPlugin.getDefault().getBundle().getEntry(file);
			HelpData data = new HelpData(url);
			Assert.assertEquals("Did not get the expected toc order from help data file " + file, expectedTocOrder, data.getTocOrder());
			Assert.assertEquals("Did not get the expected hidden tocs from help data file " + file, expectedHiddenTocs, data.getHiddenTocs());
			Assert.assertEquals("Did not get the expected hidden indexes from help data file " + file, expectedHiddenIndexes, data.getHiddenIndexes());
		}
	}

	public void testNullUrl() {
		HelpData data = new HelpData(null);
		assertEquals(0, data.getTocOrder().size());
		assertEquals(0, data.getHiddenTocs().size());
		assertEquals(0, data.getHiddenIndexes().size());
		assertTrue(data.isSortOthers());
	}
	
	public void testNullUrlWithBaseTocs() {
		HelpData data = new HelpData(null);
		setBaseTocs("/a/b.xml,/c/d.xml");
		List tocOrder = data.getTocOrder();
		assertEquals(2, tocOrder.size());
		assertEquals("/a/b.xml", tocOrder.get(0));
		assertEquals("/c/d.xml", tocOrder.get(1));
		assertEquals(0, data.getHiddenTocs().size());
		assertEquals(0, data.getHiddenIndexes().size());
		assertTrue(data.isSortOthers());
	}
	
	public void testNullUrlWithHiddenTocs() {
		HelpData data = new HelpData(null);
		setIgnoredTocs("/a/b.xml,/c/d.xml");
		assertEquals(0, data.getTocOrder().size());
		Set hiddenTocs = data.getHiddenTocs();
		assertEquals(2, hiddenTocs.size());
		assertTrue(hiddenTocs.contains("/a/b.xml"));
		assertTrue(hiddenTocs.contains("/c/d.xml"));
		assertEquals(0, data.getHiddenIndexes().size());
		assertTrue(data.isSortOthers());
	}
	
	public void testNullUrlWithHiddenIndexes() {
		HelpData data = new HelpData(null);
		setIgnoredIndexes("/a/b.xml,/c/d.xml");
		assertEquals(0, data.getTocOrder().size());
		assertEquals(0, data.getHiddenTocs().size());
		Set hiddenIndexes = data.getHiddenIndexes();
		assertEquals(2, hiddenIndexes.size());
		assertTrue(hiddenIndexes.contains("/a/b.xml"));
		assertTrue(hiddenIndexes.contains("/c/d.xml"));
		assertTrue(data.isSortOthers());
	}
}
