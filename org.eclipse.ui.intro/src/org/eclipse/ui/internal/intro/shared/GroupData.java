package org.eclipse.ui.internal.intro.shared;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupData {
	private String path;
	private ArrayList extensions = new ArrayList();
	
	public GroupData(Element element) {
		if (element.getNodeName().equals("hidden"))
			path = ISharedIntroConstants.HIDDEN;
		else
			path = element.getAttribute("path");
		NodeList children = element.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType()==Node.ELEMENT_NODE &&
					child.getNodeName().equals("extension")) {
				loadExtension((Element)child);
			}
		}
	}
	
	private void loadExtension(Element element) {
		String id = element.getAttribute("id");
		String importance = element.getAttribute("importance");
		ExtensionData ed = new ExtensionData(id, importance);
		extensions.add(ed);
	}
	
	public ExtensionData [] getExtensions() {
		return (ExtensionData[])extensions.toArray(new ExtensionData[extensions.size()]);
	}
	
	public String getPath() {
		return path;
	}
	
	public boolean contains(String extensionId) {
		for (int i=0; i<extensions.size(); i++) {
			ExtensionData ed = (ExtensionData)extensions.get(i);
			if (ed.getId().equals(extensionId))
				return true;
		}
		return false;
	}
}