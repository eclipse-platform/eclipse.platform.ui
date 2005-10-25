/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.dialogs;

import java.util.*;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.mapping.*;
import org.eclipse.team.ui.mapping.*;

public class ResourceMappingHierarchyArea extends DialogArea {

    private String description;
    private TreeViewer viewer;
    private final CompositeContentProvider contentProvider;
    
    /*
     * TODO: There are some potential problems here
     *   - the input changed method probably should not be propagated to the
     *     sub-providers. Perhaps an additional method is needed (setViewer)?
     *   - this content provider has state that is dependent on what is
     *     displayed in the view. Should a refresh of the viewer clear this state?
     *     I don't think it needs to unless the input changes (which it never does
     *     after the first set).
     */
    private static class CompositeContentProvider implements IResourceMappingContentProvider, ILabelProvider {

        private final Map providers; // Map of ModelProvider -> NavigatorContentExtension
        private final Map providerMap = new HashMap();
        private final ILabelProvider defaultLabelProvider = new ResourceMappingLabelProvider();

        public CompositeContentProvider(Map providers) {
            this.providers = providers;
        }

        public Object getInput() {
        	if (providers.size() == 1) {
        		NavigatorContentExtension nce = getNavigatorContentExtension(this);
        		Object root = nce.getModelProvider();
				providerMap.put(root, nce);
				return root;
        	}
            return this;
        }

		public Object[] getChildren(Object parentElement) {
        	IResourceMappingContentProvider singleProvider = getSingleProvider();
        	if (singleProvider != null) {
        		return singleProvider.getChildren(parentElement);
        	}
            if (parentElement == this) {
                List result = new ArrayList();
               	for (Iterator iter = providers.values().iterator(); iter.hasNext();) {
               		NavigatorContentExtension extension = (NavigatorContentExtension) iter.next();
               		IResourceMappingContentProvider provider = extension.getContentProvider();
                    Object element = extension.getModelProvider();
                    providerMap.put(element, extension);
                    result.add(element);
                }
                return result.toArray(new Object[result.size()]);
            } else {
            	NavigatorContentExtension extension = getNavigatorContentExtension(parentElement);
                if (extension != null) {
                    Object[] elements = extension.getContentProvider().getChildren(parentElement);
                    for (int i = 0; i < elements.length; i++) {
                        Object element = elements[i];
                        providerMap.put(element, extension);
                    }
                    return elements;
                }
            }
            return new Object[0];
        }

        public Object getParent(Object element) {
        	IResourceMappingContentProvider singleProvider = getSingleProvider();
        	if (singleProvider != null) {
        		return singleProvider.getParent(element);
        	}
        	if (element == this)
        		return null;
        	NavigatorContentExtension nce = getNavigatorContentExtension(element);
        	if (element == nce.getModelProvider()) {
        		return this;
        	}
            return nce.getContentProvider().getParent(element);
        }

		private NavigatorContentExtension getNavigatorContentExtension(Object element) {
			if (providers.size() == 1) {
				return ((NavigatorContentExtension)providers.values().iterator().next());
			}
			return (NavigatorContentExtension)providerMap.get(element);
		}
		
		private IResourceMappingContentProvider getSingleProvider() {
			if (providers.size() == 1)
				return getNavigatorContentExtension(this).getContentProvider();
			return null;
		}
		
        private IResourceMappingContentProvider getProvider(Object element) {
			NavigatorContentExtension e = getNavigatorContentExtension(element);
			if (e == null)
				return null;
			return e.getContentProvider();
		}

        public boolean hasChildren(Object element) {
        	IResourceMappingContentProvider singleProvider = getSingleProvider();
        	if (singleProvider != null) {
        		return singleProvider.hasChildren(element);
        	}
        	if (element != this) {	
	        	IResourceMappingContentProvider provider = getProvider(element);
	        	if (provider != null)
	        		return provider.hasChildren(element);
        	}
        	return getChildren(element).length > 0;
        }

		public Object[] getElements(Object inputElement) {
        	IResourceMappingContentProvider singleProvider = getSingleProvider();
        	if (singleProvider != null) {
        		return singleProvider.getElements(inputElement);
        	}
        	if (inputElement != this) {	
	        	IResourceMappingContentProvider provider = getProvider(inputElement);
	        	if (provider != null)
	        		return provider.getElements(inputElement);
        	}
            return getChildren(inputElement);
        }

        public void dispose() {
        	providerMap.clear();
        	for (Iterator iter = providers.values().iterator(); iter.hasNext();) {
          		NavigatorContentExtension extension = (NavigatorContentExtension) iter.next();
           		extension.dispose();
            }
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        	providerMap.clear();
           	for (Iterator iter = providers.values().iterator(); iter.hasNext();) {
           		NavigatorContentExtension extension = (NavigatorContentExtension) iter.next();
				IResourceMappingContentProvider provider = extension.getContentProvider();
                provider.inputChanged(viewer, oldInput, newInput);
            }
        }

		private ILabelProvider getLabelProvider(Object o) {
			if (o != this) {
				NavigatorContentExtension e = getNavigatorContentExtension(o);
				if (e != null)
					return e.getLabelProvider();
				Object parent = getParent(o);
				if (parent != null)
					return getLabelProvider(parent);
			}
			return defaultLabelProvider;
		}

		public Image getImage(Object element) {
			return getLabelProvider(element).getImage(element);
		}

		public String getText(Object element) {
			return getLabelProvider(element).getText(element);
		}

		public void addListener(ILabelProviderListener listener) {
			defaultLabelProvider.addListener(listener);
			for (Iterator iter = providers.values().iterator(); iter.hasNext();) {
				NavigatorContentExtension extension = (NavigatorContentExtension) iter.next();
				ILabelProvider lp = extension.getLabelProvider();
				lp.addListener(listener);
			}
		}

		public boolean isLabelProperty(Object element, String property) {
			return getLabelProvider(element).isLabelProperty(element, property);
		}

		public void removeListener(ILabelProviderListener listener) {
			defaultLabelProvider.removeListener(listener);
			for (Iterator iter = providers.values().iterator(); iter.hasNext();) {
				NavigatorContentExtension extension = (NavigatorContentExtension) iter.next();
				ILabelProvider lp = extension.getLabelProvider();
				lp.removeListener(listener);
			}
		}
        
    }
    
    public static ResourceMappingHierarchyArea create(ITeamViewerContext context) {
    	ModelProvider[] providers = context.getModelProviders();
    	Map extensions = new HashMap();
    	for (int i = 0; i < providers.length; i++) {
			ModelProvider provider = providers[i];
			INavigatorContentExtensionFactory factory = getFactory(provider);
			if (factory == null) {
				try {
					ModelProvider resourceModelProvider = ModelProvider.getModelProviderDescriptor(ModelProvider.RESOURCE_MODEL_PROVIDER_ID).getModelProvider();
					if (!extensions.containsKey(resourceModelProvider)) {
						factory = getFactory(resourceModelProvider);
					}
				} catch (CoreException e) {
					TeamUIPlugin.log(e);
				}
			}
			if (factory != null) {
				NavigatorContentExtension extension = factory.createProvider(provider, context);
				extensions.put(provider, extension);
			}
		}
        CompositeContentProvider provider = new CompositeContentProvider(extensions);
        return new ResourceMappingHierarchyArea(provider);
    }
    
    private static INavigatorContentExtensionFactory getFactory(ModelProvider provider) {
		return (INavigatorContentExtensionFactory) provider.getAdapter(INavigatorContentExtensionFactory.class);
	}

	private ResourceMappingHierarchyArea(CompositeContentProvider contentProvider) {
        this.contentProvider = contentProvider;
    }
    
    public void createArea(Composite parent) {
        Composite composite = createComposite(parent, 1, true);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);
        
        if (description != null)
            createWrappingLabel(composite, description, 1);
        
        viewer = new TreeViewer(composite);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 100;
        data.widthHint = 300;
        viewer.getControl().setLayoutData(data);
        viewer.setContentProvider(getContentProvider());
        viewer.setLabelProvider(getContentProvider());
        viewer.setInput(getInput());
    }

    private Object getInput() {
        return getContentProvider().getInput();
    }

    private CompositeContentProvider getContentProvider() {
        return contentProvider;
    }

    public void setDescription(String string) {
        description = string;
    }

}
