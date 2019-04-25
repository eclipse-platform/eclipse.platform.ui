/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	public List<Object> getList();

	public void setList(List<Object> list);

	public Set<Object> getSet();

	public void setSet(Set<Object> set);

	public Map<Object, Object> getMap();

	public void setMap(Map<Object, Object> map);
}