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
package org.eclipse.ua.tests.cheatsheet.util;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.ResourceFinder;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheet;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheetParser;

/*
 * A utility for regenerating the _serialized.txt files that contain the expected
 * output for the cheat sheet content when serialized. This reads all the cheat
 * sheet content in the /data/cheatsheet/valid folder, constructs the cheat sheet
 * model, then serializes the model to a text file, which is stored in the same
 * directory as the xml file, as <original_name>_serialized.txt.
 * 
 * These files are used by the JUnit tests to compare the result with the expected
 * result.
 * 
 * Usage:
 * 
 * 1. Run the "org.eclipse.ua.tests.cheatsheet.util.CheatSheetModelSerializerTest" eclipse application.
 * 2. Right-click in "Package Explorer -> Refresh".
 * 
 * The new files should appear.
 */
public class CheatSheetModelSerializerTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(CheatSheetModelSerializerTest.class);
	}
	
	public void testRunSerializer() {
		URL[] urls = ResourceFinder.findFiles(UserAssistanceTestPlugin.getDefault(), "data/cheatsheet/valid", ".xml", true);
		Assert.assertTrue("Unable to find sample cheat sheets to test parser", urls.length > 0);
		for (int i=0;i<urls.length;++i) {
			CheatSheetParser parser = new CheatSheetParser();
			CheatSheet sheet = (CheatSheet)parser.parse(urls[i]);
			Assert.assertNotNull("Tried parsing a valid cheat sheet but parser returned null: " + urls[i], sheet);
			
			try {
				PrintWriter out = new PrintWriter(new FileOutputStream(getResultFile(urls[i].toString().substring("file:/".length()))));
				out.print(CheatSheetModelSerializer.serialize(sheet));
				out.close();
			}
			catch (Exception e) {
				e.printStackTrace();
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
