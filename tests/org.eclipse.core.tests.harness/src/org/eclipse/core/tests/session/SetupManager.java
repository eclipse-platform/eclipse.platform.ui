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
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.*;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class SetupManager {

	private static SetupManager instance;
	private Map setups;

	public synchronized static SetupManager getInstance() {
		if (instance != null)
			return instance;
		instance = new SetupManager();
		return instance;
	}

	protected SetupManager() {
		setups = new HashMap();
		loadSetups();
	}

	public Setup getSetup(String setupId) {
		Setup setup = (Setup) setups.get(setupId);
		return setup == null ? null : (Setup) setup.clone();
	}

	private void loadSetup(Node markup) {
		NamedNodeMap attributes = markup.getAttributes();
		if (attributes == null)
			return;;
		Setup newSetup = new Setup();
		newSetup.setId(getAttribute(attributes, "id"));
		newSetup.setName(getAttribute(attributes, "name"));
		newSetup.setAllArgs(getAttribute(attributes, "allArgs"));
		newSetup.setApplicationArgs(getAttribute(attributes, "applicationArgs"));
		newSetup.setVMArgs(getAttribute(attributes, "vmArgs"));
		newSetup.setVMLocation(getAttribute(attributes, "vmLocation"));
		newSetup.setInstallLocation(getAttribute(attributes, "installLocation"));
		newSetup.setInstanceLocation(getAttribute(attributes, "instanceLocation"));
		String timeout = getAttribute(attributes, "timeout");
		if (timeout != null)
			newSetup.setTimeout(Integer.parseInt(timeout));
		String runs = getAttribute(attributes, "runs");
		if (runs != null)
			newSetup.setNumberOfRuns(Integer.parseInt(runs));
		setups.put(newSetup.getId(), newSetup);
	}

	private String getAttribute(NamedNodeMap attributes, String name) {
		Node selected = attributes.getNamedItem(name);
		return selected == null ? null : selected.getNodeValue();
	}

	private void loadSetups() {
		try {
			File setupFile = new File(System.getProperty("setup", "default-setup.xml"));
			if (!setupFile.isFile())
				if (Platform.isRunning() && Platform.inDevelopmentMode()) {
					System.out.println("No setup descriptions found, only the default setup will be available");
					return;
				} else
					throw new IllegalArgumentException("Setup file '" + setupFile.getAbsolutePath() + "' not found. Ensure you are specifying the path for an existing setup file (e.g. -Dsetup=<path-to-setup-file>)");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(System.getProperty("setup")));
			NodeList setups = doc.getDocumentElement().getChildNodes();
			for (int i = 0; i < setups.getLength(); i++)
				loadSetup(setups.item(i));
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

	public Setup getDefaultSetup() {
		String defaultSetupId = System.getProperty("setup");
		Setup defaultSetup = getSetup(defaultSetupId);
		if (defaultSetup != null)
			return defaultSetup;
		return Setup.getDefaultSetup();
	}

}