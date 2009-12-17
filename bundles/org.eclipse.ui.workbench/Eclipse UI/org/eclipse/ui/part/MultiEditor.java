/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nikolay Botev - bug 240651
 ******************************************************************************/

package org.eclipse.ui.part;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.themes.ITheme;

/**
 * A MultiEditor is a composite of editors.
 * 
 * This class is intended to be subclassed.
 * 		
 */
public abstract class MultiEditor extends AbstractMultiEditor {

	/**
     * The colors used to draw the title bar of the inner editors
     */
    public static class Gradient {
        public Color fgColor;

        public Color[] bgColors;

        public int[] bgPercents;
    }

	/**
	 * Updates the gradient in the title bar.
	 * @param editor 
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
     * @see IWorkbenchPart#setFocus()
     */
    public void setFocus() {
    	super.setFocus();
        updateGradient(getActiveEditor());
    }

    /**
     * Activates the given nested editor.
     * 
     * @param part the nested editor
     * @since 3.5
     */
    public void activateEditor(IEditorPart part) {
        IEditorPart oldEditor = getActiveEditor();
        super.activateEditor(part);
        updateGradient(oldEditor);
    }

    /**
     * Return true if the shell is activated.
     */
    protected boolean getShellActivated() {
        WorkbenchWindow window = (WorkbenchWindow) getSite().getPage()
                .getWorkbenchWindow();
        return window.getShellActivated();
    }

	public Composite getInnerEditorContainer(
			IEditorReference innerEditorReference) {
		// This method is not used by MutliEditor
		return null;
	}

	protected void innerEditorsCreated() {
		// Nothing to do
	}

}
