/*
 * Created on Jan 20, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms;
import java.util.Hashtable;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.*;
import org.eclipse.ui.forms.widgets.*;
/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public abstract class MasterDetailsPage extends FormPage {
	protected DetailsPart detailsPart;
	protected SashForm sashForm;
	private Hashtable actions;
	
	public MasterDetailsPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	protected void createFormContent(ManagedForm managedForm) {
		final Form form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		form.getBody().setLayout(layout);
		sashForm = new SashForm(form.getBody(), SWT.NULL);
		toolkit.adapt(sashForm, false, false);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		createMasterPart(managedForm, sashForm);
		createDetailsPart(managedForm, sashForm);
		createToolBarActions(managedForm);
	}

	protected abstract void createMasterPart(ManagedForm managedForm, Composite parent);
	protected abstract void registerPages(DetailsPart detailsPart);
	protected abstract void createToolBarActions(ManagedForm managedForm);

	private void createDetailsPart(final ManagedForm mform, Composite parent) {
		detailsPart = new DetailsPart(mform, parent, SWT.NULL);
		mform.addPart(detailsPart);
		registerPages(detailsPart);
	}
}