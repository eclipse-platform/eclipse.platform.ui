package org.eclipse.ui.forms;

import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.*;

/**
 * Managed form wraps a form widget and adds life cycle methods for form parts.
 * A form part is a portion of the form that participates in form life cycle
 * events.
 * <p>
 * There is requirement for 1/1 mapping between widgets and form parts. A
 * widget like Section can be a part by itself, but a number of widgets can
 * join around one form part.
 * 
 * @since 3.0
 */
public class ManagedForm implements IManagedForm {
	private ScrolledForm form;
	private FormToolkit toolkit;
	private boolean ownsToolkit;
	private Vector parts = new Vector();

	/**
	 * Creates a managed form in the provided parent. Form toolkit and widget
	 * will be created and owned by this object.
	 * 
	 * @param parent
	 *            the parent widget
	 */

	public ManagedForm(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		ownsToolkit = true;
		form = toolkit.createForm(parent);
	}
	/**
	 * Creates a managed form that will use the provided toolkit and
	 * 
	 * @param toolkit
	 * @param form
	 */
	public ManagedForm(FormToolkit toolkit, ScrolledForm form) {
		this.form = form;
		this.toolkit = toolkit;
	}

	/**
	 * Add a part to be managed by this form.
	 * 
	 * @param part
	 *            part to add
	 */
	public void addPart(IFormPart part) {
		parts.add(part);
	}
	/**
	 * Remove the part from this form.
	 * 
	 * @param part
	 *            part to remove
	 */
	public void removePart(IFormPart part) {
		parts.remove(part);
	}

	/**
	 * Returns all the parts current managed by this form.
	 */
	public IFormPart[] getParts() {
		return (IFormPart[]) parts.toArray(new IFormPart[parts.size()]);
	}
	/**
	 * Returns the toolkit used by this form.
	 * 
	 * @return the toolkit
	 */
	public FormToolkit getToolkit() {
		return toolkit;
	}

	/**
	 * Returns the form widget managed by this form.
	 * 
	 * @return the form widget
	 */
	public ScrolledForm getForm() {
		return form;
	}

	/**
	 * A part can use this method to notify other parts that implement
	 * IPartSelectionListener about selection changes.
	 * 
	 * @param part
	 *            the part that broadcasts the selection
	 * @param selection
	 *            the selection in the part
	 * @see IPartSelectionListener
	 */
	public void fireSelectionChanged(IFormPart part, ISelection selection) {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart cpart = (IFormPart) parts.get(i);
			if (part.equals(cpart))
				continue;
			if (cpart instanceof IPartSelectionListener) {
				((IPartSelectionListener) cpart).selectionChanged(
					part,
					selection);
			}
		}
	}

	/**
	 * Initializes all the parts in this form.
	 */
	public void initialize() {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			part.initialize(this);
		}
	}

	/**
	 * Disposes all the parts in this form.
	 */
	public void dispose() {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			part.dispose();
		}
		if (ownsToolkit) {
			toolkit.dispose();
		}
	}

	/**
	 * Refreshes the form by forcing all the parts to reload the content from
	 * the model.
	 */
	public void refresh() {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			part.refresh();
		}
		form.reflow(true);
	}
	
	/**
	 * Refreshes the form by forcing all the parts to reload the content from
	 * the model.
	 */
	public void commit(boolean onSave) {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			part.commit(onSave);
		}
	}

	/**
	 * Sets the form input. Managed parts could opt to react to it by selecting
	 * and/or revealing the object if they contain it.
	 * 
	 * @param input
	 *            the input object
	 */
	public void setInput(Object input) {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			part.setFormInput(input);
		}
	}
	/**
	 * Transfers the focus to the first form part.
	 */
	public void setFocus() {
		if (parts.size() > 0) {
			IFormPart part = (IFormPart) parts.get(0);
			part.setFocus();
		}
	}

	public boolean isDirty() {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			if (part.isDirty())
				return true;
		}
		return false;
	}
}