package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;

/**
 * The perspective manager manages the 'perspective' settings
 * defined by launch configurations. Specifically it: <ul>
 * <li>changes perspectives as launches are registered</li>
 * <li>change perspective when a thread suspends</li>
 * </ul>
 * 
 * @see IDebugUIContants.ATTR_RUN_PERSPECTIVE
 * @see IDebugUIContants.ATTR_DEBUG_PERSPECTIVE
 */
public class PerspectiveManager implements ILaunchListener, IDebugEventSetListener {
	
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
		DebugPlugin plugin = DebugPlugin.getDefault();
		plugin.getLaunchManager().addLaunchListener(this);
		plugin.addDebugEventListener(this);
	}

	/**
	 * Called by the debug ui plug-in on shutdown.
	 * The perspective manager de-registers as a 
	 * launch listener.
	 */
	public void shutdown() {
		DebugPlugin plugin = DebugPlugin.getDefault();
		plugin.getLaunchManager().removeLaunchListener(this);
		plugin.removeDebugEventListener(this);
	}

	/**
	 * Do nothing.
	 * 
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
	}
	
	/**
	 * Do nothing.
	 * 
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
	}	

	/** 
	 * Switch to the perspective specified by the
	 * launch configuration.
	 * 
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		String perspectiveId = null;
		// check event filters
		try {
			perspectiveId = getPerspectiveId(launch);
		} catch (CoreException e) {
			String name = DebugUIPlugin.getDefault().getModelPresentation().getText(launch);
			switchFailed(e, name);
		}
		if (perspectiveId != null) {
			switchToPerspective(perspectiveId);
		}
	}


	/**
	 * Switches to the specified perspective
	 * 
	 * @param id perspective identifier
	 */
	protected void switchToPerspective(final String id) {
		async(new Runnable() {
			public void run() {
				IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
				if (window != null) {
					try {
						window.getWorkbench().showPerspective(id, window);
					} catch (WorkbenchException e) {
						DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(),
						LaunchConfigurationsMessages.getString("PerspectiveManager.Error_1"),  //$NON-NLS-1$
						MessageFormat.format(LaunchConfigurationsMessages.getString("PerspectiveManager.Unable_to_switch_to_perspective__{0}_2"), new String[]{id}), //$NON-NLS-1$
						e);
					}
				}
			}
		});	
	}
	
	/**
	 * Utility method to submit an asnychronous runnable to the UI
	 */
	protected void async(Runnable r) {
		Display d = DebugUIPlugin.getDefault().getStandardDisplay();
		if (d != null && !d.isDisposed()) {
			d.asyncExec(r);
		}
	}
	
	/**
	 * Utility method to submit a synchronous runnable to the UI
	 */
	protected void sync(Runnable r) {
		Display d = DebugUIPlugin.getDefault().getStandardDisplay();
		if (d != null && !d.isDisposed()) {
			d.syncExec(r);
		}
	}	

	/**
	 * Reports failure to switch perspectives to the user
	 * 
	 * @param status exception status describing failure
	 * @param launchName the name of the launch that the
	 *  failure is associated with
	 */
	protected void switchFailed(final Throwable t, final String launchName) {
		sync(new Runnable() {
			public void run() {
				DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), LaunchConfigurationsMessages.getString("PerspectiveManager.Error_1"),  //$NON-NLS-1$
				 MessageFormat.format(LaunchConfigurationsMessages.getString("PerspectiveManager.Unable_to_switch_perpsectives_as_specified_by_launch__{0}_4"), new String[] {launchName}), //$NON-NLS-1$
				 t);
			}});
	}
	
	/**
	 * On a SUSPEND event, show the debug view. If no debug view is open,
	 * switch to the perspective specified by the launcher.
	 *
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		// open the debugger if this is a suspend event and the debug view is not yet open
		// and the preferences are set to switch
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.SUSPEND && event.getDetail() == event.BREAKPOINT) {
				// apply event filters
				ILaunch launch = null;
				Object source = event.getSource();
				if (source instanceof IDebugElement) {
					launch = ((IDebugElement)source).getLaunch();
				} else if (source instanceof IProcess) {
					launch = ((IProcess)source).getLaunch();
				}
				String perspectiveId = null;
				try {
					perspectiveId = getPerspectiveId(launch);
				} catch (CoreException e) {
					DebugUIPlugin.log(e);
				}
				// if no perspective specified, always switch to debug
				// perspective 

				// this has to be done in an asynch, such that the workbench
				// window can be accessed
				final String id = perspectiveId;
				Runnable r = new Runnable() {
					public void run() {
						String targetId = id;
						IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
						if (window == null) {
							return;
						}
						if (targetId == null) {
							IWorkbenchPage page = window.getActivePage();
							if (page != null) {
								IViewPart part = page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
								if (part == null) {
									targetId = IDebugUIConstants.ID_DEBUG_PERSPECTIVE;
								}
							}
						}
						if (targetId != null) {
							// re-open the window if minimized 
							Shell shell= window.getShell();
							if (shell != null && shell.getMinimized()) {
								shell.setMinimized(false);
							}						
							switchToPerspective(targetId);
						}
					}
				};
				async(r);
			}
		}
	}	
	
	/** 
	 * Returns the perspective associated with the
	 * given launch, or <code>null</code> if none.
	 * 
	 * @param launch a launch
	 * @return the perspective associated with the launch,
	 * 	or <code>null</code>
	 * @exception CoreException if unable to retrieve a required
	 *  launch configuration attribute
	 */
	protected String getPerspectiveId(ILaunch launch) throws CoreException {
		if (launch == null) {
			return null;
		}
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		String perspectiveId = null;
		String mode = launch.getLaunchMode();
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			perspectiveId = config.getAttribute(IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE, (String)null);
			if (perspectiveId != null && perspectiveId.equals(IDebugUIConstants.PERSPECTIVE_DEFAULT)) {
				perspectiveId = DebugUIPlugin.getDefault().getPreferenceStore().getString(IDebugUIConstants.PREF_SHOW_DEBUG_PERSPECTIVE_DEFAULT);
			}
		} else {
			perspectiveId = config.getAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, (String)null);
			if (perspectiveId != null && perspectiveId.equals(IDebugUIConstants.PERSPECTIVE_DEFAULT)) {
				perspectiveId = DebugUIPlugin.getDefault().getPreferenceStore().getString(IDebugUIConstants.PREF_SHOW_RUN_PERSPECTIVE_DEFAULT);
			}
		}
		if (perspectiveId != null && perspectiveId.equals(IDebugUIConstants.PERSPECTIVE_NONE)) {
			perspectiveId = null;
		}
		return perspectiveId;
	}
}
