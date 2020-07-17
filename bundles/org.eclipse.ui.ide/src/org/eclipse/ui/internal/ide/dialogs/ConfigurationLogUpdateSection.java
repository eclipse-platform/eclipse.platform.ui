/*******************************************************************************
 * Copyright (c) 2003, 2017 IBM Corporation and others.
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
package org.eclipse.ui.internal.ide.dialogs;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.about.ISystemSummarySection;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

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
		ServiceCaller.callOnce(getClass(), IProfileRegistry.class, (registry) -> {
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
			SortedSet<String> sorted = new TreeSet<>();
			for (IInstallableUnit unit : profile.available(QueryUtil.createIUAnyQuery(), null)) {
				sorted.add(NLS.bind(IDEWorkbenchMessages.ConfigurationLogUpdateSection_IU, unit.getId(), unit.getVersion()));
			}
			if (!sorted.isEmpty()) {
				writer.println(IDEWorkbenchMessages.ConfigurationLogUpdateSection_IUHeader);
				writer.println();
				for (String string : sorted)
					writer.println(string);
			}
		});
	}

	/*
	 * Query OSGi and print out the list of known bundles.
	 */
	private void writeBundles(PrintWriter writer) {
		ServiceCaller.callOnce(getClass(), PlatformAdmin.class, (admin) -> {
			State state = admin.getState(false);
			// Since this code is only called in the Help -> About -> Configuration Details case we
			// won't worry too much about performance here and we will sort the query results
			// afterwards, but before printing them out.
			SortedSet<String> sorted = new TreeSet<>();
			for (BundleDescription bundle : state.getBundles()) {
				String name = bundle.getName();
				if (name == null)
					name = bundle.getLocation();
				String message = NLS.bind(IDEWorkbenchMessages.ConfigurationLogUpdateSection_bundle, new Object[] {name, bundle.getVersion(), bundle.getLocation()});
				sorted.add(message);
			}
			if (!sorted.isEmpty()) {
				writer.println(IDEWorkbenchMessages.ConfigurationLogUpdateSection_bundleHeader);
				writer.println();
				for (String string : sorted)
					writer.println(string);
			}
		});
	}

	@Override
	public void write(PrintWriter writer) {
		writeInstalledIUs(writer);
		writer.println();
		writeBundles(writer);
	}
}
