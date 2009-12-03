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

public abstract class ModelDelta {

	private final Object object;
	private final String attributeName;
	private final Object attributeValue;

	public ModelDelta(Object object, String attributeName, Object attributeValue) {
		Assert.isNotNull(object);
		Assert.isNotNull(attributeName);

		this.object = object;
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
	}

	public Object getObject() {
		return object;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public Object getAttributeValue() {
		return attributeValue;
	}

	public abstract IStatus apply();

}
