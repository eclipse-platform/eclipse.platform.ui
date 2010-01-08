/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.statushandlers;

import java.util.Map;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.DialogTray;
import org.eclipse.jface.dialogs.ErrorSupportProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.statushandlers.AbstractStatusAreaProvider;
import org.eclipse.ui.statushandlers.StatusAdapter;


/**
 * This class is responsible for displaying the support area on the right
 * side of the status dialog.
 */
public class SupportTray extends DialogTray implements
		ISelectionChangedListener {

	private Map dialogState;


	/**
	 * @param dialogState
	 * @param listener
	 */
	public SupportTray(Map dialogState, Listener listener) {
		this.closeListener = listener;
		this.dialogState = dialogState;
		this.hideSupportButtons = getBooleanValue(IStatusDialogConstants.HIDE_SUPPORT_BUTTON);
		this.lastSelectedStatus = getCurrentStatusAdapter();
	}

	private IContributionItem closeAction;
	private Listener closeListener;
	private boolean hideSupportButtons;
	private Image normal;
	private Image hover;

	/**
	 * This composite occupies the whole space that is available to the support
	 * tray. It has hardcoded layout behavior to protect the dialog.
	 */
	private Composite supportArea;
	private Composite supportAreaContent;

	private StatusAdapter lastSelectedStatus;

	private AbstractStatusAreaProvider userSupportProvider;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogTray#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		// nothing to display. Should never happen, cause button is disabled
		// when nothing to display.

		if (providesSupport(getCurrentStatusAdapter()) == null
				&& getBooleanValue(IStatusDialogConstants.TRAY_OPENED)) {

		}

		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		container.setLayout(layout);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		container.setLayoutData(layoutData);

		container.addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event event) {
				destroyImages();
			}
		});

		if (!hideSupportButtons) {
			ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
			toolBarManager.createControl(container);
			GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
			gd.grabExcessHorizontalSpace = true;
			toolBarManager.getControl().setLayoutData(gd);
			Label separator = new Label(container, SWT.SEPARATOR
					| SWT.HORIZONTAL);
			gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gd.heightHint = 1;
			separator.setLayoutData(gd);

			createActions();
			toolBarManager.add(closeAction);

			toolBarManager.update(true);
		}

		supportArea = new Composite(container, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		supportArea.setLayout(layout);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		supportArea.setLayoutData(gd);

		if (lastSelectedStatus != null)
			createSupportArea(supportArea, lastSelectedStatus);

		Point shellSize = supportArea.getShell().getSize();
		Point desiredSize = supportArea.getShell().computeSize(SWT.DEFAULT,
				SWT.DEFAULT);
		
		if(desiredSize.y > shellSize.y){
			supportArea.getShell().setSize(shellSize.x,
					Math.min(desiredSize.y, 500));
		}

		return container;
	}

	/**
	 * Creates any custom needed by the tray, such as the close button.
	 */
	private void createImages() {
		Display display = Display.getCurrent();
		int[] shape = new int[] { 3, 3, 5, 3, 7, 5, 8, 5, 10, 3, 12, 3, 12,
				5, 10, 7, 10, 8, 12, 10, 12, 12, 10, 12, 8, 10, 7, 10, 5,
				12, 3, 12, 3, 10, 5, 8, 5, 7, 3, 5 };

		/*
		 * Use magenta as transparency color since it is used infrequently.
		 */
		Color border = display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
		Color background = display
				.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		Color backgroundHot = new Color(display, new RGB(252, 160, 160));
		Color transparent = display.getSystemColor(SWT.COLOR_MAGENTA);

		PaletteData palette = new PaletteData(new RGB[] {
				transparent.getRGB(), border.getRGB(), background.getRGB(),
				backgroundHot.getRGB() });
		ImageData data = new ImageData(16, 16, 8, palette);
		data.transparentPixel = 0;

		normal = new Image(display, data);
		normal.setBackground(transparent);
		GC gc = new GC(normal);
		gc.setBackground(background);
		gc.fillPolygon(shape);
		gc.setForeground(border);
		gc.drawPolygon(shape);
		gc.dispose();

		hover = new Image(display, data);
		hover.setBackground(transparent);
		gc = new GC(hover);
		gc.setBackground(backgroundHot);
		gc.fillPolygon(shape);
		gc.setForeground(border);
		gc.drawPolygon(shape);
		gc.dispose();

		backgroundHot.dispose();
	}
	
	/**
	 * Creates any actions needed by the tray.
	 */
	private void createActions() {
		createImages();
		closeAction = new ContributionItem() {
			public void fill(ToolBar parent, int index) {
				final ToolItem item = new ToolItem(parent, SWT.PUSH);
				item.setImage(normal);
				item.setHotImage(hover);
				item.setToolTipText(JFaceResources.getString("close")); //$NON-NLS-1$
				item.addListener(SWT.Selection, closeListener);
			}
		};
	}

	private void destroyImages() {
		if (normal != null) normal.dispose();
		if (hover != null) hover.dispose();
	}

	/**
	 * Create the area for extra error support information.
	 * 
	 * @param parent
	 *            A composite on which should be the support area created.
	 * @param statusAdapter
	 *            StatusAdapter for which should be the support area
	 *            created.
	 */
	private void createSupportArea(Composite parent,
			StatusAdapter statusAdapter) {

		ErrorSupportProvider provider = getSupportProvider();

		// default support area was disabled
		if (provider == null)
			return;

		if (supportAreaContent != null)
			supportAreaContent.dispose();

		supportAreaContent = new Composite(parent, SWT.FILL);

		GridData supportData = new GridData(SWT.FILL, SWT.FILL, true, true);
		supportAreaContent.setLayoutData(supportData);
		if (supportAreaContent.getLayout() == null) {
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			supportAreaContent.setLayout(layout); // Give it a default
			// layout
		}

		if (provider instanceof AbstractStatusAreaProvider) {
			((AbstractStatusAreaProvider) provider).createSupportArea(
					supportAreaContent, statusAdapter);
		} else {
			provider.createSupportArea(supportAreaContent, statusAdapter
					.getStatus());
		}
	}

	public ErrorSupportProvider getSupportProvider() {
		ErrorSupportProvider provider = Policy.getErrorSupportProvider();

		if (userSupportProvider != null) {
			provider = userSupportProvider;
		}

		if (getBooleanValue(IStatusDialogConstants.ENABLE_DEFAULT_SUPPORT_AREA) && provider == null) {
			provider = new StackTraceSupportArea();
		}
		return provider;
	}

	private StatusAdapter getStatusAdapterFromEvent(
			SelectionChangedEvent event) {

		ISelection selection = event.getSelection();

		if (selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			Object element = structuredSelection.getFirstElement();
			if (element instanceof StatusAdapter) {
				return (StatusAdapter) element;
			}
		}
		return null;
	}

	/**
	 * Checks if the support dialog has any support areas.
	 * 
	 * @param adapter
	 *            - a parameter for which we area checking the status adapter
	 * @return true if support dialog has any support areas to display, false
	 *         otherwise
	 */
	public ErrorSupportProvider providesSupport(StatusAdapter adapter) {
		ErrorSupportProvider provider = getSupportProvider();
		if (provider instanceof AbstractStatusAreaProvider) {
			AbstractStatusAreaProvider areaProvider = (AbstractStatusAreaProvider) provider;
			if (areaProvider.validFor(adapter)) {
				return areaProvider;
			}
			return null;
		}
		return provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		lastSelectedStatus = getStatusAdapterFromEvent(event);
		if (supportArea != null && !supportArea.isDisposed()) {
			if (lastSelectedStatus != null) {
				createSupportArea(supportArea, lastSelectedStatus);
				supportArea.layout(true);
			}
		}
	}

	/**
	 * @return Returns the supportArea.
	 */
	public Composite getSupportArea() {
		return supportArea;
	}

	private boolean getBooleanValue(Object key) {
		Boolean b = (Boolean) dialogState.get(key);
		if (b == null) {
			return false;
		}
		return b.booleanValue();
	}

	private StatusAdapter getCurrentStatusAdapter() {
		return (StatusAdapter) dialogState
				.get(IStatusDialogConstants.CURRENT_STATUS_ADAPTER);
	}
}