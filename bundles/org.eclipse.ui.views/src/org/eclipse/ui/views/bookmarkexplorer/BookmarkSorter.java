/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.views.bookmarkexplorer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

class BookmarkSorter extends ViewerSorter {
	
	private int direction;
	
	private int priorities[] = { 
		BookmarkConstants.COLUMN_FOLDER,
		BookmarkConstants.COLUMN_RESOURCE,
		BookmarkConstants.COLUMN_DESCRIPTION,
		BookmarkConstants.COLUMN_LOCATION };
									
	public BookmarkSorter() {
		direction = BookmarkConstants.SORT_ASCENDING;
	}
	
	public void reverse() {
		direction *= -1;
	}
	
	public void setTopPriority(int priority) {
		if (priority < BookmarkConstants.COLUMN_DESCRIPTION || priority > BookmarkConstants.COLUMN_LOCATION)
			return;
		
		int index = -1;
		for (int i = 0; i < priorities.length; i++) {
			if (priorities[i] == priority)
				index = i;
		}
		
		if (index == -1) {
			resetPriorities();
			direction = BookmarkConstants.SORT_ASCENDING;
			return;
		}
			
		//shift the array
		for (int i = index; i > 0; i--) {
			priorities[i] = priorities[i - 1];
		}
		priorities[0] = priority;
	}
	
	public void setDirection(int direction) {
		if (direction == BookmarkConstants.SORT_ASCENDING || direction == BookmarkConstants.SORT_DESCENDING)
			this.direction = direction;
	}
	
	public int getDirection() {
		return direction;
	}
	
	public int getTopPriority() {
		return priorities[0];
	}
	
	public int[] getPriorities() {
		return priorities;
	}
	
	public void resetPriorities() {
		priorities[0] = BookmarkConstants.COLUMN_FOLDER;
		priorities[1] = BookmarkConstants.COLUMN_RESOURCE;
		priorities[2] = BookmarkConstants.COLUMN_DESCRIPTION;
		priorities[3] = BookmarkConstants.COLUMN_LOCATION;
	}
	
	private int compare(IMarker marker1, IMarker marker2, int depth) {
		if (depth >= priorities.length)
			return 0;
		
		switch (priorities[depth]) {
			case BookmarkConstants.COLUMN_DESCRIPTION: {
				String desc1 = marker1.getAttribute(IMarker.MESSAGE, "");//$NON-NLS-1$
				String desc2 = marker2.getAttribute(IMarker.MESSAGE, "");//$NON-NLS-1$
				int result = collator.compare(desc1, desc2);
				if (result == 0)
					result = compare(marker1, marker2, depth + 1);
				return result;
			}
			case BookmarkConstants.COLUMN_RESOURCE: {
				String res1 = marker1.getResource().getName();
				String res2 = marker2.getResource().getName();
				int result = collator.compare(res1, res2);
				if (result == 0)
					result = compare(marker1, marker2, depth + 1);
				return result;
			}
			case BookmarkConstants.COLUMN_FOLDER: {
				String folder1 = BookmarkLabelProvider.getContainerName(marker1);
				String folder2 = BookmarkLabelProvider.getContainerName(marker2);
				int result = collator.compare(folder1, folder2);
				if (result == 0)
					result = compare(marker1, marker2, depth + 1);
				return result;
			}
			case BookmarkConstants.COLUMN_LOCATION: {
				int line1 = marker1.getAttribute(IMarker.LINE_NUMBER, -1);
				int line2 = marker2.getAttribute(IMarker.LINE_NUMBER, -1);
				if (line1 == line2)
					return compare(marker1, marker2, depth + 1);
				if (line1 < line2)
					return -1;
				return 1;
			}
		}
		
		return 0;
	}

	public int compare(Viewer viewer, Object e1, Object e2) {
		IMarker marker1 = (IMarker) e1;
		IMarker marker2 = (IMarker) e2;
		
		return compare(marker1, marker2, 0) * direction;
	}

	public void saveState(IDialogSettings settings) {
		if (settings == null)
			return;
			
		settings.put("columnCount", priorities.length);//$NON-NLS-1$
		settings.put("reversed", direction);//$NON-NLS-1$
		for (int i = 0; i < priorities.length; i++) 
			settings.put("priority" + i, priorities[i]);//$NON-NLS-1$
	}	
	
	public void restoreState(IDialogSettings settings) {
		if (settings == null)
			return;
		
		try {
			int columnCount = settings.getInt("columnCount");//$NON-NLS-1$
			if (priorities.length != columnCount)
				priorities = new int[columnCount];
			direction = settings.getInt("reversed");//$NON-NLS-1$
			for (int i = 0; i < priorities.length; i++)
				priorities[i] = settings.getInt("priority" + i);//$NON-NLS-1$
		}
		catch (NumberFormatException e) {
			resetPriorities();
		}
	}
}
