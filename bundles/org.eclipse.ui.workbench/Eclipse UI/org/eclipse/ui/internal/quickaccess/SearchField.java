/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.quickaccess;

import javax.annotation.PostConstruct;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.IHandlerService;

public class SearchField {
	Shell shell;
	private QuickAccessContents quickAccessContents;
	private MWindow window;

	@PostConstruct
	void createWidget(final Composite parent, MWindow window) {
		this.window = window;
		// borderColor = new Color(parent.getDisplay(), 170, 176, 191);
		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		final Text text = new Text(comp, SWT.SEARCH | SWT.ICON_SEARCH);
		GridDataFactory.fillDefaults().hint(130, SWT.DEFAULT).applyTo(text);
		text.setMessage(QuickAccessMessages.QuickAccess_EnterSearch);

		final CommandProvider commandProvider = new CommandProvider();
		QuickAccessProvider[] providers = new QuickAccessProvider[] { new EditorProvider(),
				new ViewProvider(), new PerspectiveProvider(), commandProvider,
				new ActionProvider(), new WizardProvider(), new PreferenceProvider(),
				new PropertiesProvider() };
		quickAccessContents = new QuickAccessContents(providers) {
			void updateFeedback(boolean filterTextEmpty, boolean showAllMatches) {
			}

			void doClose() {
			}

			QuickAccessElement getPerfectMatch(String filter) {
				return null;
			}

			void handleElementSelected(String string, Object selectedElement) {
				if (selectedElement instanceof QuickAccessElement) {
					QuickAccessElement element = (QuickAccessElement) selectedElement;
					text.setText(""); //$NON-NLS-1$
					element.execute();
				}
			}
		};
		quickAccessContents.hookFilterText(text);
		shell = new Shell(parent.getShell(), SWT.RESIZE | SWT.ON_TOP);
		shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				text.setText(""); //$NON-NLS-1$
				e.doit = false;
			}
		});
		GridLayoutFactory.fillDefaults().applyTo(shell);
		final Table table = quickAccessContents.createTable(shell, Window.getDefaultOrientation());
		text.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				checkFocusLost(table, text);
			}

			public void focusGained(FocusEvent e) {
				IHandlerService hs = SearchField.this.window.getContext()
						.get(IHandlerService.class);
				if (commandProvider.getContextSnapshot() == null) {
					commandProvider.setSnapshot(hs.createContextSnapshot(true));
				}
			}
		});
		shell.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				checkFocusLost(table, text);
			}

			public void focusGained(FocusEvent e) {
			}
		});
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				boolean wasVisible = shell.getVisible();
				boolean nowVisible = text.getText().length() > 0;
				if (!wasVisible && nowVisible) {
					Rectangle tempBounds = comp.getBounds();
					Rectangle compBounds = e.display.map(comp, null, tempBounds);
					Rectangle monitorBounds = comp.getMonitor().getBounds();
					int width = Math.max(350, compBounds.width);
					int height = 250;

					if (compBounds.x + width > monitorBounds.width) {
						compBounds.x = monitorBounds.width - width;
					}
					
					if (compBounds.y + height > monitorBounds.height) {
						compBounds.y = compBounds.y - tempBounds.height - height;
					}
					
					shell.setBounds(compBounds.x, compBounds.y + compBounds.height, width, height);
					shell.layout();
				}
				shell.setVisible(nowVisible);
			}
		});
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					text.setText(""); //$NON-NLS-1$
				}
			}
		});


	}

	/**
	 * @param table
	 * @param text
	 */
	protected void checkFocusLost(final Table table, final Text text) {
		table.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!table.isDisposed() && !text.isDisposed()) {
					if (!table.isFocusControl() && !text.isFocusControl()) {
						text.setText(""); //$NON-NLS-1$
						quickAccessContents.resetProviders();
					}
				}
			}
		});
	}

}
