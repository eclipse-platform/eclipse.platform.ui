/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.layout.CellLayout;
import org.eclipse.ui.internal.layout.Row;
import org.eclipse.ui.part.EditorPart;

/**
 * @since 3.0
 */
public class TitleTestEditor extends EditorPart {

	Composite composite;

	Text title;

	Text name;

	Text contentDescription;

	Label titleLabel;

	Label nameLabel;

	Label cdLabel;

	public TitleTestEditor() {
		super();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {

		if (!(input instanceof IFileEditorInput)) {
			throw new PartInitException(
					"Invalid Input: Must be IFileEditorInput");
		}
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		CellLayout layout = new CellLayout(2).setColumn(0, Row.fixed())
				.setColumn(1, Row.growing());
		composite.setLayout(layout);

		Label firstLabel = new Label(composite, SWT.NONE);
		firstLabel.setText("Title");
		title = new Text(composite, SWT.BORDER);
		title.setText(getTitle());

		title.addModifyListener(e -> setTitle(title.getText()));

		Label secondLabel = new Label(composite, SWT.NONE);
		secondLabel.setText("Name");
		name = new Text(composite, SWT.BORDER);
		name.setText(getPartName());
		name.addModifyListener(e -> setPartName(name.getText()));

		Label thirdLabel = new Label(composite, SWT.NONE);
		thirdLabel.setText("Content");
		contentDescription = new Text(composite, SWT.BORDER);
		contentDescription.setText(getContentDescription());
		contentDescription.addModifyListener(e -> setContentDescription(contentDescription.getText()));

		Label tlLabel = new Label(composite, SWT.NONE);
		tlLabel.setText("getTitle() = ");
		titleLabel = new Label(composite, SWT.NONE);

		Label nmLabel = new Label(composite, SWT.NONE);
		nmLabel.setText("getPartName() = ");
		nameLabel = new Label(composite, SWT.NONE);

		Label descLabel = new Label(composite, SWT.NONE);
		descLabel.setText("getContentDescription() = ");
		cdLabel = new Label(composite, SWT.NONE);

		updateLabels();

		addPropertyListener((source, propId) -> updateLabels());
	}

	private void updateLabels() {
		titleLabel.setText(getTitle());
		nameLabel.setText(getPartName());
		cdLabel.setText(getContentDescription());
	}

	@Override
	public void setFocus() {
		composite.setFocus();

	}

}
