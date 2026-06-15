/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Heiko Klare - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.addons;

import org.eclipse.e4.ui.workbench.addons.cleanupaddon.CleanupAddonTest;
import org.eclipse.e4.ui.workbench.addons.dndaddon.StackDropAgentTest;
import org.eclipse.e4.ui.workbench.addons.minmax.MaximizableChildrenTagTest;
import org.eclipse.e4.ui.workbench.addons.minmax.MaximizeBugTest;
import org.eclipse.e4.ui.workbench.addons.minmax.MaximizePartSashContainerPlaceholderTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ //
		StackDropAgentTest.class, //
		MaximizeBugTest.class, //
		MaximizePartSashContainerPlaceholderTest.class, //
		MaximizableChildrenTagTest.class, //
		CleanupAddonTest.class, //
})
public class AllTests {

}
