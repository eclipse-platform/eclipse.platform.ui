package org.eclipse.e4.ui.workbench.addons;

import org.eclipse.e4.ui.workbench.addons.cleanupaddon.CleanupAddonTest;
import org.eclipse.e4.ui.workbench.addons.minmax.MaximizableChildrenTag;
import org.eclipse.e4.ui.workbench.addons.minmax.MaximizeBugTest;
import org.eclipse.e4.ui.workbench.addons.minmax.MaximizePartSashContainerPlaceholderTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ //
		MaximizeBugTest.class, //
		MaximizePartSashContainerPlaceholderTest.class, //
		MaximizableChildrenTag.class, //
		CleanupAddonTest.class, //
})
public class AllTests {

}
