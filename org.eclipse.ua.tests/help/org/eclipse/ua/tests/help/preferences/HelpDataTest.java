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
package org.eclipse.ua.tests.help.preferences;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.internal.HelpData;
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

	public void testHelpData() {
		for (int i=0;i<TEST_DATA.length;++i) {
			String[][] entry = (String[][])TEST_DATA[i];
			String file = entry[0][0];
			List expectedTocOrder = Arrays.asList(entry[1]); 
			Set expectedHiddenTocs = new HashSet(Arrays.asList(entry[2])); 
			Set expectedHiddenIndexes = new HashSet(Arrays.asList(entry[3])); 
			HelpDataTester data = new HelpDataTester(file);
			Assert.assertEquals("Did not get the expected toc order from help data file " + file, expectedTocOrder, data.getTocOrder());
			Assert.assertEquals("Did not get the expected hidden tocs from help data file " + file, expectedHiddenTocs, data.getHiddenTocs());
			Assert.assertEquals("Did not get the expected hidden indexes from help data file " + file, expectedHiddenIndexes, data.getHiddenIndexes());
		}
	}

	private class HelpDataTester extends HelpData {
		
		private String path;
		
		public HelpDataTester(String path) {
			this.path = path;
		}
		
		public InputStream getHelpDataFile(String filePath) throws IOException {
			return UserAssistanceTestPlugin.getDefault().getBundle().getEntry(path).openStream();
		}
	}
}
