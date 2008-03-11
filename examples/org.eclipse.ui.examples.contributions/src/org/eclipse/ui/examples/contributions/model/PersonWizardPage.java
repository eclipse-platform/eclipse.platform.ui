/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions.model;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.examples.contributions.ContributionMessages;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Create a Person, fill in the correct fields.
 * 
 * @since 3.4
 */
public class PersonWizardPage extends WizardPage implements Listener {

	private IServiceLocator locator;
	private Text surnameText;
	private Text givennameText;
	private Text idText;

	/**
	 * @param pageName
	 */
	public PersonWizardPage(IServiceLocator locator) {
		super("personWizardPage"); //$NON-NLS-1$
		this.locator = locator;
		setTitle(ContributionMessages.PersonWizardPage_title);
		setDescription(ContributionMessages.PersonWizardPage_descriptoin);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		// top level group
		Composite topLevel = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		topLevel.setLayout(gridLayout);

		Label l = new Label(topLevel, SWT.RIGHT);
		l.setText(ContributionMessages.PersonWizardPage_id_label);
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		idText = new Text(topLevel, SWT.SINGLE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		idText.setLayoutData(gridData);
		idText.addListener(SWT.FocusOut, this);
		idText.addListener(SWT.KeyUp, this);

		l = new Label(topLevel, SWT.RIGHT);
		l.setText(ContributionMessages.InfoEditor_surname);
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		surnameText = new Text(topLevel, SWT.SINGLE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		surnameText.setLayoutData(gridData);
		surnameText.addListener(SWT.FocusOut, this);
		surnameText.addListener(SWT.KeyUp, this);

		l = new Label(topLevel, SWT.RIGHT);
		l.setText(ContributionMessages.InfoEditor_givenname);
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		givennameText = new Text(topLevel, SWT.SINGLE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		givennameText.setLayoutData(gridData);
		givennameText.addListener(SWT.FocusOut, this);
		givennameText.addListener(SWT.KeyUp, this);

		setControl(topLevel);
		setPageComplete(validatePage());
		setErrorMessage(null);
		setMessage(null);
	}

	private boolean validatePage() {
		if (getId() == 0) {
			return false;
		}
		IPersonService service = (IPersonService) locator
				.getService(IPersonService.class);
		if (service.getPerson(getId()) != null) {
			setErrorMessage(NLS.bind(
					ContributionMessages.PersonWizardPage_error_alreadyExists,
					new Integer(getId())));
			return false;
		}
		if (getSurname() == null || getSurname().length() == 0) {
			setErrorMessage(ContributionMessages.PersonWizardPage_error_missingSurname);
			return false;
		}
		if (getGivenname() == null || getGivenname().length() == 0) {
			setErrorMessage(ContributionMessages.PersonWizardPage_error_missingGivenname);
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	int getId() {
		try {
			return Integer.parseInt(idText.getText());
		} catch (NumberFormatException e) {
		}
		return 0;
	}

	String getGivenname() {
		return givennameText.getText();
	}

	String getSurname() {
		return surnameText.getText();
	}

	boolean finish() {
		IPersonService service = (IPersonService) locator
				.getService(IPersonService.class);
		Person person = service.createPerson(getId());
		if (person == null) {
			return false;
		}
		person.setGivenname(getGivenname());
		person.setSurname(getSurname());
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		// this plus validatePage() are blunt force validation.
		setPageComplete(validatePage());
	}
}
