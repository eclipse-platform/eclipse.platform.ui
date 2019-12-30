package org.eclipse.jface.tests.internal.databinding.swt;

import static org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest.suite;

import java.util.function.Function;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Widget;

import junit.framework.TestSuite;

/**
 * Test for {@link WidgetProperties#visible}.
 */
public class VisibleTest extends AbstractDefaultRealmTestCase {
	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(suite(new Delegate<>(shell -> shell)));
		suite.addTest(suite(new Delegate<>(Menu::new)));
		suite.addTest(suite(new Delegate<>(shell -> new ToolTip(shell, SWT.BALLOON))));
		suite.addTest(suite(new Delegate<>(shell -> new ToolBar(shell, SWT.HORIZONTAL))));
		suite.addTest(suite(new Delegate<>(Shell::getHorizontalBar)));
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
