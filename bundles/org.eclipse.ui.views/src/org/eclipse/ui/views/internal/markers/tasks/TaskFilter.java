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

package org.eclipse.ui.views.internal.markers.tasks;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.views.internal.markers.MarkerFilter;


class TaskFilter extends MarkerFilter {
	
	private static final String TAG_DIALOG_SECTION = "filter"; //$NON-NLS-1$
	private static final String TAG_SELECT_BY_PRIORITY = "selectByPriority"; //$NON-NLS-1$
	private static final String TAG_SELECT_BY_STATUS = "selectByStatus"; //$NON-NLS-1$
	private static final String TAG_CONTAINS = "contains"; //$NON-NLS-1$
	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String TAG_PRIORITY = "priority"; //$NON-NLS-1$
	private static final String TAG_DONE = "done"; //$NON-NLS-1$
	
	static final boolean DEFAULT_SELECT_BY_PRIORITY = false;
	static final int DEFAULT_PRIORITY = 0;
	static final boolean DEFAULT_SELECT_BY_STATUS = false;
	static final boolean DEFAULT_CONTAINS = true;
	static final boolean DEFAULT_DONE = false;
	static final int PRIORITY_HIGH = 1 << 2;
	static final int PRIORITY_NORMAL = 1 << 1;
	static final int PRIORITY_LOW = 1 << 0;
	
	private boolean selectByPriority;
	private boolean selectByStatus;
	private boolean contains;
	private String description;
	private int priority;
	private boolean done;

	/**
	 * @param rootTypes
	 */
	public TaskFilter() {
		super(new String[] {IMarker.TASK});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerFilter#select(java.lang.Object)
	 */
	public boolean select(Object item) {
		if (!isEnabled()) {
			return true;
		}
		
		return super.select(item) && selectByDescription(item) && selectByPriority(item) && selectByStatus(item);
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
	
	private boolean selectByPriority(Object item) {
		if (!selectByPriority || (priority == 0) || !(item instanceof IMarker)) {
			return true;
		}
		IMarker marker = (IMarker) item;
		int markerPriority = marker.getAttribute(IMarker.PRIORITY, -1);
		if (markerPriority == IMarker.PRIORITY_HIGH) {
			return (priority & PRIORITY_HIGH) > 0;
		}
		if (markerPriority == IMarker.PRIORITY_NORMAL) {
			return (priority & PRIORITY_NORMAL) > 0;
		}
		return (priority & PRIORITY_LOW) > 0;
	}
	
	private boolean selectByStatus(Object item) {
		if (!selectByStatus || !(item instanceof IMarker)) {
			return true;
		}
		IMarker marker = (IMarker) item;
		return done == marker.getAttribute(IMarker.DONE, false);
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
	 * @return
	 */
	public boolean getDone() {
		return done;
	}

	/**
	 * @return
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return
	 */
	public boolean getSelectByPriority() {
		return selectByPriority;
	}

	/**
	 * @return
	 */
	public boolean getSelectByStatus() {
		return selectByStatus;
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

	/**
	 * @param b
	 */
	public void setDone(boolean b) {
		done = b;
	}

	/**
	 * @param i
	 */
	public void setPriority(int i) {
		priority = i;
	}

	/**
	 * @param b
	 */
	public void setSelectByPriority(boolean b) {
		selectByPriority = b;
	}

	/**
	 * @param b
	 */
	public void setSelectByStatus(boolean b) {
		selectByStatus = b;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerFilter#resetState()
	 */
	public void resetState() {
		super.resetState();
		selectByPriority = DEFAULT_SELECT_BY_PRIORITY;
		selectByStatus = DEFAULT_SELECT_BY_STATUS;
		contains = DEFAULT_CONTAINS;
		description = ""; //$NON-NLS-1$
		priority = DEFAULT_PRIORITY;
		done = DEFAULT_DONE;
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
		settings.put(TAG_SELECT_BY_PRIORITY, selectByPriority);
		settings.put(TAG_PRIORITY, priority);
		settings.put(TAG_SELECT_BY_STATUS, selectByStatus);
		settings.put(TAG_DONE, done);		
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
		setting = settings.get(TAG_SELECT_BY_PRIORITY);
		if (setting == null) {
			selectByPriority = DEFAULT_SELECT_BY_PRIORITY;
		}
		else {
			selectByPriority = Boolean.valueOf(setting).booleanValue();
		}
		setting = settings.get(TAG_SELECT_BY_STATUS);
		if (setting == null) {
			selectByStatus = DEFAULT_SELECT_BY_STATUS;
		}
		else {
			selectByStatus = Boolean.valueOf(setting).booleanValue();
		}
		setting = settings.get(TAG_DONE);
		if (setting == null) {
			done = DEFAULT_DONE;
		}
		else {
			done = Boolean.valueOf(setting).booleanValue();		
		}
		setting = settings.get(TAG_PRIORITY);
		if (setting == null) {
			priority = DEFAULT_PRIORITY;
		}
		else {
			try {
				priority = Integer.parseInt(setting);
			}
			catch (NumberFormatException e) {
				priority = DEFAULT_PRIORITY;
			}
		}
	}

}
