/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc and others.
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
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.text.IDocument;

import org.eclipse.search.internal.core.text.IDirtyFileSearchParticipant;

public class DirtyFileSearchParticipantServiceTracker
		extends ServiceTracker<IDirtyFileSearchParticipant, IDirtyFileSearchParticipant> {

	private static final String PROPERTY_WEIGHT = "weight"; //$NON-NLS-1$
	public DirtyFileSearchParticipantServiceTracker(BundleContext context)
			throws InvalidSyntaxException {
		super(context, context.createFilter(MessageFormat.format("(&(objectClass={0}))", //$NON-NLS-1$
				IDirtyFileSearchParticipant.class.getCanonicalName())), null);
	}

	public IDirtyFileSearchParticipant checkedGetService() {
		ServiceReference<IDirtyFileSearchParticipant>[] allRefs = getServiceReferences();
		List<ServiceReference<IDirtyFileSearchParticipant>> l = Arrays.asList(allRefs);
		Collections.sort(l, new Comparator<ServiceReference<IDirtyFileSearchParticipant>>() {
			@Override
			public int compare(ServiceReference<IDirtyFileSearchParticipant> o1,
					ServiceReference<IDirtyFileSearchParticipant> o2) {
				Object o1Weight = o1.getProperty(PROPERTY_WEIGHT);
				Object o2Weight = o2.getProperty(PROPERTY_WEIGHT);
				int o1Val = o1Weight == null ? 0 : o1Weight instanceof Integer ? ((Integer) o1Weight).intValue() : 0;
				int o2Val = o2Weight == null ? 0 : o2Weight instanceof Integer ? ((Integer) o2Weight).intValue() : 0;
				return o2Val - o1Val;
			}
		});
		if (l.size() > 0) {
			return getService(l.get(0));
		}
		return new IDirtyFileSearchParticipant() {
			@Override
			public Map<IFile, IDocument> findDirtyFiles() {
				return Collections.EMPTY_MAP;
			}
		};
	}

	public void dispose() {
		close();
	}
}
