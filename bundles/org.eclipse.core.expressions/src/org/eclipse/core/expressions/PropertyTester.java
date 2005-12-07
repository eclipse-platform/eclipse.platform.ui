/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.expressions;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.internal.expressions.PropertyTesterDescriptor;

import org.osgi.framework.Bundle;

/**
 * Abstract superclass of all property testers. Implementation classes of
 * the extension point <code>org.eclipse.core.expresssions.propertyTesters
 * </code> must extend <code>PropertyTester</code>.
 * <p>
 * A property tester implements the property tests enumerated in the property
 * tester extension point. For the following property test extension
 * <pre>
 *   &lt;propertyTester
 *     	 namespace="org.eclipse.jdt.core"
 *       id="org.eclipse.jdt.core.IPackageFragmentTester"
 *       properties="isDefaultPackage"
 *       type="org.eclipse.jdt.core.IPackageFragment"
 *       class="org.eclipse.demo.MyPackageFragmentTester"&gt;
 *     &lt;/propertyTester&gt;
 * </pre>
 * the corresponding implementation class looks like:
 * <pre>
 *   public class MyPackageFragmentTester {
 *       public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
 *           IPackageFragment fragement= (IPackageFragment)receiver;
 *	         if ("isDefaultPackage".equals(property)) { 
 *               return expectedValue == null
 *               	? fragement.isDefaultPackage()
 *               	: fragement.isDefaultPackage() == ((Boolean)expectedValue).booleanValue();
 *           }
 *           Assert.isTrue(false);
 *           return false;
 *       }
 *   }
 * </pre>
 * The property can then be used in a test expression as follows:
 * <pre>
 *   &lt;instanceof value="org.eclipse.core.IPackageFragment"/&gt;
 *   &lt;test property="org.eclipse.jdt.core.isDefaultPackage"/&gt;
 * </pre>
 * </p>
 * @since 3.0 
 */
public abstract class PropertyTester implements IPropertyTester {
	
	private IConfigurationElement fConfigElement;
	private String fNamespace;
	private String fProperties;
	
	/**
	 * Initialize the property tester with the given name space and property.
	 * <p>
	 * Note: this method is for internal use only. Clients must not call 
	 * this method.
	 * </p>
	 * @param descriptor the descriptor object for this tester
	 */
	public final void internalInitialize(PropertyTesterDescriptor descriptor) { 
		fProperties= descriptor.getProperties();
		fNamespace= descriptor.getNamespace();
		fConfigElement= descriptor.getConfigurationElement();
	}
	
	/**
	 * Note: this method is for internal use only. Clients must not call 
	 * this method.
	 * 
	 * @return the property tester descriptor
	 */
	public final PropertyTesterDescriptor internalCreateDescriptor() {
		return new PropertyTesterDescriptor(fConfigElement, fNamespace, fProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean handles(String namespace, String property) {
		return fNamespace.equals(namespace) && fProperties.indexOf("," + property + ",") != -1;  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final boolean isInstantiated() {
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isDeclaringPluginActive() {
		Bundle fBundle= Platform.getBundle(fConfigElement.getNamespace());
		return fBundle.getState() == Bundle.ACTIVE;		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final IPropertyTester instantiate() {
		return this;
	}
}
