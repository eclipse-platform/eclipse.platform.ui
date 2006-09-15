/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.bookmarkexplorer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

class BookmarkSorter extends ViewerComparator {

    private int[] directions;

    private int[] priorities;

    final static int ASCENDING = 1;

    final static int DESCENDING = -1;

    final static int DESCRIPTION = 0;

    final static int RESOURCE = 1;

    final static int FOLDER = 2;

    final static int LOCATION = 3;

    final static int CREATION_TIME = 4;

    final static int[] DEFAULT_PRIORITIES = { FOLDER, RESOURCE, LOCATION,
            DESCRIPTION, CREATION_TIME };

    final static int[] DEFAULT_DIRECTIONS = { ASCENDING, //description
            ASCENDING, //resource
            ASCENDING, //folder
            ASCENDING, //location
            ASCENDING, }; //creation time

    public BookmarkSorter() {
        resetState();
    }

    public void reverseTopPriority() {
        directions[priorities[0]] *= -1;
    }

    public void setTopPriority(int priority) {
        if (priority < 0 || priority >= priorities.length) {
			return;
		}

        int index = -1;
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i] == priority) {
				index = i;
			}
        }

        if (index == -1) {
            resetState();
            return;
        }

        //shift the array
        for (int i = index; i > 0; i--) {
            priorities[i] = priorities[i - 1];
        }
        priorities[0] = priority;
        directions[priority] = DEFAULT_DIRECTIONS[priority];
    }

    public void setTopPriorityDirection(int direction) {
        if (direction == ASCENDING || direction == DESCENDING) {
			directions[priorities[0]] = direction;
		}
    }

    public int getTopPriorityDirection() {
        return directions[priorities[0]];
    }

    public int getTopPriority() {
        return priorities[0];
    }

    public int[] getPriorities() {
        return priorities;
    }

    public void resetState() {
        priorities = new int[DEFAULT_PRIORITIES.length];
        System.arraycopy(DEFAULT_PRIORITIES, 0, priorities, 0,
                priorities.length);
        directions = new int[DEFAULT_DIRECTIONS.length];
        System.arraycopy(DEFAULT_DIRECTIONS, 0, directions, 0,
                directions.length);
    }

    private int compare(IMarker marker1, IMarker marker2, int depth) {
        if (depth >= priorities.length) {
			return 0;
		}

        int column = priorities[depth];
        switch (column) {
        case DESCRIPTION: {
            String desc1 = marker1.getAttribute(IMarker.MESSAGE, "");//$NON-NLS-1$
            String desc2 = marker2.getAttribute(IMarker.MESSAGE, "");//$NON-NLS-1$
            int result = getComparator().compare(desc1, desc2);
            if (result == 0) {
				return compare(marker1, marker2, depth + 1);
			}
            return result * directions[column];
        }
        case RESOURCE: {
            String res1 = marker1.getResource().getName();
            String res2 = marker2.getResource().getName();
            int result = getComparator().compare(res1, res2);
            if (result == 0) {
				return compare(marker1, marker2, depth + 1);
			}
            return result * directions[column];
        }
        case FOLDER: {
            String folder1 = BookmarkLabelProvider.getContainerName(marker1);
            String folder2 = BookmarkLabelProvider.getContainerName(marker2);
            int result = getComparator().compare(folder1, folder2);
            if (result == 0) {
				return compare(marker1, marker2, depth + 1);
			}
            return result * directions[column];
        }
        case LOCATION: {
            int line1 = marker1.getAttribute(IMarker.LINE_NUMBER, -1);
            int line2 = marker2.getAttribute(IMarker.LINE_NUMBER, -1);
            int result = line1 - line2;
            if (result == 0) {
				return compare(marker1, marker2, depth + 1);
			}
            return result * directions[column];
        }
        case CREATION_TIME: {
            long result;
            try {
                result = marker1.getCreationTime() - marker2.getCreationTime();
            } catch (CoreException e) {
                result = 0;
            }
            if (result == 0) {
				return compare(marker1, marker2, depth + 1);
			}
            return ((int) result) * directions[column];
        }
        }

        return 0;
    }

    public int compare(Viewer viewer, Object e1, Object e2) {
        IMarker marker1 = (IMarker) e1;
        IMarker marker2 = (IMarker) e2;

        return compare(marker1, marker2, 0);
    }

    public void saveState(IDialogSettings settings) {
        if (settings == null) {
			return;
		}

        for (int i = 0; i < priorities.length; i++) {
            settings.put("priority" + i, priorities[i]);//$NON-NLS-1$
            settings.put("direction" + i, directions[i]);//$NON-NLS-1$
        }
    }

    public void restoreState(IDialogSettings settings) {
        if (settings == null) {
			return;
		}

        try {
            for (int i = 0; i < priorities.length; i++) {
                priorities[i] = settings.getInt("priority" + i);//$NON-NLS-1$
                directions[i] = settings.getInt("direction" + i);//$NON-NLS-1$
            }
        } catch (NumberFormatException e) {
            resetState();
        }
    }
}
