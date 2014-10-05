/*******************************************************************************
 * Copyright (c) 2007, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	protected List list;
	protected Set set;
	protected Map map;
	protected Bean bean;

	public Bean() {
	}

	public Bean(String value) {
		this.value = value;
	}

	public Bean(Object[] array) {
		this.array = array;
	}

	public Bean(List list) {
		this.list = list;
	}

	public Bean(Set set) {
		this.set = set;
	}

	public Bean(Map map) {
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
	public List getList() {
		return list;
	}

	@Override
	public void setList(List list) {
		changeSupport.firePropertyChange("list", this.list, this.list = list);
	}

	@Override
	public Set getSet() {
		return set;
	}

	@Override
	public void setSet(Set set) {
		changeSupport.firePropertyChange("set", this.set, this.set = set);
	}

	@Override
	public Map getMap() {
		return map;
	}

	@Override
	public void setMap(Map map) {
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
