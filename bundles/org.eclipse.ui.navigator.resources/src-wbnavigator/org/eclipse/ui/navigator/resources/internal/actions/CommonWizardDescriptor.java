/*
 * Created on Jan 14, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.navigator.resources.internal.actions;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.ActionExpression;

/**
 * @author Admin
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class CommonWizardDescriptor {

	public static final String ATT_WIZARD_ID = "wizardId"; //$NON-NLS-1$
	public static final String ATT_TYPE = "type"; //$NON-NLS-1$	
	private static final String CHILD_ENABLEMENT = "enablement"; //$NON-NLS-1$
	private static final String EMF_ENABLEMENT = "emfEnablement"; //$NON-NLS-1$
	// private EMFExpression emfEnablement;

	private String wizardId;
	private String type;

	private ActionExpression enablement;
	private IConfigurationElement configElement;


	/**
	 * @param actionElement
	 * @param targetType
	 */
	public CommonWizardDescriptor(IConfigurationElement aConfigElement) throws WorkbenchException {
		super();
		configElement = aConfigElement;
		init();
	}

	/*public boolean isEnabledFor(IStructuredSelection aStructuredSelection) {
		return (enablement != null && enablement.isEnabledFor(aStructuredSelection)) || (emfEnablement != null && emfEnablement.isEnabledFor(aStructuredSelection));
	}

	public boolean isEnabledFor(Object anElement) {
		return (enablement != null && enablement.isEnabledFor(anElement)) || (emfEnablement != null && emfEnablement.isEnabledFor(anElement));
	}*/
	
	public boolean isEnabledFor(IStructuredSelection aStructuredSelection) {
		return (enablement != null && enablement.isEnabledFor(aStructuredSelection));
	}

	public boolean isEnabledFor(Object anElement) {
		return (enablement != null && enablement.isEnabledFor(anElement));
	}



	void init() throws WorkbenchException {
		wizardId = configElement.getAttribute(ATT_WIZARD_ID);
		type = configElement.getAttribute(ATT_TYPE);

		if (wizardId == null || wizardId.length() == 0) {
			throw new WorkbenchException("Missing attribute: " + //$NON-NLS-1$
						ATT_WIZARD_ID + " in common wizard extension: " + //$NON-NLS-1$
						configElement.getDeclaringExtension().getUniqueIdentifier());
		}

		if (type == null || type.length() == 0) {
			throw new WorkbenchException("Missing attribute: " + //$NON-NLS-1$
						ATT_TYPE + " in common wizard extension: " + //$NON-NLS-1$
						configElement.getDeclaringExtension().getUniqueIdentifier());
		}

		IConfigurationElement[] children = configElement.getChildren(CHILD_ENABLEMENT);
		if (children.length == 1) {
			enablement = new ActionExpression(children[0]);
		} else if (children.length > 1) {
			throw new WorkbenchException("More than one element: " + //$NON-NLS-1$
						CHILD_ENABLEMENT + " in common wizard extension: " + //$NON-NLS-1$
						configElement.getDeclaringExtension().getUniqueIdentifier());
		}
		
	/*	children = configElement.getChildren(EMF_ENABLEMENT);
		if (children.length == 1) {
			emfEnablement = new EMFExpression(children[0]);
		} else if (children.length > 1) {
			throw new WorkbenchException("More than one element: " + //$NON-NLS-1$
						EMF_ENABLEMENT + " in common wizard extension: " + //$NON-NLS-1$
						configElement.getDeclaringExtension().getUniqueIdentifier());
		}*/
	}

	/**
	 * 
	 * @return Returns the common wizard wizardId
	 */
	public String getWizardId() {
		return wizardId;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

}