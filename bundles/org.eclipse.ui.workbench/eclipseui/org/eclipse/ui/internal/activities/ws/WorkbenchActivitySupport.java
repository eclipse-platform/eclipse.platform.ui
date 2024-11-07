/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.activities.ITriggerPointAdvisor;
import org.eclipse.ui.activities.ITriggerPointManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.WorkbenchTriggerPointAdvisor;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.activities.MutableActivityManager;
import org.eclipse.ui.internal.activities.ProxyActivityManager;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * Implementation of
 * {@link org.eclipse.ui.activities.IWorkbenchActivitySupport}.
 *
 * @since 3.0
 */
public class WorkbenchActivitySupport implements IWorkbenchActivitySupport, IExtensionChangeHandler {
	private MutableActivityManager mutableActivityManager;

	private ProxyActivityManager proxyActivityManager;

	private ImageBindingRegistry activityImageBindingRegistry;

	private ImageBindingRegistry categoryImageBindingRegistry;

	private ITriggerPointManager triggerPointManager;

	private ITriggerPointAdvisor advisor;

	/**
	 * Create a new instance of this class.
	 */
	public WorkbenchActivitySupport() {
		triggerPointManager = new TriggerPointManager();
		IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
		tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(getActivitySupportExtensionPoint()));
		mutableActivityManager = new MutableActivityManager(getTriggerPointAdvisor());
		proxyActivityManager = new ProxyActivityManager(mutableActivityManager);
		mutableActivityManager.addActivityManagerListener(new IActivityManagerListener() {

			private Set<String> lastEnabled = new HashSet<>(mutableActivityManager.getEnabledActivityIds());

			@Override
			public void activityManagerChanged(ActivityManagerEvent activityManagerEvent) {
				Set<String> activityIds = mutableActivityManager.getEnabledActivityIds();
				// only update the windows if we've not processed this new enablement state
				// already.
				if (!activityIds.equals(lastEnabled)) {
					lastEnabled = new HashSet<>(activityIds);

					// abort if the workbench isn't running
					if (!PlatformUI.isWorkbenchRunning()) {
						return;
					}

					// refresh the managers on all windows.
					final IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
					for (IWorkbenchWindow wWindow : windows) {
						if (wWindow instanceof WorkbenchWindow) {
							final WorkbenchWindow window = (WorkbenchWindow) wWindow;

							final ProgressMonitorDialog dialog = new ProgressMonitorDialog(window.getShell());

							final IRunnableWithProgress runnable = new IRunnableWithProgress() {

								/**
								 * When this operation should open a dialog
								 */
								private long openTime;

								/**
								 * Whether the dialog has been opened yet.
								 */
								private boolean dialogOpened = false;

								@Override
								public void run(IProgressMonitor monitor) {

									openTime = System.currentTimeMillis()
											+ workbench.getProgressService().getLongOperationTime();

									// two work units - updating the window bars, and updating view bars
									monitor.beginTask(ActivityMessages.ManagerTask, 2);

									monitor.subTask(ActivityMessages.ManagerWindowSubTask);

									// update window managers...
									updateWindowBars(window);
									monitor.worked(1);

									monitor.subTask(ActivityMessages.ManagerViewsSubTask);
									// update all of the (realized) views in all of the pages
									IWorkbenchPage[] pages = window.getPages();
									for (IWorkbenchPage page : pages) {
										IViewReference[] refs = page.getViewReferences();
										for (IViewReference ref : refs) {
											IViewPart part = ref.getView(false);
											if (part != null) {
												updateViewBars(part);
											}
										}
									}
									monitor.worked(1);

									monitor.done();
								}

								/**
								 * Update the managers on the given given view.
								 *
								 * @param part the view to update
								 */
								private void updateViewBars(IViewPart part) {
									IViewSite viewSite = part.getViewSite();
									// check for badly behaving or badly initialized views
									if (viewSite == null) {
										return;
									}
									IActionBars bars = viewSite.getActionBars();
									IContributionManager manager = bars.getMenuManager();
									if (manager != null) {
										updateManager(manager);
									}
									manager = bars.getToolBarManager();
									if (manager != null) {
										updateManager(manager);
									}
									manager = bars.getStatusLineManager();
									if (manager != null) {
										updateManager(manager);
									}
								}

								/**
								 * Update the managers on the given window.
								 *
								 * @param window the window to update
								 */
								private void updateWindowBars(final WorkbenchWindow window) {
									IContributionManager manager = window.getMenuBarManager();
									if (manager != null) {
										updateManager(manager);
									}
									manager = window.getCoolBarManager2();
									if (manager != null) {
										updateManager(manager);
									}
									manager = window.getToolBarManager2();
									if (manager != null) {
										updateManager(manager);
									}
									manager = window.getStatusLineManager();
									if (manager != null) {
										updateManager(manager);
									}
								}

								/**
								 * Update the given manager in the UI thread. This may also open the progress
								 * dialog if the operation is taking too long.
								 *
								 * @param manager the manager to update
								 */
								private void updateManager(final IContributionManager manager) {
									if (!dialogOpened && System.currentTimeMillis() > openTime) {
										dialog.open();
										dialogOpened = true;
									}

									manager.update(true);
								}
							};

							// don't open the dialog by default - that'll be
							// handled by the runnable if we take too long
							dialog.setOpenOnRun(false);
							// run this in the UI thread
							workbench.getDisplay()
									.asyncExec(() -> BusyIndicator.showWhile(workbench.getDisplay(), () -> {
										try {
											dialog.run(false, false, runnable);
										} catch (InvocationTargetException | InterruptedException e2) {
											log(e2);
										}
									}));
						}
					}
				}
			}

			/**
			 * Logs an error message to the workbench log.
			 *
			 * @param e the exception to log
			 */
			private void log(Exception e) {
				StatusUtil.newStatus(IStatus.ERROR, "Could not update contribution managers", e); //$NON-NLS-1$
			}
		});
	}

	@Override
	public IActivityManager getActivityManager() {
		return proxyActivityManager;
	}

	@Override
	public void setEnabledActivityIds(Set<String> enabledActivityIds) {
		mutableActivityManager.setEnabledActivityIds(enabledActivityIds);
	}

	@Override
	public ImageDescriptor getImageDescriptor(IActivity activity) {
		if (activity.isDefined()) {
			ImageDescriptor descriptor = getActivityImageBindingRegistry().getImageDescriptor(activity.getId());
			if (descriptor != null) {
				return descriptor;
			}
		}
		return WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_ACTIVITY);
	}

	@Override
	public ImageDescriptor getImageDescriptor(ICategory category) {
		if (category.isDefined()) {
			ImageDescriptor descriptor = getCategoryImageBindingRegistry().getImageDescriptor(category.getId());
			if (descriptor != null) {
				return descriptor;
			}
		}
		return WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_ACTIVITY_CATEGORY);
	}

	/**
	 * Return the activity image registry.
	 *
	 * @return the activity image registry
	 * @since 3.1
	 */
	private ImageBindingRegistry getActivityImageBindingRegistry() {
		if (activityImageBindingRegistry == null) {
			activityImageBindingRegistry = new ImageBindingRegistry(
					IWorkbenchRegistryConstants.TAG_ACTIVITY_IMAGE_BINDING);
			PlatformUI.getWorkbench().getExtensionTracker().registerHandler(activityImageBindingRegistry,
					ExtensionTracker.createExtensionPointFilter(getActivitySupportExtensionPoint()));
		}
		return activityImageBindingRegistry;
	}

	/**
	 * Return the category image registry.
	 *
	 * @return the category image registry
	 * @since 3.1
	 */
	private ImageBindingRegistry getCategoryImageBindingRegistry() {
		if (categoryImageBindingRegistry == null) {
			categoryImageBindingRegistry = new ImageBindingRegistry(
					IWorkbenchRegistryConstants.TAG_CATEGORY_IMAGE_BINDING);
			PlatformUI.getWorkbench().getExtensionTracker().registerHandler(categoryImageBindingRegistry,
					ExtensionTracker.createExtensionPointFilter(getActivitySupportExtensionPoint()));
		}
		return categoryImageBindingRegistry;
	}

	/**
	 * Dispose of the image registries and activity manager.
	 *
	 * @since 3.1
	 */
	public void dispose() {
		proxyActivityManager.dispose();
		mutableActivityManager.dispose();
		if (activityImageBindingRegistry != null) {
			activityImageBindingRegistry.dispose();
			PlatformUI.getWorkbench().getExtensionTracker().unregisterHandler(activityImageBindingRegistry);
		}
		if (categoryImageBindingRegistry != null) {
			categoryImageBindingRegistry.dispose();
			PlatformUI.getWorkbench().getExtensionTracker().unregisterHandler(categoryImageBindingRegistry);
		}

		PlatformUI.getWorkbench().getExtensionTracker().unregisterHandler(this);
	}

	/**
	 * Return the trigger point advisor.
	 *
	 * TODO: should this be part of the interface?
	 *
	 * @return the trigger point advisor
	 * @since 3.1
	 */
	public ITriggerPointAdvisor getTriggerPointAdvisor() {
		if (advisor != null) {
			return advisor;
		}

		IProduct product = Platform.getProduct();
		if (product != null) {
			TriggerPointAdvisorDescriptor descriptor = TriggerPointAdvisorRegistry.getInstance()
					.getAdvisorForProduct(product.getId());
			if (descriptor != null) {
				try {
					advisor = descriptor.createAdvisor();
				} catch (CoreException e) {
					WorkbenchPlugin.log("could not create trigger point advisor", e); //$NON-NLS-1$
				}
			}
		}

		if (advisor == null) {
			advisor = new WorkbenchTriggerPointAdvisor();
		}

		return advisor;
	}

	@Override
	public ITriggerPointManager getTriggerPointManager() {
		return triggerPointManager;
	}

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		// reset the advisor if it's the "default" advisor.
		// this will give getAdvisor the chance to find a proper trigger/binding if
		// it exists.
		if (advisor != null && advisor.getClass().equals(WorkbenchTriggerPointAdvisor.class)) {
			advisor = null;
		}
	}

	/**
	 * Return the activity support extension point.
	 *
	 * @return the activity support extension point.
	 * @since 3.1
	 */
	private IExtensionPoint getActivitySupportExtensionPoint() {
		return Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_ACTIVITYSUPPORT);
	}

	@Override
	public void removeExtension(IExtension extension, Object[] objects) {
		for (Object object : objects) {
			if (object == advisor) {
				advisor = null;
				break;
			}
		}
	}

	@Override
	public IMutableActivityManager createWorkingCopy() {
		MutableActivityManager clone = (MutableActivityManager) mutableActivityManager.clone();
		clone.unhookRegistryListeners();
		return clone;
	}
}
