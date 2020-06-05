/*******************************************************************************
 * Copyright (c) 2020 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.databinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPageService;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.databinding.typed.WorkbenchProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link WorkbenchProperties}.
 */
public class WorkbenchDatabindingTest {
	private static TestRealm realm;

	@BeforeClass
	public static void setup() {
		realm = new TestRealm();
	}

	@Test
	public void testActiveWindow() {
		IWorkbench workbench = mock(IWorkbench.class);
		IWorkbenchWindow win1 = mock(IWorkbenchWindow.class);
		IWorkbenchWindow win2 = mock(IWorkbenchWindow.class);

		when(workbench.getActiveWorkbenchWindow()) //
				.thenReturn(win1, win1, win2) // One initial value, on when listener is added, one after change
				.thenThrow(new AssertionError());

		AtomicReference<IWindowListener> listener = new AtomicReference<>();
		doAnswer(inv -> {
			listener.set((IWindowListener) inv.getArgument(0));
			return null;
		}).when(workbench).addWindowListener(any());

		List<ValueChangeEvent<?>> events = new ArrayList<>();

		IObservableValue<IWorkbenchWindow> value = WorkbenchProperties.activeWindow().observe(workbench);

		// Check initial value
		assertSame(win1, value.getValue());

		IValueChangeListener<Object> changeListener = events::add;
		value.addValueChangeListener(changeListener);

		// Update using listener
		listener.get().windowActivated(win2);

		assertSame(win2, value.getValue());
		assertEquals(1, events.size());
		ValueDiff<?> diff = events.get(0).diff;
		assertEquals(win1, diff.getOldValue());
		assertEquals(win2, diff.getNewValue());

		// Other window is deactivated
		listener.get().windowDeactivated(win1);

		assertSame(win2, value.getValue());
		assertEquals(1, events.size());

		// Active window is deactivated
		listener.get().windowDeactivated(win2);

		assertNull(value.getValue());
		assertEquals(2, events.size());

		value.removeValueChangeListener(changeListener);
		value.dispose();
	}

	@Test
	public void testActiveWindowValueType() {
		IObservableValue<IWorkbenchWindow> value = WorkbenchProperties.activeWindow().observe(mock(IWorkbench.class));
		assertEquals(IWorkbenchWindow.class, value.getValueType());
		assertEquals(IWorkbenchWindow.class, WorkbenchProperties.activeWindow().getValueType());
	}

	@Test
	public void testActivePage() {
		IWorkbenchWindow window = mock(IWorkbenchWindow.class);
		IWorkbenchPage page1 = mock(IWorkbenchPage.class);
		IWorkbenchPage page2 = mock(IWorkbenchPage.class);

		when(window.getActivePage()) //
				.thenReturn(page1, page1, page2) // One initial value, on when listener is added, one after change
				.thenThrow(new AssertionError());

		AtomicReference<IPageListener> listener = new AtomicReference<>();
		doAnswer(inv -> {
			listener.set((IPageListener) inv.getArgument(0));
			return null;
		}).when(window).addPageListener(any());

		List<ValueChangeEvent<?>> events = new ArrayList<>();

		IObservableValue<IWorkbenchPage> value = WorkbenchProperties.activePage().observe(window);

		// Check initial value
		assertSame(page1, value.getValue());

		IValueChangeListener<Object> changeListener = events::add;
		value.addValueChangeListener(changeListener);

		// Update using listener
		listener.get().pageActivated(page2);

		assertSame(page2, value.getValue());
		assertEquals(1, events.size());
		ValueDiff<?> diff = events.get(0).diff;
		assertEquals(page1, diff.getOldValue());
		assertEquals(page2, diff.getNewValue());

		// Other page is closed
		listener.get().pageClosed(page1);

		assertSame(page2, value.getValue());
		assertEquals(1, events.size());

		// Active page is closed
		listener.get().pageClosed(page2);

		assertNull(value.getValue());
		assertEquals(2, events.size());

		value.removeValueChangeListener(changeListener);
		value.dispose();
	}

	@Test
	public void testActivePageValueType() {
		IObservableValue<IWorkbenchPage> value = WorkbenchProperties.activePage().observe(mock(IPageService.class));
		assertEquals(IWorkbenchPage.class, value.getValueType());
		assertEquals(IWorkbenchPage.class, WorkbenchProperties.activePage().getValueType());
	}

	@Test
	public void testActivePartReference() {
		IPartService service = mock(IPartService.class);
		IWorkbenchPartReference part1 = mock(IWorkbenchPartReference.class);
		IWorkbenchPartReference part2 = mock(IWorkbenchPartReference.class);

		when(service.getActivePartReference()) //
				.thenReturn(part1, part1, part2) // One initial value, on when listener is added, one after change
				.thenThrow(new AssertionError());

		AtomicReference<IPartListener2> listener = new AtomicReference<>();
		doAnswer(inv -> {
			listener.set((IPartListener2) inv.getArgument(0));
			return null;
		}).when(service).addPartListener(any(IPartListener2.class));

		List<ValueChangeEvent<?>> events = new ArrayList<>();

		IObservableValue<IWorkbenchPartReference> value = WorkbenchProperties.activePartReference().observe(service);

		// Check initial value
		assertSame(part1, value.getValue());

		IValueChangeListener<Object> changeListener = events::add;
		value.addValueChangeListener(changeListener);

		// Update using listener
		listener.get().partActivated(part2);

		assertSame(part2, value.getValue());
		assertEquals(1, events.size());
		ValueDiff<?> diff = events.get(0).diff;
		assertEquals(part1, diff.getOldValue());
		assertEquals(part2, diff.getNewValue());

		// Other part is deactivated
		listener.get().partDeactivated(part1);

		assertSame(part2, value.getValue());
		assertEquals(1, events.size());

		// Active part is deactivated
		listener.get().partDeactivated(part2);

		assertNull(value.getValue());
		assertEquals(2, events.size());

		value.removeValueChangeListener(changeListener);
		value.dispose();
	}

	@Test
	public void testActivePartReferenceValueType() {
		IObservableValue<IWorkbenchPartReference> value = WorkbenchProperties.activePartReference().observe(mock(IPartService.class));
		assertEquals(IWorkbenchPartReference.class, value.getValueType());
		assertEquals(IWorkbenchPartReference.class, WorkbenchProperties.activePartReference().getValueType());
	}

	/**
	 * {@link WorkbenchProperties#activeEditorReference} is implemented using
	 * {@link WorkbenchProperties#activePartReference}, so we only need to verify
	 * that the conversion works.
	 */
	@Test
	public void testActiveEditorReference() {
		IPartService service = mock(IPartService.class);
		IWorkbenchPartReference part = mock(IWorkbenchPartReference.class);
		IEditorReference editor = mock(IEditorReference.class);

		when(service.getActivePartReference()) //
				.thenReturn(part, editor) // One initial value, on when listener is added, one after change
				.thenThrow(new AssertionError());

		IValueProperty<IPartService, IEditorReference> prop = WorkbenchProperties.activePartAsEditorReference();

		assertNull(prop.getValue(service));
		assertSame(editor, prop.getValue(service));
	}

	@Test
	public void testActiveEditorReferenceValueType() {
		IObservableValue<IEditorReference> value = WorkbenchProperties.activePartAsEditorReference().observe(mock(IPartService.class));
		assertEquals(IEditorReference.class, value.getValueType());
		assertEquals(IEditorReference.class, WorkbenchProperties.activePartAsEditorReference().getValueType());
	}

	/**
	 * {@link WorkbenchProperties#activeEditorReference} is implemented using
	 * {@link WorkbenchProperties#activePartReference}, so we only need to verify
	 * that the conversion works.
	 */
	@Test
	public void testEditorInput() {
		IEditorPart editor = mock(IEditorPart.class);
		IEditorInput input1 = mock(IEditorInput.class);
		IEditorInput input2 = mock(IEditorInput.class);

		when(editor.getEditorInput()) //
				.thenReturn(input1, input1, input2) // One initial value, on when listener is added, one after change
				.thenThrow(new AssertionError());

		AtomicReference<IPropertyListener> listener = new AtomicReference<>();
		doAnswer(inv -> {
			listener.set((IPropertyListener) inv.getArgument(0));
			return null;
		}).when(editor).addPropertyListener(any(IPropertyListener.class));

		List<ValueChangeEvent<?>> events = new ArrayList<>();

		IObservableValue<IEditorInput> value = WorkbenchProperties.editorInput().observe(editor);

		// Check initial value
		assertSame(input1, value.getValue());

		IValueChangeListener<Object> changeListener = events::add;
		value.addValueChangeListener(changeListener);

		// Update using listener
		listener.get().propertyChanged(editor, IEditorPart.PROP_INPUT);

		assertSame(input2, value.getValue());
		assertEquals(1, events.size());
		ValueDiff<?> diff = events.get(0).diff;
		assertEquals(input1, diff.getOldValue());
		assertEquals(input2, diff.getNewValue());

		value.removeValueChangeListener(changeListener);
		value.dispose();
	}

	@Test
	public void testEditorInputValueType() {
		IObservableValue<IEditorInput> value = WorkbenchProperties.editorInput().observe(mock(IEditorPart.class));
		assertEquals(IEditorInput.class, value.getValueType());
		assertEquals(IEditorInput.class, WorkbenchProperties.editorInput().getValueType());
	}

	@AfterClass
	public static void teardown() {
		realm.restore();
	}

	private static class TestRealm extends Realm {
		private Realm old;

		public TestRealm() {
			setDefault(this);
		}

		@Override
		public boolean isCurrent() {
			return true;
		}

		public void restore() {
			setDefault(old);
		}
	}
}
