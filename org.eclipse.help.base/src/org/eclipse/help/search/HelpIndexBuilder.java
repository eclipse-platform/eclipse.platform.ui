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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.HelpBaseResources;
import org.eclipse.help.internal.search.AnalyzerDescriptor;
import org.eclipse.help.internal.search.PluginVersionInfo;
import org.eclipse.help.internal.search.SearchIndex;
import org.eclipse.help.internal.toc.TocFile;
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
 * <p>
 * If the index is created for a fragment, the manifest must point at the
 * referenced fragment plug-in, while the target should be the fragment itself.
 * <p>
 * Starting from the provided target directory, index for each locale will be
 * placed in a directory with the following path:
 * 
 * <pre>
 * 
 *  
 *   
 *    
 *         target/nl/country/
 *         
 *         or
 *         
 *         target/nl/country/variant/
 *     
 *    
 *   
 *  
 * </pre>
 * 
 * <p>
 * The relative directory specified in the <code>index</code> element of the
 * <code>org.eclipse.help.toc</code> extention will be created in each of the
 * locale-specific paths (one per locale).
 * <p>
 * An instance of <code>HelpIndexBuilder</code> can be cached and used
 * multiple times for different manifest and target values.
 * 
 * @since 3.1
 */

public class HelpIndexBuilder {
	private static final String POINT_TOC = "org.eclipse.help.toc"; //$NON-NLS-1$

	private static final String EL_TOC = "toc"; //$NON-NLS-1$

	private static final String EL_INDEX = "index"; //$NON-NLS-1$

	private File manifest;

	private String indexPath;

	private File target;

	private Hashtable localeDirs = new Hashtable();

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

	class LocaleDir {
		File directory;

		String locale;

		ArrayList tocFiles = new ArrayList();

		public LocaleDir(File directory) {
			this.directory = directory;
		}

		public void addTocFile(TocFile tocFile) {
			tocFiles.add(tocFile);
		}
	}

	class IndexerPluginVersionInfo extends PluginVersionInfo {
		private static final long serialVersionUID = 1L;

		public IndexerPluginVersionInfo(PluginIdentifier id,
				PluginIdentifier fid, File dir) {
			super(SearchIndex.INDEXED_CONTRIBUTION_INFO_FILE, null, dir, false);
			createInfo(id, fid);
		}

		protected void createTable(Collection docBundleIds) {
			// do nothing
		}

		protected void createInfo(PluginIdentifier id, PluginIdentifier fid) {
			// We will ignore docBundleIds which is null anyway,
			// and use id and fid to create plugin info
			// for the target
			StringBuffer pluginVersionAndFragments = new StringBuffer();
			pluginVersionAndFragments.append(id.id);
			pluginVersionAndFragments.append(SEPARATOR);
			pluginVersionAndFragments.append(id.version.toString());
			if (fid != null) {
				pluginVersionAndFragments.append(SEPARATOR);
				pluginVersionAndFragments.append(fid.id);
				pluginVersionAndFragments.append(SEPARATOR);
				pluginVersionAndFragments.append(fid.version.toString());
			}
			this.put(id.id, pluginVersionAndFragments.toString());
		}
	}

	class IndexerTocFile extends TocFile {
		public IndexerTocFile(String plugin, String href, boolean primary,
				String locale, String extraDir) {
			super(plugin, href, primary, locale, extraDir);
		}

		public String getHref() {
			return super.getHref();
		}
	}

	// --------------------------------------------------------------------

	/**
	 * Creates a new instance of the help index builder.
	 */
	public HelpIndexBuilder() {
	}

	/**
	 * Returns the manifest file for this builder. If the target is a plug-in,
	 * it is the absolute path of <code>plugin.xml</code> file. If the target
	 * is a fragment, it is the absolute path of <code>plugin.xml</code> of
	 * the fragment plug-in.
	 * 
	 * @return the file that contains TOC extensions
	 */
	public File getManifest() {
		return manifest;
	}

	/**
	 * Sets the new manifest file (plugin.xml) to be used by this builder. If
	 * the target is a plug-in, it is the absolute path of
	 * <code>plugin.xml</code> file. If the target is a fragment, it is the
	 * absolute path of <code>plugin.xml</code> of the fragment plug-in.
	 * 
	 * @param manifest
	 *            the file that contains TOC extensions
	 */
	public void setManifest(File manifest) {
		this.manifest = manifest;
	}

	/**
	 * Returns the target directory where index should be created.
	 * 
	 * @return the target index directory
	 */
	public File getTarget() {
		return target;
	}

	/**
	 * Sets the target directory where index should be created. Locale-specific
	 * directories will be created starting from this directory.
	 * 
	 * @param target
	 *            the directory where index should be created
	 */
	public void setTarget(File target) {
		this.target = target;
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
		reset();
		if (manifest == null || target == null)
			return;
		Document doc = readXMLFile(manifest);
		if (doc == null)
			return;

		PluginIdentifier pluginID = getPluginID(doc);

		Element[] extensions = getTocExtensions(doc);
		for (int i = 0; i < extensions.length; i++) {
			processExtension(pluginID, extensions[i]);
		}
		if (indexPath == null) {
			throwCoreException("Index target path not specified.", null);
		}
		doc = null; // discard the DOM
		PluginIdentifier fragmentID = null;

		if (!manifest.getParentFile().equals(target)) {
			// target is a fragment, source is a plug-in
			File fragmentFile = new File(target, "fragment.xml");
			if (fragmentFile.exists())
				doc = readXMLFile(fragmentFile);
			fragmentID = getPluginID(doc);
		}
		monitor.beginTask("Building index: ", localeDirs.size());
		MultiStatus multiStatus = null;
		for (Enumeration enm = localeDirs.elements(); enm.hasMoreElements();) {
			// process locale dir
			LocaleDir localeDir = (LocaleDir) enm.nextElement();
			MultiStatus localeStatus = processLocaleDir(pluginID, fragmentID,
					localeDir, new SubProgressMonitor(monitor, 1));
			if (localeStatus != null) {
				if (multiStatus == null)
					multiStatus = localeStatus;
				else
					multiStatus.addAll(localeStatus);
			}
		}
		if (multiStatus != null)
			throw new CoreException(multiStatus);
	}

	private void processExtension(PluginIdentifier id, Element extensionNode) {
		NodeList children = extensionNode.getElementsByTagName(EL_TOC);
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			String file = getAttribute(node, "file");
			String primary = getAttribute(node, "primary");
			String extradir = getAttribute(node, "extradir");
			addTocFile(id, file, primary, extradir);
		}
		children = extensionNode.getElementsByTagName(EL_INDEX);
		if (children.getLength() == 1) {
			Node node = children.item(0);
			indexPath = getAttribute(node, "path");
		}
	}

	private MultiStatus processLocaleDir(PluginIdentifier id,
			PluginIdentifier fid, LocaleDir localeDir, IProgressMonitor monitor)
			throws CoreException {
		// build an index for each locale directory
		File directory = localeDir.directory;
		File indexDirectory = new File(directory, indexPath);
		prepareDirectory(indexDirectory);
		SearchIndex index = new SearchIndex(indexDirectory, localeDir.locale,
				new AnalyzerDescriptor(localeDir.locale), null);
		IndexerPluginVersionInfo docPlugins = new IndexerPluginVersionInfo(id,
				fid, indexDirectory);
		index.setDocPlugins(docPlugins);
		monitor.beginTask("", 5);
		Collection docs = collectDocs(localeDir, new SubProgressMonitor(
				monitor, 1));
		return createIndex(id.id, directory, index, docs,
				new SubProgressMonitor(monitor, 4));
	}

	private Collection collectDocs(LocaleDir localeDir, IProgressMonitor monitor)
			throws CoreException {
		HashSet docs = new HashSet();
		monitor.beginTask("", localeDir.tocFiles.size());
		for (int i = 0; i < localeDir.tocFiles.size(); i++) {
			IndexerTocFile tocFile = (IndexerTocFile) localeDir.tocFiles.get(i);
			monitor.subTask(tocFile.getHref());
			collectDocs(docs, new File(localeDir.directory, tocFile.getHref()),
					localeDir.locale);
			monitor.worked(1);
		}
		return docs;
	}

	private void collectDocs(Set docs, File tocFile, String locale)
			throws CoreException {
		if (!tocFile.exists())
			return;
		Document doc = readXMLFile(tocFile);
		add(doc.getDocumentElement(), docs, locale);
	}

	private void add(Element topic, Set hrefs, String locale) {
		String href = getAttribute(topic, "href");
		if (href != null
				&& !href.equals("") && !href.startsWith("http://") && !href.startsWith("https://")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			href = SearchIndex.getIndexableHref(href);
			if (href != null)
				hrefs.add(href);
		}
		NodeList subtopics = topic.getElementsByTagName("topic");
		for (int i = 0; i < subtopics.getLength(); i++)
			add((Element) subtopics.item(i), hrefs, locale);
	}

	private URL getIndexableURL(File directory, String href) {
		if (href == null)
			return null;
		try {
			return new URL(directory.toURL(), href);
		} catch (MalformedURLException mue) {
			System.out.println(mue);
			return null;
		}
	}

	private MultiStatus createIndex(String pluginId, File directory,
			SearchIndex index, Collection addedDocs, IProgressMonitor monitor)
			throws CoreException {
		if (!index.beginAddBatch()) {
			throwCoreException("Error while creating the index", null);
		}
		checkCancelled(monitor);
		MultiStatus multiStatus = null;
		monitor.beginTask("", addedDocs.size());
		monitor.subTask(HelpBaseResources.UpdatingIndex);

		for (Iterator it = addedDocs.iterator(); it.hasNext();) {
			String href = (String) it.next();
			URL url = getIndexableURL(directory, href);
			if (url != null) {
				IStatus status = index
						.addDocument(getName(pluginId, href), url);
				if (status.getCode() != IStatus.OK) {
					if (multiStatus == null) {
						multiStatus = new MultiStatus(
								HelpBasePlugin.PLUGIN_ID,
								IStatus.ERROR,
								"Help documentation could not be indexed completely.",
								null);
					}
					multiStatus.add(status);
				}
			}
			checkCancelled(monitor);
			monitor.worked(1);
		}
		monitor.subTask(HelpBaseResources.Writing_index);
		if (!index.endAddBatch())
			throwCoreException("Error while writing the index", null);
		return multiStatus;
	}

	private void checkCancelled(IProgressMonitor pm)
			throws OperationCanceledException {
		if (pm.isCanceled())
			throw new OperationCanceledException();
	}

	private String getName(String pluginId, String href) {
		// remove query string if any
		int i = href.indexOf('?');
		if (i != -1)
			href = href.substring(0, i);
		return "/" + pluginId + "/" + href;
	}

	/**
	 * If the path does not exist, create it. Otherwise, delete all the files in
	 * it.
	 * 
	 * @param indexDirectory
	 */

	private void prepareDirectory(File indexDirectory) throws CoreException {
		if (indexDirectory.exists()) {
			File[] files = indexDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				boolean result = file.delete();
				if (!result)
					throwCoreException("Cannot scrub target index directory",
							null);
			}
		} else {
			boolean result = indexDirectory.mkdirs();
			if (!result)
				throwCoreException("Cannot create target index directory", null);
		}
	}

	private void addTocFile(PluginIdentifier id, String file, String primary,
			String extradir) {
		boolean isPrimary = primary != null && primary.equalsIgnoreCase("true");

		File newLocaleDirs[] = computeLocaleDirectories(file, target, 0);
		for (int i = 0; i < newLocaleDirs.length; i++) {
			File directory = newLocaleDirs[i];
			LocaleDir localeDir = (LocaleDir) localeDirs.get(directory);
			if (localeDir == null) {
				localeDir = new LocaleDir(directory);
				localeDir.locale = computeLocale(id, directory);
				localeDirs.put(directory, localeDir);
			}
			localeDir.addTocFile(new IndexerTocFile(id.id, file, isPrimary,
					localeDir.locale, extradir));
		}
	}

	private String computeLocale(PluginIdentifier id, File localeDir) {
		String idVersion = id.id + "_" + id.version.toString();
		// check for default id
		if (localeDir.getName().equals(id.id)
				|| localeDir.getName().equals(idVersion))
			return Platform.getNL();
		String country = null;
		String variant = null;
		variant = localeDir.getName();
		File parent = localeDir.getParentFile();
		if (parent.getName().equals("nl")) {
			// shift
			country = variant;
			variant = null;
		} else {
			country = parent.getName();
		}
		if (variant == null)
			return country;
		else
			return country + "_" + variant;
	}

	private File[] computeLocaleDirectories(String file, File parent, int depth) {
		File[] files = parent.listFiles();
		HashSet set = new HashSet();
		if (files == null)
			return new File[0];
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(file)) {
				// match
				set.add(parent);
			} else if (depth == 0 && files[i].isDirectory()
					&& files[i].getName().equals("nl")) {
				File[] local = computeLocaleDirectories(file, files[i],
						depth + 1);
				for (int j = 0; j < local.length; j++) {
					set.add(local[j]);
				}
			} else if (depth < 3 && files[i].isDirectory()) {
				File[] local = computeLocaleDirectories(file, files[i],
						depth + 1);
				for (int j = 0; j < local.length; j++) {
					set.add(local[j]);
				}
			}
		}
		return (File[]) set.toArray(new File[set.size()]);
	}

	private void reset() {
		localeDirs.clear();
		indexPath = null;
	}

	private PluginIdentifier getPluginID(Document doc) throws CoreException {
		String id = null;
		String version = null;
		if (doc != null) {
			Node root = doc.getDocumentElement();
			id = getAttribute(root, "id");
			version = getAttribute(root, "version");
			if (id != null && version != null)
				return new PluginIdentifier(id, version);
		}
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

	private Document readXMLFile(File file) throws CoreException {
		InputStream stream = null;
		Document d = null;
		try {
			stream = new FileInputStream(file);
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