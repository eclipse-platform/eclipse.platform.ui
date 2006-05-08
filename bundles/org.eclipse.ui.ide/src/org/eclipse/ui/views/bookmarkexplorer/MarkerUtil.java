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

import com.ibm.icu.text.DateFormat;
import java.util.Date;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Utility class for accessing marker attributes.
 */
class MarkerUtil {

    /**
     * Don't allow instantiation.
     */
    private MarkerUtil() {
    }

    /**
     * Returns the ending character offset of the given marker.
     */
    static int getCharEnd(IMarker marker) {
        return marker.getAttribute(IMarker.CHAR_END, -1);
    }

    /**
     * Returns the starting character offset of the given marker.
     */
    static int getCharStart(IMarker marker) {
        return marker.getAttribute(IMarker.CHAR_START, -1);
    }

    /**
     * Returns the container name if it is defined, or empty string if not.
     */
    static String getContainerName(IMarker marker) {
        IPath path = marker.getResource().getFullPath();
        int n = path.segmentCount() - 1; // n is the number of segments in container, not path
        if (n <= 0) {
			return ""; //$NON-NLS-1$
		}
        int len = 0;
        for (int i = 0; i < n; ++i) {
			len += path.segment(i).length();
		}
        // account for /'s
        if (n > 1) {
			len += n - 1;
		}
        StringBuffer sb = new StringBuffer(len);
        for (int i = 0; i < n; ++i) {
            if (i != 0) {
				sb.append('/');
			}
            sb.append(path.segment(i));
        }
        return sb.toString();
    }

    /**
     * Returns the line number of the given marker.
     */
    static int getLineNumber(IMarker marker) {
        return marker.getAttribute(IMarker.LINE_NUMBER, -1);
    }

    /**
     * Returns the text for the location field.
     */
    static String getLocation(IMarker marker) {
        return marker.getAttribute(IMarker.LOCATION, "");//$NON-NLS-1$
    }

    /**
     * Returns the message attribute of the given marker,
     * or the empty string if the message attribute is not defined.
     */
    static String getMessage(IMarker marker) {
        return marker.getAttribute(IMarker.MESSAGE, "");//$NON-NLS-1$
    }

    /**
     * Returns the numeric value of the given string, which is assumed to represent a numeric value.
     *
     * @return <code>true</code> if numeric, <code>false</code> if not
     */
    static int getNumericValue(String value) {
        boolean negative = false;
        int i = 0;
        int len = value.length();

        // skip any leading '#'
        // workaround for 1GCE69U: ITPJCORE:ALL - Java problems should not have '#' in location.
        if (i < len && value.charAt(i) == '#') {
			++i;
		}

        if (i < len && value.charAt(i) == '-') {
            negative = true;
            ++i;
        }

        int result = 0;
        while (i < len) {
            int digit = Character.digit(value.charAt(i++), 10);
            if (digit < 0) {
                return result;
            }
            result = result * 10 + digit;
        }
        if (negative) {
            result = -result;
        }
        return result;
    }

    /**
     * Implements IProvider interface by supporting a number of
     * properties required for visual representation of markers
     * in the tasklist.
     */

    /**
     * Returns name if it is defined, or
     * blank string if not.
     */
    static String getResourceName(IMarker marker) {
        return marker.getResource().getName();
    }

    /**
     * Returns the creation time of the marker as a string.
     */
    static String getCreationTime(IMarker marker) {
        try {
            return DateFormat.getDateTimeInstance(DateFormat.LONG,
                    DateFormat.MEDIUM).format(
                    new Date(marker.getCreationTime()));
        } catch (CoreException e) {
            return null;
        }
    }
}
