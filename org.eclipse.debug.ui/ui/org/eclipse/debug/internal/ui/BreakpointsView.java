package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.*;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.help.ViewContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchViewerSorter;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * This view shows the breakpoints registered with the breakpoint manager
 */
public class BreakpointsView extends AbstractDebugView implements IDoubleClickListener {
	
	protected final static String PREFIX= "breakpoints_view.";
	
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
	 * @see IWorkbenchPart
	 */
	public void createPartControl(Composite parent) {
		fViewer= new TableViewer(parent, SWT.MULTI| SWT.H_SCROLL | SWT.V_SCROLL);
		fViewer.setContentProvider(new BreakpointsContentProvider());
		fViewer.setLabelProvider(new DelegatingModelPresentation());
		fViewer.setSorter(new WorkbenchViewerSorter());
		initializeActions();
		initializeToolBar();

		createContextMenu(((TableViewer)fViewer).getTable());
				
		fViewer.setInput(DebugPlugin.getDefault().getBreakpointManager());
		fViewer.addDoubleClickListener(this);
		fViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		
		setTitleToolTip(getTitleToolTipText(PREFIX));
		WorkbenchHelp.setHelp(
			parent,
			new ViewContextComputer(this, IDebugHelpContextIds.BREAKPOINT_VIEW));
	}

	/**
	 * @see IWorkbenchPart
	 */
	public void dispose() {
		super.dispose();
		if (fViewer != null) {
			fViewer.removeDoubleClickListener(this);
		}
		cleanupActions();
	}

	/**
	 * Initializes the actions of this view
	 */
	protected void initializeActions() {
		fBreakpointListenerActions = new Vector(2);		
		fRemoveBreakpointAction= new RemoveBreakpointAction(fViewer);
		
		fRemoveAllBreakpointsAction= new RemoveAllBreakpointsAction();
		boolean enable= DebugPlugin.getDefault().getBreakpointManager().getBreakpoints().length == 0 ? false : true;
		fRemoveAllBreakpointsAction.setEnabled(enable);
		
		fShowQualifiedNamesAction = new ShowQualifiedAction(fViewer);
		fShowQualifiedNamesAction.setChecked(true);
		
		fOpenMarkerAction= new OpenBreakpointMarkerAction(fViewer);

		fEnableDisableBreakpointAction= new EnableDisableBreakpointAction(fViewer);
		addBreakpointListenerAction(fEnableDisableBreakpointAction);
		addBreakpointListenerAction(fRemoveAllBreakpointsAction);
	}

	/**
	 * Cleans up the actions when this part is disposed
	 */
	protected void cleanupActions() {
		for (int i=0; i < fBreakpointListenerActions.size(); i++) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener((IBreakpointListener)fBreakpointListenerActions.get(i));
		} 
	}

	/**
	 * Opens a marker for the current selection.
	 * This will only occur for selections containing a single existing breakpoint.
	 */
	public void openMarkerForCurrentSelection() {
		IStructuredSelection selection= (IStructuredSelection) fViewer.getSelection();
		if (selection.size() != 1) {
			//Single selection only
			return;
		}
		//Get the selected marker
		IMarker breakpoint= (IMarker) selection.getFirstElement();
		if (!breakpoint.exists()) {
			return;
		}
		IWorkbenchWindow dwindow= getSite().getWorkbenchWindow();
		IWorkbenchPage page= dwindow.getActivePage();
		if (page == null) {
			return;
		}
		
		openEditorForBreakpoint(breakpoint, page);
	}
	
	/**
	 * Open an editor for the breakpoint.  
	 */	
	protected void openEditorForBreakpoint(IMarker marker, IWorkbenchPage page) {
		IBreakpoint breakpoint= getBreakpointManager().getBreakpoint(marker);
		String id= breakpoint.getModelIdentifier();
		IDebugModelPresentation presentation= getPresentation(id);
		IEditorInput input= presentation.getEditorInput(marker);
		String editorId= presentation.getEditorId(input, marker);
		if (input != null) {
			try {
				IEditorPart editor;
				editor= page.openEditor(input, editorId);
				editor.gotoMarker(marker);
			} catch (PartInitException e) {
				DebugUIUtils.logError(e);
			}
		}		
	}

	/**
	 * Adds items to the context menu
	 */
	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator(IDebugUIConstants.EMPTY_NAVIGATION_GROUP));
		menu.add(new Separator(IDebugUIConstants.NAVIGATION_GROUP));
		menu.add(fOpenMarkerAction);
		menu.add(new Separator(IDebugUIConstants.EMPTY_BREAKPOINT_GROUP));
		menu.add(new Separator(IDebugUIConstants.BREAKPOINT_GROUP));
		menu.add(fEnableDisableBreakpointAction);
		menu.add(fRemoveBreakpointAction);
		menu.add(fRemoveAllBreakpointsAction);
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(fShowQualifiedNamesAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * Add an action to the contributed actions collection
	 */
	public void addBreakpointListenerAction(IBreakpointListener action) {
		fBreakpointListenerActions.add(action);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(action);		
	}	
	
	/**
	 * @see IDoubleClickListener
	 */
	public void doubleClick(DoubleClickEvent event) {
		openMarkerForCurrentSelection();
	}

	/**
	 * Returns whether an editor is open on this breakpoint's
	 * resource
	 */
	protected IEditorPart getOpenEditor(IMarker marker, IWorkbenchPage page) {
		//attempt to find the editor for the input
		IBreakpoint breakpoint= getBreakpointManager().getBreakpoint(marker);
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
	
	/**
	 * Configures the toolBar
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(fRemoveBreakpointAction);
		tbm.add(fRemoveAllBreakpointsAction);
		tbm.add(fOpenMarkerAction);
		tbm.add(fShowQualifiedNamesAction);
	}
	
	/**
	 * Convience method to retrieve the breakpoint manager
	 */
	protected IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}
	
	/**
	 * Handles key events in viewer.  Specifically interested in
	 * the Delete key.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0 
			&& fRemoveBreakpointAction.isEnabled()) {
				fRemoveBreakpointAction.run();
		}
	}
}
