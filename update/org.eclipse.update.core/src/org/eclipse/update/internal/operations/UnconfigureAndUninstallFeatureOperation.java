package org.eclipse.update.internal.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.core.UpdateCore;
import org.eclipse.update.operations.IOperationListener;
import org.eclipse.update.operations.IUnconfigFeatureOperation;
import org.eclipse.update.operations.OperationsManager;
import org.osgi.service.prefs.Preferences;

public class UnconfigureAndUninstallFeatureOperation extends FeatureOperation
		implements IUnconfigureAndUninstallFeatureOperation {

	public UnconfigureAndUninstallFeatureOperation(IConfiguredSite targetSite, IFeature feature) {
		super(targetSite, feature);
	}

	public boolean execute(IProgressMonitor pm, IOperationListener listener)
			throws CoreException, InvocationTargetException {
		
		IUnconfigFeatureOperation unconfigOperation = OperationsManager.getOperationFactory().createUnconfigOperation(targetSite, feature);

		boolean isRestartNeeded = unconfigOperation.execute(pm, listener);
		
		IPreferencesService service = Platform.getPreferencesService();
		IEclipsePreferences root = service.getRootNode();
		Preferences updateConfigurationPreferences = root.node(ConfigurationScope.SCOPE).node(UpdateCore.getPlugin().toString());
		if (updateConfigurationPreferences != null) {
			String toBeUninstalled = updateConfigurationPreferences.get("toBeUninstalled", null);
			toBeUninstalled = ((toBeUninstalled == null) ? "": toBeUninstalled + ":") + feature.getVersionedIdentifier().toString(); ;
			updateConfigurationPreferences.put("toBeUninstalled", toBeUninstalled);
		}

		
		return true;
	}

}
