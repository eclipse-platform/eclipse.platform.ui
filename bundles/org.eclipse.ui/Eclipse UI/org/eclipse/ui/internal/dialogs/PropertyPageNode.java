package org.eclipse.ui.internal.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.internal.misc.UIHackFinder;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Image;

/**
 * Property page node allows us to achive presence in the property page dialog
 * without loading the page itself, thus loading the contributing plugin.
 * Only when the user selects the page will it be loaded.
 */
public class PropertyPageNode extends PreferenceNode {
	private RegistryPageContributor contributor;
	private IWorkbenchPropertyPage page;
	private Image icon;
	private IAdaptable element;
/**
 * PropertyPageNode constructor.
 */
public PropertyPageNode(RegistryPageContributor contributor, IAdaptable element) {
	super(contributor.getPageId());
	this.contributor = contributor;
	this.element = element;
}
/**
 * Creates the preference page this node stands for. If the page is null,
 * it will be created by loading the class. If loading fails,
 * empty filler page will be created instead.
 */
public void createPage() {
	try {
		page = contributor.createPage(element);
	} catch (CoreException e) {
		UIHackFinder.fixPR(); //need to use a null shell - 1FTVRKN: JFUIF:ALL - IPreferencePage getPreferencePage() does not handle errors
		ErrorDialog.openError((Shell) null, 
			"Property Page Creation Problems", 
			"Unable to create the selected property page.", 
			e.getStatus());
		page = new EmptyPropertyPage();
	}
	setPage(page);
}
/** (non-Javadoc)
 * Method declared on IPreferenceNode.
 */
public void disposeResources() {
	page = null;
	if (icon != null) {
		icon.dispose();
		icon = null;
	}
}
/**
 * Returns page icon, if defined.
 */
public Image getLabelImage() {
	if (icon==null) {
		ImageDescriptor desc = contributor.getPageIcon();
		if (desc != null) {
			icon = desc.createImage();
		}
	}
	return icon;
}
/**
 * Returns page label as defined in the registry.
 */
public String getLabelText() {
	return contributor.getPageName();	
}
}
