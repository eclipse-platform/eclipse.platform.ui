package org.eclipse.core.tests.internal.plugins;

import java.io.File;
import org.eclipse.core.tests.harness.launcher.SessionTestLauncher;

/**
 * 
 */
public class LibraryLookupTests {

public static void main(String[] args) {
	String pluginPaths = null;
	String[] testClasses = new String[] {
		"org.eclipse.core.tests.internal.plugins.JarPluginPathTest",
		"org.eclipse.core.tests.internal.plugins.JarSearchTests",
	};

	for (int i = 0; i < testClasses.length; i++) {
		String[] params = null;
		if (i == 0) {
			params = new String[] {
				"-vm", "d:/jre20011025/bin/java.exe",
				testClasses[i]
			};
		} else {
			pluginPaths = System.getProperty("user.dir").concat("/testingTemp.tempPlugin-path");
			params = new String[] {
				"-vm", "d:/jre20011025/bin/java.exe",
				"-plugins",
				pluginPaths,
				//"-debug", "test1",
				testClasses[i]
			};
		}
		new SessionTestLauncher().run(params);
		if (pluginPaths != null) {
			File pFile = new File (pluginPaths);
			pFile.delete();
		}
	}
}

}

