package org.eclipse.ui.tests.dynamicplugins;

import java.io.IOException;
import java.net.URL;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.tests.TestPlugin;

public class DynamicUtils {
	
	public static final boolean installPlugin(String pluginName) {
		// Programmatically install a new plugin
		TestPlugin plugin = TestPlugin.getDefault();
		if (plugin == null)
			return false;
		String pluginLocation = null;
		try {
			URL dataURL = Platform.resolve(plugin.getBundle().getEntry(pluginName));
			pluginLocation = "reference:" + dataURL.toExternalForm();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		TestInstallUtil install = new TestInstallUtil(pluginLocation);
		install.installBundle();
		return true;
	}

}
