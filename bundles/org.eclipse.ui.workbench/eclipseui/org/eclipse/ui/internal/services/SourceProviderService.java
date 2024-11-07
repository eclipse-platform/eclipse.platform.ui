/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * <p>
 * A service holding all of the registered source providers.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 *
 * @since 3.2
 */
public final class SourceProviderService implements ISourceProviderService, IDisposable {

	/**
	 * The source providers registered with this service. This value is never
	 * <code>null</code>. This is a map of the source name ({@link String}) to the
	 * source provider ({@link ISourceProvider}).
	 */
	private final Map<String, ISourceProvider> sourceProvidersByName = new HashMap<>();

	/**
	 * All of the source providers registered with this service. This value is never
	 * <code>null</code>.
	 */
	private final Set<ISourceProvider> sourceProviders = new HashSet<>();

	private IServiceLocator locator;

	public SourceProviderService(final IServiceLocator locator) {
		this.locator = locator;
	}

	@Override
	public void dispose() {
		final Iterator<ISourceProvider> sourceProviderItr = sourceProviders.iterator();
		while (sourceProviderItr.hasNext()) {
			final ISourceProvider sourceProvider = sourceProviderItr.next();
			sourceProvider.dispose();
		}
		sourceProviders.clear();
		sourceProvidersByName.clear();
	}

	@Override
	public ISourceProvider getSourceProvider(final String sourceName) {
		return sourceProvidersByName.get(sourceName);
	}

	@Override
	public ISourceProvider[] getSourceProviders() {
		return sourceProviders.toArray(new ISourceProvider[sourceProviders.size()]);
	}

	public void registerProvider(final ISourceProvider sourceProvider) {
		if (sourceProvider == null) {
			throw new NullPointerException("The source provider cannot be null"); //$NON-NLS-1$
		}

		for (final String sourceName : sourceProvider.getProvidedSourceNames()) {
			sourceProvidersByName.put(sourceName, sourceProvider);
		}
		sourceProviders.add(sourceProvider);
	}

	public void unregisterProvider(ISourceProvider sourceProvider) {
		if (sourceProvider == null) {
			throw new NullPointerException("The source provider cannot be null"); //$NON-NLS-1$
		}

		for (String sourceName : sourceProvider.getProvidedSourceNames()) {
			sourceProvidersByName.remove(sourceName);
		}
		sourceProviders.remove(sourceProvider);
	}

	public void readRegistry() {
		for (AbstractSourceProvider sourceProvider : WorkbenchServiceRegistry.getRegistry().getSourceProviders()) {
			sourceProvider.initialize(locator);
			registerProvider(sourceProvider);
		}
	}
}
