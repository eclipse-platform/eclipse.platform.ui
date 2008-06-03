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
package org.eclipse.team.examples.pessimistic.ui;
 
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * A wizard page that is empty to workaround a bug.
 */
public class BlankPage extends WizardPage {

	/**
	 * Creates a blank page telling the user what is about to happen.
	 */
	public BlankPage() {
		super("AddPessimisticFilesystemSupport");
		setTitle("Pessimistic filesystem provider");
		setDescription("Add pessimistic filesystem provider support to this project");
		setPageComplete(true);
	}

	/**
	 * Creates an empty control.
	 */
	public void createControl(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		client.setLayout(layout);
		setControl(client);
	}
}