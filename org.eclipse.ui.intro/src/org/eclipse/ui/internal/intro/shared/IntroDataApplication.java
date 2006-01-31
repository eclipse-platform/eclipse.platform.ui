package org.eclipse.ui.internal.intro.shared;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.intro.impl.model.loader.IntroContentParser;
import org.eclipse.ui.internal.intro.impl.model.util.BundleUtil;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class IntroDataApplication implements IPlatformRunnable {
	
class PageData {
	String id;
	ArrayList extensions = new ArrayList();
	PageData(String id) {
		this.id = id;
	}
}

	public Object run(Object args) throws Exception {
		Hashtable pages = findCandidates();
		writeIntroData(pages, System.out);
		return new Integer(0);
	}

	private void writeIntroData(Hashtable pages, PrintStream stream) {
		stream.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>"); //$NON-NLS-1$
		stream.println("<extensions>"); //$NON-NLS-1$
		writePage(ISharedIntroConstants.ID_OVERVIEW, stream, pages);
		writePage(ISharedIntroConstants.ID_FIRSTSTEPS, stream, pages);
		writePage(ISharedIntroConstants.ID_TUTORIALS, stream, pages);
		writePage(ISharedIntroConstants.ID_SAMPLES, stream, pages);
		writePage(ISharedIntroConstants.ID_WHATSNEW, stream, pages);
		writePage(ISharedIntroConstants.ID_MIGRATE, stream, pages);
		writePage(ISharedIntroConstants.ID_WEBRESOURCES, stream, pages);
		stream.println("</extensions>"); //$NON-NLS-1$
	}

	private void writePage(String id, PrintStream stream, Hashtable pages) {
		PageData pd = (PageData)pages.get(id);
		if (pd==null)
			return;
		stream.println("   <page id=\""+id+"\">");  //$NON-NLS-1$//$NON-NLS-2$
		stream.println("      <group path=\""+"??"+"\">");  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		for (int i=0; i<pd.extensions.size(); i++) {
			String eid = (String)pd.extensions.get(i);
			stream.println("         <extension id=\""+eid+"\" importance=\"low\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		stream.println("      </group>"); //$NON-NLS-1$
		stream.println("   </page>"); //$NON-NLS-1$
	}

	private Hashtable findCandidates() {
		Hashtable pages = new Hashtable();
		IConfigurationElement [] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.intro.configExtension"); //$NON-NLS-1$
		for (int i=0; i<elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals("configExtension")) { //$NON-NLS-1$
				String cid = element.getAttribute("configId"); //$NON-NLS-1$
				if (cid!=null && cid.equals("org.eclipse.ui.intro.sharedConfig")) { //$NON-NLS-1$
					addCandidate(pages, element);
				}
			}
		}
		return pages;
	}
	private void addCandidate(Hashtable pages, IConfigurationElement element) {
		String fileName = element.getAttribute("content"); //$NON-NLS-1$
		if (fileName==null)
			return;
		String bundleId = element.getDeclaringExtension().getNamespace();
		Bundle bundle = Platform.getBundle(bundleId);
		if (bundle==null)
			return;
		String content = BundleUtil.getResolvedResourceLocation("", fileName, //$NON-NLS-1$
                bundle);
        IntroContentParser parser = new IntroContentParser(content);
        Document dom = parser.getDocument();
        Element root = dom.getDocumentElement();
        Element extension = null;
        NodeList children = root.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
        	Node child = children.item(i);
        	if (child.getNodeType()==Node.ELEMENT_NODE) {
        		Element el = (Element)child;
        		if (el.getNodeName().equalsIgnoreCase("extensionContent")) { //$NON-NLS-1$
        			extension = el;
        			break;
        		}
        	}
         }
        if (extension==null)
        	return;
        String id = extension.getAttribute("id"); //$NON-NLS-1$
        String path = extension.getAttribute("path"); //$NON-NLS-1$
        if (id==null || path==null)
        	return;
        int at = path.lastIndexOf("/@"); //$NON-NLS-1$
        if (at == -1)
        	return;
        path = path.substring(0, at);
        PageData pdata = (PageData)pages.get(path);
        if (pdata==null) {
        	pdata = new PageData(path);
        	pages.put(path, pdata);
        }
        pdata.extensions.add(id);
	}
}