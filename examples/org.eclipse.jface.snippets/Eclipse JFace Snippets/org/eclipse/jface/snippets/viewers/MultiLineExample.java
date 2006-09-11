package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class MultiLineExample {

	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display, SWT.CLOSE);
		shell.setSize(400, 400);
		shell.setLayout(new GridLayout());

		MultiLineExample example = new MultiLineExample();
		example.createPartControl(shell);

		shell.open();

		while (!shell.isDisposed()) {
			display.readAndDispatch();
		}
		display.dispose();
	}


	class LineEntry {

		String line;

		int columnWidth;

		/**
		 * Create a new instance of the receiver with name text constrained to a
		 * column of width.
		 * 
		 * @param text
		 * @param width
		 */
		LineEntry(String text, int width) {
			line = text;
			columnWidth = width;
		}

		/**
		 * Get the height of the event.
		 * 
		 * @param index
		 * @return int
		 */
		public int getHeight(Event event) {
			event.gc.setLineWidth(columnWidth);
			return event.gc.textExtent(line).y;

		}

		/**
		 * Get the width of the event.
		 * 
		 * @param index
		 * @return
		 */
		public int getWidth(Event event) {

			return columnWidth;
		}


		/**
		 * Get the font we are using.
		 * 
		 * @return Font
		 */
		protected Font getFont() {
			return JFaceResources.getFont(JFaceResources.HEADER_FONT);
		}

		/**
		 * @param event
		 */
		public void draw(Event event) {
			event.gc.drawText(line, event.x, event.y);

		}
	}

	private TableViewer viewer;

	private LineEntry[] entries;

	public MultiLineExample() {
		String[] lines = new String[] {
				"This day is called the feast of Crispian:",
				"He that outlives this day, \n and comes safe home,",
				"Will stand a tip-toe when the day is named,",
				"And rouse him at the name of Crispian.",
				"He that shall live this day,\n and see old age,",
				"Will yearly on the vigil feast his neighbours,",
				"And say 'To-morrow is Saint Crispian:'",
				"Then will he strip his sleeve and show his scars.",
				"And say 'These wounds I had on Crispin's day.'",
				"Old men forget:\n yet all shall be forgot,",
				"But he'll remember with advantages",
				"What feats he did that day:\n then shall our names.",
				"Familiar in his mouth as household words",
				"Harry the king, Bedford and Exeter,",
				"Warwick and Talbot,\n Salisbury and Gloucester,",
				"Be in their flowing cups freshly remember'd.",
				"This story shall the good man teach his son;",
				"And Crispin Crispian shall ne'er go by,",
				"From this day to the ending of the world,",
				"But we in it shall be remember'd;",
				"We few,\n we happy few,\n we band of brothers;",
				"For he to-day that sheds his blood with me",
				"Shall be my brother;\n be he ne'er so vile,",
				"This day shall gentle his condition:",
				"And gentlemen in England now a-bed",
				"Shall think themselves accursed they were not here,",
				"And hold their manhoods cheap whiles any speaks",
				"That fought with us upon Saint Crispin's day." };

		entries = new LineEntry[lines.length];
		for (int i = 0; i < lines.length; i++) {
			entries[i] = new LineEntry(lines[i], 35);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent);

		viewer.setContentProvider(new IStructuredContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
			};

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return entries;
			};

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(org.eclipse.jface.viewers.Viewer viewer,
					Object oldInput, Object newInput) {
			}

		});
		createColumns();

		viewer.setLabelProvider(new OwnerDrawLabelProvider());
		viewer.setInput(this);

		GridData data = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL | GridData.FILL_BOTH);

		viewer.getControl().setLayoutData(data);

		viewer.getTable().addListener(SWT.MeasureItem, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				
				LineEntry line = (LineEntry) event.item.getData();
				Point size = event.gc.textExtent(line.line);
				event.width = 150;
				int lines = size.x / event.width + 1;
				event.height = size.y * lines;
				
			}
		});

		viewer.getTable().addListener(SWT.PaintItem, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				LineEntry entry = (LineEntry) item.getData();
				event.gc.drawText(entry.line, event.x, event.y, true);
				

			}
		});

		viewer.getTable().addListener(SWT.EraseItem, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {

				Rectangle bounds = event.getBounds();
				if ((event.detail & SWT.SELECTED) > 0) {

					Color oldForeground = event.gc.getForeground();
					Color oldBackground = event.gc.getBackground();

					event.gc.setBackground(viewer.getControl().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION));
					event.gc.setForeground(viewer.getControl().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
					event.gc.fillRectangle(bounds);
					/* restore the old GC colors */
					event.gc.setForeground(oldForeground);
					event.gc.setBackground(oldBackground);
					/* ensure that default selection is not drawn */
					event.detail &= ~SWT.SELECTED;

				}

			}
		});

		viewer.setSelection(new StructuredSelection(entries[1]));
	}

	/**
	 * Return the size of the entry at CountryEntry.
	 * 
	 * @param entry
	 * @param index
	 * @return Rectangle
	 */
	protected Rectangle measure(LineEntry entry, Event event) {
		return new Rectangle(0, 0, entry.getWidth(event), entry
				.getHeight(event));
	}

	/**
	 * Create the columns to be used in the tree.
	 */
	private void createColumns() {
		TableLayout layout = new TableLayout();
		viewer.getTable().setLayout(layout);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		TableColumn tc = new TableColumn(viewer.getTable(), SWT.NONE, 0);
		layout.addColumnData(new ColumnPixelData(350));
		tc.setText("Lines");

	}

	
	public void setFocus() {

	}

}
