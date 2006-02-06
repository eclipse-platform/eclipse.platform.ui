package org.eclipse.ui.internal.intro.shared;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PageData {
	public static final String P_LEFT = "page-content/left"; //$NON-NLS-1$
	public static final String P_RIGHT = "page-content/right"; //$NON-NLS-1$
	public static final String P_BOTTOM = "page-content/bottom"; //$NON-NLS-1$

	private String id;
	private ArrayList groups = new ArrayList();
	private GroupData hidden=null;
	
	public PageData(String id) {
		this.id = id;
	}
	
	public PageData(Element page) {
		this.id = page.getAttribute("id"); //$NON-NLS-1$
		NodeList children = page.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType()==Node.ELEMENT_NODE) {
				Element element = (Element)child;
				if (element.getNodeName().equals("group")) { //$NON-NLS-1$
					addGroup(element);
				}
				else if (element.getNodeName().equals(ISharedIntroConstants.HIDDEN)) {
					addGroup(element);
				}
			}
		}
	}
	
	public void add(GroupData gd) {
		if (gd.isHidden())
			hidden = gd;
		else
			groups.add(gd);
	}

	public void addAnchors(ArrayList result, String groupId) {
		GroupData group = findGroup(groupId);
		if (group==null) return;
		group.addAnchors(result);
	}

	public String resolvePath(String extensionId) {
		if (isHidden(extensionId))
			return null;
		GroupData ddata = null;
		for (int i=0; i<groups.size(); i++) {
			GroupData gdata = (GroupData)groups.get(i);
			if (gdata.isDefault())
				ddata=gdata;
			if (gdata.contains(extensionId)) {
				IPath resolvedPath = new Path(id);
				resolvedPath = resolvedPath.append(gdata.getPath());
				resolvedPath = resolvedPath.append(extensionId);
				return resolvedPath.toString();
			}
		}
		// resolve as default
		IPath resolvedPath = new Path(id).append(ddata.getPath());
		resolvedPath = resolvedPath.append(ISharedIntroConstants.ID_FALLBACK_ANCHOR);
		return resolvedPath.toString();
	}
	
	public boolean isHidden(String extensionId) {
		return hidden!=null && hidden.contains(extensionId);
	}
	
	public GroupData findGroup(String groupId) {
		if (groupId.equals(ISharedIntroConstants.HIDDEN))
			return hidden;
		for (int i=0; i<groups.size(); i++) {
			GroupData gdata = (GroupData)groups.get(i);
			IPath path = new Path(gdata.getPath());
			if (path.lastSegment().equals(groupId))
				return gdata;
		}
		return null;
	}

	private void addGroup(Element element) {
		GroupData gd = new GroupData(element);
		groups.add(gd);
	}
	
	public void addImplicitExtension(String extensionId, String name) {
		ExtensionData ed = findExtension(extensionId);
		if (ed!=null) {
			// see if name needs to be supplied
			if (ed.getName()==null || ed.getName().length()==0)
				ed.setName(name);
			return;
		}
		GroupData gd = findDefaultGroup();
		if (gd==null && groups.size()==0) {
			// add bottom as the default group
			gd = new GroupData("page-content/bottom", true); //$NON-NLS-1$
			groups.add(gd);
		}
		gd.addImplicitExtension(extensionId, name);
	}
	
	private GroupData findDefaultGroup() {
		for (int i=0; i<groups.size(); i++) {
			GroupData gd = (GroupData)groups.get(i);
			if (gd.isDefault())
				return gd;
		}
		return null;
	}
	
	public String getId() {
		return id;
	}
	
	private ExtensionData findExtension(String extensionId) {
		for (int i=0; i<groups.size(); i++) {
			GroupData gdata = (GroupData)groups.get(i);
			ExtensionData ed = gdata.find(extensionId);
			if (ed!=null)
				return ed;
		}
		// check the hidden
		if (hidden!=null)
			return hidden.find(extensionId);
		return null;
	}

	public String resolveExtension(String extensionId) {
		// check the explicit groups
		for (int i=0; i<groups.size(); i++) {
			GroupData gdata = (GroupData)groups.get(i);
			if (gdata.contains(extensionId)) {
				return id+"/"+gdata.getPath()+"/"+extensionId;  //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		// check the hidden
		if (hidden!=null && hidden.contains(extensionId))
			return null;
		// create the default: pick the last group
		if (groups.size()==0) return null;
		GroupData last = (GroupData)groups.get(groups.size()-1);
		return id + "/" + last.getPath() + "/" + ISharedIntroConstants.DEFAULT_ANCHOR;  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	public void write(PrintWriter writer, String indent) {
		writer.print(indent);
		String indent2 = indent+"   "; //$NON-NLS-1$
		writer.println("<page id=\""+id+"\">");  //$NON-NLS-1$//$NON-NLS-2$
		for (int i=0; i<groups.size(); i++) {
			GroupData gd = (GroupData)groups.get(i);
			gd.write(writer, indent2);
		}
		if (hidden!=null)
			hidden.write(writer, indent2);
		writer.print(indent);
		writer.println("</page>"); //$NON-NLS-1$
	}
}