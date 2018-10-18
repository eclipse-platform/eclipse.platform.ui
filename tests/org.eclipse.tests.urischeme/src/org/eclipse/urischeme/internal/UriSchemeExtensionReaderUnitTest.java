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
package org.eclipse.urischeme.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.urischeme.IScheme;
import org.junit.Before;
import org.junit.Test;

public class UriSchemeExtensionReaderUnitTest {

	private UriSchemeHandlerSpy abcHandler;
	private ConfigurationElementMock configElementForAbc;
	private UriSchemeExtensionReader extensionReader;

	@Before
	public void setup() throws Exception {
		abcHandler = new UriSchemeHandlerSpy();
		configElementForAbc = new ConfigurationElementMock("abc", "abc Scheme", abcHandler);

		extensionReader = new UriSchemeExtensionReader();

		setExtensionsInReader(configElementForAbc);
	}

	@Test
	public void returnsRegisteredHandler() throws Exception {
		assertEquals(abcHandler, extensionReader.getHandlerFromExtensionPoint("abc"));
	}

	@Test
	public void doesntReturnHandlerForUnregisteredScheme() throws Exception {
		assertNull(extensionReader.getHandlerFromExtensionPoint("xyz"));
	}

	@Test
	public void callsFirstOfTwoHandlersForSameScheme() throws Exception {
		IConfigurationElement second = new ConfigurationElementMock("abc", "abc Scheme",
				new UriSchemeHandlerSpy());
		setExtensionsInReader(configElementForAbc, second);

		assertEquals(abcHandler, extensionReader.getHandlerFromExtensionPoint("abc"));
	}

	@Test(expected = CoreException.class)
	public void throwExceptionOnWrongRegisteredType() throws Exception {
		ConfigurationElementMock element = new ConfigurationElementMock("abc", "abc Scheme", new Object());
		setExtensionsInReader(element);

		extensionReader.getHandlerFromExtensionPoint("abc");
	}

	@Test
	public void returnsAllRegisteredSchemes() throws Exception {
		ConfigurationElementMock element1 = new ConfigurationElementMock("abc", "abc Scheme", new Object());
		ConfigurationElementMock element2 = new ConfigurationElementMock("xyz", "xyz Scheme", new Object());
		setExtensionsInReader(element1, element2);

		Collection<IScheme> schemes = extensionReader.getSchemes();
		assertEquals(2, schemes.size());

		IScheme[] schemesArray = schemes.toArray(new IScheme[0]);
		assertEquals("abc", schemesArray[0].getName());
		assertEquals("abc Scheme", schemesArray[0].getDescription());
		assertEquals("xyz", schemesArray[1].getName());
		assertEquals("xyz Scheme", schemesArray[1].getDescription());
	}

	private void setExtensionsInReader(IConfigurationElement... element) throws Exception {
		extensionReader.configurationElements = element;
	}

	private final class ConfigurationElementMock implements IConfigurationElement {
		private final String uriScheme;
		private final String uriSchemeDescription;
		private final Object handler;

		private ConfigurationElementMock(String uriScheme, String uriSchemeDescription, Object handler) {
			this.uriScheme = uriScheme;
			this.uriSchemeDescription = uriSchemeDescription;
			this.handler = handler;
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public String getValueAsIs() throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public String getValue(String locale) throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public String getValue() throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public Object getParent() throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public String getNamespaceIdentifier() throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public String getNamespace() throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public String getName() throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public int getHandleId() {
			return 0;
		}

		@Override
		public IExtension getDeclaringExtension() throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public IContributor getContributor() throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public IConfigurationElement[] getChildren(String name) throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public IConfigurationElement[] getChildren() throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public String[] getAttributeNames() throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public String getAttributeAsIs(String name) throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public String getAttribute(String attrName, String locale) throws InvalidRegistryObjectException {
			return null;
		}

		@Override
		public String getAttribute(String name) throws InvalidRegistryObjectException {
			if (name.equals("uriScheme"))
				return uriScheme;
			if (name.equals("uriSchemeDescription"))
				return uriSchemeDescription;
			return null;
		}

		@Override
		public Object createExecutableExtension(String propertyName) throws CoreException {
			return propertyName.equals("class") ? handler : null;
		}
	}
}