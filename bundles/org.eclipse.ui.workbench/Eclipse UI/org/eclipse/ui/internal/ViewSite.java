package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.internal.registry.IViewDescriptor;

/**
 * A view container manages the services for a view.
 */
public class ViewSite extends PartSite
	implements IViewSite
{
/**
 * Creates a new ViewSite.
 */
public ViewSite(IViewPart view, WorkbenchPage page, IViewDescriptor desc) {
	super(view, page);
	setConfigurationElement(desc.getConfigurationElement());
}
/**
 * Returns the view.
 */
public IViewPart getViewPart() {
	return (IViewPart)getPart();
}
}
