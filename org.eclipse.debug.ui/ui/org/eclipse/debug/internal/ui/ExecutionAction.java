package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This is the debug action which appears in the desktop menu and toolbar.
 */
public abstract class ExecutionAction extends Action {

	private final static String PREFIX= "execution_action.";
	private final static String ERROR= "error.";
	private final static String STATUS= PREFIX + "status";
	private static final String LAUNCH_PREFIX= "launch_action.";
	private static final String LAUNCH_ERROR_TITLE= LAUNCH_PREFIX + ERROR + "title";
	private static final String LAUNCH_ERROR_MESSAGE= LAUNCH_PREFIX + ERROR + "message";	

	/**
	 * @see Action
	 */
	public void run() {

		final IWorkbenchWindow dwindow= DebugUIPlugin.getActiveWorkbenchWindow();
		final IStructuredSelection selection= resolveSelection(dwindow);

		// if the selection is a debug element, system process, or launch, try to do a relaunch
		if (selection != null && attemptRelaunch(selection)) {
			return;
		}

		// otherwise, resolve a launcher and an element
		final Object[] launchers= resolveLaunchers(selection);
		if (launchers.length == 0) {
			// could not determine any launchers to use to launch
			// very unlikely to happen
			beep();
			return;
		}

		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				// if there are no choices to make, do the launch
				if (launchers.length == 1 && selection != null) {
					ILauncher launcher= (ILauncher)launchers[0];
					Object[] elements= selection.toArray();
					boolean ok= launcher.launch(elements, getMode());
					if (!ok) {
						String string= DebugUIUtils.getResourceString(LAUNCH_ERROR_MESSAGE);
						String message= MessageFormat.format(string, new String[] {launcher.getLabel()});
						MessageDialog.openError(DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), DebugUIUtils.getResourceString(LAUNCH_ERROR_TITLE), message);	
					}					
				} else {
					// must choose a launcher
					useWizard(launchers, dwindow.getShell(), selection);
				}
			}
		});
	}

	/**
	 * Returns the mode of a launcher to use for this action
	 */
	protected abstract String getMode();

	/**
	 * Returns the launch manager.
	 */
	protected static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Relaunches the launch in the specified mode.
	 */
	public void relaunch(ILaunch launch, String mode) {
		RelaunchActionDelegate.relaunch(launch, mode);
	}

	/**
	 * Determines and returns the selection that provides context for the launch,
	 * or <code>null</code> if there is no selection.
	 */
	protected IStructuredSelection resolveSelection(IWorkbenchWindow window) {
		if (window == null) {
			return null;
		}
		ISelection selection= window.getSelectionService().getSelection();
		if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			// there is no obvious selection - go fishing
			selection= null;
			IWorkbenchPage page= window.getActivePage();
			if (page == null) {
				//workspace is closed
				return null;
			}

			// first, see if there is an active editor, and try its input element
			IEditorPart editor= page.getActiveEditor();
			Object element= null;
			if (editor != null) {
				element= editor.getEditorInput();
			}

			if (selection == null && element != null) {
				selection= new StructuredSelection(element);
			}
		}
		return (IStructuredSelection)selection;
	}

	/**
	 * Resolves and returns the applicable launcher(s) to be used to launch the
	 * elements in the specified selection.
	 */
	protected Object[] resolveLaunchers(IStructuredSelection selection) {
		List launchers;
		if (selection == null || selection.isEmpty()) {
			launchers= Arrays.asList(getLaunchManager().getLaunchers(getMode()));
		} else {
			launchers= new ArrayList(2);
			Iterator elements= selection.iterator();
			MultiStatus status= new MultiStatus(DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(), IDebugStatusConstants.REQUEST_FAILED, DebugUIUtils.getResourceString(STATUS), null);
			while (elements.hasNext()) {
				Object element= elements.next();
				ILauncher defaultLauncher= null;
				try {
					IResource resource= null;
					if (element instanceof IAdaptable) {
						IAdaptable el= (IAdaptable)element;
						resource= (IResource)el.getAdapter(IResource.class);
						if (resource == null) {
							resource= (IProject)el.getAdapter(IProject.class);
						}
					}
					IProject project= null;
					if (resource != null) {
						project= resource.getProject();
					}
					if (project != null) {
						defaultLauncher= getLaunchManager().getDefaultLauncher(project);
					}
					if (defaultLauncher != null) {
						if (!defaultLauncher.getModes().contains(getMode())) {
							defaultLauncher= null;
						}
					}
				} catch (CoreException e) {
					status.merge(e.getStatus());
				}
				if (defaultLauncher != null) {
					if (!launchers.contains(defaultLauncher)) {
						launchers.add(defaultLauncher);
					}
				}
			}
			if (!status.isOK()) {
				DebugUIUtils.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), PREFIX + ERROR, status);
			}
			if (launchers.isEmpty()) {
				launchers= Arrays.asList(getLaunchManager().getLaunchers(getMode()));
			}
		}
		
		return resolveVisibleLaunchers(launchers);
	}

	protected Object[] resolveVisibleLaunchers(List launchers) {
		List visibleLaunchers= new ArrayList(2);
		Iterator itr= launchers.iterator();
		while (itr.hasNext()) {
			ILauncher launcher= (ILauncher)itr.next();
			if (DebugUIPlugin.getDefault().isVisible(launcher)) {
				//cannot use itr.remove() as the list may be a fixed size list
				visibleLaunchers.add(launcher);
			}
		}
		return visibleLaunchers.toArray();
	}
	
	protected Object[] resolveWizardLaunchers(Object[] launchers) {
		List wizardLaunchers= new ArrayList(2);
		for (int i= 0 ; i < launchers.length; i++) {
			ILauncher launcher= (ILauncher)launchers[i];
			if (DebugUIPlugin.getDefault().hasWizard(launcher)) {
				wizardLaunchers.add(launcher);
			}
		}
		return wizardLaunchers.toArray();
	}

	/**
	 * If the selection contains re-launchables, a relaunch is performed
	 * for each launch and true is returned, otherwise, false is returned.
	 */
	protected boolean attemptRelaunch(IStructuredSelection selection) {
		// if the selection is a debug element, system process, or launch, do a relaunch
		Iterator objects= selection.iterator();
		List relaunchables= null;
		while (objects.hasNext()) {
			Object object= objects.next();
			ILaunch launch= null;
			if (object instanceof IDebugElement) {
				launch= ((IDebugElement)object).getLaunch();
			} else if (object instanceof ILaunch) {
				launch= (ILaunch)object;
			} else if (object instanceof IProcess) {
				launch= ((IProcess)object).getLaunch();
			}
			if (launch != null) {
				if (relaunchables == null) {
					relaunchables= new ArrayList(1);
					relaunchables.add(launch);
				} else if (!relaunchables.contains(launch)) {
					relaunchables.add(launch);
				}
			}
		}
		if (relaunchables == null) {
			return false;
		} else {
			Iterator itr= relaunchables.iterator();
			while (itr.hasNext()) {
				relaunch((ILaunch)itr.next(), getMode());
			}
			return true;
		}
	}

	/**
	 * Use the wizard to do the launch.
	 */
	protected void useWizard(Object[] launchers, Shell shell, IStructuredSelection selection) {
		launchers= resolveWizardLaunchers(launchers);
		LaunchWizard lw= new LaunchWizard(launchers, selection, getMode());
		LaunchWizardDialog dialog= new LaunchWizardDialog(shell, lw);
		dialog.open();
	}

	/**
	 * Ring the bell
	 */
	protected void beep() {
		Display display= Display.getCurrent();
		if (display != null) {
			display.beep();
		}
	}
}

