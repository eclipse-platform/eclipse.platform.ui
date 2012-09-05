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
 * {@link org.eclipse.debug.core.DebugPlugin#renderCommandLine(String[], int[])}.
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
		URL classFolderUrl = FileLocator.find(TestsPlugin.getDefault().getBundle(), new Path("bin/"), null);
		classFolderUrl= FileLocator.toFileURL(classFolderUrl);
		File classFolderFile = URIUtil.toFile(URIUtil.toURI(classFolderUrl));
		
		String[] execArgs= new String[arguments.length + 2];
		execArgs[0]= new Path(System.getProperty("java.home")).append("bin/java").toOSString();
		execArgs[1]= ArgumentsPrinter.class.getName();
		System.arraycopy(arguments, 0, execArgs, 2, arguments.length);
		
		ArrayList resultArgs = runCommandLine(execArgs, classFolderFile);
		
		assertEquals("unexpected exec result;",
				Arrays.asList(arguments).toString(),
				resultArgs.toString());
		
		if (! Platform.getOS().equals(Constants.OS_WIN32)) {
			execArgs= new String[] { "sh", "-c", execArgs[0] + " " + execArgs[1] + " " + commandLine};
			resultArgs = runCommandLine(execArgs, classFolderFile);
			assertEquals("unexpected sh exec result;",
					Arrays.asList(arguments).toString(),
					resultArgs.toString());
			
		}
	}

	private static ArrayList runCommandLine(String[] execArgs, File workingDir)
			throws CoreException, IOException {
		Process process = DebugPlugin.exec(execArgs, workingDir);
		BufferedReader procOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
		
		ArrayList procArgs= new ArrayList();
		String procArg;
		while ((procArg = procOut.readLine()) != null) {
			procArgs.add(procArg);
		}
		return procArgs;
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
