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

package org.eclipse.e4.demo.contacts.views;

import org.eclipse.e4.demo.contacts.model.Contact;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

class ContactViewerComparator extends ViewerComparator {

	@Override
	public int compare(final Viewer viewer, final Object obj1, final Object obj2) {

		if (obj1 instanceof Contact && obj2 instanceof Contact) {
			return ((Contact) obj1).getLastName().compareTo(
					((Contact) obj2).getLastName());
		} else {
			throw new IllegalArgumentException("Can only compare two Contacts.");
		}

	}
}
