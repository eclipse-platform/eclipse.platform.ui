package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.viewers.*;

import org.eclipse.ui.actions.ReadOnlyStateChecker;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Implements drag behaviour when items are dragged out of the
 * resource navigator.
 * 
 * @since 2.0
 */
public class NavigatorDragAdapter extends DragSourceAdapter {
	ISelectionProvider selectionProvider;

	private static final String CHECK_MOVE_TITLE = ResourceNavigatorMessages.getString("DragAdapter.title"); //$NON-NLS-1$
	private static final String CHECK_MOVE_MESSAGE = ResourceNavigatorMessages.getString("DragAdapter.checkMoveMessage"); //$NON-NLS-1$

	/**
	 * Constructs a new drag adapter.
	 */
	public NavigatorDragAdapter(ISelectionProvider provider) {
		selectionProvider = provider;
	}
	/**
	 * @see DragSourceListener#dragFinished
	 */
	public void dragFinished(DragSourceEvent event) {
		if (event.doit && event.detail == DND.DROP_MOVE) {
			//delete the old elements
			final int typeMask = IResource.FOLDER | IResource.FILE;

			DragSource dragSource = (DragSource) event.widget;
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
	 * @see DragSourceListener#dragSetData
	 */
	public void dragSetData(DragSourceEvent event) {
		final int typeMask = IResource.FILE | IResource.FOLDER;

		DragSource dragSource = (DragSource) event.widget;
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
	 * @see DragSourceListener#dragStart
	 */
	public void dragStart(DragSourceEvent event) {

		// Workaround for 1GEUS9V
		DragSource dragSource = (DragSource) event.widget;
		Control control = dragSource.getControl();
		if (control != control.getDisplay().getFocusControl()) {
			event.doit = false;
			return;
		}

		IStructuredSelection selection =
			(IStructuredSelection) selectionProvider.getSelection();
		for (Iterator i = selection.iterator(); i.hasNext();) {
			Object next = i.next();
			if (!(next instanceof IFile || next instanceof IFolder)) {
				event.doit = false;
				return;
			}
		}
		event.doit = true;
	}
	
	private IResource[] getSelectedResources(int resourceTypes, Shell shell) {
		List resources = new ArrayList();
		IResource[] result = new IResource[0];

		ISelection selection = selectionProvider.getSelection();
		if (!(selection instanceof IStructuredSelection) || selection.isEmpty()) {
			return null;
		}
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
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
		ReadOnlyStateChecker checker =
			new ReadOnlyStateChecker(shell, CHECK_MOVE_TITLE, CHECK_MOVE_MESSAGE);

		return checker.checkReadOnlyResources(result);
	}
}