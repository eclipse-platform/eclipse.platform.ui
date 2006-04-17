/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.views;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.ICheatSheetResource;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader;
import org.eclipse.ui.internal.provisional.cheatsheets.TaskExplorer;
import org.osgi.framework.Bundle;

public class TaskExplorerManager {
private static TaskExplorerManager instance;

    private Map images;
	
	private TaskExplorerManager() {
	
	}
	
	public static TaskExplorerManager getInstance() {
		if (instance == null) {
			instance = new TaskExplorerManager();
		}
		return instance;
	}
	
	public TaskExplorer getExplorer(String explorerKind) {
		CheatSheetRegistryReader.TaskExplorerNode explorerInfo =
			CheatSheetRegistryReader.getInstance().findTaskExplorer(explorerKind);
		if (explorerInfo != null) {
			TaskExplorer explorerInstance = null;
			Class extClass = null;
			String className = explorerInfo.getClassName();
			try {
				Bundle bundle = Platform.getBundle(explorerInfo.getPluginId());
				extClass = bundle.loadClass(className);
			} catch (Exception e) {
				String message = NLS.bind(Messages.ERROR_LOADING_CLASS, (new Object[] {className}));
				Status status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);
				CheatSheetPlugin.getPlugin().getLog().log(status);
			}
			try {
				if (extClass != null) {
					Constructor c = extClass.getConstructor(new Class[0]);
					Object[] parameters = new Object[0];
					explorerInstance = (TaskExplorer) c.newInstance(parameters);
				}
			} catch (Exception e) {
				String message = NLS.bind(Messages.ERROR_CREATING_CLASS, (new Object[] {className}));
				IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);
				CheatSheetPlugin.getPlugin().getLog().log(status);
			}
			
			return explorerInstance;
		}

		return null;
	}

	private ImageDescriptor getImageDescriptor(String explorerKind) {
		CheatSheetRegistryReader.TaskExplorerNode explorerInfo =
			CheatSheetRegistryReader.getInstance().findTaskExplorer(explorerKind);
		if (explorerInfo == null) {
			return null;
		}
	    String iconPath = explorerInfo.getIconPath();
		if (iconPath == null) {
			return null;
		}
		Bundle bundle = Platform.getBundle(explorerInfo.getPluginId());
		URL url = FileLocator.find(bundle, new Path(iconPath), null);
		try {
			url = FileLocator.resolve(url);
			return ImageDescriptor.createFromURL(url);
		} catch (IOException e) {
			return null;
		}		
	}
	
	private Map getImages() {
		if (images == null) {
			initImages();
		}
		return images;
	}
	
	
	private void initImages() {
		if (images == null) {
			images = new HashMap();
			String[] ids = CheatSheetRegistryReader.getInstance().getExplorerIds();
			for (int i = 0; i < ids.length; i++) {
				ImageDescriptor descriptor = getImageDescriptor(ids[i]);
				if (descriptor != null) {
					images.put(ids[i], descriptor.createImage());
				}
			}
		}	
	}

	public String getName(String explorerKind) {
		CheatSheetRegistryReader.TaskExplorerNode explorerInfo =
			CheatSheetRegistryReader.getInstance().findTaskExplorer(explorerKind);
		if (explorerInfo != null) {
			return explorerInfo.getName();
		}
		return null;
	}

	public Image getImage(String id) {
		return (Image)getImages().get(id);
	}

}
