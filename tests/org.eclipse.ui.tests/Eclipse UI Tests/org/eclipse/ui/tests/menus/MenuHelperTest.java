/*******************************************************************************
 * Copyright (c) 2012 Brian de Alwis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.menus;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 4.2.2
 */
public class MenuHelperTest extends TestCase {
	private IExtensionRegistry registry = RegistryFactory.getRegistry();

	/**
	 * Verify that MenuHelper#getIconURI(IConfigElement,String) looks unresolved
	 * items in the ISharedImages.
	 */
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
