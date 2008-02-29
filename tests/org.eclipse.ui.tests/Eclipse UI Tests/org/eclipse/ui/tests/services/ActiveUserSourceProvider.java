/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/**
 * A registered source provider that can update variables for the
 * IEvaluationService.
 * 
 * @since 3.4
 */
public class ActiveUserSourceProvider extends AbstractSourceProvider {
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] { "username" };

	private String username = "guest";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#getCurrentState()
	 */
	public Map getCurrentState() {
		Map map = new HashMap();
		map.put(PROVIDED_SOURCE_NAMES[0], username);
		return map;
	}

	public void setUsername(String name) {
		username = name;
		fireSourceChanged(ISources.ACTIVE_CONTEXT << 1,
				PROVIDED_SOURCE_NAMES[0], name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
	 */
	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}

}
