/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.configurator.migration;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.eclipse.update.configurator.ConfigurationActivator;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PluginParser extends DefaultHandler {
	/**
	 * Manifest schema version from "eclipse" processing directive, or
	 * <code>null</code> if none.
	 * @since 3.0
	 */
	private String schemaVersion = null;
	private PluginInfo manifestInfo = new PluginInfo();
	private List exportStatement;

	// model parser
	private ServiceReference parserReference;

	public PluginParser() {
		super();
	}

	public PluginInfo parse(String fileLocation) throws IOException, SAXException {
		SAXParserFactory factory = acquireXMLParsing();
		if (factory == null)
			return null;
		try {
			factory.setNamespaceAware(true);
			factory.setFeature("http://xml.org/sax/features/string-interning", true); //$NON-NLS-1$
			factory.setValidating(false);
			factory.newSAXParser().parse(fileLocation, this);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			releaseXMLParsing();
		}
		return manifestInfo;
	}

	public void startElement(String uri, String elementName, String qName, Attributes attributes) throws SAXException {
		if (elementName.equals("plugin") || elementName.equals("fragment")) {
			manifestInfo.pluginId = attributes.getValue("", "id");
			manifestInfo.version = attributes.getValue("", "version");
			manifestInfo.pluginClass = attributes.getValue("", "class");
			manifestInfo.schemaVersion = schemaVersion;
			if (elementName.equals("fragment")) {
				manifestInfo.masterPluginId = attributes.getValue("", "plugin-id");
				manifestInfo.masterVersion = attributes.getValue("", "plugin-version");
			}
		} else if (elementName.equals("library")) {
			if (manifestInfo.libraries == null)
				manifestInfo.libraries = new HashMap();
			exportStatement = new ArrayList();
			manifestInfo.libraries.put(attributes.getValue("", "name"), exportStatement);
		} else if (elementName.equals("import")) {
			if (manifestInfo.requires == null) {
				manifestInfo.requires = new ArrayList();
				// to avoid cycles
				if (!manifestInfo.pluginId.equals("org.eclipse.core.runtime"))
					manifestInfo.requires.add("org.eclipse.core.runtime");
			}
			String plugin = attributes.getValue("", "plugin");
			if (plugin == null)
				return;

			if (plugin.equals("org.eclipse.core.boot") || plugin.equals("org.eclipse.core.runtime"))
				return;

			String version = attributes.getValue("", "version");
			String export = attributes.getValue("", "export");
			String optional = attributes.getValue("", "optional");
			String match = attributes.getValue("", "match");
			String modImport = plugin;
			if (version != null) {
				modImport += "; " + Constants.BUNDLE_VERSION_ATTRIBUTE + "=" + version;
			}
			if (export != null) {
				modImport += "; " + Constants.PROVIDE_PACKAGES_ATTRIBUTE + "=" + export;
			}
			if (optional != null) {
				modImport += ";" + Constants.OPTIONAL_ATTRIBUTE + "=" + "true";
			}
			if (match != null) {
				modImport += ";" + Constants.VERSION_MATCH_ATTRIBUTE + "=";
				if (match.equalsIgnoreCase("perfect")) {
					modImport += Constants.VERSION_MATCH_PERFECT;
				} else if (match.equalsIgnoreCase("equivalent")) {
					modImport += Constants.VERSION_MATCH_EQUIVALENT;
				} else if (match.equalsIgnoreCase("compatible")) {
					modImport += Constants.VERSION_MATCH_COMPATIBLE;
				} else if (match.equalsIgnoreCase("greaterOrEqual")) {
					modImport += Constants.VERSION_MATCH_GREATERTHANOREQUAL;
				}
			}
			manifestInfo.requires.add(modImport);
		} else if (elementName.equals("packages")) {
			//packages filtering is done globally
			if (manifestInfo.filters == null)
				manifestInfo.filters = new HashSet(4);
			String prefixString = attributes.getValue("", "prefixes");
			StringTokenizer tok = new StringTokenizer(prefixString, ",");
			while (tok.hasMoreTokens()) {
				manifestInfo.filters.add(tok.nextToken().trim());
			}
		} else if (elementName.equals("export")) {
			//Add the export to the last library encountered
			String exportFilter = (String) attributes.getValue("", "name");
			StringTokenizer tok = new StringTokenizer(exportFilter, ",");
			while (tok.hasMoreTokens()) {
				exportStatement.add(tok.nextToken().trim());
			}
		}
	}

	private SAXParserFactory acquireXMLParsing() {
		parserReference = ConfigurationActivator.getBundleContext().getServiceReference("javax.xml.parsers.SAXParserFactory");
		if (parserReference == null)
			return null;
		return (SAXParserFactory) ConfigurationActivator.getBundleContext().getService(parserReference);
	}

	private void releaseXMLParsing() {
		if (parserReference != null)
			ConfigurationActivator.getBundleContext().ungetService(parserReference);
	}

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < args.length; i++)
			System.out.println(new PluginParser().parse(args[i]));
	}

	public class PluginInfo implements IPluginInfo {
		private String schemaVersion;
		private String pluginId;
		private String version;
		// TODO libraries and requires should be ordered.
		private Map libraries; //represent the libraries and their export statement
		private ArrayList requires;
		private String pluginClass;
		private String masterPluginId;
		private String masterVersion;
		private Set filters;

		public boolean isFragment() {
			return masterPluginId != null;
		}
		public String toString() {
			return "plugin-id: " + pluginId + "  version: " + version + " libraries: " + libraries + " class:" + pluginClass + " master: " + masterPluginId + " master-version: " + masterVersion + " requires: " + requires;
		}
		public Map getLibraries() {
			if (libraries == null)
				return new HashMap(0);
			return libraries;
		}

		public String[] getRequires() {
			if (requires == null)
				return new String[] { "org.eclipse.core.runtime.compatibility" };

			if (schemaVersion == null) {
				//Add elements on the requirement list of ui and help. 
				for (int i = 0; i < requires.size(); i++) {
					if ("org.eclipse.ui".equals(requires.get(i))) { //$NON-NLS-1$
						requires.add(i + 1, "org.eclipse.ui.workbench.texteditor;" + Constants.OPTIONAL_ATTRIBUTE + "=" + "true");
						requires.add(i + 1, "org.eclipse.jface.text;" + Constants.OPTIONAL_ATTRIBUTE + "=" + "true");
						requires.add(i + 1, "org.eclipse.ui.editors;" + Constants.OPTIONAL_ATTRIBUTE + "=" + "true");
						requires.add(i + 1, "org.eclipse.ui.views;" + Constants.OPTIONAL_ATTRIBUTE + "=" + "true");
						requires.add(i + 1, "org.eclipse.ui.ide;" + Constants.OPTIONAL_ATTRIBUTE + "=" + "true");
					} else if ("org.eclipse.help".equals(requires.get(i))) { //$NON-NLS-1$
						requires.add(i + 1, "org.eclipse.help.base;" + Constants.OPTIONAL_ATTRIBUTE + "=" + "true");
					}
				}
			}
			requires.add("org.eclipse.core.runtime.compatibility");
			
			String[] requireBundles = new String[requires.size()];
			requires.toArray(requireBundles);
			return requireBundles;
		}

		public String getMasterId() {
			return masterPluginId;
		}
		public String getMasterVersion() {
			return masterVersion;
		}
		public String getPluginClass() {
			return pluginClass;
		}
		public String getUniqueId() {
			return pluginId;
		}
		public String getVersion() {
			return version;
		}
		public Set getPackageFilters() {
			return filters;
		}
		public String[] getLibrariesName() {
			if (libraries == null)
				return new String[0];
			Set names = libraries.keySet();
			return (String[]) names.toArray(new String[names.size()]);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#processingInstruction
	 * @since 3.0
	 */
	public void processingInstruction(String target, String data) throws SAXException {
		// Since 3.0, a processing instruction of the form <?eclipse version="3.0"?> at
		// the start of the manifest file is used to indicate the plug-in manifest
		// schema version in effect. Pre-3.0 (i.e., 2.1) plug-in manifest files do not
		// have one of these, and this is how we can distinguish the manifest of a
		// pre-3.0 plug-in from a post-3.0 one (for compatibility tranformations).
		if (target.equalsIgnoreCase("eclipse")) { //$NON-NLS-1$
			// just the presence of this processing instruction indicates that this
			// plug-in is at least 3.0
			schemaVersion = "3.0"; //$NON-NLS-1$
			StringTokenizer tokenizer = new StringTokenizer(data, "=\""); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.equalsIgnoreCase("version")) { //$NON-NLS-1$
					if (!tokenizer.hasMoreTokens()) {
						break;
					}
					schemaVersion = tokenizer.nextToken();
					break;
				}
			}
		}
	}
}
