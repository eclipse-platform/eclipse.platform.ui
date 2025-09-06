/*******************************************************************************
 * Copyright (c) 2025 Ali Muhsin KÖKSAL.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Ali Muhsin KÖKSAL - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.dnd;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DragTestSuite {

	private static final boolean isDetachingSupported;

	static {
		Shell shell = new Shell();
		Composite c = new Composite(shell, SWT.NONE);
		isDetachingSupported = c.isReparentable();
		shell.dispose();
	}

	public DragTestSuite() {}

	@ParameterizedTest
	@MethodSource("dragSourceDropTargetProvider")
	void dragTest(TestDragSource dragSource, TestDropLocation dropTarget) {
		new DragTest(dragSource, dropTarget);
	}

	static Stream<Arguments> dragSourceDropTargetProvider() {
		List<Arguments> args = new ArrayList<>();

		String resNav = IPageLayout.ID_PROJECT_EXPLORER;
		String probView = IPageLayout.ID_PROBLEM_VIEW;

		TestDragSource[] viewDragSources = new TestDragSource[] { new ViewDragSource(resNav, false),
				new ViewDragSource(resNav, true), new ViewDragSource(probView, false),
				new ViewDragSource(probView, true) };

		TestDragSource[] editorDragSources = new TestDragSource[] { new EditorDragSource(0, false),
				new EditorDragSource(0, true), new EditorDragSource(2, false), new EditorDragSource(2, true) };

		for (TestDragSource source : viewDragSources) {
			for (TestDropLocation target : getViewDropTargets(source)) {
				if (target != null) {
					args.add(Arguments.of(source, target));
				}
			}
			for (TestDropLocation target : getCommonDropTargets(source)) {
				args.add(Arguments.of(source, target));
			}
		}

		for (TestDragSource source : editorDragSources) {
			for (TestDropLocation target : getEditorDropTargets(source)) {
				args.add(Arguments.of(source, target));
			}
			for (TestDropLocation target : getCommonDropTargets(source)) {
				args.add(Arguments.of(source, target));
			}
		}

		TestDragSource[] maximizedViewDragSources = new TestDragSource[] { new ViewDragSource(resNav, false, true),
				new ViewDragSource(resNav, true, true), new ViewDragSource(probView, false, true),
				new ViewDragSource(probView, true, true) };

		for (TestDragSource source : maximizedViewDragSources) {
			for (TestDropLocation target : getMaximizedViewDropTargets(source)) {
				args.add(Arguments.of(source, target));
			}
		}

		if (isDetachingSupported) {
			for (TestDragSource source : viewDragSources) {
				for (TestDropLocation target : getDetachedWindowDropTargets(source)) {
					args.add(Arguments.of(source, target));
				}
			}
			for (TestDragSource source : editorDragSources) {
				for (TestDropLocation target : getDetachedWindowDropTargets(source)) {
					args.add(Arguments.of(source, target));
				}
			}
		}

		return args.stream();
	}

	/**
	 * Returns drop targets that will only be tested for maximized views. (we only
	 * need to ensure that the view will become un-maximized -- the regular view
	 * test cases will excercise the remainder of the view dragging code). We need
	 * to drag each kind of maximized view to something that couldn't be seen while
	 * the view is maximized -- like the editor area).
	 *
	 * @since 3.1
	 */
	private static TestDropLocation[] getMaximizedViewDropTargets(IWorkbenchWindowProvider originatingWindow) {
		return new TestDropLocation[] { new EditorAreaDropTarget(originatingWindow, SWT.RIGHT) };
	}

	private static TestDropLocation[] getCommonDropTargets(IWorkbenchWindowProvider dragSource) {
		return new TestDropLocation[] {
				// Test dragging to the edges of the workbench window
				new WindowDropTarget(dragSource, SWT.TOP), new WindowDropTarget(dragSource, SWT.BOTTOM),
				new WindowDropTarget(dragSource, SWT.LEFT), new WindowDropTarget(dragSource, SWT.RIGHT) };
	}

	/**
	 * Return all drop targets that only apply to views, given the window being
	 * dragged from.
	 *
	 * @since 3.1
	 */
	private static TestDropLocation[] getViewDropTargets(IWorkbenchWindowProvider dragSource) {

		String resNav = IPageLayout.ID_PROJECT_EXPLORER;
		String probView = IPageLayout.ID_PROBLEM_VIEW;

		return new TestDropLocation[] {
				// Editor area
				new EditorAreaDropTarget(dragSource, SWT.LEFT), new EditorAreaDropTarget(dragSource, SWT.RIGHT),
				new EditorAreaDropTarget(dragSource, SWT.TOP), new EditorAreaDropTarget(dragSource, SWT.BOTTOM),

				// Resource navigator (a view that isn't in a stack)
				new ViewDropTarget(dragSource, resNav, SWT.LEFT), new ViewDropTarget(dragSource, resNav, SWT.RIGHT),
				new ViewDropTarget(dragSource, resNav, SWT.BOTTOM), new ViewDropTarget(dragSource, resNav, SWT.CENTER),
				new ViewDropTarget(dragSource, resNav, SWT.TOP),

				// Problems view (a view that is in a stack)
				// Omit the top from this test, since the meaning of dropping on the top border
				// of
				// a stack may change in the near future
				new ViewDropTarget(dragSource, probView, SWT.LEFT), new ViewDropTarget(dragSource, probView, SWT.RIGHT),
				new ViewDropTarget(dragSource, probView, SWT.BOTTOM),
				new ViewDropTarget(dragSource, probView, SWT.CENTER), new ViewDropTarget(dragSource, probView, SWT.TOP),

				// Fast view bar
				null, // new FastViewBarDropTarget(dragSource),

				// View tabs
				new ViewTabDropTarget(dragSource, resNav), new ViewTabDropTarget(dragSource, probView),
				new ViewTitleDropTarget(dragSource, probView), };
	}

	/**
	 * Return all drop targets that apply to detached windows, given the window
	 * being dragged from.
	 *
	 * @since 3.1
	 */
	private static TestDropLocation[] getDetachedWindowDropTargets(IWorkbenchWindowProvider dragSource) {
		return new TestDropLocation[] {
				// Editor area
				new ViewDropTarget(dragSource, DragDropPerspectiveFactory.dropViewId1, SWT.CENTER),
				new ViewDropTarget(dragSource, DragDropPerspectiveFactory.dropViewId3, SWT.CENTER),
				new ViewTabDropTarget(dragSource, DragDropPerspectiveFactory.dropViewId1), new DetachedDropTarget() };
	}

	private static TestDropLocation[] getEditorDropTargets(IWorkbenchWindowProvider originatingWindow) {
		String resNav = IPageLayout.ID_PROJECT_EXPLORER;
		// Drop targets that will only be tested for editors
		return new TestDropLocation[] {
				// A view
				new ViewDropTarget(originatingWindow, resNav, SWT.CENTER),

				// A stand-alone editor
				new EditorDropTarget(originatingWindow, 2, SWT.LEFT),
				new EditorDropTarget(originatingWindow, 2, SWT.RIGHT),
				new EditorDropTarget(originatingWindow, 2, SWT.TOP),
				new EditorDropTarget(originatingWindow, 2, SWT.BOTTOM),
				new EditorDropTarget(originatingWindow, 2, SWT.CENTER),

				// Editors (a stack of editors)
				new EditorDropTarget(originatingWindow, 0, SWT.LEFT),
				new EditorDropTarget(originatingWindow, 0, SWT.RIGHT),
				new EditorDropTarget(originatingWindow, 0, SWT.BOTTOM),
				new EditorDropTarget(originatingWindow, 0, SWT.CENTER), new EditorTabDropTarget(originatingWindow, 0),
				new EditorTitleDropTarget(originatingWindow, 0), };
	}

}