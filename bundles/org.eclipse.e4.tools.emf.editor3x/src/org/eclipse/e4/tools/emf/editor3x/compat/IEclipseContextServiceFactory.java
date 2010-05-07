package org.eclipse.e4.tools.emf.editor3x.compat;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.modeling.ESelectionService;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.e4.workbench.ui.internal.ReflectionContributionFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class IEclipseContextServiceFactory extends AbstractServiceFactory {

	public IEclipseContextServiceFactory() {
	}

	@SuppressWarnings("restriction")
	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator,
			IServiceLocator locator) {
		if( ! IEclipseContext.class.equals(serviceInterface) ) {
			return null;
		}
		
		IWorkbenchLocationService wls = (IWorkbenchLocationService) locator.getService(IWorkbenchLocationService.class);
		final IWorkbenchWindow window = wls.getWorkbenchWindow();
		final IWorkbenchPartSite site = wls.getPartSite();

		System.err.println("The locator: " + locator);
		System.err.println("	Window: " + window);
		System.err.println("	Site: " + site);
		
		
		Object o = parentLocator.getService(serviceInterface);
		
		// This happens when we run in plain 3.x
		// We need to create a parent service context
		if( window == null && site == null ) {
			Bundle bundle = FrameworkUtil.getBundle(IEclipseContextServiceFactory.class);
			BundleContext bundleContext = bundle.getBundleContext();
			IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(bundleContext);

			final IEclipseContext appContext = serviceContext.createChild("WorkbenchContext"); //$NON-NLS-1$
			IExtensionRegistry registry = RegistryFactory.getRegistry();
			ReflectionContributionFactory contributionFactory = new ReflectionContributionFactory(registry);
			appContext.set(IContributionFactory.class.getName(),contributionFactory);
			
			return appContext;
		} else if( o != null && site == null ) {
			final IEclipseContext windowContext = ((IEclipseContext)o).createChild("WindowContext("+window+")");
			windowContext.set(ISelectionService.class, window.getSelectionService());
			window.getSelectionService().addSelectionListener(new ISelectionListener() {
				
				public void selectionChanged(IWorkbenchPart part, ISelection selection) {
					if( ! selection.isEmpty() ) {
						IStructuredSelection s = (IStructuredSelection) selection;
						if( s.size() == 1 ) {
							windowContext.set(IServiceConstants.SELECTION, s.getFirstElement());	
						} else {
							windowContext.set(IServiceConstants.SELECTION, s.toList());
						}
					}
				}
			});
			return windowContext;
		}
		
		return o;
	}
}