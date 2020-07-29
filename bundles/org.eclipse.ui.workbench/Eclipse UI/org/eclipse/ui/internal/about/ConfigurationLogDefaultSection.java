/*******************************************************************************
 * Copyright (c) 2003, 2019 IBM Corporation and others.
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
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548516
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import static org.eclipse.e4.core.services.about.AboutSections.SECTION_INSTALLED_BUNDLES;
import static org.eclipse.e4.core.services.about.AboutSections.SECTION_INSTALLED_FEATURES;
import static org.eclipse.e4.core.services.about.AboutSections.SECTION_SYSTEM_ENVIRONMENT;
import static org.eclipse.e4.core.services.about.AboutSections.SECTION_SYSTEM_PROPERTIES;
import static org.eclipse.e4.core.services.about.AboutSections.SECTION_USER_PREFERENCES;

import java.io.PrintWriter;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.e4.core.services.about.AboutSections;
import org.eclipse.e4.core.services.about.ISystemInformation;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.about.ISystemSummarySection;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * This class puts basic platform information into the system summary log. This
 * includes sections for the java properties, the ids of all installed features
 * and plugins, as well as a the current contents of the preferences service.
 *
 * @since 3.0
 */
public class ConfigurationLogDefaultSection implements ISystemSummarySection {

	@Override
	public void write(PrintWriter writer) {
		appendSection(SECTION_SYSTEM_PROPERTIES, WorkbenchMessages.SystemSummary_systemProperties, writer);
		appendSection(SECTION_SYSTEM_ENVIRONMENT, WorkbenchMessages.SystemSummary_systemVariables, writer);
		appendSection(SECTION_INSTALLED_FEATURES, WorkbenchMessages.SystemSummary_features, writer);
		appendSection(SECTION_INSTALLED_BUNDLES, WorkbenchMessages.SystemSummary_pluginRegistry, writer);
		appendSection(SECTION_USER_PREFERENCES, WorkbenchMessages.SystemSummary_userPreferences, writer);
	}

	/**
	 * @param writer
	 */
	private void appendSection(String section, String caption, PrintWriter writer) {
		writer.println();
		writer.println(caption);
		try {
			if (!ServiceCaller.callOnce(getClass(), ISystemInformation.class,
					AboutSections.createSectionFilter(section),
					systemInformation -> systemInformation.append(writer))) {
				throw new IllegalStateException();
			}
		} catch (Exception e) {
			WorkbenchPlugin.log(NLS.bind("Failed to retrieve data for section: {0}", section), e); //$NON-NLS-1$
			writer.println(WorkbenchMessages.SystemSummary_sectionError);
		}
	}

}
