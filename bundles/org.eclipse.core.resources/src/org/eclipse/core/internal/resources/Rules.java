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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

/**
 * Class for calculating scheduling rules for various resource operations.
 */
class Rules {
	private Rules() {
	}
	public static ISchedulingRule copyRule(IResource source, IResource destination) {
		//source is not modified, destination is created
		return parent(destination);
	}
	public static ISchedulingRule deleteRule(IResource resource) {
		return parent(resource);
	}
	private static ISchedulingRule parent(IResource resource) {
		switch (resource.getType()) {
			case IResource.ROOT:
			case IResource.PROJECT:
				return resource;
			default:
				return resource.getParent();
		}
	}
	public static ISchedulingRule moveRule(IResource source, IResource destination) {
		//move needs the parent of both source and destination
		ISchedulingRule r1 = parent(source);
		ISchedulingRule r2 = parent(destination);
		if (r1.equals(r2))
			return r1;
		return new MultiRule(new ISchedulingRule[] {r1, r2});
	}
}
