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
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.internal.ui.wizards.GlobalSynchronizeWizard;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.Page;

public class SynchronizeViewDefaultPage extends Page {

	private FormToolkit forms;
	private FormText text;
	private Composite composite;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setBackground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);
		
		forms = new FormToolkit(parent.getDisplay());
		forms.setBackground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		HyperlinkGroup group = forms.getHyperlinkGroup();
		group.setBackground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		
		text = forms.createFormText(composite, true);
		String introText = new String("<form><p>" +
				"The Synchronize View allows you to view and manipulate differences between sets of resources. To start using this view you first " +
				"have to <a href='create'>create a synchronize participant</a> that you will configure to determine the resources to synchronize.</p></form>");			
		text.setText(introText, true, false);
		text.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				IWizard wizard = new GlobalSynchronizeWizard();
				WizardDialog dialog = new WizardDialog(composite.getShell(), wizard);
				dialog.open();
			}
		});
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.MessagePage#setFocus()
	 */
	public void setFocus() {
		if(composite != null && ! composite.isDisposed())
			composite.setFocus();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.Page#dispose()
	 */
	public void dispose() {
		forms.dispose();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.Page#getControl()
	 */
	public Control getControl() {
		return composite;
	}
}