package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Vector;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * This view shows the breakpoints registered with the breakpoint manager
 */
public class BreakpointsView extends AbstractDebugView {
	
	private Vector fBreakpointListenerActions;
	
	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	protected StructuredViewer createViewer(Composite parent) {
		StructuredViewer viewer = new TableViewer(parent, SWT.MULTI| SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new BreakpointsContentProvider());
		viewer.setLabelProvider(new DelegatingModelPresentation());
		viewer.setSorter(new WorkbenchViewerSorter());
		viewer.setInput(DebugPlugin.getDefault().getBreakpointManager());		
		// Necessary so that the PropertySheetView hears about selections in this view
		getSite().setSelectionProvider(viewer);
		return viewer;
	}	
	
	/**
	 * @see AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.BREAKPOINT_VIEW;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		cleanupActions();
	}

	/**
	 * Initializes the actions of this view
	 */
	protected void createActions() {
		IAction action; 
		
		setAction(REMOVE_ACTION, new RemoveBreakpointAction(getViewer()));
		
		action = new RemoveAllBreakpointsAction();
		action.setEnabled(DebugPlugin.getDefault().getBreakpointManager().getBreakpoints().length == 0 ? false : true);
		setAction("RemoveAll", action);
		
		action = new ShowQualifiedAction(getViewer());
		action.setChecked(true);
		setAction("ShowQualifiedNames", action);
		
		action = new OpenBreakpointMarkerAction(getViewer());
		setAction("GotoMarker", action);
		setAction(DOUBLE_CLICK_ACTION, action);
		
		setAction("EnableDisableBreakpoint", new EnableDisableBreakpointAction(getViewer()));
		
		addBreakpointListenerAction(getAction("EnableDisableBreakpoint"));
		addBreakpointListenerAction(getAction("RemoveAll"));
	}

	/**
	 * Cleans up the actions when this part is disposed
	 */
	protected void cleanupActions() {
		if (getBreakpointListenerActions() == null) {
			return;
		}
		DebugPlugin dp= DebugPlugin.getDefault();
		IBreakpointManager bm= dp.getBreakpointManager();
		
		for (int i=0; i < getBreakpointListenerActions().size(); i++) {
			bm.removeBreakpointListener((IBreakpointListener)getBreakpointListenerActions().get(i));
		} 
	}	

	/**
	 * Adds items to the context menu
	 * 
	 * @param menu The menu to contribute to
	 */
	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator(IDebugUIConstants.EMPTY_NAVIGATION_GROUP));
		menu.add(new Separator(IDebugUIConstants.NAVIGATION_GROUP));
		menu.add(getAction("GotoMarker"));
		menu.add(new Separator(IDebugUIConstants.EMPTY_BREAKPOINT_GROUP));
		menu.add(new Separator(IDebugUIConstants.BREAKPOINT_GROUP));
		menu.add(getAction("EnableDisableBreakpoint"));
		menu.add(getAction(REMOVE_ACTION));
		menu.add(getAction("RemoveAll"));
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(getAction("ShowQualifiedNames"));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * Add an action to the contributed actions collection
	 * 
	 * @param action The action to add to the collection.
	 */
	public void addBreakpointListenerAction(IAction action) {
		getBreakpointListenerActions().add(action);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener((IBreakpointListener)action);		
	}	

	/**
	 * Returns an editor part that is open on this breakpoint's
	 * resource
	 * 
	 * @param marker The marker to use to find the correct editor
	 * @param page The page to check for the current editors
	 * @return The editor part that is open on the resource associated
	 * 		with the marker or <code>null</code> if no editor is open
	 * 		on this resource.
	 */
	protected IEditorPart getOpenEditor(IMarker marker, IWorkbenchPage page) {
		//attempt to find the editor for the input
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint breakpoint= manager.getBreakpoint(marker);
		String id= breakpoint.getModelIdentifier();
		IDebugModelPresentation presentation= getPresentation(id);
		IEditorInput editorElement = presentation.getEditorInput(marker);
		IEditorPart[] editors= page.getEditors();
		for (int i= 0; i < editors.length; i++) {
			IEditorPart part= editors[i];
			if (part.getEditorInput().equals(editorElement)) {
				page.bringToTop(part);
				return part;
			}
		}
		//did not find an open editor
		return null;
	}
	
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(getAction(REMOVE_ACTION));
		tbm.add(getAction("RemoveAll"));
		tbm.add(getAction("GotoMarker"));
		tbm.add(getAction("ShowQualifiedNames"));
	}
		
	protected Vector getBreakpointListenerActions() {
		if(fBreakpointListenerActions == null) {
			fBreakpointListenerActions= new Vector(2);
		}
		return fBreakpointListenerActions;
	}

}
