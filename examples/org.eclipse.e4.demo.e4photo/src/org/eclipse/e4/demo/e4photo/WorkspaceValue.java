package org.eclipse.e4.demo.e4photo;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IComputedValue;

public class WorkspaceValue implements IComputedValue {

	public Object compute(IEclipseContext context) {
		ResourcesPlugin.getPlugin().getPluginPreferences().setValue(ResourcesPlugin.PREF_AUTO_REFRESH, true);
		return ResourcesPlugin.getWorkspace();
	}

}
