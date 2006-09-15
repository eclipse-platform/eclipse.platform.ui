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

package org.eclipse.ui.views.tasklist;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * This is the task list's sorter.
 */
class TaskSorter extends ViewerComparator {
    private int[] priorities;

    private int[] directions;

    final static int ASCENDING = 1;

    final static int DEFAULT_DIRECTION = 0;

    final static int DESCENDING = -1;

    final static int TYPE = 0;

    final static int COMPLETION = 1;

    final static int PRIORITY = 2;

    final static int DESCRIPTION = 3;

    final static int RESOURCE = 4;

    final static int FOLDER = 5;

    final static int LOCATION = 6;

    final static int CREATION_TIME = 7;

    final static int[] DEFAULT_PRIORITIES = { FOLDER, RESOURCE, LOCATION,
            DESCRIPTION, TYPE, PRIORITY, COMPLETION, CREATION_TIME };

    final static int[] DEFAULT_DIRECTIONS = { DESCENDING, //type
            DESCENDING, //completed
            DESCENDING, //priority
            ASCENDING, //description
            ASCENDING, //resource
            ASCENDING, //folder
            ASCENDING, //location
            ASCENDING }; //creation time

    /**
     * Creates a new task sorter.
     */
    public TaskSorter() {
        resetState();
    }

    /* (non-Javadoc)
     * Method declared on ViewerSorter.
     */
    /**
     * Compares two markers, sorting first by the main column of this sorter,
     * then by subsequent columns, depending on the column sort order.
     */
    public int compare(Viewer viewer, Object e1, Object e2) {
        IMarker m1 = (IMarker) e1;
        IMarker m2 = (IMarker) e2;
        return compareColumnValue(m1, m2, 0);
    }

    public void setTopPriority(int priority) {
        if (priority < 0 || priority >= priorities.length) {
			return;
		}

        int index = -1;
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i] == priority) {
                index = i;
                break;
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

    public int getTopPriority() {
        return priorities[0];
    }

    public int[] getPriorities() {
        return priorities;
    }

    public void setTopPriorityDirection(int direction) {
        if (direction == DEFAULT_DIRECTION) {
			directions[priorities[0]] = DEFAULT_DIRECTIONS[priorities[0]];
		} else if (direction == ASCENDING || direction == DESCENDING) {
			directions[priorities[0]] = direction;
		}
    }

    public int getTopPriorityDirection() {
        return directions[priorities[0]];
    }

    public void reverseTopPriority() {
        directions[priorities[0]] *= -1;
    }

    public void resetState() {
        priorities = new int[DEFAULT_PRIORITIES.length];
        System.arraycopy(DEFAULT_PRIORITIES, 0, priorities, 0,
                priorities.length);
        directions = new int[DEFAULT_DIRECTIONS.length];
        System.arraycopy(DEFAULT_DIRECTIONS, 0, directions, 0,
                directions.length);
    }

    /* (non-Javadoc)
     * Method declared on ViewerSorter.
     */
    /**
     * Compares two markers, based only on the value of the specified column.
     */
    private int compareColumnValue(IMarker m1, IMarker m2, int depth) {
        if (depth >= priorities.length) {
			return 0;
		}

        int columnNumber = priorities[depth];
        int direction = directions[columnNumber];
        switch (columnNumber) {
        case TYPE: {
            /* category */
            int result = getCategoryOrder(m1) - getCategoryOrder(m2);
            if (result == 0) {
				return compareColumnValue(m1, m2, depth + 1);
			}
            return result * direction;
        }
        case COMPLETION: {
            /* completed */
            int result = getCompletedOrder(m1) - getCompletedOrder(m2);
            if (result == 0) {
				return compareColumnValue(m1, m2, depth + 1);
			}
            return result * direction;
        }
        case PRIORITY: {
            /* priority */
            int result = getPriorityOrder(m1) - getPriorityOrder(m2);
            if (result == 0) {
				return compareColumnValue(m1, m2, depth + 1);
			}
            return result * direction;
        }
        case DESCRIPTION: {
            /* description */
            int result = getComparator().compare(MarkerUtil.getMessage(m1), MarkerUtil
                    .getMessage(m2));
            if (result == 0) {
				return compareColumnValue(m1, m2, depth + 1);
			}
            return result * direction;
        }
        case RESOURCE: {
            /* resource name */
            IResource r1 = m1.getResource();
            IResource r2 = m2.getResource();
            String n1 = r1.getName();
            String n2 = r2.getName();
            int result = getComparator().compare(n1, n2);
            if (result == 0) {
				return compareColumnValue(m1, m2, depth + 1);
			}
            return result * direction;
        }
        case FOLDER: {
            /* container name */
            String c1 = MarkerUtil.getContainerName(m1);
            String c2 = MarkerUtil.getContainerName(m2);
            int result = c1.equals(c2) ? 0 : getComparator().compare(c1, c2);
            if (result == 0) {
				return compareColumnValue(m1, m2, depth + 1);
			}
            return result * direction;
        }
        case LOCATION: {
            /* line and location */
            int result = compareLineAndLocation(m1, m2);
            if (result == 0) {
				return compareColumnValue(m1, m2, depth + 1);
			}
            return result * direction;
        }
        case CREATION_TIME: {
            /* creation time */
            int result = compareCreationTime(m1, m2);
            if (result == 0) {
				return compareColumnValue(m1, m2, depth + 1);
			}
            return result * direction;
        }
        default:
            return 0;
        }
    }

    /**
     * Compares the creation time of two markers.
     */
    private int compareCreationTime(IMarker m1, IMarker m2) {
        long result;
        try {
            result = m1.getCreationTime() - m2.getCreationTime();
        } catch (CoreException e) {
            result = 0;
        }
        if (result > 0) {
			return 1;
		} else if (result < 0) {
			return -1;
		}
        return 0;
    }

    /**
     * Compares the line number and location of the two markers.
     * If line number is specified for both, this sorts first by line number (numerically), 
     * then by start offset (numerically), then by location (textually).
     * If line number is not specified for either, this sorts by location.
     * Otherwise, if only one has a line number, this sorts by the combined text for line number and location.
     */
    private int compareLineAndLocation(IMarker m1, IMarker m2) {
        int line1 = MarkerUtil.getLineNumber(m1);
        int line2 = MarkerUtil.getLineNumber(m2);
        if (line1 != -1 && line2 != -1) {
            if (line1 != line2) {
                return line1 - line2;
            }
            int start1 = MarkerUtil.getCharStart(m1);
            int start2 = MarkerUtil.getCharStart(m2);
            if (start1 != -1 && start2 != -1) {
                if (start1 != start2) {
                    return start1 - start2;
                }
            }
            String loc1 = MarkerUtil.getLocation(m1);
            String loc2 = MarkerUtil.getLocation(m2);
            return getComparator().compare(loc1, loc2);
        }
        if (line1 == -1 && line2 == -1) {
            String loc1 = MarkerUtil.getLocation(m1);
            String loc2 = MarkerUtil.getLocation(m2);
            return getComparator().compare(loc1, loc2);
        }
        String loc1 = MarkerUtil.getLineAndLocation(m1);
        String loc2 = MarkerUtil.getLineAndLocation(m2);
        return getComparator().compare(loc1, loc2);
    }

    /**
     * Returns the sort order for the given marker based on its category.
     * Lower numbers appear first.
     */
    private int getCategoryOrder(IMarker marker) {
        if (MarkerUtil.isMarkerType(marker, IMarker.PROBLEM)) {
            switch (MarkerUtil.getSeverity(marker)) {
            case IMarker.SEVERITY_ERROR:
                return 4;
            case IMarker.SEVERITY_WARNING:
                return 3;
            case IMarker.SEVERITY_INFO:
                return 2;
            }
        } else if (MarkerUtil.isMarkerType(marker, IMarker.TASK)) {
            return 1;
        }
        return 1000;
    }

    /**
     * Returns the sort order for the given marker based on its completion status.
     * Lower numbers appear first.
     */
    private int getCompletedOrder(IMarker marker) {
        if (MarkerUtil.isMarkerType(marker, IMarker.TASK)) {
			return MarkerUtil.isComplete(marker) ? 2 : 1;
		}
        return 0;
    }

    /**
     * Returns the sort order for the given marker based on its priority.
     */
    private int getPriorityOrder(IMarker marker) {
        if (MarkerUtil.isMarkerType(marker, IMarker.TASK)) {
			return MarkerUtil.getPriority(marker);
		}
        return -1;
    }

    public void saveState(IDialogSettings settings) {
        if (settings == null) {
			return;
		}

        for (int i = 0; i < directions.length; i++) {
            settings.put("direction" + i, directions[i]);//$NON-NLS-1$
            settings.put("priority" + i, priorities[i]);//$NON-NLS-1$
        }
    }

    public void restoreState(IDialogSettings settings) {
        if (settings == null) {
			return;
		}

        try {
            for (int i = 0; i < priorities.length; i++) {
                directions[i] = settings.getInt("direction" + i);//$NON-NLS-1$
                priorities[i] = settings.getInt("priority" + i);//$NON-NLS-1$
            }
        } catch (NumberFormatException e) {
            resetState();
        }
    }

}
