package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.*;
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.internal.ViewPane;
import java.util.*;

/**
 * An editor container manages the services for an editor.
 */
public class ViewSite extends PartSite
	implements IViewSite
{
/**
 * ViewSite constructor comment.
 */
public ViewSite(IViewPart view, WorkbenchPage persp, IViewDescriptor desc,IMemento mem) {
	super(view, persp,mem);
	setConfigurationElement(desc.getConfigurationElement());
}
/**
 * Returns the view
 */
public IViewPart getViewPart() {
	return (IViewPart)getPart();
}
}
