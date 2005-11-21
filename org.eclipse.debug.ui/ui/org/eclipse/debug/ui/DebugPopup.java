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

public abstract class DebugPopup extends PopupDialog {

    private ITextViewer fViewer;

    private IHandlerActivation fActivation;

    private IHandlerService fHandlerService;

    public DebugPopup(Shell parent, ITextViewer viewer) {
        super(parent, PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE, true, true, false, true, null, null);
        fViewer = viewer;
    }

    protected String getInfoText() {
        return null;
    }

    protected String getCommandId() {
        return null;
    }

    protected void persist() {
    }

    protected abstract Control createDialogArea(Composite parent);

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

    protected IDialogSettings getDialogSettings() {
        IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
        return settings;
    }

    
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

    public boolean close() {
        if (fActivation != null)
            fHandlerService.deactivateHandler(fActivation);

        return super.close();
    }
}
