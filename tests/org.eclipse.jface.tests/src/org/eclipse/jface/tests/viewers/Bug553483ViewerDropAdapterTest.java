/*******************************************************************************
 * Copyright (c) 2020 Gerhard Kreuzer.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gerhard Kreuzer - initial API and implementation (bug 553483)
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;

/**
 *
 */
public class Bug553483ViewerDropAdapterTest extends TestCase {

	private Display display;
	private Shell shell;
	private Point srcPos1;
	private Point tgtPos1;
	private Point srcPos2;
	private Point tgtPos2;
	private int numberOfDrops;

	@Override
	public void setUp() throws Exception {
		display = new Display();
		shell = new Shell(display);

		shell.setLayout(new RowLayout(SWT.HORIZONTAL));

		TreeViewer srcViewer = new TreeViewer(shell);

		srcViewer.setContentProvider(new ContentProvider());

		srcViewer.setInput(new String[] { "1", "2", "3", "4" });

		// the drag source shall support 'copy' only
		int dragOps = DND.DROP_COPY;
		Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };
		srcViewer.addDragSupport(dragOps, transfers, new ViewerDragSource(srcViewer));

		TreeViewer tgtViewer = new TreeViewer(shell);

		tgtViewer.setContentProvider(new ContentProvider());

		tgtViewer.setInput(new String[] { "A", "B", "C", "D" });

		// the drop source accepts both
		int dropOps = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] dropTransfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };
		tgtViewer.addDropSupport(dropOps, dropTransfers, new ViewerDropTarget(tgtViewer));

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// for the first time idle => all is up and running

				// determine the required mouse positions...
				Rectangle srcItemBounds = srcViewer.getTree().getItem(1).getBounds();

				srcPos1 = srcViewer.getTree().toDisplay(srcItemBounds.x + srcItemBounds.width / 2,
						srcItemBounds.y + srcItemBounds.height / 2);

				tgtPos1 = new Point(srcPos1.x + tgtViewer.getTree().getBounds().x - srcViewer.getTree().getBounds().x,
						srcPos1.y);

				srcItemBounds = srcViewer.getTree().getItem(2).getBounds();

				srcPos2 = srcViewer.getTree().toDisplay(srcItemBounds.x + srcItemBounds.width / 2,
						srcItemBounds.y + srcItemBounds.height / 2);

				tgtPos2 = new Point(srcPos2.x + tgtViewer.getTree().getBounds().x - srcViewer.getTree().getBounds().x,
						srcPos2.y);

				// and go to test now
				return;
			}
		}
	}

	@Override
	public void tearDown() throws Exception {
		assertTrue(shell.isDisposed());

		display.dispose();
	}

	public void testBug553483() {
		boolean copyWasPosted = false;
		boolean moveWasPosted = false;

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				if (!copyWasPosted) {
					assertEquals(0, numberOfDrops);

					// first post a valid copy drag & drop
					postDragAndDropCopyEvents();

					copyWasPosted = true;
				} else if (!moveWasPosted) {
					assertEquals(1, numberOfDrops);

					// after the first drop, trigger a move drag & drop (which should not succeed)
					postDragAndDropMoveEvents();

					moveWasPosted = true;
				} else {
					display.asyncExec(() -> shell.dispose());
				}
			}
		}

		// the second drag & drop must have been refused (== 'move' for a source that
		// does support 'copy' only)
		assertEquals(1, numberOfDrops);
	}

	private void postDragAndDropCopyEvents() {
		// ctrl + drag & drop => copy

		shell.forceActive();

		Event keyEvent = new Event();
		Event mouseEvent = new Event();

		// press ctrl
		keyEvent.keyCode = SWT.CTRL;
		keyEvent.stateMask = SWT.NONE;
		keyEvent.type = SWT.KeyDown;
		display.post(keyEvent);

		// emulate drag & drop
		mouseEvent.x = srcPos1.x;
		mouseEvent.y = srcPos1.y;
		mouseEvent.type = SWT.MouseMove;
		mouseEvent.stateMask = SWT.CTRL;
		display.post(mouseEvent);

		mouseEvent.button = 1;
		mouseEvent.count = 1;
		mouseEvent.type = SWT.MouseDown;
		mouseEvent.stateMask = SWT.CTRL;
		display.post(mouseEvent);

		mouseEvent.x = tgtPos1.x;
		mouseEvent.y = tgtPos1.y;
		mouseEvent.type = SWT.MouseMove;
		mouseEvent.stateMask = SWT.CTRL;
		display.post(mouseEvent);

		mouseEvent.type = SWT.MouseUp;
		mouseEvent.stateMask = SWT.CTRL;
		display.post(mouseEvent);

		// finally release ctrl
		keyEvent.keyCode = SWT.CTRL;
		keyEvent.stateMask = SWT.CTRL;
		keyEvent.type = SWT.KeyUp;
		display.post(keyEvent);
	}

	private void postDragAndDropMoveEvents() {
		// this time w/o ctrl => move

		shell.forceActive();

		Event mouseEvent = new Event();

		mouseEvent.x = srcPos2.x;
		mouseEvent.y = srcPos2.y;
		mouseEvent.type = SWT.MouseMove;
		display.post(mouseEvent);

		mouseEvent.button = 1;
		mouseEvent.count = 1;
		mouseEvent.type = SWT.MouseDown;
		display.post(mouseEvent);

		mouseEvent.x = tgtPos2.x;
		mouseEvent.y = tgtPos2.y;
		mouseEvent.type = SWT.MouseMove;
		display.post(mouseEvent);

		mouseEvent.type = SWT.MouseUp;
		display.post(mouseEvent);
	}

	private static class ViewerDragSource implements DragSourceListener {
		private final Viewer viewer;

		ViewerDragSource(final Viewer v) {
			viewer = v;
		}

		@Override
		public void dragStart(final DragSourceEvent event) {
			LocalSelectionTransfer.getTransfer().setSelection(viewer.getSelection());
		}

		@Override
		public void dragSetData(final DragSourceEvent event) {
			// nothing to do here
		}

		@Override
		public void dragFinished(final DragSourceEvent event) {
			LocalSelectionTransfer.getTransfer().setSelection(null);
		}
	}

	private class ViewerDropTarget extends ViewerDropAdapter {
		ViewerDropTarget(final Viewer viewer) {
			super(viewer);
		}

		@Override
		public boolean validateDrop(final Object target, final int operation, final TransferData transferType) {
			return true;
		}

		@Override
		public boolean performDrop(final Object data) {
			++numberOfDrops;

			return true;
		}

	}

	private static class ContentProvider implements ITreeContentProvider {
		@Override
		public boolean hasChildren(Object element) {
			return false;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return ArrayContentProvider.getInstance().getElements(inputElement);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return null;
		}
	}

}
