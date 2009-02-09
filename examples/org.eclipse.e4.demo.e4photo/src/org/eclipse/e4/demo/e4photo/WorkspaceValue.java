package org.eclipse.e4.demo.e4photo;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.services.context.IComputedValue;
import org.eclipse.e4.core.services.context.IEclipseContext;

public class WorkspaceValue implements IComputedValue {

	public Object compute(IEclipseContext context, Object[] arguments) {
		ResourcesPlugin.getPlugin().getPluginPreferences().setValue(ResourcesPlugin.PREF_AUTO_REFRESH, true);
		return ResourcesPlugin.getWorkspace();
	}

}
