/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.search.internal.core.text;

import java.util.ArrayList;
import java.util.Objects;

import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.internal.core.SearchCoreMessages;
import org.eclipse.search.internal.core.SearchCorePlugin;


public class TextSearchEngineRegistry {
	private static final String DEFAULT_PREFERENCE_NODE_ID = "org.eclipse.search"; //$NON-NLS-1$
	public static final String PREFERENCE_ENGINE_KEY = "org.eclipse.search.textSearchEngine"; //$NON-NLS-1$

	private static final String EXTENSION_POINT_ID= "org.eclipse.search.textSearchEngine"; //$NON-NLS-1$
	private static final String ENGINE_NODE_NAME= "textSearchEngine"; //$NON-NLS-1$
	private static final String ATTRIB_ID= "id"; //$NON-NLS-1$
	private static final String ATTRIB_LABEL= "label"; //$NON-NLS-1$
	private static final String ATTRIB_CLASS= "class"; //$NON-NLS-1$

	private TextSearchEngine fPreferredEngine;
	private String fPreferredEngineId;
	private String fgPreferenceNodeId;

	public TextSearchEngineRegistry() {
		fPreferredEngineId= null; // only null when not initialized
		fPreferredEngine= null;
	}

	public void setPreferenceNodeId(String id) {
		fgPreferenceNodeId = id;
	}

	protected String getPreferenceNodeId() {
		return fgPreferenceNodeId == null ? DEFAULT_PREFERENCE_NODE_ID : fgPreferenceNodeId; // $NON-NLS-1$
	}

	public TextSearchEngine getPreferred() {
		String preferredId= getPreferredEngineID();
		if (!Objects.equals(preferredId, fPreferredEngineId) || fPreferredEngine == null) {
			updateEngine(preferredId);
		}
		return fPreferredEngine;
	}

	private void updateEngine(String preferredId) {
		if (preferredId != null && !preferredId.isEmpty()) {
			TextSearchEngine engine= createFromExtension(preferredId);
			if (engine != null) {
				fPreferredEngineId= preferredId;
				fPreferredEngine= engine;
				return;
			}
			// creation failed, clear preference
			setPreferredEngineID(""); // set to default //$NON-NLS-1$
		}
		// empty string: default engine
		fPreferredEngineId= ""; //$NON-NLS-1$
		fPreferredEngine= TextSearchEngine.createDefault();
	}

	private String getPreferredEngineID() {
		String nodeId = getPreferenceNodeId();
		String preferedEngine = Platform.getPreferencesService().get(PREFERENCE_ENGINE_KEY, null,
				new IEclipsePreferences[] { InstanceScope.INSTANCE.getNode(nodeId) });
		return preferedEngine;
	}

	private void setPreferredEngineID(String id) {
		String pluginId = getPreferenceNodeId();
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(pluginId);
		preferences.put(PREFERENCE_ENGINE_KEY, id);
		try {
			// forces the application to save the preferences
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	private TextSearchEngine createFromExtension(final String id) {
		final TextSearchEngine[] res= new TextSearchEngine[] { null };

		ISafeRunnable safe = new ISafeRunnable() {
			@Override
			public void run() throws Exception {
				IConfigurationElement[] extensions= Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
				for (IConfigurationElement curr : extensions) {
					if (ENGINE_NODE_NAME.equals(curr.getName()) && id.equals(curr.getAttribute(ATTRIB_ID))) {
						res[0]= (TextSearchEngine) curr.createExecutableExtension(ATTRIB_CLASS);
						return;
					}
				}
			}
			@Override
			public void handleException(Throwable e) {
				SearchCorePlugin.log(e);
			}
		};
		try {
			safe.run();
		} catch (Exception | LinkageError e) {
			safe.handleException(e);
		}
		return res[0];
	}

	public String[][] getAvailableEngines() {
		ArrayList<String[]> res= new ArrayList<>();
		res.add(new String[] { SearchCoreMessages.TextSearchEngineRegistry_defaulttextsearch_label, "" }); //$NON-NLS-1$

		IConfigurationElement[] extensions= Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
		for (IConfigurationElement engine : extensions) {
			if (ENGINE_NODE_NAME.equals(engine.getName())) {
				res.add(new String[] { engine.getAttribute(ATTRIB_LABEL), engine.getAttribute(ATTRIB_ID) });
			}
		}
		return res.toArray(new String[res.size()][]);
	}
}
