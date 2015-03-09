/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.intro.IntroMessages;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Simple view that will wrap an <code>IIntroPart</code>.
 *
 * @since 3.0
 */
public final class ViewIntroAdapterPart extends ViewPart {

    private IIntroPart introPart;

    private IIntroSite introSite;

    private boolean handleZoomEvents = true;

	private IEventBroker eventBroker;

	private EventHandler zoomChangeListener = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			if (!handleZoomEvents)
				return;

			Object changedObj = event.getProperty(EventTags.ELEMENT);
			if (!(changedObj instanceof MPartStack))
				return;

			if (changedObj != getIntroStack())
				return;

			if (UIEvents.isADD(event)
					&& UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
							IPresentationEngine.MAXIMIZED)) {
				setStandby(false);
			} else if (UIEvents.isREMOVE(event)
					&& UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE,
							IPresentationEngine.MAXIMIZED)) {
				setStandby(true);
			}
		}
	};


    /**
     * Adds a listener that toggles standby state if the view pane is zoomed.
     */
    private void addZoomListener() {
		ViewSite site = (ViewSite) getViewSite();
		MPart introModelPart = site.getModel();
		if (introModelPart == null || introModelPart.getContext() == null)
			return;

		eventBroker = introModelPart.getContext().get(IEventBroker.class);
		eventBroker.subscribe(UIEvents.ApplicationElement.TOPIC_TAGS, zoomChangeListener);
    }

	private MPartStack getIntroStack() {
		ViewSite site = (ViewSite) getViewSite();

		MPart introModelPart = site.getModel();
		MUIElement introPartParent = introModelPart.getCurSharedRef().getParent();
		if (introPartParent instanceof MPartStack)
			return (MPartStack) introPartParent;

		return null;
	}

    /**
     * Forces the standby state of the intro part.
     *
     * @param standby update the standby state
     */
    public void setStandby(final boolean standby) {
		final Control control = (Control) ((PartSite) getSite()).getModel().getWidget();
        BusyIndicator.showWhile(control.getDisplay(), new Runnable() {
            @Override
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

    @Override
	public void createPartControl(Composite parent) {
        addZoomListener();
        introPart.createPartControl(parent);

		ViewSite site = (ViewSite) getViewSite();
		MPart introModelPart = site.getModel();
		if (introModelPart.getCurSharedRef() != null) {
			MUIElement parentElement = introModelPart.getCurSharedRef().getParent();
			if (parentElement instanceof MPartStack) {
				setStandby(!parentElement.getTags().contains(IPresentationEngine.MAXIMIZED));
			}
		}
    }

    @Override
	public void dispose() {
		eventBroker.unsubscribe(zoomChangeListener);

    	setBarVisibility(true);
        super.dispose();
        getSite().getWorkbenchWindow().getWorkbench().getIntroManager()
                .closeIntro(introPart);
        introPart.dispose();
    }

    @Override
	public <T> T getAdapter(Class<T> adapter) {
        return introPart.getAdapter(adapter);
    }

    @Override
	public Image getTitleImage() {
        return introPart.getTitleImage();
    }

    @Override
	public String getTitle() {
    	// this method is called eagerly before our init method is called (and
    	// therefore before our intropart is created).  By default return
    	// the view title from the view declaration.  We will fire a property
    	// change to set the title to the proper value in the init method.
    	return introPart == null ? super.getTitle() : introPart.getTitle();
    }

    @Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site);
        Workbench workbench = (Workbench) site.getWorkbenchWindow()
                .getWorkbench();
        try {
            introPart = workbench.getWorkbenchIntroManager()
                    .createNewIntroPart();
            // reset the part name of this view to be that of the intro title
            setPartName(introPart.getTitle());
            introPart.addPropertyListener(new IPropertyListener() {
                @Override
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
                            IntroMessages.Intro_could_not_create_proxy, new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, IntroMessages.Intro_could_not_create_proxy, e));
        }
    }

    @Override
	public void setFocus() {
        introPart.setFocus();
    }

    @Override
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

		boolean layout = false; // don't layout unless things have actually changed
		if (visible) {
			// Restore the last 'saved' state
			boolean coolbarVisible = PrefUtil
					.getInternalPreferenceStore().getBoolean(
							IPreferenceConstants.COOLBAR_VISIBLE);
			boolean persBarVisible = PrefUtil
					.getInternalPreferenceStore().getBoolean(
							IPreferenceConstants.PERSPECTIVEBAR_VISIBLE);
			layout = (coolbarVisible != window.getCoolBarVisible())
				|| (persBarVisible != window.getPerspectiveBarVisible());
			window.setCoolBarVisible(coolbarVisible);
			window.setPerspectiveBarVisible(persBarVisible);
		} else {
			layout = !window.getCoolBarVisible() || !window.getPerspectiveBarVisible();
			window.setCoolBarVisible(false);
			window.setPerspectiveBarVisible(false);
		}

		if (layout) {
			window.getShell().layout();
		}
	}
}
