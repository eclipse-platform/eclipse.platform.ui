/*
 * Created on Oct 22, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.internal.forms;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IPersistableElement;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WizardEditorInput implements IWizardEditorInput {
	private IWizard wizard;
	private boolean formWizard;
	
	public WizardEditorInput(IWizard wizard, boolean formWizard) {
		this.wizard = wizard;
		this.formWizard = formWizard;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.IWizardEditorInput#getWizard()
	 */
	public IWizard getWizard() {
		return wizard;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.IWizardEditorInput#isFormWizard()
	 */
	public boolean isFormWizard() {
		return formWizard;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return wizard.getWindowTitle();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWizard.class))
			return wizard;
		return null;
	}
}
