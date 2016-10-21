/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.contenttype.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/*
 * TODO get this suite to also include ContentTypeTests contributed in
 * eclipse.platform.resources repository (and move those tests here)
 */
@RunWith(Suite.class)
@SuiteClasses(value = { UserContentTypeTest.class, })
public class AllTests {

}
