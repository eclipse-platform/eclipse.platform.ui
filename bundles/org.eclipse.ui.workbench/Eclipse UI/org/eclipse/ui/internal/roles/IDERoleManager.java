/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.roles;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceNode;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.registry.ICategory;
import org.eclipse.ui.internal.registry.IViewDescriptor;
import org.eclipse.ui.internal.registry.IViewRegistry;
import org.eclipse.ui.internal.registry.NewWizardsRegistryReader;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;

/**
 * The IDERoleManager is the class that implements the IDE specific behaviour
 * of the RoleManager. This is experimental API and will be subject to
 * refactoring at any time.
 */
public class IDERoleManager extends RoleManager {

	private IResourceChangeListener listener;

	/**
	 * Create a new instance of the receiver. */
	public IDERoleManager() {
		super();
	}

	/*
	 * (non-Javadoc) 
	 * @see org.eclipse.ui.internal.roles.RoleManager#connectToPlatform()
	 */
	protected void connectToPlatform() {

		listener = getChangeListener();
		WorkbenchPlugin.getPluginWorkspace().addResourceChangeListener(listener);
		createPreferenceMappings();
        createNewWizardMappings();
        createPerspectiveMappings();
        createViewMappings();
	}

    /**
	 * Get a change listener for listening to resource changes.
	 * 
	 * @return
	 */
	private IResourceChangeListener getChangeListener() {
		return new IResourceChangeListener() {
			/*
			 * (non-Javadoc) @see
			 * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
			 */
			public void resourceChanged(IResourceChangeEvent event) {

				IResourceDelta mainDelta = event.getDelta();

				if (mainDelta == null)
					return;
				//Has the root changed?
				if (mainDelta.getKind() == IResourceDelta.CHANGED
					&& mainDelta.getResource().getType() == IResource.ROOT) {

					try {
						IResourceDelta[] children = mainDelta.getAffectedChildren();
						for (int i = 0; i < children.length; i++) {
							IResourceDelta delta = children[i];
							if (delta.getResource().getType() == IResource.PROJECT) {
								IProject project = (IProject) delta.getResource();
								String[] ids = project.getDescription().getNatureIds();
								for (int j = 0; j < ids.length; j++) {
									enableActivities(ids[j]);
								}
							}
						}

					} catch (CoreException exception) {
						//Do nothing if there is a CoreException
					}
				}

			}
		};
	}

	/*
	 * (non-Javadoc) @see
	 * org.eclipse.ui.internal.roles.RoleManager#shutdownManager()
	 */
	public void shutdownManager() {
		super.shutdownManager();
		if (listener != null) {
			WorkbenchPlugin.getPluginWorkspace().removeResourceChangeListener(listener);
		}
	}

    /**
     * Create the mappings for the new wizard object activity manager.
     * Objects of interest in this manager are Strings (wizard IDs).
     */
    private void createNewWizardMappings() {
        NewWizardsRegistryReader reader = new NewWizardsRegistryReader(false);
        WizardCollectionElement wizardCollection = (WizardCollectionElement)reader.getWizards();
        ObjectActivityManager manager = ObjectActivityManager.getManager(IWorkbenchConstants.PL_NEW, true);
        Object [] wizards = flattenWizards(wizardCollection);
        for (int i = 0; i < wizards.length; i++) {
            WorkbenchWizardElement element = (WorkbenchWizardElement)wizards[i];
            manager.addObject(element.getConfigurationElement().getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier(), element.getID(), element.getID());
            
        }
        RoleManager.getInstance().applyPatternBindings(manager);        
    }
    
    /**
     * Create the mappings for the perspective object activity manager.  
     * Objects of interest in this manager are Strings (perspective IDs).
     */
    private void createPerspectiveMappings() {
        IPerspectiveRegistry registry = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
        IPerspectiveDescriptor [] descriptors = registry.getPerspectives();
        ObjectActivityManager manager = ObjectActivityManager.getManager(IWorkbenchConstants.PL_PERSPECTIVES, true);
        for (int i = 0; i < descriptors.length; i++) {
            String localId = descriptors[i].getId();
            if (!(descriptors[i] instanceof PerspectiveDescriptor)) {
                // this situation doesn't currently occur.  
                // All of our IPerspectiveDescriptors are PerspectiveDescriptors
                // give it a plugin ID of * to represent internal "plugins" (custom perspectives)
                // These objects will always be "active".
                manager.addObject("*", localId, localId); //$NON-NLS-1$
                continue;
            }
            IConfigurationElement element = ((PerspectiveDescriptor)descriptors[i]).getConfigElement();
            if (element == null) {
                // Custom perspective
                // Give it a plugin ID of * to represent internal "plugins" (custom perspectives)
                // These objects will always be "active".                
                manager.addObject("*", localId, localId); //$NON-NLS-1$
                continue;
            }
            String pluginId = element.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier();
            manager.addObject(pluginId, localId, localId);              
        }
        RoleManager.getInstance().applyPatternBindings(manager);        
    }    

	/**
	 * Create the mappings for the preference page object activity manager.
     * Objects of interest in this manager are WorkbenchPreferenceNodes. 
	 */
	private void createPreferenceMappings() {		
		PreferenceManager preferenceManager = WorkbenchPlugin.getDefault().getPreferenceManager();
		//add all WorkbenchPreferenceNodes to the manager
		ObjectActivityManager objectManager =
			ObjectActivityManager.getManager(IWorkbenchConstants.PL_PREFERENCES, true);
		for (Iterator i = preferenceManager.getElements(PreferenceManager.PRE_ORDER).iterator(); i.hasNext();) {
			IPreferenceNode node = (IPreferenceNode) i.next();
			if (node instanceof WorkbenchPreferenceNode) {
				WorkbenchPreferenceNode workbenchNode = ((WorkbenchPreferenceNode) node);
				objectManager.addObject(workbenchNode.getPluginId(), workbenchNode.getExtensionLocalId(), node);
			}
		}
		// and then apply the default bindings
		RoleManager.getInstance().applyPatternBindings(objectManager);
	}
   
    /**
     * Create the mappings for the perspective object activity manager.  
     * Objects of interest in this manager are Strings (view IDs as well as view
     * category IDs (in the form "{ID}*").
     */
    private void createViewMappings() {
        IViewRegistry viewRegistry = WorkbenchPlugin.getDefault().getViewRegistry();
        ObjectActivityManager objectManager =
            ObjectActivityManager.getManager(IWorkbenchConstants.PL_VIEWS, true);        
        
        IViewDescriptor [] viewDescriptors = viewRegistry.getViews();
        for (int i = 0; i < viewDescriptors.length; i++) {
            IConfigurationElement element = viewDescriptors[i].getConfigurationElement();
            objectManager.addObject(element.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier(), viewDescriptors[i].getId(), viewDescriptors[i].getId());
        }
        
        // this is a temporary hack until we decide whether categories warrent their own
        // object manager.  
        ICategory[] categories = viewRegistry.getCategories();
        for (int i = 0; i < categories.length; i++) {
            IConfigurationElement element = (IConfigurationElement) categories[i].getAdapter(IConfigurationElement.class);
            if (element != null) {
                String categoryId = createViewCategoryIdKey(categories[i].getId());
                objectManager.addObject(element.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier(), categoryId, categoryId);
            }
        }
        
        // and then apply the default bindings
        RoleManager.getInstance().applyPatternBindings(objectManager);        
    }
    
    /**
     * Temporary utility method to create a key/object value from a given view 
     * category ID.
     * 
     * @param id
     * @return the value of id + '*'
     * @since 3.0
     */
    public static String createViewCategoryIdKey(String id) {
        return id + '*';
    }
       
    /**
     * Take the tree WizardCollecitonElement structure and flatten it into a list
     * of WorkbenchWizardElements. 
     * 
     * @param wizardCollection
     * @return 
     * @since 3.0
     */
    private Object[] flattenWizards(WizardCollectionElement wizardCollection) {
        return flattenWizards(wizardCollection, new HashSet());
    }

    /**
     * @param wizardCollection
     * @param list
     * @return
     * @since 3.0
     */
    private Object[] flattenWizards(WizardCollectionElement wizardCollection, Collection wizards) {
        wizards.addAll(Arrays.asList(wizardCollection.getWizards()));
        for (int i = 0; i < wizardCollection.getChildren().length; i++) {
            WizardCollectionElement child = (WizardCollectionElement) wizardCollection.getChildren()[i];
            wizards.addAll(Arrays.asList(flattenWizards(child, wizards)));            
        }
        return wizards.toArray();
    }
}
