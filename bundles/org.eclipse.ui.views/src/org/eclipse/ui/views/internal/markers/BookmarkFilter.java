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

package org.eclipse.ui.views.internal.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.IDialogSettings;

class BookmarkFilter extends MarkerFilter {

	private final static String TAG_CONTAINS = "contains"; //$NON-NLS-1$
	private final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private final static String TAG_DIALOG_SECTION = "filter"; //$NON-NLS-1$

	final static boolean DEFAULT_CONTAINS = true;
	final static String DEFAULT_DESCRIPTION = ""; //$NON-NLS-1$

	private boolean contains;
	private String description;

	public BookmarkFilter() {
		super(new String[] { IMarker.BOOKMARK });
	}
	
	public boolean select(Object item) {
		return isEnabled() ? super.select(item) && selectByDescription(item) : true;
	}

	private boolean selectByDescription(Object item) {
		if (!(item instanceof IMarker) || description == null || description.equals("")) //$NON-NLS-1$
			return true;
		
		IMarker marker = (IMarker) item;
		String markerDescription = marker.getAttribute(IMarker.MESSAGE, DEFAULT_DESCRIPTION); //$NON-NLS-1$
		int index = markerDescription.indexOf(description);
		return contains ? (index >= 0) : (index < 0); 
	}
	
	public boolean getContains() {
		return contains;
	}

	public String getDescription() {
		return description;
	}

	public void setContains(boolean contains) {
		this.contains = contains;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void resetState() {
		super.resetState();
		contains = DEFAULT_CONTAINS;
		description = DEFAULT_DESCRIPTION;
	}

	public void restoreState(IDialogSettings dialogSettings) {		
		super.restoreState(dialogSettings);
		IDialogSettings settings = dialogSettings.getSection(TAG_DIALOG_SECTION);
		
		if (settings != null) {
			String setting = settings.get(TAG_CONTAINS);

			if (setting != null)
				contains = Boolean.valueOf(setting).booleanValue();
				
			setting = settings.get(TAG_DESCRIPTION);
			
			if (setting != null)			
				description = new String(setting);
		}
	}

	public void saveState(IDialogSettings dialogSettings) {
		super.saveState(dialogSettings);
		
		if (dialogSettings != null) {
			IDialogSettings settings = dialogSettings.getSection(TAG_DIALOG_SECTION);

			if (settings == null)
				settings = dialogSettings.addNewSection(TAG_DIALOG_SECTION);

			settings.put(TAG_CONTAINS, contains);
			settings.put(TAG_DESCRIPTION, description);
		}
	}
}
