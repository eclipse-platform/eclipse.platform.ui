package org.eclipse.ui.forms;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.forms.widgets.*;

/**
 * Managed form wraps a form widget and adds life cycle methods
 * for form parts. A form part is a portion of the form that
 * participates in form life cycle events. 
 * <p>There is no 1/1 mapping between widgets and form parts.
 * A widget like Section can be a part by itself, but a number
 * of widgets can gather around one form part.
 * 
 * @since 3.0
 */
public interface IManagedForm {
/**
 * Returns the toolkit used by this form.
 * @return the toolkit
 */
	public FormToolkit getToolkit();

/**
 * Returns the form widget managed by this form.
 * @return the form widget
 */
	public ScrolledForm getForm();

/**
 * Reflows the form as a result of the layout change.
 * @param changed if <code>true</code>, discard cached layout information
 */
	public void reflow(boolean changed);
/**
 * A part can use this method to notify other parts that
 * implement IPartSelectionListener about selection changes.
 * @param part the part that broadcasts the selection
 * @param selection the selection in the part
 */
	public void fireSelectionChanged(IFormPart part, ISelection selection);
	
/**
 * Returns all the parts currently managed by this form.
 * @return the managed parts
 */	
	IFormPart [] getParts();
	
/**
 * Sets the input of this page to the provided object.
 * @param input the new page input
 */
	void setInput(Object input);
/**
 * Returns the current page input.
 * @return page input object or <code>null</code> if not applicable.
 */
	Object getInput();
}