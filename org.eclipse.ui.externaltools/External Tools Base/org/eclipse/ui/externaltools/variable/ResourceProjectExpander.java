package org.eclipse.ui.externaltools.variable;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IResource;

/**
 * Expands a resource's project type variable into the desired
 * result format.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class ResourceProjectExpander extends ResourceExpander {

	/**
	 * Create an instance
	 */
	public ResourceProjectExpander() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on ResourceExpander.
	 */
	/*package*/ IResource expandUsingContext(ExpandVariableContext context) {
		return context.getProject();
	}
	
	/* (non-Javadoc)
	 * Method declared on ResourceExpander.
	 */
	/*package*/ IResource expandToMember(String varValue) {
		IResource member = super.expandToMember(varValue);
		if (member != null)
			return member.getProject();
		else
			return null;
	}
}
