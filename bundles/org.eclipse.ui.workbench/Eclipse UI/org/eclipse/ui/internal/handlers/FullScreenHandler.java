/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others. All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Simon Scholz <simon.scholz@vogella.com> - initial API and implementation;
 * 	Patrik Suzzi <psuzzi@gmail.com> - Bug 491572, 491785, 492749
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.Arrays;
import java.util.Optional;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.bindings.internal.ContextSet;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.keys.IBindingService;

/**
 * Handler, which enables a full screen mode.
 *
 * @since 3.5
 *
 */
public class FullScreenHandler extends AbstractHandler {

	private static final String FULL_SCREEN_COMMAND_ID = "org.eclipse.ui.window.fullscreenmode"; //$NON-NLS-1$
	private static final String FULL_SCREEN_COMMAND_DO_NOT_SHOW_INFO_AGAIN_PREF_ID = "org.eclipse.ui.window.fullscreenmode.donotshowinfoagain"; //$NON-NLS-1$

	private boolean showInfoPopup;

	private int timeLastEvent;
	private FullScreenInfoPopup fullScreenInfoPopup;

	@Override
	public Object execute(ExecutionEvent event) {
		// 493186 skips execution of duplicated event
		if (checkDuplicatedEvent(event)) {
			return null;
		}
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		Shell shell = window.getShell();
		IBindingService bindingService = window.getService(IBindingService.class);
		ECommandService commandService = window.getService(ECommandService.class);
		BindingTableManager bindingTableManager = window.getService(BindingTableManager.class);
		IContextService bindingContextService = window.getService(IContextService.class);

		showInfoPopup = !WorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(FULL_SCREEN_COMMAND_DO_NOT_SHOW_INFO_AGAIN_PREF_ID);

		Optional<TriggerSequence> sequence = getKeybindingSequence(bindingService, commandService, bindingTableManager,
				bindingContextService, FULL_SCREEN_COMMAND_ID);

		String keybinding = sequence.map(t -> t.format()).orElse(""); //$NON-NLS-1$

		shell.setFullScreen(!shell.getFullScreen());

		if (shell.getFullScreen()) {
			String message = WorkbenchMessages.ToggleFullScreenMode_ActivationPopup_Description_NoKeybinding;
			if (!keybinding.isEmpty()) {
				message = NLS.bind(WorkbenchMessages.ToggleFullScreenMode_ActivationPopup_Description, keybinding);
			}
			if (showInfoPopup) {
				fullScreenInfoPopup = new FullScreenInfoPopup(shell, PopupDialog.HOVER_SHELLSTYLE, true, false,
						false, false, false, null, null, message);
				fullScreenInfoPopup.open();
			}
		} else {
			if (fullScreenInfoPopup != null) {
				fullScreenInfoPopup.close();
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Check if an event is duplicate, by recording and comparing the time of
	 * the trigger event. Returns true if an event is triggered twice with an
	 * event with the same time
	 */
	boolean checkDuplicatedEvent(ExecutionEvent event) {
		if (event != null && event.getTrigger() != null && event.getTrigger() instanceof Event) {
			int time = ((Event) event.getTrigger()).time;
			if (time == timeLastEvent) {
				return true;
			}
			timeLastEvent = time;
		}
		return false;
	}

	private static class FullScreenInfoPopup extends PopupDialog {

		private String message;
		private String messageDoNotShowAgain;

		public FullScreenInfoPopup(Shell parent, int shellStyle, boolean takeFocusOnOpen, boolean persistSize,
				boolean persistLocation, boolean showDialogMenu, boolean showPersistActions, String titleText,
				String infoText, String message) {
			super(parent, shellStyle, takeFocusOnOpen, persistSize, persistLocation, showDialogMenu, showPersistActions,
					titleText, infoText);
			this.message = message;
			this.messageDoNotShowAgain = WorkbenchMessages.ToggleFullScreenMode_ActivationPopup_DoNotShowAgain;
		}

		@Override
		protected Point getInitialLocation(Point initialSize) {
			if (getShell().getParent() == null) {
				return super.getInitialLocation(initialSize);
			}
			Rectangle bounds = getShell().getParent().getMonitor().getBounds();
			GC gc = new GC(getShell().getDisplay());
			int textExtendX = gc.textExtent(message).x;
			gc.dispose();

			return new Point(bounds.x + bounds.width / 2 - textExtendX / 2, bounds.y + bounds.height / 5);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			Label label = new Label(composite, SWT.NONE);
			label.setText(message);
			GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
			gd.horizontalIndent = PopupDialog.POPUP_HORIZONTALSPACING;
			gd.verticalIndent = PopupDialog.POPUP_VERTICALSPACING;
			label.setLayoutData(gd);

			Button btnDoNotShow = new Button(composite, SWT.CHECK);
			btnDoNotShow.setText(messageDoNotShowAgain);
			btnDoNotShow.setSelection(WorkbenchPlugin.getDefault().getPreferenceStore()
					.getBoolean(FULL_SCREEN_COMMAND_DO_NOT_SHOW_INFO_AGAIN_PREF_ID));
			GridData gd2 = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
			gd2.horizontalIndent = PopupDialog.POPUP_HORIZONTALSPACING;
			gd2.verticalIndent = PopupDialog.POPUP_VERTICALSPACING;
			btnDoNotShow.setLayoutData(gd2);

			composite.addDisposeListener((e) -> {
				WorkbenchPlugin.getDefault().getPreferenceStore()
						.setValue(FULL_SCREEN_COMMAND_DO_NOT_SHOW_INFO_AGAIN_PREF_ID, btnDoNotShow.getSelection());
			});

			return composite;
		}

	}

	protected Optional<TriggerSequence> getKeybindingSequence(IBindingService bindingService,
			ECommandService eCommandService, BindingTableManager bindingTableManager, IContextService contextService,
			String commandId) {
		TriggerSequence triggerSequence = bindingService.getBestActiveBindingFor(commandId);
		// FIXME Bug 491701 - [KeyBinding] get best active binding is not
		// working
		if (triggerSequence == null) {
			ParameterizedCommand cmd = eCommandService.createCommand(commandId, null);
			ContextSet contextSet = bindingTableManager
					.createContextSet(Arrays.asList(contextService.getDefinedContexts()));
			Binding binding = bindingTableManager.getBestSequenceFor(contextSet, cmd);
			if (binding != null) {
				triggerSequence = binding.getTriggerSequence();
			}
		}
		return Optional.ofNullable(triggerSequence);
	}

}
