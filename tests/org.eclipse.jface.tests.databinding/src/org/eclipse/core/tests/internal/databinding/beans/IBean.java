/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 256150)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since 3.2
 *
 */
public interface IBean {
	public String getValue();

	public void setValue(String value);

	public Object[] getArray();

	public void setArray(Object[] array);

	public List getList();

	public void setList(List list);

	public Set getSet();

	public void setSet(Set set);

	public Map getMap();

	public void setMap(Map map);
}