package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.EnableDisableBreakpointAction;
import org.eclipse.debug.internal.ui.actions.OpenBreakpointMarkerAction;
import org.eclipse.debug.internal.ui.actions.RemoveAllBreakpointsAction;
import org.eclipse.debug.internal.ui.actions.RemoveBreakpointAction;
import org.eclipse.debug.internal.ui.actions.ShowBreakpointsForModelAction;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * This view shows the breakpoints registered with the breakpoint manager
 */
public class BreakpointsView extends AbstractDebugView {
	
	class BreakpointsSorter extends ViewerSorter {
		/**
		 * @see ViewerSorter#isSorterProperty(Object, String)
		 */
		public boolean isSorterProperty(Object element,String propertyId) {
			return propertyId.equals(IBasicPropertyConstants.P_TEXT);
		}
		
		/**
		 * Returns a negative, zero, or positive number depending on whether
		 * the first element is less than, equal to, or greater than
		 * the second element.
		 * <p>
		 * Group breakpoints by debug model
		 * 	within debug model, group breakpoints by type 
		 * 		within type groups, sort by line number (if applicable) and then
		 * 		alphabetically by label
		 * 
		 * @param viewer the viewer
		 * @param e1 the first element
		 * @param e2 the second element
		 * @return a negative number if the first element is less than the 
		 *  second element; the value <code>0</code> if the first element is
		 *  equal to the second element; and a positive number if the first
		 *  element is greater than the second element
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {
	
			IBreakpoint b1= (IBreakpoint)e1;
			IBreakpoint b2= (IBreakpoint)e2;
			String modelId1= b1.getModelIdentifier();
			String modelId2= b2.getModelIdentifier();
			int result= modelId1.compareTo(modelId2);
			if (result != 0) {
				return result;
			}
			String type1= ""; //$NON-NLS-1$
			String type2= ""; //$NON-NLS-1$
			try {
				type1= b1.getMarker().getType();
			} catch (CoreException ce) {
				DebugUIPlugin.logError(ce);
			}
			try {
				type2= b2.getMarker().getType();	
			} catch (CoreException e) {
				DebugUIPlugin.logError(e);
			}
		
			result= type1.compareTo(type2);
			if (result != 0) {
				return result;
			}
			// model and type are the same
		
			ILabelProvider lprov = (ILabelProvider) ((StructuredViewer)viewer).getLabelProvider();
			String name1= lprov.getText(e1);
			String name2= lprov.getText(e2);
	
			boolean lineBreakpoint= false;
			try {
				lineBreakpoint= b1.getMarker().isSubtypeOf(IBreakpoint.LINE_BREAKPOINT_MARKER);
			} catch (CoreException ce) {
				DebugUIPlugin.logError(ce);
			}
			if (lineBreakpoint) {
				return compareLineBreakpoints(b1, b2, name1,name2);
			} 
			
			return name1.compareTo(name2);		
		}
		
		protected int compareLineBreakpoints(IBreakpoint b1, IBreakpoint b2, String name1, String name2) {
			int colon1= name1.indexOf(':');
			if (colon1 != -1) {
				int colon2= name2.indexOf(':');
				if (colon2 != -1) {
					String upToColon1= name1.substring(0, colon1);
					if (name2.startsWith(upToColon1)) {
						int l1= 0;
						int l2= 0;
						try {
							l1= ((ILineBreakpoint)b1).getLineNumber();	
						} catch (CoreException e) {
							DebugUIPlugin.logError(e);
						}
						try {
							l2= ((ILineBreakpoint)b2).getLineNumber();	
						} catch (CoreException e) {
							DebugUIPlugin.logError(e);
						}
						return l1 - l2;
					}
				}
			}
			return name1.compareTo(name2);
		}
	}

	private BreakpointsViewEventHandler fEventHandler;
	
	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		StructuredViewer viewer = new TableViewer(parent, SWT.MULTI| SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new BreakpointsViewContentProvider());
		viewer.setLabelProvider(new DelegatingModelPresentation());
		viewer.setSorter(new BreakpointsSorter());
		viewer.setInput(DebugPlugin.getDefault().getBreakpointManager());		
		// Necessary so that the PropertySheetView hears about selections in this view
		getSite().setSelectionProvider(viewer);
		setEventHandler(new BreakpointsViewEventHandler());
		return viewer;
	}	
	
	/**
	 * @see AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.BREAKPOINT_VIEW;
	}

	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		IAction action= getAction("ShowBreakpointsForModel"); //$NON-NLS-1$
		if (action != null) {
			((ShowBreakpointsForModelAction)action).dispose(); 
		}
		
		super.dispose();
		
		if (getEventHandler() != null) {
			getEventHandler().dispose();
		}
	}

	/**
	 * @see AbstractDebugView#createActions()
	 */
	protected void createActions() {
		
		setAction(REMOVE_ACTION, new RemoveBreakpointAction(getViewer()));
			
		IAction action = new RemoveAllBreakpointsAction();
		action.setEnabled(DebugPlugin.getDefault().getBreakpointManager().getBreakpoints().length == 0 ? false : true);
		setAction("RemoveAll", action); //$NON-NLS-1$
		
		action = new OpenBreakpointMarkerAction(getViewer());
		setAction("GotoMarker", action); //$NON-NLS-1$
		setAction(DOUBLE_CLICK_ACTION, action);
		
		setAction("EnableDisableBreakpoint", new EnableDisableBreakpointAction(getViewer())); //$NON-NLS-1$
		
		setAction("ShowBreakpointsForModel", new ShowBreakpointsForModelAction(getStructuredViewer(),this)); //$NON-NLS-1$
	}

	/**
	 * Adds items to the context menu.
	 * 
	 * @param menu The menu to contribute to
	 */
	protected void fillContextMenu(IMenuManager menu) {
		updateActions();
		menu.add(new Separator(IDebugUIConstants.EMPTY_NAVIGATION_GROUP));
		menu.add(new Separator(IDebugUIConstants.NAVIGATION_GROUP));
		menu.add(getAction("GotoMarker")); //$NON-NLS-1$
		menu.add(new Separator(IDebugUIConstants.EMPTY_BREAKPOINT_GROUP));
		menu.add(new Separator(IDebugUIConstants.BREAKPOINT_GROUP));
		menu.add(getAction("EnableDisableBreakpoint")); //$NON-NLS-1$
		menu.add(getAction(REMOVE_ACTION));
		menu.add(getAction("RemoveAll")); //$NON-NLS-1$
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(getAction("ShowBreakpointsForModel")); //$NON-NLS-1$
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(getAction("ShowBreakpointsForModel")); //$NON-NLS-1$
		tbm.add(getAction(REMOVE_ACTION));
		tbm.add(getAction("RemoveAll")); //$NON-NLS-1$
		tbm.add(getAction("GotoMarker")); //$NON-NLS-1$
		tbm.add(new Separator(IDebugUIConstants.RENDER_GROUP));
	}

	/**
	 * Provides the contents for a this view
	 */
	class BreakpointsViewContentProvider implements IStructuredContentProvider {

		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object parent) {
			return ((IBreakpointManager) parent).getBreakpoints();
		}
		
		/**
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}
	
		/**
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	/**
	 * Handles breakpoint events, updating the breakpoints view
	 * and viewer.
	 */
	class BreakpointsViewEventHandler implements IBreakpointListener {
	
		/**
		 * Constructs an enent handler for the breakpoints view.
		 */
		public BreakpointsViewEventHandler() {
			DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		}
		
		public void dispose() {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		}
	
	
		/**
		 * @see IBreakpointListener#breakpointAdded(IBreakpoint)
		 */
		public void breakpointAdded(final IBreakpoint breakpoint) {
			if (breakpoint.getMarker().exists()) {		
				asyncExec(new Runnable() {
					public void run() {
						((TableViewer)getViewer()).add(breakpoint);
						updateActions();
					}
				});
			}
		}
	
	
		/**
		 * @see IBreakpointListener#breakpointRemoved(IBreakpoint, IMarkerDelta)
		 */
		public void breakpointRemoved(final IBreakpoint breakpoint, IMarkerDelta delta) {
			asyncExec(new Runnable() {
				public void run() {
					((TableViewer)getViewer()).remove(breakpoint);
					updateActions();
				}
			});
		}
	
		/**
		 * @see IBreakpointListener#breakpointChanged(IBreakpoint, IMarkerDelta)
		 */
		public void breakpointChanged(final IBreakpoint breakpoint, IMarkerDelta delta) {
			if (breakpoint.getMarker().exists()) {
				asyncExec(new Runnable() {
					public void run() {
						((TableViewer)getViewer()).refresh(breakpoint);
						updateActions();
					}
				});
			}
		}
	}
	
	/**
	 * Returns this view's event handler
	 * 
	 * @return a breakpoint view event handler
	 */
	protected BreakpointsViewEventHandler getEventHandler() {
		return fEventHandler;
	}

	/**
	 * Sets this view's event handler.
	 * 
	 * @param eventHandler a breakpoint view event handler
	 */
	private void setEventHandler(BreakpointsViewEventHandler eventHandler) {
		fEventHandler = eventHandler;
	}
}
