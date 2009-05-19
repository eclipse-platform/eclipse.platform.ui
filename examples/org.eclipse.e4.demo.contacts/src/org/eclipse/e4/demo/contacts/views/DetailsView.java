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
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class DetailsView {

	private final DetailComposite detailComposite;

	public DetailsView(Composite parent) {
		detailComposite = new DetailComposite(parent, SWT.NONE, false, null,
				null);

		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	public Class<Contact> getInputType() {
		return Contact.class;
	}

	public void setInput(Object input) {
		if (input instanceof Contact) {
			detailComposite.update((Contact) input);
		}
	}

}
