/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.io.*;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.HelpUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * Manages the scope for the federated search.
 */
public class ScopeSetManager {
	private ScopeSet activeSet;
	private static final String ACTIVE_SET = "activeScopeSet"; //$NON-NLS-1$

	private ArrayList sets;

	public ScopeSetManager() {
		ensureLocation();
		loadScopeSets();
	}

	public void add(ScopeSet set) {
		sets.add(set);
	}

	public void remove(ScopeSet set) {
		sets.remove(set);
		set.dispose();
	}

	public void setActiveSet(ScopeSet set) {
		if (this.activeSet!=null) {
			this.activeSet.save();
		}
		this.activeSet = set;
	}
	
	public static void ensureLocation() {
		IPath location = HelpUIPlugin.getDefault().getStateLocation();
		location = location.append("scope_sets"); //$NON-NLS-1$
		File dir = location.toFile();
		if (dir.exists()==false)
			dir.mkdir();
	}

	public void save() {
		ensureLocation();
		for (int i = 0; i < sets.size(); i++) {
			ScopeSet set = (ScopeSet) sets.get(i);
			set.save();
		}
		IDialogSettings settings = HelpUIPlugin.getDefault()
				.getDialogSettings();
		if (activeSet != null)
			settings.put(ACTIVE_SET, activeSet.getName());
	}

	public ScopeSet[] getScopeSets() {
		return (ScopeSet[]) sets.toArray(new ScopeSet[sets.size()]);
	}

	private void loadScopeSets() {
		sets = new ArrayList();
		IPath location = HelpUIPlugin.getDefault().getStateLocation();
		location = location.append("scope_sets"); //$NON-NLS-1$
		File dir = location.toFile();
		ScopeSet defSet=null;
		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".pref"); //$NON-NLS-1$
				}
			});
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				String name = file.getName();
				int loc = name.indexOf(".pref"); //$NON-NLS-1$
				if (loc != -1) {
					ScopeSet set = new ScopeSet(name.substring(0, loc));
					sets.add(set);
					if (set.isDefault())
						defSet=set;
				}
			}
		}
		if (sets.size()==1) {
			activeSet = (ScopeSet)sets.get(0);
		}
		if (defSet==null)
			sets.add(new ScopeSet());
	}

	/**
	 * @return Returns the activeSet.
	 */
	public ScopeSet getActiveSet() {
		if (activeSet == null) {
			IDialogSettings settings = HelpUIPlugin.getDefault()
					.getDialogSettings();
			String name = settings.get(ACTIVE_SET);
			activeSet = findSet(name);
		}
		return activeSet;
	}
	// if name is not null, return the scope set with the
	// matching name; otherwise, return the default set
	public ScopeSet findSet(String name) {
		for (int i = 0; i < sets.size(); i++) {
			ScopeSet set = (ScopeSet) sets.get(i);
			if (name!=null) {
				if (set.getName().equals(name))
					return set;
			}
			else if (set.isDefault())
				return set;
		}
		return null;
	}
}