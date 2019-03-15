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
package org.eclipse.urischeme;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.urischeme.internal.registration.RegistrationLinux;
import org.eclipse.urischeme.internal.registration.RegistrationMacOsX;
import org.eclipse.urischeme.internal.registration.RegistrationWindows;

/**
 * Interface for registration or uri schemes in the different operating systems
 * (MacOSX, Linux and Windows)<br />
 * Call <code>getInstance()</code> to get an OS specific instance.
 *
 */
public interface IOperatingSystemRegistration {

	/**
	 * Returns the operating system specific implementation of
	 * IOperatingSystemRegistration
	 *
	 * @return an instance of IOperatingSystemRegistration
	 */
	static IOperatingSystemRegistration getInstance() {
		if (Platform.OS_MACOSX.equals(Platform.getOS())) {
			return new RegistrationMacOsX();
		} else if (Platform.OS_LINUX.equals(Platform.getOS())) {
			return new RegistrationLinux();
		} else if (Platform.OS_WIN32.equals(Platform.getOS())) {
			return new RegistrationWindows();
		}
		return null;
	}

	/**
	 * Registers/Unregisters uri schemes for this Eclipse installation
	 *
	 * @param toAdd    the uri schemes which this Eclipse should handle additionally
	 * @param toRemove the uri schemes which this Eclipse should not handle anymore
	 * @throws Exception something went wrong
	 */
	void handleSchemes(Collection<IScheme> toAdd, Collection<IScheme> toRemove) throws Exception;

	/**
	 * Takes the given schemes and fills information like whether they are
	 * registered for this instance and the handler location.
	 *
	 * @param schemes
	 * @return schemes with information
	 * @throws Exception something went wrong
	 */
	List<ISchemeInformation> getSchemesInformation(Collection<IScheme> schemes) throws Exception;

	/**
	 * @return the Eclipse executable
	 */
	String getEclipseLauncher();

	/**
	 *
	 * This method returns if the current operating system allows to register an uri
	 * scheme that this already handled by another application.
	 *
	 * If the operating system does store this information in de-central way the
	 * implementation should return false.
	 *
	 * @return <code>true</code> if registering of other application's uri scheme is
	 *         supported - <code>false</code> otherwise.
	 */
	boolean canOverwriteOtherApplicationsRegistration();

}
