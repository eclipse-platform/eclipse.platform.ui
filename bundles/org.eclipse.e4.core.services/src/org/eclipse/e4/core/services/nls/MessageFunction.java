/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.nls;

/**
 * Function interface that is used to bind a property of an object (e.g. a SWT
 * control) to a field of a Messages instance. This interface is intended to be
 * used with implementations that use Java 8, so it is possible to operate using
 * method references and lambdas.
 * <p>
 * This is a functional interface whose functional method is {@link #apply(M)}.
 * </p>
 * <p>
 * When updating to Java 8 this interface can be removed and replaced with the
 * general <code>java.util.function.Function</code>
 * </p>
 *
 * @since 2.0
 *
 * @param <M>
 *            the message class type
 */
public interface MessageFunction<M> {

	/**
	 *
	 * @param m
	 *            The message instance from which the value should be retrieved.
	 * @return The message value out of the given messages instance.
	 */
	String apply(M m);
}
