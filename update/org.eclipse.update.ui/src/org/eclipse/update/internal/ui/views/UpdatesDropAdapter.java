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
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.*;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class UpdatesDropAdapter extends ViewerDropAdapter {
	private static final String KEY_CONFLICT =
		"UpdatesDropAdapter.nameConflict";
	private TransferData currentTransfer;

	public UpdatesDropAdapter(Viewer viewer) {
		super(viewer);
	}

	/**
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	public boolean performDrop(Object data) {
		if (data instanceof Object[]) {
			Object[] objects = (Object[]) data;
			for (int i = 0; i < objects.length; i++) {
				Object obj = objects[i];
				if (!dropObject(obj))
					return false;
			}
			saveModel();
			return true;
		}
		return false;
	}

	private boolean dropObject(Object object) {
		if (object instanceof NamedModelObject)
			return dropNamedModelObject((NamedModelObject) object);
		return false;
	}

	private boolean dropNamedModelObject(NamedModelObject object) {
		return addToModel(object);
	}

	private void saveModel() {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		model.saveBookmarks();
	}

	private boolean addToModel(NamedModelObject object) {
		BookmarkFolder parentFolder =
			(BookmarkFolder) getRealTarget(getCurrentTarget());
		return addToModel(
			getViewer().getControl().getShell(),
			parentFolder,
			object);
	}

	static boolean addToModel(
		Shell shell,
		BookmarkFolder parentFolder,
		NamedModelObject object) {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();

		Object[] siblings =
			parentFolder != null
				? parentFolder.getChildren(parentFolder)
				: model.getBookmarks();

		boolean mustRename = false;

		for (int i = 0; i < siblings.length; i++) {
			NamedModelObject sibling = (NamedModelObject) siblings[i];
			if (sibling.getName().equals(object.getName())) {
				mustRename = true;
				break;
			}
		}
		if (mustRename) {
			RenameDialog dialog = new RenameDialog(shell, object, siblings);
			dialog.create();
			dialog.getShell().setSize(350, 150);
			dialog.getShell().setText(
				UpdateUI.getString(KEY_CONFLICT));
			if (dialog.open() != RenameDialog.OK)
				return false;
		}
		if (parentFolder != null)
			parentFolder.addChild(object);
		else {
			model.addBookmark(object);
		}
		return true;
	}

	static Object getRealTarget(Object target) {
		if (target instanceof NamedModelObject
			&& !(target instanceof BookmarkFolder)) {
			NamedModelObject sibling = (NamedModelObject) target;
			return sibling.getParent(sibling);
		}
		return target;
	}

	/**
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	public boolean validateDrop(
		Object target,
		int operation,
		TransferData transferType) {
		currentTransfer = transferType;
		if (currentTransfer != null
			&& UpdateModelDataTransfer.getInstance().isSupportedType(
				currentTransfer)) {
			return validateTarget();
		}
		return false;
	}

	private boolean validateTarget() {
		Object target = getCurrentTarget();
		if (target == null || target instanceof NamedModelObject)
			return true;
		else
			return false;
	}

}
