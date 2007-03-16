/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.forms.examples.internal.rcp;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class HeadlessPage extends FormPage {
	private int count;

	public HeadlessPage(FormEditor editor, int count) {
		super(editor, "page"+count, "Page "+count);
		this.count = count;
	}
	
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = managedForm.getForm().getBody();
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		body.setLayout(layout);
		Label label = toolkit.createLabel(body, "The content of the headless page #"+count);
		GridData gd = new GridData();
		gd.horizontalSpan = 4;
		label.setLayoutData(gd);
		for (int i=0; i<80; i++) {
			toolkit.createLabel(body, "Field "+i);
			Text text = toolkit.createText(body, null);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			text.setLayoutData(gd);
		}
	}
}
