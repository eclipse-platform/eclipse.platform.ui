/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.about.ISystemSummarySection;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.DateFormat;

/**
 * This class contains utility methods that clients may use to obtain
 * information about the system configuration.
 * 
 * @since 3.4
 * 
 */
public final class ConfigurationInfo {

	/**
	 * Return the build id for this instance. This may be <code>null</code> in
	 * the event that this property is undefined.
	 * 
	 * @return the build id or <code>null</code>
	 */
	public static String getBuildId() {
		return System.getProperty("eclipse.buildId", null); //$NON-NLS-1$
	}

	/**
	 * Return a multi-line String that describes the current configuration. This
	 * may include but is not limited to system properties, installed bundles,
	 * and installed features. The specific format of this message is undefined
	 * and may change at any time.
	 * 
	 * <p>
	 * The contents of this String are in part constructed via
	 * {@link ISystemSummarySection} that are registered with this running
	 * instance of the workbench.
	 * </p>
	 * 
	 * @return the configuration info
	 */
	public static String getSystemSummary() {
		StringWriter out = new StringWriter();
		PrintWriter writer = new PrintWriter(out);
		writer.println(NLS.bind(WorkbenchMessages.SystemSummary_timeStamp,
				DateFormat
						.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
						.format(new Date())));

		ConfigurationInfo.appendExtensions(writer);
		writer.close();
		return out.toString();
	}

	/*
	 * Appends the contents of all extensions to the configurationLogSections
	 * extension point.
	 */
	private static void appendExtensions(PrintWriter writer) {
		IConfigurationElement[] configElements = getSortedExtensions();
		for (int i = 0; i < configElements.length; ++i) {
			IConfigurationElement element = configElements[i];

			Object obj = null;
			try {
				obj = WorkbenchPlugin.createExtension(element,
						IWorkbenchConstants.TAG_CLASS);
			} catch (CoreException e) {
				WorkbenchPlugin.log(
						"could not create class attribute for extension", //$NON-NLS-1$
						e.getStatus());
			}

			writer.println();
			writer.println(NLS.bind(
					WorkbenchMessages.SystemSummary_sectionTitle, element
							.getAttribute("sectionTitle"))); //$NON-NLS-1$

			if (obj instanceof ISystemSummarySection) {
				ISystemSummarySection logSection = (ISystemSummarySection) obj;
				logSection.write(writer);
			} else {
				writer.println(WorkbenchMessages.SystemSummary_sectionError);
			}
		}
	}

	private static IConfigurationElement[] getSortedExtensions() {
		IConfigurationElement[] configElements = Platform
				.getExtensionRegistry().getConfigurationElementsFor(
						PlatformUI.PLUGIN_ID,
						IWorkbenchRegistryConstants.PL_SYSTEM_SUMMARY_SECTIONS);

		Arrays.sort(configElements, new Comparator() {
			Collator collator = Collator.getInstance(Locale.getDefault());

			public int compare(Object a, Object b) {
				IConfigurationElement element1 = (IConfigurationElement) a;
				IConfigurationElement element2 = (IConfigurationElement) b;

				String id1 = element1.getAttribute("id"); //$NON-NLS-1$
				String id2 = element2.getAttribute("id"); //$NON-NLS-1$

				if (id1 != null && id2 != null && !id1.equals(id2)) {
					return collator.compare(id1, id2);
				}

				String title1 = element1.getAttribute("sectionTitle"); //$NON-NLS-1$ 
				String title2 = element2.getAttribute("sectionTitle"); //$NON-NLS-1$

				if (title1 == null) {
					title1 = ""; //$NON-NLS-1$
				}
				if (title2 == null) {
					title2 = ""; //$NON-NLS-1$
				}

				return collator.compare(title1, title2);
			}
		});

		return configElements;
	}
}
