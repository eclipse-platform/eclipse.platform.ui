/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.preferences;

/**
 * @since 3.1
 */
public abstract class AbstractIntegerListener extends AbstractPropertyListener {

	private IDynamicPropertyMap map;
	private int defaultValue;
	private String propertyId;

	public AbstractIntegerListener() {
	}

	public void attach(IDynamicPropertyMap map, String propertyId, int defaultValue) {
		this.defaultValue = defaultValue;
		this.propertyId = propertyId;
		if (this.map != null) {
			this.map.removeListener(this);
		}

		this.map = map;

		if (this.map != null) {
			this.map.addListener(new String[] { propertyId }, this);
		}
	}

	@Override
	protected void update() {
		handleValue(PropertyUtil.get(map, propertyId, defaultValue));
	}

	/**
	 * @param b
	 * @since 3.1
	 */
	protected abstract void handleValue(int b);

}
