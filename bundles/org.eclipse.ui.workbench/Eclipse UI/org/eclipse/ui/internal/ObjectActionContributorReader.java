package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * This reader loads the popup menu manager with all the
 * popup menu contributors found in the workbench registry.
 */
public class ObjectActionContributorReader extends RegistryReader {

	public final static	String TAG_OBJECT_CONTRIBUTION = "objectContribution";//$NON-NLS-1$
	private final static	String ATT_OBJECTCLASS = "objectClass";//$NON-NLS-1$
	private ObjectActionContributorManager manager;
/**
 * Creates popup menu contributor from this element.
 */
protected void processObjectContribution(IConfigurationElement element) {
	String objectClassName = element.getAttribute(ATT_OBJECTCLASS);
	if (objectClassName == null) {
		logMissingAttribute(element, ATT_OBJECTCLASS);
		return;
	}
	
	IObjectContributor contributor = new ObjectActionContributor(element);
	manager.registerContributor(contributor, objectClassName);
}
/**
 * Implements abstract method to handle configuration elements. 
 */
protected boolean readElement(IConfigurationElement element) {
	String tagName = element.getName();
	if (tagName.equals(TAG_OBJECT_CONTRIBUTION)) {
		processObjectContribution(element);
		return true;
	}
	if (tagName.equals(ViewerActionBuilder.TAG_CONTRIBUTION_TYPE)) {
		return true;
	}

	return false;
}
/**
 * Reads the registry and registers popup menu contributors
 * found there.
 */
public void readPopupContributors(ObjectActionContributorManager mng) {
	manager = mng;
	IPluginRegistry registry = Platform.getPluginRegistry();
	readRegistry(registry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_POPUP_MENU);
}
}
