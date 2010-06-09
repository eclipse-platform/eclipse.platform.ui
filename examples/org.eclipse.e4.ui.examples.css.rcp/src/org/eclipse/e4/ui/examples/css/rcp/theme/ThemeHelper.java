package org.eclipse.e4.ui.examples.css.rcp.theme;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.e4.ui.examples.css.rcp.Activator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ThemeHelper {
	private static IThemeEngine engine = null;
	private static Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

	public static IThemeEngine getEngine() {
		if (engine == null) {
			engine = getThemeEngine();
		}
		return engine;
	}

	private static IThemeEngine getThemeEngine() {
		BundleContext context = bundle.getBundleContext();

		ServiceReference ref = context.getServiceReference(IThemeManager.class
				.getName());
		IThemeManager manager = (IThemeManager) context.getService(ref);

		return manager.getEngineForDisplay(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow() == null ? Display.getCurrent()
				: PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getShell().getDisplay());
	}

}
