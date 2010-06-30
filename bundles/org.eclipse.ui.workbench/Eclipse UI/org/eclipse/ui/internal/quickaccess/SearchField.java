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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SearchField {
	Shell shell;

	@PostConstruct
	void createWidget(final Composite parent) {
		// borderColor = new Color(parent.getDisplay(), 170, 176, 191);
		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		final Text text = new Text(comp, SWT.SEARCH | SWT.ICON_SEARCH);
		GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(text);
		text.setMessage(QuickAccessMessages.QuickAccess_EnterSearch);

		QuickAccessProvider[] providers = new QuickAccessProvider[] { new EditorProvider(),
				new ViewProvider(), new PerspectiveProvider(), new CommandProvider(),
				new ActionProvider(), new WizardProvider(), new PreferenceProvider(),
				new PropertiesProvider() };
		QuickAccessContents quickAccessContents = new QuickAccessContents(providers) {
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
		GridLayoutFactory.fillDefaults().applyTo(shell);
		quickAccessContents.createTable(shell, Window.getDefaultOrientation());
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
}
