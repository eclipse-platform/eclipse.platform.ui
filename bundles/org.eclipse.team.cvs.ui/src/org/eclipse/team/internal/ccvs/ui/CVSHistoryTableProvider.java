/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.ui;

import java.net.URI;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.history.*;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.filehistory.CVSFileRevision;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.core.history.LocalFileRevision;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.history.AbstractHistoryCategory;
import org.eclipse.team.internal.ui.history.DateHistoryCategory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;

import com.ibm.icu.text.DateFormat;

public class CVSHistoryTableProvider {

	public static final String CATEGORIES_COLOR = "org.eclipse.team.cvs.ui.fontsandcolors.cvshistorypagecategories";  //$NON-NLS-1$
	
	private IFileHistory currentFileHistory;
	private IFile workspaceFile;
	
	/* private */TreeViewer viewer;
	/* private */Font currentRevisionFont;

	private boolean baseModified;

	//column constants
	private static final int COL_REVISIONID = 0;
	private static final int COL_TAGS = 1;
	private static final int COL_DATE = 2;
	private static final int COL_AUTHOR = 3;
	private static final int COL_COMMENT = 4;

	/**
	 * The history label provider.
	 */
	class HistoryLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider, IFontProvider {
		
		Image dateImage = null;
		ImageDescriptor dateDesc = null;
		
		Image localRevImage = null;
		ImageDescriptor localRevDesc = null;
		
		Image remoteRevImage = null;
		ImageDescriptor remoteRevDesc = null;
		
		ThemeListener themeListener;
		
		public HistoryLabelProvider(CVSHistoryTableProvider provider){
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
			
			if (remoteRevImage != null) {
				remoteRevImage.dispose();
				remoteRevImage = null;
			}
			
			if (themeListener != null){
				PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(themeListener);
			}
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof DateHistoryCategory &&
				columnIndex == COL_REVISIONID){
				if (dateImage == null){
					dateDesc = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_DATES_CATEGORY);
					dateImage = dateDesc.createImage();
				} 
				return dateImage;
			}
			
			if (element instanceof LocalFileRevision &&
					columnIndex == COL_REVISIONID){
				if (localRevImage == null){
					localRevDesc = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_LOCALREVISION_TABLE);
					localRevImage = localRevDesc.createImage();
				}
				return localRevImage;
			}
			
			if (element instanceof CVSFileRevision &&
					columnIndex == COL_REVISIONID){
				if (remoteRevImage == null){
					remoteRevDesc = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_REMOTEREVISION_TABLE);
					remoteRevImage = remoteRevDesc.createImage();
				}
				return remoteRevImage;
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof AbstractHistoryCategory){
				if (columnIndex != COL_REVISIONID)
					return ""; //$NON-NLS-1$
				
				return ((AbstractHistoryCategory) element).getName();
			}
			
			IFileRevision entry = adaptToFileRevision(element);
			if (entry == null)
				return ""; //$NON-NLS-1$
			switch (columnIndex) {
				case COL_REVISIONID :
					String revision = entry.getContentIdentifier();
					String currentRevision = getCurrentRevision();
					if (currentRevision != null && currentRevision.equals(revision)) {
						if (baseModified)
							revision = NLS.bind(CVSUIMessages.nameAndRevision, new String[] { revision, CVSUIMessages.CVSHistoryTableProvider_base}); //NLS.bind(CVSUIMessages.currentRevision, new String[] { revision }); 
						else
							revision = NLS.bind(CVSUIMessages.currentRevision, new String[] {revision}); //NLS.bind(CVSUIMessages.currentRevision, new String[] { revision });
					}
					return revision;
				case COL_TAGS:
					ITag[] tags = entry.getTags();
					StringBuffer result = new StringBuffer();
					for (int i = 0; i < tags.length; i++) {
						result.append(tags[i].getName());
						if (i < tags.length - 1) {
							result.append(", "); //$NON-NLS-1$
						}
					}
					return result.toString();
				case COL_DATE :
					long date = entry.getTimestamp();
					Date dateFromLong = new Date(date);
					return DateFormat.getInstance().format(dateFromLong);
				case COL_AUTHOR :
					return entry.getAuthor();
				case COL_COMMENT :
					String comment = entry.getComment();
					if (comment != null){
						int index = comment.indexOf("\n"); //$NON-NLS-1$
						switch (index) {
							case -1:
								return comment;
							case 0:
								return CVSUIMessages.HistoryView_______4; 
							default:
								return NLS.bind(CVSUIMessages.CVSCompareRevisionsInput_truncate, new String[] { comment.substring(0, index) });
						}
					}
			}
			return ""; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			if (element instanceof AbstractHistoryCategory){
				ITheme current = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
				return current.getColorRegistry().get(CVSHistoryTableProvider.CATEGORIES_COLOR);
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
			String revision = entry.getContentIdentifier();
			//String comment = entry.getComment();
			String tempCurrentRevision = getCurrentRevision();
			Font returnFont = null;
			
			if (tempCurrentRevision != null && tempCurrentRevision.equals(revision)) {
				returnFont = getCurrentRevisionFont();
			}
			//Check to see if this is the local workspace file
			if (workspaceFile != null){
				URI entryURI = entry.getURI();
				URI workspaceURI = workspaceFile.getLocationURI();
				if (entryURI != null && workspaceURI != null){
					if (entryURI.compareTo(workspaceURI) == 0)
						return getCurrentRevisionFont();	
				}
			}
			
			return returnFont;
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
		
		private VersionCollator versionCollator = new VersionCollator();
		
		// column headings:	"Revision" "Tags" "Date" "Author" "Comment"
		private int[][] SORT_ORDERS_BY_COLUMN = { {COL_REVISIONID, COL_DATE, COL_AUTHOR, COL_COMMENT, COL_TAGS}, /* revision */
		{COL_TAGS, COL_DATE, COL_REVISIONID, COL_AUTHOR, COL_COMMENT}, /* tags */
		{COL_DATE, COL_REVISIONID, COL_AUTHOR, COL_COMMENT, COL_TAGS}, /* date */
		{COL_AUTHOR, COL_REVISIONID, COL_DATE, COL_COMMENT, COL_TAGS}, /* author */
		{COL_COMMENT, COL_REVISIONID, COL_DATE, COL_AUTHOR, COL_TAGS} /* comment */
		};

		/**
		 * The constructor.
		 */
		public HistoryComparator(int columnNumber) {
			this.columnNumber = columnNumber;
		}

		/**
		 * Compares two log entries, sorting first by the main column of this sorter,
		 * then by subsequent columns, depending on the column sort order.
		 */
		public int compare(Viewer compareViewer, Object o1, Object o2) {
			if (o1 instanceof AbstractHistoryCategory || o2 instanceof AbstractHistoryCategory)
				return 0;
			
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
				case 0 : /* revision */
					if (e1 instanceof LocalFileRevision ||
						e2 instanceof LocalFileRevision) {
						//compare based on dates
						long date1 = e1.getTimestamp();
						long date2 = e2.getTimestamp();
						if (date1 == date2)
							return 0;

						return date1 > date2 ? -1 : 1;
					}
					return versionCollator.compare(e1.getContentIdentifier(), e2.getContentIdentifier());
				case 1: /* tags */
					ITag[] tags1 = e1.getTags();
					ITag[] tags2 = e2.getTags();
					if (tags2.length == 0) {
						return -1;
					}
					if (tags1.length == 0) {
						return 1;
					}
					return getComparator().compare(tags1[0].getName(), tags2[0].getName());
				case 2 : /* date */
					long date1 = e1.getTimestamp();
					long date2 = e2.getTimestamp();
					if (date1 == date2)
						return 0;

					return date1 > date2 ? -1 : 1;

				case 3 : /* author */
					String author1 = e1.getAuthor();
					String author2 = e2.getAuthor();
					if (author2 == null)
						return -1;
					
					if (author1 == null)
						return 1;
					
					return getComparator().compare(author1, author2);
				case 4 : /* comment */
					String comment1 = e1.getComment();
					String comment2 = e2.getComment();
					if (comment2 == null)
						return -1;
					
					if (comment1 == null)
						return 1;
					
					return getComparator().compare(comment1, comment2);
				default :
					return 0;
			}
		}

		/**
		 * Returns the number of the column by which this is sorting.
		 */
		public int getColumnNumber() {
			return columnNumber;
		}

		/**
		 * Returns true for descending, or false
		 * for ascending sorting order.
		 */
		public boolean isReversed() {
			return reversed;
		}

		/**
		 * Sets the sorting order.
		 */
		public void setReversed(boolean newReversed) {
			reversed = newReversed;
		}
	}

	protected IFileRevision adaptToFileRevision(Object element) {
		// Get the log entry for the provided object
		IFileRevision entry = null;
		if (element instanceof IFileRevision) {
			entry = (IFileRevision) element;
		} else if (element instanceof IAdaptable) {
			entry = (IFileRevision) ((IAdaptable) element).getAdapter(IFileRevision.class);
		} else if (element instanceof AbstractHistoryCategory){
			entry = ((AbstractHistoryCategory) element).getRevisions()[0];
		}
		return entry;
	}

	/**
	 * Create a TableViewer that can be used to display a list of IFileRevision instances.
	 * Ths method provides the labels and sorter but does not provide a content provider
	 * 
	 * @param parent
	 * @return TableViewer
	 */
	public TreeViewer createTree(Composite parent) {
		Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		tree.setLayoutData(data);

		TableLayout layout = new TableLayout();
		tree.setLayout(layout);

		this.viewer = new TreeViewer(tree);
		
		createColumns(tree, layout);

		viewer.setLabelProvider(new HistoryLabelProvider(this));

		// By default, reverse sort by revision. 
		// If local filter is on sort by date
		HistoryComparator sorter = new HistoryComparator(COL_DATE);
		/*HistorySorter sorter = new HistorySorter(COL_REVISIONID);
		sorter.setReversed(true);*/
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

	/**
	 * Creates the columns for the history table.
	 */
	private void createColumns(Tree tree, TableLayout layout) {
		SelectionListener headerListener = getColumnListener(viewer);
		// revision
		TreeColumn col = new TreeColumn(tree, SWT.NONE);
		col.setResizable(true);
		col.setText(TeamUIMessages.GenericHistoryTableProvider_Revision);
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));

		// tags
		col = new TreeColumn(tree, SWT.NONE);
		col.setResizable(true);
		col.setText(CVSUIMessages.HistoryView_tags); 
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));
		// creation date
		col = new TreeColumn(tree, SWT.NONE);
		col.setResizable(true);
		col.setText(TeamUIMessages.GenericHistoryTableProvider_RevisionTime);
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));

		// author
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
		layout.addColumnData(new ColumnWeightData(50, true));
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

	public void setFile(IFileHistory fileHistory, IFile workspaceFile) {
		this.currentFileHistory = fileHistory;
		this.workspaceFile = workspaceFile;
	}


	public IFileHistory getIFileHistory() {
		return this.currentFileHistory;
	}

	public String getCurrentRevision() {
		
		try {
			if (workspaceFile != null) {
				ICVSFile cvsWorkspaceFile = CVSWorkspaceRoot.getCVSFileFor(workspaceFile);
				byte[] syncBytes = cvsWorkspaceFile.getSyncBytes();
				if (syncBytes != null) {
					String workspaceRevision = ResourceSyncInfo.getRevision(syncBytes);
					return workspaceRevision;
				}
			}
		
		} catch (CVSException e) {
		}

		return null;
	}
	
	/*
	 * Used to reset the sorting for the table provider; if local files
	 * are included in the table, then we sort by date. Otherwise we default
	 * to sorting by revision 
	 */
	public void setLocalRevisionsDisplayed(boolean displayed){
		//init sort to sort by revision
		int column = COL_REVISIONID;
		if (displayed){
			//locals displayed, if the base has been modified then sort by DATE
			column = COL_DATE;	
		}
		
		HistoryComparator oldSorter = (HistoryComparator) viewer.getComparator();
		if (oldSorter != null && column == oldSorter.getColumnNumber()) {
			oldSorter.setReversed(column == COL_REVISIONID);
			viewer.refresh();
		} else {
			HistoryComparator newSorter = new HistoryComparator(column);
			newSorter.setReversed(column == COL_REVISIONID);
			viewer.setComparator(newSorter);
			
		}
	}

	public void setBaseModified(boolean modified) {
		this.baseModified=modified;
	}
	
	private static class ThemeListener implements IPropertyChangeListener {

		private final CVSHistoryTableProvider provider;
		
		ThemeListener(CVSHistoryTableProvider provider) {
			this.provider= provider;
		}
		public void propertyChange(PropertyChangeEvent event) {
			provider.viewer.refresh();
		}
	}

	public void setWorkspaceFile(IFile workspaceFile) {
		this.workspaceFile = workspaceFile;
	}
}
