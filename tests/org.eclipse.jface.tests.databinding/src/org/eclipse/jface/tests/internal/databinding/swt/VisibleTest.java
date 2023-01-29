package org.eclipse.jface.tests.internal.databinding.swt;

import static org.junit.Assert.assertThrows;

import java.util.function.Function;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.swt.SWTObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.util.TestCollection;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.swt.widgets.Widget;
import org.junit.Test;

/**
 * Test for {@link WidgetProperties#visible}.
 */
public class VisibleTest extends AbstractDefaultRealmTestCase {
	public static void addConformanceTest(TestCollection suite) {
		suite.addTest(SWTMutableObservableValueContractTest.class, new Delegate<>(shell -> shell));
		suite.addTest(SWTMutableObservableValueContractTest.class, new Delegate<>(Menu::new));
		suite.addTest(SWTMutableObservableValueContractTest.class,
				new Delegate<>(shell -> new ToolTip(shell, SWT.BALLOON)));
		suite.addTest(SWTMutableObservableValueContractTest.class,
				new Delegate<>(shell -> new ToolBar(shell, SWT.HORIZONTAL)));
		suite.addTest(SWTMutableObservableValueContractTest.class, new Delegate<>(Shell::getHorizontalBar));

		suite.addTest(SWTObservableValueContractTest.class, new Delegate<>(shell -> shell));
		suite.addTest(SWTObservableValueContractTest.class, new Delegate<>(Menu::new));
		suite.addTest(SWTObservableValueContractTest.class, new Delegate<>(shell -> new ToolTip(shell, SWT.BALLOON)));
		suite.addTest(SWTObservableValueContractTest.class,
				new Delegate<>(shell -> new ToolBar(shell, SWT.HORIZONTAL)));
		suite.addTest(SWTObservableValueContractTest.class, new Delegate<>(Shell::getHorizontalBar));
	}

	@Test
	public void testUnsupportedWidget() {
		// A widget that isn't supported
		Tracker tracker = new Tracker(new Shell(), SWT.NONE);
		assertThrows(IllegalArgumentException.class, () -> WidgetProperties.visible().observe(tracker));
	}

	static class Delegate<W extends Widget> extends AbstractObservableValueContractDelegate {
		private Shell shell;
		private W widget;

		private Function<Shell, W> widgetFactory;

		public Delegate(Function<Shell, W> widgetFactory) {
			super();
			this.widgetFactory = widgetFactory;
		}

		@Override
		public void setUp() {
			shell = new Shell(SWT.H_SCROLL);
			widget = widgetFactory.apply(shell);
		}

		@Override
		public void tearDown() {
			shell.dispose();
		}

		@Override
		public IObservableValue<?> createObservableValue(Realm realm) {
			return WidgetProperties.visible().observe(realm, widget);
		}

		@Override
		public void change(IObservable observable) {
			@SuppressWarnings("unchecked")
			IObservableValue<Boolean> value = (IObservableValue<Boolean>) observable;
			value.setValue(!value.getValue());
		}

		@Override
		public Object getValueType(IObservableValue<?> observable) {
			return Boolean.class;
		}

		@Override
		public Object createValue(IObservableValue<?> observable) {
			return !(Boolean) observable.getValue();
		}
	}
}
