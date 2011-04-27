/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.SafeRunnable;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchPreferencePage;
import org.eclipse.search.ui.text.TextSearchQueryProvider;

import org.eclipse.search2.internal.ui.SearchMessages;

public class TextSearchQueryProviderRegistry {

	private static final String EXTENSION_POINT_ID= "org.eclipse.search.textSearchQueryProvider"; //$NON-NLS-1$
	private static final String PROVIDER_NODE_NAME= "textSearchQueryProvider"; //$NON-NLS-1$
	private static final String ATTRIB_ID= "id"; //$NON-NLS-1$
	private static final String ATTRIB_LABEL= "label"; //$NON-NLS-1$
	private static final String ATTRIB_CLASS= "class"; //$NON-NLS-1$

	private TextSearchQueryProvider fPreferredProvider;
	private String fPreferredProviderId;

	public TextSearchQueryProviderRegistry() {
		fPreferredProviderId= null; // only null when not initialized
		fPreferredProvider= null;
	}

	public TextSearchQueryProvider getPreferred() {
		String preferredId= getPreferredEngineID();
		if (!preferredId.equals(fPreferredProviderId)) {
			updateProvider(preferredId);
		}
		return fPreferredProvider;
	}

	private void updateProvider(String preferredId) {
		fPreferredProviderId= preferredId;
		fPreferredProvider= null;
		if (preferredId.length() != 0) { // empty string: default engine
			fPreferredProvider= createFromExtension(preferredId);
		}
		if (fPreferredProvider == null) {
			fPreferredProvider= new DefaultTextSearchQueryProvider();
		}
	}

	private String getPreferredEngineID() {
		IPreferenceStore prefs= SearchPlugin.getDefault().getPreferenceStore();
		String preferedEngine= prefs.getString(SearchPreferencePage.TEXT_SEARCH_QUERY_PROVIDER);
		return preferedEngine;
	}

	private TextSearchQueryProvider createFromExtension(final String id) {
		final TextSearchQueryProvider[] res= new TextSearchQueryProvider[] { null };

		SafeRunnable safe= new SafeRunnable() {
			public void run() throws Exception {
				IConfigurationElement[] extensions= Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
				for (int i= 0; i < extensions.length; i++) {
					IConfigurationElement curr= extensions[i];
					if (PROVIDER_NODE_NAME.equals(curr.getName()) && id.equals(curr.getAttribute(ATTRIB_ID))) {
						res[0]= (TextSearchQueryProvider) curr.createExecutableExtension(ATTRIB_CLASS);
						return;
					}
				}
			}
			public void handleException(Throwable e) {
				SearchPlugin.log(e);
			}
		};
		SafeRunnable.run(safe);
		return res[0];
	}

	public String[][] getAvailableProviders() {
		ArrayList res= new ArrayList();
		res.add(new String[] { SearchMessages.TextSearchQueryProviderRegistry_defaultProviderLabel, "" }); //$NON-NLS-1$

		IConfigurationElement[] extensions= Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
		for (int i= 0; i < extensions.length; i++) {
			IConfigurationElement engine= extensions[i];
			if (PROVIDER_NODE_NAME.equals(engine.getName())) {
				res.add(new String[] { engine.getAttribute(ATTRIB_LABEL), engine.getAttribute(ATTRIB_ID) });
			}
		}
		return (String[][]) res.toArray(new String[res.size()][]);
	}
}
