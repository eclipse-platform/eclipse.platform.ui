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

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.resource.IImageURLModifier;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageFileNameProvider;
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

	private Path tempDir;

	private ImageDescriptor descriptor;

	@BeforeEach
	public void setUp() throws IOException {
		tempDir = Files.createTempDirectory("ImageURLModifierTrackerTest");
		descriptor = ImageDescriptor.createFromURL(image("original").toUri().toURL());
	}

	@AfterEach
	public void tearDown() throws IOException {
		for (ServiceRegistration<IImageURLModifier> registration : registrations) {
			try {
				registration.unregister();
			} catch (IllegalStateException e) {
				// already unregistered by the test
			}
		}
		try (Stream<Path> files = Files.list(tempDir)) {
			for (Path file : files.toList()) {
				Files.delete(file);
			}
		}
		Files.delete(tempDir);
	}

	@Test
	public void testHighestRankedModifierIsInstalled() throws IOException {
		register("a", 1);
		register("b", 5);
		register("c", 3);
		assertEquals("b.png", loadedFileName());
	}

	@Test
	public void testRemovingInstalledModifierFallsBackToNext() throws IOException {
		register("a", 1);
		ServiceRegistration<IImageURLModifier> b = register("b", 5);

		b.unregister();
		assertEquals("a.png", loadedFileName());

		registrations.get(0).unregister();
		assertEquals("original.png", loadedFileName());
	}

	@Test
	public void testRankingChangeIsApplied() throws IOException {
		ServiceRegistration<IImageURLModifier> a = register("a", 1);
		register("b", 5);

		a.setProperties(FrameworkUtil.asDictionary(Map.of(Constants.SERVICE_RANKING, 10)));
		assertEquals("a.png", loadedFileName());
	}

	private ServiceRegistration<IImageURLModifier> register(String name, int ranking) throws IOException {
		Path replacement = image(name);
		IImageURLModifier modifier = _ -> {
			try {
				return replacement.toUri().toURL();
			} catch (MalformedURLException e) {
				throw new IllegalStateException(e);
			}
		};
		ServiceRegistration<IImageURLModifier> registration = context.registerService(IImageURLModifier.class,
				modifier, FrameworkUtil.asDictionary(Map.of(Constants.SERVICE_RANKING, ranking)));
		registrations.add(registration);
		return registration;
	}

	private Path image(String name) throws IOException {
		Path file = tempDir.resolve(name + ".png");
		if (!Files.exists(file)) {
			Files.createFile(file);
		}
		return file;
	}

	private String loadedFileName() {
		String path = Adapters.adapt(descriptor, ImageFileNameProvider.class).getImagePath(100);
		return Path.of(path).getFileName().toString();
	}
}
