/*******************************************************************************
 * Copyright (c) 2003, 2019 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474132
 *     Paul Pazderski - Bug 546537: migrate to JUnit4 suite because EvaluationServiceTest use JUnit4 features
 *******************************************************************************/
package org.eclipse.ui.tests.services;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests general to services.
 * @since 3.3
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	EvaluationServiceTest.class,
	ContributedServiceTest.class,
	WorkbenchSiteProgressServiceTest.class,
// TODO EditorSourceTest.class
	})
public final class ServicesTestSuite {
}
