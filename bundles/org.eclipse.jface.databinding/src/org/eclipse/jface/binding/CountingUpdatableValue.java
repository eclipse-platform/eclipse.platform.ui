/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.binding;

import java.util.List;

/**
 * @since 3.2
 * 
 */
public class CountingUpdatableValue extends UpdatableValue {

	private final IUpdatableValue updatableList;

	private final int offset;

	/**
	 * @param updatableList
	 * @param offset
	 */
	public CountingUpdatableValue(IUpdatableValue updatableList, int offset) {
		this.updatableList = updatableList;
		this.offset = offset;
		if (!List.class.isAssignableFrom(updatableList.getValueType())) {
			throw new IllegalArgumentException();
		}
		updatableList.addChangeListener(new IChangeListener() {

			public void handleChange(IChangeEvent changeEvent) {
				fireChangeEvent(IChangeEvent.CHANGE, null, null);
			}
		});
	}

	public void setValue(Object value) {
		throw new UnsupportedOperationException();
	}

	public Object getValue() {
		return new Integer(((List) updatableList.getValue()).size() + offset);
	}

	public Class getValueType() {
		return Integer.class;
	}

}
