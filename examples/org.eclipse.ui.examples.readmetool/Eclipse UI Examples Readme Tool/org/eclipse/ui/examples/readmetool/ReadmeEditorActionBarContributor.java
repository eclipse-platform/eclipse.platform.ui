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

package org.eclipse.ui.examples.readmetool;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.LabelRetargetAction;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;

/**
 * This class demonstrates action contribution for the readme editor.
 * A number of menu, toolbar, and status line contributions are defined
 * in the workbench window.  These actions are shared among all 
 * readme editors, and are only visible when a readme editor is 
 * active.  Otherwise, they are invisible.
 */
public class ReadmeEditorActionBarContributor extends
        BasicTextEditorActionContributor {
    private EditorAction action1;

    private RetargetAction action2;

    private LabelRetargetAction action3;

    private EditorAction handler2;

    private EditorAction handler3;

    private EditorAction handler4;

    private EditorAction handler5;

    private DirtyStateContribution dirtyStateContribution;

    class EditorAction extends Action {
        private Shell shell;

        private IEditorPart activeEditor;

        public EditorAction(String label) {
            super(label);
        }

        public void setShell(Shell shell) {
            this.shell = shell;
        }

        public void run() {
            String editorName = MessageUtil.getString("Empty_Editor_Name"); //$NON-NLS-1$
            if (activeEditor != null)
                editorName = activeEditor.getTitle();
            MessageDialog
                    .openInformation(
                            shell,
                            MessageUtil.getString("Readme_Editor"), //$NON-NLS-1$
                            MessageUtil
                                    .format(
                                            "ReadmeEditorActionExecuted", new Object[] { getText(), editorName })); //$NON-NLS-1$
        }

        public void setActiveEditor(IEditorPart part) {
            activeEditor = part;
        }
    }

    /**
     * Creates a new ReadmeEditorActionBarContributor.
     */
    public ReadmeEditorActionBarContributor() {
        action1 = new EditorAction(MessageUtil.getString("Editor_Action1")); //$NON-NLS-1$
        action1.setToolTipText(MessageUtil.getString("Readme_Editor_Action1")); //$NON-NLS-1$
        action1
                .setDisabledImageDescriptor(ReadmeImages.EDITOR_ACTION1_IMAGE_DISABLE);
        action1.setImageDescriptor(ReadmeImages.EDITOR_ACTION1_IMAGE_ENABLE);
        action1.setHoverImageDescriptor(ReadmeImages.EDITOR_ACTION1_IMAGE);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(action1, IReadmeConstants.EDITOR_ACTION1_CONTEXT);

        action2 = new RetargetAction(IReadmeConstants.RETARGET2, MessageUtil
                .getString("Editor_Action2")); //$NON-NLS-1$
        action2.setToolTipText(MessageUtil.getString("Readme_Editor_Action2")); //$NON-NLS-1$
        action2
                .setDisabledImageDescriptor(ReadmeImages.EDITOR_ACTION2_IMAGE_DISABLE);
        action2.setImageDescriptor(ReadmeImages.EDITOR_ACTION2_IMAGE_ENABLE);
        action2.setHoverImageDescriptor(ReadmeImages.EDITOR_ACTION2_IMAGE);

        action3 = new LabelRetargetAction(IReadmeConstants.LABELRETARGET3,
                MessageUtil.getString("Editor_Action3")); //$NON-NLS-1$
        action3
                .setDisabledImageDescriptor(ReadmeImages.EDITOR_ACTION3_IMAGE_DISABLE);
        action3.setImageDescriptor(ReadmeImages.EDITOR_ACTION3_IMAGE_ENABLE);
        action3.setHoverImageDescriptor(ReadmeImages.EDITOR_ACTION3_IMAGE);

        handler2 = new EditorAction(MessageUtil.getString("Editor_Action2")); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp(action2, IReadmeConstants.EDITOR_ACTION2_CONTEXT);

        handler3 = new EditorAction(MessageUtil.getString("Editor_Action3")); //$NON-NLS-1$
        handler3.setToolTipText(MessageUtil.getString("Readme_Editor_Action3")); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp(action3, IReadmeConstants.EDITOR_ACTION3_CONTEXT);

        handler4 = new EditorAction(MessageUtil.getString("Editor_Action4")); //$NON-NLS-1$
        handler5 = new EditorAction(MessageUtil.getString("Editor_Action5")); //$NON-NLS-1$
        handler5.setToolTipText(MessageUtil.getString("Readme_Editor_Action5")); //$NON-NLS-1$

        dirtyStateContribution = new DirtyStateContribution();
    }

    /** (non-Javadoc)
     * Method declared on EditorActionBarContributor
     */
    public void contributeToMenu(IMenuManager menuManager) {
        // Run super.
        super.contributeToMenu(menuManager);

        // Editor-specitic menu
        MenuManager readmeMenu = new MenuManager(MessageUtil
                .getString("Readme_Menu")); //$NON-NLS-1$
        // It is important to append the menu to the
        // group "additions". This group is created
        // between "Project" and "Tools" menus
        // for this purpose.
        menuManager.insertAfter("additions", readmeMenu); //$NON-NLS-1$
        readmeMenu.add(action1);
        readmeMenu.add(action2);
        readmeMenu.add(action3);
    }

    /** (non-Javadoc)
     * Method declared on EditorActionBarContributor
     */
    public void contributeToStatusLine(IStatusLineManager statusLineManager) {
        // Run super.
        super.contributeToStatusLine(statusLineManager);
        // Test status line.	
        statusLineManager.setMessage(MessageUtil.getString("Editor_is_active")); //$NON-NLS-1$
        statusLineManager.add(dirtyStateContribution);
    }

    /** (non-Javadoc)
     * Method declared on EditorActionBarContributor
     */
    public void contributeToToolBar(IToolBarManager toolBarManager) {
        // Run super.
        super.contributeToToolBar(toolBarManager);

        // Add toolbar stuff.
        toolBarManager.add(new Separator("ReadmeEditor")); //$NON-NLS-1$
        toolBarManager.add(action1);
        toolBarManager.add(action2);
        toolBarManager.add(action3);
    }

    /** (non-Javadoc)
     * Method declared on IEditorActionBarContributor
     */
    public void dispose() {
        // Remove retarget actions as page listeners
        getPage().removePartListener(action2);
        getPage().removePartListener(action3);
    }

    /** (non-Javadoc)
     * Method declared on IEditorActionBarContributor
     */
    public void init(IActionBars bars, IWorkbenchPage page) {
        super.init(bars, page);
        bars.setGlobalActionHandler(IReadmeConstants.RETARGET2, handler2);
        bars.setGlobalActionHandler(IReadmeConstants.LABELRETARGET3, handler3);
        bars.setGlobalActionHandler(IReadmeConstants.ACTION_SET_RETARGET4,
                handler4);
        bars.setGlobalActionHandler(IReadmeConstants.ACTION_SET_LABELRETARGET5,
                handler5);

        // Hook retarget actions as page listeners
        page.addPartListener(action2);
        page.addPartListener(action3);
        IWorkbenchPart activePart = page.getActivePart();
        if (activePart != null) {
            action2.partActivated(activePart);
            action3.partActivated(activePart);
        }
    }

    /** (non-Javadoc)
     * Method declared on IEditorActionBarContributor
     */
    public void setActiveEditor(IEditorPart editor) {
        // Run super.
        super.setActiveEditor(editor);

        // Target shared actions to new editor
        action1.setActiveEditor(editor);
        handler2.setActiveEditor(editor);
        handler3.setActiveEditor(editor);
        handler4.setActiveEditor(editor);
        handler5.setActiveEditor(editor);
        dirtyStateContribution.editorChanged(editor);
    }
}
