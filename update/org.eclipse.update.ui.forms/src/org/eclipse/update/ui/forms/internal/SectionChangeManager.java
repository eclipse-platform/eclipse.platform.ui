package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

public class SectionChangeManager {
	Hashtable sources = new Hashtable();

public SectionChangeManager() {
	super();
}
public void dispatchNotification(
	FormSection source,
	int type,
	Object changeObject) {
	Vector targets = (Vector) sources.get(source);
	if (targets != null) {
		for (Iterator iter = targets.iterator(); iter.hasNext();) {
			FormSection section = (FormSection) iter.next();
			section.sectionChanged(source, type, changeObject);
		}
	}
}
public void linkSections(FormSection source, FormSection target) {
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
