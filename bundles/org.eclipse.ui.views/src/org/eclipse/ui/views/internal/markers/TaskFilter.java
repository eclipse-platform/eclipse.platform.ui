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

class TaskFilter extends MarkerFilter {

	private static final String TAG_CONTAINS = "contains"; //$NON-NLS-1$
	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String TAG_DIALOG_SECTION = "filter"; //$NON-NLS-1$	
	private static final String TAG_DONE = "done"; //$NON-NLS-1$
	private static final String TAG_PRIORITY = "priority"; //$NON-NLS-1$
	private static final String TAG_SELECT_BY_DONE = "selectByDone"; //$NON-NLS-1$
	private static final String TAG_SELECT_BY_PRIORITY = "selectByPriority"; //$NON-NLS-1$

	final static boolean DEFAULT_CONTAINS = true;
	final static String DEFAULT_DESCRIPTION = ""; //$NON-NLS-1$	
	final static boolean DEFAULT_DONE = false;
	final static int DEFAULT_PRIORITY = 0;
	final static boolean DEFAULT_SELECT_BY_DONE = false;
	final static boolean DEFAULT_SELECT_BY_PRIORITY = false;
	
	final static int PRIORITY_HIGH = 1 << 2;
	final static int PRIORITY_NORMAL = 1 << 1;
	final static int PRIORITY_LOW = 1 << 0;

	private boolean contains;
	private String description;
	private boolean done;
	private int priority;
	private boolean selectByPriority;
	private boolean selectByDone;

	public TaskFilter() {
		super(new String[] { IMarker.TASK });
	}
	
	public boolean select(Object item) {
		return isEnabled() ? super.select(item) && selectByDescription(item) && selectByDone(item) && selectByPriority(item) : true;
	}
	
	private boolean selectByDescription(Object item) {
		if (!(item instanceof IMarker) || description == null || description.equals("")) //$NON-NLS-1$
			return true;
		
		IMarker marker = (IMarker) item;
		String markerDescription = marker.getAttribute(IMarker.MESSAGE, DEFAULT_DESCRIPTION); //$NON-NLS-1$
		int index = markerDescription.indexOf(description);
		return contains ? (index >= 0) : (index < 0);		
	}

	private boolean selectByDone(Object item) {
		if (item instanceof IMarker && selectByDone)
			return done == ((IMarker) item).getAttribute(IMarker.DONE, false);
			
		return true;
	}

	private boolean selectByPriority(Object item) {
		if (item instanceof IMarker && priority != 0 && selectByPriority) {
			int markerPriority = ((IMarker) item).getAttribute(IMarker.PRIORITY, -1);
		
			if (markerPriority == IMarker.PRIORITY_HIGH)
				return (priority & PRIORITY_HIGH) > 0;		
			else if (markerPriority == IMarker.PRIORITY_NORMAL)
				return (priority & PRIORITY_NORMAL) > 0;
			else if (markerPriority == IMarker.PRIORITY_LOW)
				return (priority & PRIORITY_LOW) > 0;
		}
		
		return true;
	}

	public boolean getContains() {
		return contains;
	}

	public String getDescription() {
		return description;
	}

	public boolean getDone() {
		return done;
	}

	public int getPriority() {
		return priority;
	}

	public boolean getSelectByDone() {
		return selectByDone;
	}

	public boolean getSelectByPriority() {
		return selectByPriority;
	}

	public void setContains(boolean contains) {
		this.contains = contains;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setDone(boolean done) {
		this.done = done;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setSelectByDone(boolean selectByDone) {
		this.selectByDone = selectByDone;
	}
	
	public void setSelectByPriority(boolean selectByPriority) {
		this.selectByPriority = selectByPriority;
	}

	public void resetState() {
		super.resetState();
		contains = DEFAULT_CONTAINS;
		description = DEFAULT_DESCRIPTION;		
		done = DEFAULT_DONE;
		priority = DEFAULT_PRIORITY;
		selectByDone = DEFAULT_SELECT_BY_DONE;
		selectByPriority = DEFAULT_SELECT_BY_PRIORITY;
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

			setting = settings.get(TAG_DONE);
			
			if (setting != null)
				done = Boolean.valueOf(setting).booleanValue();

			setting = settings.get(TAG_PRIORITY);

			if (setting != null)
				try {
					priority = Integer.parseInt(setting);		
				}
				catch (NumberFormatException eNumberFormat) {
				}

			setting = settings.get(TAG_SELECT_BY_DONE);
			
			if (setting != null)
				selectByDone = Boolean.valueOf(setting).booleanValue();

			setting = settings.get(TAG_SELECT_BY_PRIORITY);
			
			if (setting != null)
				selectByPriority = Boolean.valueOf(setting).booleanValue();
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
			settings.put(TAG_DONE, done);		
			settings.put(TAG_PRIORITY, priority);
			settings.put(TAG_SELECT_BY_DONE, selectByDone);
			settings.put(TAG_SELECT_BY_PRIORITY, selectByPriority);
		}
	}
}
