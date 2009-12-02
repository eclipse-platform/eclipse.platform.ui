/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.modeling;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

public abstract class ModelDeltaOperation {

	private final Object object;
	private final Object value;

	public ModelDeltaOperation(Object object, Object value) {
		Assert.isNotNull(object);
		this.object = object;
		this.value = value;
	}

	public Object getObject() {
		return object;
	}

	public Object getValue() {
		return value;
	}

	public abstract IStatus apply();

}
