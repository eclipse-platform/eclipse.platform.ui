/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.urischeme.IOperatingSystemRegistration;
import org.eclipse.urischeme.ISchemeInformation;
import org.eclipse.urischeme.IUriSchemeExtensionReader.Scheme;
/**
 * Windows OS specific handling of schemes
 *
 */
public class RegistrationWindows implements IOperatingSystemRegistration {
	IRegistryWriter registryWriter;

	/**
	 * Creates an instance of RegistryWriter. The instance would help in
	 * reading,writing and removing entries from Windows Registry.
	 */
	public RegistrationWindows() {
		this(new RegistryWriter());
	}

	/**
	 * Creates an instance of RegistryWriter. The instance would help in
	 * reading,writing and removing entries from Windows Registry.
	 *
	 * This constructor is predominantly added for writing unit test methods for
	 * this class
	 *
	 * @param registryWriter the interface for windows registry handling
	 */
	public RegistrationWindows(IRegistryWriter registryWriter) {
		this.registryWriter = registryWriter;
	}

	@Override
	public void handleSchemes(Collection<ISchemeInformation> toAdd, Collection<ISchemeInformation> toRemove)
			throws Exception {
		for (ISchemeInformation scheme : toAdd) {
			registryWriter.addScheme(scheme.getScheme());
		}
		for (ISchemeInformation scheme : toRemove) {
			registryWriter.removeScheme(scheme.getScheme());
		}
	}

	/**
	 * Takes the given schemes,converts them to schemeInformation type by adding all
	 * the properties like schemeName,schemeDescription, handled(is handled by
	 * current instance) and handlerPath. If there is no handlerPath defined it is
	 * set to "<none>"
	 *
	 * @param schemes The schemes that should be checked for registrations.
	 * @return the registered schemes.
	 * @throws Exception
	 */
	@Override
	public List<ISchemeInformation> getSchemesInformation(Collection<Scheme> schemes) throws Exception {
		String launcher = getEclipseLauncher();

		List<ISchemeInformation> schemeInformations = new ArrayList<>();

		for (Scheme scheme : schemes) {
			SchemeInformation schemeInfo = new SchemeInformation(scheme.getUriScheme(),
					scheme.getUriSchemeDescription(), null);
			String path = registryWriter.getRegisteredHandlerPath(schemeInfo.getScheme());
			if (path == null) {
				path = ""; //$NON-NLS-1$
			}
			schemeInfo.setHandled(path.equals(launcher));
			schemeInfo.setHandlerLocation(path);
			schemeInformations.add(schemeInfo);
		}
		return schemeInformations;
	}

	@Override
	public String getEclipseLauncher() {
		return System.getProperty("eclipse.launcher");//$NON-NLS-1$
	}
}
