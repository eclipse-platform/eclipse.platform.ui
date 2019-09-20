/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Hochstein (Freescale) - Bug 393703: NotHandledException selecting inactive command under 'Previous Choices' in Quick access
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 428050, 472654
 *     Brian de Alwis - Fix size computation to account for trim
 *     Markus Kuppe <bugs.eclipse.org@lemmster.de> - Bug 449485: [QuickAccess] "Widget is disposed" exception in errorlog during shutdown due to quickaccess.SearchField.storeDialog
 *     Elena Laskavaia <elaskavaia.cdt@gmail.com> - Bug 433746: [QuickAccess] SWTException on closing quick access shell
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 488926, 491278, 491291, 491312, 491293, 436788, 513436
 ******************************************************************************/
package org.eclipse.ui.internal.quickaccess;

import java.util.Arrays;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.bindings.internal.ContextSet;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.keys.IBindingService;

public class SearchField {

	private static final String QUICK_ACCESS_COMMAND_ID = "org.eclipse.ui.window.quickAccess"; //$NON-NLS-1$

	private ToolItem quickAccessButton;
	private Display display;
	private ParameterizedCommand quickAccessCommand;
	private TriggerSequence triggerSequence = null;

	private Listener previousFocusListener = e -> {
		if (e.widget instanceof Control && e.widget != quickAccessButton) {
			previousFocusControl = (Control) e.widget;
		}
	};
	private Control previousFocusControl;

	@PostConstruct
	void createControls(final Composite parent) {
		this.quickAccessCommand = eCommandService.createCommand(QUICK_ACCESS_COMMAND_ID, null);

		display = parent.getDisplay();
		display.addFilter(SWT.FocusIn, previousFocusListener);
		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setSize(SWT.DEFAULT, 32);
		GridLayoutFactory.swtDefaults().margins(3, 3).applyTo(comp);
		updateQuickAccessTriggerSequence();
		quickAccessButton = createQuickAccessToolbar(comp);
		updateQuickAccessText();

		quickAccessButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			// release mouse button = click = CTRL+3 -> activate QuickAccess
			activate(previousFocusControl);
		}));
	}

	@Inject
	@Optional
	protected void keybindingPreferencesChanged(
			@SuppressWarnings("restriction") @Preference(nodePath = "org.eclipse.ui.workbench", value = "org.eclipse.ui.commands") String preferenceValue) {
		if (preferenceValue != null) {
			updateQuickAccessText();
		}

	}

	@Inject
	private BindingTableManager manager;
	@Inject
	private ECommandService eCommandService;
	@Inject
	private IContextService contextService;
	@Inject
	private IBindingService bindingService;
	@Inject
	private ICommandImageService commandImageService;

	/**
	 * Compute the best binding for the command and sets the trigger
	 *
	 */
	protected void updateQuickAccessTriggerSequence() {
		triggerSequence = bindingService.getBestActiveBindingFor(QUICK_ACCESS_COMMAND_ID);
		// FIXME Bug 491701 - [KeyBinding] get best active binding is not working
		if (triggerSequence == null) {
			ContextSet contextSet = manager.createContextSet(Arrays.asList(contextService.getDefinedContexts()));
			Binding binding = manager.getBestSequenceFor(contextSet, quickAccessCommand);
			triggerSequence = (binding == null) ? null : binding.getTriggerSequence();
		}
	}

	private ToolItem createQuickAccessToolbar(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.marginLeft = layout.marginRight = 8;
		layout.marginBottom = 0;
		layout.marginTop = 0;
		comp.setLayout(layout);
		ToolBar toolbar = new ToolBar(comp, SWT.FLAT | SWT.WRAP | SWT.RIGHT);

		ToolItem quickAccessToolItem = new ToolItem(toolbar, SWT.PUSH);

		try {
			quickAccessToolItem.setText(quickAccessCommand.getName());
		} catch (NotDefinedException e) {
			WorkbenchPlugin.log(e);
		}
		ImageDescriptor imageDescriptor = commandImageService.getImageDescriptor(quickAccessCommand.getId());
		if (imageDescriptor != null) {
			Image image = imageDescriptor.createImage();
			quickAccessToolItem.setImage(image);
			quickAccessToolItem.addDisposeListener(e -> image.dispose());
		}
		toolbar.addMenuDetectListener(e -> {
			if (toolbar.getMenu() == null) {
				toolbar.setMenu(parent.getMenu());
				e.doit = true;
			}
		});
		return quickAccessToolItem;
	}

	private void updateQuickAccessText() {
		if (quickAccessButton == null || quickAccessButton.isDisposed()) {
			return;
		}
		updateQuickAccessTriggerSequence();

		if (triggerSequence != null) {
			quickAccessButton.setToolTipText(
					NLS.bind(QuickAccessMessages.QuickAccess_TooltipDescription, triggerSequence.format()));
		} else {
			quickAccessButton.setToolTipText(QuickAccessMessages.QuickAccess_TooltipDescription_Empty);
		}
		quickAccessButton.getParent().requestLayout();

	}

	public void activate(final Control previousFocusControl) {
		final QuickAccessDialog quickAccessDialog = new QuickAccessDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow(), null);
		quickAccessDialog.getShell().addDisposeListener(e -> {
			if (previousFocusControl != null && !previousFocusControl.isDisposed()) {
				previousFocusControl.setFocus();
			} else {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window != null) {
					window.getShell().setFocus();
				}
			}
		});
		quickAccessDialog.open();
	}

	@PreDestroy
	void dispose() {
		if (display != null && !display.isDisposed()) {
			display.removeFilter(SWT.FocusIn, previousFocusListener);
		}
	}

}
