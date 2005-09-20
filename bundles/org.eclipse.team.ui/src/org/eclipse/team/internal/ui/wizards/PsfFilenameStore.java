/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.wizards;

import java.util.Vector;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

public class PsfFilenameStore {
	// Most recently used filename is first in the array.
	// Least recently used filename is at the end of the list.
	// When the list overflows, items drop off the end.
	private static final int HISTORY_LENGTH = 10;

	private static final String STORE_SECTION = "ImportPSFDialog"; //$NON-NLS-1$
	private static final String FILENAMES = "filenames"; //$NON-NLS-1$
	private static final String PREVIOUS = "previous"; //$NON-NLS-1$

	// If a PSF file was selected when the wizard was opened, then this is it.
	// This is only a cache; it is not part of the history until the user has used it.
	private static String _selectedFilename = null;

	private static IDialogSettings _section;

	private PsfFilenameStore() {
		// All-static
	}

	public static void setDefaultFromSelection(IWorkbench workbench) {
		// Scan the workbench for a selected PSF file
		IWorkbenchWindow wnd = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage pg = wnd.getActivePage();
		ISelection sel = pg.getSelection();

		if (!(sel instanceof IStructuredSelection)) {
			return;
		}
		IStructuredSelection selection = (IStructuredSelection)sel;

		Object firstElement = selection.getFirstElement();
		if (!(firstElement instanceof IAdaptable)) {
			return;
		}
		Object o = ((IAdaptable) firstElement).getAdapter(IResource.class);
		if (o == null) {
			return;
		}
		IResource resource = (IResource) o;

		if (resource.getType() != IResource.FILE) {
			return;
		}

		if (!resource.isAccessible()) {
			return;
		}

		String extension = resource.getFileExtension();
		if (extension == null || !extension.equalsIgnoreCase("psf")) { //$NON-NLS-1$
			return;
		}

		IWorkspace workspace = resource.getWorkspace();
		workspace.getRoot().getFullPath();

		IPath path = resource.getLocation();
		_selectedFilename = path.toOSString();
	}

	public static String getSuggestedDefault() {
		if (_selectedFilename != null) {
			return _selectedFilename;
		}
		return getPrevious();
	}

	private static String getPrevious() {
		IDialogSettings section = getSettingsSection();
		String retval = section.get(PREVIOUS);
		if (retval == null) {
			retval = ""; //$NON-NLS-1$
		}
		return retval;
	}

	public static String[] getHistory() {
		IDialogSettings section = getSettingsSection();
		String[] arr = section.getArray(FILENAMES);
		if (arr == null) {
			arr = new String[0];
		}
		return arr;
	}

	public static void remember(String filename) {
		Vector filenames = createVector(getHistory());
		if (filenames.contains(filename)) {
			// The item is in the list. Remove it and add it back at the
			// beginning. If it already was at the beginning this will be a
			// waste of time, but it's not even measurable so I don't care.
			filenames.remove(filename);
		}
		// Most recently used filename goes to the beginning of the list
		filenames.add(0, filename);

		// Forget any overflowing items
		while (filenames.size() > HISTORY_LENGTH) {
			filenames.remove(HISTORY_LENGTH);
		}

		// Make it an array
		String[] arr = (String[]) filenames.toArray(new String[filenames.size()]);

		IDialogSettings section = getSettingsSection();
		section.put(FILENAMES, arr);
		section.put(PREVIOUS, filename);
	}

	private static Vector createVector(Object[] arr) {
		Vector v = new Vector();
		for (int ix = 0; ix < arr.length; ++ix) {
			v.add(ix, arr[ix]);
		}
		return v;
	}

	private static IDialogSettings getSettingsSection() {
		if (_section != null)
			return _section;

		IDialogSettings settings = TeamUIPlugin.getPlugin().getDialogSettings();
		_section = settings.getSection(STORE_SECTION);
		if (_section != null)
			return _section;

		_section = settings.addNewSection(STORE_SECTION);
		return _section;
	}
}
