package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.*;
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.internal.ViewPane;

/**
 * An editor container manages the services for an editor.
 */
public class ViewSite extends PartSite
	implements IViewSite
{
/**
 * ViewSite constructor comment.
 */
public ViewSite(IViewPart view, WorkbenchPage persp, IViewDescriptor desc) {
	super(view, persp);
	setConfigurationElement(desc.getConfigurationElement());
}
/**
 * Returns the view
 */
public IViewPart getViewPart() {
	return (IViewPart)getPart();
}
}
