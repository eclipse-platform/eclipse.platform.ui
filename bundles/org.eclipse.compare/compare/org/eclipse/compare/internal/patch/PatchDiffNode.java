/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.*;

public abstract class PatchDiffNode extends DiffNode {

	private Object fElement;

	public PatchDiffNode(Object patchElement, IDiffContainer parent, int kind,
			ITypedElement ancestor, ITypedElement left, ITypedElement right) {
		super(parent, kind, ancestor, left, right);
		fElement = patchElement;
	}

	public PatchDiffNode(Object patchElement, IDiffContainer parent, int kind) {
		super(parent, kind);
		fElement = patchElement;
	}

	public boolean isEnabled() {
		return getPatcher().isEnabled(getPatchElement());
	}

	public void setEnabled(boolean enabled) {
		getPatcher().setEnabled(getPatchElement(), enabled);
	}

	public Object getPatchElement() {
		return fElement;
	}

	protected abstract Patcher getPatcher();

}
