package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import java.util.*;
import org.eclipse.swt.SWT;

public class ReviewPage extends WizardPage {
	private Vector jobs;

	/**
	 * Constructor for ReviewPage
	 */
	public ReviewPage(Vector jobs) {
		super("Review");
		setTitle("Installation Overview");
		setDescription("Review the list of features scheduled to be installed or uninstalled. "+
		               "The features will be processed in the order as shown in the list.");
		this.jobs = jobs;
	}

	/**
	 * @see DialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		setControl(client);
	}
}