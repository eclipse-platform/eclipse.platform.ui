/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.preferences;


import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

public class ProcessPropertyPage extends PropertyPage {

	/**
	 * Constructor for ProcessPropertyPage
	 */
	public ProcessPropertyPage() {
		super();
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite ancestor) {

		Font font = ancestor.getFont();
		noDefaultAndApplyButton();
		
		Composite parent= new Composite(ancestor, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		parent.setLayout(layout);
		
		Label l1= new Label(parent, SWT.NULL);
		l1.setText(DebugPreferencesMessages.ProcessPropertyPage_Command_Line__1); 
		
		GridData gd= new GridData();
		gd.verticalAlignment= GridData.BEGINNING;
		l1.setLayoutData(gd);
		l1.setFont(font);
		Text l2= new Text(parent, SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= convertWidthInCharsToPixels(80);
		gd.heightHint= convertHeightInCharsToPixels(15);
		l2.setLayoutData(gd);
		l2.setFont(font);
		initCommandLineLabel(l2);
		
		return parent;
	}
	
	private void initCommandLineLabel(Text l) {
		Object o= getElement();
		if (o instanceof IDebugElement)
			o= ((IDebugElement)o).getDebugTarget().getProcess();
		if (o instanceof IProcess) {
			IProcess process= (IProcess)o;
			String cmdLine= process.getAttribute(IProcess.ATTR_CMDLINE);
			if (cmdLine != null)
				l.setText(cmdLine);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
			getControl(),
			IDebugHelpContextIds.PROCESS_PROPERTY_PAGE);
	}

}
