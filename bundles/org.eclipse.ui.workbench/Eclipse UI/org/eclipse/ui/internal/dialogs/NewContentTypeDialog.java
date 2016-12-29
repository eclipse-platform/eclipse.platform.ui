/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A dialog that asks for initial values to create a new content-type.
 *
 * @since 3.109
 */
public class NewContentTypeDialog extends TitleAreaDialog {

	private String name;
	private IContentTypeManager manager;
	private ControlDecoration decorator;

	/**
	 * @param parentShell
	 */
	protected NewContentTypeDialog(Shell parentShell, IContentTypeManager manager, IContentType parent) {
		super(parentShell);
		this.manager = manager;
		String baseName = name = WorkbenchMessages.ContentTypes_newContentTypeDialog_defaultNameNoParent;
		if (parent != null) {
			baseName = name = NLS.bind(WorkbenchMessages.ContentTypes_newContentTypeDialog_defaultNameWithParent,
					parent.getName());
		}
		int suffix = 2;
		while (manager.getContentType(name) != null) {
			name = baseName + " (" + suffix + ')'; //$NON-NLS-1$
			suffix++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.
	 * Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(WorkbenchMessages.ContentTypes_newContentTypeDialog_title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);
		Composite contents = new Composite(parentComposite, SWT.NONE);
		contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label nameLabel = new Label(contents, SWT.LEFT);
		nameLabel.setText(WorkbenchMessages.ContentTypes_newContentTypeDialog_nameLabel);
		Text nameText = new Text(contents, SWT.SINGLE | SWT.BORDER);
		nameText.setText(name);
		nameText.addModifyListener(event -> {
			name = nameText.getText();
			if (validateName()) {
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				decorator.hide();
			} else {
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				decorator.show();
			}
		});
		decorator = new ControlDecoration(nameText, SWT.TOP | SWT.LEFT);
		decorator.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage());
		decorator.setDescriptionText(WorkbenchMessages.ContentTypes_newContentTypeDialog_invalidContentTypeName);
		decorator.hide();
		setTitle(WorkbenchMessages.ContentTypes_newContentTypeDialog_title);
		setMessage(WorkbenchMessages.ContentTypes_newContentTypeDialog_descritption);

		Dialog.applyDialogFont(parentComposite);

		Point defaultMargins = LayoutConstants.getMargins();
		GridLayoutFactory.fillDefaults().numColumns(2).margins(defaultMargins.x, defaultMargins.y)
				.generateLayout(contents);

		return contents;
	}

	private boolean validateName() {
		return name.length() > 0 && manager.getContentType(name) == null;
	}

	public String getName() {
		return name;
	}
}
