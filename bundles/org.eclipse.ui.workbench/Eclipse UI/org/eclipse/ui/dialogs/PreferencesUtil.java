/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.dialogs;

import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;

/**
 * The PreferencesUtil class is the class that opens a properties or preference
 * dialog on a set of ids.
 *
 * @since 3.1
 */
public final class PreferencesUtil {

	/**
	 * Constant denoting no option.
	 *
	 * @since 3.5
	 */
	public static final int OPTION_NONE = 0;

	/**
	 * Constant for configuring a preferences or properties dialog in which the user
	 * cannot "unfilter" to show a larger set of pages than was passed to
	 * {@link #createPreferenceDialogOn(Shell, String, String[], Object, int)} or
	 * {@link #createPropertyDialogOn(Shell, IAdaptable, String, String[], Object, int)}
	 * .
	 *
	 * @since 3.5
	 */
	public static final int OPTION_FILTER_LOCKED = 1;

	/**
	 * Apply the data to the first page if there is any.
	 *
	 * @param data         The data to be applied
	 * @param displayedIds The ids to filter to.
	 * @param dialog       The dialog to apply to.
	 * @param options
	 */
	private static void applyOptions(Object data, String[] displayedIds, FilteredPreferenceDialog dialog, int options) {
		if (data != null) {
			dialog.setPageData(data);
			IPreferencePage page = dialog.getCurrentPage();
			if (page instanceof PreferencePage) {
				((PreferencePage) page).applyData(data);
			}
		}

		if (displayedIds != null) {
			dialog.showOnly(displayedIds);
		}

		if ((options & OPTION_FILTER_LOCKED) != 0) {
			dialog.setLocked(true);
		}
	}

	/**
	 * Creates a workbench preference dialog and selects particular preference page.
	 * If there is already a preference dialog open this dialog is used and its
	 * selection is set to the page with id preferencePageId. Show the other pages
	 * as filtered results using whatever filtering criteria the search uses. It is
	 * the responsibility of the caller to then call <code>open()</code>. The call
	 * to <code>open()</code> will not return until the dialog closes, so this is
	 * the last chance to manipulate the dialog.
	 *
	 * @param shell            The Shell to parent the dialog off of if it is not
	 *                         already created. May be <code>null</code> in which
	 *                         case the active workbench window will be used if
	 *                         available.
	 * @param preferencePageId The identifier of the preference page to open; may be
	 *                         <code>null</code>. If it is <code>null</code>, then
	 *                         the preference page is not selected or modified in
	 *                         any way.
	 * @param displayedIds     The ids of the other pages to be displayed using the
	 *                         same filtering criterea as search. If this is
	 *                         <code>null</code>, then the all preference pages are
	 *                         shown.
	 * @param data             Data that will be passed to all of the preference
	 *                         pages to be applied as specified within the page as
	 *                         they are created. If the data is <code>null</code>
	 *                         nothing will be called.
	 *
	 * @return a preference dialog.
	 * @since 3.1
	 * @see PreferenceDialog#PreferenceDialog(Shell, PreferenceManager)
	 */
	public static PreferenceDialog createPreferenceDialogOn(Shell shell, String preferencePageId, String[] displayedIds,
			Object data) {
		return createPreferenceDialogOn(shell, preferencePageId, displayedIds, data, OPTION_NONE);
	}

	/**
	 * Creates a workbench preference dialog to a particular preference page. Show
	 * the other pages as filtered results using whatever filtering criteria the
	 * search uses. It is the responsibility of the caller to then call
	 * <code>open()</code>. The call to <code>open()</code> will not return until
	 * the dialog closes, so this is the last chance to manipulate the dialog.
	 *
	 * @param shell          The shell to use to parent the dialog if required.
	 * @param propertyPageId The identifier of the preference page to open; may be
	 *                       <code>null</code>. If it is <code>null</code>, then the
	 *                       dialog is opened with no selected page.
	 * @param element        IAdaptable An adaptable element to open the dialog on.
	 * @param displayedIds   The ids of the other pages to be displayed using the
	 *                       same filtering criterea as search. If this is
	 *                       <code>null</code>, then the all preference pages are
	 *                       shown.
	 * @param data           Data that will be passed to all of the preference pages
	 *                       to be applied as specified within the page as they are
	 *                       created. If the data is <code>null</code> nothing will
	 *                       be called.
	 *
	 * @return A preference dialog showing properties for the selection or
	 *         <code>null</code> if it could not be created.
	 * @since 3.1
	 */
	public static PreferenceDialog createPropertyDialogOn(Shell shell, final IAdaptable element, String propertyPageId,
			String[] displayedIds, Object data) {
		return createPropertyDialogOn(shell, element, propertyPageId, displayedIds, data, OPTION_NONE);
	}

	/**
	 * Creates a workbench preference dialog and selects particular preference page.
	 * If there is already a preference dialog open this dialog is used and its
	 * selection is set to the page with id preferencePageId. Show the other pages
	 * as filtered results using whatever filtering criteria the search uses. It is
	 * the responsibility of the caller to then call <code>open()</code>. The call
	 * to <code>open()</code> will not return until the dialog closes, so this is
	 * the last chance to manipulate the dialog.
	 *
	 * @param shell            The Shell to parent the dialog off of if it is not
	 *                         already created. May be <code>null</code> in which
	 *                         case the active workbench window will be used if
	 *                         available.
	 * @param preferencePageId The identifier of the preference page to open; may be
	 *                         <code>null</code>. If it is <code>null</code>, then
	 *                         the preference page is not selected or modified in
	 *                         any way.
	 * @param displayedIds     The ids of the other pages to be displayed using the
	 *                         same filtering criterea as search. If this is
	 *                         <code>null</code>, then the all preference pages are
	 *                         shown.
	 * @param data             Data that will be passed to all of the preference
	 *                         pages to be applied as specified within the page as
	 *                         they are created. If the data is <code>null</code>
	 *                         nothing will be called.
	 * @param options          a bitwise OR of option constants
	 *
	 * @return a preference dialog.
	 * @since 3.5
	 * @see PreferenceDialog#PreferenceDialog(Shell, PreferenceManager)
	 */
	public static PreferenceDialog createPreferenceDialogOn(Shell shell, String preferencePageId, String[] displayedIds,
			Object data, int options) {
		FilteredPreferenceDialog dialog = WorkbenchPreferenceDialog.createDialogOn(shell, preferencePageId);

		applyOptions(data, displayedIds, dialog, options);

		return dialog;
	}

	/**
	 * Creates a workbench preference dialog to a particular preference page. Show
	 * the other pages as filtered results using whatever filtering criteria the
	 * search uses. It is the responsibility of the caller to then call
	 * <code>open()</code>. The call to <code>open()</code> will not return until
	 * the dialog closes, so this is the last chance to manipulate the dialog.
	 *
	 * @param shell          The shell to use to parent the dialog if required.
	 * @param propertyPageId The identifier of the preference page to open; may be
	 *                       <code>null</code>. If it is <code>null</code>, then the
	 *                       dialog is opened with no selected page.
	 * @param element        IAdaptable An adaptable element to open the dialog on.
	 * @param displayedIds   The ids of the other pages to be displayed using the
	 *                       same filtering criteria as search. If this is
	 *                       <code>null</code>, then the all preference pages are
	 *                       shown.
	 * @param data           Data that will be passed to all of the preference pages
	 *                       to be applied as specified within the page as they are
	 *                       created. If the data is <code>null</code> nothing will
	 *                       be called.
	 * @param options        a bitwise OR of option constants
	 *
	 * @return A preference dialog showing properties for the selection or
	 *         <code>null</code> if it could not be created.
	 * @since 3.5
	 */
	public static PreferenceDialog createPropertyDialogOn(Shell shell, final IAdaptable element, String propertyPageId,
			String[] displayedIds, Object data, int options) {

		FilteredPreferenceDialog dialog = PropertyDialog.createDialogOn(shell, propertyPageId, element);

		if (dialog == null) {
			return null;
		}

		applyOptions(data, displayedIds, dialog, options);

		return dialog;

	}

	/**
	 * Creates a workbench preference dialog to a particular preference page. Show
	 * the other pages as filtered results using whatever filtering criteria the
	 * search uses. It is the responsibility of the caller to then call
	 * <code>open()</code>. The call to <code>open()</code> will not return until
	 * the dialog closes, so this is the last chance to manipulate the dialog.
	 *
	 * @param shell          The shell to use to parent the dialog if required.
	 * @param propertyPageId The identifier of the preference page to open; may be
	 *                       <code>null</code>. If it is <code>null</code>, then the
	 *                       dialog is opened with no selected page.
	 * @param element        An element to open the dialog on.
	 * @param displayedIds   The IDs of the other pages to be displayed using the
	 *                       same filtering criteria as search. If this is
	 *                       <code>null</code>, then the all preference pages are
	 *                       shown.
	 * @param data           Data that will be passed to all of the preference pages
	 *                       to be applied as specified within the page as they are
	 *                       created. If the data is <code>null</code> nothing will
	 *                       be called.
	 * @param options        a bitwise OR of option constants
	 *
	 * @return A preference dialog showing properties for the selection or
	 *         <code>null</code> if it could not be created.
	 * @since 3.6
	 */
	public static PreferenceDialog createPropertyDialogOn(Shell shell, final Object element, String propertyPageId,
			String[] displayedIds, Object data, int options) {
		FilteredPreferenceDialog dialog = PropertyDialog.createDialogOn(shell, propertyPageId, element);
		if (dialog == null)
			return null;
		applyOptions(data, displayedIds, dialog, options);
		return dialog;
	}

	/**
	 * Indicates whether the specified element has at least one property page
	 * contributor.
	 *
	 * @param element an adapter element of a property page
	 * @return true for having at least one contributor; false otherwise
	 * @since 3.4
	 */
	public static boolean hasPropertiesContributors(Object element) {
		if (element == null || !(element instanceof IAdaptable))
			return false;
		Collection contributors = PropertyPageContributorManager.getManager().getApplicableContributors(element);
		return contributors != null && contributors.size() > 0;
	}

	/**
	 * Return all of the properties page contributors for an element.
	 *
	 * @param element the element to process
	 * @return {@link IPreferenceNode}[]
	 * @since 3.4
	 */
	public static IPreferenceNode[] propertiesContributorsFor(Object element) {
		PropertyPageManager pageManager = new PropertyPageManager();
		if (element == null) {
			return null;
		}
		// load pages for the selection
		// fill the manager with contributions from the matching contributors
		PropertyPageContributorManager.getManager().contribute(pageManager, element);
		// testing if there are pages in the manager
		List<IPreferenceNode> pages = pageManager.getElements(PreferenceManager.PRE_ORDER);
		return pages.toArray(IPreferenceNode[]::new);
	}

}
