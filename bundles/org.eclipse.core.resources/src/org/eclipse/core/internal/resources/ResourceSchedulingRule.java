/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A scheduling rule based on resource ancestry.
 * @see WorkspaceJob.newSchedulingRule
 */
public class ResourceSchedulingRule implements ISchedulingRule {
	private final IResource resource;
	ResourceSchedulingRule(IResource resource) {
		this.resource = resource;
	}
	IResource getResource() {
		return resource;
	}
	public boolean isConflicting(ISchedulingRule rule) {
		if (rule.getClass() != ResourceSchedulingRule.class)
			return false;
		IPath myPath = resource.getFullPath();
		IPath otherPath = ((ResourceSchedulingRule)rule).resource.getFullPath();
		return myPath.isPrefixOf(otherPath) || otherPath.isPrefixOf(myPath);
	}
	/*
	 * For debugging purposes only.
	 */
	 public String toString()  {
	 	return "ResourceRule(" + resource + ')'; //$NON-NLS-1$
	 }
}