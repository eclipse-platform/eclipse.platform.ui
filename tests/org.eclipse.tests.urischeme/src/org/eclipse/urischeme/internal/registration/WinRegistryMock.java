/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     SAP SE - initial API and implementation
*******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WinRegistryMock implements IWinRegistry {

	List<Entry> setValues = new ArrayList<>();
	List<String> deletedKeys = new ArrayList<>();
	Map<String, String> valuesForKeys = new HashMap<>();
	WinRegistryException setValueForKeyException = null;
	WinRegistryException getValueForKeyException = null;
	WinRegistryException deleteKeyException = null;

	@Override
	public void setValueForKey(String key, String attribute, String value) throws WinRegistryException {
		if (setValueForKeyException != null) {
			throw setValueForKeyException;
		}
		setValues.add(new Entry(key, attribute, value));
	}

	@Override
	public String getValueForKey(String key, String attribute) throws WinRegistryException {
		if (getValueForKeyException != null) {
			throw getValueForKeyException;
		}
		return valuesForKeys.get(key + "-" + attribute);
	}

	@Override
	public void deleteKey(String key) throws WinRegistryException {
		if (deleteKeyException != null) {
			throw deleteKeyException;
		}
		deletedKeys.add(key);
	}

	public static class Entry {
		public String key;
		public String attribute;
		public String value;

		public Entry(String key, String attribute, String value) {
			super();
			this.key = key;
			this.attribute = attribute;
			this.value = value;
		}
	}
}