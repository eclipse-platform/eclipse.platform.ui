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
package org.eclipse.core.internal.expressions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.expressions.PropertyTester;

public class TypeExtension {
	
	private static final String TYPE= "type"; //$NON-NLS-1$
	private static final IPropertyTester[] EMPTY_TYPE_EXTENDER_ARRAY= new IPropertyTester[0];
	private static final TypeExtension[] EMPTY_TYPE_EXTENSION_ARRAY= new TypeExtension[0];

	/* a special property tester instance that used to signal that method searching has to continue */
	/* package */ static final IPropertyTester CONTINUE= new IPropertyTester() {
		public boolean handles(String namespace, String method) {
			return false;
		}
		public boolean isLoaded() {
			return true;
		}
		public boolean canLoad() {
			return true;
		}
		public IPropertyTester load() {
			return this;
		}
		public boolean test(Object receiver, String method, Object[] args, Object expectedValue) {
			return false;
		}
	};
		
	/* a special type extension instance that marks the end of an evaluation chain */
	private static final TypeExtension END_POINT= new TypeExtension() {
		/* package */ IPropertyTester findTypeExtender(TypeExtensionManager manager, String namespace, String name, String extPoint, boolean staticMethod) throws CoreException {
			return CONTINUE;
		}
	};
		
	/* the type this extension is extending */
	private Class fType;
	/* the list of associated extenders */
	private IPropertyTester[] fExtenders;
	
	/* the extension associated with <code>fType</code>'s super class */
	private TypeExtension fExtends;
	/* the extensions associated with <code>fTypes</code>'s interfaces */ 
	private TypeExtension[] fImplements;
	
	private TypeExtension() {
		// special constructor to create the CONTINUE instance
	}
	
	/* package */ TypeExtension(Class type) {
		Assert.isNotNull(type);
		fType= type;
	}
	
	/* package */ IPropertyTester findTypeExtender(TypeExtensionManager manager, String namespace, String method, String extPoint, boolean staticMethod) throws CoreException {
		synchronized (this) {
			if (fExtenders == null)
				initialize(extPoint);
		}
		IPropertyTester result;
		
		// handle extenders associated with this interface
		for (int i= 0; i < fExtenders.length; i++) {
			IPropertyTester extender= fExtenders[i];
			if (extender == null || !extender.handles(namespace, method))
				continue;
			if (extender.isLoaded()) {
				return extender;
			} else {
				if (extender.canLoad()) {
					try {
						PropertyTesterDescriptor descriptor= (PropertyTesterDescriptor)extender;
						IPropertyTester inst= descriptor.load();
						((PropertyTester)inst).initialize(descriptor.getNamespace(), descriptor.getProperties());
						synchronized (fExtenders) {
							fExtenders[i]= extender= inst;
						}
						return extender;
					} catch (CoreException e) {
						synchronized (fExtenders) {
							fExtenders[i]= null;
						}
						throw e;
					} catch (ClassCastException e) {
						synchronized (fExtenders) {
							fExtenders[i]= null;
						}
						throw new CoreException(new ExpressionStatus(
							ExpressionStatus.TYPE_EXTENDER_INCORRECT_TYPE,
							ExpressionMessages.getString("TypeExtender.incorrectType"),  //$NON-NLS-1$
							e));
					}
				} else {
					return extender;
				}
			}
		}
		
		// there is no inheritance for static methods
		if (staticMethod) 
			return CONTINUE;
		
		// handle extends chain
		synchronized (this) {
			if (fExtends == null) {
				Class superClass= fType.getSuperclass();
				if (superClass != null) {
					fExtends= manager.get(superClass);
				} else {
					fExtends= END_POINT;
				}
			}
		}
		result= fExtends.findTypeExtender(manager, namespace, method, extPoint, staticMethod);
		if (result != CONTINUE)
			return result;
		
		// handle implements chain
		synchronized (this) {
			if (fImplements == null) {
				Class[] interfaces= fType.getInterfaces();
				if (interfaces.length == 0) {
					fImplements= EMPTY_TYPE_EXTENSION_ARRAY;
				} else {
					fImplements= new TypeExtension[interfaces.length];
					for (int i= 0; i < interfaces.length; i++) {
						fImplements[i]= manager.get(interfaces[i]);
					}				
				}
			}
		}
		for (int i= 0; i < fImplements.length; i++) {
			result= fImplements[i].findTypeExtender(manager, namespace, method, extPoint, staticMethod);
			if (result != CONTINUE)
				return result;
		}
		return CONTINUE;
	}
	
	private void initialize(String extPoint) {
		IPluginRegistry registry= Platform.getPluginRegistry();
		IConfigurationElement[] ces= registry.getConfigurationElementsFor(
			ExpressionPlugin.getPluginId(), 
			extPoint); 
		String fTypeName= fType.getName();
		List result= new ArrayList(2);
		for (int i= 0; i < ces.length; i++) {
			IConfigurationElement config= ces[i];
			if (fTypeName.equals(config.getAttribute(TYPE)))
				result.add(new PropertyTesterDescriptor(config));
		}
		if (result.size() == 0)
			fExtenders= EMPTY_TYPE_EXTENDER_ARRAY;
		else
			fExtenders= (IPropertyTester[])result.toArray(new IPropertyTester[result.size()]);
	}
}
