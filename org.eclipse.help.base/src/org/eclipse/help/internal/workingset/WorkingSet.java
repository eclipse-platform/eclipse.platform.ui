/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.workingset;

import java.util.*;

import org.w3c.dom.*;

public class WorkingSet {
	private String name;
	private List elements;

	public WorkingSet(String name) {
		this(name, (List) null);
	}

	public WorkingSet(String name, List elements) {
		this.name = name;
		if (elements == null)
			elements = new ArrayList();

		this.elements = elements;
	}

	public WorkingSet(String name, AdaptableHelpResource[] elements) {
		this.name = name;
		if (elements == null)
			elements = new AdaptableHelpResource[0];

		this.elements = new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			this.elements.add(elements[i]);
		}
	}

	public void removeElement(AdaptableHelpResource element) {
		// Note: this is based on equality of IHelpResource and
		// AdaptableHelpResource
		elements.remove(element);
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		if (newName == null)
			return;
		name = newName;
	}

	public AdaptableHelpResource[] getElements() {
		AdaptableHelpResource[] array = new AdaptableHelpResource[elements
				.size()];
		elements.toArray(array);
		return array;
	}

	public void setElements(AdaptableHelpResource[] elements) {
		this.elements = new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++)
			this.elements.add(elements[i]);
	}

	public void saveState(Element parent) {
		Document doc = parent.getOwnerDocument();
		Element ws = doc.createElement("workingSet"); //$NON-NLS-1$
		ws.setAttribute("name", name); //$NON-NLS-1$
		parent.appendChild(ws);

		for (Iterator it = elements.iterator(); it.hasNext();) {
			Element child = doc.createElement("item"); //$NON-NLS-1$
			AdaptableHelpResource helpResource = (AdaptableHelpResource) it
					.next();
			helpResource.saveState(child);
			ws.appendChild(child);
		}
	}
}
