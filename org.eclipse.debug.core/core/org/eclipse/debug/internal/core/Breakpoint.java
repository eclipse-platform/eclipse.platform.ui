package org.eclipse.debug.internal.core;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IBreakpoint;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.debug.core.model.IDebugTarget;

public abstract class Breakpoint implements IBreakpoint {
	
	private static String fMarkerType= IDebugConstants.BREAKPOINT_MARKER;
	
	/**
	 * The set of attributes used to configure a breakpoint
	 */
	protected static final String[] fgBreakpointAttributes= new String[]{IDebugConstants.MODEL_IDENTIFIER, IDebugConstants.ENABLED};	
	
	protected IMarker fMarker= null;
	protected boolean installed= false;
	
	/**
	 * Returns the marker type that this breakpoint creates.
	 * Subclasses should override this method.
	 */
	public static String getMarkerType() {
		return fMarkerType;
	}

	/**
	 * Constructor for Breakpoint
	 */
	public Breakpoint() {
	}
	
	/**
	 * Create a breakpoint for the given marker
	 */
	public Breakpoint(IMarker marker) {
		try {
			if (marker.isSubtypeOf(IDebugConstants.BREAKPOINT_MARKER)) {
				fMarker= marker;
			}
		} catch (CoreException ce) {
		}
	}
	
	public void setMarker(IMarker marker) {
		fMarker= marker;
	}
	
	/**
	 * Returns whether the given object is equal to this object.
	 * 
	 * Two breakpoints are equal if their markers have the same id.
	 * A breakpoint is not equal to any other kind of object.
	 */
	public boolean equals(Object item) {
		if (item instanceof IBreakpoint) {
			return getId() == ((IBreakpoint)item).getId();
		}
		return false;
	}
	
	/**
	 * Configures the given breakpoint's <code>MODEL_IDENTIFIER</code>
	 * and <code>ENABLED</code> attributes to the given values.
	 * This is a convenience method for
	 * <code>IBreakpoint.setAttribute(String, Object)</code> and
	 * <code>IBreakpoint.setAttribute(String, boolean)</code>.
	 * <code>IBreakpoint.setAttribute(String, int)</code>.
	 *
	 * @param modelIdentifier the identifier of the debug model plug-in
	 *    the breakpoint is associated with
	 * @param enabled the initial value of the enabled attribute of the
	 *	breakpoint marker
	 * 
	 * @exception CoreException if setting an attribute fails
	 * @see IBreakpoint#setAttribute(String, Object)
	 * @see IBreakpoint#setAttribute(String, boolean)
	 * @see IBreakpoint#setAttribute(String, int)
	 */
	public void configure(String modelIdentifier, boolean enabled) throws CoreException {
		setAttributes(fgBreakpointAttributes, new Object[]{modelIdentifier, new Boolean(enabled)});
	}	
	
	/**
	 * @see IBreakpoint#addToTarget(IDebugTarget)
	 */
	public abstract void addToTarget(IDebugTarget target);
	
	/**
	 * @see IBreakpoint#changeForTarget(IDebugTarget)
	 */
	public abstract void changeForTarget(IDebugTarget target);
	
	/**
	 * @see IBreakpoint#removeFromTarget(IDebugTarget)
	 */
	public abstract void removeFromTarget(IDebugTarget target);
	
	/**
	 * Enable the breakpoint
	 */
	public void enable() throws CoreException {
		fMarker.setAttribute(IDebugConstants.ENABLED, true);
	}
	
	/**
	 * Returns whether the breakpoint is enabled
	 */
	public boolean isEnabled() throws CoreException {
		return fMarker.getAttribute(IDebugConstants.ENABLED, false);
	}
	
	/**
	 * @see IBreakpoint#toggleEnabled()
	 */
	public void toggleEnabled() throws CoreException {
		if (isEnabled()) {
			disable();
		} else {
			enable();
		}
	}
	
	/**
	 * Disable the breakpoint
	 */
	public void disable() throws CoreException {
		fMarker.setAttribute(IDebugConstants.ENABLED, false);		
	}
	
	/**
	 * Returns whether the breakpoint is disabled
	 */
	public boolean isDisabled() throws CoreException {
		return !isEnabled();
	}

	/**
	 * @see IBreakpoint#delete()
	 */
	public void delete() throws CoreException {
		fMarker.delete();
	}

	/**
	 * @see IBreakpoint#exists()
	 */
	public boolean exists() {
		return fMarker.exists();
	}
	

	/**
	 * @see IBreakpoint#getMarker()
	 */
	public IMarker getMarker() {
		return fMarker;
	}

	/**
	 * @see IBreakpoint#getId()
	 */
	public long getId() {
		return fMarker.getId();
	}

	/**
	 * @see IBreakpoint#getResource()
	 */
	public IResource getResource() {
		return fMarker.getResource();
	}	

	/**
	 * @see IBreakpoint#getType()
	 */
	public String getType() throws CoreException {
		return fMarker.getType();
	}

	/**
	 * Returns the model identifier for the given breakpoint.
	 */
	public String getModelIdentifier() {
		return (String)getAttribute(IDebugConstants.MODEL_IDENTIFIER, null);
	}
	
	/**
	 * Returns the attribute with the given name.  The result is an instance of one
	 * of the following classes: <code>String</code>, <code>Integer</code>, 
	 * or <code>Boolean</code>.
	 * Returns <code>null</code> if the attribute is undefined.
	 *
	 * @param attributeName the name of the attribute
	 * @return the value, or <code>null</code> if the attribute is undefined.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This breakpoint does not exist.</li>
	 * </ul>
	 */
	protected Object getAttribute(String attributeName) throws CoreException {
		return fMarker.getAttribute(attributeName);
	}

	/**
	 * Returns the integer-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined.
	 * or the marker does not exist or is not an integer value.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 */
	protected int getAttribute(String attributeName, int defaultValue) {
		return fMarker.getAttribute(attributeName, defaultValue);
	}

	/**
	 * Returns the string-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined
	 * or the marker does not exist or is not a string value.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 */
	protected String getAttribute(String attributeName, String defaultValue) {
		return fMarker.getAttribute(attributeName, defaultValue);
	}	
	
	/**
	 * Returns the boolean-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined
	 * or the marker does not exist or is not a boolean value.
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
 	 * @return the value or the default value if no value was found.
	 */
	protected boolean getAttribute(String attributeName, boolean defaultValue) {
		return fMarker.getAttribute(attributeName, defaultValue);
	}

	/**
	 * Returns a map with all the attributes for the marker.
	 * If the marker has no attributes then <code>null</code> is returned.
	 *
	 * @return a map of attribute keys and values (key type : <code>String</code> 
	 *		value type : <code>String</code>, <code>Integer</code>, or 
	 *		<code>Boolean</code>) or <code>null</code>.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> The marker does not exist.</li>
	 * </ul>
	 */
	protected Map getAttributes() throws CoreException {
		return fMarker.getAttributes();
	}

	/**
	 * Returns the attributes with the given names.  The result is an an array 
	 * whose elements correspond to the elements of the given attribute name
	 * array.  Each element is <code>null</code> or an instance of one
	 * of the following classes: <code>String</code>, <code>Integer</code>, 
	 * or <code>Boolean</code>.
	 *
	 * @param attributeNames the names of the attributes
	 * @return the values of the given attributes.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> The marker does not exist.</li>
	 * </ul>
	 */
	protected Object[] getAttributes(String[] attributeNames) throws CoreException {
		return fMarker.getAttributes(attributeNames);
	}


	/**
	 * Returns the <code>boolean</code> attribute of the given breakpoint
	 * or <code>false</code> if the attribute is not set.
	 */
	protected boolean getBooleanAttribute(String attribute) {
		return getAttribute(attribute, false);
	}	

	/**
	 * Sets the <code>boolean</code> attribute of the given breakpoint.
	 */
	protected void setBooleanAttribute(String attribute, boolean value) throws CoreException {
		setAttribute(attribute, value);	
	}	

	/**
	 * Sets the integer-valued attribute with the given name.  
	 * <p>
	 * This method changes resources; these changes will be reported
	 * in a subsequent resource change event, including an indication 
	 * that this marker has been modified.
	 * </p>
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * </ul>
	 */
	protected void setAttribute(String attributeName, int value)
		throws CoreException {
			fMarker.setAttribute(attributeName, value);
	}

	/**
	 * Sets the attribute with the given name.  The value must be <code>null</code> or 
	 * an instance of one of the following classes: 
	 * <code>String</code>, <code>Integer</code>, or <code>Boolean</code>.
	 * If the value is <code>null</code>, the attribute is considered to be undefined.
	 * <p>
	 * This method changes resources; these changes will be reported
	 * in a subsequent resource change event, including an indication 
	 * that this marker has been modified.
	 * </p>
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value, or <code>null</code> if the attribute is to be undefined
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> The marker does not exist.</li>
	 * </ul>
	 */
	protected void setAttribute(String attributeName, Object value)
		throws CoreException {
			fMarker.setAttribute(attributeName, value);
	}

	/**
	 * Sets the boolean-valued attribute with the given name.  
	 * <p>
	 * This method changes resources; these changes will be reported
	 * in a subsequent resource change event, including an indication 
	 * that this marker has been modified.
	 * </p>
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> The marker does not exist.</li>
	 * </ul>
	 */
	protected void setAttribute(String attributeName, boolean value)
		throws CoreException {
			fMarker.setAttribute(attributeName, value);
	}

	/**
	 * Sets the given attribute key-value pairs on this marker.
	 * The values must be <code>null</code> or an instance of 
	 * one of the following classes: <code>String</code>, 
	 * <code>Integer</code>, or <code>Boolean</code>.
	 * If a value is <code>null</code>, the new value of the 
	 * attribute is considered to be undefined.
	 * <p>
	 * This method changes resources; these changes will be reported
	 * in a subsequent resource change event, including an indication 
	 * that this marker has been modified.
	 * </p>
	 *
	 * @param attributeNames an array of attribute names
	 * @param values an array of attribute values
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * </ul>
	 */
	protected void setAttributes(String[] attributeNames, Object[] values)
		throws CoreException {
			fMarker.setAttributes(attributeNames, values);
	}

	/**
	 * Sets the attributes for this marker to be the ones contained in the
	 * given table. The values must be an instance of one of the following classes: 
	 * <code>String</code>, <code>Integer</code>, or <code>Boolean</code>.
	 * Attributes previously set on the marker but not included in the given map
	 * are considered to be removals. Setting the given map to be <code>null</code>
	 * is equivalent to removing all marker attributes.
	 * <p>
	 * This method changes resources; these changes will be reported
	 * in a subsequent resource change event, including an indication 
	 * that this marker has been modified.
	 * </p>
	 *
	 * @param attributes a map of attribute names to attribute values 
	 *		(key type : <code>String</code> value type : <code>String</code>, 
	 *		<code>Integer</code>, or <code>Boolean</code>) or <code>null</code>
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> The marker does not exist.</li>
	 * </ul>
	 */
	protected void setAttributes(Map attributes) throws CoreException {
		fMarker.setAttributes(attributes);
	}

}

