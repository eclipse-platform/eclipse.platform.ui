package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

/**
 * Example for https://bugs.eclipse.org/bugs/show_bug.cgi?id=566936
 *
 */
public class Bug566936TreeViewerTest {

	public static boolean flag = true;

	private static class MyModel {
		public int counter;

		public MyModel(int counter) {
			this.counter = counter;
		}

		@Override
		public String toString() {
			return "Item " + this.counter;
		}
	}

	private class MyEditingSupport extends EditingSupport {

		private String property;

		public MyEditingSupport(ColumnViewer viewer, String property) {
			super(viewer);
			this.property = property;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return "Column " + property + " => " + element;
		}

		@Override
		protected void setValue(Object element, Object value) {

		}

	}

	private static class MyColumnLabelProvider extends ColumnLabelProvider implements ITreeContentProvider {

		public MyColumnLabelProvider(Tree tree, int columnIndex) {
			super();
			this.tree = tree;
			this.columnIndex = columnIndex;
		}

		private int columnIndex;
		private Tree tree;

		@Override
		public String getText(Object element) {
			return "Column " + tree.getColumnOrder()[columnIndex] + " => " + element;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List list) {
				return list.toArray();
			}
			return new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return new Object[0];
		}

		@Override
		public boolean hasChildren(Object element) {
			return true;
		}
	}

	public Bug566936TreeViewerTest(Shell shell) {
		final TreeViewer v = new TreeViewer(shell, SWT.BORDER | SWT.FULL_SELECTION);
		v.setContentProvider(new MyColumnLabelProvider(v.getTree(), 0));

		TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(v,
				new MyFocusBorderCellHighlighter(v)) {

			@Override
			public ViewerCell getFocusCell() {
				ViewerCell cell = super.getFocusCell();

				if (cell != null) {
					return cell;
				}

				return v.getCell(new Point(5, 5));

			}

		};
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(v) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		int feature = ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION;

		TreeViewerEditor.create(v, focusCellManager, actSupport, feature);

		String[] columLabels = { "Column 1", "Column 2", "Column 3", "Col 4", "Col 5", "Col 6", "Col 7", "Col 8",
				"Col 9", "Col 10", "Col 11", "Col 12" };
		int property = 0;
		for (String label : columLabels) {
			createColumnFor(v, label, property++);
		}

		List<MyModel> model = createModel();

		v.setInput(model);
		v.setSelection(new StructuredSelection(model.get(0)));
		v.getTree().setLinesVisible(true);
		v.getTree().setHeaderVisible(true);
		v.getTree().setFocus();
	}

	private void createColumnFor(TreeViewer v, String label, int columnIndex) {

		TreeViewerColumn viewerColumn = new TreeViewerColumn(v, SWT.NONE, columnIndex);
		viewerColumn.getColumn().setWidth(300);
		viewerColumn.getColumn().setMoveable(true);
		viewerColumn.getColumn().setText(label);
		viewerColumn.getColumn().setResizable(true);

		viewerColumn.setEditingSupport(new MyEditingSupport(v, columnIndex + ""));
		viewerColumn.setLabelProvider(new MyColumnLabelProvider(v.getTree(), columnIndex));
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			elements.add(new MyModel(i));
		}
		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Bug566936TreeViewerTest(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

}

class MyFocusBorderCellHighlighter extends FocusCellHighlighter {

	/**
	 * @param viewer the viewer
	 */
	public MyFocusBorderCellHighlighter(ColumnViewer viewer) {
		super(viewer);
		hookListener(viewer);
	}

	private static void markFocusedCell(Event event) {
		GC gc = event.gc;

		Rectangle rect = event.getBounds();
		gc.drawFocus(rect.x, rect.y, rect.width, rect.height);

		event.detail &= ~SWT.SELECTED;
	}

	private static void removeSelectionInformation(ViewerCell cell) {

		if (cell != null) {
			Rectangle rect = cell.getBounds();
			int x = cell.getColumnIndex() == 0 ? 0 : rect.x;
			int width = cell.getColumnIndex() == 0 ? rect.x + rect.width : rect.width;
			cell.getControl().redraw(x, rect.y, width, rect.height, true);
		}

	}

	private void hookListener(final ColumnViewer viewer) {

		Listener listener = event -> {
			if ((event.detail & SWT.SELECTED) > 0) {
				ViewerCell focusCell = getFocusCell();
				if (focusCell == null) {
					return;
				}
				ViewerRow row = focusCell.getViewerRow();

				Assert.isNotNull(row, "Internal structure invalid. Item without associated row is not possible."); //$NON-NLS-1$

				ViewerCell cell = row.getCell(event.index);

				if (!cell.equals(focusCell)) {
					removeSelectionInformation(cell);
				} else {
					markFocusedCell(event);
				}
			}
		};
		viewer.getControl().addListener(SWT.EraseItem, listener);
	}

	/**
	 * @param cell the cell which is colored
	 * @return the color
	 */
	protected Color getSelectedCellBackgroundColor(ViewerCell cell) {
		return null;
	}

	/**
	 * @param cell the cell which is colored
	 * @return the color
	 */
	protected Color getSelectedCellForegroundColor(ViewerCell cell) {
		return null;
	}

	@Override
	protected void focusCellChanged(ViewerCell newCell, ViewerCell oldCell) {

		// Redraw new area
		if (newCell != null) {
			Rectangle rect = newCell.getBounds();
			int x = newCell.getColumnIndex() == 0 ? 0 : rect.x;
			int width = newCell.getColumnIndex() == 0 ? rect.x + rect.width : rect.width;
			newCell.getControl().redraw(x, rect.y, width, rect.height, true);
		}

		if (oldCell != null) {
			Rectangle rect = oldCell.getBounds();
			int x = oldCell.getColumnIndex() == 0 ? 0 : rect.x;
			int width = oldCell.getColumnIndex() == 0 ? rect.x + rect.width : rect.width;
			oldCell.getControl().redraw(x, rect.y, width, rect.height, true);
		}

	}
}