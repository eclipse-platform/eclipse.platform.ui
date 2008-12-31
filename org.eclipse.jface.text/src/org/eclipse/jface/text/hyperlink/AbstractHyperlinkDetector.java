/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.hyperlink;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;


/**
 * A hyperlink detector that can provide adapters through
 * a context that can be set by the creator of this hyperlink
 * detector.
 * <p>
 * Clients may subclass.
 * </p>
 *
 * @since 3.3
 */
public abstract class AbstractHyperlinkDetector implements IHyperlinkDetector, IHyperlinkDetectorExtension {

	/**
	 * The context of this hyperlink detector.
	 */
	private IAdaptable fContext;

	/**
	 * Sets this hyperlink detector's context which
	 * is responsible to provide the adapters.
	 *
	 * @param context the context for this hyperlink detector
	 * @throws IllegalArgumentException if the context is <code>null</code>
	 * @throws IllegalStateException if this method is called more than once
	 */
	public final void setContext(IAdaptable context) throws IllegalStateException, IllegalArgumentException {
		Assert.isLegal(context != null);
		if (fContext != null)
			throw new IllegalStateException();
		fContext= context;
	}

	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetectorExtension#dispose()
	 */
	public void dispose() {
		fContext= null;
	}

	/**
	 * Returns an object which is an instance of the given class
	 * and provides additional context for this hyperlink detector.
	 *
	 * @param adapterClass the adapter class to look up
	 * @return an instance that can be cast to the given class,
	 *			or <code>null</code> if this object does not
	 *			have an adapter for the given class
	 */
	protected final Object getAdapter(Class adapterClass) {
		Assert.isLegal(adapterClass != null);
		if (fContext != null)
			return fContext.getAdapter(adapterClass);
		return null;
	}

}
