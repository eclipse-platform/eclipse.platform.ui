/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.model.BookmarkFolder;
import org.eclipse.update.internal.ui.model.NamedModelObject;
import org.eclipse.update.internal.ui.model.UpdateModel;
import org.eclipse.update.internal.ui.search.SearchObject;

import java.util.*;

public class UpdatesDragAdapter extends DragSourceAdapter {
	ISelectionProvider selectionProvider;
	Object dragData;

	/**
	 * NavigatorDragAction constructor comment.
	 */
	public UpdatesDragAdapter(ISelectionProvider provider) {
		selectionProvider = provider;
	}

	/**
	 * Returns the data to be transferred in a drag and drop
	 * operation.
	 */
	public void dragSetData(DragSourceEvent event) {
		if (event.doit == false)
			return;
		if (UpdateModelDataTransfer
			.getInstance()
			.isSupportedType(event.dataType)) {
			event.data = getSelectedModelObjects();
			dragData = event.data;
			return;
		}
		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = createTextualRepresentation((IStructuredSelection) selectionProvider.getSelection());
			dragData = null;
			return;
		}
	}
	
	static String createTextualRepresentation(IStructuredSelection sel) {
		StringBuffer buf = new StringBuffer();
		for (Iterator iter=sel.iterator(); iter.hasNext();) {
			String name = iter.next().toString();
			buf.append(name);
			buf.append(" ");
		}
		return buf.toString();
	}
	/**
	 * All selection must be named model objects.
	 */
	public void dragStart(DragSourceEvent event) {

		// Workaround for 1GEUS9V
		DragSource dragSource = (DragSource) event.widget;
		Control control = dragSource.getControl();
		if (control != control.getDisplay().getFocusControl()) {
			event.doit = false;
			return;
		}

		event.doit = canDrag();
	}

	public void dragFinished(DragSourceEvent event) {
		if (event.doit == false || dragData == null)
			return;
		if (event.detail == DND.DROP_MOVE) {
			NamedModelObject[] objects = (NamedModelObject[]) dragData;
			UpdateModel model = UpdateUI.getDefault().getUpdateModel();
			for (int i = 0; i < objects.length; i++) {
				NamedModelObject obj = objects[i];
				Object parent = obj.getParent(obj);
				if (parent instanceof DiscoveryFolder)
					continue;
				if (parent == null) {
					model.removeBookmark(obj);
				} else {
					((BookmarkFolder) parent).removeChildren(
						new NamedModelObject[] { obj });
				}
			}
		}
		dragData = null;
	}
	
	private boolean canDrag() {
		return canCopy((IStructuredSelection)selectionProvider.getSelection());
	}
	
	static boolean canCopy(IStructuredSelection selection) {
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (!(obj instanceof NamedModelObject))
				return false;
			if (obj instanceof SearchObject) {
				SearchObject sobj = (SearchObject) obj;
				if (sobj.isCategoryFixed()
					|| sobj.isPersistent() == false)
					return false;
			}
			if (obj instanceof DiscoveryFolder) return false;
		}
		return true;
	}
	
	private NamedModelObject[] getSelectedModelObjects() {
		return createObjectRepresentation((IStructuredSelection)selectionProvider.getSelection());
	}

	static NamedModelObject[] createObjectRepresentation(IStructuredSelection selection) {
		ArrayList objects = new ArrayList();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof NamedModelObject)
				objects.add(obj);
			else
				return new NamedModelObject[0];
		}
		return (NamedModelObject[]) objects.toArray(
			new NamedModelObject[objects.size()]);
	}
}