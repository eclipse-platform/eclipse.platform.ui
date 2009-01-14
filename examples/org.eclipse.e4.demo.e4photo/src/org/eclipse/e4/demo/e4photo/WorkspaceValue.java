package org.eclipse.e4.demo.e4photo;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.services.ComputedValue;
import org.eclipse.e4.core.services.Context;

public class WorkspaceValue extends ComputedValue {

	@Override
	protected Object compute(Context context) {
		ResourcesPlugin.getPlugin().getPluginPreferences().setValue(ResourcesPlugin.PREF_AUTO_REFRESH, true);
		return ResourcesPlugin.getWorkspace();
	}

}
