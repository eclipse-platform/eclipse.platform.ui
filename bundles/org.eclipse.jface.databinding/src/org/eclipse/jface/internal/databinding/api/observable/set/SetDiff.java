/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.api.observable.set;

import java.util.Collections;
import java.util.Set;

/**
 * @since 3.2
 *
 */
public final class SetDiff implements ISetDiff {

	private Set additions;
	private Set removals;

	/**
	 * @param additions
	 * @param removals
	 */
	public SetDiff(Set additions, Set removals) {
		super();
		this.additions = Collections.unmodifiableSet(additions);
		this.removals = Collections.unmodifiableSet(removals);
	}

	public Set getAdditions() {
		return additions;
	}

	public Set getRemovals() {
		return removals;
	}

}
