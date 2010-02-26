/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.about.ISystemSummarySection;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Writes information about the update configurer into the system summary.
 * 
 * @since 3.0
 */
public class ConfigurationLogUpdateSection implements ISystemSummarySection {

	/*
	 * Query the profile and print out the list of IUs which are installed.
	 */
	private void writeInstalledIUs(PrintWriter writer) {
		BundleContext context = IDEWorkbenchPlugin.getDefault().getBundle().getBundleContext();
		if (context == null)
			return;

		// Print out the list of IUs which are installed in the profile.
		ServiceReference reference = context.getServiceReference(IProfileRegistry.class.getName());
		if (reference == null)
			return;
		try {
			IProfileRegistry registry = (IProfileRegistry) context.getService(reference);
			if (registry == null)
				return;
			IProfile profile = registry.getProfile(IProfileRegistry.SELF);
			if (profile == null)
				return;

			writer.println(IDEWorkbenchMessages.ConfigurationLogUpdateSection_installConfiguration);
			writer.println(" " + NLS.bind(IDEWorkbenchMessages.ConfigurationLogUpdateSection_lastChangedOn, DateFormat.getDateInstance().format(new Date(profile.getTimestamp())))); //$NON-NLS-1$
			writer.println(" " + NLS.bind(IDEWorkbenchMessages.ConfigurationLogUpdateSection_location, profile.getProperty(IProfile.PROP_INSTALL_FOLDER))); //$NON-NLS-1$
			writer.println(" " + NLS.bind(IDEWorkbenchMessages.ConfigurationLogUpdateSection_timestamp, Long.toString(profile.getTimestamp()))); //$NON-NLS-1$
			writer.println();

			// Since this code is only called in the Help -> About -> Configuration Details case we
			// won't worry too much about performance here and we will sort the query results
			// afterwards, but before printing them out.
			IQueryResult result = profile.available(QueryUtil.createIUAnyQuery(), null);
			SortedSet sorted = new TreeSet();
			for (Iterator iter = result.iterator(); iter.hasNext();) {
				IInstallableUnit unit = (IInstallableUnit) iter.next();
				sorted.add(NLS.bind(IDEWorkbenchMessages.ConfigurationLogUpdateSection_IU, unit.getId(), unit.getVersion()));
			}
			if (!sorted.isEmpty()) {
				writer.println(IDEWorkbenchMessages.ConfigurationLogUpdateSection_IUHeader);
				writer.println();
				for (Iterator iter = sorted.iterator(); iter.hasNext();)
					writer.println(iter.next());
			}
		} finally {
			context.ungetService(reference);
		}
	}

	/*
	 * Query OSGi and print out the list of known bundles.
	 */
	private void writeBundles(PrintWriter writer) {
		BundleContext context = IDEWorkbenchPlugin.getDefault().getBundle().getBundleContext();
		if (context == null)
			return;
		ServiceReference reference = context.getServiceReference(PlatformAdmin.class.getName());
		if (reference == null)
			return;
		PlatformAdmin admin = (PlatformAdmin) context.getService(reference);
		try {
			State state = admin.getState(false);
			BundleDescription[] bundles = state.getBundles();
			// Since this code is only called in the Help -> About -> Configuration Details case we
			// won't worry too much about performance here and we will sort the query results
			// afterwards, but before printing them out.
			SortedSet sorted = new TreeSet();
			for (int i = 0; i < bundles.length; i++) {
				BundleDescription bundle = bundles[i];
				String name = bundle.getName();
				if (name == null)
					name = bundle.getLocation();
				String message = NLS.bind(IDEWorkbenchMessages.ConfigurationLogUpdateSection_bundle, new Object[] {name, bundle.getVersion(), bundle.getLocation()});
				sorted.add(message);
			}
			if (!sorted.isEmpty()) {
				writer.println(IDEWorkbenchMessages.ConfigurationLogUpdateSection_bundleHeader);
				writer.println();
				for (Iterator iter = sorted.iterator(); iter.hasNext();)
					writer.println(iter.next());
			}
		} finally {
			context.ungetService(reference);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.about.ISystemSummarySection#write(java.io.PrintWriter)
	 */
	public void write(PrintWriter writer) {
		writeInstalledIUs(writer);
		writer.println();
		writeBundles(writer);
	}
}
