package org.eclipse.help.internal.filter;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.*;

import org.eclipse.help.*;
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
	
	public WorkingSet(String name, IHelpResource[] elements) {
		this.name = name;
		if (elements == null)
			elements = new IHelpResource[0];
		
		this.elements = new ArrayList(elements.length);
		for (int i=0; i<elements.length; i++)
			addElement(elements[i]);
	}
	
	public void addElement(IHelpResource element) {
		elements.add(element);
	}
	
	public void removeElement(IHelpResource element) {
		elements.remove(element);
	}
	
	public String getName() {
		return name;
	}
	
	public IHelpResource[] getElements() {
		IHelpResource[] array = new IHelpResource[elements.size()];
		elements.toArray(array);
		return array;
	}
	
	public void saveState(Element parent) {
		Document doc = parent.getOwnerDocument();
		Element ws = doc.createElement("workingSet");
		ws.setAttribute("name", name);
		parent.appendChild(ws);
		
		for (Iterator it=elements.iterator(); it.hasNext(); ) {
			Element child = doc.createElement("item");
			IHelpResource helpResource = (IHelpResource)it.next();
			child.setAttribute("href", helpResource.getHref());
			child.setAttribute("label", helpResource.getLabel());
			ws.appendChild(child);
		}
	}
}
