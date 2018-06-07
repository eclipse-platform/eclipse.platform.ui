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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.urischeme.IUriSchemeHandler;
import org.eclipse.urischeme.IUriSchemeProcessor;
import org.junit.Before;
import org.junit.Test;

public class UriSchemeProcessorUnitTest {

	private static final String ABC_SCHEME = "abc";
	private static final String XYZ_SCHEME = "xyz";

	private static final String ABC_URI = ABC_SCHEME + "://test";
	private static final String XYZ_URI = XYZ_SCHEME + "://test";

	private HandlerMock abcHandler;
	private ConfigElementMock configElementForAbc;
	private UriSchemeProcessor schemeProcessor;

	@Before
	public void setup() throws Exception {
		abcHandler = new HandlerMock();
		configElementForAbc = new ConfigElementMock(ABC_SCHEME, abcHandler);

		schemeProcessor = new UriSchemeProcessor();

		registerExtensions(schemeProcessor, configElementForAbc);
	}

	@Test
	public void callsRegisteredlUriSchemeHandler() throws Exception {
		schemeProcessor.handleUri(ABC_SCHEME, ABC_URI);

		String errorMsg = "Registered handler was not called for '" + ABC_URI + "'";
		assertTrue(errorMsg, abcHandler.handled.contains(ABC_URI));
	}

	@Test
	public void doesntCallHandlerForUnregisteredScheme() throws Exception {
		schemeProcessor.handleUri(XYZ_SCHEME, XYZ_URI);

		assertFalse(abcHandler.called);
	}

	@Test
	public void callsFirstOfTwoHandlersForSameScheme() throws Exception {
		HandlerMock secondAbcHandler = new HandlerMock();
		IConfigurationElement secondConfigElementForAbc = new ConfigElementMock(ABC_SCHEME,
				secondAbcHandler);
		registerExtensions(schemeProcessor, configElementForAbc, secondConfigElementForAbc);

		schemeProcessor.handleUri(ABC_SCHEME, ABC_URI);

		assertTrue(abcHandler.called);
		assertFalse(secondAbcHandler.called);
	}

	@Test
	public void buffersExtensionAndCreatesThemOnlyOnce() throws Exception {
		schemeProcessor.handleUri(ABC_SCHEME, ABC_URI);
		schemeProcessor.handleUri(ABC_SCHEME, ABC_URI);

		assertTrue(abcHandler.called);
		assertEquals("Extension created more than once", 1, configElementForAbc.extensionCreatedCount);
	}

	@Test(expected = CoreException.class)
	public void throwExceptionOnWrongRegisteredType() throws Exception {
		Object handlerWithWrongType = new Object();
		ConfigElementMock element = new ConfigElementMock(ABC_SCHEME, handlerWithWrongType);
		registerExtensions(schemeProcessor, element);

		schemeProcessor.handleUri(ABC_SCHEME, ABC_URI);
	}

	private void registerExtensions(IUriSchemeProcessor instance, IConfigurationElement... element) throws Exception {
		Field configurationElementsFields = UriSchemeProcessor.class.getDeclaredField("configurationElements");
		configurationElementsFields.setAccessible(true);
		configurationElementsFields.set(instance, element);
	}

	private final class ConfigElementMock implements IConfigurationElement {
		private final String uriScheme;
		private final Object handler;

		public int extensionCreatedCount = 0;

		private ConfigElementMock(String uriScheme, Object handler) {
			this.uriScheme = uriScheme;
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
			return null;
		}

		@Override
		public Object createExecutableExtension(String propertyName) throws CoreException {
			extensionCreatedCount++;
			return handler;
		}
	}

	private class HandlerMock implements IUriSchemeHandler {

		public Set<String> handled = new HashSet<>();

		public boolean called = false;

		@Override
		public void handle(String uri) {
			called = true;
			handled.add(uri);
		}
	};

}
