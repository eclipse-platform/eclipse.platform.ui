/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.content;

import java.util.Collection;
import java.util.Iterator;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.*;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class PreferenceModifyListener extends org.eclipse.core.runtime.preferences.PreferenceModifyListener {
	@Override
	public IEclipsePreferences preApply(IEclipsePreferences node) {
		Preferences root = node.node("/"); //$NON-NLS-1$
		try {
			if (root.nodeExists(InstanceScope.SCOPE)) {
				Preferences instance = root.node(InstanceScope.SCOPE);
				if (instance.nodeExists(ContentTypeManager.CONTENT_TYPE_PREF_NODE)) {
					// Invalidate content type managers
					Bundle bundle = FrameworkUtil.getBundle(IContentTypeManager.class);
					BundleContext context = bundle == null ? null : bundle.getBundleContext();
					if (context != null) {
						Collection<ServiceReference<IContentTypeManager>> srs = context
								.getServiceReferences(IContentTypeManager.class, null);
						Iterator<ServiceReference<IContentTypeManager>> it = srs.iterator();
						while (it.hasNext()) {
							ServiceReference<IContentTypeManager> sr = it.next();
							try {
								IContentTypeManager ctm = context.getService(sr);
								if (ctm instanceof ContentTypeManager) {
									((ContentTypeManager) ctm).invalidate();
								}
							} finally {
								context.ungetService(sr);
							}
						}
					}
				}
			}
		} catch (BackingStoreException e) {
			// do nothing
		} catch (InvalidSyntaxException e) {
			// do nothing
		}
		return node;
	}
}
