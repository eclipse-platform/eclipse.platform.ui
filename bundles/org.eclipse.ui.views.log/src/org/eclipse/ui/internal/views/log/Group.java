/*******************************************************************************
 *  Copyright (c) 2007, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bugs 202583, 207344
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 218648
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 485843
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import java.io.PrintWriter;

/**
 * Groups other Abstract Entries under given name.
 */
public class Group extends AbstractEntry {

	private String name;

	public Group(String name) {
		this.name = name;
	}

	@Override
	public void write(PrintWriter writer) {
		Object[] children = getChildren(null);
		for (Object element : children) {
			AbstractEntry entry = (AbstractEntry) element;
			entry.write(writer);
			writer.println();
		}
	}

	@Override
	public String toString() {
		return name;
	}

}