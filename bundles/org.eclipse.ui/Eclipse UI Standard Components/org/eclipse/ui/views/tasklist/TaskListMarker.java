package org.eclipse.ui.views.tasklist;

import java.util.Map;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/** The TaskListMarker is a wrapper for a marker with the 
 * attributes relevant to the task list cached.
 */
class TaskListMarker implements IMarker {

	private static String[] attributeNames =
		{
			IMarker.MESSAGE,
			IMarker.LINE_NUMBER,
			IMarker.LOCATION,
			IMarker.SEVERITY,
			IMarker.DONE,
			IMarker.PRIORITY };

	private Object[] attributes = new Object[6];

	IMarker marker;

	/**
	 * Create a new instance of the receiver that caches the 
	 * supplied marker
	 */

	TaskListMarker(IMarker cachedMarker) {
		this.marker = cachedMarker;
		populateAttributes();
	}

	/* 
	 * Get all of the attributes relevant to marker and cache them
	 * for later lookup
	 */
	private void populateAttributes() {

		Object[] queriedAttributes;

		//Set the receivers attributes to the default
		this.attributes = new Object[6];
		attributes[0] = "";
		attributes[1] = new Integer(-1);
		attributes[2] = "";
		attributes[3] = new Integer(IMarker.SEVERITY_WARNING);
		attributes[4] = new Boolean(false);
		attributes[5] = new Integer(IMarker.PRIORITY_NORMAL);

		try {
			queriedAttributes = marker.getAttributes(attributeNames);
		} catch (CoreException exception) {
			//There has been an exception - leave with defaults
			return;
		}

		for (int i = 0; i < this.attributes.length; i++) {
			if (queriedAttributes[i] != null)
				this.attributes[i] = queriedAttributes[i];
		}
	}

	/**
	 * @see IMarker#delete()
	 */
	public void delete() throws CoreException {
		this.marker.delete();
	}

	/**
	 * @see IMarker#exists()
	 */
	public boolean exists() {
		return this.marker.exists();
	}

	/**
	 * Return the index of the attribute in the attribute names
	 * if it is there. If not return -1
	 */

	private int indexOfAttribute(String attributeName) {
		for (int i = 0; i < attributeNames.length; i++) {
			if (attributeNames[i].equals(attributeName))
				return i;
		}
		return -1;
	}

	/**
	 * @see IMarker#getAttribute(String)
	 */
	public Object getAttribute(String name) throws CoreException {
		int index = indexOfAttribute(name);
		if (index > 0)
			return attributes[index];
		else
			return marker.getAttribute(name);
	}

	/**
	 * @see IMarker#getAttribute(String, int)
	 */
	public int getAttribute(String name, int defaultValue) {
		int index = indexOfAttribute(name);
		if (index > 0)
			return ((Integer) attributes[index]).intValue();
		else
			return marker.getAttribute(name, defaultValue);
	}

	/**
	 * @see IMarker#getAttribute(String, String)
	 */
	public String getAttribute(String arg0, String arg1) {
		int index = indexOfAttribute(arg0);
		if (index > 0)
			return ((String) attributes[index]);
		else
			return marker.getAttribute(arg0, arg1);
	}

	/**
	 * @see IMarker#getAttribute(String, boolean)
	 */
	public boolean getAttribute(String arg0, boolean arg1) {
		int index = indexOfAttribute(arg0);
		if (index > 0)
			return ((Boolean) attributes[index]).booleanValue();
		else
			return marker.getAttribute(arg0, arg1);
	}

	/**
	 * @see IMarker#getAttributes()
	 */
	public Map getAttributes() throws CoreException {
		return this.marker.getAttributes();
	}

	/**
	 * @see IMarker#getAttributes(String[])
	 */
	public Object[] getAttributes(String[] arg0) throws CoreException {
		return this.marker.getAttributes(arg0);
	}

	/**
	 * @see IMarker#getId()
	 */
	public long getId() {
		return this.marker.getId();
	}

	/**
	 * @see IMarker#getResource()
	 */
	public IResource getResource() {
		return this.marker.getResource();
	}

	/**
	 * @see IMarker#getType()
	 */
	public String getType() throws CoreException {
		return this.marker.getType();
	}

	/**
	 * @see IMarker#isSubtypeOf(String)
	 */
	public boolean isSubtypeOf(String arg0) throws CoreException {
		return this.marker.isSubtypeOf(arg0);
	}

	/**
	 * @see IMarker#setAttribute(String, int)
	 */
	public void setAttribute(String arg0, int arg1) throws CoreException {
		this.marker.setAttribute(arg0, arg1);
		if (indexOfAttribute(arg0) > 0)
			populateAttributes();
	}

	/**
	 * @see IMarker#setAttribute(String, Object)
	 */
	public void setAttribute(String arg0, Object arg1) throws CoreException {
		this.marker.setAttribute(arg0, arg1);
		if (indexOfAttribute(arg0) > 0)
			populateAttributes();
	}

	/**
	 * @see IMarker#setAttribute(String, boolean)
	 */
	public void setAttribute(String arg0, boolean arg1) throws CoreException {
		this.marker.setAttribute(arg0, arg1);
		if (indexOfAttribute(arg0) > 0)
			populateAttributes();
	}

	/**
	 * @see IMarker#setAttributes(String[], Object[])
	 */
	public void setAttributes(String[] arg0, Object[] arg1) throws CoreException {
		this.marker.setAttributes(arg0, arg1);
		populateAttributes();
	}

	/**
	 * @see IMarker#setAttributes(Map)
	 */
	public void setAttributes(Map arg0) throws CoreException {
		this.marker.setAttributes(arg0);
		populateAttributes();
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class arg0) {
		return this.marker.getAdapter(arg0);
	}

}