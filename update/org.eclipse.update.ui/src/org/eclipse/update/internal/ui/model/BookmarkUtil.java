package org.eclipse.update.internal.ui.model;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.xerces.parsers.DOMParser;
import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.search.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class BookmarkUtil {
	public static void parse(String fileName, Vector bookmarks) {
		File file = new File(fileName);
		if (!file.exists())
			return;
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

	public static BookmarkFolder getFolder(Vector bookmarks, IPath path) {
		NamedModelObject object = find(bookmarks, path);
		if (object != null && object instanceof BookmarkFolder)
			return (BookmarkFolder) object;
		return null;
	}

	public static NamedModelObject find(Vector bookmarks, IPath path) {
		Object[] array = bookmarks.toArray();
		return find(array, path);
	}

	private static NamedModelObject find(Object[] array, IPath path) {
		String name = path.segment(0);
		for (int i = 0; i < array.length; i++) {
			NamedModelObject obj = (NamedModelObject) array[i];
			if (obj.getName().equals(name)) {
				if (obj instanceof BookmarkFolder) {
					if (path.segmentCount() > 1) {
						IPath childPath = path.removeFirstSegments(1);
						BookmarkFolder folder = (BookmarkFolder) obj;
						return find(folder.getChildren(null), childPath);
					}
				}
				return obj;
			}
		}
		return null;
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
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			NamedModelObject object = null;
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().equals("site")) {
					object = createSite(child);

				} else if (child.getNodeName().equals("folder")) {
					object = createFolder(child);
				} else if (child.getNodeName().equals("search")) {
					object = createSearch(child);
				}
			}
			if (object != null) {
				if (folder != null) {
					folder.addChild(object);
				} else {
					bookmarks.add(object);
				}
				object.setModel(model);
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
	private static SearchObject createSearch(Node child) {
		String name = getAttribute(child, "name");
		String categoryId = getAttribute(child, "category");
		String fixed = getAttribute(child, "fixed");
		boolean fixedCategory = fixed.equals("true");
		SearchCategoryDescriptor desc =
			SearchCategoryRegistryReader.getDefault().getDescriptor(categoryId);
		SearchObject search = new SearchObject(name, desc, fixedCategory);
		if (child.hasChildNodes()) {
			NodeList children = child.getChildNodes();
			Hashtable settings = search.getSettings();
			for (int i = 0; i < children.getLength(); i++) {
				Node param = children.item(i);
				if (param.getNodeType() == Node.ELEMENT_NODE
					&& param.getNodeName().equals("param")) {
					String key = getAttribute(param, "name");
					String value = getAttribute(param, "value");
					settings.put(key, value);
				}
			}
		}
		return search;
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
		} else if (obj instanceof SearchObject) {
			SearchObject search = (SearchObject) obj;
			if (search.isPersistent() == false)
				return;
			String name = search.getName();
			String categoryId = search.getCategoryId();
			String fixed = search.isCategoryFixed() ? "true" : "false";
			writer.println(
				indent
					+ "<search name=\""
					+ name
					+ "\" category=\""
					+ categoryId
					+ "\" fixed=\""
					+ fixed
					+ "\">");
			Hashtable settings = search.getSettings();
			String indent2 = indent + "   ";
			for (Enumeration enum = settings.keys(); enum.hasMoreElements();) {
				String key = (String) enum.nextElement();
				String value = (String) settings.get(key);
				writer.println(
					indent2 + "<param name=\"" + key + "\" value=\"" + value + "\"/>");
			}
			writer.println(indent + "</search>");
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