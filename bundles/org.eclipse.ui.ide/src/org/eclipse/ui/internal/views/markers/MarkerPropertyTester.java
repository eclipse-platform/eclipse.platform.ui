/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.actions.SimpleWildcardTester;

/**
 * The property tester used to test the attributes of the Marker
 * 
 * @since 3.5
 * @author Prakash G.R.
 * 
 */
public class MarkerPropertyTester extends PropertyTester {

	/**
	 * An attribute indicating the marker priority (value
	 * <code>"priority"</code>). The attribute value in xml must be one of
	 * <code>IMarker.PRIORITY_LOW, 
	 * IMarker.PRIORITY_NORMAL, or IMarker.PRIORITY_HIGH</code>
	 */
	public static final String PRIORITY = IMarker.PRIORITY;

	/**
	 * An attribute indicating the marker severity (value
	 * <code>"severity"</code>). The attribute value in xml in xml must be one
	 * of <code>IMarker.SEVERITY_ERROR, 
	 * IMarker.SEVERITY_WARNING, or IMarker.SEVERITY_INFO</code>
	 */
	public static final String SEVERITY = IMarker.SEVERITY;

	/**
	 * An attribute indicating whether the marker is considered done (value
	 * <code>"done"</code>). The attribute value in xml must be one of
	 * <code>"true" or "false"</code>.
	 */
	public static final String DONE = IMarker.DONE;

	/**
	 * An attribute indicating the marker message (value <code>"message"</code>
	 * ). The attribute value in xml is unconstrained. "*" may be used at the
	 * start or the end to represent "one or more characters".
	 */
	public static final String MESSAGE = IMarker.MESSAGE;

	/**
	 * An attribute indicating the marker type (value <code>"type"</code>). The
	 * attribute value in xml should match one of the marker types defined in
	 * the workbench's marker extension point. Common examples are
	 * <code>IMarker.TASK, IMarker.BOOKMARK, and IMarker.MARKER</code>.
	 */

	public static final String TYPE = "type"; //$NON-NLS-1$

	/**
	 * An attribute indicating the marker super type (value
	 * <code>"superType"</code>). The attribute value in xml should match one of
	 * the marker types defined in the workbench's marker extension point.
	 * Common examples are
	 * <code>IMarker.TASK, IMarker.BOOKMARK, and IMarker.MARKER</code>.
	 */
	public static final String SUPER_TYPE = "superType"; //$NON-NLS-1$

	/**
	 * An attribute indicating the type of resource associated with the marker
	 * (value <code>"resourceType"</code>). The attribute value in xml must be
	 * one of <code>IResource.FILE, IResource.PROJECT, IResource.FOLDER,
	 * or IResource.ROOT</code>.
	 */
	public static final String RESOURCE_TYPE = "resourceType"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkerPropertyTester() {
	}

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		return test(((IMarker) receiver), property, expectedValue.toString());
	}

	/**
	 * Returns whether the specific integer attribute matches a value.
	 */
	private static boolean testIntegerAttribute(IMarker marker,
			String attrName, String value) {
		Integer i1, i2;
		try {
			i1 = (Integer) marker.getAttribute(attrName);
			if (i1 == null) {
				return false;
			}
		} catch (CoreException e) {
			return false;
		}
		try {
			i2 = Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return i1.equals(i2);
	}

	/**
	 * Tests the attribute's value for the given marker
	 * 
	 * @param marker
	 *            - The marker
	 * @param attributeName
	 *            - Name of the attribute
	 * @param expectedValue
	 *            - Value to test for
	 * @return - true, if the value is same as expected, false otherwise
	 */
	public static boolean test(IMarker marker, String attributeName,
			String expectedValue) {

		if (TYPE.equals(attributeName)) {
			try {
				return expectedValue.equals(marker.getType());
			} catch (CoreException e) {
				return false;
			}
		} else if (SUPER_TYPE.equals(attributeName)) {
			try {
				return marker.isSubtypeOf(expectedValue);
			} catch (CoreException e) {
				return false;
			}
		} else if (PRIORITY.equals(attributeName)) {
			return testIntegerAttribute(marker, IMarker.PRIORITY, expectedValue);
		} else if (SEVERITY.equals(attributeName)) {
			return testIntegerAttribute(marker, IMarker.SEVERITY, expectedValue);
		} else if (MESSAGE.equals(attributeName)) {
			try {
				String msg = (String) marker.getAttribute(IMarker.MESSAGE);
				if (msg == null) {
					return false;
				}
				return SimpleWildcardTester.testWildcardIgnoreCase(
						expectedValue, msg);
			} catch (CoreException e) {
				return false;
			}
		} else if (DONE.equals(attributeName)) {
			try {
				expectedValue = expectedValue.toLowerCase();
				Boolean done = (Boolean) marker.getAttribute(IMarker.DONE);
				if (done == null) {
					return false;
				}
				return (done.booleanValue() == expectedValue.equals("true"));//$NON-NLS-1$
			} catch (CoreException e) {
				return false;
			}
		} else if (RESOURCE_TYPE.equals(attributeName)) {
			int desiredType = 0;

			try {
				desiredType = Integer.parseInt(expectedValue);
			} catch (NumberFormatException eNumberFormat) {
			}

			if (!(desiredType == IResource.FILE
					|| desiredType == IResource.FOLDER
					|| desiredType == IResource.PROJECT || desiredType == IResource.ROOT)) {
				return false;
			}

			return (marker.getResource().getType() & desiredType) > 0;
		}
		return false;
	}

}
