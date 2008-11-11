package org.eclipse.e4.demo.e4photo;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.services.IServiceLocator;

public class ServiceFactory extends
		org.eclipse.e4.core.services.AbstractServiceFactory {

	public ServiceFactory() {
	}

	public Object create(Class serviceInterface, IServiceLocator parentLocator,
			IServiceLocator locator) {
		if (serviceInterface == IWorkspace.class) {
			ResourcesPlugin.getPlugin().getPluginPreferences().setValue(ResourcesPlugin.PREF_AUTO_REFRESH, true);
			return ResourcesPlugin.getWorkspace();
		} else if (serviceInterface == Realm.class) {
			return new LockRealm();
		}
		return null;
	}

}
