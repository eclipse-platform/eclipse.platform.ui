/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory.renderings;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GoToAddressComposite {
	
	private Label fLabel;
	private Text fExpression;
	private Button fOKButton;
	private Button fCancelButton;
	private Composite fComposite;
	private Button fOffsetButton;
	
	/**
	 * @param parent
	 * @return
	 */
	public Control createControl(Composite parent)
	{
		fComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		layout.makeColumnsEqualWidth = false;
		layout.marginHeight = 0;
		layout.marginLeft = 0;
		fComposite.setLayout(layout);
		
		fLabel = new Label(fComposite, SWT.NONE);
		fLabel.setText(DebugUIMessages.GoToAddressComposite_0);
		
		fExpression = new Text(fComposite, SWT.SINGLE | SWT.BORDER);
		fExpression.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fOffsetButton = new Button(fComposite, SWT.CHECK);
		fOffsetButton.setText(DebugUIMessages.GoToAddressComposite_3);
		fOffsetButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (fOffsetButton.getSelection())
					fLabel.setText(DebugUIMessages.GoToAddressComposite_4);
				else
					fLabel.setText(DebugUIMessages.GoToAddressComposite_0);
				fComposite.layout();
			}});
		
		
		fOKButton = new Button(fComposite, SWT.NONE);
		fOKButton.setText(DebugUIMessages.GoToAddressComposite_1);
		
		fCancelButton = new Button(fComposite, SWT.NONE);
		fCancelButton.setText(DebugUIMessages.GoToAddressComposite_2);
		
		return fComposite;
	}
	
	public int getHeight()
	{
		int height = fComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		return height;
	}
	
	public Button getButton(int id)
	{
		if (id == IDialogConstants.OK_ID)
			return fOKButton;
		else if (id == IDialogConstants.CANCEL_ID)
			return fCancelButton;
		return null;
	}
	
	public String getExpressionText()
	{
		return fExpression.getText();
	}
	
	public Text getExpressionWidget()
	{
		return fExpression;
	}
	
	public boolean isOffset()
	{
		return fOffsetButton.getSelection();
	}
	

}
