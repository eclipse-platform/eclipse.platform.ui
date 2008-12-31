/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.rulers;

import org.eclipse.core.runtime.Assert;

/**
 * Describes one placement constraint of a contribution to the
 * <code>org.eclipse.ui.texteditor.rulerColumns</code> extension point.
 *
 * @since 3.3
 */
public final class RulerColumnPlacementConstraint {
	private final String fId;
	private final boolean fBefore;

	/**
	 * Creates a new constraint.
	 *
	 * @param id the id of the referenced contribution
	 * @param before <code>true</code> if the specifying should come <i>before</i>,
	 *        <code>false</code> if it should come <i>after</i> the contribution referenced by
	 *        id.
	 */
	RulerColumnPlacementConstraint(String id, boolean before) {
		Assert.isLegal(id != null);
		fId= id;
		fBefore= before;
	}

	/**
	 * Returns the identifier of the referenced column contribution.
	 *
	 * @return the identifier of the referenced column contribution
	 */
	public String getId() {
		return fId;
	}

	/**
	 * Returns <code>true</code> if the receiver is a <i>before</i> constraint,
	 * <code>false</code> if it is an <i>after</i> constraint.
	 *
	 * @return <code>true</code> if the receiver is a <i>before</i> constraint,
	 *         <code>false</code> if it is an <i>after</i> constraint
	 */
	public boolean isBefore() {
		return fBefore;
	}
}
