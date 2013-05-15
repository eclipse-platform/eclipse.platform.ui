/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.contacts.handlers;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.demo.contacts.model.Contact;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.program.Program;

public class SendEmailHandler {
	@Inject
	@Named(IServiceConstants.ACTIVE_SELECTION)
	Contact selected;

	@Execute
	public void execute() {
		if(selected != null && selected.getEmail() != null
				&& selected.getEmail().length() != 0) {
			Program.launch("mailto:" + selected.getEmail());
		}
	}

	@CanExecute
	public boolean canExecute() {
		return selected != null && selected.getEmail() != null
				&& selected.getEmail().length() != 0;
	}

}