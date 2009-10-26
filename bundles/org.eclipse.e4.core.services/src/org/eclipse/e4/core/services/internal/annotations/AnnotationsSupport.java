/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.eclipse.e4.core.services.internal.context.InjectionProperties;

/**
 * Placeholder for annotations support to be replaced by a fragment.
 */
public class AnnotationsSupport {

	static public InjectionProperties getInjectProperties(Field field) {
		return null;
	}

	static public InjectionProperties getInjectProperties(Method method) {
		return null;
	}

	static public InjectionProperties getInjectProperties(Class type) {
		return null;
	}

	static public boolean isPostConstruct(Method method) {
		return false;
	}

	static public boolean isPreDestory(Method method) {
		return false;
	}

}
