package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegateWithEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This is the debug action which appears in the desktop menu and toolbar.
 */
public abstract class ExecutionAction implements IActionDelegateWithEvent {
	
	/**
	 * @see IActionDelegateWithEvent#runWithEvent(IAction, Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		runLaunchConfiguration();
	}
	
	private void runLaunchConfiguration() {
		IWorkbenchWindow dwindow= DebugUIPlugin.getActiveWorkbenchWindow();
		IStructuredSelection selection= resolveSelection(dwindow);
		LaunchConfigurationDialog lcd = new LaunchConfigurationDialog(DebugUIPlugin.getShell(), selection, getMode());		
		lcd.open();
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
	protected static IStructuredSelection resolveSelection(IWorkbenchWindow window) {
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
	 * Resolves and returns the applicable project(s) associated with the
	 * elements in the specified selection.
	 */
	protected static IProject[] resolveProjects(IStructuredSelection selection) {
		
		if (selection == null || selection.isEmpty()) {
			return new IProject[0];
		} else {
			Vector projects = new Vector(1);
			Iterator elements= selection.iterator();
			while (elements.hasNext()) {
				Object element= elements.next();
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
				if (project != null && !projects.contains(project)) {
					projects.add(project);
				}
			}
			IProject[] list= new IProject[projects.size()];
			projects.copyInto(list);
			return list;
		}
		
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
	 * Ring the bell
	 */
	protected void beep() {
		Display display= Display.getCurrent();
		if (display != null) {
			display.beep();
		}
	}
	
	/**
	 * @see runWithEvent(IAction, Event)
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart part) {
	}
}