/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.SelectionEnabler;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This class describes the object contribution element within the popup menu
 * action registry.
 */
public class ObjectActionContributor extends PluginActionBuilder implements
        IObjectActionContributor, IAdaptable {    

    private static final String P_TRUE = "true"; //$NON-NLS-1$

    private IConfigurationElement config;

    private boolean configRead = false;

    private boolean adaptable = false;
    
    private String objectClass;

    /**
     * The constructor.
     * 
     * @param config the element
     */
    public ObjectActionContributor(IConfigurationElement config) {
        this.config = config;
        this.adaptable = P_TRUE.equalsIgnoreCase(config
                .getAttribute(IWorkbenchRegistryConstants.ATT_ADAPTABLE));
        this.objectClass = config.getAttribute(IWorkbenchRegistryConstants.ATT_OBJECTCLASS);
    }

    /* (non-Javadoc)
     * Method declared on IObjectContributor.
     */
    public boolean canAdapt() {
        return adaptable;
    }
    
    /**
     * Return the object class for this contributor.
     * 
     * @return the object class
     */
	public String getObjectClass() {
		return objectClass;
	}

    /* (non-Javadoc)
     * Method declared on IObjectActionContributor.
     */
    public void contributeObjectActionIdOverrides(List actionIdOverrides) {
        if (!configRead) {
			readConfigElement();
		}

        // Easy case out if no actions
        if (currentContribution.actions != null) {
            for (int i = 0; i < currentContribution.actions.size(); i++) {
                ActionDescriptor ad = (ActionDescriptor) currentContribution.actions
                        .get(i);
                String id = ad.getAction().getOverrideActionId();
                if (id != null) {
					actionIdOverrides.add(id);
				}
            }
        }
    }

    /**
     * Contributes actions applicable for the current selection.
     */
    public boolean contributeObjectActions(final IWorkbenchPart part,
            IMenuManager menu, ISelectionProvider selProv,
            List actionIdOverrides) {
        if (!configRead) {
			readConfigElement();
		}

        // Easy case out if no actions
        if (currentContribution.actions == null) {
			return false;
		}

        // Get a structured selection.	
        ISelection sel = selProv.getSelection();
        if ((sel == null) || !(sel instanceof IStructuredSelection)) {
			return false;
		}
        IStructuredSelection ssel = (IStructuredSelection) sel;
        
        if(canAdapt()) {        	
           IStructuredSelection newSelection = LegacyResourceSupport.adaptSelection(ssel, getObjectClass());     
           if(newSelection.size() != ssel.size()) {
        	   if (Policy.DEBUG_CONTRIBUTIONS) {
				WorkbenchPlugin.log("Error adapting selection to " + getObjectClass() +  //$NON-NLS-1$
            			". Contribution " + getID(config) + " is being ignored"); //$NON-NLS-1$ //$NON-NLS-2$            	
			}
            	return false;
           }
           ssel = newSelection;
        }
        
        final IStructuredSelection selection = ssel;
        	
        // Generate menu.
        for (int i = 0; i < currentContribution.actions.size(); i++) {
            ActionDescriptor ad = (ActionDescriptor) currentContribution.actions
                    .get(i);
            if (!actionIdOverrides.contains(ad.getId())) {
                currentContribution.contributeMenuAction(ad, menu, true);
                // Update action for the current selection and part.
                if (ad.getAction() instanceof ObjectPluginAction) {
                    final ObjectPluginAction action = (ObjectPluginAction) ad
                            .getAction();
                    ISafeRunnable runnable = new ISafeRunnable() {
						public void handleException(Throwable exception) {
							WorkbenchPlugin.log("Failed to update action "  //$NON-NLS-1$
									+ action.getId(), exception);
						}

						public void run() throws Exception {
		                    action.setActivePart(part);
		                    action.selectionChanged(selection);
						}
                    };
                    SafeRunner.run(runnable);
                }
            }
        }
        return true;
    }

    /**
	 * Contributes menus applicable for the current selection.
	 */
    public boolean contributeObjectMenus(IMenuManager menu,
            ISelectionProvider selProv) {
        if (!configRead) {
			readConfigElement();
		}

        // Easy case out if no menus
        if (currentContribution.menus == null) {
			return false;
		}

        // Get a structured selection.	
        ISelection sel = selProv.getSelection();
        if ((sel == null) || !(sel instanceof IStructuredSelection)) {
			return false;
		}

        // Generate menu.
        for (int i = 0; i < currentContribution.menus.size(); i++) {
            IConfigurationElement menuElement = (IConfigurationElement) currentContribution.menus
                    .get(i);
            currentContribution.contributeMenu(menuElement, menu, true);
        }
        return true;
    }

    /* (non-Javadoc)
     * Method declared on PluginActionBuilder.
     */
    protected ActionDescriptor createActionDescriptor(
            IConfigurationElement element) {
        return new ActionDescriptor(element, ActionDescriptor.T_POPUP);
    }

    /* (non-Javadoc)
     * Method declared on PluginActionBuilder.
     */
    protected BasicContribution createContribution() {
        return new ObjectContribution();
    }

    /**
     * Returns true if name filter is not specified for the contribution
     * or the current selection matches the filter.
     */
    public boolean isApplicableTo(Object object) {
        if (!configRead) {
			readConfigElement();
		}

        // Perform all tests with an instance of the objectClass and not
        // the actual selected object.
        if (canAdapt()) {
			Object adapted = LegacyResourceSupport.getAdapter(object, getObjectClass());
			if (adapted == null) {
				if (Policy.DEBUG_CONTRIBUTIONS) {
					WorkbenchPlugin
							.log("Error adapting " + object.getClass().getName() + //$NON-NLS-1$
									" to " //$NON-NLS-1$
									+ getObjectClass()
									+ ". Contribution " + getID(config) + " is being ignored"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				object = adapted;
			}
		}
			
        if (!testName(object)) {
			return false;
		}

        return ((ObjectContribution) currentContribution)
                .isApplicableTo(object);
    }

    /**
     * Reads the configuration element and all the children.
     * This creates an action descriptor for every action in the extension.
     */
    private void readConfigElement() {
        currentContribution = createContribution();
        readElementChildren(config);
        configRead = true;
    }

    /* (non-Javadoc)
     * Method declared on PluginActionBuilder.
     */
    protected boolean readElement(IConfigurationElement element) {
        String tag = element.getName();

        // Found visibility sub-element
        if (tag.equals(IWorkbenchRegistryConstants.TAG_VISIBILITY)) {
            ((ObjectContribution) currentContribution)
                    .setVisibilityTest(element);
            return true;
        }

        // Found filter sub-element				
        if (tag.equals(IWorkbenchRegistryConstants.TAG_FILTER)) {
            ((ObjectContribution) currentContribution).addFilterTest(element);
            return true;
        }

        if (tag.equals(IWorkbenchRegistryConstants.TAG_ENABLEMENT)) {
            ((ObjectContribution) currentContribution)
                    .setEnablementTest(element);
            return true;
        }

        return super.readElement(element);
    }

    /**
     * Returns whether the current selection matches the contribution name filter.
     */
    private boolean testName(Object object) {
        String nameFilter = config.getAttribute(IWorkbenchRegistryConstants.ATT_NAME_FILTER);
        if (nameFilter == null) {
			return true;
		}
        String objectName = null;
        IWorkbenchAdapter de = (IWorkbenchAdapter)Util.getAdapter(object, IWorkbenchAdapter.class);
        if (de != null) {
			objectName = de.getLabel(object);
		}
        if (objectName == null) {
            objectName = object.toString();
        }
        return SelectionEnabler.verifyNameMatch(objectName, nameFilter);
    }

    /**
     * Helper class to collect the menus and actions defined within a
     * contribution element.
     */
    private static class ObjectContribution extends BasicContribution {
        private ObjectFilterTest filterTest;

        private ActionExpression visibilityTest;

        private Expression enablement;

        /**
         * Add a filter test.
         * 
         * @param element the element
         */
        public void addFilterTest(IConfigurationElement element) {
            if (filterTest == null) {
				filterTest = new ObjectFilterTest();
			}
            filterTest.addFilterElement(element);
        }

        /**
         * Set the visibility test.
         * 
         * @param element the element
         */
        public void setVisibilityTest(IConfigurationElement element) {
            visibilityTest = new ActionExpression(element);
        }

        /**
         * Set the enablement test.
         * 
         * @param element the element
         */
        public void setEnablementTest(IConfigurationElement element) {
            try {
                enablement = ExpressionConverter.getDefault().perform(element);
            } catch (CoreException e) {
                WorkbenchPlugin.log(e);
            }
        }

        /**
         * Returns true if name filter is not specified for the contribution
         * or the current selection matches the filter.
         * 
         * @param object the object to test
         * @return whether we're applicable
         */
        public boolean isApplicableTo(Object object) {
            boolean result = true;
            if (visibilityTest != null) {
                result = result && visibilityTest.isEnabledFor(object);
                if (!result) {
					return result;
				}
            } else if (filterTest != null) {
                result = result && filterTest.matches(object, true);
                if (!result) {
					return result;
				}
            }
            if (enablement != null) {
                try {
                    IEvaluationContext context = new EvaluationContext(null,
                            object);
                    context.setAllowPluginActivation(true);
                    context.addVariable("selection", object); //$NON-NLS-1$
                    EvaluationResult evalResult = enablement.evaluate(context);
                    if (evalResult == EvaluationResult.FALSE) {
						return false;
					}
                } catch (CoreException e) {
                    enablement = null;
                    WorkbenchPlugin.log(e);
                    result = false;
                }
            }
            return result;
        }
    }    
    
    /**
     * Debugging helper that will print out the contribution names for this
     * contributor.
     */
    public String toString() {
    	StringBuffer buffer = new StringBuffer();
    	IConfigurationElement[] children = config.getChildren();
    	for (int i = 0; i < children.length; i++) {
			IConfigurationElement element = children[i];
			String label = element.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
			if(label != null) {
				buffer.append(label);
				buffer.append('\n'); 
			}
		}
    	return buffer.toString();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IConfigurationElement.class)) {
			return config;
		}
		return null;
	}
}
