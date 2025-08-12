/*******************************************************************************
 * Copyright (c) 2014  Thibault Le Ouay and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 450212
 ******************************************************************************/

package org.eclipse.e4.emf.xpath.test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ //
		ExampleQueriesTestCase.class, //
		ExampleQueriesApplicationTest.class, //
})
public class EMFTestSuite {

}
