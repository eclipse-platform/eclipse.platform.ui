/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.akrogen.tkui.css.swt.examples.chat;

import java.io.InputStream;

import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.core.impl.engine.CSSErrorHandlerImpl;
import org.akrogen.tkui.css.swt.engine.CSSSWTEngineImpl;
import org.akrogen.tkui.css.swt.resources.CSSSWTResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

public abstract class AbstractChatExample {

	protected static Display display;
	private static CTabFolder tabFolder;
	private static Tree tree;
	private static final String[] GROUPS = new String[] { "Work", "RCP Dev",
			"Friends" };
	private static final String[] NAMES = new String[] { "Betty Zechman",
			"Susan Adams", "Samantha Daryn", "Ted Amado" };
	private static final String INPUT_TEXT = "Do you know where I can find the Eclipsecon ppt template?";
	private static final String OUTPUT_TEXT = "Matt> Gotta sec to chat?\n\r\n\rBetty> Sure what's up";

	private InputStream styleSheetStream;

	protected CSSEngine engine;

	public AbstractChatExample(InputStream styleSheetStream) {
		this.styleSheetStream = styleSheetStream;
	}

	public void display() throws Exception {
		/*
		 * Create the display and shell.
		 */
		display = new Display();
		final Shell shell = new Shell(display);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 3;
		shell.setLayout(layout);
		/*
		 * Create a toolbar
		 */
		{
			ToolBar toolbar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT
					| SWT.NO_FOCUS);
			toolbar.setForeground(display.getSystemColor(SWT.COLOR_RED));
			toolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
					| GridData.GRAB_HORIZONTAL));
			ToolItem item = new ToolItem(toolbar, SWT.PUSH);
			item.setText("File");
			item = new ToolItem(toolbar, SWT.PUSH);
			item.setText("Edit");
			item = new ToolItem(toolbar, SWT.PUSH);
			item.setText("Help");
		}
		
		
		if (styleSheetStream == null) {
			// Create Styles themes
			createThemesStyleComposite(shell);			
		}
		
		/*
		 * Create a sash form.
		 */
		SashForm form = new SashForm(shell, SWT.NONE);
		form.setLayoutData(new GridData(GridData.FILL_BOTH));
		/*
		 * Create the buddylist tree.
		 */
		{
			tree = new Tree(form, SWT.SINGLE);
			tree.addSelectionListener(new SelectionAdapter() {
				public void widgetDefaultSelected(SelectionEvent e) {
					if (((TreeItem) e.item).getParentItem() != null) {
						try {
							createChatControl(e.item);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}

			});
			for (int i = 0; i < GROUPS.length; i++) {
				String g = GROUPS[i];
				TreeItem parentItem = new TreeItem(tree, SWT.NONE);
				parentItem.setText(g);
				for (int j = 0; j < NAMES.length; j++) {
					String n = NAMES[j];
					TreeItem item = new TreeItem(parentItem, SWT.NONE);
					item.setText(n);
				}
				parentItem.setExpanded(true);
			}
		}
		/*
		 * Add the tabfolder
		 */
		{
			tabFolder = new CTabFolder(form, SWT.CLOSE);
			tabFolder.setUnselectedCloseVisible(true);
			tabFolder.setUnselectedImageVisible(true);
			form.setWeights(new int[] { 30, 70 });
			// open a couple chats
			createChatControl(tree.getItem(0).getItems()[0]);
			createChatControl(tree.getItem(0).getItems()[1]);
			tabFolder.setSelection(tabFolder.getItem(0));
		}
		/*
		 * Create a statusbar
		 */
		{
			CLabel statusbar = new CLabel(shell, SWT.NONE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL
					| GridData.GRAB_HORIZONTAL);
			statusbar.setLayoutData(gd);
			statusbar.setText("Samantha Daryn is online");
		}
		/*
		 * StyleHelper is used to parse and apply styles.
		 */
		engine = getCSSEngine();
		if (styleSheetStream != null) {
			engine.parseStyleSheet(styleSheetStream);
			engine.applyStyles(shell, true);
		}
		/*
		 * Now we open the shell.
		 */
		shell.setSize(new Point(600, 600));
		shell.open();
		shell.setText("CSS Instant Messaging");
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/*
	 * The key listeners we add workaround a bug int Text that don't repaint
	 * properly when they have background images.
	 */
	protected void createChatControl(Widget item) throws Exception {
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText("Chat with " + ((TreeItem) item).getText());
		SashForm textForm = new SashForm(tabFolder, SWT.VERTICAL);
		final Text text1 = new Text(textForm, SWT.MULTI);
		text1.setData("id", "output");
		text1.setText(OUTPUT_TEXT);
		text1.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent arg0) {
				text1.redraw();
			}
		});
		final Text text2 = new Text(textForm, SWT.MULTI);
		text2.setData("id", "input");
		text2.setText(INPUT_TEXT);
		text2.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent arg0) {
				text2.redraw();
			}
		});
		tabItem.setControl(textForm);
		textForm.setWeights(new int[] { 80, 20 });
		getCSSEngine().applyStyles(textForm, false);
		tabFolder.setSelection(tabItem);
	}
	
	protected void createThemesStyleComposite(final Composite parent) {
		Composite themesComposite = new Composite(parent, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		themesComposite.setLayout(fillLayout);
		// no style Radio
		Label noStyleLabel = new Label(themesComposite, SWT.NONE);
		noStyleLabel.setText("No style");
		Button noStyleRadio = new Button(themesComposite, SWT.RADIO);
		noStyleRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CSSEngine engine = getCSSEngine();
				engine.reset();
				engine.applyStyles(parent, true, true);
			}
		});
		// Matrix style Radio
		Label matrixStyleLabel = new Label(themesComposite, SWT.NONE);
		matrixStyleLabel.setText("Matrix style");
		Button matrixStyleRadio = new Button(themesComposite, SWT.RADIO);
		matrixStyleRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CSSEngine engine = getCSSEngine();
				engine.reset();
				try {
					engine.parseStyleSheet(CSSSWTResources.getSWTMatrix());
					engine.applyStyles(parent, true, true);
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		// Osx style Radio
		Label osxStyleLabel = new Label(themesComposite, SWT.NONE);
		osxStyleLabel.setText("Osx style");
		Button osxStyleRadio = new Button(themesComposite, SWT.RADIO);
		osxStyleRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CSSEngine engine = getCSSEngine();
				engine.reset();
				try {
					engine.parseStyleSheet(CSSSWTResources.getSWTOsx());
					engine.applyStyles(parent, true, true);
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});	
		// Vista style Radio
		Label vistaStyleLabel = new Label(themesComposite, SWT.NONE);
		vistaStyleLabel.setText("Vista style");
		Button vistaStyleRadio = new Button(themesComposite, SWT.RADIO);
		vistaStyleRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CSSEngine engine = getCSSEngine();
				engine.reset();
				try {
					engine.parseStyleSheet(CSSSWTResources.getSWTVista());
					engine.applyStyles(parent, true, true);
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});			
	}

	protected CSSEngine getCSSEngine() {
		if (engine == null) {
			engine = new CSSSWTEngineImpl(display);
			// Print stack trace when Exception is thrown
			engine.setErrorHandler(CSSErrorHandlerImpl.INSTANCE);
		}
		return engine;
	}
}
