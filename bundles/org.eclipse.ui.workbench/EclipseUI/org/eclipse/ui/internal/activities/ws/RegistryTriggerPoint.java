/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.activities.Persistence;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.1
 */
public class RegistryTriggerPoint extends AbstractTriggerPoint {

	private String id;

	private IConfigurationElement element;

	private Map<String, String> hints;

	/**
	 * Create a new instance of this class.
	 *
	 * @param id      the id of the trigger point
	 * @param element the defining configuration element
	 */
	public RegistryTriggerPoint(String id, IConfigurationElement element) {
		this.id = id;
		this.element = element;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getStringHint(String key) {
		return getHints().get(key);
	}

	@Override
	public boolean getBooleanHint(String key) {
		return Boolean.parseBoolean(getStringHint(key));
	}

	/**
	 * Lazily create the hints.
	 *
	 * @return the hint map
	 */
	private Map<String, String> getHints() {
		if (hints == null) {
			hints = new HashMap<>();

			IConfigurationElement[] hintElements = element.getChildren(IWorkbenchRegistryConstants.TAG_HINT);
			for (IConfigurationElement hintElement : hintElements) {
				String id = hintElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
				String value = hintElement.getAttribute(IWorkbenchRegistryConstants.ATT_VALUE);

				if (id == null || value == null) {
					Persistence.log(element, Persistence.ACTIVITY_TRIGGER_HINT_DESC, "hint must contain ID and value"); //$NON-NLS-1$
					continue;
				}
				hints.put(id, value);
			}
		}

		return hints;
	}
}
