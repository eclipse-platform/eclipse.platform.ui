/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
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
package org.eclipse.ui.internal.preferences;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @since 3.1
 */
public class PropertyMapUnion implements IPropertyMap {

	private Map<String, PropertyInfo> values;

	private static final class PropertyInfo {
		Object value;
		boolean commonAttribute;

		PropertyInfo(Object value, boolean commonAttribute) {
			this.value = value;
			this.commonAttribute = commonAttribute;
		}
	}

	@Override
	public Set<String> keySet() {
		return values.keySet();
	}

	@Override
	public Object getValue(String propertyId, Class propertyType) {
		PropertyInfo info = values.get(propertyId);

		if (info == null) {
			return null;
		}

		Object value = info.value;

		if (propertyType.isInstance(value)) {
			return value;
		}

		return null;
	}

	@Override
	public boolean isCommonProperty(String propertyId) {
		PropertyInfo info = values.get(propertyId);

		if (info == null) {
			return false;
		}

		return info.commonAttribute;
	}

	@Override
	public boolean propertyExists(String propertyId) {
		return values.get(propertyId) != null;
	}

	@Override
	public void setValue(String propertyId, Object newValue) {
		PropertyInfo info = new PropertyInfo(newValue, true);

		values.put(propertyId, info);
	}

	public void addMap(IPropertyMap toAdd) {
		Set<String> keySet = toAdd.keySet();

		// Update any existing attributes
		for (String key : keySet()) {
			PropertyInfo localInfo = values.get(key);
			// Shouldn't be null, but just in case...
			if (localInfo != null) {
				// If the attribute exists in the new map
				if (toAdd.propertyExists(key)) {
					// Determine if the value is common
					Object value = toAdd.getValue(key, Object.class);

					if (!Objects.equals(value, toAdd.getValue(key, Object.class))) {
						// Set the value to null if not common
						localInfo.value = null;
					}

					// The attribute must be common in both the receiver and the new map to be
					// common
					// everywhere
					localInfo.commonAttribute = localInfo.commonAttribute && toAdd.isCommonProperty(key);
				} else {
					// If the attribute doesn't exist in the new map, it cannot be common
					localInfo.commonAttribute = false;
				}
			}
		}

		// Add any new attributes that exist in the target
		for (String element : keySet) {
			PropertyInfo localInfo = values.get(element);
			if (localInfo == null) {
				Object value = toAdd.getValue(element, Object.class);

				boolean isCommon = toAdd.isCommonProperty(element);

				localInfo = new PropertyInfo(value, isCommon);
				values.put(element, localInfo);
			}
		}
	}

	public void removeValue(String propertyId) {
		values.remove(propertyId);
	}
}
