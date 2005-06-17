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

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.expressions.IPropertyTester;
import org.eclipse.core.expressions.PropertyTester;

public class TypeExtension {
	
	private static final TypeExtension[] EMPTY_TYPE_EXTENSION_ARRAY= new TypeExtension[0];

	/* a special property tester instance that used to signal that method searching has to continue */
	/* package */ static final IPropertyTester CONTINUE= new IPropertyTester() {
		public boolean handles(String namespace, String method) {
			return false;
		}
		public boolean isInstantiated() {
			return true;
		}
		public boolean isDeclaringPluginActive() {
			return true;
		}
		public IPropertyTester instantiate() {
			return this;
		}
		public boolean test(Object receiver, String method, Object[] args, Object expectedValue) {
			return false;
		}
	};
		
	/* a special type extension instance that marks the end of an evaluation chain */
	private static final TypeExtension END_POINT= new TypeExtension() {
		/* package */ IPropertyTester findTypeExtender(TypeExtensionManager manager, String namespace, String name, boolean staticMethod) throws CoreException {
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
	
	/* package */ IPropertyTester findTypeExtender(TypeExtensionManager manager, String namespace, String method, boolean staticMethod) throws CoreException {
		if (fExtenders == null) {
			fExtenders= manager.loadTesters(fType);
		}
		IPropertyTester result;
		
		// handle extenders associated with this type extender
		for (int i= 0; i < fExtenders.length; i++) {
			IPropertyTester extender= fExtenders[i];
			if (extender == null || !extender.handles(namespace, method))
				continue;
			if (extender.isInstantiated()) {
				if (extender.isDeclaringPluginActive()) {
					return extender;
				} else {
					PropertyTester tester= (PropertyTester)extender;
					fExtenders[i]= extender= tester.internalCreateDescriptor();
					return extender;
				}
			} else {
				if (extender.isDeclaringPluginActive()) {
					try {
						PropertyTesterDescriptor descriptor= (PropertyTesterDescriptor)extender;
						IPropertyTester inst= descriptor.instantiate();
						((PropertyTester)inst).internalInitialize(descriptor);
						fExtenders[i]= extender= inst;
						return extender;
					} catch (CoreException e) {
						fExtenders[i]= null;
						throw e;
					} catch (ClassCastException e) {
						fExtenders[i]= null;
						throw new CoreException(new ExpressionStatus(
							ExpressionStatus.TYPE_EXTENDER_INCORRECT_TYPE,
							ExpressionMessages.TypeExtender_incorrectType,  
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
		if (fExtends == null) {
			Class superClass= fType.getSuperclass();
			if (superClass != null) {
				fExtends= manager.get(superClass);
			} else {
				fExtends= END_POINT;
			}
		}
		result= fExtends.findTypeExtender(manager, namespace, method, staticMethod);
		if (result != CONTINUE)
			return result;
		
		// handle implements chain
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
		for (int i= 0; i < fImplements.length; i++) {
			result= fImplements[i].findTypeExtender(manager, namespace, method, staticMethod);
			if (result != CONTINUE)
				return result;
		}
		return CONTINUE;
	}
}
