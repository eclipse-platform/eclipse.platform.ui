package org.eclipse.ui.views.markers.internal;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * CategorySorter is the sorter that takes categories and the viewer into account.
 *
 */
public class CategorySorter extends ViewerSorter implements Comparator {
	TableSorter innerSorter;

	/**
	 * Create a new instance of the receiver wrapping a sorter.
	 * 
	 * @param sorter
	 */
	CategorySorter(TableSorter sorter) {
		innerSorter = sorter;
	}

	IField typeField = new FieldMarkerType();

	IField categoryField = new FieldCategory();
	
	IField hierarchyField = new FieldHierarchy();

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

		MarkerNode marker1 = (MarkerNode) obj1;
		MarkerNode marker2 = (MarkerNode) obj2;
		
		if(!marker1.isConcrete() || !marker2.isConcrete())
			return marker1.getDescription().compareTo(
				marker2.getDescription());
	

		if (depth == 0) { // Is this the type check?
			int result = categoryField.compare(obj1, obj2);
			if (continueSearching && result == 0)
				return compare(obj1, obj2, depth + 1, continueSearching);
			return result;
		}

		if (depth == 1) { // Is this the type check?
			int result = typeField.compare(obj1, obj2);
			if (continueSearching && result == 0)
				return compare(obj1, obj2, depth + 1, continueSearching);
			return result;
		}

		// Now we just use the sorters are we are past the two reserved depths

		String type;
		int categoryDepth = depth - 2;
		try {
			type = ((ConcreteMarker) marker1).getMarker().getType();
		} catch (CoreException e) {
			IDEWorkbenchPlugin.log(e.getLocalizedMessage(), e);
			return 0;
		}
		TableSorter categorySorter = MarkerSupportRegistry.getInstance()
				.getSorterFor(type);

		int categoryCount = categorySorter.getFields().length;

		if (categoryDepth > categoryCount)// Are we past categories?
			return innerSorter.compare(obj1, obj2, depth - 2 - categoryCount,
					continueSearching);
		int result = categorySorter.getFields()[categoryDepth].compare(obj1,
				obj2);
		if (continueSearching && result == 0)
			return categorySorter.compare(obj1, obj2, categoryDepth + 1,
					continueSearching);
		return result;
	}

	/**
	 * Return the category field at index field index.
	 * 
	 * @param fieldIndex
	 * @param type
	 * @return IField
	 */
	public IField getCategoryField(int fieldIndex, String type) {
		switch (fieldIndex) {
		case 0:
			return categoryField;
		case 1:
			return typeField;

		default:
			return MarkerSupportRegistry.getInstance().getSorterFor(type)
					.getFields()[fieldIndex - 2];
		}

	}

	/**
	 * Return the number of categories possible for type type.
	 * 
	 * @param type
	 * @return int
	 */
	public int getCategoryFieldCount(String type) {
		return MarkerSupportRegistry.getInstance().getSorterFor(type)
				.getFields().length + 2;
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
	 * Return whether or not there would be an entry for
	 * i for type type.
	 * @param i
	 * @param type
	 * @return boolean
	 */
	public boolean hasField(int i, String type) {
		return getCategoryFieldCount(type) >= i;
	}
}
