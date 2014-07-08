/*******************************************************************************
 * Copyright (c) 2014 Manumitting Technologies Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis (MTI) - initial API and implementation
 *     René Brandstetter - Bug 419749 - [Workbench] [e4 Workbench] - Remove the deprecated PackageAdmin
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

/**
 * Sort {@link IExtension}s by their plug-in dependencies such that any extension from bundle A
 * should be sorted before any extensions from any bundles that depend on A.
 * <p>
 * Consider having extensions from bundles A, B, and C, where A depends on B, and B depends on C.
 * The result of this sort should be the extensions from C, then from B, and finally from A.
 */
public class ExtensionsSort extends TopologicalSort<IExtension, Bundle> {

	@Override
	protected Bundle getId(IExtension extension) {
		IContributor contributor = extension.getContributor();
		return Activator.getDefault().getBundleForName(contributor.getName());
	}

	/**
	 * Returns the bundles that the given bundle requires.
	 * 
	 * @return An unmodifiable {@link Iterable} set of bundles currently required by the geiven
	 *         bundle. An empty {@link Iterable} will be returned if the given {@code Bundle} object
	 *         has become stale or there are no bundles required.
	 * @throws NullPointerException
	 *             if {@code bundle} is <code>null</code>
	 */
	@Override
	protected Collection<Bundle> getRequirements(Bundle bundle) {
		// punt to getDependents()
		return null;
	}

	/**
	 * Recursively collects all bundles which depend-on/require the given {@link BundleWiring}.
	 * <p>
	 * All re-exports will be followed and also be contained in the result.
	 * </p>
	 * 
	 * @param dependents
	 *            the result which will contain all the bundles which require the given
	 *            {@link BundleWiring}
	 * @param providerWiring
	 *            the {@link BundleWiring} for which the requirers should be resolved
	 * @throws NullPointerException
	 *             if either the requiring or the providerWiring is <code>null</code>
	 */
	private static void addDependents(Set<Bundle> dependents, BundleWiring providerWiring) {
		List<BundleWire> requirerWires = providerWiring
				.getProvidedWires(BundleNamespace.BUNDLE_NAMESPACE);
		if (requirerWires == null) {
			// we don't hold locks while checking the graph, just return if no longer isInUse
			return;
		}
		for (BundleWire requireBundleWire : requirerWires) {
			Bundle requirer = requireBundleWire.getRequirer().getBundle();
			if (dependents.contains(requirer)) {
				continue;
			}
			dependents.add(requirer);
			String reExport = requireBundleWire.getRequirement().getDirectives()
					.get(BundleNamespace.REQUIREMENT_VISIBILITY_DIRECTIVE);
			if (BundleNamespace.VISIBILITY_REEXPORT.equals(reExport)) {
				addDependents(dependents, requireBundleWire.getRequirerWiring());
			}
		}
	}

	/**
	 * Returns the bundles that currently require the given bundle.
	 * <p>
	 * If {@code bundle} is required and then re-exported by another bundle then all the requiring
	 * bundles of the re-exporting bundle are included in the returned array as they transitively
	 * depend on {@code bundle}.
	 * </p>
	 * 
	 * @return An unmodifiable {@link Iterable} set of bundles currently requiring {@code bundle}.
	 *         An empty {@link Iterable} will be returned if the {@code bundle} has become stale or
	 *         has no dependents.
	 * @throws NullPointerException
	 *             if {@code bundle} is <code>null</code>
	 */
	@Override
	protected Collection<Bundle> getDependencies(Bundle bundle) {
		BundleWiring providerWiring = bundle.adapt(BundleWiring.class);
		if (!providerWiring.isInUse()) {
			return Collections.emptySet();
		}

		Set<Bundle> required = new HashSet<Bundle>();
		addDependents(required, providerWiring);
		return Collections.unmodifiableSet(required);
	}
}
