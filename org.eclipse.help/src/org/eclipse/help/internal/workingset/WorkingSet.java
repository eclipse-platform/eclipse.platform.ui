package org.eclipse.help.internal.workingset;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.*;

import org.eclipse.help.internal.HelpSystem;
import org.w3c.dom.*;

public class WorkingSet {
	private String name;
	private List elements;

	public WorkingSet(String name) {
		this(name, (List)null);
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
		for (int i=0; i<elements.length; i++) {
			this.elements.add(elements[i]);
		}
	}
	
	public void removeElement(AdaptableHelpResource element) {
		// Note: this is based on equality of IHelpResource and AdaptableHelpResource
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
		AdaptableHelpResource[] array = new AdaptableHelpResource[elements.size()];
		elements.toArray(array);
		return array;
	}
	
	public void setElements(AdaptableHelpResource[] elements) {
		this.elements = new ArrayList(elements.length);
		for (int i=0; i<elements.length; i++)
			this.elements.add(elements[i]);
	}
	
	public void saveState(Element parent) {
		Document doc = parent.getOwnerDocument();
		Element ws = doc.createElement("workingSet");
		ws.setAttribute("name", name);
		parent.appendChild(ws);
		
		for (Iterator it=elements.iterator(); it.hasNext(); ) {
			Element child = doc.createElement("item");
			AdaptableHelpResource helpResource = (AdaptableHelpResource)it.next();
			helpResource.saveState(child);
			ws.appendChild(child);
		}
	}
}
