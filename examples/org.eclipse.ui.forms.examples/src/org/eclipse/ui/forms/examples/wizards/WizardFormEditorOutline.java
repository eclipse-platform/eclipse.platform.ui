/*
 * Created on Oct 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.wizards;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.forms.WizardFormEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


/**
 * @author dejan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class WizardFormEditorOutline implements IContentOutlinePage {
	private WizardFormEditor editor;
	private ContextHelpPart contextHelpPart;

	public WizardFormEditorOutline(WizardFormEditor editor) {
		this.editor = editor;
		contextHelpPart = new ContextHelpPart();
	}

	public void createControl(Composite parent) {
		FormToolkit toolkit = editor.getToolkit();
		contextHelpPart.createControl(parent, toolkit);
		update();
	}

	public Control getControl() {
		return contextHelpPart.getControl();
	}

	public void dispose() {
		contextHelpPart.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.intro.impl.parts.IStandbyContentPart#setFocus()
	 */
	public void setFocus() {
		contextHelpPart.setFocus();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void setActionBars(IActionBars actionBars) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
	}
	
	public void update() {
		IWizardPage page = editor.getCurrentPage();
		contextHelpPart.update(page!=null?page.getControl():null);
	}
}
