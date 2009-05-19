/*******************************************************************************
 * Copyright (c) 2009 Siemens AG and others.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Kai Tödter - initial implementation
 ******************************************************************************/

package org.eclipse.e4.demo.contacts.handlers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.demo.contacts.model.Contact;
import org.eclipse.e4.demo.contacts.model.ContactsRepositoryFactory;
import org.eclipse.e4.workbench.ui.IExceptionHandler;

public class DeleteContactHandler {

	public void execute(Contact contact, IProgressMonitor monitor,
			IExceptionHandler exceptionHandler) {
		ContactsRepositoryFactory.getContactsRepository()
				.removeContact(contact);
	}
}
