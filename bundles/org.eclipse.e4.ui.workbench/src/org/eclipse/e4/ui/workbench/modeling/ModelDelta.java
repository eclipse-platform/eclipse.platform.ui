/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.modeling;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Assert;

/**
 * @noreference This class is not intended to be referenced by clients.
 * @since 1.0
 */
public abstract class ModelDelta implements IDelta {

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

	protected Object convert(Object value) {
		if (value instanceof IDelta) {
			IDelta delta = (IDelta) value;
			delta.apply();
			return delta.getObject();
		} else if (value instanceof List<?>) {
			List<?> values = (List<?>) value;
			List<Object> objects = new ArrayList<Object>(values.size());
			for (int i = 0; i < values.size(); i++) {
				Object object = values.get(i);
				if (object instanceof IDelta) {
					IDelta delta = (IDelta) object;
					delta.apply();
					object = delta.getObject();
				}
				objects.add(object);
			}

			return objects;
		}

		return value;
	}

	public Object getAttributeValue() {
		return convert(attributeValue);
	}

}
