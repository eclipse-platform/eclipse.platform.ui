/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.akrogen.tkui.css.swt.examples.csseditors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

public class CSSEditorSWTWidgets extends AbstractCSSSWTEditor {

	public void createContent(Composite parent) {

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 3;
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(layout);

		// Create SWT Text
		Text text = new Text(composite, SWT.BORDER);
		text.setText("bla bla bla...");
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create SWT Text [SWT.MULTI]
		Text textArea = new Text(composite, SWT.MULTI | SWT.BORDER);
		textArea.setText("bla bla bla...[SWT.MULTI]");
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 100;
		textArea.setLayoutData(gridData);

		// Create SWT Label
		Label label = new Label(composite, SWT.NONE);
		label.setText("bla bla bla...");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create Button
		Button button = new Button(composite, SWT.BORDER);
		button.setText("SWT Button");

		// Create Button [SWT.CHECK]
		Button checkbox = new Button(composite, SWT.CHECK);
		checkbox.setText("SWT Button [SWT.CHECK]");

		// Create Button [SWT.RADIO]
		Button radio = new Button(composite, SWT.RADIO);
		radio.setText("SWT Button [SWT.RADIO]");

		// Create Combo
		Combo combo = new Combo(composite, SWT.BORDER);
		combo.add("Item 1");
		combo.add("Item 2");
		combo.select(0);

		// Create CTabFolder
		CTabFolder tabFolder = new CTabFolder(composite, SWT.CLOSE);
		tabFolder.setUnselectedCloseVisible(true);
		tabFolder.setUnselectedImageVisible(true);
				
		CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NONE);
		tabItem1.setText("Tab 1");
		
		CTabItem tabItem2 = new CTabItem(tabFolder, SWT.NONE);
		tabItem2.setText("Tab 2");
		
		tabFolder.setSelection(0);
	}

	public static void main(String[] args) {
		CSSEditorSWTWidgets editor = new CSSEditorSWTWidgets();
		editor.display();
	}
}
