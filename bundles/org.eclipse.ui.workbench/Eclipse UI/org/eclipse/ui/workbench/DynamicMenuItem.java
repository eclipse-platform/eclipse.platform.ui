/*******************************************************************************
 * Copyright (c) 2023 ETAS GmbH and others, all rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     ETAS GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.workbench;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class DynamicMenuItem {

	private static final String PLUGIN_ID = "org.eclipse.ui.workbench"; //$NON-NLS-1$
	private static final String DYNAMIC_MENU_ITEMS = "dynamicMenuItems"; //$NON-NLS-1$

	public List<String> getDynamicMenuItems() {
		IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		String jsn = pref.get(DYNAMIC_MENU_ITEMS, null);
		if (jsn != null) {
			Gson gsn = new Gson();
			Type typ = new TypeToken<List<String>>() {
			}.getType();
			return gsn.fromJson(jsn, typ);
		}
		return new ArrayList<>();
	}

	public void setDynamicMenuItems(Set<String> set) {
		IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		Gson gsn = new Gson();
		String jsn = gsn.toJson(set);
		pref.put(DYNAMIC_MENU_ITEMS, jsn);
		try {
			pref.flush();
		} catch (org.osgi.service.prefs.BackingStoreException e) {
			e.printStackTrace();
		}
	}

}
