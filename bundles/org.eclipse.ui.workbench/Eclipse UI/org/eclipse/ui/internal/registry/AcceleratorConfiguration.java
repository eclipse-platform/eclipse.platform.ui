/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.registry;

public final class AcceleratorConfiguration {
	
	private String id;
	private String name;
	private String description;
	private String parentId;
	private String pluginId;
	
	AcceleratorConfiguration(String id, String name, String description, String parentId, String pluginId) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.parentId = parentId;
		this.pluginId = pluginId;
	}
	
	public String getId() {
		return id;	
	}
	
	public String getName() {
		return name;
	}		
	
	public String getDescription() {
		return description;	
	}
	
	public String getParentId() {
		return parentId;
	}
	
	public String getPluginId() {
		return pluginId;
	}
}
