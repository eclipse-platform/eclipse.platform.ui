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
public abstract class ConditionalUpdatableValue extends UpdatableValue {

	private final IUpdatableValue innerUpdatableValue;

	IChangeListener changeListener = new IChangeListener() {
		public void handleChange(IChangeEvent changeEvent) {
			fireChangeEvent(IChangeEvent.CHANGE, null, null);
		}
	};

	/**
	 * @param innerUpdatableValue
	 */
	public ConditionalUpdatableValue(IUpdatableValue innerUpdatableValue) {
		this.innerUpdatableValue = innerUpdatableValue;
		innerUpdatableValue.addChangeListener(changeListener);
	}

	public void setValue(Object value) {
		throw new UnsupportedOperationException();
	}

	public Object getValue() {
		Object currentValue = innerUpdatableValue.getValue();
		return new Boolean(compute(currentValue));
	}

	abstract protected boolean compute(Object currentValue);

	public Class getValueType() {
		return Boolean.class;
	}

	public void dispose() {
		super.dispose();
		innerUpdatableValue.removeChangeListener(changeListener);
	}

}
