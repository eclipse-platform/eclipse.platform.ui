/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.internal.markers.bookmarks;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.views.internal.markers.MarkerFilter;


class BookmarkFilter extends MarkerFilter {

	private static final String TAG_DIALOG_SECTION = "filter"; //$NON-NLS-1$
	private static final String TAG_CONTAINS = "contains"; //$NON-NLS-1$
	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

	static final boolean DEFAULT_CONTAINS = true;

	private boolean contains;
	private String description;

	/**
	 * @param rootTypes
	 */
	public BookmarkFilter() {
		super(new String[] {IMarker.BOOKMARK});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerFilter#select(java.lang.Object)
	 */
	public boolean select(Object item) {
		if (!isEnabled()) {
			return true;
		}
		
		return super.select(item) && selectByDescription(item);
	}

	private boolean selectByDescription(Object item) {
		if (!(item instanceof IMarker) || description == null || description.equals("")) { //$NON-NLS-1$
			return true;
		}
		IMarker marker = (IMarker) item;
		String markerDescription = marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
		int index = markerDescription.indexOf(description);
		return contains ? (index >= 0) : (index < 0); 
		
	}
	
	/**
	 * @return
	 */
	public boolean getContains() {
		return contains;
	}

	/**
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param b
	 */
	public void setContains(boolean b) {
		contains = b;
	}

	/**
	 * @param string
	 */
	public void setDescription(String string) {
		description = string;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerFilter#resetState()
	 */
	public void resetState() {
		super.resetState();
		contains = DEFAULT_CONTAINS;
		description = ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerFilter#saveState(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	public void saveState(IDialogSettings dialogSettings) {
		super.saveState(dialogSettings);
		if (dialogSettings == null) {
			return;
		}

		IDialogSettings settings = dialogSettings.getSection(TAG_DIALOG_SECTION);
		if (settings == null) {
			settings = dialogSettings.addNewSection(TAG_DIALOG_SECTION);
		}

		settings.put(TAG_CONTAINS, contains);
		settings.put(TAG_DESCRIPTION, description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerFilter#restoreState(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	public void restoreState(IDialogSettings dialogSettings) {
		super.restoreState(dialogSettings);
		if (dialogSettings == null) {
			resetState();
			return;
		}
		
		IDialogSettings settings = dialogSettings.getSection(TAG_DIALOG_SECTION);
		if (settings == null) {
			resetState();
			return;
		}

		String setting = settings.get(TAG_CONTAINS);
		if (setting == null) {
			contains = DEFAULT_CONTAINS;
		}
		else {
			contains = Boolean.valueOf(setting).booleanValue();
		}
		setting = settings.get(TAG_DESCRIPTION);
		if (setting == null) {
			description = ""; //$NON-NLS-1$
		}
		else {
			description = new String(setting);
		}
	}

}
