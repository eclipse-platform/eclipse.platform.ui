/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Red Hat Inc. - Bug 474132
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 504029
 *     Lucas Bullen (Red Hat Inc.) - Bugs 519525, 520250, and 520251
 *     Tim Neumann <tim.neumann@advantest.com> - Bug 485167
 *******************************************************************************/
package org.eclipse.ui.tests;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Test all areas of the UI.
 */
@Suite
@SelectPackages({ "org.eclipse.ui.tests", "org.eclipse.ui.internal.ide" })
public class UiTestSuite {
}
