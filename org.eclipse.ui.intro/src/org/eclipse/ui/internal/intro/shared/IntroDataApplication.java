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
		stream.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
		stream.println("<extensions>");
		writePage("overview", stream, pages);
		writePage("firststeps", stream, pages);
		writePage("tutorials", stream, pages);
		writePage("samples", stream, pages);
		writePage("migrate", stream, pages);
		writePage("whatsnew", stream, pages);
		writePage("webresources", stream, pages);
		stream.println("</extensions>");
	}

	private void writePage(String id, PrintStream stream, Hashtable pages) {
		PageData pd = (PageData)pages.get(id);
		if (pd==null)
			return;
		stream.println("   <page id=\""+id+"\">");
		stream.println("      <group path=\""+"??"+"\">");
		for (int i=0; i<pd.extensions.size(); i++) {
			String eid = (String)pd.extensions.get(i);
			stream.println("         <extension id=\""+eid+"\" importance=\"low\"/>");
		}
		stream.println("      </group>");
		stream.println("   </page>");
	}

	private Hashtable findCandidates() {
		Hashtable pages = new Hashtable();
		IConfigurationElement [] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.intro.configExtension");
		for (int i=0; i<elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals("configExtension")) {
				String cid = element.getAttribute("configId");
				if (cid!=null && cid.equals("org.eclipse.ui.intro.sharedConfig")) {
					addCandidate(pages, element);
				}
			}
		}
		return pages;
	}
	private void addCandidate(Hashtable pages, IConfigurationElement element) {
		String fileName = element.getAttribute("content");
		if (fileName==null)
			return;
		String bundleId = element.getDeclaringExtension().getNamespace();
		Bundle bundle = Platform.getBundle(bundleId);
		if (bundle==null)
			return;
		String content = BundleUtil.getResolvedResourceLocation("", fileName,
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
        		if (el.getNodeName().equalsIgnoreCase("extensionContent")) {
        			extension = el;
        			break;
        		}
        	}
         }
        if (extension==null)
        	return;
        String id = extension.getAttribute("id");
        String path = extension.getAttribute("path");
        if (id==null || path==null)
        	return;
        int at = path.lastIndexOf("/@");
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