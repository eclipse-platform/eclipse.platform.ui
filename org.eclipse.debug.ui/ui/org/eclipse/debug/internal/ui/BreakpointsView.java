package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * This view shows the breakpoints registered with the breakpoint manager
 */
public class BreakpointsView extends AbstractDebugView implements ISelectionChangedListener, IDoubleClickListener {
	
	protected final static String PREFIX= "breakpoints_view.";
	
	/**
	 * The various actions of the context menu of this view
	 */
	private OpenMarkerAction fOpenMarkerAction;
	private RemoveBreakpointAction fRemoveBreakpointAction;
	private RemoveAllBreakpointsAction fRemoveAllBreakpointsAction;
	private EnableDisableBreakpointAction fEnableDisableBreakpointAction;
	private ShowQualifiedAction fShowQualifiedNamesAction;
	
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
		fViewer.addSelectionChangedListener(this);
		fViewer.addDoubleClickListener(this);
		fViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		
		setTitleToolTip(getTitleToolTipText(PREFIX));
	}

	/**
	 * @see IWorkbenchPart
	 */
	public void dispose() {
		super.dispose();
		if (fViewer != null) {
			fViewer.removeSelectionChangedListener(this);
			fViewer.removeDoubleClickListener(this);
		}
		cleanupActions();
	}

	/**
	 * Initializes the actions of this view
	 */
	protected void initializeActions() {
		fRemoveBreakpointAction= new RemoveBreakpointAction(fViewer);
		fRemoveBreakpointAction.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE));
		fRemoveBreakpointAction.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE));
		fRemoveBreakpointAction.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE));
		
		fRemoveAllBreakpointsAction= new RemoveAllBreakpointsAction();
		fRemoveAllBreakpointsAction.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE_ALL));
		fRemoveAllBreakpointsAction.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_ALL));
		fRemoveAllBreakpointsAction.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_ALL));
		boolean enable= DebugPlugin.getDefault().getBreakpointManager().getBreakpoints().length == 0 ? false : true;
		fRemoveAllBreakpointsAction.setEnabled(enable);
		
		fShowQualifiedNamesAction = new ShowQualifiedAction(fViewer);
		fShowQualifiedNamesAction.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_QUALIFIED_NAMES));
		fShowQualifiedNamesAction.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_QUALIFIED_NAMES));
		fShowQualifiedNamesAction.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_QUALIFIED_NAMES));
		fShowQualifiedNamesAction.setChecked(true);
		
		fOpenMarkerAction= new OpenBreakpointMarkerAction(fViewer);
		ISharedImages images= DebugUIPlugin.getDefault().getWorkbench().getSharedImages();
		fOpenMarkerAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_OPEN_MARKER));

		fEnableDisableBreakpointAction= new EnableDisableBreakpointAction(fViewer);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(fEnableDisableBreakpointAction);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(fRemoveAllBreakpointsAction);	
	}

	/**
	 * Cleans up the actions when this part is disposed
	 */
	protected void cleanupActions() {
		if (fEnableDisableBreakpointAction != null) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(fEnableDisableBreakpointAction);
		} 
		if (fRemoveAllBreakpointsAction != null) {		
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(fRemoveAllBreakpointsAction);
		}
	}

	/**
	 * Opens a marker for the current selection.
	 * An editor will be opened if <code>open</code> is <code>true</code>
	 * This will only occur for selections containing a single breakpoint.
	 */
	public void openMarkerForCurrentSelection(boolean open) {
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
		IEditorPart editor= getOpenEditor(breakpoint, page);
		if (editor != null) {
			editor.gotoMarker(breakpoint);
			return;
		}
		if (!open) {
			return;
		}
		
		openEditorForBreakpoint(breakpoint, page);
	}
	
	/**
	 * Open an editor for the breakpoint.  
	 */	
	protected void openEditorForBreakpoint(IMarker breakpoint, IWorkbenchPage page) {
		String id= getBreakpointManager().getModelIdentifier(breakpoint);
		IDebugModelPresentation presentation= getPresentation(id);
		IEditorInput input= presentation.getEditorInput(breakpoint);
		String editorId= presentation.getEditorId(input, breakpoint);
		if (input != null) {
			try {
				IEditorPart editor;
				editor= page.openEditor(input, editorId);
				editor.gotoMarker(breakpoint);
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
	 * @see IDoubleClickListener
	 */
	public void doubleClick(DoubleClickEvent event) {
		openMarkerForCurrentSelection(true);
	}

	/**
	 * @see ISelectionChangedListener
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		if (event.getSelection().isEmpty()) {
			return;
		}
		//FIXME: See PR 1G4CLUB
		openMarkerForCurrentSelection(false);
	}

	/**
	 * Returns whether an editor is open on this breakpoint's
	 * resource
	 */
	protected IEditorPart getOpenEditor(IMarker breakpoint, IWorkbenchPage page) {
		//attempt to find the editor for the input
		String id= getBreakpointManager().getModelIdentifier(breakpoint);
		IDebugModelPresentation presentation= getPresentation(id);
		IEditorInput editorElement = presentation.getEditorInput(breakpoint);
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
