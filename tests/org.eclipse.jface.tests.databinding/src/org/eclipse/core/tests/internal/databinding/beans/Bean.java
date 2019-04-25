/*******************************************************************************
 * Copyright (c) 2007, 2009 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 221351, 256150, 264619
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple Java Bean for testing.
 *
 * @since 3.3
 */
public class Bean implements IBean {
	protected PropertyChangeSupport changeSupport = new PropertyChangeSupport(
			this);
	protected String value;
	protected Object[] array;
	protected List<Object> list;
	protected Set<Object> set;
	protected Map<Object, Object> map;
	protected Bean bean;

	public Bean() {
	}

	public Bean(String value) {
		this.value = value;
	}

	public Bean(Object[] array) {
		this.array = array;
	}

	public Bean(List<Object> list) {
		this.list = list;
	}

	public Bean(Set<Object> set) {
		this.set = set;
	}

	public Bean(Map<Object, Object> map) {
		this.map = map;
	}

	public Bean(Bean bean) {
		this.bean = bean;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		changeSupport.firePropertyChange("value", this.value,
				this.value = value);
	}

	@Override
	public Object[] getArray() {
		return array;
	}

	@Override
	public void setArray(Object[] array) {
		changeSupport.firePropertyChange("array", this.array,
				this.array = array);
	}

	@Override
	public List<Object> getList() {
		return list;
	}

	@Override
	public void setList(List<Object> list) {
		changeSupport.firePropertyChange("list", this.list, this.list = list);
	}

	@Override
	public Set<Object> getSet() {
		return set;
	}

	@Override
	public void setSet(Set<Object> set) {
		changeSupport.firePropertyChange("set", this.set, this.set = set);
	}

	@Override
	public Map<Object, Object> getMap() {
		return map;
	}

	@Override
	public void setMap(Map<Object, Object> map) {
		changeSupport.firePropertyChange("map", this.map, this.map = map);
	}

	public Bean getBean() {
		return bean;
	}

	public void setBean(Bean bean) {
		changeSupport.firePropertyChange("bean", this.bean, this.bean = bean);
	}

	public boolean hasListeners(String propertyName) {
		return changeSupport.hasListeners(propertyName);
	}
}
