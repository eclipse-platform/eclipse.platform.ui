package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
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
public class PerspectiveManager implements ILaunchListener {
	
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
	 * launches to be registered.
	 */
	public void startup() {
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
	}

	/**
	 * Called by the debug ui plug-in on shutdown.
	 * The perspective manager de-registers as a 
	 * launch listener.	
	 */
	public void shutdown() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
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
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		if (config != null) {
			String mode = launch.getLaunchMode();
			String perspectiveId = null;
			try {
				if (mode.equals(ILaunchManager.DEBUG_MODE)) {
					perspectiveId = config.getAttribute(IDebugUIConstants.ATTR_DEBUG_PERSPECTIVE, null);
				} else {
					perspectiveId = config.getAttribute(IDebugUIConstants.ATTR_RUN_PERSPECTIVE, null);
				}
			} catch (CoreException e) {
				final CoreException ce = e;
				final ILaunchConfiguration lc = config;
				async(new Runnable() {
					public void run() {
						DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), "Error", 
						 MessageFormat.format("Unable to switch perpsectives as specified by launch configuration: {0}", new String[] {lc.getName()}),
						 ce.getStatus());
					}});
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
							window.openPage(id, null);
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
	 * Utility method to submit an asychronous runnable to the UI
	 */
	protected void async(Runnable r) {
		Display d = DebugUIPlugin.getDefault().getDisplay();
		if (d != null) {
			d.asyncExec(r);
		}
	}
}
