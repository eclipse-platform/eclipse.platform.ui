/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.parts.*;

/**
 * 
 */
public class DuplicateConflictsDialog extends MessageDialog {

	private TreeViewer treeViewer;
	private ArrayList conflicts;

	class ConflictContentProvider
		extends DefaultContentProvider
		implements ITreeContentProvider, IStructuredContentProvider {
		public Object[] getElements(Object input) {
			return getChildren(input);
		}
		public Object getParent(Object child) {
			return null;
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof ArrayList)
				return true;
			return false;
		}
		public Object[] getChildren(Object parent) {
			if (parent instanceof ArrayList)
				return ((ArrayList) parent).toArray();
			return new Object[0];
		}
	}

	class ConflictLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof ArrayList) {
				ArrayList list = (ArrayList) obj;
				for (int i = 0; i < list.size(); i++) {
					DuplicateConflictsValidator.IdEntry entry =
						(DuplicateConflictsValidator.IdEntry) (list).get(i);
					if (entry.isInstallCandidate())
						return entry.getFeature().getLabel();
				}
			}
			return super.getText(obj);
		}
		public Image getImage(Object obj) {
			int flags = 0;
			if (obj instanceof ArrayList)
				flags = UpdateLabelProvider.F_WARNING;
			if (obj instanceof DuplicateConflictsValidator.IdEntry
				|| obj instanceof ArrayList)
				return UpdateUI.getDefault().getLabelProvider().get(
					UpdateUIImages.DESC_FEATURE_OBJ,
					flags);
			return null;
		}
	}

	public DuplicateConflictsDialog(Shell shell, ArrayList conflicts) {
		super(
			shell,
			UpdateUIMessages.DuplicateConflictsDialog_title, 
			null,
			UpdateUIMessages.DuplicateConflictsDialog_message, 
			WARNING,
			new String[] {
				IDialogConstants.YES_LABEL,
				IDialogConstants.NO_LABEL },
			0);
		this.conflicts = conflicts;
		UpdateUI.getDefault().getLabelProvider().connect(this);
	}

	public boolean close() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		return super.close();
	}

	protected Control createCustomArea(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		client.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		client.setLayout(layout);

		Label label = new Label(client, SWT.NULL);
		label.setText(UpdateUIMessages.DuplicateConflictsDialog_treeLabel); 

		treeViewer = new TreeViewer(client, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		gd.widthHint = 300;
		treeViewer.getTree().setLayoutData(gd);
		treeViewer.setContentProvider(new ConflictContentProvider());
		treeViewer.setLabelProvider(new ConflictLabelProvider());
		treeViewer.setAutoExpandLevel(10);
		treeViewer.setSorter(new ViewerSorter() {
		});
		treeViewer.setInput(conflicts);
		return client;
	}

}
