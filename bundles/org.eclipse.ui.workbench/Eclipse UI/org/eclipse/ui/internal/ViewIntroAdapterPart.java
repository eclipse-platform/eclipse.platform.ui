/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.intro.IntroMessages;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.part.ViewPart;

/**
 * Simple view that will wrap an <code>IIntroPart</code>.
 * 
 * @since 3.0
 */
public final class ViewIntroAdapterPart extends ViewPart {

    private IIntroPart introPart;

    private IIntroSite introSite;

    private boolean handleZoomEvents = true;

    /**
     * Adds a listener that toggles standby state if the view pane is zoomed. 
     */
    private void addPaneListener() {
        ((PartSite) getSite()).getPane().addPropertyChangeListener(
                new IPropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        if (handleZoomEvents) {
                            if (event.getProperty()
                                    .equals(PartPane.PROP_ZOOMED)) {
                                boolean standby = !((Boolean) event
                                        .getNewValue()).booleanValue();
                                setStandby(standby);
                            }
                        }
                    }
                });
    }

    /**
     * Forces the standby state of the intro part.
     * 
     * @param standby update the standby state
     */
    public void setStandby(final boolean standby) {
        final Control control = ((PartSite) getSite()).getPane().getControl();
        BusyIndicator.showWhile(control.getDisplay(), new Runnable() {
            public void run() {
                try {
                    control.setRedraw(false);
                    introPart.standbyStateChanged(standby);
                } finally {
                    control.setRedraw(true);
                }

                setBarVisibility(standby);
            }
        });
    }

    /**
     * Toggles handling of zoom events.
     * 
     * @param handle whether to handle zoom events
     */
    public void setHandleZoomEvents(boolean handle) {
        handleZoomEvents = handle;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        addPaneListener();
        introPart.createPartControl(parent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {
    	setBarVisibility(true);
        super.dispose();
        getSite().getWorkbenchWindow().getWorkbench().getIntroManager()
                .closeIntro(introPart);
        introPart.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        return introPart.getAdapter(adapter);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#getTitleImage()
     */
    public Image getTitleImage() {
        return introPart.getTitleImage();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site);
        Workbench workbench = (Workbench) site.getWorkbenchWindow()
                .getWorkbench();
        try {
            introPart = workbench.getWorkbenchIntroManager()
                    .createNewIntroPart();
            introPart.addPropertyListener(new IPropertyListener() {
                public void propertyChanged(Object source, int propId) {
                    firePropertyChange(propId);
                }
            });
            introSite = new ViewIntroAdapterSite(site, workbench
                    .getIntroDescriptor());
            introPart.init(introSite, memento);
        } catch (CoreException e) {
            WorkbenchPlugin
                    .log(
                            IntroMessages
                                    .getString("Intro.could_not_create_proxy"), new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, IntroMessages.getString("Intro.could_not_create_proxy"), e)); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        introPart.setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
        introPart.saveState(memento);
    }

	/**
	 * Sets whether the CoolBar/PerspectiveBar should be visible.
	 * 
	 * @param visible whether the CoolBar/PerspectiveBar should be visible
	 * @since 3.1
	 */
	private void setBarVisibility(final boolean visible) {
		WorkbenchWindow window = (WorkbenchWindow) getSite()
				.getWorkbenchWindow();

		final boolean layout = (visible != window.getCoolBarVisible())
				|| (visible != window.getPerspectiveBarVisible()); // don't layout unless things have actually changed
		if (visible) {
			window.setCoolBarVisible(true);
			window.setPerspectiveBarVisible(true);
		} else {
			window.setCoolBarVisible(false);
			window.setPerspectiveBarVisible(false);
		}

		if (layout)
			window.getShell().layout();
	}
}