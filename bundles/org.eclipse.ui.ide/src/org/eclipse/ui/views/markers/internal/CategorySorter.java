package org.eclipse.ui.views.markers.internal;

import java.util.Comparator;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * CategorySorter is the sorter that takes categories and the viewer into
 * account.
 * 
 */
public class CategorySorter extends ViewerSorter implements Comparator {
	TableSorter innerSorter;

	IField categoryField;

	boolean reverseSort = false;
	
	private final String TAG_FIELD = "categoryField"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver wrapping a sorter.
	 * 
	 * @param sorter
	 */
	CategorySorter(TableSorter sorter) {
		innerSorter = sorter;
	}

	/**
	 * Compare obj1 and obj starting with field depth.
	 * 
	 * @param obj1
	 * @param obj2
	 * @param depth
	 * @param continueSearching
	 * @return int
	 * @see ViewerSorter#compare(Viewer, Object, Object)
	 */
	int compare(Object obj1, Object obj2, int depth, boolean continueSearching) {

		if (obj1 == null || obj2 == null || !(obj1 instanceof MarkerNode)
				|| !(obj2 instanceof MarkerNode)) {
			return 0;
		}

		MarkerNode marker1;
		MarkerNode marker2;
		if (reverseSort) {
			marker1 = (MarkerNode) obj2;
			marker2 = (MarkerNode) obj1;
		} else {
			marker1 = (MarkerNode) obj1;
			marker2 = (MarkerNode) obj2;
		}

		if (!marker1.isConcrete() || !marker2.isConcrete())
			return marker1.getDescription().compareTo(marker2.getDescription());

		if (categoryField == null)
			return innerSorter.compare(marker1, marker2, depth,
					continueSearching);

		if (depth == 0) { // Is this the hierachy check
			int result = categoryField.compare(marker1, marker2);
			if (continueSearching && result == 0)
				return innerSorter.compare(marker1, marker2, 0,
						continueSearching);
			return result;
		}

		// Now head for the table sorter
		return innerSorter.compare(marker1, marker2, depth - 1,
				continueSearching);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		return compare(e1, e2, 0, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {
		return compare(arg0, arg1, 0, true);
	}

	/**
	 * Get the category field.
	 * 
	 * @return IField
	 */
	public IField getCategoryField() {
		return categoryField;
	}

	/**
	 * Set the field that we are categorizing by.
	 * 
	 * @param field
	 */
	public void setCategoryField(IField field) {
		this.categoryField = field;
		reverseSort = false;
	}

	/**
	 * Reverse the direction we are sorting in
	 */
	public void reverseSortDirection() {
		reverseSort = !reverseSort;

	}

	/**
	 * Set the inner sorter to the new sorter.
	 * @param sorter2
	 */
	public void setTableSorter(TableSorter sorter2) {
		innerSorter = sorter2;
		
	}

	/**
	 * Save the state of the receiver.
	 * @param dialogSettings
	 */
	public void saveState(IDialogSettings dialogSettings) {
		if (dialogSettings == null) {
			return;
		}

		IDialogSettings settings = dialogSettings
				.getSection(TableSorter.TAG_DIALOG_SECTION);
		if (settings == null) {
			settings = dialogSettings.addNewSection(TableSorter.TAG_DIALOG_SECTION);
		}
		
		String description = Util.EMPTY_STRING;
		if(categoryField != null)
			description = categoryField.getDescription();
		
		settings.put(TAG_FIELD, description);
		
	}

	/**
	 * Restore the state of the receiver from the dialog settings.
	 * @param dialogSettings
	 * @param view
	 */
	public void restoreState(IDialogSettings dialogSettings, ProblemView view) {
		if (dialogSettings == null) {
			return;
		}

		IDialogSettings settings = dialogSettings
				.getSection(TableSorter.TAG_DIALOG_SECTION);
		if (settings == null) 
			return;
		
		String description = settings.get(TAG_FIELD);
		
		if(description.length() == 0){
			categoryField = null;
			return;
		}
		
		categoryField = view.findField(description);
	}
}
