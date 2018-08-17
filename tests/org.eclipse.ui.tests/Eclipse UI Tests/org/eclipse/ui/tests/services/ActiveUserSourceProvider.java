/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public void dispose() {
	}

	@Override
	public Map getCurrentState() {
		Map<String, String> map = new HashMap<>();
		map.put(PROVIDED_SOURCE_NAMES[0], username);
		return map;
	}

	public void setUsername(String name) {
		username = name;
		fireSourceChanged(ISources.ACTIVE_CONTEXT << 1,
				PROVIDED_SOURCE_NAMES[0], name);
	}

	@Override
	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}

}
