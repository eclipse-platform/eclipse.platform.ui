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
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.DefaultContentProvider;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public abstract class BaseNewWizardPage extends WizardPage {
	private static final String KEY_NAME = "BaseNewWizardPage.name";
	private static final String KEY_CREATE_IN = "BaseNewWizardPage.createIn";
	private static final String KEY_EXISTING = "BaseNewWizardPage.existing";
	private static final String KEY_INVALID = "BaseNewWizardPage.invalid";
	private static final String KEY_MISSING_NAME = "BaseNewWizardPage.missingName";
	private BookmarkFolder folder;
	private String name;
	private TreeViewer tree;
	private Text nameText;
	private Text containerText;
	private boolean showError=false;

	class ContainerContentProvider
		extends DefaultContentProvider
		implements ITreeContentProvider {

		public Object[] getChildren(Object parent) {
			if (parent instanceof UpdateModel) {
				return ((UpdateModel) parent).getBookmarks();
			}
			if (parent instanceof BookmarkFolder) {
				return ((BookmarkFolder) parent).getChildren(parent);
			}
			return new Object[0];
		}

		public Object getParent(Object child) {
			if (child instanceof NamedModelObject)
				return ((NamedModelObject) child).getParent(null);
			return null;
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof BookmarkFolder) {
				return ((BookmarkFolder) parent).hasChildren();
			}
			return false;
		}

		public Object[] getElements(Object obj) {
			return getChildren(obj);
		}
	}

	class ContainerLabelProvider extends LabelProvider {
		public Image getImage(Object obj) {
			if (obj instanceof BookmarkFolder)
				return UpdateUI.getDefault().getLabelProvider().get(UpdateUIImages.DESC_BFOLDER_OBJ);
			return super.getImage(obj);
		}
	}

	public BaseNewWizardPage(BookmarkFolder folder) {
		super("");
		this.folder = folder;
		UpdateUI.getDefault().getLabelProvider().connect(this);
	}
	
	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		super.dispose();
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_NAME));
		nameText = new Text(container, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		nameText.setLayoutData(gd);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
		createClientControl(container, layout.numColumns);
		label = new Label(container, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_CREATE_IN));
		containerText = new Text(container, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		if (folder != null)
			containerText.setText(folder.getPath().toString());
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_EXISTING));
		gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		tree = new TreeViewer(container);
		gd = new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.widthHint = 250;
		gd.heightHint = 200;
		tree.getControl().setLayoutData(gd);
		tree.setContentProvider(new ContainerContentProvider());
		tree.setLabelProvider(new ContainerLabelProvider());
		tree.addFilter(new ViewerFilter() {
			public boolean select(Viewer v, Object parent, Object child) {
				return (child instanceof BookmarkFolder);
			}
		});
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				handleSelectionChanged((IStructuredSelection) e.getSelection());
			}
		});
		tree.setInput(UpdateUI.getDefault().getUpdateModel());
		if (folder != null)
			tree.setSelection(new StructuredSelection(folder), true);
		validatePage();
		setControl(container);
	}
	
	protected void setDelayedErrorMessage(String text) {
		if (showError)
			setErrorMessage(text);
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			nameText.setFocus();
			showError=true;
		}
	}

	protected abstract void createClientControl(Composite parent, int span);

	protected void validatePage() {
		String message = null;
		boolean complete = true;
		if (containerText.getText().length() > 0) {
			folder = getFolderFromPath(containerText.getText());
			if (folder == null) {
				message = UpdateUI.getString(KEY_INVALID);
			}
		} else
			folder = null;
		String name = nameText.getText().trim();
		if (name.length() == 0) {
			complete = false;
			message = UpdateUI.getString(KEY_MISSING_NAME);
		}
		setDelayedErrorMessage(message);
		setPageComplete(message == null && complete);
	}
	private BookmarkFolder getFolderFromPath(String path) {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		return model.getFolder(new Path(path));
	}
	private void handleSelectionChanged(IStructuredSelection selection) {
		folder = (BookmarkFolder) selection.getFirstElement();
		if (folder != null)
			containerText.setText(folder.getPath().toString());
		else
			containerText.setText("");
	}
	public BookmarkFolder getFolder() {
		return folder;
	}
	public String getName() {
		return nameText.getText().trim();
	}
	public abstract boolean finish();

	protected void addToModel(NamedModelObject object) {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		BookmarkFolder parentFolder = getFolder();
		if (parentFolder != null)
			parentFolder.addChild(object);
		else {
			model.addBookmark(object);
		}
		model.saveBookmarks();
	}
}