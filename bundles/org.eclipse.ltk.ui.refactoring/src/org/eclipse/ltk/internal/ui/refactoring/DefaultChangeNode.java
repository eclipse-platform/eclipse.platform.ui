/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.ltk.core.refactoring.Change;

public class DefaultChangeNode extends AbstractChangeNode {

	public DefaultChangeNode(PreviewNode parent, Change change) {
		super(parent, change);
	}

	@Override
	int getActive() {
		return getDefaultChangeActive();
	}

	@Override
	PreviewNode[] doCreateChildren() {
		return EMPTY_CHILDREN;
	}
}