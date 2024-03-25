package org.eclipse.e4.ui.workbench.addons;

import org.eclipse.e4.ui.workbench.addons.cleanupaddon.CleanupAddonTest;
import org.eclipse.e4.ui.workbench.addons.minmax.MaximizableChildrenTag;
import org.eclipse.e4.ui.workbench.addons.minmax.MaximizeBugTest;
import org.eclipse.e4.ui.workbench.addons.minmax.MaximizePartSashContainerPlaceholderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ //
		MaximizeBugTest.class, //
		MaximizePartSashContainerPlaceholderTest.class, //
		MaximizableChildrenTag.class, //
		CleanupAddonTest.class, //
})
public class AllTests {

}
