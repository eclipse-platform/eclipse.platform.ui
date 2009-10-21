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
package org.eclipse.e4.core.services.internal.context;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;

/**
 * Context injection implementation. See the class comment of {@link ContextInjectionFactory} for
 * details on the injection algorithm.
 */
public class ContextInjectionImpl implements IContextConstants {

	final protected String fieldPrefix;
	final protected int fieldPrefixLength;
	/**
	 * We keep one injector per context.
	 */
	private Map injectors = new HashMap(); // IEclipseContext -> injector

	final protected String setMethodPrefix;

	public ContextInjectionImpl() {
		this(INJECTION_FIELD_PREFIX, INJECTION_SET_METHOD_PREFIX);
	}

	public ContextInjectionImpl(String fieldPrefix, String setMethodPrefix) {
		this.fieldPrefix = (fieldPrefix != null) ? fieldPrefix : INJECTION_FIELD_PREFIX;
		this.setMethodPrefix = (setMethodPrefix != null) ? setMethodPrefix
				: INJECTION_SET_METHOD_PREFIX;

		fieldPrefixLength = this.fieldPrefix.length();
	}

	synchronized public void injectInto(final Object userObject, final IEclipseContext context) {
		ContextToObjectLink link;
		synchronized (injectors) {
			if (injectors.containsKey(context))
				link = (ContextToObjectLink) injectors.get(context);
			else {
				link = new ContextToObjectLink(context, fieldPrefix, setMethodPrefix);
				injectors.put(context, link);
			}
		}
		context.runAndTrack(link, new Object[] { userObject });
	}

	public static Object invoke(final Object userObject, final String methodName,
			final IEclipseContext context, final Object defaultValue) {
		return ContextToObjectLink.processInvoke(userObject, methodName, context, defaultValue);
	}
}
