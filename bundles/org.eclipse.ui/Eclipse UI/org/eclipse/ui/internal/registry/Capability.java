package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * This class captures the attributes of a project capability. Project capabilities
 * have a 1-1 relationship with project natures. A capability is the user-interface
 * aspect of a project nature.
 * <p>
 * Some capabilities take control of the user interface of other capabilities, so
 * each capability had a (possibly empty) list of ids of other capabilities for
 * which it controls the UI. For example, Capability A may want to control the user
 * interface of capability B if the project nature associated with A requires the
 * project nature associated with B. Often, the developer of A wants to limit the
 * information about B that the user is asked for.
 * </p>
 */
public class Capability {
	private String id;
	private String name;
	private String icon;
	private String natureId;
	private String categoryPath;
	private String installWizard;
	private String description;
	
	// A list of ids of other capabilities for which this capability
	// handles the user interface. May be empty.
	private List handleUIs;
	
	public Capability(String id, String name, String icon, String natureId,
			String categoryPath, String installWizard, String description) {
		this.id = id;
		this.name = name;
		this.icon = icon;
		this.natureId = natureId;
		this.categoryPath = categoryPath;
		this.installWizard = installWizard;
		this.description = description;
		this.handleUIs = new ArrayList();	
	}
	
	/**
	 * Adds the id of a capability for which this capability handles'
	 * the user interface.
	 */
	public void addHandleUI(String capabilityId) {
		handleUIs.add(capabilityId);	
	}
	
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getIcon() {
		return icon;
	}
	public String getNatureId() {
		return natureId;
	}
	public String getCategoryPath() {
		return categoryPath;
	}
	public String getInstallWizard() {
		return installWizard;
	}
	public String getDescription() {
		return description;
	}
	/**
	 * Returns a list of ids of other capabilities for which this 
	 * capability handles the user interface.
	 */
	public List getHandleUIs() {
		return handleUIs;	
	}
}
