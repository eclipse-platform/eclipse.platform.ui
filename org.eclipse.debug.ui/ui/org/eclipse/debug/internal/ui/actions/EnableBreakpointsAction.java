package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.util.Iterator;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public class EnableBreakpointsAction implements IViewActionDelegate, IPartListener, IBreakpointListener {
	
	private IViewPart fView;
	private IAction fAction;
	
	public EnableBreakpointsAction() {
	}
		
	protected IViewPart getView() {
		return fView;
	}

	protected void setView(IViewPart view) {
		fView = view;
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		setView(view);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		view.getViewSite().getPage().addPartListener(this);
	}

	protected void update() {
		selectionChanged(getAction(), getView().getViewSite().getSelectionProvider().getSelection());
	}
	
	/**
	 * This action enables breakpoints.
	 */
	protected boolean isEnableAction() {
		return true;
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getSelection();
		int size= selection.size();
		if (size == 0) {
			return;
		}
		Iterator enum= selection.iterator();
		MultiStatus ms= new MultiStatus(DebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, ActionMessages.getString("EnableBreakpointAction.Enable_breakpoint(s)_failed_2"), null); //$NON-NLS-1$
		while (enum.hasNext()) {
			IBreakpoint breakpoint = (IBreakpoint) enum.next();
			try {
				if (size > 1) {
					if (isEnableAction()) {
						breakpoint.setEnabled(true);
					} else {
						breakpoint.setEnabled(false);
					}
				} else {
					breakpoint.setEnabled(!breakpoint.isEnabled());
				}
			} catch (CoreException e) {
				ms.merge(e.getStatus());
			}
		}
		if (!ms.isOK()) {
			IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
			if (window != null) {
				DebugUIPlugin.errorDialog(window.getShell(), ActionMessages.getString("EnableBreakpointAction.Enabling_breakpoints_3"), ActionMessages.getString("EnableBreakpointAction.Exceptions_occurred_enabling_the_breakpoint(s)._4"), ms); //$NON-NLS-2$ //$NON-NLS-1$
			} else {
				DebugUIPlugin.log(ms);
			}
		}
	}

	private IStructuredSelection getSelection() {
		return (IStructuredSelection)getView().getViewSite().getSelectionProvider().getSelection();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		IStructuredSelection sel= (IStructuredSelection)selection;
		Object o= sel.getFirstElement();
		if (!(o instanceof IBreakpoint)) {
			return;
		}
		
		Iterator enum= sel.iterator();
		IBreakpoint bp= (IBreakpoint)enum.next();
		if (!enum.hasNext()) {
			//single selection
			try {
				if (bp.isEnabled()) {
					action.setEnabled(!isEnableAction());
				} else {
					action.setEnabled(isEnableAction());
				}
			} catch (CoreException ce) {
				IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
				if (window != null) {
					DebugUIPlugin.errorDialog(window.getShell(), ActionMessages.getString("EnableBreakpointAction.Enabling_breakpoints_3"), ActionMessages.getString("EnableBreakpointAction.Exceptions_occurred_enabling_the_breakpoint(s)._4"), ce); //$NON-NLS-2$ //$NON-NLS-1$
				} else {
					DebugUIPlugin.log(ce);
				}
			}
		}
	}
	

	/**
	 * Removes this action as a breakpoint and part listener.
	 */
	public void dispose() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		getView().getViewSite().getPage().removePartListener(this);
	}
	
	/**
	 * @see IBreakpointListener#breakpointAdded(IBreakpoint)
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
	}
	
	
	/**
	 * @see IBreakpointListener#breakpointRemoved(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointRemoved(final IBreakpoint breakpoint, IMarkerDelta delta) {	
		asynchUpdate();
	}
	
	/**
	 * @see IBreakpointListener#breakpointChanged(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		asynchUpdate();
	}
	
	protected void asynchUpdate() {
		if (getAction() == null) {
			return;
		}
		IWorkbenchWindow window= getView().getViewSite().getPage().getWorkbenchWindow();
		if (window == null) {
			return;
		}
		Shell shell= window.getShell();
		if (shell == null || shell.isDisposed()) {
			return;
		}
		Runnable r= new Runnable() {
			public void run() {
				IWorkbenchWindow window= getView().getViewSite().getPage().getWorkbenchWindow();
				if (window == null) {
					return;
				}
				Shell shell= window.getShell();
				if (shell == null || shell.isDisposed()) {
					return;
				}
				update();
			}
		};
		
		shell.getDisplay().asyncExec(r);
	}
	
	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}
	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part.equals(getView())) {
			dispose();
		}
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}

}

