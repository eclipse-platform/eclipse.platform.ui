/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.dependencies;

import org.eclipse.core.internal.runtime.Assert;

class Dependency implements IDependency {
	private int changedMark;
	private Object requiredObjectId;
	private Object requiredVersionId;
	private IMatchRule matchRule;
	private boolean optional;
	private Object resolvedVersionId;
	public Dependency(Object requiredObjectId, IMatchRule matchRule, Object requiredVersionId, boolean optional) {
		Assert.isNotNull(requiredObjectId);
		Assert.isNotNull(matchRule);
		this.requiredObjectId = requiredObjectId;
		this.requiredVersionId = requiredVersionId;
		this.matchRule = requiredVersionId == null ? new UnspecifiedVersionMatchRule() : matchRule;
		this.optional = optional;
	}
	public IMatchRule getMatchRule() {
		return this.matchRule;
	}
	public Object getRequiredObjectId() {
		return this.requiredObjectId;
	}
	public Object getRequiredVersionId() {
		return this.requiredVersionId;
	}
	public boolean isOptional() {
		return this.optional;
	}
	public Object getResolvedVersionId() {
		return resolvedVersionId;
	}
	void setResolvedVersionId(Object resolvedVersionId, int changedMark) {
		if ((resolvedVersionId == null && this.resolvedVersionId == null) || (resolvedVersionId != null && resolvedVersionId.equals(this.resolvedVersionId)))
			return;
		this.resolvedVersionId = resolvedVersionId;
		this.changedMark = changedMark;	
	}
	int getChangedMark() {
		return changedMark;
	}	
	public String toString() {
		return " -> " + getRequiredObjectId() + "_" + getRequiredVersionId() + " (" + getMatchRule() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	class UnspecifiedVersionMatchRule implements IMatchRule {
		public boolean isSatisfied(Object required, Object available) {
			return true;
		}
	}
}
