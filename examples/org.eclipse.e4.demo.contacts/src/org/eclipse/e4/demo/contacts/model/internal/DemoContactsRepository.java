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

package org.eclipse.e4.demo.contacts.model.internal;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.demo.contacts.model.Contact;
import org.eclipse.e4.demo.contacts.model.IContactsRepository;

public class DemoContactsRepository implements IContactsRepository {

	private final Collection<Contact> contacts = Collections
			.synchronizedCollection(new ArrayList<Contact>());

	public DemoContactsRepository() {

		URL url = FileLocator.find(Platform
				.getBundle("org.eclipse.e4.demo.contacts"), new Path("vcards"),
				null);

		try {
			URI uri = FileLocator.toFileURL(url).toURI();
			File directory = new File(uri);
			for (String file : directory.list()) {
				if (file.endsWith(".vcf")) {
					Contact contact = new Contact();
					contact.readFromVCard(directory.getAbsolutePath()
							+ File.separator + file);
					contacts.add(contact);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addContact(final Contact contact) {
		contacts.add(contact);
	}

	public Collection<Contact> getAllContacts() {
		return Collections.unmodifiableCollection(contacts);
	}

	public void removeContact(final Contact contact) {
		contacts.remove(contact);
	}
}
