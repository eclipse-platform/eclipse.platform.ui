/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.propertytester;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/**
 * A property tester for various properties of resource mappings
 * 
 * @since 3.2
 */
public class ResourceMappingPropertyTester extends ResourcePropertyTester {
	@Override
	public boolean test(Object receiver, String method, Object[] args, Object expectedValue) {
		if (!(receiver instanceof ResourceMapping))
			return false;
		if (!method.equals(PROJECT_PERSISTENT_PROPERTY))
			return false;
		//Note: we currently say the test is satisfied if any project associated
		//with the mapping satisfies the test.  
		IProject[] projects = ((ResourceMapping) receiver).getProjects();
		if (projects.length == 0)
			return false;
		String propertyName;
		String expectedVal;
		if (args.length == 0) {
			propertyName = toString(expectedValue);
			expectedVal = null;//any value will do
		} else if (args.length == 1) {
			propertyName = toString(args[0]);
			expectedVal = null;//any value will do
		} else {
			propertyName = toString(args[0]);
			expectedVal = toString(args[1]);
		}
		QualifiedName key = toQualifedName(propertyName);
		boolean found = false;
		for (int i = 0; i < projects.length; i++) {
			try {
				Object actualVal = projects[i].getPersistentProperty(key);
				//the value is not set, so keep looking on other projects
				if (actualVal == null)
					continue;
				//record that we have found at least one value
				found = true;
				//expected value of null means we expect *any* value, rather than expecting no value
				if (expectedVal == null)
					continue;
				//if the value we find does not match, then the property is not satisfied
				if (!expectedVal.equals(actualVal.toString()))
					return false;
			} catch (CoreException e) {
				// ignore
			}
		}
		//if any projects had the property set, the condition is satisfied
		return found;
	}
}
