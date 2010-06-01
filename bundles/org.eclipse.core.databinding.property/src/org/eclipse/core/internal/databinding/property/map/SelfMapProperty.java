/*******************************************************************************
 * Copyright (c) 2009, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 263868)
 *     Matthew Hall - bug 268203
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.map;

import java.util.Map;

import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.map.SimpleMapProperty;

/**
 * @since 3.3
 * 
 */
public final class SelfMapProperty extends SimpleMapProperty {
	private final Object keyType;
	private final Object valueType;

	/**
	 * @param keyType
	 * @param valueType
	 */
	public SelfMapProperty(Object keyType, Object valueType) {
		this.keyType = keyType;
		this.valueType = valueType;
	}

	public Object getKeyType() {
		return keyType;
	}

	public Object getValueType() {
		return valueType;
	}

	protected Map doGetMap(Object source) {
		return (Map) source;
	}

	protected void doSetMap(Object source, Map map, MapDiff diff) {
		doUpdateMap(source, diff);
	}

	protected void doUpdateMap(Object source, MapDiff diff) {
		diff.applyTo((Map) source);
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return null; // no listener API
	}

	protected void doAddListener(Object source, INativePropertyListener listener) {
	}

	protected void doRemoveListener(Object source,
			INativePropertyListener listener) {
	}
}