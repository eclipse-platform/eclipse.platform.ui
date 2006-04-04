/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.propertytester;

import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

/**
 * A property tester for various properties of files.
 * 
 * @since 3.2
 */
public class FilePropertyTester extends ResourcePropertyTester {

	/**
	 * A property indicating that we are looking to verify that the file matches
	 * the content type matching the given identifier. The identifier is
	 * provided as the expected value.
	 */
	private static final String CONTENT_TYPE_ID = "contentTypeId"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.resources.ResourcePropertyTester#test(java.lang.Object,
	 *      java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String method, Object[] args, Object expectedValue) {
		if ((receiver instanceof IFile) && method.equals(CONTENT_TYPE_ID))
			return testContentType((IFile) receiver, toString(expectedValue));
		return false;
	}

	/**
	 * Tests whether the content type for <code>file</code> matches the
	 * <code>contentTypeId</code>. It is possible that this method call could
	 * cause the file to be read. It is also possible (through poor plug-in
	 * design) for this method to load plug-ins.
	 * 
	 * @param file
	 *            The file for which the content type should be determined; must
	 *            not be <code>null</code>.
	 * @param contentTypeId
	 *            The expected content type; must not be <code>null</code>.
	 * @return <code>true</code> iff the best matching content type has an
	 *         identifier that matches <code>contentTypeId</code>;
	 *         <code>false</code> otherwise.
	 */
	private boolean testContentType(final IFile file, String contentTypeId) {
		final String expectedValue = contentTypeId.trim();

		String actualValue = null;
		try {
			IContentDescription contentDescription = file.getContentDescription();
			if (contentDescription != null) {
				IContentType contentType = contentDescription.getContentType();
				actualValue = contentType.getId();
			}
		} catch (CoreException e) {
			Policy.log(IStatus.ERROR, "Core exception while retrieving the content description", e);//$NON-NLS-1$
		}
		return expectedValue.equals(actualValue);
	}

}
