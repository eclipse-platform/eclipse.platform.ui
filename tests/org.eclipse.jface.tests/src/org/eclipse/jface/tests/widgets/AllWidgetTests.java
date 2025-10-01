/*******************************************************************************
 * Copyright (c) 2018, 2019 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.jface.tests.widgets;

import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SelectClasses;

@Suite
@SelectClasses({ TestUnitButtonFactory.class, //
		TestUnitCompositeFactory.class, //
		TestUnitControlFactory.class, //
		TestUnitGroupFactory.class, //
		TestUnitItemFactory.class, //
		TestUnitLabelFactory.class, //
		TestUnitLinkFactory.class, //
		TestUnitSashFactory.class, //
		TestUnitSashFormFactory.class, //
		TestUnitShellFactory.class, //
		TestUnitSpinnerFactory.class, //
		TestUnitTableFactory.class, //
		TestUnitTableColumnFactory.class, //
		TestUnitTextFactory.class, //
		TestUnitTreeColumnFactory.class, //
		TestUnitTreeFactory.class, //
		TestUnitWidgetFactory.class, //
})
public class AllWidgetTests {

}
