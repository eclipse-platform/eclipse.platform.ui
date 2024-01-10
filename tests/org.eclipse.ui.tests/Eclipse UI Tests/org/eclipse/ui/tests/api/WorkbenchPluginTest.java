/*******************************************************************************
 * Copyright (c) 2023 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeFalse;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Platform.OS;
import org.eclipse.core.tests.harness.TestBarrier2;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.junit.Test;

public class WorkbenchPluginTest {
	/**
	 * The test is supposed to initialize the image registry of an AbstractUIPlugin
	 * from another thread. To this end, we use a new plug-in instance to simulate
	 * the behavior of any specialization of an AbstractUIPlugin (like
	 * WorkbenchPluginTest) being first accessed from another thread.
	 */
	private final AbstractUIPlugin testPlugin = new AbstractUIPlugin() {
	};

	@Test
	public void testGetImageRegistryFromAdditionalDisplay() {
		assumeFalse("multiple displays are not allowed on Linux", OS.isLinux());
		assumeFalse("multiple displays are not allowed on MaxOS", OS.isMac());

		String dummyFilename = "test"; //$NON-NLS-1$
		Display displayInOtherThread = initializeDisplayInOtherThread();
		try {
			displayInOtherThread.syncExec(() -> getFileIcon(dummyFilename));
			final Image iconFromMainThread = getFileIcon(dummyFilename);
			disposeDisplay(displayInOtherThread);
			assertThat("icon retrieved in main thread has been disposed through other display",
					iconFromMainThread.isDisposed(), is(false));
		} finally {
			disposeDisplay(displayInOtherThread);
		}
	}

	private Image getFileIcon(String filename) {
		ImageRegistry registry = testPlugin.getImageRegistry();
		Image fileIcon = registry.get(filename);
		if (fileIcon != null) {
			return fileIcon;
		}
		ImageDescriptor imageDescriptor = PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(filename);
		if (imageDescriptor != null) {
			registry.put(filename, imageDescriptor);
		}
		return registry.get(filename);
	}

	private Display initializeDisplayInOtherThread() {
		AtomicReference<Display> displayReference = new AtomicReference<>();
		TestBarrier2 displayCreationBarrier = new TestBarrier2();
		new Thread(() -> {
			Display display = new Display();
			displayCreationBarrier.setStatus(TestBarrier2.STATUS_DONE);
			displayReference.set(display);
			while (!display.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		}, "async display creation").start();
		displayCreationBarrier.waitForStatus(TestBarrier2.STATUS_DONE);
		return displayReference.get();
	}

	private void disposeDisplay(Display display) {
		if (!display.isDisposed()) {
			display.syncExec(() -> display.dispose());
		}
	}

}
