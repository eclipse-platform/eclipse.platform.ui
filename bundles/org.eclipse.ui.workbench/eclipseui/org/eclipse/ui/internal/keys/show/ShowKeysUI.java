/*******************************************************************************
 * Copyright (c) 2019 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Georgi (SAP SE) - Bug 540440
 *******************************************************************************/
package org.eclipse.ui.internal.keys.show;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Manages opening and closing of the popup for a given command. Keeps track of
 * opened popups and only allows one at a time.
 */
public class ShowKeysUI implements IDisposable {

	private IPreferenceStore preferenceStore;
	private IServiceLocator serviceLocator;
	private ShowKeysPopup shortcutPopup;

	public ShowKeysUI(IServiceLocator serviceLocator, IPreferenceStore preferenceStore) {
		this.serviceLocator = serviceLocator;
		this.preferenceStore = preferenceStore;
	}

	public void open(String commandId, Event trigger) {
		open(commandId, null, trigger, false);
	}

	public void openForPreview(String commandId, String description) {
		open(commandId, description, null, true);
	}

	@Override
	public void dispose() {
		closePopup();
	}

	private void open(String commandId, String description, Event trigger, boolean force) {
		String formattedShortcut = getFormattedShortcut(trigger, commandId);
		// no UI for commands w/o key binding, unless explicitly specified
		if (formattedShortcut == null && !force) {
			return;
		}
		try {
			ICommandService cmdService = this.serviceLocator.getService(ICommandService.class);
			Command command = cmdService.getCommand(commandId);
			if (!command.isHandled() || !command.isEnabled()) {
				return;
			}
			String name = command.getName();
			if (description == null) {
				description = command.getDescription();
			}
			openPopup(formattedShortcut, name, description);
		} catch (NotDefinedException ignore) {
		}
	}

	private String getFormattedShortcut(Event trigger, String commandId) {
		if (trigger != null) {
			int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(trigger);
			KeyStroke keyStroke = SWTKeySupport.convertAcceleratorToKeyStroke(accelerator);
			IBindingService bindingService = serviceLocator.getService(IBindingService.class);
			// keyboard trigger
			if (preferenceStore.getBoolean(IPreferenceConstants.SHOW_KEYS_ENABLED_FOR_KEYBOARD)
					&& KeyStroke.NO_KEY != keyStroke.getNaturalKey()) {
				// return a binding that completes on the same keystroke (no guarantee it's the
				// right one that was actually used, but usually correct enough)
				return Arrays.stream(bindingService.getActiveBindingsFor(commandId)) //
						.filter(binding -> {
							Trigger[] triggers = binding.getTriggers();
							Trigger lastTrigger = triggers[triggers.length - 1];
							return lastTrigger.equals(keyStroke);
						}).findFirst() //
						.map(TriggerSequence::format) //
						.orElse(null);
			}
			// mouse-triggered event
			else if (preferenceStore.getBoolean(IPreferenceConstants.SHOW_KEYS_ENABLED_FOR_MOUSE_EVENTS)
					&& KeyStroke.NO_KEY == keyStroke.getNaturalKey()) {
				return bindingService.getBestActiveBindingFormattedFor(commandId);
			}
		}
		return null;
	}

	private void openPopup(String shortcut, String shortcutText, String shortcutDescription) {
		// Schedule the UI opening in the event loop. This allows having the popup on
		// top of whatever UI is opened right now. E.g. we can now draw on top of the
		// Quick Access UI rather than being hidden underneath it.
		Display.getDefault().asyncExec(() -> {
			closePopup();
			int timeToClose = this.preferenceStore.getInt(IPreferenceConstants.SHOW_KEYS_TIME_TO_CLOSE);
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			this.shortcutPopup = new ShowKeysPopup(shell, timeToClose);
			this.shortcutPopup.setShortcut(shortcut, shortcutText, shortcutDescription);
			this.shortcutPopup.open();
		});
	}

	private void closePopup() {
		if (this.shortcutPopup != null) {
			this.shortcutPopup.close();
			this.shortcutPopup = null;
		}
	}

	/**
	 * Lightweight popup to a shortcut plus a description
	 */
	private static class ShowKeysPopup extends Window {

		private static final String POPUP_COLOR_BG = PlatformUI.PLUGIN_ID + ".showkeys.backgroundColor"; //$NON-NLS-1$
		private static final String POPUP_COLOR_FG = PlatformUI.PLUGIN_ID + ".showkeys.foregroundColor"; //$NON-NLS-1$
		private static final int POPUP_FONT_SIZEFACTOR_KEY_LABEL = 2;
		private static final int POPUP_FONT_SIZEFACTOR_KEY = POPUP_FONT_SIZEFACTOR_KEY_LABEL + 1;
		private static final int MARGIN_BOTTOM = 25;
		private static final String keysPageId = "org.eclipse.ui.preferencePages.Keys"; //$NON-NLS-1$

		private final List<Resource> resources = new ArrayList<>(3);
		private final int timeToClose;
		private String shortcut;
		private String shortcutText;
		private String shortcutDescription;
		private boolean readyToClose = true;

		public ShowKeysPopup(Shell parentShell, int timeToClose) {
			super(parentShell);
			this.timeToClose = timeToClose;
			setShellStyle((SWT.NO_TRIM | SWT.ON_TOP | SWT.TOOL) & ~SWT.APPLICATION_MODAL);
		}

		public void setShortcut(String shortcut, String shortcutText, String shortcutDescription) {
			this.shortcut = shortcut;
			this.shortcutText = shortcutText;
			this.shortcutDescription = shortcutDescription;
		}

		@Override
		public int open() {
			scheduleClose();

			Shell shell = getShell();
			if (shell == null || shell.isDisposed()) {
				shell = null;
				// create the window
				create();
				shell = getShell();
			}

			// limit the shell size to the display size
			constrainShellSize();

			shell.setVisible(true);

			return OK;
		}

		private void scheduleClose() {
			this.readyToClose = true;
			Display.getDefault().timerExec(this.timeToClose, () -> {
				if (ShowKeysPopup.this.readyToClose && getShell() != null && !getShell().isDisposed()) {
					close();
				}
			});
		}

		@Override
		public boolean close() {
			boolean closed = super.close();

			for (Resource resource : this.resources) {
				resource.dispose();
			}
			this.resources.clear();

			return closed;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);

			Color color = JFaceResources.getColorRegistry().get(POPUP_COLOR_BG);
			newShell.setBackground(color);
			newShell.setAlpha(170);
		}

		@Override
		protected Control createContents(Composite parent) {
			Font font = JFaceResources.getDialogFont();
			FontData[] defaultFontData = font.getFontData();
			Color foregroundColor = JFaceResources.getColorRegistry().get(POPUP_COLOR_FG);

			Composite contents = new Composite(parent, SWT.NONE);
			GridLayoutFactory.swtDefaults().applyTo(contents);
			contents.setBackground(parent.getBackground());
			hookDoubleClickListener(contents);

			String primaryText = null;
			if (shortcut != null && shortcutText != null) {
				primaryText = shortcut + " – " + shortcutText; //$NON-NLS-1$
			} else if (shortcut != null) {
				primaryText = shortcut;
			} else if (shortcutText != null) {
				primaryText = shortcutText;
			}
			if (primaryText != null) {
				Label shortcutLabel = new Label(contents, SWT.CENTER);
				GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(shortcutLabel);
				FontData fontData = new FontData(defaultFontData[0].getName(),
						defaultFontData[0].getHeight() * POPUP_FONT_SIZEFACTOR_KEY, SWT.BOLD);
				Font shortcutFont = new Font(getShell().getDisplay(), fontData);
				this.resources.add(shortcutFont);
				shortcutLabel.setBackground(parent.getBackground());
				shortcutLabel.setForeground(foregroundColor);
				shortcutLabel.setFont(shortcutFont);
				shortcutLabel.setText(primaryText);
				hookDoubleClickListener(shortcutLabel);
			}
			if (shortcutDescription != null) {
				Label shortcutDescriptionLabel = new Label(contents, SWT.CENTER);
				GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(shortcutDescriptionLabel);
				FontData fontData = new FontData(defaultFontData[0].getName(),
						(int) (defaultFontData[0].getHeight() * 1.3), SWT.NORMAL);
				Font shortcutFont = new Font(getShell().getDisplay(), fontData);
				this.resources.add(shortcutFont);
				shortcutDescriptionLabel.setFont(shortcutFont);
				shortcutDescriptionLabel.setBackground(parent.getBackground());
				shortcutDescriptionLabel.setForeground(foregroundColor);
				shortcutDescriptionLabel.setText(shortcutDescription);
				hookDoubleClickListener(shortcutDescriptionLabel);
			}

			return contents;
		}

		private void hookDoubleClickListener(Control control) {
			control.addListener(SWT.MouseDoubleClick, e -> {
				PreferencesUtil.createPreferenceDialogOn(getParentShell(), keysPageId, null, null).open();
			});
		}

		@Override
		protected Point getInitialLocation(Point initialSize) {
			Composite parent = getShell().getParent();
			Rectangle parentBounds = parent.getBounds();

			Monitor monitor = parent.getMonitor();
			Rectangle monitorBounds = monitor.getClientArea();

			Point centerPoint = Geometry.centerPoint(parent.getBounds());

			return new Point(centerPoint.x - (initialSize.x / 2), //
					Math.max(monitorBounds.y, //
							parentBounds.y + parentBounds.height - (initialSize.y) - MARGIN_BOTTOM));
		}
	}

}
