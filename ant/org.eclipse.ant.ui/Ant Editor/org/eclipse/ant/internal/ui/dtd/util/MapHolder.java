/*******************************************************************************
 * Copyright (c) 2002, 2005 Object Factory Inc.
 *
 * This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *		Object Factory Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.dtd.util;

/**
 * @author Bob Foster
 */
public class MapHolder extends SortedMapFactory implements IMapHolder {

	public Object[] keys;
	public Object[] values;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ant.internal.ui.dtd.util.IKeyHolder#getKeys()
	 */
	@Override
	public Object[] getKeys() {
		return keys;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ant.internal.ui.dtd.util.IKeyHolder#setKeys(java.lang.Object[])
	 */
	@Override
	public void setKeys(Object[] keys) {
		this.keys = keys;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ant.internal.ui.dtd.util.IValueHolder#getValues()
	 */
	@Override
	public Object[] getValues() {
		return values;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ant.internal.ui.dtd.util.IValueHolder#setValues(java.lang.Object[])
	 */
	@Override
	public void setValues(Object[] values) {
		this.values = values;
	}
}
