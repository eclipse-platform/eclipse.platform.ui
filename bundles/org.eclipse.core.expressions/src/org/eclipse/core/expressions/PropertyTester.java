/*******************************************************************************
 * Copyright (c) 2000, 2004 International Business Machines Corp. and others.
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

/**
 * Abstract superclass of all property testers. Implementation classes of
 * the extension point <code>org.eclipse.core.expresssions.propertyTesters
 * </code> must extend <code>PropertyTester</code>.
 * <p>
 * A property tester implements the property tests enumerated in the property
 * tester extension point. For the following property test extension
 * <pre>
 *   <propertyTester
 *     	 namespace="org.eclipse.jdt.core"
 *       id="org.eclipse.jdt.core.IPackageFragmentTester"
 *       properties="isDefaultPackage"
 *       type="org.eclipse.jdt.core.IPackageFragment"
 *       class="org.eclipse.demo.MyPackageFragmentTester">
 *     </propertyTester>
 * </pre>
 * the corresponding implemenation class looks like:
 * <pre>
 *   public class MyPackageFragmentTester {
 *       public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
 *           IPackageFragment fragement= (IPackageFragment)receiver;
 *	         if ("isDefaultPackage".equals(method)) { 
 *               return fragement.isDefaultPackage();
 *           }
 *           Assert.isTrue(false);
 *           return false;
 *       }
 *   }
 * </pre>
 * The property can then be used in a test expression as follows:
 * <pre>
 *   <instanceof value="org.eclipse.core.IPackageFragment"/>
 *   <test property="org.eclipse.jdt.core.isDefaultPackage"/>
 * </pre>
 * </p>
 * @since 3.0 
 */
public abstract class PropertyTester implements IPropertyTester {
	
	private String fProperties;
	private String fNamespace;
	
	/**
	 * Initialize the property tester with the given name space and property.
	 * <p>
	 * Note: this method is for internal use only. Clients should not call 
	 * this method.
	 * </p>
	 * @param namespace the name space this tester belongs to
	 * @param property the properties this tester supports
	 */
	public final void initialize(String namespace, String properties) {
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
