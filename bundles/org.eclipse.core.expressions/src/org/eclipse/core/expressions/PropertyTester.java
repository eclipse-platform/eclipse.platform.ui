/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.core.expressions;

import org.eclipse.core.internal.expressions.Assert;
import org.eclipse.core.internal.expressions.IPropertyTester;

/**
 * Abstract superclass of all type extenders.
 * 
 * @since 3.0 
 */
public abstract class PropertyTester implements IPropertyTester {
	
	private String fProperties;
	private String fNamespace;
	
	/**
	 * Initializes the property tester whith the given namespace and property.
	 * <p>
	 * Note: this method is for internal use only. Clients should not call 
	 * this method.
	 * </p>
	 * @param namespace the namespace this tester belongs to
	 * @param property the properties this tester supports
	 */
	public void initialize(String namespace, String properties) {
		Assert.isNotNull(properties);
		Assert.isNotNull(namespace);
		fProperties= properties;
		fNamespace= namespace;
	}
	
	/* (non-Javadoc)
	 */
	public final boolean handles(String namespace, String property) {
		return fNamespace.equals(namespace) && fProperties.indexOf("," + property + ",") != -1;  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	/* (non-Javadoc)
	 */
	public final boolean isLoaded() {
		return true;
	}
	
	/* (non-Javadoc)
	 */
	public final boolean canLoad() {
		return true;
	}
	
	/* (non-Javadoc)
	 */
	public IPropertyTester load() {
		return this;
	}
}
