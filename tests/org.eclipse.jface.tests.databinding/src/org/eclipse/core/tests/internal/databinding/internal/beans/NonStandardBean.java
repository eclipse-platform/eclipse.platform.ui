/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.internal.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Simple non-standard (java.util.List-based property) Java Bean for testing.
 * 
 * @since 3.3
 */
public class NonStandardBean {
	/* package */PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	private String value;
	private List list = new ArrayList();
	private Set set;

	public NonStandardBean() {
	}

	public NonStandardBean(String value) {
		this.value = value;
	}

	public NonStandardBean(List list) {
		this.list = list;
	}

	public NonStandardBean(Set set) {
		this.set = set;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		changeSupport.firePropertyChange("value", this.value, this.value = value);
	}

	public List getList() {
		return list;
	}

	public void setList(List list) {
		changeSupport.firePropertyChange("list", this.list, this.list = list);
	}

	public Set getSet() {
		return set;
	}

	public void setSet(Set set) {
		changeSupport.firePropertyChange("set", this.set, this.set = set);
	}
}
