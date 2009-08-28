/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 221704)
 *     Matthew Hall - bug 223114, 226289, 247875, 246782, 249526, 268022,
 *                    251424
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable.masterdetail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.ObservableTracker;
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
			if (isDisposed())
				return;
			ObservableTracker.setIgnore(true);
			try {
				Map oldMap = new HashMap(wrappedMap);
				updateDetailMap();
				fireMapChange(Diffs.computeMapDiff(oldMap, wrappedMap));
			} finally {
				ObservableTracker.setIgnore(false);
			}
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
		Assert.isTrue(!master.isDisposed(), "Master observable is disposed"); //$NON-NLS-1$

		this.master = master;
		this.detailFactory = detailFactory;
		this.detailKeyType = keyType;
		this.detailValueType = valueType;

		master.addDisposeListener(new IDisposeListener() {
			public void handleDispose(DisposeEvent staleEvent) {
				dispose();
			}
		});

		ObservableTracker.setIgnore(true);
		try {
			updateDetailMap();
		} finally {
			ObservableTracker.setIgnore(false);
		}
		master.addValueChangeListener(masterChangeListener);
	}

	private void updateDetailMap() {
		final Object masterValue = master.getValue();
		if (detailMap != null) {
			detailMap.removeMapChangeListener(detailChangeListener);
			detailMap.dispose();
		}

		if (masterValue == null) {
			detailMap = null;
			wrappedMap = Collections.EMPTY_MAP;
		} else {
			ObservableTracker.setIgnore(true);
			try {
				detailMap = (IObservableMap) detailFactory
						.createObservable(masterValue);
			} finally {
				ObservableTracker.setIgnore(false);
			}
			DetailObservableHelper.warnIfDifferentRealms(getRealm(), detailMap
					.getRealm());
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

	public Object put(final Object key, final Object value) {
		ObservableTracker.setIgnore(true);
		try {
			return detailMap.put(key, value);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	public void putAll(final Map map) {
		ObservableTracker.setIgnore(true);
		try {
			detailMap.putAll(map);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	public Object remove(final Object key) {
		ObservableTracker.setIgnore(true);
		try {
			return detailMap.remove(key);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	public void clear() {
		ObservableTracker.setIgnore(true);
		try {
			detailMap.clear();
		} finally {
			ObservableTracker.setIgnore(false);
		}
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
