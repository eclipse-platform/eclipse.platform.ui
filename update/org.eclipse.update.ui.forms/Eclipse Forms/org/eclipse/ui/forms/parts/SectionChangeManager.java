/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.parts;

import java.util.*;

public class SectionChangeManager {
	Hashtable sources = new Hashtable();

	public SectionChangeManager() {
		super();
	}
	public void dispatchNotification(
		SectionPart source,
		int type,
		Object changeObject) {
		Vector targets = (Vector) sources.get(source);
		if (targets != null) {
			for (Iterator iter = targets.iterator(); iter.hasNext();) {
				SectionPart section = (SectionPart) iter.next();
				section.sectionChanged(source, type, changeObject);
			}
		}
	}
	public void linkSections(SectionPart source, SectionPart target) {
		source.setManager(this);
		Vector targets = null;

		targets = (Vector) sources.get(source);
		if (targets == null) {
			targets = new Vector();
			sources.put(source, targets);
		}
		targets.addElement(target);
	}
}
