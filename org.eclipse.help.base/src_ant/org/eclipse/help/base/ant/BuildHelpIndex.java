/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.base.ant;

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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class BuildHelpIndex extends Task {
	private static final String POINT_TOC = "org.eclipse.help.toc";
	private String manifest;
	private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
	.newInstance();	
	private DocumentBuilder parser;
	
	class PluginIdentifier {
		String id;
		PluginVersionIdentifier version;
		public PluginIdentifier(String id, String version) {
			this.id = id;
			this.version = new PluginVersionIdentifier(version);
		}
	}
	
	public void execute() throws BuildException {
		IProgressMonitor monitor = 
			(IProgressMonitor) getProject().getReferences().get(AntCorePlugin.ECLIPSE_PROGRESS_MONITOR);

		Document doc = readManifestFile();
		if (doc == null)
			return;
		
		PluginIdentifier pluginID = getPluginID(doc);

		Node[] extensions = getTocExtensions(doc);
		for (int i = 0; i < extensions.length; i++) {
			Node extensionNode = extensions[i];
			String file = getAttribute(extensionNode, "file");
			String primary = getAttribute(extensionNode, "primary");
			String extradir = getAttribute(extensionNode, "extradir");
			addTocFile(pluginID, file, primary, extradir);
		}
		doc = null; // discard the DOM
	}

	private void addTocFile(PluginIdentifier pluginID, String file, String primary, String extradir) {
		System.out.println("Adding toc: file="+file+", primary="+primary+", extradir="+extradir);
	}

	private PluginIdentifier getPluginID(Document doc) {
		Node root = doc.getDocumentElement();
		String id = getAttribute(root, "id");
		String version = getAttribute(root, "version");
		if (id!=null && version!=null)
			return new PluginIdentifier(id, version);
		// check for the OSGi manifest
		File file =
			new Path(manifest).isAbsolute()
				? new File(manifest)
				: new File(getProject().getBaseDir(), manifest);
		File OSGiFile = new File(file.getParentFile(), "META-INF/MANIFEST.MF"); //$NON-NLS-1$

		if (OSGiFile.exists()) {
			try {
				Manifest OSGiManifest = new Manifest(new FileInputStream(OSGiFile));
				Dictionary headers = manifestToProperties(OSGiManifest.getMainAttributes());
				String value = headers.get(Constants.BUNDLE_SYMBOLICNAME).toString();
				if (value == null)
					return null;
				ManifestElement[] elements = ManifestElement.parseHeader(Constants.BUNDLE_SYMBOLICNAME, value);
				if (elements.length > 0)
					id= elements[0].getValue();
				value = headers.get(Constants.BUNDLE_VERSION).toString();
				if (value == null)
					return null;
				elements = ManifestElement.parseHeader(Constants.BUNDLE_VERSION, value);
				if (elements.length >0)
					version = elements[0].getValue();
				if (id!=null && version!=null)
					return new PluginIdentifier(id, version);
			} catch (Exception e1) {
				System.out.print(e1.getMessage());
			}
		}
		return null;
	}
	
	private String getAttribute(Node node, String name) {
		NamedNodeMap atts = node.getAttributes();
		if (atts!=null) {
			Node att = atts.getNamedItem(name);
			if (att!=null)
				return att.getNodeValue();
		}
		return null;
	}

	public void setManifest(String manifest) {
		this.manifest = manifest;
	}

	private Document readManifestFile() {
		if (manifest == null) {
			//System.out.println(
				//NLS.bind(PDEMessages.Builders_Convert_missingAttribute, "manifest")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		
		File file =
			new Path(manifest).isAbsolute()
				? new File(manifest)
				: new File(getProject().getBaseDir(), manifest);
		InputStream stream = null;
		Document d = null;
		try {
			stream = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(stream, "utf-8"); //$NON-NLS-1$

			InputSource inputSource = new InputSource(reader);
			inputSource.setSystemId(file.toString());

			if (parser==null)
				parser = documentBuilderFactory
					.newDocumentBuilder();
			d = parser.parse(inputSource);
			stream.close();
		} catch (Exception e) {
			if (e.getMessage() != null)
				System.out.println(e.getMessage());
			return null;
		}
		finally {
			if (stream!=null) {
				try {
					stream.close();
				}
				catch (IOException e) {
				}
				stream = null;
			}
		}
		return d;
	}
	
	private Node [] getTocExtensions(Document doc) {
		ArrayList list = new ArrayList();
		Node root = doc.getDocumentElement();
		NodeList children = doc.getElementsByTagName("extension");
		for (int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			String point = getAttribute(child, "point");
			if (point.equals(POINT_TOC))
				list.add(child);
		}
		return (Node[])list.toArray(new Node[list.size()]);
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
}
