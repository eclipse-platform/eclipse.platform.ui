/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 424730
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs;

import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * A dialog for creating, editing, replacing, or removing a bundleclass.
 *
 * @author Steven Spungin
 *
 */
public class BundleClassDialog extends Dialog implements UriDialog {
	private Text txtUri;
	private Text txtBundle;
	private Text txtClass;
	private Text txtPackage;
	public String uri = ""; //$NON-NLS-1$
	private String bundle = ""; //$NON-NLS-1$
	private String clazz = ""; //$NON-NLS-1$
	private String pakage = ""; //$NON-NLS-1$
	private Composite parent;
	protected boolean ignoreModify;
	private IEclipseContext context;

	static Pattern patternBundleClass = Pattern.compile("bundleclass:/*([^/]+)/((.*)\\.)?([^\\.]+)"); //$NON-NLS-1$

	public BundleClassDialog(Shell parentShell, IEclipseContext context) {
		super(parentShell);
		this.context = context;
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText(Messages.BundleClassDialog_bundleClassEditor);
		super.configureShell(newShell);
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		this.parent = parent;
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));

		ToolBar toolBar = new ToolBar(composite, SWT.NO_FOCUS);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		{
			Label lbl = new Label(composite, SWT.NONE);
			lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lbl.setText(Messages.BundleClassDialog_uri);

			txtUri = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtUri.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtUri.setText("bundleclass://"); //$NON-NLS-1$

			txtUri.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (ignoreModify) {
						return;
					}
					ignoreModify = true;
					setUri(txtUri.getText());
					txtBundle.setText(bundle);
					txtPackage.setText(pakage);
					txtClass.setText(clazz);
					ignoreModify = false;
					parent.pack();
				}
			});
		}

		// common listener for text boxes
		ModifyListener listener = new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (ignoreModify) {
					return;
				}
				ignoreModify = true;
				String prefix;
				prefix = "bundleclass://"; //$NON-NLS-1$
				setUri(prefix + txtBundle.getText() + "/" + txtPackage.getText() + "." + txtClass.getText()); //$NON-NLS-1$//$NON-NLS-2$
				txtUri.setText(getUri());
				ignoreModify = false;
			}
		};

		{
			Link lbl = new Link(composite, SWT.NONE);
			lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lbl.setText("<A>" + Messages.BundleClassDialog_bundle + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			txtBundle = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtBundle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtBundle.addModifyListener(listener);

			lbl.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IEclipseContext childCtx = context.createChild();
					childCtx.set(Bundle.class, FrameworkUtil.getBundle(FindContributionDialog.class));
					childCtx.set("bundle", getBundle()); //$NON-NLS-1$
					childCtx.set("package", getPackage()); //$NON-NLS-1$
					childCtx.set("mode", "show-bundles"); //$NON-NLS-1$ //$NON-NLS-2$
					FindContributionDialog dlg = new FindContributionDialog(childCtx);
					if (dlg.open() == Dialog.OK) {
						String uri = getUri(dlg);
						Matcher matcher = getMatcher(uri);
						if (matcher.matches()) {
							txtBundle.setText(matcher.group(1));
						}
					}
				}
			});
		}

		{
			Link lbl = new Link(composite, SWT.NONE);
			lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lbl.setText("<A>" + Messages.BundleClassDialog_package + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			txtPackage = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtPackage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtPackage.addModifyListener(listener);

			lbl.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IEclipseContext childCtx = context.createChild();
					childCtx.set(Bundle.class, FrameworkUtil.getBundle(FindContributionDialog.class));
					childCtx.set("bundle", getBundle()); //$NON-NLS-1$
					childCtx.set("package", getPackage()); //$NON-NLS-1$
					childCtx.set("mode", "show-packages"); //$NON-NLS-1$ //$NON-NLS-2$
					FindContributionDialog dlg = new FindContributionDialog(childCtx);
					if (dlg.open() == Dialog.OK) {
						String uri = getUri(dlg);
						Matcher matcher = getMatcher(uri);
						if (matcher.matches()) {
							txtPackage.setText(matcher.group(3));
						}
					}
				}
			});
		}

		{
			Link lbl = new Link(composite, SWT.NONE);
			lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lbl.setText("<A>" + Messages.BundleClassDialog_class + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$

			txtClass = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtClass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtClass.addModifyListener(listener);

			lbl.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IEclipseContext childCtx = context.createChild();
					childCtx.set(Bundle.class, FrameworkUtil.getBundle(FindContributionDialog.class));
					childCtx.set("bundle", getBundle()); //$NON-NLS-1$
					childCtx.set("package", getPackage()); //$NON-NLS-1$

					FindContributionDialog dlg = new FindContributionDialog(childCtx);
					if (dlg.open() == Window.OK) {
						txtUri.setText(getUri(dlg));
					}
				}
			});
		}

		ToolItem btnGo = new ToolItem(toolBar, SWT.PUSH);
		// btnGo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
		// false, 2, 1));
		btnGo.setText(Messages.BundleClassDialog_create_goto);
		btnGo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					MPart dummyPart = MBasicFactory.INSTANCE.createPart();
					dummyPart.setContributionURI(getUri());
					final IContributionClassCreator c = context.get(ModelEditor.class).getContributionCreator(org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.PART);
					c.createOpen(dummyPart, context.get(EditingDomain.class), context.get(IProject.class), getShell());
					setUri(dummyPart.getContributionURI());
					okPressed();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		});

		ToolItem btnFind = new ToolItem(toolBar, SWT.PUSH);
		// btnFind.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
		// false, 2, 1));
		btnFind.setText(Messages.BundleClassDialog_find + "..."); //$NON-NLS-2$ //$NON-NLS-1$
		btnFind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IEclipseContext childCtx = context.createChild();
				childCtx.set(Bundle.class, FrameworkUtil.getBundle(FindContributionDialog.class));
				FindContributionDialog dialog = new FindContributionDialog(childCtx);
				if (dialog.open() == Dialog.OK) {
					txtUri.setText(getUri(dialog));
				}
			}
		});

		ToolItem btnRemove = new ToolItem(toolBar, SWT.PUSH);
		// btnRemove.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
		// false, false, 2, 1));
		btnRemove.setText("Remove"); //$NON-NLS-1$
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txtUri.setText(""); //$NON-NLS-1$
				close();
			}
		});

		txtUri.setText(uri);
		txtBundle.setText(bundle);
		txtClass.setText(clazz);
		return composite;
	}

	protected String getUri(FindContributionDialog dlg) {
		return dlg.getBundleclassUri();
	}

	@Override
	public int open() {
		int ret = super.open();
		return ret;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public void setUri(String uri) {
		this.uri = uri == null ? "" : uri.toString(); //$NON-NLS-1$
		Matcher matcher = getMatcher(this.uri);
		if (matcher.matches()) {
			bundle = matcher.group(1);
			pakage = matcher.group(3);
			if (pakage == null) {
				pakage = ""; //$NON-NLS-1$
			}
			clazz = matcher.group(4);
		} else {
			bundle = ""; //$NON-NLS-1$
			pakage = ""; //$NON-NLS-1$
			clazz = ""; //$NON-NLS-1$
		}
	}

	protected String getBundle() {
		return bundle;
	}

	protected void setBundle(String bundle) {
		this.bundle = bundle;
	}

	protected String getClazz() {
		return clazz;
	}

	protected void setClazz(String clazz) {
		this.clazz = clazz;
	}

	protected String getPackage() {
		return pakage;
	}

	protected void setPackage(String pakage) {
		this.pakage = pakage;
	}

	private Matcher getMatcher(String uri) {
		return patternBundleClass.matcher(uri);
	}
}