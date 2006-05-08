/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.part;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.EditorSite;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.PartService;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.themes.ITheme;

/**
 * A MultiEditor is a composite of editors.
 * 
 * This class is intended to be subclassed.
 * 		
 */
public abstract class MultiEditor extends EditorPart {

    private int activeEditorIndex;

    private IEditorPart innerEditors[];

	private IPartListener2 propagationListener;

    /**
     * Constructor for TileEditor.
     */
    public MultiEditor() {
        super();
    }

    /*
     * @see IEditorPart#doSave(IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
        for (int i = 0; i < innerEditors.length; i++) {
            IEditorPart e = innerEditors[i];
            e.doSave(monitor);
        }
    }

    /**
     * Create the control of the inner editor.
     * 
     * Must be called by subclass.
     */
    public Composite createInnerPartControl(Composite parent,
            final IEditorPart e) {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayout(new FillLayout());
        e.createPartControl(content);
        parent.addListener(SWT.Activate, new Listener() {
            public void handleEvent(Event event) {
                if (event.type == SWT.Activate) {
					activateEditor(e);
				}
            }
        });
        return content;
    }

    /*
     * @see IEditorPart#doSaveAs()
     */
    public void doSaveAs() {
        //no-op
    }

    /*
     * @see IEditorPart#init(IEditorSite, IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        init(site, (MultiEditorInput) input);
    }

    /*
     * @see IEditorPart#init(IEditorSite, IEditorInput)
     */
    public void init(IEditorSite site, MultiEditorInput input)
            throws PartInitException {
        setInput(input);
        setSite(site);
        setPartName(input.getName());
        setTitleToolTip(input.getToolTipText());
        setupEvents();
    }

    /*
     * @see IEditorPart#isDirty()
     */
    public boolean isDirty() {
        for (int i = 0; i < innerEditors.length; i++) {
            IEditorPart e = innerEditors[i];
            if (e.isDirty()) {
				return true;
			}
        }
        return false;
    }

    /*
     * @see IEditorPart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return false;
    }

    /*
     * @see IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        innerEditors[activeEditorIndex].setFocus();
        updateGradient(innerEditors[activeEditorIndex]);
    }

    /**
     * Returns the active inner editor.
     */
    public final IEditorPart getActiveEditor() {
        return innerEditors[activeEditorIndex];
    }

    /**
     * Returns an array with all inner editors.
     */
    public final IEditorPart[] getInnerEditors() {
        return innerEditors;
    }

    /**
     * Set the inner editors.
     * 
     * Should not be called by clients.
     */
    public final void setChildren(IEditorPart[] children) {
        innerEditors = children;
        activeEditorIndex = 0;
    }

    /**
     * Activates the given nested editor.
     * 
     * @param part the nested editor
     * @since 3.0
     */
    protected void activateEditor(IEditorPart part) {
        IEditorPart oldEditor = getActiveEditor();
        activeEditorIndex = getIndex(part);
        IEditorPart e = getActiveEditor();
        EditorSite innerSite = (EditorSite) e.getEditorSite();
        ((WorkbenchPage) innerSite.getPage()).requestActivation(e);
        updateGradient(oldEditor);
    }

    /**
     * Returns the index of the given nested editor.
     * 
     * @return the index of the nested editor
     * @since 3.0
     */
    protected int getIndex(IEditorPart editor) {
        for (int i = 0; i < innerEditors.length; i++) {
            if (innerEditors[i] == editor) {
				return i;
			}
        }
        return -1;
    }

    /**
     * Updates the gradient in the title bar.
     */
    public void updateGradient(IEditorPart editor) {
        boolean activeEditor = editor == getSite().getPage().getActiveEditor();
        boolean activePart = editor == getSite().getPage().getActivePart();

        ITheme theme = editor.getEditorSite().getWorkbenchWindow()
                .getWorkbench().getThemeManager().getCurrentTheme();
        Gradient g = new Gradient();

        ColorRegistry colorRegistry = theme.getColorRegistry();
        if (activePart) {
            g.fgColor = colorRegistry
                    .get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR);
            g.bgColors = new Color[2];
            g.bgColors[0] = colorRegistry
                    .get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START);
            g.bgColors[1] = colorRegistry
                    .get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END);
        } else {
            if (activeEditor) {
                g.fgColor = colorRegistry
                        .get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR);
                g.bgColors = new Color[2];
                g.bgColors[0] = colorRegistry
                        .get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START);
                g.bgColors[1] = colorRegistry
                        .get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END);
            } else {
                g.fgColor = colorRegistry
                        .get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR);
                g.bgColors = new Color[2];
                g.bgColors[0] = colorRegistry
                        .get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START);
                g.bgColors[1] = colorRegistry
                        .get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END);
            }
        }
        g.bgPercents = new int[] { theme
                .getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT) };

        drawGradient(editor, g);
    }

    /**
     * Draw the gradient in the title bar.
     */
    protected abstract void drawGradient(IEditorPart innerEditor, Gradient g);

    /**
     * Return true if the shell is activated.
     */
    protected boolean getShellActivated() {
        WorkbenchWindow window = (WorkbenchWindow) getSite().getPage()
                .getWorkbenchWindow();
        return window.getShellActivated();
    }

    /**
     * The colors used to draw the title bar of the inner editors
     */
    public static class Gradient {
        public Color fgColor;

        public Color[] bgColors;

        public int[] bgPercents;
    }
    
    
    
    /**
     * Set up the MultiEditor to propagate events like partClosed().
     *
     * @since 3.2
     */
    private void setupEvents() {
		propagationListener = new IPartListener2() {
			public void partActivated(IWorkbenchPartReference partRef) {
			}

			public void partBroughtToTop(IWorkbenchPartReference partRef) {
			}

			public void partClosed(IWorkbenchPartReference partRef) {
				IWorkbenchPart part = partRef.getPart(false);
				if (part == MultiEditor.this && innerEditors != null) {
					PartService partService = ((WorkbenchPage) getSite()
							.getPage()).getPartService();
					for (int i = 0; i < innerEditors.length; i++) {
						IEditorPart editor = innerEditors[i];
						IWorkbenchPartReference innerRef = ((PartSite) editor
								.getSite()).getPartReference();
						partService.firePartClosed(innerRef);
					}
				}
			}

			public void partDeactivated(IWorkbenchPartReference partRef) {
			}

			public void partOpened(IWorkbenchPartReference partRef) {
				IWorkbenchPart part = partRef.getPart(false);
				if (part == MultiEditor.this && innerEditors != null) {
					PartService partService = ((WorkbenchPage) getSite()
							.getPage()).getPartService();
					for (int i = 0; i < innerEditors.length; i++) {
						IEditorPart editor = innerEditors[i];
						IWorkbenchPartReference innerRef = ((PartSite) editor
								.getSite()).getPartReference();
						partService.firePartOpened(innerRef);
					}
				}
			}

			public void partHidden(IWorkbenchPartReference partRef) {
			}

			public void partVisible(IWorkbenchPartReference partRef) {
			}

			public void partInputChanged(IWorkbenchPartReference partRef) {
			}
		};
		getSite().getPage().addPartListener(propagationListener);
    }

    /**
     * Release the added listener.
     * 
     * @since 3.2
     */
	public void dispose() {
		getSite().getPage().removePartListener(propagationListener);
		super.dispose();
	}
}
