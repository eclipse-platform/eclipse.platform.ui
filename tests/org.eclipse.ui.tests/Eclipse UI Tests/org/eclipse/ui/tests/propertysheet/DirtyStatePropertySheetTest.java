/*******************************************************************************
 * Copyright (c) 2016 Andrey Loskutov <loskutov@gmx.de>.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.propertysheet;

import static org.eclipse.ui.internal.SaveableHelper.isDirtyStateSupported;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISecondarySaveableSource;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.5
 */
@RunWith(JUnit4.class)
public class DirtyStatePropertySheetTest extends AbstractPropertySheetTest {

	private AdaptingSaveableView saveableView;
	private ISecondarySaveableSource dirtyDisallowed;
	private ISecondarySaveableSource dirtyAllowed;
	private MockAdapterFactory adapterFactory;

	static class MockAdapterFactory implements IAdapterFactory {

		private final Map<Class<?>, Object> adaptersMap;

		public MockAdapterFactory() {
			adaptersMap = new HashMap<>();
		}

		@Override
		public Class<?>[] getAdapterList() {
			return adaptersMap.keySet().toArray(new Class[0]);
		}

		public <T> void setAdapter(Class<T> clazz, T adapter) {
			adaptersMap.put(clazz, adapter);
		}

		@Override
		public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
			return adapterType.cast(adaptersMap.get(adapterType));
		}
	}

	public DirtyStatePropertySheetTest() {
		super(DirtyStatePropertySheetTest.class.getName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		PropertySheetPerspectiveFactory.applyPerspective(activePage);
		dirtyDisallowed = new ISecondarySaveableSource() {
		};
		dirtyAllowed = new ISecondarySaveableSource() {
			@Override
			public boolean isDirtyStateSupported() {
				return true;
			}
		};
		adapterFactory = new MockAdapterFactory();
		propertySheet = (PropertySheet) activePage.showView(IPageLayout.ID_PROP_SHEET);
		saveableView = (AdaptingSaveableView) activePage.showView(AdaptingSaveableView.ID_ADAPTING_SAVEABLE);

		// some basic checks
		assertEquals(activePage.getActivePart(), saveableView);
		PropertySheetPage page = (PropertySheetPage) propertySheet.getCurrentPage();
		assertEquals(saveableView, page.getAdapter(ISaveablePart.class));
		assertEquals(saveableView, propertySheet.getAdapter(ISaveablePart.class));
		assertFalse(propertySheet.isDirtyStateSupported());
	}

	@Override
	protected void doTearDown() throws Exception {
		Platform.getAdapterManager().unregisterAdapters(adapterFactory);
		activePage.resetPerspective();
		super.doTearDown();
	}

	@Test
	public void testIsDirtyStateIndicationSupported() throws Exception {
		assertTrue(isDirtyStateSupported(saveableView));

		// override default for this view
		saveableView.setAdapter(ISecondarySaveableSource.class, dirtyAllowed);
		assertTrue(isDirtyStateSupported(propertySheet));

		// check if we can also reset it back
		saveableView.setAdapter(ISecondarySaveableSource.class, dirtyDisallowed);
		assertFalse(isDirtyStateSupported(propertySheet));

		// check if we can set global adapter, set to default first (no
		// adapters)
		saveableView.setAdapter(ISecondarySaveableSource.class, null);
		assertFalse(isDirtyStateSupported(propertySheet));

		// register a global adapter and test
		adapterFactory.setAdapter(ISecondarySaveableSource.class, dirtyAllowed);
		Platform.getAdapterManager().registerAdapters(adapterFactory, ISecondarySaveableSource.class);
		assertTrue(isDirtyStateSupported(propertySheet));
	}

}
