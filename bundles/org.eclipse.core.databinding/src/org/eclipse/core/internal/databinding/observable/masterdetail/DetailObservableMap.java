/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 221704)
 *     Matthew Hall - bug 223114, 226289
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable.masterdetail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.ObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Assert;

/**
 * @since 1.1
 * 
 */
public class DetailObservableMap extends ObservableMap implements IObserving {
	private boolean updating = false;

	private IObservableValue master;
	private IObservableFactory detailFactory;

	private IObservableMap detailMap;

	private Object detailKeyType;
	private Object detailValueType;

	private IValueChangeListener masterChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			Map oldMap = new HashMap(wrappedMap);
			updateDetailMap();
			fireMapChange(Diffs.computeMapDiff(oldMap, wrappedMap));
		}
	};

	private IMapChangeListener detailChangeListener = new IMapChangeListener() {
		public void handleMapChange(MapChangeEvent event) {
			if (!updating) {
				fireMapChange(event.diff);
			}
		}
	};

	/**
	 * Constructs a new DetailObservableMap
	 * 
	 * @param detailFactory
	 *            observable factory that creates IObservableMap instances given
	 *            the current value of master observable value
	 * @param master
	 * @param keyType
	 * @param valueType
	 * 
	 */
	public DetailObservableMap(IObservableFactory detailFactory,
			IObservableValue master, Object keyType, Object valueType) {
		super(master.getRealm(), Collections.EMPTY_MAP);
		this.master = master;
		this.detailFactory = detailFactory;

		updateDetailMap();
		master.addValueChangeListener(masterChangeListener);
	}

	private void updateDetailMap() {
		Object masterValue = master.getValue();
		if (detailMap != null) {
			detailMap.removeMapChangeListener(detailChangeListener);
			detailMap.dispose();
		}

		if (masterValue == null) {
			detailMap = null;
			wrappedMap = Collections.EMPTY_MAP;
		} else {
			detailMap = (IObservableMap) detailFactory
					.createObservable(masterValue);
			wrappedMap = detailMap;

			if (detailKeyType != null) {
				Object innerKeyType = detailMap.getKeyType();

				Assert.isTrue(detailKeyType.equals(innerKeyType),
						"Cannot change key type in a nested observable map"); //$NON-NLS-1$
			}

			if (detailValueType != null) {
				Object innerValueType = detailMap.getValueType();

				Assert.isTrue(detailValueType.equals(innerValueType),
						"Cannot change value type in a nested observable map"); //$NON-NLS-1$
			}

			detailMap.addMapChangeListener(detailChangeListener);
		}
	}

	public Object getKeyType() {
		return detailKeyType;
	}

	public Object getValueType() {
		return detailValueType;
	}

	public Object put(Object key, Object value) {
		return detailMap.put(key, value);
	}

	public void putAll(Map map) {
		detailMap.putAll(map);
	}

	public Object remove(Object key) {
		return detailMap.remove(key);
	}

	public void clear() {
		detailMap.clear();
	}

	public synchronized void dispose() {
		if (master != null) {
			master.removeValueChangeListener(masterChangeListener);
			master = null;
			masterChangeListener = null;
		}
		detailFactory = null;
		if (detailMap != null) {
			detailMap.removeMapChangeListener(detailChangeListener);
			detailMap.dispose();
			detailMap = null;
		}
		detailChangeListener = null;
		super.dispose();
	}

	public Object getObserved() {
		if (detailMap instanceof IObserving) {
			return ((IObserving) detailMap).getObserved();
		}
		return null;
	}
}
