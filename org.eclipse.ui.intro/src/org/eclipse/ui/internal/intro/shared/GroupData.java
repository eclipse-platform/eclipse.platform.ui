package org.eclipse.ui.internal.intro.shared;

import java.util.ArrayList;

import org.eclipse.ui.intro.config.IntroElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupData {
	boolean fDefault=false;
	private String path;
	private ArrayList extensions = new ArrayList();

	public GroupData(Element element) {
		if (element.getNodeName().equals("hidden")) //$NON-NLS-1$
			path = ISharedIntroConstants.HIDDEN;
		else
			path = element.getAttribute("path"); //$NON-NLS-1$
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("extension")) { //$NON-NLS-1$
				loadExtension((Element) child);
			}
		}
		String df = element.getAttribute("default"); //$NON-NLS-1$
		if (df!=null && df.equalsIgnoreCase("true")) //$NON-NLS-1$
			fDefault = true;
	}
	
	public boolean isDefault() {
		return fDefault;
	}

	public void addAnchors(ArrayList result) {
		for (int i = 0; i < extensions.size(); i++) {
			ExtensionData edata = (ExtensionData) extensions.get(i);
			String id = edata.getId();
			IntroElement anchor = new IntroElement("anchor"); //$NON-NLS-1$
			anchor.setAttribute("id", id); //$NON-NLS-1$
			result.add(anchor);
		}
	}

	private void loadExtension(Element element) {
		String id = element.getAttribute("id"); //$NON-NLS-1$
		String importance = element.getAttribute("importance"); //$NON-NLS-1$
		ExtensionData ed = new ExtensionData(id, importance);
		extensions.add(ed);
	}

	public ExtensionData[] getExtensions() {
		return (ExtensionData[]) extensions.toArray(new ExtensionData[extensions.size()]);
	}

	public String getPath() {
		return path;
	}

	public boolean contains(String extensionId) {
		for (int i = 0; i < extensions.size(); i++) {
			ExtensionData ed = (ExtensionData) extensions.get(i);
			if (ed.getId().equals(extensionId))
				return true;
		}
		return false;
	}
}