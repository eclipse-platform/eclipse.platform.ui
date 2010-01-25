/*******************************************************************************
 * Copyright (c) 2008, 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.core.internal.resources.projectvariables;

import org.eclipse.core.resources.variableresolvers.PathVariableResolver;

import org.eclipse.core.filesystem.URIUtil;

import java.net.URI;

import org.eclipse.core.resources.IResource;

import java.util.*;

/**
 * Path Variable representing the parent directory of the variable provided
 * in argument, following the syntax:
 * 
 * "${PARENT-COUNT-MyVariable}"
 *
 */
public class ParentVariableResolver extends PathVariableResolver {

	final public static String NAME = "PARENT"; //$NON-NLS-1$

	public ParentVariableResolver() {
		// nothing
	}

	public Object[] getExtensions(String variable, IResource resource) {
		LinkedList result = new LinkedList();
		Iterator it = Arrays.asList(resource.getProject().getPathVariableManager().getPathVariableNames()).iterator();
		while(it.hasNext()) {
			String value = (String) it.next();
			if (!value.equals("PARENT"))  		//$NON-NLS-1$
				result.add("1-" + value); 	//$NON-NLS-1$
		}
		return result.toArray();
	}

	public String getValue(String variable, IResource resource) {
		int index = variable.indexOf('-');
		if (index == -1 || index == (variable.length() -1))
			return null;
		
		String countRemaining = variable.substring(index + 1);
		index = countRemaining.indexOf('-');
		if (index == -1 || index == (variable.length() -1))
			return null;

		String countString = countRemaining.substring(0, index);
		int count = 0;
		try {
			count = Integer.parseInt(countString);
			if (count < 0)
				return null;
		}catch (NumberFormatException e) {
			return null;
		}
		String argument = countRemaining.substring(index + 1);
		
		URI value = resource.getProject().getPathVariableManager().getValue(argument, resource);
		if (value == null)
			return null;
		value = resource.getProject().getPathVariableManager().resolveURI(value, resource);
		value = URIUtil.toURI(URIUtil.toPath(value).removeLastSegments(count));
			
		return value.toASCIIString();
	}
}
