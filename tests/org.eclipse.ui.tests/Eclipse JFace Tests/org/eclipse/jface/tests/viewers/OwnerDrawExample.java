package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeItem;

public class OwnerDrawExample {
	
	public static void main(String[] args){
		
		Display display = new Display();
		Shell shell = new Shell(display,SWT.NONE);
		shell.setSize(400, 400);
		shell.setLayout(new GridLayout());
		
		OwnerDrawExample example = new OwnerDrawExample();
		example.createPartControl(shell);
		
		shell.open();
		
		while(!shell.isDisposed()){
			display.readAndDispatch();
		}
		
	}

	private static int COLUMN_COUNT = 3;

	class OwnerDrawColumn extends ViewerColumn {

		Control control;

		/**
		 * Create a new instance of the receiver with no label provider.
		 * 
		 * @param columnOwner
		 */
		public OwnerDrawColumn(TableColumn columnOwner) {
			super(columnOwner, null);
			control = columnOwner.getParent();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ViewerColumn#refresh(org.eclipse.jface.viewers.ViewerCell)
		 */
		public void refresh(ViewerCell cell) {
			Rectangle cellBounds = cell.getBounds();
			control.redraw(cellBounds.x, cellBounds.y, cellBounds.width,
					cellBounds.height, true);
		}

	}

	class CountryEntry {

		String name;

		String cupYear;

		/**
		 * Create a new instance of the receiver.
		 * 
		 * @param countryName
		 * @param worldCupYear
		 */
		CountryEntry(String countryName, String worldCupYear) {
			name = countryName;
			cupYear = worldCupYear;
		}

	}

	private class GermanyEntry extends CountryEntry {

		GermanyEntry() {
			super("Deutschland", "1990");
		}

	}

	private class AustriaEntry extends CountryEntry {

		AustriaEntry() {
			super("Österreich", "TBD");
		}
	}

	private class EnglandEntry extends CountryEntry {
		EnglandEntry() {
			super("England", "1966");
		}
	}

	private TableViewer viewer;

	private CountryEntry[] entries;

	public OwnerDrawExample() {
		entries = new CountryEntry[3];
		entries[0] = new AustriaEntry();
		entries[1] = new GermanyEntry();
		entries[2] = new EnglandEntry();
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
				TreeItem item = (TreeItem) event.item;
				int index = event.index;

				event.setBounds(measure((CountryEntry) item.getData(), index));
			}
		});

	}

	/**
	 * Return the size of the entry at CountryEntry.
	 * @param entry
	 * @param index
	 * @return Rectangle
	 */
	protected Rectangle measure(CountryEntry entry, int index) {
		return new Rectangle (0,0,100,100);
	}

	/**
	 * Create the columns to be used in the tree.
	 */
	private void createColumns() {
		TableLayout layout = new TableLayout();
		viewer.getTable().setLayout(layout);
		viewer.getTable().setHeaderVisible(true);

		for (int i = 0; i < COLUMN_COUNT; i++) {
			TableColumn tc = new TableColumn(viewer.getTable(), SWT.NONE, i);
			viewer.setViewerColumn(i, new OwnerDrawColumn(tc));
		}
		;
	}

	public void setFocus() {

	}

}
