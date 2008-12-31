/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	int getActive() {
		return getDefaultChangeActive();
	}

	PreviewNode[] doCreateChildren() {
		return EMPTY_CHILDREN;
	}
}