/*******************************************************************************
 * Copyright (c) 2019, 2024 Remain Software and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wim Jongman - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.SelectionListenerFactory;
import org.eclipse.ui.SelectionListenerFactory.ISelectionModel;
import org.eclipse.ui.SelectionListenerFactory.Predicates;
import org.eclipse.ui.tests.SelectionProviderView;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the ISelectionService class.
 */
public class SelectionListenerFactoryTest implements ISelectionListener {
	private static final String KNOCK_KNOCK = "KnockKnock";

	private IWorkbenchWindow fWindow;

	private IWorkbenchPage fPage;

	private boolean fEventReceived;

	private int fCounter;
	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Before
	public void doSetUp() throws Exception {
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}

	/**
	 * Purges the UI queue. Use this to let the UI render e.g. after showview.
	 */
	private void purgeQueue() {
		while (fPage.getWorkbenchWindow().getShell().getDisplay().readAndDispatch()) {
		}
	}

	/**
	 * Tests the emptySelection Predicate block
	 */
	@Test
	public void testEmptySelectionListener() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this, Predicates.emptySelection);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection());
		assertFalse("Empty selection not ignored", fEventReceived);
	}

	/**
	 * Tests the emptySelection Predicate allow
	 */
	@Test
	public void testEmptySelectionListener2() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this, Predicates.emptySelection);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Filled selection not delivered", fEventReceived);
	}

	/**
	 * Tests the prevention of offering the same selection twice from the same part.
	 */
	@Test
	public void testAlreadyDeliveredPart1() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this, Predicates.alreadyDelivered);
		fPage.addSelectionListener(listener);
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Same selection was offered twice, same part.", fEventReceived);
	}

	/**
	 * Tests the prevention of offering the same selection twice from the same part
	 * but allow from another part.
	 */
	@Test
	public void testAlreadyDeliveredPart2() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		SelectionProviderView view2 = (SelectionProviderView) fPage.showView(SelectionProviderView.ID_2);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this, Predicates.alreadyDelivered);
		fPage.addSelectionListener(listener);
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		clearEventState();
		view2.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Same selection but different part was not offered.", fEventReceived);
	}

	/**
	 * Tests the prevention of offering the same selection twice from any part 1.
	 */
	@Test
	public void testAlreadyDeliveredAnyPartPart1() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.alreadyDeliveredAnyPart);
		fPage.addSelectionListener(listener);
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		SelectionProviderView view2 = (SelectionProviderView) fPage.showView(SelectionProviderView.ID_2);
		view2.setSelection(new StructuredSelection(KNOCK_KNOCK));
		purgeQueue();
		clearEventState();
		view2.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Same selection was offered twice from other part", fEventReceived);
	}

	/**
	 * Tests the prevention of offering the same selection twice from any part 2.
	 */
	@Test
	public void testAlreadyDeliveredAnyPartPart2() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.alreadyDeliveredAnyPart);
		fPage.addSelectionListener(listener);
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Same selection was offered twice", fEventReceived);
	}

	/**
	 * Tests if the selection is received when the selection part is visible.
	 */
	@Test
	public void testSelectionPartVisibleT1() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.selectionPartVisible);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Selection not received from visible part", fEventReceived);
	}

	/**
	 * Tests that the selection is not received when the selection part is
	 * invisible.
	 */
	@Test
	public void testSelectionPartVisibleT2() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID, null,
				IWorkbenchPage.VIEW_CREATE);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.selectionPartVisible);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Selection offered from hidden part", fEventReceived);
	}

	/**
	 * Tests that the selection is not received when the selection part is
	 * invisible.
	 */
	@Test
	public void testSelectionPartVisibleT3() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID, null,
				IWorkbenchPage.VIEW_CREATE);
		ISelectionListener listener = SelectionListenerFactory.createVisibleListener(view, this, m -> true);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Selection offered from hidden part", fEventReceived);
	}

	/**
	 * Tests if the selection is received when the selection part is visible.
	 */
	@Test
	public void testSelectionPartVisibleT4() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createVisibleListener(view, this, m -> true);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Selection not received from visible part", fEventReceived);
	}

	/**
	 * Tests that the selection is not received from my own part 1.
	 */
	@Test
	public void testSelfMuteT1() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this, Predicates.selfMute);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Selection offered from own part", fEventReceived);
	}

	/**
	 * Tests that the selection is not received from my own part 2.
	 */
	@Test
	public void testSelfMuteT2() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		SelectionProviderView view2 = (SelectionProviderView) fPage.showView(SelectionProviderView.ID_2);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this, Predicates.selfMute);
		fPage.addSelectionListener(listener);
		clearEventState();
		view2.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Selection offered from other part but not received", fEventReceived);
	}

	/**
	 * Tests that the selection is received when the target part is visible.
	 */
	@Test
	public void testTargetPartVisibleT1() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this, Predicates.targetPartVisible);
		fPage.addSelectionListener(listener);
		clearEventState();
		purgeQueue();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Selection not offered", fEventReceived);
	}

	/**
	 * Tests that the selection is not received when the target part is invisible.
	 */
	@Test
	public void testTargetPartVisibleT2() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this, Predicates.targetPartVisible);
		fPage.addSelectionListener(listener);
		// hides the first view
		SelectionProviderView view2 = (SelectionProviderView) fPage.showView(SelectionProviderView.ID_2);
		purgeQueue();
		clearEventState();
		view2.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Selection offered but should not", fEventReceived);
	}

	/**
	 * Tests that the target part receives the current selection when it pops up.
	 */
	@Test
	public void testDeliverDelayed() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this, Predicates.targetPartVisible);
		fPage.addSelectionListener(listener);
		// hides the first view
		SelectionProviderView view2 = (SelectionProviderView) fPage.showView(SelectionProviderView.ID_2);
		purgeQueue();
		view2.setSelection(new StructuredSelection(KNOCK_KNOCK));
		clearEventState();
		fPage.showView(SelectionProviderView.ID);
		assertTrue("Selection not offered but should", fEventReceived);
	}

	/**
	 * Tests that the selection is not received when the selection part is
	 * invisible.
	 */
	@Test
	public void testSelfMuteT3() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		SelectionProviderView view2 = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createVisibleSelfMutedListener(view, this);
		fPage.addSelectionListener(listener);
		fPage.setPartState(fPage.getReference(view), IWorkbenchPage.STATE_MINIMIZED);
		clearEventState();
		view2.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Selection offered even when we were not visible", fEventReceived);
	}

	/**
	 * Tests that the selection is not received when the selection part is
	 * invisible.
	 */
	@Test
	public void testSelfMuteT4() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createVisibleSelfMutedListener(view, this,
				Predicates.selfMute);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Selection offered from the same part", fEventReceived);
	}

	/**
	 * Tests that the selection is not received when the selection part is
	 * invisible.
	 */
	@Test
	public void testSelfMuteT5() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		SelectionProviderView view2 = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createVisibleSelfMutedListener(view, this);
		fPage.addSelectionListener(listener);
		view2.setSelection(new StructuredSelection(KNOCK_KNOCK));
		clearEventState();
		view2.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Selection offered twice", fEventReceived);
	}

	/**
	 * Tests minimum selection block.
	 */
	@Test
	public void testMinimumSelectionSizeT1() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.minimalSelectionSize(2));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(new Object[] { view }));
		assertFalse("Selection offered", fEventReceived);
	}

	/**
	 * Tests minimum selection positive.
	 */
	@Test
	public void testMinimumSelectionSizeT2() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.minimalSelectionSize(2));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(new Object[] { view, view }));
		assertTrue("Selection not offered", fEventReceived);
	}

	/**
	 * Tests that the selection is not received when the selection part is
	 * invisible.
	 */
	@Test
	public void testMinimumSelectionSizeT3() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.minimalSelectionSize(2));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(new Object[] { view, view, view }));
		assertTrue("Selection not offered", fEventReceived);
	}

	/**
	 * Tests minimum selection negative on the selection type.
	 */
	@Test
	public void testMinimumSelectionSizeT4() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.minimalSelectionSize(2));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(() -> true);
		assertFalse("Selection offered but should not", fEventReceived);
	}

	/**
	 * Tests that the selection is not received when the selection part is
	 * invisible.
	 */
	@Test
	public void testSelectionSizeT1() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this, Predicates.selectionSize(4));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(new Object[] { view, view, view }));
		assertFalse("Selection offered but wrong selection size", fEventReceived);
	}

	/**
	 * Tests that the selection is not received when the selection part is
	 * invisible.
	 */
	@Test
	public void testSelectionSizeT2() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this, Predicates.selectionSize(3));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(new Object[] { view, view, view }));
		assertTrue("Selection not offered", fEventReceived);
	}

	/**
	 * Test 1 for the {@link Predicates#adaptsTo(Class)} predicate.
	 */
	@Test
	public void testAdaptsToT1() throws Throwable {

		IAdapterFactory f = new IAdapterFactory() {

			@Override
			public Class<?>[] getAdapterList() {
				return new Class<?>[] { Bird.class, Sparrow.class };
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T getAdapter(Object pAdaptableObject, Class<T> pAdapterType) {
				if (pAdaptableObject instanceof Sparrow && pAdapterType.equals(Bird.class)) {
					return (T) new Bird();
				}
				return null;
			}
		};
		Platform.getAdapterManager().registerAdapters(f, Sparrow.class);

		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.adaptsTo(Bird.class));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(new Sparrow()));
		assertTrue("Selection not offered", fEventReceived);
	}

	/**
	 * Test 2 for the {@link Predicates#adaptsTo(Class)} predicate.
	 */
	@Test
	public void testAdaptsToT2() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.adaptsTo(SelectionProviderView.class));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Selection offered but should not", fEventReceived);
	}

	/**
	 * Test 3 for the {@link Predicates#adaptsTo(Class)} predicate.
	 */
	@Test
	public void testAdaptsToT3() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.adaptsTo(SelectionProviderView.class));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(view));
		assertTrue("Selection offered but should not", fEventReceived);
	}

	/**
	 * Test 4 for the {@link Predicates#adaptsTo(Class)} predicate.
	 */
	@Test
	public void testAdaptsToT4() throws Throwable {

		IAdapterFactory f = new IAdapterFactory() {

			@Override
			public Class<?>[] getAdapterList() {
				return new Class<?>[] { Bird.class, Sparrow.class };
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T getAdapter(Object pAdaptableObject, Class<T> pAdapterType) {
				if (pAdaptableObject instanceof Sparrow && pAdapterType.equals(Bird.class)) {
					return (T) new Bird();
				}
				return null;
			}
		};
		Platform.getAdapterManager().registerAdapters(f, Sparrow.class);

		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.adaptsTo(Sparrow.class));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(new Bird()));
		assertFalse("Selection offered", fEventReceived);
	}

	/**
	 * Test 5 for the {@link Predicates#adaptsTo(Class)} predicate.
	 */
	@Test
	public void testAdaptsToT5() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.adaptsTo(SelectionProviderView.class));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(() -> true);
		assertFalse("Selection offered but should not", fEventReceived);
	}

	/**
	 * Tests user defined predicate 1.
	 */
	@Test
	public void testUserPredicate() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this, m -> false);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Selection offered but should not", fEventReceived);
	}

	/**
	 * Tests user defined predicate 2.
	 */
	@Test
	public void testUserPredicateT2() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				m -> m.getTargetPart().equals(view));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Selection should be offered", fEventReceived);
	}

	/**
	 * Tests user defined predicate 3.
	 */
	@Test
	public void testUserPredicateT3() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				m -> m.getTargetPart().equals(view));
		SelectionListenerFactory.decorate(listener, m -> true);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Selection should be offered", fEventReceived);
	}

	/**
	 * Tests user defined predicate 4.
	 */
	@Test
	public void testUserPredicateT4() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				m -> m.getTargetPart().equals(view));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Selection should not be offered", fEventReceived);
	}

	/**
	 * Create listener factory method test.
	 */
	@Test
	public void testCreateListenerTest() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		assertThrows(ClassCastException.class, () -> SelectionListenerFactory.createListener(view, m -> true));
	}

	/**
	 * Null Listener test. Selection should be offered when the view is made
	 * visible.
	 */
	@Test
	@Ignore
	public void testNullListener() throws Throwable {
		INullSelectionListener nullSelectionListener = (pPart, pSelection) -> fEventReceived = true;
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createVisibleListener(view, nullSelectionListener,
				m -> true);
		fPage.addSelectionListener(listener);
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
//		SelectionProviderView view2 = (SelectionProviderView) fPage.showView(SelectionProviderView.ID_2);
//		view2.setNullSelection();
		clearEventState();
		fPage.showView(SelectionProviderView.ID);
		assertTrue("Selection not offered", fEventReceived);
	}

	/**
	 * Test the decorate method 1.
	 */
	@Test
	public void testDecorateT1() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createVisibleListener(view, this);

		Predicate<ISelectionModel> pp = model -> {
			System.out.println("Return true");
			return true;
		};

		SelectionListenerFactory.decorate(listener, pp);
		fPage.addSelectionListener(listener);
		purgeQueue();
		view.setSelection(new StructuredSelection());
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Selection should be offered", fEventReceived);
	}

	/**
	 * Test the decorate method 2.
	 */
	@Test
	public void testDecorateT2() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createVisibleListener(view, this);
		Predicate<ISelectionModel> predicate = getCountingPredicate(true)
				.and(getCountingPredicate(false).and(getCountingPredicate(true)));
		SelectionListenerFactory.decorate(listener, predicate);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Selection should not be offered", fEventReceived);
	}

	private Predicate<ISelectionModel> getCountingPredicate(boolean bool) {
		return pT -> {
			fCounter++;
			System.out.println("Return " + bool);
			return bool;
		};
	}

	/**
	 * Test the decorate method 3.
	 */
	@Test
	public void testDecorateT3() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createVisibleListener(view, this);
		Predicate<ISelectionModel> predicate = getCountingPredicate(true)
				.and(getCountingPredicate(true).and(getCountingPredicate(true)));
		SelectionListenerFactory.decorate(listener, predicate);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Selection should not be offered", fEventReceived);
	}

	/**
	 * Test the decorate method 4.
	 */
	@Test
	public void testDecorateT4() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createVisibleListener(view, this);
		Predicate<ISelectionModel> predicate = getCountingPredicate(true)
				.and(getCountingPredicate(true).and(getCountingPredicate(true)));
		SelectionListenerFactory.decorate(listener, predicate);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Counter must be three but is " + fCounter, fCounter == 3);
	}

	/**
	 * Test the decorate method 5.
	 */
	@Test
	public void testDecorateT5() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createVisibleListener(view, this);
		Predicate<ISelectionModel> predicate = getCountingPredicate(true)
				.and(getCountingPredicate(false).and(getCountingPredicate(true)));
		SelectionListenerFactory.decorate(listener, predicate);
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Counter must be two but is " + fCounter, fCounter == 2);
	}

	/**
	 * Tests selection type
	 */
	@Test
	public void testSelectionTypeT1() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.selectionType(ITreeSelection.class));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertFalse("Selection should not be offered", fEventReceived);
	}

	/**
	 * Tests selection type
	 */
	@Test
	public void testSelectionTypeT2() throws Throwable {
		SelectionProviderView view = (SelectionProviderView) fPage.showView(SelectionProviderView.ID);
		ISelectionListener listener = SelectionListenerFactory.createListener(view, this,
				Predicates.selectionType(IStructuredSelection.class));
		fPage.addSelectionListener(listener);
		clearEventState();
		view.setSelection(new StructuredSelection(KNOCK_KNOCK));
		assertTrue("Selection should be offered", fEventReceived);
	}

	/**
	 * Clear the event state.
	 */
	private void clearEventState() {
		fCounter = 0;
		fEventReceived = false;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		fEventReceived = true;
	}

	public static class Bird {
	}

	public static class Sparrow {
	}
}
