package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.resources.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.model.*;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.views.contentoutline.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * Content outline page for the readme editor.
 */
public class ReadmeContentOutlinePage extends ContentOutlinePage {
	protected IFile input;
	
	class OutlineAction extends Action {
		private Shell shell;
		public OutlineAction(String label) {
			super(label);
			getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					setEnabled(!event.getSelection().isEmpty());
				}
			});
		}
		public void setShell(Shell shell) {
			this.shell = shell;
		}
		public void run() {
			MessageDialog.openInformation(shell,
				MessageUtil.getString("Readme_Outline"),  //$NON-NLS-1$
				MessageUtil.getString("ReadmeOutlineActionExecuted")); //$NON-NLS-1$
		}
	}
	
/**
 * Creates a new ReadmeContentOutlinePage.
 */
public ReadmeContentOutlinePage(IFile input) {
	super();
	this.input = input;
}
/**  
 * Creates the control and registers the popup menu for this page
 * Menu id "org.eclipse.ui.examples.readmetool.outline"
 */
public void createControl(Composite parent) {
	super.createControl(parent);

	WorkbenchHelp.setHelp(getControl(), IReadmeConstants.CONTENT_OUTLINE_PAGE_CONTEXT);

	TreeViewer viewer = getTreeViewer();
	viewer.setContentProvider(new WorkbenchContentProvider());
	viewer.setLabelProvider(new WorkbenchLabelProvider());
	viewer.setInput(getContentOutline(input));
	initDragAndDrop();
	
	// Configure the context menu.
	MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
	menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));	
	menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));	 //$NON-NLS-1$

	Menu menu = menuMgr.createContextMenu(viewer.getTree());
	viewer.getTree().setMenu(menu);
	// Be sure to register it so that other plug-ins can add actions.
	getSite().registerContextMenu("org.eclipse.ui.examples.readmetool.outline", menuMgr, viewer);  //$NON-NLS-1$

	getSite().getActionBars().setGlobalActionHandler(
		IReadmeConstants.RETARGET2, 
		new OutlineAction(MessageUtil.getString("Outline_Action2")));  //$NON-NLS-1$

	OutlineAction action = new OutlineAction(MessageUtil.getString("Outline_Action3")); //$NON-NLS-1$
	action.setToolTipText(MessageUtil.getString("Readme_Outline_Action3")); //$NON-NLS-1$
	getSite().getActionBars().setGlobalActionHandler(
		IReadmeConstants.LABELRETARGET3, 
		action);  
	action = new OutlineAction(MessageUtil.getString("Outline_Action4")); //$NON-NLS-1$
	getSite().getActionBars().setGlobalActionHandler(
		IReadmeConstants.ACTION_SET_RETARGET4, 
		action);  
	action = new OutlineAction(MessageUtil.getString("Outline_Action5")); //$NON-NLS-1$
	action.setToolTipText(MessageUtil.getString("Readme_Outline_Action5")); //$NON-NLS-1$
	getSite().getActionBars().setGlobalActionHandler(
		IReadmeConstants.ACTION_SET_LABELRETARGET5, 
		action);  
}
/**
 * Gets the content outline for a given input element.
 * Returns the outline (a list of MarkElements), or null
 * if the outline could not be generated.
 */
private IAdaptable getContentOutline(IAdaptable input) {
	return ReadmeModelFactory.getInstance().getContentOutline(input);
}
/**
 * Initializes drag and drop for this content outline page.
 */
private void initDragAndDrop() {
	int ops = DND.DROP_COPY | DND.DROP_MOVE;
	Transfer[] transfers = new Transfer[] {TextTransfer.getInstance(), PluginTransfer.getInstance()};
	getTreeViewer().addDragSupport(ops, transfers, new ReadmeContentOutlineDragListener(this));
}
/**
 * Forces the page to update its contents.
 *
 * @see ReadmeEditor#doSave(IProgressMonitor)
 */
public void update() {
	getControl().setRedraw(false);
	getTreeViewer().setInput(getContentOutline(input));
	getTreeViewer().expandAll();
	getControl().setRedraw(true);
}
}
