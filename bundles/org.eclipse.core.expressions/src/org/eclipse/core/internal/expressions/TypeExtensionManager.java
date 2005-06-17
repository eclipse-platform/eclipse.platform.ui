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
package org.eclipse.core.internal.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.expressions.IPropertyTester;

public class TypeExtensionManager implements IRegistryChangeListener {
	
	private String fExtensionPoint; 
	
	private static final String TYPE= "type"; //$NON-NLS-1$
	
	private static final IPropertyTester[] EMPTY_PROPERTY_TESTER_ARRAY= new IPropertyTester[0];
	
	private static final IPropertyTester NULL_PROPERTY_TESTER= new IPropertyTester() {
		public boolean handles(String namespace, String property) {
			return false;
		}
		public boolean isInstantiated() {
			return true;
		}
		public boolean isDeclaringPluginActive() {
			return true;
		}
		public IPropertyTester instantiate() throws CoreException {
			return this;
		}
		public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
			return false;
		}
	};
	
	/*
	 * Map containing all already created type extension object. 
	 */
	private Map/*<Class, TypeExtension>*/ fTypeExtensionMap;
	
	/*
	 * Table containing mapping of class name to configuration element 
	 */
	private Map/*<String, List<IConfigurationElement>>*/ fConfigurationElementMap;
	
	/*
	 * A cache to give fast access to the last 1000 method invocations.
	 */
	private PropertyCache fPropertyCache;
	
	
	public TypeExtensionManager(String extensionPoint) {
		Assert.isNotNull(extensionPoint);
		fExtensionPoint= extensionPoint;
		Platform.getExtensionRegistry().addRegistryChangeListener(this);
		initializeCaches();
	}

	public synchronized Property getProperty(Object receiver, String namespace, String method) throws CoreException  {
		long start= 0;
		if (Expressions.TRACING)
			start= System.currentTimeMillis();
		
		// if we call a static method than the receiver is the class object
		Class clazz= receiver instanceof Class ? (Class)receiver : receiver.getClass();
		Property result= new Property(clazz, namespace, method);
		Property cached= fPropertyCache.get(result);
		if (cached != null) {
			if (cached.isValidCacheEntry()) {
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
		IPropertyTester extender= extension.findTypeExtender(this, namespace, method, receiver instanceof Class);
		if (extender == TypeExtension.CONTINUE || extender == null) {
			throw new CoreException(new ExpressionStatus(
				ExpressionStatus.TYPE_EXTENDER_UNKOWN_METHOD,
				Messages.format(
					ExpressionMessages.TypeExtender_unknownMethod,  //$NON-NLS-1$
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
	
	/*
	 * This method doesn't need to be synchronized since it is called
	 * from withing the getProperty method which is synchronized
	 */
	/* package */ TypeExtension get(Class clazz) {
		TypeExtension result= (TypeExtension)fTypeExtensionMap.get(clazz);
		if (result == null) {
			result= new TypeExtension(clazz);
			fTypeExtensionMap.put(clazz, result);
		}
		return result;
	}
	
	/*
	 * This method doesn't need to be synchronized since it is called
	 * from withing the getProperty method which is synchronized
	 */
	/* package */ IPropertyTester[] loadTesters(Class type) {
		if (fConfigurationElementMap == null) {
			fConfigurationElementMap= new HashMap();
			IExtensionRegistry registry= Platform.getExtensionRegistry();
			IConfigurationElement[] ces= registry.getConfigurationElementsFor(
				ExpressionPlugin.getPluginId(), 
				fExtensionPoint); 
			for (int i= 0; i < ces.length; i++) {
				IConfigurationElement config= ces[i];
				String typeAttr= config.getAttribute(TYPE);
				List typeConfigs= (List)fConfigurationElementMap.get(typeAttr);
				if (typeConfigs == null) {
					typeConfigs= new ArrayList();
					fConfigurationElementMap.put(typeAttr, typeConfigs);
				}
				typeConfigs.add(config);
			}
		}
		String typeName= type.getName();
		List typeConfigs= (List)fConfigurationElementMap.get(typeName);
		if (typeConfigs == null)
			return EMPTY_PROPERTY_TESTER_ARRAY;
		else {
			IPropertyTester[] result= new IPropertyTester[typeConfigs.size()];
			for (int i= 0; i < result.length; i++) {
				IConfigurationElement config= (IConfigurationElement)typeConfigs.get(i);
				try {
					result[i]= new PropertyTesterDescriptor(config);
				} catch (CoreException e) {
					ExpressionPlugin.getDefault().getLog().log(e.getStatus());
					result[i]= NULL_PROPERTY_TESTER;
				}
			}
			fConfigurationElementMap.remove(typeName);
			return result;
		}
	}
	
	public void registryChanged(IRegistryChangeEvent event) {
		IExtensionDelta[] deltas= event.getExtensionDeltas(ExpressionPlugin.getPluginId(), fExtensionPoint);
		if (deltas.length > 0) {
			initializeCaches();
		}
	}
	
	private synchronized void initializeCaches() {
		fTypeExtensionMap= new HashMap();
		fConfigurationElementMap= null;
		fPropertyCache= new PropertyCache(1000);
	}
}
