package org.eclipse.ui.views.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.*;
import org.eclipse.help.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.IViewPart;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.widgets.Control;
import java.util.*;

/**
 * For determining the help context for a property sheet page.
 */ 
/* package */ class PropertySheetPageContextComputer implements IContextComputer {
	private PropertySheetViewer viewer;
	private ArrayList contextList;
	private Object context;
/**
 * Creates a new context computer for a property sheet page.
 *
 * @param page the property sheet page
 * @param helpContext a single help context id (type <code>String</code>) or
 *  help context object (type <code>IContext</code>)
 */
public PropertySheetPageContextComputer(PropertySheetViewer propertySheetViewer, Object helpContext) {
	Assert.isTrue(helpContext instanceof String || helpContext instanceof IContext);
	viewer = propertySheetViewer;
	context = helpContext;
}
/**
 * Add the contexts to the context list.
 *
 * @param object the contexts (<code>Object[]</code> or <code>IContextComputer</code>)
 * @param event the help event 
 */
private void addContexts(Object object, HelpEvent event) {
	Assert.isTrue(
		object instanceof Object[] || 
		object instanceof IContextComputer ||
		object instanceof String);
		
	if (object instanceof String) {
		contextList.add(object);
		return;
	}
 
 	Object[] contexts;
	if (object instanceof IContextComputer) 
		// get local contexts
		contexts = ((IContextComputer)object).getLocalContexts(event);
	else
		contexts = (Object[])object;

	// copy the contexts into our list	
	for (int i = 0; i < contexts.length; i++) 
		contextList.add(contexts[i]); 
}
/* (non-Javadoc)
 * Method declared on IContextComputer.
 */
public Object[] computeContexts(HelpEvent event) {
	contextList = new ArrayList();

	// Add the context for the selected item
	IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
	if (!selection.isEmpty()) {
		IPropertySheetEntry entry = (IPropertySheetEntry)selection.getFirstElement();
		Object helpContextIds = entry.getHelpContextIds();
		if (helpContextIds != null)
			addContexts(helpContextIds, event);
	}
		
	// Add the context for the page
	contextList.add(context);
	
	// Return the contexts
	return contextList.toArray();
}
/**
 * Returns the context set on this page.
 *
 * @return the context set on this page. (type <code>String</code>) or
 *  help context object (type <code>IContext</code>)
 */
public Object[] getLocalContexts(HelpEvent event) {
	return new Object[] {context};
}
}
