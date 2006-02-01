package org.eclipse.ui.internal.intro.shared;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PageData {
	private String id;
	private ArrayList groups = new ArrayList();
	private GroupData hidden=null;
	
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
	
	private GroupData findGroup(String groupId) {
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
	
	public String getId() {
		return id;
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
}