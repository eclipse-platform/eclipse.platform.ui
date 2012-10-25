/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipe.debug.tests.launching;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.tests.TestsPlugin;
import org.eclipse.osgi.service.environment.Constants;

/**
 * Tests {@link org.eclipse.debug.core.DebugPlugin#parseArguments(String)} and
 * {@link org.eclipse.debug.core.DebugPlugin#renderArguments(String[], int[])}.
 */
public class ArgumentParsingTests extends TestCase {
	
	private void execute1Arg(String cmdLine) throws Exception {
		execute1Arg(cmdLine, cmdLine);
	}
	
	private void execute1Arg(String cmdLine, String argParsed) throws Exception {
		execute1Arg(cmdLine, argParsed, cmdLine);
	}
	
	private void execute1Arg(String cmdLine, String argParsed, String rendered) throws Exception {
		execute("a " + cmdLine + " b", new String[] { "a", argParsed, "b" }, "a " + rendered + " b");
	}
	
	private static void execute(String commandLine, String[] expectedArgs) throws Exception {
		execute(commandLine, expectedArgs, commandLine);
	}
	
	private static void execute(String commandLine, String[] expectedArgs, String expectedRendered) throws Exception {
		String[] arguments = DebugPlugin.parseArguments(commandLine);
		assertEquals("unexpected parseArguments result;",
				Arrays.asList(expectedArgs).toString(),
				Arrays.asList(arguments).toString());
		
		runCommandLine(commandLine, arguments);
		
		String rendered = DebugPlugin.renderArguments(arguments, null);
		assertEquals("unexpected renderArguments result;", expectedRendered, rendered);
		
		if (!commandLine.equals(rendered)) {
			String[] arguments2 = DebugPlugin.parseArguments(rendered);
			assertEquals("parsing rendered command line doesn't yield original arguments;",
					Arrays.asList(expectedArgs).toString(),
					Arrays.asList(arguments2).toString());
		}
		
	}

	private static void runCommandLine(String commandLine, String[] arguments) throws IOException,
			URISyntaxException, CoreException {
		URL classPathUrl = FileLocator.find(TestsPlugin.getDefault().getBundle(), new Path("bin/"), null);
		if (classPathUrl == null) { // not running from the workspace, but from the built bundle
			classPathUrl = FileLocator.find(TestsPlugin.getDefault().getBundle(), Path.ROOT, null);
		}
		classPathUrl = FileLocator.toFileURL(classPathUrl);
		File classPathFile = URIUtil.toFile(URIUtil.toURI(classPathUrl));
		
		String[] execArgs= new String[arguments.length + 4];
		execArgs[0]= new Path(System.getProperty("java.home")).append("bin/java").toOSString();
		execArgs[1]= "-cp";
		execArgs[2]= classPathFile.getAbsolutePath();
		execArgs[3]= ArgumentsPrinter.class.getName();
		System.arraycopy(arguments, 0, execArgs, 4, arguments.length);
		
		ArrayList resultArgs = runCommandLine(execArgs);
		
		assertEquals("unexpected exec result;",
				Arrays.asList(arguments).toString(),
				resultArgs.toString());
		
		if (! Platform.getOS().equals(Constants.OS_WIN32)) {
			execArgs = new String[] { "sh", "-c", execArgs[0] + " " + execArgs[1] + " " + execArgs[2] + " " + execArgs[3] + " " + commandLine };
			resultArgs = runCommandLine(execArgs);
			assertEquals("unexpected sh exec result;",
					Arrays.asList(arguments).toString(),
					resultArgs.toString());
		}
	}

	private static ArrayList runCommandLine(String[] execArgs)
			throws CoreException, IOException {
		execArgs = quoteWindowsArgs(execArgs);
		Process process = DebugPlugin.exec(execArgs, null);
		BufferedReader procOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
		
		ArrayList procArgs= new ArrayList();
		String procArg;
		while ((procArg = procOut.readLine()) != null) {
			procArgs.add(procArg);
		}
		return procArgs;
	}
	
	private static String[] quoteWindowsArgs(String[] cmdLine) {
		// see https://bugs.eclipse.org/387504#c13 , workaround for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6511002
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			String[] winCmdLine = new String[cmdLine.length];
			for (int i = 0; i < cmdLine.length; i++) {
				winCmdLine[i] = winQuote(cmdLine[i]);
			}
			cmdLine = winCmdLine;
		}
		return cmdLine;
	}
	
	
	private static boolean needsQuoting(String s) {
		int len = s.length();
		if (len == 0) // empty string has to be quoted
			return true;
		for (int i = 0; i < len; i++) {
			switch (s.charAt(i)) {
				case ' ': case '\t': case '\\': case '"':
					return true;
			}
		}
		return false;
	}

	private static String winQuote(String s) {
		if (! needsQuoting(s))
			return s;
		s = s.replaceAll("([\\\\]*)\"", "$1$1\\\\\""); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.replaceAll("([\\\\]*)\\z", "$1$1"); //$NON-NLS-1$ //$NON-NLS-2$
		return "\"" + s + "\""; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// -- tests:

	public void testEmpty() throws Exception {
		execute("", new String[0]);
	}

	public void test1arg() throws Exception {
		execute("a", new String[] { "a" });
	}

	public void test2arg() throws Exception {
		execute("a b", new String[] { "a", "b" });
	}

	public void test100arg() throws Exception {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < 100; i++)
			buf.append("a ");
		String[] args = new String[100];
		Arrays.fill(args, "a");
		execute(buf.toString(), args, buf.toString().trim());
	}
	
	public void testEscape() throws Exception {
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			execute1Arg("\\1");
		} else {
			execute1Arg("\\1", "1", "1");
		}
	}
	
	public void testEscapeDoubleQuote1() throws Exception {
		execute1Arg("\\\"", "\"", "\\\"");
	}
	
	public void testEscapeDoubleQuote2() throws Exception {
		execute1Arg("arg=\\\"bla\\\"", "arg=\"bla\"", "arg=\\\"bla\\\"");
	}
	
	public void testDoubleQuoted1() throws Exception {
		execute1Arg("\"1 2\"", "1 2");
	}

	public void testDoubleQuoted2() throws Exception {
		execute1Arg("\"1\"", "1", "1");
	}
	
	public void testDoubleQuoted3() throws Exception {
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
//			execute1Arg("\"\"", "", "\"\""); // would be correct, but ProcessImpl is buggy on Windows JDKs
			execute1Arg("\"\"");
		} else {
			execute1Arg("\"\"", "", "\"\"");
		}
	}
	
	public void testDoubleQuoted4() throws Exception {
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			execute1Arg("\"\"\"\"", "\"", "\\\"");
		} else {
			execute1Arg("\"\"\"\"", "", "\"\"");
		}
	}
	
	public void testDoubleQuoted5() throws Exception {
		execute1Arg("ab\"cd\"ef\"gh\"", "abcdefgh", "abcdefgh");
	}
	
	public void testDoubleQuotedWithSpace1() throws Exception {
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			execute1Arg("\"\"\"1\"\" 2\"", "\"1\" 2", "\"\\\"1\\\" 2\"");
		} else {
			execute1Arg("\"\"\"1\"\" 2\"", "1 2", "\"1 2\"");
		}
	}
	
	public void testDoubleQuotedWithSpace2() throws Exception {
		execute1Arg("\"\\\"1\\\" 2\"", "\"1\" 2");
	}
	
	public void testSingleQuoted1() throws Exception {
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			execute("'1 2'", new String[] { "'1", "2'" });
		} else {
			execute("'1 2'", new String[] { "1 2" }, "\"1 2\"");
		}
	}
	
	public void testSingleQuoted2() throws Exception {
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			execute1Arg("'1'", "'1'", "'1'");
		} else {
			execute1Arg("'1'", "1", "1");
		}
	}
	
	public void testWindows1() throws Exception {
		execute("\"a b c\" d e", new String[] { "a b c", "d", "e" });
	}
	
	public void testWindows2() throws Exception {
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			execute("\"ab\\\"c\" \"\\\\\" d", new String[] { "ab\"c", "\\", "d" }, "ab\\\"c \\ d");
		} else {
			execute("\"ab\\\"c\" \"\\\\\" d", new String[] { "ab\"c", "\\", "d" }, "ab\\\"c \\\\ d");
		}
	}
	
	public void testWindows3() throws Exception {
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			execute("a\\\\\\b d\"e f\"g h", new String[] { "a\\\\\\b", "de fg", "h" }, "a\\\\\\b \"de fg\" h");
		} else {
			execute("a\\\\\\b d\"e f\"g h", new String[] { "a\\b", "de fg", "h" }, "a\\\\b \"de fg\" h");
		}
	}
	
	public void testWindows4() throws Exception {
		execute("a\\\\\\\"b c d", new String[] { "a\\\"b", "c", "d" });
	}
	
	public void testWindows5() throws Exception {
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			execute("a\\\\\\\\\"b c\" d e", new String[] { "a\\\\b c", "d", "e" }, "\"a\\\\b c\" d e");
		} else {
			execute("a\\\\\\\\\"b c\" d e", new String[] { "a\\\\b c", "d", "e" }, "\"a\\\\\\\\b c\" d e");
		}
	}
	
	public void testAllInOne() throws Exception {
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			execute("1 \"\" 2 \" \" 3 \\\" 4 \"a b\" 5 \\\"bla\\\" 6 \"ab\"cd 7 ef\"gh\" 8 i\"\"j 9 \"x\\\"y\\\\\" 10 z\\\\z 11 \"two-quotes:\"\"\"\"\" 12 \"g\"\"h\" 13 \"\"\"a\"\" b\"",
					new String[] { "1", "\"\"", "2", " ", "3", "\"", "4", "a b", "5", "\"bla\"", "6", "abcd", "7", "efgh", "8", "ij", "9", "x\"y\\", "10", "z\\\\z", "11", "two-quotes:\"\"", "12", "g\"h", "13", "\"a\" b" },
					"1 \"\" 2 \" \" 3 \\\" 4 \"a b\" 5 \\\"bla\\\" 6 abcd 7 efgh 8 ij 9 x\\\"y\\ 10 z\\\\z 11 two-quotes:\\\"\\\" 12 g\\\"h 13 \"\\\"a\\\" b\"");
		} else {
			execute("1 \"\" 2 \" \" 3 \\\" 4 \"a b\" 5 \\\"bla\\\" 6 \"ab\"cd 7 ef\"gh\" 8 i\"\"j 9 \"x\\\"y\\\\\" 10 z\\\\z 11 \"two-quotes:\"\"\"\"\" 12 \"g\"\"h\" 13 \"\"\"a\"\" b\"",
					new String[] { "1", "", "2", " ", "3", "\"", "4", "a b", "5", "\"bla\"", "6", "abcd", "7", "efgh", "8", "ij", "9", "x\"y\\", "10", "z\\z", "11", "two-quotes:", "12", "gh", "13", "a b" },
					"1 \"\" 2 \" \" 3 \\\" 4 \"a b\" 5 \\\"bla\\\" 6 abcd 7 efgh 8 ij 9 x\\\"y\\\\ 10 z\\\\z 11 two-quotes: 12 gh 13 \"a b\"");
		}
	}
	
}
