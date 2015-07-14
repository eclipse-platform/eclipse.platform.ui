/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.LinkedList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.workbench.modeling.IDelta;

public class CompositeDelta implements IDelta {

	private final Object object;

	private final LinkedList<IDelta> deltas = new LinkedList<>();

	public CompositeDelta(Object object) {
		this.object = object;
	}

	public void add(IDelta delta) {
		deltas.add(delta);
	}

	@Override
	public IStatus apply() {
		for (IDelta delta : deltas) {
			delta.apply();
		}
		return Status.OK_STATUS;
	}

	@Override
	public Object getObject() {
		return object;
	}

}
