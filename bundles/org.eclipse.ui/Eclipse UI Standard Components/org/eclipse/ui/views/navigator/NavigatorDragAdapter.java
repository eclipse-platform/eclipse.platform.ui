package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.ReadOnlyStateChecker;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Implements drag behaviour when items are dragged out of the
 * resource navigator.
 */
/* package */ class NavigatorDragAdapter extends DragSourceAdapter {
	ISelectionProvider selectionProvider;

private static final String CHECK_MOVE_TITLE = WorkbenchMessages.getString("MoveResourceAction.title"); //$NON-NLS-1$
private static final String CHECK_MOVE_MESSAGE = WorkbenchMessages.getString("MoveResourceAction.checkMoveMessage"); //$NON-NLS-1$

/**
 * NavigatorDragAction constructor comment.
 */
public NavigatorDragAdapter(ISelectionProvider provider) {
	selectionProvider = provider;
}
/**
 * Invoked when an action occurs. 
 * Argument context is the Window which contains the UI from which this action was fired.
 * This default implementation prints the name of this class and its label.
 * @see IAction#run
 */
public void dragFinished(DragSourceEvent event) {
	if (event.doit && event.detail == DND.DROP_MOVE) {
		//delete the old elements
		final int typeMask = IResource.FOLDER | IResource.FILE;
		
		DragSource dragSource = (DragSource)event.widget;
		Control control = dragSource.getControl();
		Shell shell = control.getShell();
		
		IResource[] resources = getSelectedResources(typeMask, shell);
		if (resources == null)
			return;
		for (int i = 0; i < resources.length; i++) {
			try {
				resources[i].delete(IResource.KEEP_HISTORY | IResource.FORCE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
}
/**
 * Returns the data to be transferred in a drag and drop
 * operation.
 */
public void dragSetData(DragSourceEvent event) {
	final int typeMask = IResource.FILE | IResource.FOLDER;
	
	DragSource dragSource = (DragSource)event.widget;
	Control control = dragSource.getControl();
	Shell shell = control.getShell();
	
	IResource[] resources = getSelectedResources(typeMask, shell);
	if (resources == null || resources.length == 0)
		return;

	//use resource transfer if possible
	if (ResourceTransfer.getInstance().isSupportedType(event.dataType)) {
		event.data = resources;
		return;
	}
	
	//resort to a file transfer
	if (!FileTransfer.getInstance().isSupportedType(event.dataType))
		return;

	// Get the path of each file and set as the drag data
	final int len = resources.length;
	String[] fileNames = new String[len];
	for (int i = 0, length = len; i < length; i++) {
		fileNames[i] = resources[i].getLocation().toOSString();
	}
	event.data = fileNames;
}
/**
 * All selection must be files or folders.
 */
public void dragStart(DragSourceEvent event) {

	// Workaround for 1GEUS9V
	DragSource dragSource = (DragSource)event.widget;
	Control control = dragSource.getControl();
	if (control != control.getDisplay().getFocusControl()){
		event.doit = false;
		return;
	}
	
	IStructuredSelection selection = (IStructuredSelection)selectionProvider.getSelection();
	for (Iterator i = selection.iterator(); i.hasNext();) {
		Object next = i.next();
		if (!(next instanceof IFile || next instanceof IFolder)) {
			event.doit = false;
			return;
		}
	}
	event.doit = true;
}
protected IResource[] getSelectedResources(int resourceTypes, Shell shell) {
	List resources = new ArrayList();
	IResource[] result = new IResource[0];

	ISelection selection = selectionProvider.getSelection();
	if (!(selection instanceof IStructuredSelection) || selection.isEmpty()) {
		return null;
	}
	IStructuredSelection structuredSelection = (IStructuredSelection)selection;
	if (structuredSelection == null)
		return null;

	// loop through list and look for matching items
	Iterator enum = structuredSelection.iterator();
	while (enum.hasNext()) {
		Object obj = enum.next();
		if (obj instanceof IResource) {
			IResource res = (IResource) obj;
			if ((res.getType() & resourceTypes) == res.getType()) {
				resources.add(res);
			}
		}
	}
	result = new IResource[resources.size()];
	resources.toArray(result);
	ReadOnlyStateChecker checker = new ReadOnlyStateChecker(shell,CHECK_MOVE_TITLE,CHECK_MOVE_MESSAGE); 

	return checker.checkReadOnlyResources(result);
}
}
