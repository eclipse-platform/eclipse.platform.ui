/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

public class TypeExtensionManager {
	
	private String fExtensionPoint; 
	
	/*
	 * Map containing all already instanciated type extension object. Key is
	 * of type <code>Class</code>, value is of type <code>TypeExtension</code>. 
	 */
	private final Map/*<Class, TypeExtension>*/ fTypeExtensionMap= new HashMap();
	
	/*
	 * A cache to give fast access to the last 1000 method invocations.
	 */
	private final PropertyCache fPropertyCache= new PropertyCache(1000);
	
	
	public TypeExtensionManager(String extensionPoint) {
		Assert.isNotNull(extensionPoint);
		fExtensionPoint= extensionPoint;
	}

	public Property getProperty(Object receiver, String namespace, String method) throws CoreException  {
		long start= 0;
		if (Expressions.TRACING)
			start= System.currentTimeMillis();
		
		// if we call a static method than the receiver is the class object
		Class clazz= receiver instanceof Class ? (Class)receiver : receiver.getClass();
		Property result= new Property(clazz, namespace, method);
		Property cached= fPropertyCache.get(result);
		if (cached != null) {
			if (cached.isLoaded() || !cached.canLoad()) {
				if (Expressions.TRACING) {
					System.out.println("[Type Extension] - method " + //$NON-NLS-1$
						clazz.getName() + "#" + method + //$NON-NLS-1$
						" found in cache: " +  //$NON-NLS-1$
						(System.currentTimeMillis() - start) + " ms."); //$NON-NLS-1$
				}
				return cached;
			}
			// The type extender isn't loaded in the cached method but can be loaded
			// now. So remove method from cache and do the normal look up so that the
			// implementation class gets loaded.
			fPropertyCache.remove(cached);
		}
		TypeExtension extension= get(clazz);
		IPropertyTester extender= extension.findTypeExtender(this, namespace, method, fExtensionPoint, receiver instanceof Class);
		if (extender == TypeExtension.CONTINUE || extender == null) {
			throw new CoreException(new ExpressionStatus(
				ExpressionStatus.TYPE_EXTENDER_UNKOWN_METHOD,
				ExpressionMessages.getFormattedString(
					"TypeExtender.unknownMethod",  //$NON-NLS-1$
					new Object[] {method, clazz.toString()})));
		}
		result.setPropertyTester(extender);
		fPropertyCache.put(result);
		if (Expressions.TRACING) {
			System.out.println("[Type Extension] - method " + //$NON-NLS-1$
				clazz.getName() + "#" + method + //$NON-NLS-1$
				" not found in cache: " +  //$NON-NLS-1$
				(System.currentTimeMillis() - start) + " ms."); //$NON-NLS-1$
		}
		return result;
	}
	
	public TypeExtension get(Class clazz) {
		synchronized(fTypeExtensionMap) {
			TypeExtension result= (TypeExtension)fTypeExtensionMap.get(clazz);
			if (result == null) {
				result= new TypeExtension(clazz);
				fTypeExtensionMap.put(clazz, result);
			}
			return result;
		}
	}	
}
