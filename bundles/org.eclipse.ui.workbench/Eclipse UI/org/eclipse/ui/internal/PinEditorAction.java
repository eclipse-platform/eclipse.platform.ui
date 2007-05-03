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

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.tweaklets.TabBehaviour;
import org.eclipse.ui.internal.tweaklets.Tweaklets;

/**
 * Action to toggle the pin state of an editor. If an editor is
 * pinned, then it is not reused.
 */
public class PinEditorAction extends ActiveEditorAction {
    private IPropertyListener propListener = new IPropertyListener() {
        public void propertyChanged(Object source, int propId) {
            if (propId == WorkbenchPartReference.INTERNAL_PROPERTY_PINNED) {
                WorkbenchPartReference ref = (WorkbenchPartReference)source;
                setChecked(ref.isPinned());
            } else if (propId == IWorkbenchPartConstants.PROP_DIRTY) {
        		if (((TabBehaviour)Tweaklets.get(TabBehaviour.KEY)).autoPinOnDirty()) {
					WorkbenchPartReference ref = (WorkbenchPartReference) source;
					if (ref.isDirty()) {
						ref.setPinned(true);
					}
				}
            }
        }
    };

    /**
     * Creates a PinEditorAction.
     */
    public PinEditorAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.PinEditorAction_text, window); 
        setActionDefinitionId("org.eclipse.ui.window.pinEditor"); //$NON-NLS-1$
        setToolTipText(WorkbenchMessages.PinEditorAction_toolTip); 
        setId("org.eclipse.ui.internal.PinEditorAction"); //$NON-NLS-1$
        // @issue need help constant for this?
        //	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.SAVE_ACTION});
        setImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_PIN_EDITOR));
        setDisabledImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_PIN_EDITOR_DISABLED));
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        if (getWorkbenchWindow() == null) {
            // action has been dispose
            return;
        }
        IEditorPart editor = getActiveEditor();
        if (editor != null) {
            WorkbenchPartReference ref = getReference(editor);
            ref.setPinned(isChecked());
        }
    }

    private WorkbenchPartReference getReference(IEditorPart editor) {
        return (WorkbenchPartReference)((EditorSite)editor.getSite()).getPartReference();
    }
    
    /* (non-Javadoc)
     * Method declared on ActiveEditorAction.
     */
    protected void updateState() {
        if (getWorkbenchWindow() == null || getActivePage() == null) {
            setChecked(false);
            setEnabled(false);
            return;
        }

        IEditorPart editor = getActiveEditor();
        boolean enabled = (editor != null);
        setEnabled(enabled);
        if (enabled) {
            setChecked(getReference(editor).isPinned());
        } else {
            setChecked(false);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.ActiveEditorAction#editorActivated(org.eclipse.ui.IEditorPart)
     */
    protected void editorActivated(IEditorPart part) {
        super.editorActivated(part);
        if (part != null) {
            getReference(part).addInternalPropertyListener(propListener);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.ActiveEditorAction#editorDeactivated(org.eclipse.ui.IEditorPart)
     */
    protected void editorDeactivated(IEditorPart part) {
        super.editorDeactivated(part);
        if (part != null) {
            getReference(part).removeInternalPropertyListener(propListener);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
     */
    public void dispose() {
        // deactivate current editor now before super dispose because active editor will be null after call
        editorDeactivated(getActiveEditor());
        super.dispose();
    }
}
