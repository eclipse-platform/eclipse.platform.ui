package org.eclipse.update.internal.ui.model;

import java.util.*;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.SAXException;
import java.io.*;
import org.w3c.dom.*;
import java.net.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class BookmarkUtil {
	public static void parse(String fileName, Vector bookmarks) {
		File file = new File(fileName);
		if (!file.exists()) return;
		DOMParser parser = new DOMParser();
		try {
			parser.parse(fileName);
			Document doc = parser.getDocument();
			Node root = doc.getDocumentElement();
			processRoot(root, bookmarks);
		} catch (SAXException e) {
			UpdateUIPlugin.logException(e);
		} catch (IOException e) {
			UpdateUIPlugin.logException(e);
		}
	}

	public static SiteBookmark[] getBookmarks(Vector bookmarks) {
		ArrayList result = new ArrayList();
		for (int i = 0; i < bookmarks.size(); i++) {
			processEntry(bookmarks.get(i), result);
		}
		return (SiteBookmark[]) result.toArray(new SiteBookmark[result.size()]);
	}

	private static void processRoot(Node root, Vector bookmarks) {
		if (root.getNodeName().equals("bookmarks")) {
			NodeList children = root.getChildNodes();
			processChildren(children, null, bookmarks);
		}
	}
	private static void processChildren(
		NodeList children,
		BookmarkFolder folder,
		Vector bookmarks) {
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().equals("site")) {
					SiteBookmark site = createSite(child);
					if (folder != null) {
						folder.addChild(site);
					} else {
						bookmarks.add(site);
					}
				} else if (child.getNodeName().equals("folder")) {
					BookmarkFolder cfolder = createFolder(child);
					if (folder != null)
						folder.addChild(cfolder);
					else
						bookmarks.add(cfolder);
				}
			}
		}
	}

	private static SiteBookmark createSite(Node child) {
		SiteBookmark bookmark = new SiteBookmark();
		String name = getAttribute(child, "name");
		URL url = null;
		try {
			url = new URL(getAttribute(child, "url"));
		} catch (MalformedURLException e) {
		}
		return new SiteBookmark(name, url);
	}

	private static BookmarkFolder createFolder(Node child) {
		BookmarkFolder folder = new BookmarkFolder();
		String name = getAttribute(child, "name");
		folder.setName(name);
		if (child.hasChildNodes()) {
			NodeList children = child.getChildNodes();
			processChildren(children, folder, null);
		}
		return folder;
	}
	public static void store(String fileName, Vector bookmarks) {
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			PrintWriter writer = new PrintWriter(fos);
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.println("<bookmarks>");
			for (int i = 0; i < bookmarks.size(); i++) {
				Object obj = bookmarks.get(i);
				writeObject("   ", obj, writer);
			}
			writer.println("</bookmarks>");
			writer.flush();
			writer.close();
			fos.close();
		} catch (IOException e) {
		}
	}
	private static void writeObject(
		String indent,
		Object obj,
		PrintWriter writer) {
		if (obj instanceof SiteBookmark) {
			SiteBookmark bookmark = (SiteBookmark) obj;
			String name = bookmark.getName();
			String url = bookmark.getURL().toString();
			writer.println(indent + "<site name=\"" + name + "\" url=\"" + url + "\"/>");
		} else if (obj instanceof BookmarkFolder) {
			BookmarkFolder folder = (BookmarkFolder) obj;
			String name = folder.getName();
			writer.println(indent + "<folder name=\"" + name + "\">");
			Object[] children = folder.getChildren(folder);
			String indent2 = indent + "   ";
			for (int i = 0; i < children.length; i++) {
				writeObject(indent2, children[i], writer);
			}
			writer.println(indent + "</folder>");
		}
	}

	private static String getAttribute(Node node, String name) {
		NamedNodeMap atts = node.getAttributes();
		Node att = atts.getNamedItem(name);
		if (att != null) {
			return att.getNodeValue();
		}
		return "";
	}
	private static void processFolder(BookmarkFolder folder, ArrayList result) {
		Object[] children = folder.getChildren(folder);
		for (int i = 0; i < children.length; i++) {
			processEntry(children[i], result);
		}
	}
	private static void processEntry(Object obj, ArrayList result) {
		if (obj instanceof SiteBookmark)
			result.add(obj);
		else if (obj instanceof BookmarkFolder) {
			processFolder((BookmarkFolder) obj, result);
		}
	}
}