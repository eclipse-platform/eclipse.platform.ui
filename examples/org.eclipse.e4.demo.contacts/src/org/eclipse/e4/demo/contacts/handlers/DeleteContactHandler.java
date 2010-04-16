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

import javax.inject.Named;

import org.eclipse.e4.demo.contacts.model.Contact;
import org.eclipse.e4.demo.contacts.model.ContactsRepositoryFactory;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.services.IServiceConstants;

public class DeleteContactHandler {
	
	boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) MContext context) {
		Contact contact = (Contact) context.getContext().get(
				IServiceConstants.SELECTION);
		return contact != null;
	}

	void execute(@Named(IServiceConstants.ACTIVE_PART) MContext context) {
		Contact contact = (Contact) context.getContext().get(
				IServiceConstants.SELECTION);
		ContactsRepositoryFactory.getContactsRepository()
				.removeContact(contact);
	}
}
