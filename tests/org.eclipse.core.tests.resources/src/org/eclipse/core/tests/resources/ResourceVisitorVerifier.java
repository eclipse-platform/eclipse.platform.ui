/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.util.*;
import junit.framework.Assert;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public class ResourceVisitorVerifier extends Assert implements IResourceVisitor {
	Set expected;
	StringBuffer message;
	boolean success = true;
	boolean verified = false;

	public ResourceVisitorVerifier() {
		super();
		reset();
	}

	public void addExpected(IResource resource) {
		expected.add(resource);
	}

	public void addExpected(IResource[] resources) {
		for (int i = 0; i < resources.length; i++)
			expected.add(resources[i]);
	}

	public boolean visit(IResource resource) throws CoreException {
		boolean included = expected.remove(resource);
		if (!included) {
			success = false;
			log(resource.getFullPath() + " was not expected.");
		}
		return true;
	}

	private void log(String text) {
		message.append("\n" + text);
	}

	private void verify() {
		if (verified)
			return;
		// Add messages for the resources which weren't visited but were expected.
		for (Iterator i = expected.iterator(); i.hasNext();) {
			success = false;
			log(((IResource) i.next()).getFullPath() + " was not visited.");
		}
		verified = true;
	}

	public boolean isValid() {
		verify();
		return success;
	}

	public String getMessage() {
		return message.toString();
	}

	public void reset() {
		expected = new HashSet();
		message = new StringBuffer();
		verified = false;
	}
}
