/*******************************************************************************
 * Copyright (c) 2018 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.pluginchecks;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.resource.Namespace;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Checks that plugin.xml of specified bundles is valid and referred classes can
 * be accessed
 *
 */
public class PluginWalkerTest {

	private BundleContext bundleContext;
	private List<String> bundlesWithPluginXml;

	@Before
	public void setup() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(PluginWalkerTest.this.getClass());
		assertNotNull("Make sure you're running this as a plugin test", bundle);
		assertNotNull(bundle);
		bundleContext = bundle.getBundleContext();
		bundlesWithPluginXml = Arrays.asList("org.eclipse.e4.ui.css.swt", "org.eclipse.e4.ui.model.workbench",
				"org.eclipse.e4.ui.workbench.swt", "org.eclipse.ui.forms", "org.eclipse.ui.themes",
				"org.eclipse.e4.ui.workbench", "org.eclipse.e4.ui.workbench.addons.swt", "org.eclipse.ui.ide",
				"org.eclipse.e4.ui.css.core", "org.eclipse.ui.workbench", "org.eclipse.ui.navigator.resources",
				"org.eclipse.ui.navigator", "org.eclipse.ui.views", "org.eclipse.ui.views.properties.tabbed",
				"org.eclipse.ui", "org.eclipse.ui.browser", "org.eclipse.e4.ui.css.swt.theme",
				"org.eclipse.ui.ide.application", "org.eclipse.ui.monitoring");
	}

	@Test
	public void validateAccessToBundle() throws Exception {
		for (String bundleSymbolicName : bundlesWithPluginXml) {
			Bundle bundle = getBundle(bundleContext, bundleSymbolicName);
			assertNotNull(bundle);
		}
	}

	@Test
	public void ensurePluginxmlContainsAtLeastOneEntry() throws Exception {
		for (String bundleSymbolicName : bundlesWithPluginXml) {

			Bundle bundle = getBundle(bundleContext, bundleSymbolicName);
			Document doc = getDocument(bundle);
			NodeList extensions = doc.getElementsByTagName("extension");
			NodeList extensionpoint = doc.getElementsByTagName("extension-point");

			boolean hasExtension = extensions.getLength() > 0;
			boolean hasExtensionPoint = extensionpoint.getLength() > 0;

			assertTrue(
					"plugin.xml from " + bundleSymbolicName
							+ "  must contain at least one extension point or extension",
					hasExtension || hasExtensionPoint);
		}

	}

	@Test
	public void ensureExtensionPointClassesAreAccessable() throws Exception {
		for (String bundleSymbolicName : bundlesWithPluginXml) {

			IExtensionRegistry registry = RegistryFactory.getRegistry();
			IExtensionPoint[] extensionPoints = registry.getExtensionPoints();
			for (IExtensionPoint point : extensionPoints) {
				IConfigurationElement[] configurationElements = point.getConfigurationElements();
				for (IConfigurationElement element : configurationElements) {
					IExtension declaringExtension = element.getDeclaringExtension();
					String name = declaringExtension.getContributor().getName();
					if (name.equals(bundleSymbolicName)) {

						String clsSpec = element.getAttribute("class");
						if (clsSpec != null && clsSpec.length() > 0) {
							Collection<BundleWiring> wirings = findWirings(bundleSymbolicName, bundleContext);

							// remove : for factories
							int indexLastColumn = clsSpec.lastIndexOf(':');
							if (indexLastColumn != -1) {
								clsSpec = clsSpec.substring(0, indexLastColumn);
							}
							System.out.println(clsSpec);

							int indexLastDot = clsSpec.lastIndexOf('.');
							String classPackageName = '/' + clsSpec.substring(0, indexLastDot).replace('.', '/');
							String classResourceName = clsSpec.substring(indexLastDot + 1) + ".class"; //$NON-NLS-1$
							for (BundleWiring bundleWiring : wirings) {
								if (!checkClassResource(classPackageName, classResourceName, bundleWiring)) {
									fail("Class " + clsSpec + " not found");
								}
							}
						}
					}

				}
			}

		}
	}

	private static InputStream getPluginXml(Bundle bundle) throws IOException {
		URL entry = bundle.getEntry("plugin.xml");
		return entry.openConnection().getInputStream();
	}

	private static Bundle getBundle(BundleContext bundleContext, String symbolicName) {
		Bundle result = null;
		for (Bundle candidate : bundleContext.getBundles()) {
			if (candidate.getSymbolicName().equals(symbolicName)) {
				if (result == null || result.getVersion().compareTo(candidate.getVersion()) < 0) {
					result = candidate;
				}
			}
		}
		return result;
	}

	/**
	 * Parses the plugin.xml file and provides access to its content
	 *
	 * @param bundle
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private static Document getDocument(Bundle bundle) throws SAXException, IOException, ParserConfigurationException {
		Document doc = null;
		DocumentBuilder builder = createDocumentBuilder();
		try (InputStream pluginXml = getPluginXml(bundle)) {
			assertNotNull(pluginXml);
			// test fails if malformed
			doc = builder.parse(pluginXml);
		}
		return doc;
	}

	private static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder;
	}

	private Collection<BundleWiring> findWirings(final String bundleSymbolicName, BundleContext bundleContext) {
		Requirement req = new Requirement() {
			@Override
			public Resource getResource() {
				// no resource
				return null;
			}

			@Override
			public String getNamespace() {
				return IdentityNamespace.IDENTITY_NAMESPACE;
			}

			@Override
			public Map<String, String> getDirectives() {
				return Collections.singletonMap(Namespace.REQUIREMENT_FILTER_DIRECTIVE,
						"(" + IdentityNamespace.IDENTITY_NAMESPACE + "=" + bundleSymbolicName + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			@Override
			public Map<String, Object> getAttributes() {
				return Collections.emptyMap();
			}
		};
		Collection<BundleCapability> identities = bundleContext.getBundle(Constants.SYSTEM_BUNDLE_LOCATION)
				.adapt(FrameworkWiring.class).findProviders(req);
		Collection<BundleWiring> result = new ArrayList<>(1); // normally
																// only
																// one
		for (BundleCapability identity : identities) {
			BundleRevision revision = identity.getRevision();
			BundleWiring wiring = revision.getWiring();
			if (wiring != null) {
				if ((revision.getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
					// fragment case; need to get the host wiring
					wiring = wiring.getRequiredWires(HostNamespace.HOST_NAMESPACE).get(0).getProviderWiring();
				}
				result.add(wiring);
			}
		}
		return result;
	}

	private boolean checkClassResource(String classPackageName, String classFileName, BundleWiring wiring) {
		if (wiring == null) {
			return false;
		}
		if ((wiring.getRevision().getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
			// fragment case; need to get the host wiring
			wiring = wiring.getRequiredWires(HostNamespace.HOST_NAMESPACE).get(0).getProviderWiring();
		}
		Collection<String> classResourcePaths = wiring.listResources(classPackageName, classFileName, 0);
		return classResourcePaths != null && !classResourcePaths.isEmpty();
	}
}
