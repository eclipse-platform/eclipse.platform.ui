package org.eclipse.ui.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.wizard.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.core.resources.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.internal.*;

/**
 * Standard project reference page for a wizard that creates a 
 * project resource.
 * <p>
 * This page may be used by clients as-is; it may be also be
 * subclassed to suit.
 * </p>
 * <p>
 * Example useage:
 * <pre>
 * referencePage = new WizardNewProjectReferencePage("basicReferenceProjectPage");
 * referencePage.setTitle("Project");
 * referencePage.setDescription("Select referenced projects.");
 * </pre>
 * </p>
 * @deprecated Multiple project type creation wizards are no longer recommended.
 * 		The workbench provides one wizard to the user to create a project resource.
 * 		Plug-ins should now use the org.eclipse.ui.capabilities extension point. See
 * 		also CreateProjectAction if the plug-in needs to launch the new project wizard.
 */
public class WizardNewProjectReferencePage extends WizardPage {
	// widgets
	private CheckboxTableViewer referenceProjectsViewer;

	private static final String REFERENCED_PROJECTS_TITLE = WorkbenchMessages.getString("WizardNewProjectReferences.title"); //$NON-NLS-1$
	private static final int PROJECT_LIST_MULTIPLIER = 15;
/**
 * Creates a new project reference wizard page.
 *
 * @param pageName the name of this page
 * 
 * @deprecated Multiple project type creation wizards are no longer recommended.
 * 		The workbench provides one wizard to the user to create a project resource.
 * 		Plug-ins should now use the org.eclipse.ui.capabilities extension point. See
 * 		also CreateProjectAction if the plug-in needs to launch the new project wizard.
 */
public WizardNewProjectReferencePage(String pageName) {
	super(pageName);
}
/** (non-Javadoc)
 * Method declared on IDialogPage.
 */
public void createControl(Composite parent) {
	Composite composite = new Composite(parent, SWT.NONE);
	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_PROJECT_REFERENCE_WIZARD_PAGE);
	
	Label referenceLabel = new Label(composite, SWT.NONE);
	referenceLabel.setText(REFERENCED_PROJECTS_TITLE);

	referenceProjectsViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;

	data.heightHint =
		getDefaultFontHeight(
			referenceProjectsViewer.getTable(),
			PROJECT_LIST_MULTIPLIER);
	referenceProjectsViewer.getTable().setLayoutData(data);
	referenceProjectsViewer.setLabelProvider(new WorkbenchLabelProvider());
	referenceProjectsViewer.setContentProvider(getContentProvider());
	referenceProjectsViewer.setInput(ResourcesPlugin.getWorkspace());

	setControl(composite);
}
/**
 * Returns a content provider for the reference project
 * viewer. It will return all projects in the workspace.
 *
 * @return the content provider
 */
protected IStructuredContentProvider getContentProvider() {
	return new WorkbenchContentProvider() {
		public Object[] getChildren(Object element) {
			if (!(element instanceof IWorkspace))
				return new Object[0];
			IProject[] projects = ((IWorkspace)element).getRoot().getProjects();
			return projects == null ? new Object[0] : projects;
		}
	};
}
/**
 * Get the defualt widget height for the supplied control.
 * @return int
 * @param control - the control being queried about fonts
 * @param lines - the number of lines to be shown on the table.
 */
private static int getDefaultFontHeight(Control control, int lines) {
	FontData[] viewerFontData = control.getFont().getFontData();
	int fontHeight = 10;

	//If we have no font data use our guess
	if (viewerFontData.length > 0)
		fontHeight = viewerFontData[0].getHeight();
	return lines * fontHeight;

}
/**
 * Returns the referenced projects selected by the user.
 *
 * @return the referenced projects
 */
public IProject[] getReferencedProjects() {
	Object[] elements = referenceProjectsViewer.getCheckedElements();
	IProject[] projects = new IProject[elements.length];
	System.arraycopy(elements, 0, projects, 0, elements.length);
	return projects;	
}
}
