/*
 * Created on Oct 22, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.internal.forms;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IEditorInput;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IWizardEditorInput extends IEditorInput {
	IWizard getWizard();
	boolean isFormWizard();
}
