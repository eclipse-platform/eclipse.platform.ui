/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.session;

import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.xml.parsers.*;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class SetupManager {

	private static SetupManager instance;
	private String defaultVariations = "";
	private Map setups;

	public synchronized static SetupManager getInstance() {
		if (instance != null)
			return instance;
		instance = new SetupManager();
		return instance;
	}

	static String[] parseItems(String string) {
		if (string == null)
			return new String[0];
		StringTokenizer tokenizer = new StringTokenizer(string, ","); //$NON-NLS-1$
		if (!tokenizer.hasMoreTokens())
			return new String[0];
		String first = tokenizer.nextToken().trim();
		if (!tokenizer.hasMoreTokens())
			return new String[] {first};
		ArrayList items = new ArrayList();
		items.add(first);
		do {
			items.add(tokenizer.nextToken().trim());
		} while (tokenizer.hasMoreTokens());
		return (String[]) items.toArray(new String[items.size()]);
	}

	protected SetupManager() {
		setups = new HashMap();
		loadSetups();
	}

	private String getAttribute(NamedNodeMap attributes, String name) {
		Node selected = attributes.getNamedItem(name);
		return selected == null ? null : selected.getNodeValue();
	}

	/**
	 * Returns a brand new setup object configured according to the current
	 * default setup settings.
	 * 
	 * @return a new setup object
	 */
	public Setup getDefaultSetup() {
		String[] variationIds = getDefaultVariationIds();
		Setup defaultSetup = Setup.getDefaultSetup();
		for (int i = 0; i < variationIds.length; i++) {
			Setup variation = getSetup(variationIds[i]);
			if (variation != null)
				defaultSetup.merge(variation);
		}
		return defaultSetup;
	}

	private String[] getDefaultVariationIds() {
		return parseItems(System.getProperty("setup.variations", defaultVariations));
	}

	public Setup getSetup(String setupId) {
		Setup setup = (Setup) setups.get(setupId);
		return setup == null ? null : (Setup) setup.clone();
	}

	private void loadEclipseArgument(Setup newSetup, Element toParse) {
		newSetup.setEclipseArgument(toParse.getAttribute("option"), toParse.getAttribute("value"));
	}

	private void loadProperty(Setup newSetup, Element toParse) {
		newSetup.setSystemProperty(toParse.getAttribute("key"), toParse.getAttribute("value"));
	}

	private void loadSetup(Element markup) {
		NamedNodeMap attributes = markup.getAttributes();
		if (attributes == null)
			return;
		Setup newSetup = new Setup();
		newSetup.setId(getAttribute(attributes, "id"));
		newSetup.setName(getAttribute(attributes, "name"));
		String timeout = getAttribute(attributes, "timeout");
		if (timeout != null)
			newSetup.setTimeout(Integer.parseInt(timeout));
		NodeList children = markup.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node next = children.item(i);
			if (!(next instanceof Element))
				continue;
			Element toParse = (Element) next;
			if (toParse.getTagName().equals("eclipseArg"))
				loadEclipseArgument(newSetup, toParse);
			else if (toParse.getTagName().equals("vmArg"))
				loadVMArgument(newSetup, toParse);
			else if (toParse.getTagName().equals("property"))
				loadProperty(newSetup, toParse);
		}
		setups.put(newSetup.getId(), newSetup);
	}

	private void loadSetups() {
		try {
			File setupFile = new File(System.getProperty("setup.file", "default-setup.xml"));
			if (!setupFile.isFile())
				if (Platform.isRunning() && Platform.inDevelopmentMode()) {
					System.out.println("No setup descriptions found, only the default setup will be available");
					return;
				} else
					throw new IllegalArgumentException("Setup file '" + setupFile.getAbsolutePath() + "' not found. Ensure you are specifying the path for an existing setup file (e.g. -Dsetup.file=<path-to-setup-file>)");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(System.getProperty("setup.file")));
			Element root = doc.getDocumentElement();
			defaultVariations = root.getAttribute("default");
			NodeList variations = root.getChildNodes();
			for (int i = 0; i < variations.getLength(); i++) {
				Node next = variations.item(i);
				if (!(next instanceof Element))
					continue;
				Element toParse = (Element) next;
				if (!toParse.getTagName().equals("variant"))
					continue;
				loadSetup(toParse);
			}
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadVMArgument(Setup newSetup, Element toParse) {
		newSetup.setVMArgument(toParse.getAttribute("option"), toParse.getAttribute("value"));
	}

}