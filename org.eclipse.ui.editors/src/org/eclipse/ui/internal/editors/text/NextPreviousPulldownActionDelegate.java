/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.editors.text;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.ibm.icu.text.Collator;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;

/**
 * The abstract class for next and previous pulldown action delegates.
 *
 * @since 3.0
 */
public abstract class NextPreviousPulldownActionDelegate extends Action implements IMenuCreator, IWorkbenchWindowPulldownDelegate2 {

	/** The cached menu. */
	private Menu fMenu;

	/** The preference store */
	private IPreferenceStore fStore;

	/** Action for handling menu item selection. */
	private static class NavigationEnablementAction extends Action implements Comparable<NavigationEnablementAction> {

		/** The preference store. */
		private IPreferenceStore fStore;

		/** The preference key for the value in the store. */
		private String fKey;

		/**
		 * The display string.
		 * @since 3.2
		 */
		private String fName;

		/**
		 * Creates a named navigation enablement action.
		 *
		 * @param name the name of this action
		 * @param store the preference store
		 * @param key the preference key
		 */
		public NavigationEnablementAction(String name, IPreferenceStore store, String key) {
			super(name, IAction.AS_CHECK_BOX);
			fStore= store;
			fKey= key;
			fName= name;
			setChecked(fStore.getBoolean(fKey));
		}

		@Override
		public void run() {
			fStore.setValue(fKey, isChecked());
		}

		@Override
		public int compareTo(NavigationEnablementAction o) {
			return Collator.getInstance().compare(fName, o.fName);
		}
	}

	/**
	 * Returns the preference key to be used in the
	 * <code>NavigationEnablementAction</code>.
	 *
	 * @param annotationPreference the annotation preference
	 * @return the preference key or <code>null</code> if the key is not defined in XML
	 */
	public abstract String getPreferenceKey(AnnotationPreference annotationPreference);

	@Override
	public Menu getMenu(Control parent) {
		if (fMenu != null)
			fMenu.dispose();

		fMenu= new Menu(parent);
		fillMenu(fMenu);

		return fMenu;
	}

	/**
	 * Creates a next previous action delegate.
	 */
	public NextPreviousPulldownActionDelegate() {
		fStore= EditorsPlugin.getDefault().getPreferenceStore();
	}

	@Override
	public Menu getMenu(Menu parent) {
		if (fMenu == null) {
			fMenu= new Menu(parent);
			fillMenu(fMenu);
		}

		return fMenu;
	}

	@Override
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
			fMenu= null;
		}
	}

	/**
	 * Fills the given menu using marker
	 * annotation preferences.
	 *
	 * @param menu the menu to fill
	 */
	private void fillMenu(Menu menu) {
		IAction[] actions= getActionsFromDescriptors();

		for (int i= 0; i < actions.length; i++) {
			ActionContributionItem item= new ActionContributionItem(actions[i]);
			item.fill(menu, -1);
		}
	}

	/**
	 * Creates actions using marker
	 * annotation preferences.
	 *
	 * @return the navigation enablement actions
	 */
	private IAction[] getActionsFromDescriptors() {
		MarkerAnnotationPreferences fMarkerAnnotationPreferences= EditorsPlugin.getDefault().getMarkerAnnotationPreferences();
		SortedSet<NavigationEnablementAction> containers= new TreeSet<>();

		Iterator<AnnotationPreference> iter= fMarkerAnnotationPreferences.getAnnotationPreferences().iterator();
		while (iter.hasNext()) {
			AnnotationPreference preference= iter.next();
			String key= preference.getShowInNextPrevDropdownToolbarActionKey();
			if (key != null && fStore.getBoolean(key)) {
				String preferenceKey= getPreferenceKey(preference);

				/*
				 * Fixes bug 41689
				 * This code can be simplified if we decide that
				 * we don't allow to use different settings for go to
				 * previous and go to next annotation.
				 */
				preferenceKey= preference.getIsGoToNextNavigationTargetKey();

				if (preferenceKey != null)
					containers.add(new NavigationEnablementAction(preference.getPreferenceLabel(), fStore, preferenceKey));
			}
		}

		return containers.toArray(new Action[containers.size()]);
	}

	@Override
	public void init(IWorkbenchWindow window) {
	}

	@Override
	public void run(IAction action) {
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
