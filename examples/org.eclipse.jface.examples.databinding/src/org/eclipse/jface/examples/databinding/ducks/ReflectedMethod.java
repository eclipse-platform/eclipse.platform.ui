/*
 * Copyright (C) 2005, 2006 db4objects Inc.  http://www.db4o.com
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 */
package org.eclipse.jface.examples.databinding.ducks;

import java.lang.reflect.Method;

/**
 * ReflectedMethod.  Encapsulates a method that may or may not exist on
 * some receiver.  Invocation policy is that if the method can be invoked,
 * it is.  On failure, returns null.
 *
 * @author djo
 */
public class ReflectedMethod {

    private Object subject;
    private Method method;

    /**
     * Constructor ReflectedMethod.  Create a ReflectedMethod object.
     *
     * @param subject The object on which the method lives.
     * @param methodName The name of the method.
     * @param paramTypes The method's parameter types.
     */
    public ReflectedMethod(Object subject, String methodName, Class[] paramTypes) {
        this.subject = subject;
        method = null;
        try {
        	method = subject.getClass().getMethod(methodName, paramTypes);
        } catch (Exception e) {
        	System.out.println(e);
        }
    }

    /**
     * Method exists.  Returns true if the underlying method exists, false
     * otherwise.
     *
     * @return true if the underlying method exists, false otherwise.
     */
    public boolean exists() {
        return method != null;
    }

    /**
     * Method invoke.  If possible, invoke the encapsulated method with the
     * specified parameters.
     *
     * @param params An Object[] containing the parameters to pass.
     * @return any return value or null if there was no return value or an
     * error occured.
     */
    public Object invoke(Object[] params) {
        if (method == null)
            return null;
        try {
        	if (!method.isAccessible()) {
        		method.setAccessible(true);
        	}
        	return method.invoke(subject, params);
        } catch (Exception e) {
            return null;
        }
    }

	/**
	 * Method getType.  Returns the return type of the method.
	 *
	 * @return The return type or null if none.
	 */
	public Class getType() {
		return method.getReturnType();
	}
}


