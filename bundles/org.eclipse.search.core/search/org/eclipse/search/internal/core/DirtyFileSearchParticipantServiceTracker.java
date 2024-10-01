/*******************************************************************************
 * Copyright (c) 2023, 2024 Red Hat Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import org.eclipse.search.internal.core.text.DirtyFileProvider;

public class DirtyFileSearchParticipantServiceTracker extends ServiceTracker<DirtyFileProvider, DirtyFileProvider> {

	private static final String PROPERTY_WEIGHT = "weight"; //$NON-NLS-1$

	public DirtyFileSearchParticipantServiceTracker(BundleContext context) throws InvalidSyntaxException {
		super(context, context.createFilter(MessageFormat.format("(&(objectClass={0}))", //$NON-NLS-1$
				DirtyFileProvider.class.getCanonicalName())), null);
	}

	private final static Comparator<ServiceReference<DirtyFileProvider>> BY_WEIGHT = Comparator.comparing(
			o -> o.getProperty(PROPERTY_WEIGHT), //
			Comparator.nullsFirst(Comparator.comparing(Integer.class::isInstance) // false<true
					.thenComparing(Integer.class::cast)));

	public DirtyFileProvider checkedGetService() {
		ServiceReference<DirtyFileProvider>[] allRefs = getServiceReferences();
		if (allRefs != null && allRefs.length > 0) {
			Optional<ServiceReference<DirtyFileProvider>> reference = Arrays.stream(allRefs).max(BY_WEIGHT);
			if (reference.isPresent()) {
				return getService(reference.get());
			}
		}
		return Collections::emptyMap;
	}
}
