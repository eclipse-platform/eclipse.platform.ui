package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * A capability is the user interface aspect of a project's nature. There is
 * a 1-1 relationship between a capability and a nature. It is possible for
 * a nature to not have a capability, but the opposite is not true - that is,
 * a capability must represent a nature.
 * <p>
 * A capability can take control of the user interface of other capabilities. It
 * is then responsible for collecting the necessary information and adding the
 * natures represented by these capabilites.
 * </p>
 */
public class Capability {
	private static final String ATT_ID = "id";
	private static final String ATT_NAME = "name";
	private static final String ATT_ICON = "icon";
	private static final String ATT_NATURE_ID = "natureId";
	private static final String ATT_CATEGORY = "category";
	private static final String ATT_INSTALL_WIZARD = "installWizard";
	private static final String ATT_DESCRIPTION = "description";
	
	private String id;
	private String natureId;
	private String name;
	private ImageDescriptor icon;
	private IConfigurationElement element;
	private ArrayList handleUIs;
	
	/**
	 * Creates an instance of <code>Capability</code> using the
	 * information provided by the configuration element.
	 * 
	 * @param configElement the <code>IConfigurationElement<code> containing
	 * 		the attributes
	 * @param reader the <code>CapabilityRegistryReader<code> used to log missing attributes
	 * @throws a <code>WorkbenchException</code> if the ID or label is <code>null</code
	 */
	public Capability(IConfigurationElement configElement, CapabilityRegistryReader reader)
		throws WorkbenchException
	{
		boolean missingAttribute = false;
		String attr_id = element.getAttribute(ATT_ID);
		String attr_name = element.getAttribute(ATT_NAME);
		String attr_nature = element.getAttribute(ATT_NATURE_ID);
			
		if (attr_id == null) {
			reader.logMissingAttribute(element, ATT_ID);
			missingAttribute = true;
		}
		if (attr_name == null) {
			reader.logMissingAttribute(element, ATT_NAME);
			missingAttribute = true;
		}
		if (attr_nature == null) {
			reader.logMissingAttribute(element, ATT_NATURE_ID);
			missingAttribute = true;
		}
		if (element.getAttribute(ATT_INSTALL_WIZARD) == null) {
			reader.logMissingAttribute(element, ATT_INSTALL_WIZARD);
			missingAttribute = true;
		}
		
		if (missingAttribute)
			throw new WorkbenchException("Missing required category attribute"); //$NON-NLS-1$

		id = attr_id;
		name = attr_name;
		natureId = attr_nature;
		element = configElement;
	}
	
	/**
	 * Adds the id of a capability for which this capability handles
	 * the user interface.
	 */
	public void addHandleUI(String capabilityId) {
		if (handleUIs == null)
			handleUIs = new ArrayList(4);
		handleUIs.add(capabilityId);	
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public ImageDescriptor getIconDescriptor() {
		if (icon == null) {
			IExtension extension = element.getDeclaringExtension();
			String location = element.getAttribute(ATT_ICON);
			icon = WorkbenchImages.getImageDescriptorFromExtension(extension, location);
		}
		return icon;
	}
	
	public String getNatureId() {
		return natureId;
	}
	
	public String getCategoryPath() {
		return element.getAttribute(ATT_CATEGORY);
	}
	
	public IWizard getInstallWizard() {
		try {
			return (IWizard)element.createExecutableExtension(ATT_INSTALL_WIZARD);
		} catch (CoreException e) {
			return null;
		}
	}
	
	public String getDescription() {
		return element.getAttribute(ATT_DESCRIPTION);
	}
	
	/**
	 * Returns a list of ids of other capabilities for which this 
	 * capability handles the user interface, or <code>null</code>
	 * if not applicable.
	 */
	public ArrayList getHandleUIs() {
		return handleUIs;	
	}
}
