/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A hierarchical rule based on IPath.  A path rule contains another path
 * rule if its path is a prefix of the other rule's path.  A path rule is conflicting
 * with another rule if either one is a prefix of the other.
 */
public class PathRule implements ISchedulingRule {
	private IPath path;

	public PathRule(IPath path) {
		this.path = path;
	}

	public PathRule(String pathString) {
		this.path = new Path(pathString);
	}

	public boolean contains(ISchedulingRule rule) {
		if (this == rule)
			return true;
		if (!(rule instanceof PathRule))
			return false;
		return path.isPrefixOf(((PathRule) rule).getFullPath());
	}

	public boolean isConflicting(ISchedulingRule rule) {
		if (!(rule instanceof PathRule))
			return false;
		IPath otherPath = ((PathRule) rule).getFullPath();
		return path.isPrefixOf(otherPath) || otherPath.isPrefixOf(path);
	}

	public IPath getFullPath() {
		return path;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "PathRule(" + path + ")";
	}
}
