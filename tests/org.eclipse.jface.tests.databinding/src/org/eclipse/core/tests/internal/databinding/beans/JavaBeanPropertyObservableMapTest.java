/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 246103)
 *     Ovidio Mallo - bug 301774
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.MapChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 * 
 */
public class JavaBeanPropertyObservableMapTest extends
		AbstractDefaultRealmTestCase {
	public void testSetBeanProperty_CorrectForNullOldAndNewValues() {
		// The java bean spec allows the old and new values in a
		// PropertyChangeEvent to be null, which indicates that an unknown
		// change occured.

		// This test ensures that JavaBeanObservableValue fires the correct
		// value diff even if the bean implementor is lazy :-P

		Bean bean = new AnnoyingBean();
		bean.setMap(Collections.singletonMap("key", "old"));

		IObservableMap map = BeansObservables.observeMap(
				new CurrentRealm(true), bean, "map");
		MapChangeEventTracker tracker = MapChangeEventTracker.observe(map);

		bean.setMap(Collections.singletonMap("key", "new"));

		assertEquals(1, tracker.count);

		assertEquals(Collections.EMPTY_SET, tracker.event.diff.getAddedKeys());
		assertEquals(Collections.singleton("key"), tracker.event.diff
				.getChangedKeys());
		assertEquals(Collections.EMPTY_SET, tracker.event.diff.getRemovedKeys());

		assertEquals("old", tracker.event.diff.getOldValue("key"));
		assertEquals("new", tracker.event.diff.getNewValue("key"));
	}

	/**
	 * Makes sure that the map set on the Bean model after changing the
	 * observable map is modifiable (see bugs 285307 and 301774).
	 */
	public void testUpdatedBeanMapIsModifiable() {
		Bean bean = new Bean(new ArrayList());
		IObservableMap observable = BeansObservables.observeMap(bean, "map");

		observable.put(new Object(), new Object());
		bean.getMap().clear();
	}

	/**
	 * Makes sure that the map set on the Pojo model after changing the
	 * observable map is modifiable (see bugs 285307 and 301774).
	 */
	public void testUpdatedPojoMapIsModifiable() {
		Bean bean = new Bean(new ArrayList());
		IObservableMap observable = PojoObservables.observeMap(bean, "map");

		observable.put(new Object(), new Object());
		bean.getMap().clear();
	}
}
