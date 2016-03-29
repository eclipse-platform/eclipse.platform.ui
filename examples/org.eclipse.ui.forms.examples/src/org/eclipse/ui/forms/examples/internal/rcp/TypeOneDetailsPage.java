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
package org.eclipse.ui.forms.examples.internal.rcp;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.widgets.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TypeOneDetailsPage implements IDetailsPage {
	private IManagedForm mform;
	private TypeOne input;
	private Button [] choices;
	private Button flag;
	private Text text;
	private static final String RTEXT_DATA =
			"<form><p>An example of a free-form text that should be "+
			"wrapped below the section with widgets.</p>"+
			"<p>It can contain simple tags like <a>links</a> and <b>bold text</b>.</p></form>";

	public TypeOneDetailsPage() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	public void initialize(IManagedForm mform) {
		this.mform = mform;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public void createContents(Composite parent) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 5;
		layout.leftMargin = 5;
		layout.rightMargin = 2;
		layout.bottomMargin = 2;
		parent.setLayout(layout);

		FormToolkit toolkit = mform.getToolkit();
		Section s1 = toolkit.createSection(parent, Section.DESCRIPTION);
		s1.marginWidth = 10;
		s1.setText("Type One Details");
		s1.setDescription("Set the properties of the selected TypeOne object.");
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		s1.setLayoutData(td);
		toolkit.createCompositeSeparator(s1);
		Composite client = toolkit.createComposite(s1);
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = glayout.marginHeight = toolkit.getBorderStyle()==SWT.BORDER?0:2;
		glayout.numColumns = 2;
		client.setLayout(glayout);
		//client.setBackground(client.getDisplay().getSystemColor(SWT.COLOR_CYAN));

		SelectionListener choiceListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Integer value = (Integer)e.widget.getData();
				if (input!=null) {
					input.setChoice(value.intValue());
				}
			}
		};
		GridData gd;
		choices = new Button[TypeOne.CHOICES.length];
		for (int i=0; i<TypeOne.CHOICES.length; i++) {
			choices[i] = toolkit.createButton(client, TypeOne.CHOICES[i], SWT.RADIO);
			choices[i].setData(Integer.valueOf(i));
			choices[i].addSelectionListener(choiceListener);
			gd = new GridData();
			gd.horizontalSpan = 2;
			choices[i].setLayoutData(gd);
		}
		createSpacer(toolkit, client, 2);
		flag = toolkit.createButton(client, "Value of the flag property", SWT.CHECK);
		flag.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (input!=null)
					input.setFlag(flag.getSelection());
			}
		});
		gd = new GridData();
		gd.horizontalSpan = 2;
		flag.setLayoutData(gd);
		createSpacer(toolkit, client, 2);

		toolkit.createLabel(client, "Text property:");
		text = toolkit.createText(client, "", SWT.SINGLE);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (input!=null)
					input.setText(text.getText());
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 10;
		text.setLayoutData(gd);

		createSpacer(toolkit, client, 2);

		FormText rtext = toolkit.createFormText(parent, false);
		rtext.setText(RTEXT_DATA, true, false);
		td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		rtext.setLayoutData(td);

		toolkit.paintBordersFor(client);
		s1.setClient(client);
	}
	private void createSpacer(FormToolkit toolkit, Composite parent, int span) {
		Label spacer = toolkit.createLabel(parent, "");
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		spacer.setLayoutData(gd);
	}
	private void update() {
		for (int i=0; i<TypeOne.CHOICES.length; i++) {
			choices[i].setSelection(input!=null && input.getChoice()==i);
		}
		flag.setSelection(input!=null && input.getFlag());
		text.setText(input!=null && input.getText()!=null?input.getText():"");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#inputChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IFormPart part, ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection)selection;
		if (ssel.size()==1) {
			input = (TypeOne)ssel.getFirstElement();
		}
		else
			input = null;
		update();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#commit()
	 */
	public void commit(boolean onSave) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#setFocus()
	 */
	public void setFocus() {
		choices[0].setFocus();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#dispose()
	 */
	public void dispose() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}
	public boolean isStale() {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#refresh()
	 */
	public void refresh() {
		update();
	}
	public boolean setFormInput(Object input) {
		return false;
	}
}
