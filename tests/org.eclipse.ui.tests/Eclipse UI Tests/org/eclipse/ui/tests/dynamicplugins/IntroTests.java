/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertThrows;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.intro.IIntroDescriptor;
import org.eclipse.ui.internal.intro.IIntroRegistry;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.tests.leaks.LeakTests;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class IntroTests extends DynamicTestCase {

	private static final String PRODUCT_ID = "org.eclipse.ui.tests.someProduct";
	private static final String INTRO_ID = "org.eclipse.newIntro1.newIntro1";
	private IntroDescriptor oldDesc;
	private IWorkbenchWindow window;


	public IntroTests() {
		super(IntroTests.class.getSimpleName());
	}

	@Test
	public void testIntroClosure() throws IllegalArgumentException, InterruptedException {
		getBundle();
		Workbench workbench = Workbench.getInstance();
		IntroDescriptor testDesc = (IntroDescriptor) WorkbenchPlugin
		.getDefault().getIntroRegistry().getIntro(
				INTRO_ID);
		workbench.setIntroDescriptor(testDesc);

		ReferenceQueue<IIntroPart> queue = new ReferenceQueue<>();
		IIntroPart intro = workbench.getIntroManager().showIntro(window, false);
		WeakReference<IIntroPart> ref = new WeakReference<>(intro, queue);
		assertNotNull(intro);
		intro = null; //null the reference
		removeBundle();
		LeakTests.checkRef(queue, ref);

		assertNull(workbench.getIntroManager().getIntro());
	}

	@Test
	public void testIntroProperties() throws CoreException {
		IIntroRegistry registry = WorkbenchPlugin.getDefault().getIntroRegistry();
		assertNull(registry.getIntroForProduct(PRODUCT_ID));
		assertNull(registry.getIntro(INTRO_ID));
		getBundle();
		assertNotNull(registry.getIntroForProduct(PRODUCT_ID));
		IIntroDescriptor desc = registry.getIntro(INTRO_ID);
		assertNotNull(desc);
		testIntroProperties(desc);
		removeBundle();
		assertNull(registry.getIntro(INTRO_ID));
		assertNull(registry.getIntroForProduct(PRODUCT_ID));
		assertThrows(RuntimeException.class, () -> testIntroProperties(desc));
	}

	private void testIntroProperties(IIntroDescriptor desc) throws CoreException {
		assertNotNull(desc.getId());
		try {
			assertNotNull(desc.createIntro());
		}
		catch (CoreException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	protected String getExtensionId() {
		return "newIntro1.testDynamicIntroAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_INTRO;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newIntro1";
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		oldDesc = Workbench.getInstance().getIntroDescriptor();
		window = openTestWindow();
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		Workbench.getInstance().setIntroDescriptor(oldDesc);
	}

	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicIntro";
	}
}
