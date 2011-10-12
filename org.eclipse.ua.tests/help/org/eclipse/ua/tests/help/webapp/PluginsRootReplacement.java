/*******************************************************************************
 *  Copyright (c) 2009, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.help.internal.webapp.servlet.PluginsRootResolvingStream;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.ResourceFinder;

/**
 * Test for replacing PLUGINS_ROOT with a relative path
 */

public class PluginsRootReplacement extends TestCase {

	public void testEmpty() {
	    final String input = "";
		checkFilter(input, input);
	}

	public void testNoMatch() {
	    final String input = "<HEAD><HEAD/>";
		checkFilter(input, input);
	}

	public void testPartialMatch() {
	    final String input = "<A href = \"PLUGINS\">";
		checkFilter(input, input);
	}
	
	public void testEndsUnmatched() {
	    final String input = "<A href = \"PLUGIN";
		checkFilter(input, input);
	}

	public void testNotAtStart() {
	    final String input = "<A href = \"../PLUGINS_ROOT/plugin/a.html\">";
		checkFilter(input, input);
	}

	public void testAtStart() {
	    final String input = "<A href = \"PLUGINS_ROOT/plugin/a.html\">";
	    final String expected = "<A href = \"../plugin/a.html\">";
		checkFilter(input, expected);
	}

	public void testSecondArg() {
	    final String input = "<A alt=\"alt\" href = \"PLUGINS_ROOT/plugin/a.html\">";
	    final String expected = "<A alt=\"alt\" href = \"../plugin/a.html\">";
		checkFilter(input, expected);
	}
	

	public void testMultipleMatches() {
	    final String input = "<A href = \"PLUGINS_ROOT/plugin/a.html\"><A href = \"PLUGINS_ROOT/plugin/b.html\">";
	    final String expected = "<A href = \"../plugin/a.html\"><A href = \"../plugin/b.html\">";
		checkFilter(input, expected);
	}

	private void checkFilter(final String input, final String expected) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		OutputStream filteredOutput = new PluginsRootResolvingStream(output, null, "../");
		try {
			filteredOutput.write(input.getBytes());
			filteredOutput.close();
		} catch (IOException e) {
			fail("IO Exception");
		}
		assertEquals(expected, output.toString());
	}

	public void testHelpContentActiveAction() throws IOException {
		String filename = "ua_help_content_active_action.htm";
		checkFileContentsPreserved(filename);
	}
	
	public void testHelpContentActiveDebug() throws IOException {
		String filename = "ua_help_content_active_debug.htm";
		checkFileContentsPreserved(filename);
	}

	public void testHelpContentActiveInvoke() throws IOException {
		String filename = "ua_help_content_active_invoke.htm";
		checkFileContentsPreserved(filename);
	}

	public void testHelpContentActive() throws IOException {
		String filename = "ua_help_content_active.htm";
		checkFileContentsPreserved(filename);
	}
	
	public void testHelpContentManifest() throws IOException {
		String filename = "ua_help_content_manifest.htm";
		checkFileContentsPreserved(filename);
	}
	
	public void testHelpContentProcess() throws IOException {
		String filename = "ua_help_content_process.htm";
		checkFileContentsPreserved(filename);
	}

	public void testHelpContentNested() throws IOException {
		String filename = "ua_help_content_nested.htm";
		checkFileContentsPreserved(filename);
	}
	
	public void testHelpContentToc() throws IOException {
		String filename = "ua_help_content_toc.htm";
		checkFileContentsPreserved(filename);
	}
	
	public void testHelpContentXhtml() throws IOException {
		String filename = "ua_help_content_xhtml.htm";
		checkFileContentsPreserved(filename);
	}
	
	public void testHelpContent() throws IOException {
		String filename = "ua_help_content.htm";
		checkFileContentsPreserved(filename);
	}
	
	/*
	 * Test a pages from the help system to make sure there is no corruption
	 * when it is transformed.
	 */
	private void checkFileContentsPreserved(String filename) throws IOException {
		URL testURL = ResourceFinder.findFile(UserAssistanceTestPlugin.getDefault(),
		"/data/help/performance/search/" + filename);
		assertNotNull(testURL);
		InputStream input = testURL.openStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		while (input.available() > 0) {
			int next = input.read();
			output.write(next);
		}
		String data = output.toString();
		checkFilter(data, data);
		input.close();
		output.close();
	}
	
}
