package org.eclipse.e4.demo.e4photo;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.e4.workbench.ui.WorkbenchFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class Application implements IApplication {

	private ServiceTracker instanceLocation;
	private BundleContext context;
	private ServiceTracker bundleTracker;

	public Object start(IApplicationContext applicationContext)
			throws Exception {
		this.context = Activator.getContext();

		Display display = new Display();
		final WorkbenchFactory workbenchFactory = new WorkbenchFactory(
				getInstanceLocation(), getBundleAdmin(), RegistryFactory
						.getRegistry());
		final URI initialWorkbenchDefinitionInstance = URI
				.createPlatformPluginURI(
						"/org.eclipse.e4.demo.e4photo/Application.xmi", true);

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				try {
					IWorkbench wb = workbenchFactory
							.create(initialWorkbenchDefinitionInstance);
					wb.run();
				} catch (ThreadDeath th) {
					throw th;
				} catch (Exception ex) {
					ex.printStackTrace();
				} catch (Error err) {
					err.printStackTrace();
				}
			}
		});
		return IApplication.EXIT_OK;
	}

	public void stop() {

	}

	public Location getInstanceLocation() {
		if (instanceLocation == null) {
			Filter filter = null;
			try {
				filter = context.createFilter(Location.INSTANCE_FILTER);
			} catch (InvalidSyntaxException e) {
				// ignore this. It should never happen as we have tested the
				// above format.
			}
			instanceLocation = new ServiceTracker(context, filter, null);
			instanceLocation.open();
		}
		return (Location) instanceLocation.getService();
	}

	private PackageAdmin getBundleAdmin() {
		if (bundleTracker == null) {
			if (context == null)
				return null;
			bundleTracker = new ServiceTracker(context, PackageAdmin.class
					.getName(), null);
			bundleTracker.open();
		}
		return (PackageAdmin) bundleTracker.getService();
	}

}
