/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 424730
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.AbstractIconDialogWithHardcodedScope;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.AbstractIconDialogWithScopeAndFilter;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
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
public class IconDialog extends Dialog implements UriDialog {
	private Text txtUri;
	private Text txtBundle;
	private Text txtIcon;
	private Text txtPath;
	public String uri = ""; //$NON-NLS-1$
	private String bundle = ""; //$NON-NLS-1$
	private String icon = ""; //$NON-NLS-1$
	private String path = ""; //$NON-NLS-1$
	protected boolean ignoreModify;
	private IEclipseContext context;

	static Pattern patternIcon = Pattern.compile("platform:/plugin/*([^/]+)/((.*)/)?([^/]+)"); //$NON-NLS-1$

	public IconDialog(Shell parentShell, IEclipseContext context) {
		super(parentShell);
		this.context = context;
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText(Messages.IconDialog_uriEditor);
		super.configureShell(newShell);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));

		ToolBar toolBar = new ToolBar(composite, SWT.NO_FOCUS);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		{
			Label lbl = new Label(composite, SWT.NONE);
			lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lbl.setText(Messages.IconDialog_uri);

			txtUri = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtUri.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtUri.setText("platform://plugin/"); //$NON-NLS-1$

			txtUri.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (ignoreModify) {
						return;
					}
					ignoreModify = true;
					setUri(txtUri.getText());
					txtBundle.setText(bundle);
					txtPath.setText(path);
					txtIcon.setText(icon);
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

				prefix = "platform:/plugin/"; //$NON-NLS-1$
				setUri(prefix + txtBundle.getText() + "/" + txtPath.getText() + "/" + txtIcon.getText()); //$NON-NLS-1$//$NON-NLS-2$

				txtUri.setText(getUri());
				ignoreModify = false;
			}
		};

		{
			Link lbl = new Link(composite, SWT.NONE);
			lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lbl.setText("<A>" + Messages.IconDialog_bundle + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$

			txtBundle = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtBundle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtBundle.addModifyListener(listener);

			lbl.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IEclipseContext childCtx = context.createChild();
					childCtx.set(Bundle.class, FrameworkUtil.getBundle(FindContributionDialog.class));
					childCtx.set("bundle", getBundle()); //$NON-NLS-1$
					childCtx.set("package", getPath()); //$NON-NLS-1$
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
			lbl.setText("path"); //$NON-NLS-1$

			txtPath = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtPath.addModifyListener(listener);
		}

		{
			Link lbl = new Link(composite, SWT.NONE);
			lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lbl.setText("<A>" + Messages.IconDialog_icon + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$

			txtIcon = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtIcon.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtIcon.addModifyListener(listener);

			lbl.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IEclipseContext childCtx = context.createChild();
					childCtx.set(Bundle.class, FrameworkUtil.getBundle(FindContributionDialog.class));
					childCtx.set(Messages.IconDialog_bundle, getBundle());
					childCtx.set(Messages.IconDialog_folder, getPath());
					AbstractIconDialogWithHardcodedScope dlg = new AbstractIconDialogWithHardcodedScope(getParentShell(), childCtx) {

						@Override
						protected String getShellTitle() {
							return org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.IconDialog_selectIcon;
						}

						@Override
						protected String getDialogTitle() {
							return org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.IconDialog_selectIcon_TITLE;
						}

						@Override
						protected String getDialogMessage() {
							return org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.IconDialog_selectIcon_MESSAGE;
						}
					};
					if (dlg.open() == Dialog.OK) {
						txtUri.setText(dlg.getValue());
					}
				}
			});
		}

		ToolItem btnFind = new ToolItem(toolBar, SWT.PUSH);
		// btnFind.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
		// false, 2, 1));
		btnFind.setText(Messages.IconDialog_find + "..."); //$NON-NLS-1$
		btnFind.setImage(new Image(getShell().getDisplay(), getClass().getResourceAsStream("/icons/full/obj16/find.png"))); //$NON-NLS-1$
		btnFind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IEclipseContext childCtx = context.createChild();
				AbstractIconDialogWithScopeAndFilter dlg = new FindIconDialog(getParentShell(), childCtx);
				if (dlg.open() == Dialog.OK) {
					txtUri.setText(dlg.getValue());
					// getContents().pack(true);
					getShell().pack();
				}
			}
		});

		ToolItem btnRemove = new ToolItem(toolBar, SWT.PUSH);
		// btnRemove.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
		// false, false, 2, 1));
		btnRemove.setText("Remove"); //$NON-NLS-1$
		btnRemove.setImage(new Image(getShell().getDisplay(), getClass().getResourceAsStream("/icons/full/obj16/remove_filter.png"))); //$NON-NLS-1$
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txtUri.setText(""); //$NON-NLS-1$
				close();
			}
		});

		txtUri.setText(uri);
		txtBundle.setText(bundle);
		txtIcon.setText(icon);
		return composite;
	}

	protected String getUri(FindContributionDialog dlg) {
		return dlg.getPlatformUri();
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
			path = matcher.group(3);
			if (path == null) {
				path = ""; //$NON-NLS-1$
			}
			icon = matcher.group(4);
		} else {
			bundle = ""; //$NON-NLS-1$
			path = ""; //$NON-NLS-1$
			icon = ""; //$NON-NLS-1$
		}
	}

	protected String getBundle() {
		return bundle;
	}

	protected void setBundle(String bundle) {
		this.bundle = bundle;
	}

	protected String getIcon() {
		return icon;
	}

	protected void setIcon(String icon) {
		this.icon = icon;
	}

	protected String getPath() {
		return path;
	}

	protected void setPath(String pakage) {
		this.path = pakage;
	}

	private Matcher getMatcher(String uri) {
		Matcher matcher;
		matcher = patternIcon.matcher(uri);
		return matcher;
	}
}