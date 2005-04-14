/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Builds a help search index for a plug-in by looking for the
 * <code>org.eclipse.help.toc</code> extensions in the provided manifest file.
 * Search index is only created if index path is specified in the extension.
 * Index will contain data for all the topics listed in all the TOCs declared in
 * the plug-in.
 * 
 * @since 3.1
 */

public class HelpIndexBuilder {
	private static final String POINT_TOC = "org.eclipse.help.toc"; //$NON-NLS-1$
	private static final String EL_TOC = "toc"; //$NON-NLS-1$

	private File manifest;

	private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
			.newInstance();

	private DocumentBuilder parser;

	/**
	 * Creates a new instance of help index builder.
	 * 
	 * @param manifest
	 *            the manifest file (plugin.xml) that will be parsed to find
	 *            TOCs that can be indexed.
	 */
	public HelpIndexBuilder(File manifest) {
		this.manifest = manifest;
	}

	class PluginIdentifier {
		String id;

		PluginVersionIdentifier version;

		public PluginIdentifier(String id, String version) {
			this.id = id;
			this.version = new PluginVersionIdentifier(version);
		}
	}

	/**
	 * Creates the plug-in search index by parsing the provided plugin.xml file,
	 * looking for TOC extensions. If at least one of them has
	 * <code>index</code> element, all topics listed in all the TOCs in the
	 * plug-in will be indexed and stored in the path specified by the
	 * <code>index</code> element.
	 * 
	 * @param monitor
	 *            the monitor to track index creation progress
	 * @throws CoreException
	 *             if there are problems during index creation.
	 */

	public void execute(IProgressMonitor monitor) throws CoreException {
		Document doc = readManifestFile();
		if (doc == null)
			return;

		PluginIdentifier pluginID = getPluginID(doc);

		Element[] extensions = getTocExtensions(doc);
		for (int i = 0; i < extensions.length; i++) {
			processExtension(extensions[i]);
		}
		doc = null; // discard the DOM
	}
	
	private void processExtension(Element extensionNode) {
		NodeList children = extensionNode.getElementsByTagName(EL_TOC);
		for (int i=0; i<children.getLength(); i++) {
			Node node = children.item(i);
			String file = getAttribute(node, "file");
			String primary = getAttribute(node, "primary");
			String extradir = getAttribute(node, "extradir");
			addTocFile(file, primary, extradir);
		}
	}

	/**
	 * Returns the manifest file for this builder.
	 * 
	 * @return
	 */
	public File getManifest() {
		return manifest;
	}

	/**
	 * Sets the new manifest file (plugin.xml) to be used by this builder.
	 * 
	 * @param manifest
	 */
	public void setManifest(File manifest) {
		this.manifest = manifest;
	}

	private void addTocFile(String file,
			String primary, String extradir) {
	}

	private PluginIdentifier getPluginID(Document doc) throws CoreException {
		Node root = doc.getDocumentElement();
		String id = getAttribute(root, "id");
		String version = getAttribute(root, "version");
		if (id != null && version != null)
			return new PluginIdentifier(id, version);
		// check for the OSGi manifest
		File OSGiFile = new File(manifest.getParentFile(),
				"META-INF/MANIFEST.MF"); //$NON-NLS-1$

		if (OSGiFile.exists()) {
			try {
				Manifest OSGiManifest = new Manifest(new FileInputStream(
						OSGiFile));
				Dictionary headers = manifestToProperties(OSGiManifest
						.getMainAttributes());
				String value = headers.get(Constants.BUNDLE_SYMBOLICNAME)
						.toString();
				if (value == null)
					return null;
				ManifestElement[] elements = ManifestElement.parseHeader(
						Constants.BUNDLE_SYMBOLICNAME, value);
				if (elements.length > 0)
					id = elements[0].getValue();
				value = headers.get(Constants.BUNDLE_VERSION).toString();
				if (value == null)
					return null;
				elements = ManifestElement.parseHeader(
						Constants.BUNDLE_VERSION, value);
				if (elements.length > 0)
					version = elements[0].getValue();
				if (id != null && version != null)
					return new PluginIdentifier(id, version);
			} catch (Exception e1) {
				throwCoreException("Error extracting plug-in identifier.", e1);
			}
		}
		return null;
	}

	private String getAttribute(Node node, String name) {
		NamedNodeMap atts = node.getAttributes();
		if (atts != null) {
			Node att = atts.getNamedItem(name);
			if (att != null)
				return att.getNodeValue();
		}
		return null;
	}

	private Document readManifestFile() throws CoreException {
		if (manifest == null) {
			// System.out.println(
			// NLS.bind(PDEMessages.Builders_Convert_missingAttribute,
			// "manifest")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		InputStream stream = null;
		Document d = null;
		try {
			stream = new FileInputStream(manifest);
			InputStreamReader reader = new InputStreamReader(stream, "utf-8"); //$NON-NLS-1$
			InputSource inputSource = new InputSource(reader);
			inputSource.setSystemId(manifest.toString());

			if (parser == null)
				parser = documentBuilderFactory.newDocumentBuilder();
			d = parser.parse(inputSource);
		} catch (Exception e) {
			throwCoreException("Error parsing plugin.xml file.", e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
				stream = null;
			}
		}
		return d;
	}

	private Element[] getTocExtensions(Document doc) {
		ArrayList list = new ArrayList();
		Node root = doc.getDocumentElement();
		NodeList children = doc.getElementsByTagName("extension");
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			String point = getAttribute(child, "point");
			if (point.equals(POINT_TOC))
				list.add(child);
		}
		return (Element[]) list.toArray(new Element[list.size()]);
	}

	private Properties manifestToProperties(Attributes d) {
		Iterator iter = d.keySet().iterator();
		Properties result = new Properties();
		while (iter.hasNext()) {
			Attributes.Name key = (Attributes.Name) iter.next();
			result.put(key.toString(), d.get(key));
		}
		return result;
	}

	private void throwCoreException(String message, Throwable t)
			throws CoreException {
		IStatus status = new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID,
				IStatus.OK, message, t);
		throw new CoreException(status);
	}
}