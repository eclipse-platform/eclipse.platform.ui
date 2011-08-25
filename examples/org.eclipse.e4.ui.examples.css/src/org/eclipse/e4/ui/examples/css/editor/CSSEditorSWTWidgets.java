/*******************************************************************************
s * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.examples.css.editor;

import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
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
import org.eclipse.swt.widgets.ToolBar;

public class CSSEditorSWTWidgets extends AbstractCSSSWTEditor {

	public void createContent(Composite parent) {

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 3;
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(layout);

		//Create SWT ToolBar
		ToolBar toolbar = new ToolBar(composite, SWT.HORIZONTAL);
		toolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
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
		{
			CTabFolder tabFolder = new CTabFolder(composite, SWT.CLOSE);
			tabFolder.setUnselectedCloseVisible(true);
			tabFolder.setUnselectedImageVisible(true);
					
			CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NONE);
			tabItem1.setText("Busy");
			WidgetElement.setCSSClass(tabItem1, "busy");
			
			CTabItem tabItem2 = new CTabItem(tabFolder, SWT.NONE);
			tabItem2.setText("Modified");
			WidgetElement.setCSSClass(tabItem2, "modified");
			
			tabFolder.setSelection(0);
		}
		// Create ETabFolder
		{
			CTabFolder etabFolder = new CTabFolder(composite, SWT.CLOSE);
			etabFolder.setUnselectedCloseVisible(true);
			etabFolder.setUnselectedImageVisible(true);
					
			CTabItem etabItem1 = new CTabItem(etabFolder, SWT.NONE);
			etabItem1.setText("Busy");
			WidgetElement.setCSSClass(etabItem1, "busy");
			
			CTabItem etabItem2 = new CTabItem(etabFolder, SWT.NONE);
			etabItem2.setText("Modified");
			WidgetElement.setCSSClass(etabItem2, "modified");
			
			etabFolder.setSelection(0);
		}

	}

	public static void main(String[] args) {
		CSSEditorSWTWidgets editor = new CSSEditorSWTWidgets();
		editor.display();
	}
}
