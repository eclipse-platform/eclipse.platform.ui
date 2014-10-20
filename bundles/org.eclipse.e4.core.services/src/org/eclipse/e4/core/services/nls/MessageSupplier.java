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
 * Supplier interface that is used to bind a method of an object (e.g. a SWT
 * control) to a field of a Messages instance. Typically the instance field that
 * contains the localization value will be returned.
 * <p>
 * This is a functional interface whose functional method is {@link #get()}.
 * </p>
 * <p>
 * When updating to Java 8 this interface can be removed and replaced with the
 * general <code>java.util.function.Supplier</code>
 * </p>
 *
 * @since 2.0
 *
 */
public interface MessageSupplier {

	/**
	 *
	 * @return The value this {@link MessageSupplier} holds.
	 */
	String get();
}
