package org.eclipse.ui.views.navigator;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.jface.viewers.*;
import java.util.*;

/**
 * Implements the go to resource action. Opens a dialog and set
 * the navigator selection with the resource selected by
 * the user.
 */
public class GotoResourceAction extends ResourceNavigatorAction {
/**
 * Creates a new instance of the class.
 */
/*package */ GotoResourceAction(ResourceNavigator navigator, String label) {
	super(navigator, label);
}
/**
 * Collect all resources in the workbench and add them to the <code>resources</code>
 * list.
 */
private void collectAllResources(IContainer container,ArrayList resources) {
	try {
		IResource members[] = container.members();
		for (int i = 0; i < members.length; i++){
			IResource r = members[i];
			resources.add(r);
			if(r.getType() != IResource.FILE)
				collectAllResources((IContainer)r,resources);
		}
	} catch (CoreException e) {
		//TBD: What to do here.
	}
}
/**
 * Collect all resources in the workbench open a dialog asking
 * the user to select a resource and change the selection in
 * the navigator.
 */
public void run() {
	IContainer cont = (IContainer)getResourceViewer().getInput();
	ArrayList resources = new ArrayList();
	collectAllResources(cont,resources);
	IResource resourcesArray[] = new IResource[resources.size()];
	resources.toArray(resourcesArray);
	GotoResourceDialog dialog = new GotoResourceDialog(getShell(),resourcesArray);
 	dialog.open();
	IResource selection = dialog.getSelection();
	if(selection == null)
		return;
	getResourceViewer().setSelection(new StructuredSelection(selection),true);
}
}
