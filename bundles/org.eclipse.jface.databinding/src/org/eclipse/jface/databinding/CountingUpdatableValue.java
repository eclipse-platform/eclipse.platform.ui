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
package org.eclipse.jface.databinding;

import java.util.List;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
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
