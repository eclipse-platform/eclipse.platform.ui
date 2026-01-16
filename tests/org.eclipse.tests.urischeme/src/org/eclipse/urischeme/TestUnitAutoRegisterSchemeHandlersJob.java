/*******************************************************************************
 * Copyright (c) 2020 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.urischeme.internal.registration.Scheme;
import org.eclipse.urischeme.internal.registration.SchemeInformation;
import org.junit.jupiter.api.Test;
import org.osgi.service.prefs.BackingStoreException;

@SuppressWarnings("restriction")
public class TestUnitAutoRegisterSchemeHandlersJob {

	private PreferenceMock preferenceNode;
	private OperatingSystemRegistrationMock osRegistration;
	private static final String HELLO_URI_SCHEME = "hello";
	private final static IScheme helloScheme = new Scheme(HELLO_URI_SCHEME, "helloScheme");
	private static final String HELLO1_URI_SCHEME = "hello1";
	private final static IScheme hello1Scheme = new Scheme(HELLO1_URI_SCHEME, "hello1Scheme");
	private static final boolean SUPPORTS_REGISTRATION = true;
	private static final boolean DOESNT_SUPPORT_REGISTRATION = false;

	@Test
	public void noSchemeDoesNothing() throws Exception {
		AutoRegisterSchemeHandlersJob job = createJob(Collections.emptyList(), "", Collections.emptyList(),
				SUPPORTS_REGISTRATION);

		job.run(new NullProgressMonitor());

		assertNull(preferenceNode.writtenValue, "Nothing should be written to preferences");
		assertFalse(preferenceNode.flushed, "Flush on preferences should not be called");
		assertNull(osRegistration.addedSchemes, "No schemes should be registered");
		assertNull(osRegistration.removedSchemes, "No schemes should be un-registered");

	}

	@Test
	public void noNewSchemeDoesNothing() throws Exception {
		AutoRegisterSchemeHandlersJob job = createJob(Arrays.asList(helloScheme), HELLO_URI_SCHEME,
				Collections.emptyList(), SUPPORTS_REGISTRATION);

		job.run(new NullProgressMonitor());

		assertNull(preferenceNode.writtenValue, "Nothing should be written to preferences");
		assertFalse(preferenceNode.flushed, "Flush on preferences should not be called");
		assertNull(osRegistration.addedSchemes, "No schemes should be registered");
		assertNull(osRegistration.removedSchemes, "No schemes should be un-registered");

	}

	@Test
	public void newSchemeRegistersNewScheme() throws Exception {
		SchemeInformation helloSchemeInfo = new SchemeInformation(helloScheme.getName(), helloScheme.getDescription());
		helloSchemeInfo.setHandled(true);
		SchemeInformation hello1SchemeInfo = new SchemeInformation(hello1Scheme.getName(),
				hello1Scheme.getDescription());
		hello1SchemeInfo.setHandled(false);
		List<ISchemeInformation> schemeInfos = Arrays.asList(hello1SchemeInfo);

		AutoRegisterSchemeHandlersJob job = createJob(Arrays.asList(helloScheme, hello1Scheme), HELLO_URI_SCHEME,
				schemeInfos, SUPPORTS_REGISTRATION);

		job.run(new NullProgressMonitor());

		assertEquals(helloScheme.getName() + "," + hello1Scheme.getName(),
				preferenceNode.writtenValue, "Wrong values written to preferences");
		assertTrue(preferenceNode.flushed, "Preferences not flushed");
		assertEquals(hello1SchemeInfo,
				osRegistration.addedSchemes.iterator().next(), "Wrong schemes have been registered");
		assertTrue(osRegistration.removedSchemes.isEmpty(), "No schemes should be un-registered");
	}

	@Test
	public void newAlreadyRegisteredSchemeDoesNoting() throws Exception {
		SchemeInformation helloSchemeInfo = new SchemeInformation(helloScheme.getName(), helloScheme.getDescription());
		helloSchemeInfo.setHandled(true);
		SchemeInformation hello1SchemeInfo = new SchemeInformation(hello1Scheme.getName(),
				hello1Scheme.getDescription());
		hello1SchemeInfo.setHandled(true);
		List<ISchemeInformation> schemeInfos = new ArrayList<>();

		AutoRegisterSchemeHandlersJob job = createJob(Arrays.asList(helloScheme, hello1Scheme), HELLO_URI_SCHEME,
				schemeInfos, SUPPORTS_REGISTRATION);

		job.run(new NullProgressMonitor());

		assertNull(preferenceNode.writtenValue, "Nothing should be written to preferences");
		assertFalse(preferenceNode.flushed, "Flush on preferences should not be called");
		assertNull(osRegistration.addedSchemes, "No schemes should be registered");
		assertNull(osRegistration.removedSchemes, "No schemes should be un-registered");
	}

	@Test
	public void unregisteredSchemeThatWasAutoregisterdOnceDoesNotAutoregisterAgain() throws Exception {
		SchemeInformation helloSchemeInfo = new SchemeInformation(helloScheme.getName(), helloScheme.getDescription());
		helloSchemeInfo.setHandled(false);
		SchemeInformation hello1SchemeInfo = new SchemeInformation(hello1Scheme.getName(),
				hello1Scheme.getDescription());
		hello1SchemeInfo.setHandled(true);
		List<ISchemeInformation> schemeInfos = Arrays.asList(helloSchemeInfo, hello1SchemeInfo);

		AutoRegisterSchemeHandlersJob job = createJob(Arrays.asList(helloScheme, hello1Scheme),
				HELLO_URI_SCHEME + "," + HELLO1_URI_SCHEME, schemeInfos, SUPPORTS_REGISTRATION);

		job.run(new NullProgressMonitor());

		assertNull(preferenceNode.writtenValue, "Nothing should be written to preferences");
		assertFalse(preferenceNode.flushed, "Flush on preferences should not be called");
		assertNull(osRegistration.addedSchemes, "No schemes should be registered");
		assertNull(osRegistration.removedSchemes, "No schemes should be un-registered");
	}

	@Test
	public void registrationOnUnsupportedRegistrationDoesNothing() throws Exception {
		AutoRegisterSchemeHandlersJob job = createJob(new ArrayList<>(), "dontCare", new ArrayList<>(),
				DOESNT_SUPPORT_REGISTRATION);
		assertFalse(job.shouldSchedule(), "Job should not run on OSes that don't support registration");
	}

	private AutoRegisterSchemeHandlersJob createJob(Collection<IScheme> installedSchemes,
			String alreadyProcessedSchemes, List<ISchemeInformation> registeredSchemes, boolean supportsRegistration) {
		ExtensionReaderStub extensionReader = new ExtensionReaderStub(installedSchemes);
		osRegistration = new OperatingSystemRegistrationMock(registeredSchemes, supportsRegistration);
		preferenceNode = new PreferenceMock();
		preferenceNode.currentValue = alreadyProcessedSchemes;

		return new AutoRegisterSchemeHandlersJob(preferenceNode, extensionReader, osRegistration);
	}

	private static final class PreferenceMock extends EclipsePreferences {

		private static final String PROCESSED_SCHEMES_PREFERENCE = "processedSchemes"; //$NON-NLS-1$

		public boolean flushed = false;
		public String writtenValue;
		public String currentValue;

		@Override
		public String get(String key, String defaultValue) {
			if (PROCESSED_SCHEMES_PREFERENCE.equals(key)) {
				return currentValue;
			} else {
				throw new IllegalArgumentException("Wrong key provided");
			}
		}

		@Override
		public void put(String key, String newValue) {
			if (PROCESSED_SCHEMES_PREFERENCE.equals(key)) {
				writtenValue = newValue;
			} else {
				throw new IllegalArgumentException("Wrong key provided");
			}
		}

		@Override
		public void flush() throws BackingStoreException {
			flushed = true;
		}

	}

	private static final class ExtensionReaderStub implements IUriSchemeExtensionReader {
		public Collection<IScheme> schemes;

		public ExtensionReaderStub(Collection<IScheme> schemes) {
			this.schemes = schemes;
		}

		@Override
		public Collection<IScheme> getSchemes() {
			return schemes;
		}

		@Override
		public IUriSchemeHandler getHandlerFromExtensionPoint(String uriScheme) {
			return null;
		}
	}

	private static final class OperatingSystemRegistrationMock implements IOperatingSystemRegistration {

		private final List<ISchemeInformation> schemeInformations;
		public Collection<IScheme> addedSchemes = null;
		public Collection<IScheme> removedSchemes = null;
		public boolean supportsRegistration;

		public OperatingSystemRegistrationMock(List<ISchemeInformation> schemeInformations,
				boolean supportsRegistration) {
			this.schemeInformations = schemeInformations;
			this.supportsRegistration = supportsRegistration;
		}

		@Override
		public void handleSchemes(Collection<IScheme> toAdd, Collection<IScheme> toRemove) throws Exception {
			this.addedSchemes = toAdd;
			this.removedSchemes = toRemove;
		}

		@Override
		public List<ISchemeInformation> getSchemesInformation(Collection<IScheme> schemes) throws Exception {
			return schemeInformations;
		}

		@Override
		public String getEclipseLauncher() {
			return "";
		}

		@Override
		public boolean canOverwriteOtherApplicationsRegistration() {
			return false;
		}

		@Override
		public boolean supportsRegistration() {
			return supportsRegistration;
		}
	}
}
