package org.eclipse.core.tests.internal.plugins;

import junit.framework.*;

public class AllTests extends TestCase {

public AllTests() {
	super(null);
}

public AllTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(PluginResolveTest_1.suite());
	suite.addTest(PluginResolveTest_2.suite());
	suite.addTest(PluginResolveTest_3.suite());
	suite.addTest(PluginResolveTest_4.suite());
	suite.addTest(PluginResolveTest_5.suite());
	suite.addTest(PluginResolveTest_6.suite());
	suite.addTest(PluginResolveTest_7.suite());
	suite.addTest(PluginResolveTest_8.suite());
	suite.addTest(PluginResolveTest_9.suite());
	suite.addTest(PluginResolveTest_10.suite());
	suite.addTest(PluginResolveTest_11.suite());
	suite.addTest(PluginResolveTest_12.suite());
	suite.addTest(PluginResolveTest_13.suite());
	suite.addTest(PluginResolveTest_14.suite());
	suite.addTest(PluginResolveTest_15.suite());
	suite.addTest(PluginResolveTest_16.suite());
	suite.addTest(PluginResolveTest_17.suite());
	suite.addTest(PluginResolveTest_18.suite());
	suite.addTest(PluginResolveTest_19.suite());
	suite.addTest(PluginResolveTest_20.suite());
	suite.addTest(PluginResolveTest_21.suite());
	suite.addTest(PluginResolveTest_22.suite());
	suite.addTest(PluginResolveTest_23.suite());
	suite.addTest(PluginResolveTest_24.suite());
	suite.addTest(PluginResolveTest_25.suite());
	suite.addTest(PluginResolveTest_26.suite());
	suite.addTest(PluginResolveTest_27.suite());
	suite.addTest(PluginResolveTest_28.suite());
	suite.addTest(PluginResolveTest_29.suite());
	suite.addTest(PluginResolveTest_30.suite());
	suite.addTest(PluginResolveTest_31.suite());
	suite.addTest(PluginResolveTest_32.suite());
	suite.addTest(PluginResolveTest_33.suite());
	suite.addTest(PluginVersionTest.suite());
	suite.addTest(PluginVersionTest_2.suite());
	suite.addTest(PluginXmlTest.suite());
	suite.addTest(BasicXMLTest.suite());
	suite.addTest(NumberOfElementsTest.suite());
	suite.addTest(ExtensiveLibraryTest.suite());
	suite.addTest(ExtensiveRequiresTest.suite());
	suite.addTest(BadPluginsTest.suite());
	suite.addTest(ExtensiveFragmentTest.suite());
	suite.addTest(BasicFragmentTest.suite());
	suite.addTest(SoftPrereqTest_1.suite());
	suite.addTest(SoftPrereqTest_2.suite());
	suite.addTest(SoftPrereqTest_3.suite());
	suite.addTest(SoftPrereqTest_4.suite());
	suite.addTest(SoftPrereqTest_5.suite());
	suite.addTest(SoftPrereqTest_6.suite());
	suite.addTest(SoftPrereqTest_7.suite());
	suite.addTest(SoftPrereqTest_8.suite());
	suite.addTest(SoftPrereqTest_9.suite());
	suite.addTest(SoftPrereqTest_10.suite());
	suite.addTest(SoftPrereqTest_11.suite());
	suite.addTest(SoftPrereqTest_12.suite());
	suite.addTest(SoftPrereqTest_13.suite());
	suite.addTest(SoftPrereqTest_14.suite());
	suite.addTest(SoftPrereqTest_15.suite());
	suite.addTest(SoftPrereqTest_16.suite());
	suite.addTest(SoftPrereqTest_17.suite());
	suite.addTest(SoftPrereqTest_18.suite());
	suite.addTest(SoftPrereqTest_19.suite());
	suite.addTest(SoftPrereqTest_20.suite());
	suite.addTest(SoftPrereqTest_21.suite());
	suite.addTest(SoftPrereqTest_22.suite());
	suite.addTest(ConflictResolveTest_1.suite());
	suite.addTest(ConflictResolveTest_2.suite());
	suite.addTest(ConflictResolveTest_3.suite());
	suite.addTest(ConflictResolveTest_4.suite());
	suite.addTest(ConflictResolveTest_5.suite());
	suite.addTest(ConflictResolveTest_6.suite());
	suite.addTest(ConflictResolveTest_7.suite());
	suite.addTest(CircularTest.suite());
	suite.addTest(RegressionResolveTest_1.suite());
	suite.addTest(RegressionResolveTest_2.suite());
	suite.addTest(RegressionResolveTest_3.suite());
	suite.addTest(RegressionResolveTest_4.suite());
	suite.addTest(RegressionResolveTest_5.suite());
	suite.addTest(RegressionResolveTest_6.suite());
	suite.addTest(RegressionResolveTest_7.suite());
	suite.addTest(RegressionResolveTest_8.suite());
	suite.addTest(RegressionResolveTest_9.suite());
	suite.addTest(RegressionResolveTest_10.suite());
	suite.addTest(FragmentResolveTest_1.suite());
	suite.addTest(FragmentResolveTest_2.suite());
	suite.addTest(FragmentResolveTest_3.suite());
	suite.addTest(FragmentResolveTest_4.suite());
	suite.addTest(FragmentResolveTest_5.suite());
	suite.addTest(FragmentResolveTest_6.suite());
	suite.addTest(FragmentResolveTest_7.suite());
	suite.addTest(FragmentResolveTest_8.suite());
	suite.addTest(FragmentResolveTest_9.suite());
	suite.addTest(FragmentResolveTest_10.suite());
	suite.addTest(FragmentResolveTest_11.suite());
	suite.addTest(FragmentResolveTest_12.suite());
	suite.addTest(FragmentResolveTest_13.suite());
	return suite;
}
}
