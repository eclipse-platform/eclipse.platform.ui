/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public abstract class AbstractDebugActionDelegate implements IWorkbenchWindowActionDelegate, IViewActionDelegate, ISelectionListener, INullSelectionListener {
	
	/**
	 * The underlying action for this delegate
	 */
	private IAction fAction;
	/**
	 * This action's view part, or <code>null</code>
	 * if not installed in a view.
	 */
	private IViewPart fViewPart;
	
	/**
	 * Cache of the most recent seletion
	 */
	private IStructuredSelection fSelection;
	
	/**
	 * Whether this delegate has been initialized
	 */
	private boolean fInitialized = false;
	
	/**
	 * The window associated with this action delegate
	 * May be <code>null</code>
	 */
	protected IWorkbenchWindow fWindow;
	
	/**
	 * The background delegate manager keeps track of the debug
	 * actions delegates which do their work in a background job.
	 * The manager disables these delegates while one of them is
	 * running.
	 */
	protected static class BackgroundDelegateManager {
		private List fBackgroundDelegates= new ArrayList();
		private boolean fIsJobRunning= false;
		
		/**
		 * Registers the given delegate with this manager
		 * @param delegate
		 */
		public void addBackgroundDelegate(AbstractDebugActionDelegate delegate) {
			fBackgroundDelegates.add(delegate);
		}
		
		/**
		 * A background job has been started for one of the action delegates.
		 * Disable all background delegates.
		 */
		public void jobStarted() {
			fIsJobRunning= true;
			Iterator iter= fBackgroundDelegates.iterator();
			while (iter.hasNext()) {
				AbstractDebugActionDelegate delegate= (AbstractDebugActionDelegate) iter.next();
				delegate.getAction().setEnabled(false);
			}
		}
		
		/**
		 * A background job has finished for one of the action delegates.
		 * Update the enabled state of all background delegates.
		 */
		public void jobStopped() {
			fIsJobRunning= false;
			Iterator iter= fBackgroundDelegates.iterator();
			while (iter.hasNext()) {
				AbstractDebugActionDelegate delegate= (AbstractDebugActionDelegate) iter.next();
				delegate.update(delegate.getAction(), delegate.getSelection());
			}
		}

		/**
		 * @return
		 */
		public boolean isJobRunning() {
			return fIsJobRunning;
		}
	}
	
	/**
	 * The background delegate manager which disables and reenables background action delegates.
	 */
	protected static BackgroundDelegateManager fgBackgroundActionManager= new BackgroundDelegateManager();
	
	/**
	 * It's crucial that delegate actions have a zero-arg constructor so that
	 * they can be reflected into existence when referenced in an action set
	 * in the plugin's plugin.xml file.
	 */
	public AbstractDebugActionDelegate() {
		if (isRunInBackground()) {
			fgBackgroundActionManager.addBackgroundDelegate(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose(){
		if (getWindow() != null) {
			getWindow().getSelectionService().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window){
		// listen to selection changes in the debug view
		setWindow(window);
		window.getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action){
		Iterator selectionIter= getSelection().iterator();
		
		String pluginId= DebugUIPlugin.getUniqueIdentifier();
		MultiStatus status= 
			new MultiStatus(pluginId, DebugException.REQUEST_FAILED, getStatusMessage(), null); 
		if (isRunInBackground()) {
			runInBackground(action, selectionIter, status);
		} else {
			runInForeground(selectionIter, status);
		}		
	}
	
	/**
	 * Runs this action in a background job.
	 */
	private void runInBackground(IAction action, final Iterator selectionIter, final MultiStatus status) {
		Job job= new Job(action.getText()) {
			protected IStatus run(IProgressMonitor monitor) {
				while (selectionIter.hasNext()) {
					Object element= selectionIter.next();
					try {
						doAction(element);
					} catch (DebugException e) {
						status.merge(e.getStatus());
					}
				}
				DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
					public void run() {
						fgBackgroundActionManager.jobStopped();
					}
				});
				return status;
			}
		};
		fgBackgroundActionManager.jobStarted();
		job.schedule();
	}
	
	/**
	 * Runs this action in the UI thread.
	 */
	private void runInForeground(final Iterator selectionIter, final MultiStatus status) {
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				while (selectionIter.hasNext()) {
					Object element= selectionIter.next();
					try {
						doAction(element);
					} catch (DebugException e) {
						status.merge(e.getStatus());
					}
				}
			}
		});
		reportErrors(status);
	}

	private void reportErrors(final MultiStatus ms) {
		if (!ms.isOK()) {
			IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
			if (window != null) {
				DebugUIPlugin.errorDialog(window.getShell(), getErrorDialogTitle(), getErrorDialogMessage(), ms);
			} else {
				DebugUIPlugin.log(ms);
			}
		}
	}

	/**
	 * Returns whether or not this action should be run in the background.
	 * Subclasses may override.
	 * @return whether or not this action should be run in the background
	 */
	protected boolean isRunInBackground() {
		return false;
	}

	/**
	 * AbstractDebugActionDelegates come in 2 flavors: IViewActionDelegate, 
	 * IWorkbenchWindowActionDelegate delegates.
	 * </p>
	 * <ul>
	 * <li>IViewActionDelegate delegate: getView() != null</li>
	 * <li>IWorkbenchWindowActionDelegate: getView == null</li>
	 * </ul>
	 * <p>
	 * Only want to call update(action, selection) for IViewActionDelegates.
	 * An initialize call to update(action, selection) is made for all flavors to set the initial
	 * enabled state of the underlying action.
	 * IWorkbenchWindowActionDelegate's listen to selection changes
	 * in the debug view only.
	 * </p>
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection s) {
		boolean wasInitialized= initialize(action, s);		
		if (!wasInitialized) {
			if (getView() != null) {
				update(action, s);
			}
		}
	}
	
	protected void update(IAction action, ISelection s) {
		if (isRunInBackground() && fgBackgroundActionManager.isJobRunning()) {
			// Don't update enablement of background delegates while a job is running.
			return;
		}
		if (s instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)s;
			action.setEnabled(getEnableStateForSelection(ss));
			setSelection(ss);
		} else {
			action.setEnabled(false);
			setSelection(StructuredSelection.EMPTY);
		}
	}
	
	/**
	 * Performs the specific action on this element.
	 */
	protected abstract void doAction(Object element) throws DebugException;

	/**
	 * Returns the String to use as an error dialog title for
	 * a failed action. Default is to return null.
	 */
	protected String getErrorDialogTitle(){
		return null;
	}
	/**
	 * Returns the String to use as an error dialog message for
	 * a failed action. This message appears as the "Message:" in
	 * the error dialog for this action.
	 * Default is to return null.
	 */
	protected String getErrorDialogMessage(){
		return null;
	}
	/**
	 * Returns the String to use as a status message for
	 * a failed action. This message appears as the "Reason:"
	 * in the error dialog for this action.
	 * Default is to return the empty String.
	 */
	protected String getStatusMessage(){
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		fViewPart = view;
	}
	
	/**
	 * Returns this action's view part, or <code>null</code>
	 * if not installed in a view.
	 * 
	 * @return view part or <code>null</code>
	 */
	protected IViewPart getView() {
		return fViewPart;
	}

	/**
	 * Initialize this delegate, updating this delegate's
	 * presentation.
	 * As well, all of the flavors of AbstractDebugActionDelegates need to 
	 * have the initial enabled state set with a call to update(IAction, ISelection).
	 * 
	 * @param action the presentation for this action
	 * @return whether the action was initialized
	 */
	protected boolean initialize(IAction action, ISelection selection) {
		if (!isInitialized()) {
			setAction(action);
			if (getView() == null) {
				//update on the selection in the debug view
				IWorkbenchWindow window= getWindow();
				if (window != null && window.getShell() != null && !window.getShell().isDisposed()) {
					IWorkbenchPage page= window.getActivePage();
					if (page != null) {
						selection= page.getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
					}
				}
			}
			update(action, selection);
			setInitialized(true);
			return true;
		}
		return false;
	}

	/**
	 * Returns the most recent selection
	 * 
	 * @return structured selection
	 */	
	protected IStructuredSelection getSelection() {
		if (getView() != null) {
			//cannot used the cached selection in a view
			//as the selection can be out of date for context menu
			//actions. See bug 14556
			ISelection s= getView().getViewSite().getSelectionProvider().getSelection();
			if (s instanceof IStructuredSelection) {
				return (IStructuredSelection)s;
			} 
			return StructuredSelection.EMPTY;
		}
		return fSelection;
	}
	
	/**
	 * Sets the most recent selection
	 * 
	 * @parm selection structured selection
	 */	
	private void setSelection(IStructuredSelection selection) {
		fSelection = selection;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		update(getAction(), selection);
	}
	
	protected void setAction(IAction action) {
		fAction = action;
	}

	protected IAction getAction() {
		return fAction;
	}
	
	protected void setView(IViewPart viewPart) {
		fViewPart = viewPart;
	}
	
	protected boolean isInitialized() {
		return fInitialized;
	}

	protected void setInitialized(boolean initialized) {
		fInitialized = initialized;
	}

	protected IWorkbenchWindow getWindow() {
		return fWindow;
	}

	protected void setWindow(IWorkbenchWindow window) {
		fWindow = window;
	}
	
	/**
	 * Return whether the action should be enabled or not based on the given selection.
	 */
	protected boolean getEnableStateForSelection(IStructuredSelection selection) {
		if (selection.size() == 0) {
			return false;
		}
		Iterator itr= selection.iterator();
		while (itr.hasNext()) {
			Object element= itr.next();
			if (!isEnabledFor(element)) {
				return false;
			}
		}
		return true;		
	}

	protected boolean isEnabledFor(Object element) {
		return true;
	}
}