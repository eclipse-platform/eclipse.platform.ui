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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.help.ViewContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * This view shows the breakpoints registered with the breakpoint manager
 */
public class BreakpointsView extends AbstractDebugView implements IDoubleClickListener {
	
	/**
	 * The various actions of the context menu of this view
	 */
	private OpenMarkerAction fOpenMarkerAction;
	private RemoveBreakpointAction fRemoveBreakpointAction;
	private RemoveAllBreakpointsAction fRemoveAllBreakpointsAction;
	private EnableDisableBreakpointAction fEnableDisableBreakpointAction;
	private ShowQualifiedAction fShowQualifiedNamesAction;
	private Vector fBreakpointListenerActions;
	
	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		setViewer(new TableViewer(parent, SWT.MULTI| SWT.H_SCROLL | SWT.V_SCROLL));
		getViewer().setContentProvider(new BreakpointsContentProvider());
		getViewer().setLabelProvider(new DelegatingModelPresentation());
		getViewer().setSorter(new WorkbenchViewerSorter());
		initializeActions();
		initializeToolBar();

		createContextMenu(((TableViewer)getViewer()).getTable());
				
		getViewer().setInput(DebugPlugin.getDefault().getBreakpointManager());
		getViewer().addDoubleClickListener(this);
		getViewer().getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		
		WorkbenchHelp.setHelp(
			parent,
			new ViewContextComputer(this, IDebugHelpContextIds.BREAKPOINT_VIEW));
			
		// Necessary so that the PropertySheetView hears about selections in this view
		getSite().setSelectionProvider(getViewer());
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (getViewer() != null) {
			getViewer().removeDoubleClickListener(this);
		}
		cleanupActions();
	}

	/**
	 * Initializes the actions of this view
	 */
	protected void initializeActions() {
		setBreakpointListenerActions(new Vector(2));		
		setRemoveBreakpointAction(new RemoveBreakpointAction(getViewer()));
		
		setRemoveAllBreakpointsAction(new RemoveAllBreakpointsAction());
		boolean enable= DebugPlugin.getDefault().getBreakpointManager().getBreakpoints().length == 0 ? false : true;
		getRemoveAllBreakpointsAction().setEnabled(enable);
		
		setShowQualifiedNamesAction(new ShowQualifiedAction(getViewer()));
		getShowQualifiedNamesAction().setChecked(true);
		
		setOpenMarkerAction(new OpenBreakpointMarkerAction(getViewer()));

		setEnableDisableBreakpointAction(new EnableDisableBreakpointAction(getViewer()));
		addBreakpointListenerAction(getEnableDisableBreakpointAction());
		addBreakpointListenerAction(getRemoveAllBreakpointsAction());
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
		
		for (int i=0; i < fBreakpointListenerActions.size(); i++) {
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
		menu.add(getOpenMarkerAction());
		menu.add(new Separator(IDebugUIConstants.EMPTY_BREAKPOINT_GROUP));
		menu.add(new Separator(IDebugUIConstants.BREAKPOINT_GROUP));
		menu.add(getEnableDisableBreakpointAction());
		menu.add(getRemoveBreakpointAction());
		menu.add(getRemoveAllBreakpointsAction());
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(getShowQualifiedNamesAction());
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * Add an action to the contributed actions collection
	 * 
	 * @param action The action to add to the collection.
	 */
	public void addBreakpointListenerAction(IBreakpointListener action) {
		getBreakpointListenerActions().add(action);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(action);		
	}	
	
	/**
	 * @see IDoubleClickListener#doubleClick(DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		getOpenMarkerAction().run();
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
		tbm.add(getRemoveBreakpointAction());
		tbm.add(getRemoveAllBreakpointsAction());
		tbm.add(getOpenMarkerAction());
		tbm.add(getShowQualifiedNamesAction());
	}
	
	/**
	 * Handles key events in viewer.  Specifically interested in
	 * the Delete key.
	 * 
	 * @param event The key event that has occurred.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0 
			&& getRemoveBreakpointAction().isEnabled()) {
				getRemoveBreakpointAction().run();
		}
	}
	
	protected Vector getBreakpointListenerActions() {
		return fBreakpointListenerActions;
	}

	protected void setBreakpointListenerActions(Vector breakpointListenerActions) {
		fBreakpointListenerActions = breakpointListenerActions;
	}

	protected EnableDisableBreakpointAction getEnableDisableBreakpointAction() {
		return fEnableDisableBreakpointAction;
	}

	protected void setEnableDisableBreakpointAction(EnableDisableBreakpointAction enableDisableBreakpointAction) {
		fEnableDisableBreakpointAction = enableDisableBreakpointAction;
	}

	protected OpenMarkerAction getOpenMarkerAction() {
		return fOpenMarkerAction;
	}

	protected void setOpenMarkerAction(OpenMarkerAction openMarkerAction) {
		fOpenMarkerAction = openMarkerAction;
	}

	protected RemoveAllBreakpointsAction getRemoveAllBreakpointsAction() {
		return fRemoveAllBreakpointsAction;
	}

	protected void setRemoveAllBreakpointsAction(RemoveAllBreakpointsAction removeAllBreakpointsAction) {
		fRemoveAllBreakpointsAction = removeAllBreakpointsAction;
	}

	protected RemoveBreakpointAction getRemoveBreakpointAction() {
		return fRemoveBreakpointAction;
	}

	protected void setRemoveBreakpointAction(RemoveBreakpointAction removeBreakpointAction) {
		fRemoveBreakpointAction = removeBreakpointAction;
	}

	protected ShowQualifiedAction getShowQualifiedNamesAction() {
		return fShowQualifiedNamesAction;
	}

	protected void setShowQualifiedNamesAction(ShowQualifiedAction showQualifiedNamesAction) {
		fShowQualifiedNamesAction = showQualifiedNamesAction;
	}
}
