/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.history;

import com.ibm.icu.text.DateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.core.history.LocalFileRevision;
import org.eclipse.team.internal.ui.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;

public class LocalHistoryTableProvider {
	/**
	 * The Local history label provider.
	 */
	class LocalHistoryLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider, IFontProvider {
		
		Image dateImage = null;
		ImageDescriptor dateDesc = null;
		
		Image localRevImage = null;
		ImageDescriptor localRevDesc = null;
		
		ThemeListener themeListener;
		
		public LocalHistoryLabelProvider(LocalHistoryTableProvider provider){
				PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(themeListener= new ThemeListener(provider));
		}
		
		public void dispose() {
			if (dateImage != null){
				dateImage.dispose();
				dateImage = null;
			}
			
			if (localRevImage != null) {
				localRevImage.dispose();
				localRevImage = null;
			}
					
			if (themeListener != null){
				PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(themeListener);
			}
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof DateHistoryCategory && columnIndex == COL_DATE) {
				if (dateImage == null) {
					dateDesc = TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_DATES_CATEGORY);
					dateImage = dateDesc.createImage();
				}
				return dateImage;
			}

			if (element instanceof LocalFileRevision && columnIndex == COL_DATE) {
				if (localRevImage == null) {
					localRevDesc = TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_LOCALREVISION_TABLE);
					localRevImage = localRevDesc.createImage();
				}
				return localRevImage;
			}
			
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof AbstractHistoryCategory){
				if (columnIndex != COL_DATE)
					return ""; //$NON-NLS-1$
				
				return ((AbstractHistoryCategory) element).getName();
			}
			
			IFileRevision entry = adaptToFileRevision(element);
			if (entry == null)
				return ""; //$NON-NLS-1$
			switch (columnIndex) {
				case COL_DATE :
					long date = entry.getTimestamp();
					Date dateFromLong = new Date(date);
					return DateFormat.getInstance().format(dateFromLong);
			}
			return ""; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			if (element instanceof AbstractHistoryCategory){
				ITheme current = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
				return current.getColorRegistry().get("org.eclipse.team.cvs.ui.fontsandcolors.cvshistorypagecategories");  //$NON-NLS-1$
			}
			
			IFileRevision entry = adaptToFileRevision(element);
			if (!entry.exists()) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
			}

			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
		 */
		public Font getFont(Object element) {
			if (element instanceof AbstractHistoryCategory) {
				return getCurrentRevisionFont();
			}
			
			IFileRevision entry = adaptToFileRevision(element);
			if (entry == null)
				return null;
			long timestamp = entry.getTimestamp();
			long tempCurrentTimeStamp = getCurrentRevision();
			if (tempCurrentTimeStamp != 0 && tempCurrentTimeStamp==timestamp) {
				return getCurrentRevisionFont();
			}
			return null;
		}

		private Font getCurrentRevisionFont() {
			if (currentRevisionFont == null) {
				Font defaultFont = JFaceResources.getDefaultFont();
				FontData[] data = defaultFont.getFontData();
				for (int i = 0; i < data.length; i++) {
					data[i].setStyle(SWT.BOLD);
				}
				currentRevisionFont = new Font(viewer.getTree().getDisplay(), data);
			}
			return currentRevisionFont;
		}
	}

	/**
	 * The history sorter
	 */
	class HistoryComparator extends ViewerComparator {
		private boolean reversed = false;
		private int columnNumber;
		
		// column headings:	"Revision" "Tags" "Date" "Author" "Comment"
		private int[][] SORT_ORDERS_BY_COLUMN = { 
				{COL_DATE}, /* date */
		};

		/**
		 * The constructor.
		 * @param columnNumber 
		 */
		public HistoryComparator(int columnNumber) {
			this.columnNumber = columnNumber;
		}

		/**
		 * Compares two log entries, sorting first by the main column of this sorter,
		 * then by subsequent columns, depending on the column sort order.
		 */
		public int compare(Viewer compareViewer, Object o1, Object o2) {
			/*if (o1 instanceof AbstractCVSHistoryCategory || o2 instanceof AbstractCVSHistoryCategory)
				return 0;*/
			
			IFileRevision e1 = adaptToFileRevision(o1);
			IFileRevision e2 = adaptToFileRevision(o2);
			int result = 0;
			if (e1 == null || e2 == null) {
				result = super.compare(compareViewer, o1, o2);
			} else {
				int[] columnSortOrder = SORT_ORDERS_BY_COLUMN[columnNumber];
				for (int i = 0; i < columnSortOrder.length; ++i) {
					result = compareColumnValue(columnSortOrder[i], e1, e2);
					if (result != 0)
						break;
				}
			}
			if (reversed)
				result = -result;
			return result;
		}

		/**
		 * Compares two markers, based only on the value of the specified column.
		 */
		int compareColumnValue(int columnNumber, IFileRevision e1, IFileRevision e2) {
			switch (columnNumber) {
				case 0 : /* date */
					long date1 = e1.getTimestamp();
					long date2 = e2.getTimestamp();
					if (date1 == date2)
						return 0;

					return date1 > date2 ? -1 : 1;
					
				default :
					return 0;
			}
		}

		/**
		 * Returns the number of the column by which this is sorting.
		 * @return the column number
		 */
		public int getColumnNumber() {
			return columnNumber;
		}

		/**
		 * Returns true for descending, or false
		 * for ascending sorting order.
		 * @return returns true if reversed
		 */
		public boolean isReversed() {
			return reversed;
		}

		/**
		 * Sets the sorting order.
		 * @param newReversed 
		 */
		public void setReversed(boolean newReversed) {
			reversed = newReversed;
		}
	}
	
	/* private */ TreeViewer viewer;
	/* private */Font currentRevisionFont;
	
	private IFile currentFile;
	
	//column constants
	private static final int COL_DATE = 0;
	
	protected IFileRevision adaptToFileRevision(Object element) {
		// Get the log entry for the provided object
		IFileRevision entry = null;
		if (element instanceof IFileRevision) {
			entry = (IFileRevision) element;
		} else if (element instanceof IAdaptable) {
			entry = (IFileRevision) ((IAdaptable) element).getAdapter(IFileRevision.class);
		} else if (element instanceof AbstractHistoryCategory){
			IFileRevision[] revisions = ((AbstractHistoryCategory) element).getRevisions();
			if (revisions.length > 0)
				entry = revisions[0]; 
		}
		return entry;
	}

	/**
	 * Creates the columns for the history table.
	 */
	private void createColumns(Tree tree, TableLayout layout) {
		SelectionListener headerListener = getColumnListener(viewer);
		// creation date
		TreeColumn col = new TreeColumn(tree, SWT.NONE);
		col.setResizable(true);
		col.setText(TeamUIMessages.GenericHistoryTableProvider_RevisionTime);
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));

		/*// author
		col = new TreeColumn(tree, SWT.NONE);
		col.setResizable(true);
		col.setText(TeamUIMessages.GenericHistoryTableProvider_Author);
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));

		//comment
		col = new TreeColumn(tree, SWT.NONE);
		col.setResizable(true);
		col.setText(TeamUIMessages.GenericHistoryTableProvider_Comment);
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(50, true));*/
	}
	
	/**
	 * Adds the listener that sets the sorter.
	 */
	private SelectionListener getColumnListener(final TreeViewer treeViewer) {
		/**
		 * This class handles selections of the column headers.
		 * Selection of the column header will cause resorting
		 * of the shown tasks using that column's sorter.
		 * Repeated selection of the header will toggle
		 * sorting order (ascending versus descending).
		 */
		return new SelectionAdapter() {
			/**
			 * Handles the case of user selecting the
			 * header area.
			 * <p>If the column has not been selected previously,
			 * it will set the sorter of that column to be
			 * the current tasklist sorter. Repeated
			 * presses on the same column header will
			 * toggle sorting order (ascending/descending).
			 */
			public void widgetSelected(SelectionEvent e) {
				// column selected - need to sort
				int column = treeViewer.getTree().indexOf((TreeColumn) e.widget);
				HistoryComparator oldSorter = (HistoryComparator) treeViewer.getComparator();
				if (oldSorter != null && column == oldSorter.getColumnNumber()) {
					oldSorter.setReversed(!oldSorter.isReversed());
					treeViewer.refresh();
				} else {
					treeViewer.setComparator(new HistoryComparator(column));
				}
			}
		};
	}

	/**
	 * Create a TreeViewer that can be used to display a list of IFile instances.
	 * Ths method provides the labels and sorter but does not provide a content provider
	 * 
	 * @param parent
	 * @return TableViewer
	 */
	public TreeViewer createTree(Composite parent) {
		Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(false);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		tree.setLayoutData(data);

		TableLayout layout = new TableLayout();
		tree.setLayout(layout);

		this.viewer = new TreeViewer(tree);
		
		createColumns(tree, layout);

		viewer.setLabelProvider(new LocalHistoryLabelProvider(this));

		// By default, reverse sort by revision. 
		// If local filter is on sort by date
		HistoryComparator sorter = new HistoryComparator(COL_DATE);
		sorter.setReversed(false);
		viewer.setComparator(sorter);

		tree.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (currentRevisionFont != null) {
					currentRevisionFont.dispose();
				}
			}
		});
		
		return viewer;
	}

	public void setFile(IFile file) {
		this.currentFile = file;
	}
	
	public long getCurrentRevision() {

		if (currentFile != null) {
			return currentFile.getLocalTimeStamp();
		}

		return 0;
	}

	private static class ThemeListener implements IPropertyChangeListener {

		private final LocalHistoryTableProvider provider;
		
		ThemeListener(LocalHistoryTableProvider provider) {
			this.provider= provider;
		}
		public void propertyChange(PropertyChangeEvent event) {
			provider.viewer.refresh();
		}
	}
}
