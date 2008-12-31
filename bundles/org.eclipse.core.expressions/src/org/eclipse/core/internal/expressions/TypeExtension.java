/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import org.eclipse.core.expressions.IPropertyTester;
import org.eclipse.core.expressions.PropertyTester;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

public class TypeExtension {

	private static final TypeExtension[] EMPTY_TYPE_EXTENSION_ARRAY= new TypeExtension[0];

	/* a special property tester instance that is used to signal that method searching has to continue */
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
		/* package */ IPropertyTester findTypeExtender(TypeExtensionManager manager, String namespace, String name, boolean staticMethod, boolean forcePluginActivation) throws CoreException {
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

	/* package */ IPropertyTester findTypeExtender(TypeExtensionManager manager, String namespace, String method, boolean staticMethod, boolean forcePluginActivation) throws CoreException {
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
				// There is no need to check for an active plug-in here. If a plug-in
				// gets uninstalled we receive an registry event which will flush the whole
				// type extender cache and will reinstantiate the testers. However Bundle#stop
				// isn't handled by this. According to bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=130338
				// we don't have to support stop in 3.2. If we have to in the future we have to
				// reactivate the stopped plug-in if we are in forcePluginActivation mode.
				return extender;
			} else {
				if (extender.isDeclaringPluginActive() || forcePluginActivation) {
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
		result= fExtends.findTypeExtender(manager, namespace, method, staticMethod, forcePluginActivation);
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
			result= fImplements[i].findTypeExtender(manager, namespace, method, staticMethod, forcePluginActivation);
			if (result != CONTINUE)
				return result;
		}
		return CONTINUE;
	}
}
