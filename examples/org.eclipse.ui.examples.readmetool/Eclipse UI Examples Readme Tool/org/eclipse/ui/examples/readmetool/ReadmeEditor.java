/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.examples.readmetool;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.dnd.IDragAndDropService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * This class implements the Readme editor.  Since the readme
 * editor is mostly just a text editor, there is very little
 * implemented in this actual class.  It can be regarded as
 * simply decorating the text editor with a content outline.
 */
public class ReadmeEditor extends TextEditor {
    protected ReadmeContentOutlinePage page;

    /**
     * Creates a new ReadmeEditor.
     */
    public ReadmeEditor() {
        super();
    }

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		StyledText tw = getSourceViewer().getTextWidget();

		// Add a 'TextTransfer' drop target to the editor
		int ops = DND.DROP_DEFAULT | DND.DROP_COPY;
		Transfer[] transfers = { TextTransfer.getInstance() };
		DropTargetListener editorListener = new DropTargetListener() {

			@Override
			public void dragEnter(DropTargetEvent event) {
				event.detail = DND.DROP_COPY;
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				event.detail = DND.DROP_COPY;
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SCROLL | DND.FEEDBACK_SELECT;
			}

			@Override
			public void drop(DropTargetEvent event) {
		        if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
					String text = (String) event.data;
					getSourceViewer().getTextWidget().insert(text);
				}
			}

			@Override
			public void dropAccept(DropTargetEvent event) {
			}

		};

		IDragAndDropService dtSvc = getSite().getService(IDragAndDropService.class);
		dtSvc.addMergedDropTarget(tw, ops, transfers, editorListener);
	}

    @Override
	public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);
        if (page != null)
            page.update();
    }

    @SuppressWarnings("unchecked")
    @Override
	public <T> T getAdapter(Class<T> key) {
        if (key.equals(IContentOutlinePage.class)) {
            IEditorInput input = getEditorInput();
            if (input instanceof IFileEditorInput) {
                page = new ReadmeContentOutlinePage(((IFileEditorInput) input)
                        .getFile());
                return (T) page;
            }
        }
        return super.getAdapter(key);
    }

    @Override
	protected void editorContextMenuAboutToShow(IMenuManager parentMenu) {
        super.editorContextMenuAboutToShow(parentMenu);
        parentMenu.add(new Separator());
        IMenuManager subMenu = new MenuManager(MessageUtil.getString("Add")); //$NON-NLS-1$
        parentMenu.add(subMenu);
        if (subMenu != null) {
            // Add readme actions with various attributes
            Object[][] att = new Object[][] { { IReadmeConstants.MARKER_ATT_ID,
                    Integer.valueOf(1234) } };
            subMenu
                    .add(new AddReadmeMarkerAction(
                            this,
                            MessageUtil
                                    .getString("Add_readme_marker_action_label") + "1", //$NON-NLS-1$ //$NON-NLS-2$
                            att,
                            MessageUtil
                                    .getString("Readme_marker_message_example") + " id=1234")); //$NON-NLS-1$ //$NON-NLS-2$

            att = new Object[][] { { IReadmeConstants.MARKER_ATT_LEVEL,
                    Integer.valueOf(7) } };
            subMenu
                    .add(new AddReadmeMarkerAction(
                            this,
                            MessageUtil
                                    .getString("Add_readme_marker_action_label") + "2", //$NON-NLS-1$ //$NON-NLS-2$
                            att,
                            MessageUtil
                                    .getString("Readme_marker_message_example") + " level=7")); //$NON-NLS-1$ //$NON-NLS-2$

            att = new Object[][] {
                    { IReadmeConstants.MARKER_ATT_LEVEL, Integer.valueOf(7) },
                    { IReadmeConstants.MARKER_ATT_DEPT, "infra" } }; //$NON-NLS-1$
            subMenu
                    .add(new AddReadmeMarkerAction(
                            this,
                            MessageUtil
                                    .getString("Add_readme_marker_action_label") + "3", //$NON-NLS-1$ //$NON-NLS-2$
                            att,
                            MessageUtil
                                    .getString("Readme_marker_message_example") + " level=7, department=infra")); //$NON-NLS-1$ //$NON-NLS-2$

            att = new Object[][] { { IReadmeConstants.MARKER_ATT_CODE, "red" } }; //$NON-NLS-1$
            subMenu
                    .add(new AddReadmeMarkerAction(
                            this,
                            MessageUtil
                                    .getString("Add_readme_marker_action_label") + "4", //$NON-NLS-1$ //$NON-NLS-2$
                            att,
                            MessageUtil
                                    .getString("Readme_marker_message_example") + " code=red")); //$NON-NLS-1$ //$NON-NLS-2$

            att = new Object[][] { { IReadmeConstants.MARKER_ATT_LANG,
                    "english" } }; //$NON-NLS-1$
            subMenu
                    .add(new AddReadmeMarkerAction(
                            this,
                            MessageUtil
                                    .getString("Add_readme_marker_action_label") + "5", //$NON-NLS-1$ //$NON-NLS-2$
                            att,
                            MessageUtil
                                    .getString("Readme_marker_message_example") + " language=english")); //$NON-NLS-1$ //$NON-NLS-2$

            att = new Object[][] {
                    { IReadmeConstants.MARKER_ATT_ID, Integer.valueOf(1234) },
                    { IReadmeConstants.MARKER_ATT_LEVEL, Integer.valueOf(7) },
                    { IReadmeConstants.MARKER_ATT_DEPT, "infra" }, //$NON-NLS-1$
                    { IReadmeConstants.MARKER_ATT_CODE, "red" }, //$NON-NLS-1$
                    { IReadmeConstants.MARKER_ATT_LANG, "english" } }; //$NON-NLS-1$
            subMenu
                    .add(new AddReadmeMarkerAction(
                            this,
                            MessageUtil
                                    .getString("Add_readme_marker_action_label") + "6", //$NON-NLS-1$ //$NON-NLS-2$
                            att,
                            MessageUtil
                                    .getString("Readme_marker_message_example") + //$NON-NLS-1$
                                    " id=1234, level=7, department=infra, code=red, language=english")); //$NON-NLS-1$

            att = new Object[0][0];
            subMenu
                    .add(new AddReadmeMarkerAction(
                            this,
                            MessageUtil
                                    .getString("Add_readme_marker_action_label") + "7", //$NON-NLS-1$ //$NON-NLS-2$
                            att,
                            MessageUtil
                                    .getString("Readme_marker_message_example") + " No attributes specified")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}
