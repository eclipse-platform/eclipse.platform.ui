package org.eclipse.debug.internal.ui.views.variables;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Updates the variables view
 */
public class VariablesViewEventHandler extends AbstractDebugEventHandler {
	
	private boolean fViewVisible= true;
	
	/**
	 * The part listener for this view. Set to <code>null</code> when this view
	 * isn't currently listening to part changes.
	 */
	private VariablesViewPartListener fPartListener= null;
	
	/**
	 * Part listener that disables updating when the variables view is not
	 * visible and reenables updating when the view appears.
	 */
	private class VariablesViewPartListener implements IPartListener2 {
		private void visibilityChanged(IWorkbenchPartReference ref) {
			IWorkbenchPart part= ref.getPart(false);
			if (part != null && part == getView()) {
				boolean isVisible= isViewVisible();
				if (isVisible != fViewVisible) {
					fViewVisible= isVisible;
					if (isVisible) {
						refresh();
					}
				}
			}
		}
		public void partActivated(IWorkbenchPartReference ref) {
			visibilityChanged(ref);
		}
		public void partBroughtToTop(IWorkbenchPartReference ref) {
			visibilityChanged(ref);
		}
		public void partOpened(IWorkbenchPartReference ref) {
			visibilityChanged(ref);
		}
		public void partVisible(IWorkbenchPartReference ref) {
			visibilityChanged(ref);
		}
		public void partHidden(IWorkbenchPartReference ref) {
			visibilityChanged(ref);
		}
		public void partClosed(IWorkbenchPartReference ref) {
			visibilityChanged(ref);
		}
		public void partDeactivated(IWorkbenchPartReference ref) {
			visibilityChanged(ref);
		}
	}
	
	/**
	 * Constructs a new event handler on the given view
	 * 
	 * @param view variables view
	 */
	public VariablesViewEventHandler(AbstractDebugView view) {
		super(view);
		// The launch listener registers and deregisters the view's part listener when there are active launches
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(new ILaunchesListener() {
			/**
			 * Stop listening when all launches are removed
			 */
			public void launchesRemoved(ILaunch[] launches) {
				ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
				if (manager.getLaunches().length == 0) {
					deregisterPartListener();
				}
			}
			/**
			 * Start listening to part activations when a launch is added.
			 */
			public void launchesAdded(ILaunch[] launches) {
				registerPartListener();
			}
			public void launchesChanged(ILaunch[] launches) {
			}
		});
		fViewVisible= isViewVisible();
		// if there are already launches, must add a part listener
		if (DebugPlugin.getDefault().getLaunchManager().getLaunches().length > 0) {
			registerPartListener();
		}
	}
	
	/**
	 * Creates and registers a part listener with this event handler's page,
	 * if one does not already exist.
	 */
	protected void registerPartListener() {
		if (fPartListener == null) {
			fPartListener= new VariablesViewPartListener();
			getView().getSite().getPage().addPartListener(new VariablesViewPartListener());
		}		
	}
	
	/**
	 * Deregisters and disposes this event handler's part listener.
	 */
	protected void deregisterPartListener() {
		if (fPartListener != null) {
			getView().getSite().getPage().removePartListener(fPartListener);
			fPartListener = null;
		}
	}
	
	/**
	 * Returns the active workbench page or <code>null</code> if none.
	 */
	protected IWorkbenchPage getActivePage() {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		return window.getActivePage();
	}
	
	/**
	 * @see AbstractDebugEventHandler#handleDebugEvents(DebugEvent[])
	 */
	protected void doHandleDebugEvents(DebugEvent[] events) {
		if (!fViewVisible) {
			return;
		}
		for (int i = 0; i < events.length; i++) {	
			DebugEvent event = events[i];
			switch (event.getKind()) {
				case DebugEvent.SUSPEND:
					if (fViewVisible) {
						doHandleSuspendEvent(event);
					}
					break;
				case DebugEvent.CHANGE:
					if (fViewVisible) {
						doHandleChangeEvent(event);
					}
					break;
				case DebugEvent.RESUME:
					if (fViewVisible) {
						doHandleResumeEvent(event);
					}
					break;
				case DebugEvent.TERMINATE:
					// Always handle terminate events so we can deregister the part
					// listener even if the view isn't visible.
					doHandleTerminateEvent(event);
					break;
			}
		}
	}

	/**
	 * Clear the variables immediately upon resume.
	 */
	protected void doHandleResumeEvent(DebugEvent event) {
		if (!event.isStepStart() && !event.isEvaluation()) {
			// Clear existing variables from the view
			getVariablesView().setViewerInput(StructuredSelection.EMPTY);
		}
	}

	/**
	 * Clear any cached variable expansion state for the
	 * terminated thread/target. Also, remove the part listener if there are
	 * no more active debug targets.
	 */
	protected void doHandleTerminateEvent(DebugEvent event) {
		if (fViewVisible) {
			getVariablesView().clearExpandedVariables(event.getSource());
		}
	}
	
	/**
	 * Process a SUSPEND event
	 */
	protected void doHandleSuspendEvent(DebugEvent event) {
		if (event.getDetail() != DebugEvent.EVALUATION_IMPLICIT) {
			// Don't refresh everytime an implicit evaluation finishes
			if (event.getSource() instanceof ISuspendResume) {
				if (!((ISuspendResume)event.getSource()).isSuspended()) {
					// no longer suspended
					return;
				}
			}
			refresh();
			if (event.getDetail() == DebugEvent.STEP_END) {
				getVariablesView().populateDetailPane();
			}
		}		
	}
	
	/**
	 * Process a CHANGE event
	 */
	protected void doHandleChangeEvent(DebugEvent event) {
		if (event.getDetail() == DebugEvent.STATE) {
			// only process variable state changes
			if (event.getSource() instanceof IVariable) {
				refresh(event.getSource());
			}
		} else {
			refresh();
		}	
	}	

	protected VariablesView getVariablesView() {
		return (VariablesView)getView();
	}
	
}

