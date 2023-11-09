/*******************************************************************************
 * Copyright (c) 2012 Brian de Alwis and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brian de Alwis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.menus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.junit.Test;

/**
 * @since 4.2.2
 */
public class MenuHelperTest {
	private final IExtensionRegistry registry = RegistryFactory.getRegistry();

	/**
	 * Verify that MenuHelper#getIconURI(IConfigElement,String) looks unresolved
	 * items in the ISharedImages.
	 */
	@Test
	public void test391232() {
		IExtension extension = registry
				.getExtension("org.eclipse.ui.tests.MenuHelperTest");
		assertNotNull(extension);
		assertTrue(extension.getConfigurationElements().length == 1);
		IConfigurationElement menuContribution = extension
				.getConfigurationElements()[0];
		assertEquals(IWorkbenchRegistryConstants.PL_MENU_CONTRIBUTION,
				menuContribution.getName());
		assertTrue(menuContribution.getChildren().length > 0);
		IConfigurationElement menuElement = menuContribution.getChildren()[0];
		assertEquals(IWorkbenchRegistryConstants.TYPE_MENU,
				menuElement.getName());
		assertNotNull(menuElement
				.getAttribute(IWorkbenchRegistryConstants.ATT_ICON));

		String uri = MenuHelper.getIconURI(menuElement,
				IWorkbenchRegistryConstants.ATT_ICON);
		assertNotNull(uri);
		// contribution specifies "IMG_OBJ_FOLDER"
		assertEquals(
				"platform:/plugin/org.eclipse.ui/icons/full/obj16/fldr_obj.png",
				uri);
	}
}
