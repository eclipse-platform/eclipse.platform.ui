/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	private static class NavigationEnablementAction extends Action implements Comparable {

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

		/*
		 * @see IAction#run()
		 */
		public void run() {
			fStore.setValue(fKey, isChecked());
		}

		/*
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 * @since 3.2
		 */
		public int compareTo(Object o) {
			if (!(o instanceof NavigationEnablementAction))
				return -1;

			String otherName= ((NavigationEnablementAction)o).fName;

			return Collator.getInstance().compare(fName, otherName);
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

	/*
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
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

	/*
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		if (fMenu == null) {
			fMenu= new Menu(parent);
			fillMenu(fMenu);
		}

		return fMenu;
	}

	/*
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
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
		SortedSet containers= new TreeSet();

		Iterator iter= fMarkerAnnotationPreferences.getAnnotationPreferences().iterator();
		while (iter.hasNext()) {
			AnnotationPreference preference= (AnnotationPreference)iter.next();
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

		return (IAction[]) containers.toArray(new Action[containers.size()]);
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
