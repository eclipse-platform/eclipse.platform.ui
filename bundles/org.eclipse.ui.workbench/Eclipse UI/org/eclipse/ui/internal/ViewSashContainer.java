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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;

/**
 * Represents the top level container.
 */
public class ViewSashContainer extends PartSashContainer {
    public ViewSashContainer(WorkbenchPage page, Composite parent) {
        super("root layout container", page, parent);//$NON-NLS-1$
    }

    /**
     * Gets root container for this part.
     */
    public ViewSashContainer getRootContainer() {
        return this;
    }

    /**
     * Subclasses override this method to specify
     * the composite to use to parent all children
     * layout parts it contains.
     */
    protected Composite createParent(Composite parentWidget) {
        return parentWidget;
    }

    /**
     * Subclasses override this method to dispose
     * of any swt resources created during createParent.
     */
    protected void disposeParent() {
        // do nothing
    }

    /**
     * Get the part control.  This method may return null.
     */
    public Control getControl() {
        return this.parent;
    }

    /**
     * @see IPersistablePart
     */
    public IStatus restoreState(IMemento memento) {
        MultiStatus result = new MultiStatus(
                PlatformUI.PLUGIN_ID,
                IStatus.OK,
                WorkbenchMessages.RootLayoutContainer_problemsRestoringPerspective, null);

        // Read the info elements.
        IMemento[] children = memento.getChildren(IWorkbenchConstants.TAG_INFO);

        // Create a part ID to part hashtable.
        final Map mapIDtoPart = new HashMap(children.length);

        // Loop through the info elements.
        for (int i = 0; i < children.length; i++) {
            // Get the info details.
            IMemento childMem = children[i];
            String partID = childMem.getString(IWorkbenchConstants.TAG_PART);
            final String relativeID = childMem
                    .getString(IWorkbenchConstants.TAG_RELATIVE);
            int relationship = 0;
            float ratio = 0.0f;
            int left = 0, right = 0;
            if (relativeID != null) {
                relationship = childMem.getInteger(
                        IWorkbenchConstants.TAG_RELATIONSHIP).intValue();

                // Note: the ratio is used for reading pre-3.0 eclipse workspaces. It should be ignored
                // if "left" and "right" are available.
                Float ratioFloat = childMem
                        .getFloat(IWorkbenchConstants.TAG_RATIO);
                Integer leftInt = childMem
                        .getInteger(IWorkbenchConstants.TAG_RATIO_LEFT);
                Integer rightInt = childMem
                        .getInteger(IWorkbenchConstants.TAG_RATIO_RIGHT);
                if (leftInt != null && rightInt != null) {
                    left = leftInt.intValue();
                    right = rightInt.intValue();
                } else {
                    if (ratioFloat != null) {
                        ratio = ratioFloat.floatValue();
                    }
                }
            }
            String strFolder = childMem
                    .getString(IWorkbenchConstants.TAG_FOLDER);

            // Create the part.
            LayoutPart part = null;
            if (strFolder == null) {
				part = new PartPlaceholder(partID);
			} else {
                ViewStack folder = new ViewStack(page);
                folder.setID(partID);
                result.add(folder.restoreState(childMem
                        .getChild(IWorkbenchConstants.TAG_FOLDER)));
                ContainerPlaceholder placeholder = new ContainerPlaceholder(
                        partID);
                placeholder.setRealContainer(folder);
                part = placeholder;
            }
            // 1FUN70C: ITPUI:WIN - Shouldn't set Container when not active
            part.setContainer(this);

            final int myLeft = left, myRight= right, myRelationship = relationship;
            final float myRatio = ratio;
            final LayoutPart myPart = part;
            
            StartupThreading.runWithoutExceptions(new StartupRunnable() {

				public void runWithException() throws Throwable {
					// Add the part to the layout
		            if (relativeID == null) {
		                add(myPart);
		            } else {
		                LayoutPart refPart = (LayoutPart) mapIDtoPart.get(relativeID);
		                if (refPart != null) {
		                    if (myLeft != 0) {
								add(myPart, myRelationship, myLeft, myRight, refPart);
							} else {
								add(myPart, myRelationship, myRatio, refPart);
							}
		                } else {
		                    WorkbenchPlugin
		                            .log("Unable to find part for ID: " + relativeID);//$NON-NLS-1$
		                }
		            }
				}});
            
            mapIDtoPart.put(partID, part);
        }
        return result;
    }

    /**
     * @see IPersistablePart
     */
    public IStatus saveState(IMemento memento) {
        RelationshipInfo[] relationships = computeRelation();

        MultiStatus result = new MultiStatus(
                PlatformUI.PLUGIN_ID,
                IStatus.OK,
                WorkbenchMessages.RootLayoutContainer_problemsSavingPerspective, null); 

        // Loop through the relationship array.
        for (int i = 0; i < relationships.length; i++) {
            // Save the relationship info ..
            //		private LayoutPart part;
            // 		private int relationship;
            // 		private float ratio;
            // 		private LayoutPart relative;
            RelationshipInfo info = relationships[i];
            IMemento childMem = memento
                    .createChild(IWorkbenchConstants.TAG_INFO);
            childMem.putString(IWorkbenchConstants.TAG_PART, info.part.getID());
            if (info.relative != null) {
                childMem.putString(IWorkbenchConstants.TAG_RELATIVE,
                        info.relative.getID());
                childMem.putInteger(IWorkbenchConstants.TAG_RELATIONSHIP,
                        info.relationship);
                childMem.putInteger(IWorkbenchConstants.TAG_RATIO_LEFT,
                        info.left);
                childMem.putInteger(IWorkbenchConstants.TAG_RATIO_RIGHT,
                        info.right);

                // The ratio is only needed for saving workspaces that can be read by old versions
                // of Eclipse. It is not used in newer versions of Eclipse, which use the "left"
                // and "right" attributes instead.
                childMem.putFloat(IWorkbenchConstants.TAG_RATIO, info
                        .getRatio());
            }

            // Is this part a folder or a placeholder for one?
            ViewStack folder = null;
            if (info.part instanceof ViewStack) {
                folder = (ViewStack) info.part;
            } else if (info.part instanceof ContainerPlaceholder) {
                LayoutPart part = ((ContainerPlaceholder) info.part)
                        .getRealContainer();
                if (part instanceof ViewStack) {
					folder = (ViewStack) part;
				}
            }

            // If this is a folder (ViewStack) save the contents.
            if (folder != null) {
                childMem.putString(IWorkbenchConstants.TAG_FOLDER, "true");//$NON-NLS-1$
                
                IMemento folderMem = childMem
                        .createChild(IWorkbenchConstants.TAG_FOLDER);
                result.add(folder.saveState(folderMem));
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartSashContainer#getDockingRatio(org.eclipse.ui.internal.LayoutPart, org.eclipse.ui.internal.LayoutPart)
     */
    protected float getDockingRatio(LayoutPart dragged, LayoutPart target) {
        if (isStackType(target)) {
            return super.getDockingRatio(dragged, target);
        } else {
            return 0.25f;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartSashContainer#isStackType(org.eclipse.ui.internal.LayoutPart)
     */
    public boolean isStackType(LayoutPart toTest) {
        return (toTest instanceof ViewStack);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartSashContainer#isPaneType(org.eclipse.ui.internal.LayoutPart)
     */
    public boolean isPaneType(LayoutPart toTest) {
        return (toTest instanceof ViewPane);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartSashContainer#createStack(org.eclipse.ui.internal.LayoutPart)
     */
    protected PartStack createStack() {
        ViewStack result = new ViewStack(page);
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartSashContainer#setVisiblePart(org.eclipse.ui.internal.ILayoutContainer, org.eclipse.ui.internal.LayoutPart)
     */
    protected void setVisiblePart(ILayoutContainer container,
            LayoutPart visiblePart) {
        if (container instanceof ViewStack) {
            ViewStack tabFolder = (ViewStack) container;

            tabFolder.setSelection(visiblePart);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartSashContainer#getVisiblePart(org.eclipse.ui.internal.ILayoutContainer)
     */
    protected LayoutPart getVisiblePart(ILayoutContainer container) {
        return ((ViewStack) container).getSelection();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartSashContainer#derefPart(org.eclipse.ui.internal.LayoutPart)
     */
    protected void derefPart(LayoutPart sourcePart) {
        page.getActivePerspective().getPresentation().derefPart(sourcePart);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartSashContainer#addChild(org.eclipse.ui.internal.PartSashContainer.RelationshipInfo)
     */
    protected void addChild(RelationshipInfo info) {
        LayoutPart child = info.part;

        // Nasty hack: ensure that all views end up inside a tab folder.
        // Since the view title is provided by the tab folder, this ensures
        // that views don't get created without a title tab.
        if (child instanceof ViewPane) {
            ViewStack folder = new ViewStack(page);
            folder.add(child);
            info.part = folder;
        }

        super.addChild(info);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.ILayoutContainer#replace(org.eclipse.ui.internal.LayoutPart, org.eclipse.ui.internal.LayoutPart)
     */
    public void replace(LayoutPart oldChild, LayoutPart newChild) {
        if (!isChild(oldChild)) {
            return;
        }

        // Nasty hack: ensure that all views end up inside a tab folder.
        // Since the view title is provided by the tab folder, this ensures
        // that views don't get created without a title tab.
        if (newChild instanceof ViewPane) {
            ViewStack folder = new ViewStack(page);
            folder.add(newChild);
            newChild = folder;
        }

        super.replace(oldChild, newChild);
    }
}
