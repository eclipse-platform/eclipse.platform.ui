/*******************************************************************************
 * Copyright (c) 2002, 2003 Object Factory Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *		Object Factory Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.ant.ui.internal.dtd.util;

/**
 * @author Bob Foster
 */
public class MapHolder extends SortedMapFactory implements IMapHolder {

	public Object[] keys;
	public Object[] values;
	
	/**
	 * @see com.objfac.util.IKeyHolder#getKeys()
	 */
	public Object[] getKeys() {
		return keys;
	}

	/**
	 * @see com.objfac.util.IKeyHolder#setKeys(Object[])
	 */
	public void setKeys(Object[] keys) {
		this.keys = keys;
	}

	/**
	 * @see com.objfac.util.IValueHolder#getValues()
	 */
	public Object[] getValues() {
		return values;
	}

	/**
	 * @see com.objfac.util.IValueHolder#setValues(Object[])
	 */
	public void setValues(Object[] values) {
		this.values = values;
	}

}
