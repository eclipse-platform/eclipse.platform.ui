package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.RenameResourceAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The ResourceNavigatorRenameAction is the rename action used by the
 * ResourceNavigator that also allows updating after rename.
 * @since 2.0
 */
public class ResourceNavigatorRenameAction extends RenameResourceAction {
	private TreeViewer viewer;
	private boolean isActivating = false;
	private boolean wasActivated = false;
	private Item item;
	/**
	 * Create a ResourceNavigatorRenameAction and use the tree of the supplied viewer
	 * for editing.
	 * @param shell Shell
	 * @param treeViewer TreeViewer
	 */
	public ResourceNavigatorRenameAction(Shell shell, TreeViewer treeViewer) {
		super(shell, treeViewer.getTree());
		WorkbenchHelp.setHelp(
			this,
			INavigatorHelpContextIds.RESOURCE_NAVIGATOR_RENAME_ACTION);
		this.viewer = treeViewer;
		
		treeViewer.getControl().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				handleMouseDown(e);
			}
			public void mouseDoubleClick(MouseEvent e) {
				handleMouseDoubleClick(e);
			}
		});
		
	}
	/* (non-Javadoc)
	 * Run the action to completion using the supplied path.
	 */
	protected void runWithNewPath(IPath path, IResource resource) {
		IWorkspaceRoot root = resource.getProject().getWorkspace().getRoot();
		super.runWithNewPath(path, resource);
		if (this.viewer != null) {
			IResource newResource = root.findMember(path);
			if (newResource != null)
				this.viewer.setSelection(new StructuredSelection(newResource), true);
		}
	}
	/**
	* Handle the key release
	*/
	public void handleKeyReleased(KeyEvent event) {
		if (event.keyCode == SWT.F2 && event.stateMask == 0 && isEnabled()) {
			run();
		}
	}
	/**
	 * Handles the double click event.
	 */
	public void handleMouseDoubleClick(MouseEvent event) {
		//The last mouse down was a double click. Cancel
		//the rename activation.
		isActivating = false;
	}
	/**
	 * Handles the mouse down event.
	 * Activates rename if it is not a double click.
	 *
	 * This implementation must:
	 *	i) activate rename when clicking over the item's text or over the item's image.
	 *	ii) activate it only if the item is already selected.
	 *	iii) do NOT activate it on a double click (whether the item is selected or not).
	 */
	public void handleMouseDown(MouseEvent event) {
		if (event.button != 1) {
			return;
		}
			
		Item[] items = viewer.getTree().getSelection();
		// Do not rename if more than one item is selected.
		if (items.length != 1) {
			item = null;
			return;
		}
	
		if(item != items[0]) {
			//This mouse down was a selection. Keep the selection and return;
			item = items[0];
			return;
		}
	
		//It may be a double click. If so, the activation was started by the first click.
		if(isActivating || wasActivated)
			return;
			
		isActivating = true;
		//Post the activation. So it may be canceled if it was a double click.
		postActivation(event);
	}
	/**
	 * Starts a thread to perform a rename unless double-click or
	 * selection of another item occurs.
	 */
	private void postActivation(final MouseEvent event) {
		if(!isActivating)
			return;
		
		(new Thread() {
			public void run() {
				try { Thread.sleep(400); } catch (Exception e){}
				if(isActivating && !wasActivated) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							wasActivated = true;
							ResourceNavigatorRenameAction.this.run();
							isActivating = false;
							wasActivated = false;
						}
					});
				}
			}
		}).start();
	}
}