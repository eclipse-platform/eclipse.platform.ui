/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.util.HashMap;
import java.util.Map;

/**
 * A concrete implementation of the {@link IDecorationContext} interface,
 * suitable for instantiating.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * @since 3.2
 */
public class DecorationContext implements IDecorationContext {
	
	/**
	 * Constant that defines a default decoration context that has
	 * no context ids associated with it.
	 */
	public static final IDecorationContext DEFAULT_CONTEXT = new DecorationContext();
	
	private String[] contextIds;
	private Map properties = new HashMap();

	/**
	 * Create a decoration context.
	 */
	public DecorationContext() {
		this.contextIds = new String[0];
	}
	
	/**
	 * Create a decoration context with the given context ids
	 * @param contextIds the context ids for this decoration context
	 * @deprecated to be removed by M5
	 */
	public DecorationContext(String[] contextIds) {
		this.contextIds = contextIds;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDecorationContext#getContextIds()
	 */
	public String[] getContextIds() {
		return contextIds;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDecorationContext#getProperty(java.lang.String)
	 */
	public Object getProperty(String property) {
		return properties.get(property);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDecorationContext#getProperties()
	 */
	public String[] getProperties() {
		return (String[]) properties.keySet().toArray(new String[properties.size()]);
	}

	/**
	 * Set the pgiven roperty to the given value. Setting the value of
	 * a propert to <code>null</code> removes the property from
	 * the context.
	 * @param property the property
	 * @param value the value of the property or <code>null</code>
	 * if the property is to be removed.
	 */
	public void putProperty(String property, Object value) {
		if (value == null) {
			properties.remove(property);
		} else {
			properties.put(property, value);
		}
	}
}
