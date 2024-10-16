/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * A registry between a given string id and a configuration element that
 * corresponds to a control contribution.
 */
public class ControlContributionRegistry {

	private static Map<String, IConfigurationElement> registry = new HashMap<>();

	public static void clear() {
		registry.clear();
	}

	public static void add(String id, IConfigurationElement element) {
		registry.put(id, element);
	}

	public static IConfigurationElement get(String id) {
		return registry.get(id);
	}

}
