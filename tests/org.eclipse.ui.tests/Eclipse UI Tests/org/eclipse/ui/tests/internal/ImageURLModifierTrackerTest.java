/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
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
package org.eclipse.ui.tests.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.resource.IImageURLModifier;
import org.eclipse.jface.resource.ImageDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * Tests that the workbench installs the highest ranked {@link IImageURLModifier}
 * service in JFace.
 */
public class ImageURLModifierTrackerTest {

	private final BundleContext context = FrameworkUtil.getBundle(ImageURLModifierTrackerTest.class)
			.getBundleContext();
	private final List<ServiceRegistration<IImageURLModifier>> registrations = new ArrayList<>();
	private ImageDescriptor descriptor;

	@BeforeEach
	public void setUp() throws Exception {
		descriptor = ImageDescriptor.createFromURL(URI.create("file:/original.png").toURL());
	}

	@AfterEach
	public void tearDown() {
		for (ServiceRegistration<IImageURLModifier> registration : registrations) {
			try {
				registration.unregister();
			} catch (IllegalStateException e) {
				// already unregistered by the test
			}
		}
	}

	@Test
	public void testHighestRankedModifierIsInstalled() {
		register("a", 1);
		register("b", 5);
		register("c", 3);

		assertEquals("file:/b.png", resolvedURL());
	}

	@Test
	public void testRemovingInstalledModifierFallsBackToNext() {
		register("a", 1);
		ServiceRegistration<IImageURLModifier> b = register("b", 5);

		b.unregister();
		assertEquals("file:/a.png", resolvedURL());

		registrations.get(0).unregister();
		assertEquals("file:/original.png", resolvedURL());
	}

	@Test
	public void testRankingChangeIsApplied() {
		ServiceRegistration<IImageURLModifier> a = register("a", 1);
		register("b", 5);

		a.setProperties(FrameworkUtil.asDictionary(Map.of(Constants.SERVICE_RANKING, 10)));

		assertEquals("file:/a.png", resolvedURL());
	}

	private ServiceRegistration<IImageURLModifier> register(String name, int ranking) {
		IImageURLModifier modifier = url -> {
			try {
				return URI.create("file:/" + name + ".png").toURL();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		};
		ServiceRegistration<IImageURLModifier> registration = context.registerService(IImageURLModifier.class,
				modifier, FrameworkUtil.asDictionary(Map.of(Constants.SERVICE_RANKING, ranking)));
		registrations.add(registration);
		return registration;
	}

	private String resolvedURL() {
		return Adapters.adapt(descriptor, URL.class).toExternalForm();
	}
}
