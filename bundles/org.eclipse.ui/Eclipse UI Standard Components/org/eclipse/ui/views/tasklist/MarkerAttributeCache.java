package org.eclipse.ui.views.tasklist;

import java.util.Hashtable;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * The MarkerAttributeCache is the class that holds onto the attributes
 * for markers to prevent constant file I/O access for operations
 * that query the same information several times like sorting.
 */

/*package*/
class MarkerAttributeCache {

	private Hashtable messages = new Hashtable();
	private Hashtable lineNumbers = new Hashtable();
	private Hashtable locations = new Hashtable();
	private Hashtable severities = new Hashtable();
	private Hashtable priorities = new Hashtable();
	private Hashtable containerNames = new Hashtable();
	private Hashtable types = new Hashtable();
	private Hashtable completion = new Hashtable();
	private static String[] attributeNames =
		{
			IMarker.MESSAGE,
			IMarker.LINE_NUMBER,
			IMarker.LOCATION,
			IMarker.SEVERITY,
			IMarker.DONE,
			IMarker.PRIORITY };

	public MarkerAttributeCache() {
	}

	/* 
	 * Get all of the attributes relevant to marker and cache them
	 * for later lookup
	 */
	private void getAttributes(IMarker marker) {

		Object[] attributes = new Object[6];
		try {
			attributes = marker.getAttributes(attributeNames);
		} catch (CoreException excption) {
			//There has been an exception - populate with defaults
			messages.put(marker, "");
			lineNumbers.put(marker, new Integer(-1));
			locations.put(marker, "");
			severities.put(marker, new Integer(IMarker.SEVERITY_WARNING));
			completion.put(marker, new Boolean(false));
			priorities.put(marker, new Integer(IMarker.PRIORITY_NORMAL));
			return;
		}
	
		addEntry(messages,marker,attributes[0],"");
		addEntry(lineNumbers,marker,attributes[1],new Integer(-1));
		addEntry(locations,marker,attributes[2],"");
		addEntry(severities,marker,attributes[3],new Integer(IMarker.SEVERITY_WARNING));
		addEntry(completion,marker,attributes[4],new Boolean(false));
		addEntry(priorities,marker,attributes[5],new Integer(IMarker.PRIORITY_NORMAL));

	}
	
	/**
	 * Add the value to the table keyed on the marker. If that
	 * value is null add the default instead
	 */ 
	private void addEntry(Hashtable table, IMarker marker, Object value, Object defaultValue){
		if(value == null)
			table.put(marker,defaultValue);
		else
			table.put(marker,value);
	}		

	public String getMessage(IMarker marker) {
		if (!this.messages.containsKey(marker))
			getAttributes(marker);
		return (String) messages.get(marker);

	}

	/**
	* Returns the line number of the given marker.
	*/
	public int getLineNumber(IMarker marker) {
		if (!this.lineNumbers.containsKey(marker))
			getAttributes(marker);
		return ((Integer) lineNumbers.get(marker)).intValue();
	}
	/**
	 * Returns the text for the location field.
	 */
	public String getLocation(IMarker marker) {
		if (!this.locations.containsKey(marker))
			getAttributes(marker);
		return (String) locations.get(marker);
	}

	/**
	* Returns the severity of the given marker.  Default is SEVERITY_WARNING.
	*/
	public int getSeverity(IMarker marker) {
		if (!this.severities.containsKey(marker))
			getAttributes(marker);
		return ((Integer) severities.get(marker)).intValue();

	}

	/**
	* Returns the priority of the given marker.  Default is PRIORITY_NORMAL.
	*/
	public int getPriority(IMarker marker) {
		if (!this.priorities.containsKey(marker))
			getAttributes(marker);
		return ((Integer) priorities.get(marker)).intValue();
	}

	/**
	* Returns the sort order for the given marker based on its completion status.
	* Lower numbers appear first.
	*/
	public int getCompletedOrder(IMarker marker) {

		if (!this.completion.containsKey(marker))
			getAttributes(marker);
		return ((Boolean) completion.get(marker)).booleanValue() ? 0 : 1;
	}

	public String getContainerName(IMarker marker) {

		if (!containerNames.contains(marker)) {
			// taking substring from resource's path string is 40x faster 
			// than getting relative path string from resource's parent
			String path = marker.getResource().getFullPath().toString();
			int i = path.lastIndexOf(IPath.SEPARATOR);
			String parentName;
			if (i == 0)
				parentName = ""; //$NON-NLS-1$
			else
				parentName = path.substring(1, i);
			containerNames.put(marker, parentName);
		}
		return (String) containerNames.get(marker);
	}

	/**
	* Returns the text for the line and location column.
	*/
	public String getLineAndLocation(IMarker marker) {
		int lineNumber = getLineNumber(marker);
		String location = getLocation(marker);
		if (lineNumber == -1) {
			if (location.equals("")) { //$NON-NLS-1$
				return ""; //$NON-NLS-1$
			} else {
				return location;
			}
		} else {
			if (location.equals("")) { //$NON-NLS-1$
				return TaskListMessages.format(
					"TaskList.line",
					new Object[] { new Integer(lineNumber)});
				//$NON-NLS-1$
			} else {
				return TaskListMessages.format(
					"TaskList.lineAndLocation",
					new Object[] { new Integer(lineNumber), location });
				//$NON-NLS-1$
			}
		}
	}

	/**
	* Returns whether the given marker is of the given type (either directly or indirectly).
	*/
	public boolean isMarkerType(IMarker marker, String type) {
		if (type.equals(types.get(marker)))
			return true;
		try {
			return marker.isSubtypeOf(type);
		} catch (CoreException e) {
			return false;
		}
	}

}