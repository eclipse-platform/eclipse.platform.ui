/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * A <code>PopupDialog</code> that is automatically positioned relative
 * to the current selection on viewer on which it is installed. The 
 * popup can be dismissed in the same manor as all popup dialogs, but 
 * additionally allows users the option of specifying a command id that 
 * can be used to persist the contents of the dialog.
 * 
 * Client are expected to subclass this.
 * 
 * Note: This class subclasses PopupDialog which is currently
 * marked as experimental API. Users should therefore consider
 * this class to be experimental as well.
 * @see org.eclipse.jface.dialogs.PopupDialog
 * 
 * @since 3.2
 */
public abstract class DebugPopup extends PopupDialog {

    private ITextViewer fViewer;

    private IHandlerActivation fActivation;

    private IHandlerService fHandlerService;

    /**
     * Constructs a new popup dialog of type <code>PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE</code>
     * @param parent The parent shell
     * @param viewer The text viewer on which the popup is to be installed
     */
    public DebugPopup(Shell parent, ITextViewer viewer) {
        super(parent, PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE, true, true, false, true, null, null);
        fViewer = viewer;
    }

    /**
     * Returns the text to be shown in the popups's info area. 
     * May return <code>null</code>
     * 
     * @return The text to be shown in the popup's info area or <code>null</code>
     */
    protected String getInfoText() {
        return null;
    }

    /**
     * Returns the command id to be used for persisting the contents of the
     * dialog. If the contents should not be persisted, this method should 
     * return null. 
     * 
     * @return The command id to be used for persisting the contents of the
     * dialog or <code>null</code>
     */
    protected String getCommandId() {
        return null;
    }

    /**
     * Persists the contents of the dialog.
     */
    protected void persist() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.PopupDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected abstract Control createDialogArea(Composite parent);


    /**
     * Returns the initial location to use for the shell based upon the 
     * current selection in the viewer. Bottom is preferred to top, and 
     * right is preferred to left, therefore if possible the popup will
     * be located below and to the right of the selection.
     * 
     * @param initialSize
     *            the initial size of the shell, as returned by
     *            <code>getInitialSize</code>.
     * @return the initial location of the shell
     */
    protected Point getInitialLocation(Point initialSize) {
        StyledText textWidget = fViewer.getTextWidget();
        Point docRange = textWidget.getSelectionRange();
        int midOffset = docRange.x + (docRange.y / 2);
        Point point = textWidget.getLocationAtOffset(midOffset);
        point = textWidget.toDisplay(point);

        GC gc = new GC(textWidget);
        gc.setFont(textWidget.getFont());
        int height = gc.getFontMetrics().getHeight();
        gc.dispose();
        point.y += height;

        Rectangle monitor = textWidget.getMonitor().getClientArea();
        if (monitor.width < point.x + initialSize.x) {
            point.x = Math.max(0, point.x - initialSize.x);
        }
        if (monitor.height < point.y + initialSize.y) {
            point.y = Math.max(0, point.y - initialSize.y - height);
        }

        return point;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.PopupDialog#getDialogSettings()
     */
    protected IDialogSettings getDialogSettings() {
        IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
        return settings;
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.PopupDialog#open()
     */
    public int open() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        String commandId = getCommandId();
        if (commandId != null) {
            IHandler fCloseHandler = new AbstractHandler() {
                public Object execute(ExecutionEvent event) throws ExecutionException {
                    persist();
                    close();
                    return null;
                }
            };

            fHandlerService = (IHandlerService) workbench.getAdapter(IHandlerService.class);
            fActivation = fHandlerService.activateHandler(commandId, fCloseHandler);
        }

        String infoText = getInfoText();
        if (infoText != null)
            setInfoText(infoText);
        
        return super.open();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.PopupDialog#close()
     */
    public boolean close() {
        if (fActivation != null)
            fHandlerService.deactivateHandler(fActivation);

        return super.close();
    }
}
