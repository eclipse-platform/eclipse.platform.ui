/**
 *  Copyright (c) 2017 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide CodeMining support with CodeMiningManager - Bug 527720
 */
package org.eclipse.jface.text.codemining;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;

/**
 * A codemining provider that can provide adapters through a context that can be set by the creator
 * of this codemining provider.
 * <p>
 * Clients may subclass.
 * </p>
 *
 * @since 3.13
 */
public abstract class AbstractCodeMiningProvider implements ICodeMiningProvider {

	/**
	 * The context of this codemining provider.
	 */
	private IAdaptable context;

	/**
	 * Sets this codemining provider's context which is responsible to provide the adapters.
	 *
	 * @param context the context for this codemining provider
	 * @throws IllegalArgumentException if the context is <code>null</code>
	 * @throws IllegalStateException if this method is called more than once
	 */
	public final void setContext(IAdaptable context) throws IllegalStateException, IllegalArgumentException {
		Assert.isLegal(context != null);
		if (this.context != null)
			throw new IllegalStateException();
		this.context= context;
	}

	@Override
	public void dispose() {
		context= null;
	}

	/**
	 * Returns an object which is an instance of the given class and provides additional context for
	 * this codemining provider.
	 *
	 * @param adapterClass the adapter class to look up
	 * @return an instance that can be cast to the given class, or <code>null</code> if this object
	 *         does not have an adapter for the given class
	 */
	protected final <T> T getAdapter(Class<T> adapterClass) {
		Assert.isLegal(adapterClass != null);
		if (context != null)
			return context.getAdapter(adapterClass);
		return null;
	}
}
