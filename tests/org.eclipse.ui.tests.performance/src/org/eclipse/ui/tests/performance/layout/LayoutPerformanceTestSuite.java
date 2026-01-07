package org.eclipse.ui.tests.performance.layout;

import java.util.stream.Stream;

import org.eclipse.ui.tests.performance.UIPerformanceTestRule;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ //
		ComputeSizeTest.class, //
		LayoutTest.class, //
		ResizeTest.class //
})
public class LayoutPerformanceTestSuite {

	public static Stream<Arguments> data() {
		return Stream.of(
				Arguments.of(new ViewWidgetFactory(UIPerformanceTestRule.INTRO_VIEW), false),
				Arguments.of(new EditorWidgetFactory("1.perf_basic"), false)
		);
	}
}
