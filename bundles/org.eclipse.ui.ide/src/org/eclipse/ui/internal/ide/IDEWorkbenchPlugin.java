/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 489250
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/

package org.eclipse.ui.internal.ide;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.registry.MarkerImageProviderRegistry;
import org.eclipse.ui.internal.ide.registry.ProjectImageRegistry;
import org.eclipse.ui.internal.ide.registry.UnassociatedEditorStrategyRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * This internal class represents the top of the IDE workbench.
 *
 * This class is responsible for tracking various registries
 * font, preference, graphics, dialog store.
 *
 * This class is explicitly referenced by the
 * IDE workbench plug-in's  "plugin.xml"
 *
 * @since 3.0
 */
public class IDEWorkbenchPlugin extends AbstractUIPlugin {
	// Default instance of the receiver
	private static IDEWorkbenchPlugin inst;

	/**
	 * The IDE workbench plugin ID.
	 */
	public static final String IDE_WORKBENCH = "org.eclipse.ui.ide"; //$NON-NLS-1$

	/**
	 * The ID of the default text editor.
	 * This must correspond to EditorsUI.DEFAULT_TEXT_EDITOR_ID.
	 */
	public static final String DEFAULT_TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$

	// IDE workbench extension point names
	public static final String PL_MARKER_IMAGE_PROVIDER = "markerImageProviders"; //$NON-NLS-1$

	public static final String PL_MARKER_HELP = "markerHelp"; //$NON-NLS-1$

	public static final String PL_MARKER_RESOLUTION = "markerResolution"; //$NON-NLS-1$

	public static final String PL_PROJECT_NATURE_IMAGES = "projectNatureImages"; //$NON-NLS-1$

	private static final String ICONS_PATH = "$nl$/icons/full/";//$NON-NLS-1$

	private static final int PROBLEMS_VIEW_CREATION_DELAY= 6000;

	/**
	 * Project image registry; lazily initialized.
	 */
	private ProjectImageRegistry projectImageRegistry = null;

	/**
	 * Marker image registry; lazily initialized.
	 */
	private MarkerImageProviderRegistry markerImageProviderRegistry = null;

	/**
	 * Unassociated file/editor strategy registry; lazily initialized
	 */
	private UnassociatedEditorStrategyRegistry unassociatedEditorStrategyRegistry = null;

	private ResourceManager resourceManager;

	/**
	 * Create an instance of the receiver.
	 */
	public IDEWorkbenchPlugin() {
		super();
		inst = this;
	}

	/**
	 * Creates an extension. If the extension plugin has not been loaded a busy
	 * cursor will be activated during the duration of the load.
	 *
	 * @param element        the config element defining the extension
	 * @param classAttribute the name of the attribute carrying the class
	 * @return Object the extension object
	 * @throws CoreException if extension creation failed
	 */
	public static Object createExtension(final IConfigurationElement element,
			final String classAttribute) throws CoreException {
		// If plugin has been loaded create extension.
		// Otherwise, show busy cursor then create extension.
		Bundle plugin = Platform.getBundle(element.getContributor().getName());
		if (plugin.getState() == Bundle.ACTIVE) {
			return element.createExecutableExtension(classAttribute);
		}
		final Object[] ret = new Object[1];
		final CoreException[] exc = new CoreException[1];
		BusyIndicator.showWhile(null, () -> {
			try {
				ret[0] = element.createExecutableExtension(classAttribute);
			} catch (CoreException e) {
				exc[0] = e;
			}
		});
		if (exc[0] != null) {
			throw exc[0];
		}
		return ret[0];
	}

	/* Return the default instance of the receiver. This represents the runtime plugin.
	 *
	 * @see AbstractPlugin for the typical implementation pattern for plugin classes.
	 */
	public static IDEWorkbenchPlugin getDefault() {
		return inst;
	}

	/**
	 * Return the workspace used by the workbench
	 *
	 * This method is internal to the workbench and must not be called
	 * by any plugins.
	 */
	public static IWorkspace getPluginWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Logs the given message to the platform log.
	 *
	 * If you have an exception in hand, call log(String, Throwable) instead.
	 *
	 * If you have a status object in hand call log(String, IStatus) instead.
	 *
	 * This convenience method is for internal use by the IDE Workbench only and
	 * must not be called outside the IDE Workbench.
	 *
	 * @param message
	 *            A high level UI message describing when the problem happened.
	 */
	public static void log(String message) {
		getDefault().getLog().log(
				StatusUtil.newStatus(IStatus.ERROR, message, null));
	}

	/**
	 * Logs the given message and throwable to the platform log.
	 *
	 * If you have a status object in hand call log(String, IStatus) instead.
	 *
	 * This convenience method is for internal use by the IDE Workbench only and
	 * must not be called outside the IDE Workbench.
	 *
	 * @param message
	 *            A high level UI message describing when the problem happened.
	 * @param t
	 *            The throwable from where the problem actually occurred.
	 */
	public static void log(String message, Throwable t) {
		IStatus status = StatusUtil.newStatus(IStatus.ERROR, message, t);
		log(message, status);
	}

	/**
	 * Logs the given throwable to the platform log, indicating the class and
	 * method from where it is being logged (this is not necessarily where it
	 * occurred).
	 *
	 * This convenience method is for internal use by the IDE Workbench only and
	 * must not be called outside the IDE Workbench.
	 *
	 * @param clazz
	 *            The calling class.
	 * @param methodName
	 *            The calling method name.
	 * @param t
	 *            The throwable from where the problem actually occurred.
	 */
	public static void log(Class<?> clazz, String methodName, Throwable t) {
		String msg = MessageFormat.format("Exception in {0}.{1}: {2}", //$NON-NLS-1$
				clazz.getName(), methodName, t);
		log(msg, t);
	}

	/**
	 * Logs the given message and status to the platform log.
	 *
	 * This convenience method is for internal use by the IDE Workbench only and
	 * must not be called outside the IDE Workbench.
	 *
	 * @param message
	 *            A high level UI message describing when the problem happened.
	 *            May be <code>null</code>.
	 * @param status
	 *            The status describing the problem. Must not be null.
	 */
	public static void log(String message, IStatus status) {

		//1FTUHE0: ITPCORE:ALL - API - Status & logging - loss of semantic info

		if (message != null) {
			getDefault().getLog().log(
					StatusUtil.newStatus(IStatus.ERROR, message, null));
		}

		getDefault().getLog().log(status);
	}

	@Override
	protected void refreshPluginActions() {
		// do nothing
	}


	/**
	 * Return the manager that maps project nature ids to images.
	 */
	public ProjectImageRegistry getProjectImageRegistry() {
		if (projectImageRegistry == null) {
			projectImageRegistry = new ProjectImageRegistry();
			projectImageRegistry.load();
		}
		return projectImageRegistry;
	}

	/**
	 * Returns the marker image provider registry for the workbench.
	 *
	 * @return the marker image provider registry
	 */
	public MarkerImageProviderRegistry getMarkerImageProviderRegistry() {
		if (markerImageProviderRegistry == null) {
			markerImageProviderRegistry = new MarkerImageProviderRegistry();
		}
		return markerImageProviderRegistry;
	}

	/**
	 * Returns the unassociated file/editor strategy registry for the workbench.
	 *
	 * @return the unassociated file/editor strategy registry
	 */
	public synchronized UnassociatedEditorStrategyRegistry getUnassociatedEditorStrategyRegistry() {
		if (unassociatedEditorStrategyRegistry == null) {
			unassociatedEditorStrategyRegistry = new UnassociatedEditorStrategyRegistry();
		}
		return unassociatedEditorStrategyRegistry;
	}

	/**
	 * Returns the about information of all known features,
	 * omitting any features which are missing this information.
	 *
	 * @return a possibly empty list of about infos
	 */
	public AboutInfo[] getFeatureInfos() {
		// cannot be cached since bundle groups come and go
		List<AboutInfo> infos = new ArrayList<>();

		// add an entry for each bundle group
		IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
		if (providers != null) {
			for (IBundleGroupProvider provider : providers) {
				for (IBundleGroup bundleGroup : provider.getBundleGroups()) {
					infos.add(new AboutInfo(bundleGroup));
				}
			}
		}

		return infos.toArray(new AboutInfo[infos.size()]);
	}

	/**
	 * Get the workbench image with the given path relative to ICON_PATH.
	 *
	 * @param relativePath relative path of image
	 * @return ImageDescriptor or <code>null</code>
	 */
	public static ImageDescriptor getIDEImageDescriptor(String relativePath){
		return ResourceLocator.imageDescriptorFromBundle(IDE_WORKBENCH, ICONS_PATH + relativePath).orElse(null);
	}

	/**
	 * Return the resourceManager used by this plug-in.
	 *
	 * @return the resource manager
	 */
	public ResourceManager getResourceManager() {
		if(resourceManager == null){
			resourceManager = new LocalResourceManager(JFaceResources.getResources());
		}
		return resourceManager;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		if (resourceManager != null) {
			resourceManager.dispose();
		}
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		createProblemsViews();
	}

	/**
	 * Create (but don't activate) the Problems views so that the view's tooltip and icon are
	 * up-to-date.
	 */
	private void createProblemsViews() {
		final Runnable r= new Runnable() {
			@Override
			public void run() {
				IWorkbench workbench = PlatformUI.isWorkbenchRunning() ? PlatformUI.getWorkbench() : null;
				if (workbench != null && (workbench.getDisplay().isDisposed() || workbench.isClosing())) {
					return;
				}

				if (workbench == null || workbench.isStarting()) {
					Display.getCurrent().timerExec(PROBLEMS_VIEW_CREATION_DELAY, this);
					return;
				}
				// We can't access preferences store before scheduling the job
				// because this would cause instance area to be initialized
				// before user selected the workspace location.
				// See bug 514297 and
				// org.eclipse.core.internal.runtime.DataArea.assertLocationInitialized()
				if (!getDefault().getPreferenceStore()
						.getBoolean(IDEInternalPreferences.SHOW_PROBLEMS_VIEW_DECORATIONS_ON_STARTUP)) {
					return;
				}
				for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
					IWorkbenchPage activePage= window.getActivePage();
					if (activePage == null) {
						continue;
					}
					for (IViewReference viewReference : activePage.getViewReferences()) {
						if (IPageLayout.ID_PROBLEM_VIEW.equals(viewReference.getId())) {
							try {
								activePage.showView(viewReference.getId(), viewReference.getSecondaryId(), IWorkbenchPage.VIEW_CREATE);
							} catch (PartInitException e) {
								log("Could not create Problems view", e.getStatus()); //$NON-NLS-1$
							}
						}
					}
				}
			}
		};
		Display display = Display.getCurrent();
		if (display != null) {
			display.timerExec(PROBLEMS_VIEW_CREATION_DELAY, r);
		} else {
			Job job = new Job("Initializing Problems view") { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					IWorkbench workbench = PlatformUI.isWorkbenchRunning() ? PlatformUI.getWorkbench() : null;
					if (workbench == null) {
						// Workbench not created yet, so avoid using display to
						// avoid crash like in bug 513901
						schedule(PROBLEMS_VIEW_CREATION_DELAY);
						return Status.OK_STATUS;
					}
					if (workbench.isClosing()) {
						return Status.CANCEL_STATUS;
					}
					PlatformUI.getWorkbench().getDisplay().asyncExec(r);
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.setUser(false);
			job.schedule(PROBLEMS_VIEW_CREATION_DELAY);
		}
	}
}
