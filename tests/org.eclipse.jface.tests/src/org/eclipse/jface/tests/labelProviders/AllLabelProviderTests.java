package org.eclipse.jface.tests.labelProviders;

import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SelectClasses;

@Suite
@SelectClasses({ ColorAndFontLabelProviderTest.class, //
		ColorAndFontViewerLabelProviderTest.class, //
		ColumnLabelProviderLambdaTest.class, //
		CompositeLabelProviderTableTest.class, //
		DecoratingLabelProviderTests.class, //
		DecoratingLabelProviderTreePathTest.class, //
		DecoratingLabelProviderTreeTest.class, //
		DecoratingStyledCellLabelProviderTest.class, //
		IDecorationContextTest.class, //
		LabelProviderLambdaTest.class, //
		LabelProviderTest.class, //
})
public class AllLabelProviderTests {

}
