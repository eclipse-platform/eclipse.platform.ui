/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

/**
 * A property tester for various properties of resource mappings
 * 
 * @since 3.2
 */
public class ResourceMappingPropertyTester extends ResourcePropertyTester {
	public boolean test(Object receiver, String method, Object[] args, Object expectedValue) {
		if (!(receiver instanceof ResourceMapping))
			return false;
		if (!method.equals(PROJECT_PERSISTENT_PROPERTY))
			return false;
		//Note: we currently say the test is satisfied if any project associated
		//with the mapping satisfies the test.  
		IProject[] projects = ((ResourceMapping)receiver).getProjects();
		for (int i = 0; i < projects.length; i++) {
			//TODO this is currently not very efficient because the args are parsed each time
			if (testProperty(projects[i], true, args, expectedValue))
				return true;
		}
		return false;
	}
}
