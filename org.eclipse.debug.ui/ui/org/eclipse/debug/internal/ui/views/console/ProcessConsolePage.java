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
package org.eclipse.debug.internal.ui.views.console;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;

/**
 * A page for a console connected to I/O streams of a process
 * 
 * @since 3.0
 */
public class ProcessConsolePage extends IOConsolePage implements  IShowInSource, IShowInTargetList, IDebugEventSetListener, ISelectionListener {

	// scroll lock
	private boolean fIsLocked = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_CONSOLE_SCROLL_LOCK);
	
	// actions
	private ConsoleTerminateAction fTerminate;
	private ConsoleRemoveAllTerminatedAction fRemoveTerminated;

	
	
	/**
	 * Constructs a new process page 
	 */
	public ProcessConsolePage(IConsoleView view, ProcessConsole console) {
	    super(console, view);
	    setAutoScroll(!fIsLocked);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
	    super.createControl(parent);	    
		getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
	}

	/**
	 * Fill the context menu
	 * 
	 * @param menu menu
	 */
	protected void contextMenuAboutToShow(IMenuManager menu) {
	    super.contextMenuAboutToShow(menu);
		menu.add(fTerminate);	
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#dispose()
	 */
	public void dispose() {
	    super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(this);
		getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);

		if (fRemoveTerminated != null) {
			fRemoveTerminated.dispose();
		}
	}

	protected void createActions() {
	    super.createActions();
		fRemoveTerminated = new ConsoleRemoveAllTerminatedAction();
		ProcessConsole console = (ProcessConsole) getConsole();
		fTerminate = new ConsoleTerminateAction(console);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
			
		
	protected void configureToolBar(IToolBarManager mgr) {
	    super.configureToolBar(mgr);
		mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fTerminate);
		mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fRemoveTerminated);
	}

	/**
	 * Returns the process associated with this page
	 * 
	 * @return the process associated with this page
	 */
	protected IProcess getProcess() {
		return ((ProcessConsole)getConsole()).getProcess();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (getProcess().equals(DebugUITools.getCurrentProcess())) {
			getConsoleView().display(getConsole());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class required) {
	    Object adapter = super.getAdapter(required);
	    if (adapter == null) {
			if (IShowInSource.class.equals(required)) {
				return this;
			}
			if (IShowInTargetList.class.equals(required)) {
				return this; 
			}
	    }
		return null;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IShowInSource#getShowInContext()
	 */
	public ShowInContext getShowInContext() {
		IProcess process = getProcess();
		if (process == null) {
			return null;
		} 
		IDebugTarget target = (IDebugTarget)process.getAdapter(IDebugTarget.class);
		ISelection selection = null;
		if (target == null) {
			selection = new StructuredSelection(process);
		} else {
			selection = new StructuredSelection(target);
		}
		return new ShowInContext(null, selection);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IShowInTargetList#getShowInTargetIds()
	 */
	public String[] getShowInTargetIds() {
		return new String[] {IDebugUIConstants.ID_DEBUG_VIEW};
	}
	
	/**
	 * Update terminate action.
	 * 
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getSource().equals(getProcess())) {
				Runnable r = new Runnable() {
					public void run() {
						if (isAvailable()) {
							fTerminate.update();
						}				
					}
				};
				if (isAvailable()) {				
					getControl().getDisplay().asyncExec(r);
				}
			}
		}
	}

	/**
	 * Returns whether this page's controls are available.
	 * 
	 * @return whether this page's controls are available
	 */
	protected boolean isAvailable() {
		return getControl() != null;
	}
	
}
