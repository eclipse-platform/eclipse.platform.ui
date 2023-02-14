/*******************************************************************************
 * Copyright (c) 2022, 2023 EclipseSource GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     EclipseSource GmbH - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.compatibility.migration.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.e4.tools.compatibility.migration.tests.util.TestWorkbenchAdvisor;
import org.eclipse.e4.tools.internal.compatibiliy.migration.E4MigrationTool;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class E4MigrationToolTest {

	private Display display = null;

	@BeforeEach
	void setUp() throws Exception {
		display = PlatformUI.createDisplay();

	}

	@AfterEach
	void tearDown() throws Exception {
		display.dispose();
	}


	@Test
	void testConvert() throws FileNotFoundException, IOException, WorkbenchException, InterruptedException, ExecutionException {

		IMemento memento;
		try (FileInputStream input = new FileInputStream(
				Paths.get("resources/perspective_3x.xml").toAbsolutePath().toString())) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
			memento = XMLMemento.createReadRoot(reader);
		}

		CompletableFuture<MApplication> future = new CompletableFuture<>();
		TestWorkbenchAdvisor testWorkbenchAdvisor = new TestWorkbenchAdvisor() {

			@Override
			public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
				super.createWorkbenchWindowAdvisor(configurer);
				MApplication mApplication = E4MigrationTool.convert(memento);
				future.complete(mApplication);
				PlatformUI.getWorkbench().close();
			}

		};
		PlatformUI.createAndRunWorkbench(display, testWorkbenchAdvisor);
		MApplication mApplication = future.get();

		assertEquals(1, mApplication.getChildren().size());

		MWindowElement mWindowElement = mApplication.getChildren().get(0).getChildren().get(0);
		@SuppressWarnings("unchecked")
		MElementContainer<MPerspectiveStack> windowContainer = (MElementContainer<MPerspectiveStack>)mWindowElement;
		assertEquals(3, windowContainer.getChildren().get(0).getChildren().size());
		MPerspective resourceExportPerspective = windowContainer.getChildren().get(0).getChildren().get(0);
		assertEquals("ResourceExport", resourceExportPerspective.getElementId());

	}

	@Test
	void testFromSource() throws InterruptedException, ExecutionException {

		CompletableFuture<Boolean> future = new CompletableFuture<>();
		TestWorkbenchAdvisor testWorkbenchAdvisor = new TestWorkbenchAdvisor() {

			@Override
			public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
				super.createWorkbenchWindowAdvisor(configurer);
				try {
					E4MigrationTool.convert(Paths.get("resources/perspective_3x.xml").toFile(),
							Paths.get("resources/e4_test.xml").toFile());
				} catch (WorkbenchException | IOException e) {
					e.printStackTrace();
					future.complete(false);
					PlatformUI.getWorkbench().close();
				}
				future.complete(true);
				PlatformUI.getWorkbench().close();
			}

		};
		PlatformUI.createAndRunWorkbench(display, testWorkbenchAdvisor);
		Boolean result = future.get();
		if(!result) {
			fail();
		}
	}

}
