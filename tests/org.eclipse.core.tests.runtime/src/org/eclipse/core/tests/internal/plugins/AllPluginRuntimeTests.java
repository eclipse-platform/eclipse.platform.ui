package org.eclipse.core.tests.internal.plugins;

import java.io.*;
import junit.framework.*;

/* Test Notes:
	Under the test directory plugins.activation.1, there should
	be 4 directories:  plugin.a, plugin.b, plugin.c and plugin.d.
	Each of these directories should contain two files:
	plugin.xml - where the id should be	the same as the 
		directory (e.g. plugin.a) and
	plugin_<x>.jar - where <x> is a, b, c or d depending on which
		directory you are in.  Each plugin_<x>.jar should contain
		the following:
		- org.eclipse.core.tests.internal.plugin.<x>.api (all classes) 
		- org.eclipse.core.tests.internal.plugin.<x> (all classes)
		- org.eclipse.core.tests.internal.plugins.TestClass (one class)
*/

public class AllPluginRuntimeTests {

/**
 * Each batch should be run separate. So, comment out all of them and
 * uncomment only the ones you want to run.
 */
protected static String[] getTests() {
	return new String[] {
		"internal.plugins.GetPluginPathTest",
		"internal.plugins.ActivationTest_Base",
		"internal.plugins.ActivationTest_DirectOtherRef",
		"internal.plugins.ActivationTest_DirectPluginRef",
		"internal.plugins.ActivationTest_IndirectRef",
		"internal.plugins.ActivationTest_NoRef",
		"internal.plugins.PlatformURLPerformanceTest",
		"internal.plugins.PlatformURLPerformanceTestConnection",
		"internal.plugins.PlatformURLTest",
		"internal.plugins.LoaderTest",
	};
}
public static void main(String[] args) throws Exception {
	String[] testIds = getTests();
	String classpath = System.getProperty("java.class.path");
	String pluginPathFile = System.getProperty("user.dir").concat("/testingTemp.plugin-path");
	for (int i = 0; i < testIds.length; i++) {
		String[] params = new String[] {
			"jre/bin/java", 
			"-cp",
			classpath,
			"org.eclipse.core.tests.harness.launcher.Main", 
			"-test", testIds[i],
			"-platform", "fixed_test_folder",
			"-dev",
			"bin",
			(i < (testIds.length-1) ? "-nocleanup" : ""),
			(i > 0 ? "-plugins" : ""), 
			(i > 0 ? pluginPathFile : ""),
		};
		Process p = Runtime.getRuntime().exec(params);
		boolean[] finished = new boolean[1];
		finished[0] = false;
		new Thread(getRunnable(p.getInputStream(), finished)).start();
		new Thread(getRunnable(p.getErrorStream(), finished)).start();
		p.waitFor();
		finished[0] = true;
	}
	File pFile = new File (pluginPathFile);
	pFile.delete();
}
protected static void print(String[] params) {
	System.out.println();
	for (int i = 0; i < params.length; i++) {
		System.out.print(params[i]);
		System.out.print(" ");
	}
	System.out.println();
}
protected static Runnable getRunnable(final InputStream input, final boolean[] finished) {
	return new Runnable() {
		public void run() {
			try {
				StringBuffer sb = new StringBuffer();
				while (!finished[0]) {
					int c = input.read();
					if (c != -1)
						sb.append((char) c);
				}
				input.close();
				System.out.print(sb.toString());
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
	};
}
}