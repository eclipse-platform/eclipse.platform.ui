package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;

/**
 * The perspective manager manages the 'perspective' settings
 * defined by lanuch configurations. Specifically it: <ul>
 * <li>changes perspectives as launches are registered</li>
 * <li>(will) change perspective when a thread suspends</li>
 * <li>(will) open the console when there is program output</li>
 * </ul>
 * 
 * @see IDebugUIContants.ATTR_RUN_PERSPECTIVE
 * @see IDebugUIContants.ATTR_DEBUG_PERSPECTIVE
 */
public class PerspectiveManager implements ILaunchListener, IDebugEventListener {
	
	/**
	 * Singleton perspective manager
	 */
	private static PerspectiveManager fgManager;
	
	/**
	 * Constructs the perspective manager
	 */
	private PerspectiveManager() {
	}
	
	/**
	 * Returns the perspective manager
	 */
	public static PerspectiveManager getDefault() {
		if (fgManager == null) {
			fgManager = new PerspectiveManager();
		}
		return fgManager;
	}
	
	/**
	 * Called by the debug ui plug-in on startup.
	 * The perspective manager starts listening for
	 * launches to be registered, and for debug events.
	 */
	public void startup() {
		DebugPlugin plugin = DebugPlugin.getDefault();
		plugin.getLaunchManager().addLaunchListener(this);
		plugin.addDebugEventListener(this);
	}

	/**
	 * Called by the debug ui plug-in on shutdown.
	 * The perspective manager de-registers as a 
	 * launch listener, and debug event listener
	 */
	public void shutdown() {
		DebugPlugin plugin = DebugPlugin.getDefault();
		plugin.getLaunchManager().removeLaunchListener(this);
		plugin.removeDebugEventListener(this);
	}

	/**
	 * Do nothing.
	 * 
	 * @see ILaunchListener#launchDeregistered(ILaunch)
	 */
	public void launchDeregistered(ILaunch launch) {
	}

	/** 
	 * Switch to the perspective specified by the
	 * launch configuration.
	 * 
	 * @see ILaunchListener#launchRegistered(ILaunch)
	 */
	public void launchRegistered(ILaunch launch) {
		final ILaunchConfiguration config = launch.getLaunchConfiguration();
		if (config != null) {
			String mode = launch.getLaunchMode();
			String perspectiveId = null;
			try {
				if (mode.equals(ILaunchManager.DEBUG_MODE)) {
					perspectiveId = config.getAttribute(IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE, null);
				} else {
					perspectiveId = config.getAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, null);
				}
			} catch (final CoreException e) {
				switchFailed(e.getStatus(), config.getName());
				return;
			}
			if (perspectiveId != null) {
				switchToPerspective(perspectiveId);
			}
		}
	}


	/**
	 * Switches to the specified perspective
	 * 
	 * @param id perspective identifier
	 * 
	 * [Issue: what should we supply as input to a new page?] 
	 */
	protected void switchToPerspective(final String id) {
		final IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			final IWorkbenchPage page = findPage(id);
			if (page == null) {
				async(new Runnable() {
					public void run() {
						try {
							window.openPage(id, ResourcesPlugin.getWorkspace().getRoot());
						} catch (WorkbenchException e) {
							DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(),
							"Error", 
							MessageFormat.format("Unable to switch to perspective: {0}", new String[]{id}),
							e.getStatus());
						}
					}
				});
				
			} else {
				if (page.equals(window.getActivePage())) {
					// no switch required
					return;
				} else {
					async(new Runnable() {
						public void run() {
							window.setActivePage(page);
						}
					});
					
				}
			}
			
		}
	}
	
	/**
	 * Returns a page in the current workbench window with the
	 * given identifier, or <code>null</code> if none.
	 * 
	 * @param id perpsective identifier
	 * @return workbench page, or <code>null</code>
	 */
	protected IWorkbenchPage findPage(String id) {
		IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage[] pages = window.getPages();
			for (int i = 0; i < pages.length; i++) {
				if (pages[i].getPerspective().getId().equals(id)) {
					return pages[i];
				}
			}
		}
		return null;
	}
	
	/**
	 * Utility method to submit an asnychronous runnable to the UI
	 */
	protected void async(Runnable r) {
		Display d = DebugUIPlugin.getDefault().getDisplay();
		if (d != null) {
			d.asyncExec(r);
		}
	}
	
	/**
	 * Utility method to submit a synchronous runnable to the UI
	 */
	protected void sync(Runnable r) {
		Display d = DebugUIPlugin.getDefault().getDisplay();
		if (d != null) {
			d.syncExec(r);
		}
	}	
	
	/**
	 * When a thread suspends, switch perspectives as defined by
	 * the associated lanuch configuration.
	 * 
	 * @see IDebugEventListener#handleDebugEvent(DebugEvent)
	 */
	public void handleDebugEvent(DebugEvent event) {
		if (event.getKind() == DebugEvent.SUSPEND) {
			Object source = event.getSource();
			if (source instanceof IDebugElement) {
				ILaunch launch = ((IDebugElement)source).getLaunch();
				if (launch != null) {
					ILaunchConfiguration config = launch.getLaunchConfiguration();
					if (config != null) {
						String perspectiveId = null;
						try {
							perspectiveId = config.getAttribute(IDebugUIConstants.ATTR_TARGET_SUSPEND_PERSPECTIVE, null);
						} catch (CoreException e) {
							switchFailed(e.getStatus(), config.getName());
						}
					}
				}
			}
		}
	}

	/**
	 * Reports failure to switch perspectives to the user
	 * 
	 * @param status exception status describing failure
	 * @param configName the name of the launch configuration that the
	 *  failure is associated with
	 */
	protected void switchFailed(final IStatus status, final String configName) {
		sync(new Runnable() {
			public void run() {
				DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), "Error", 
				 MessageFormat.format("Unable to switch perpsectives as specified by launch configuration: {0}", new String[] {configName}),
				 status);
			}});
	}
}
