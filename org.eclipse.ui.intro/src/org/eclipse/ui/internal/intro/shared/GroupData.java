package org.eclipse.ui.internal.intro.shared;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.eclipse.ui.intro.config.IntroElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupData {
	boolean fDefault=false;
	private String path;
	private ArrayList extensions = new ArrayList();
	
	public GroupData(String path, boolean defaultGroup) {
		fDefault = defaultGroup;
		this.path = path;
	}

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
	
	public boolean isHidden() {
		return (path.equals(ISharedIntroConstants.HIDDEN));
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
	
	public void add(ExtensionData ed) {
		extensions.add(ed);
	}
	public void remove(ExtensionData ed) {
		extensions.remove(ed);
	}
	
	public void addImplicitExtension(String id, String name) {
		ExtensionData ed = new ExtensionData(id, name, ISharedIntroConstants.LOW, true);
		extensions.add(ed);
	}

	private void loadExtension(Element element) {
		String id = element.getAttribute("id"); //$NON-NLS-1$
		String name = element.getAttribute("name"); //$NON-NLS-1$
		String importance = element.getAttribute("importance"); //$NON-NLS-1$
		ExtensionData ed = new ExtensionData(id, name, importance, false);
		extensions.add(ed);
	}

	public ExtensionData[] getExtensions() {
		return (ExtensionData[]) extensions.toArray(new ExtensionData[extensions.size()]);
	}

	public String getPath() {
		return path;
	}

	public boolean contains(String extensionId) {
		return find(extensionId)!=null;
	}
	
	ExtensionData find(String extensionId) {
		for (int i = 0; i < extensions.size(); i++) {
			ExtensionData ed = (ExtensionData) extensions.get(i);
			if (ed.getId().equals(extensionId))
				return ed;
		}
		return null;
	}
	
	public boolean canMoveUp(ExtensionData ed) {
		int index = extensions.indexOf(ed);
		return (index>0);
	}
	
	public boolean canMoveDown(ExtensionData ed) {
		int index = extensions.indexOf(ed);
		return (index!= -1 && index < extensions.size()-1);
	}
	
	public void moveUp(ExtensionData ed) {
		int index = extensions.indexOf(ed);
		ExtensionData swapped = (ExtensionData)extensions.get(index-1);
		extensions.set(index, swapped);
		extensions.set(index-1, ed);
	}

	public void moveDown(ExtensionData ed) {
		int index = extensions.indexOf(ed);
		ExtensionData swapped = (ExtensionData)extensions.get(index+1);
		extensions.set(index, swapped);
		extensions.set(index+1, ed);		
	}
	
	public void write(PrintWriter writer, String indent) {
		writer.print(indent);
		writer.print("<group path=\""+path+"\"");  //$NON-NLS-1$ //$NON-NLS-2$
		if (fDefault)
			writer.println(" default=\"true\">"); //$NON-NLS-1$
		else
			writer.println(">"); //$NON-NLS-1$
		for (int i=0; i<extensions.size(); i++) {
			ExtensionData ed = (ExtensionData)extensions.get(i);
			ed.write(writer, indent+"   "); //$NON-NLS-1$
		}
		writer.print(indent);
		writer.println("</group>"); //$NON-NLS-1$
	}
}