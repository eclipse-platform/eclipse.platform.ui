package org.eclipse.debug.ui;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Handles properties for instances of IBreakpoint
 */
public class BreakpointPropertySource implements IPropertySource {

	protected IBreakpoint fBreakpoint;	
	
	/**
	 * The property descriptors that are common to all IBreakpoints
	 */
	private static IPropertyDescriptor[] fBaseDescriptors;

	// Property Values
	protected static final String P_ID_ENABLE = "enable";
	
	private static final String P_ENABLE = "enabled";
	
	protected static final String P_VALUE_TRUE_LABEL = "true"; 
	protected static final String P_VALUE_FALSE_LABEL = "false";

	protected static final Integer P_VALUE_TRUE = new Integer(0);
	protected static final Integer P_VALUE_FALSE = new Integer(1);
	
	protected static String[] BOOLEAN_LABEL_ARRAY = new String[] {P_VALUE_TRUE_LABEL, P_VALUE_FALSE_LABEL};

	public static class BooleanLabelProvider extends LabelProvider {
		public String getText(Object element) {
			String[] values = new String[] {P_VALUE_TRUE_LABEL, P_VALUE_FALSE_LABEL};
			return values[((Integer)element).intValue()];
		}
	}

	static {
		fBaseDescriptors = new IPropertyDescriptor[1];
		PropertyDescriptor propertyDescriptor;
				
		propertyDescriptor = new ComboBoxPropertyDescriptor(P_ID_ENABLE, P_ENABLE, BOOLEAN_LABEL_ARRAY);
		propertyDescriptor.setLabelProvider(new BooleanLabelProvider());
		fBaseDescriptors[0] = propertyDescriptor;	
	}

	/**
	 * @see IPropertySource#getEditableValue()
	 */
	public Object getEditableValue() {
		return this;
	}

	/**
	 * @see IPropertySource#getPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return fBaseDescriptors;
	}

	/**
	 * @see IPropertySource#getPropertyValue(Object)
	 */
	public Object getPropertyValue(Object id) {
		if (id.equals(P_ID_ENABLE)) {
			try {
				return fBreakpoint.isEnabled() ? P_VALUE_TRUE : P_VALUE_FALSE;
			} catch (CoreException ce) {
				return null;
			}
		}
		return null;
	}

	/**
	 * @see IPropertySource#isPropertySet(Object)
	 */
	public boolean isPropertySet(Object id) {
		return false;
	}

	/**
	 * @see IPropertySource#resetPropertyValue(Object)
	 */
	public void resetPropertyValue(Object id) {
	}

	/**
	 * @see IPropertySource#setPropertyValue(Object, Object)
	 */
	public void setPropertyValue(Object id, Object value) {
		// To avoid requiring an extension to IPropertySource that includes API
		// to set the breakpoint, we use this method in a somewhat non-inutuitive way.
		// Note we can't set the breakpoint through a constructor since this object is
		// created as an executable extension, which requires a no-arg constructor.
		if (id.equals("breakpoint")) {
			fBreakpoint = (IBreakpoint) value;
		}
		else if (id.equals(P_ID_ENABLE)) {
			try {
				fBreakpoint.setEnabled(((Integer)value).equals(P_VALUE_TRUE) ? true : false);
			} catch (CoreException ce) {
			}		
		}
	}
	
	/**
	 * Convenience method to get the model presentation text for a debug object
	 */
	protected String getModelPresentationText(Object obj) {
		return DebugUIPlugin.getModelPresentation().getText(obj);
	}
	
}

