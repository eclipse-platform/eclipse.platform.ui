/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionData;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionResultHandler;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.Filter;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ClassContributionCollector;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class ContributionClassDialog extends TitleAreaDialog {
	private IProject project;
	private Image javaClassImage;
	private MApplicationElement contribution;
	private EditingDomain editingDomain;
	private TableViewer viewer;
	private EStructuralFeature feature;
	private Messages Messages;

	public ContributionClassDialog(Shell parentShell, IProject project, EditingDomain editingDomain, MApplicationElement contribution, EStructuralFeature feature, Messages Messages) {
		super(parentShell);
		this.project = project;
		this.contribution = contribution;
		this.editingDomain = editingDomain;
		this.feature = feature;
		this.Messages = Messages;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);

		getShell().setText(Messages.ContributionClassDialog_ShellTitle);
		setTitle(Messages.ContributionClassDialog_DialogTitle);
		setMessage(Messages.ContributionClassDialog_DialogMessage);

		final Image titleImage = new Image(comp.getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/wizban/newclass_wiz.png")); //$NON-NLS-1$
		setTitleImage(titleImage);

		getShell().addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				javaClassImage.dispose();
				titleImage.dispose();
			}
		});

		javaClassImage = new Image(getShell().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/obj16/class_obj.gif")); //$NON-NLS-1$

		Composite container = new Composite(comp, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Label l = new Label(container, SWT.NONE);
		l.setText(Messages.ContributionClassDialog_Label_Classname);

		final Text t = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(container, SWT.NONE);

		viewer = new TableViewer(container);
		GridData gd = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(gd);
		viewer.setContentProvider(new ObservableListContentProvider());
		viewer.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				ContributionData data = (ContributionData) cell.getElement();
				StyledString styledString = new StyledString(data.className, null);

				if (data.bundleName != null) {
					styledString.append(" - " + data.bundleName, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				}

				if (data.sourceType != null) {
					styledString.append(" - ", StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
					styledString.append(data.sourceType + "", StyledString.COUNTER_STYLER); //$NON-NLS-1$
				}

				if (data.iconPath == null) {
					cell.setImage(javaClassImage);
				}

				cell.setText(styledString.getString());
				cell.setStyleRanges(styledString.getStyleRanges());
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		final WritableList list = new WritableList();
		viewer.setInput(list);

		final ClassContributionCollector collector = getCollector();

		t.addModifyListener(new ModifyListener() {
			private ContributionResultHandlerImpl currentResultHandler;

			public void modifyText(ModifyEvent e) {
				if (currentResultHandler != null) {
					currentResultHandler.cancled = true;
				}
				list.clear();
				currentResultHandler = new ContributionResultHandlerImpl(list);
				Filter filter = new Filter(project, t.getText());
				collector.findContributions(filter, currentResultHandler);
			}
		});

		return comp;
	}

	@Override
	protected void okPressed() {
		IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
		if (!s.isEmpty()) {
			ContributionData cd = (ContributionData) s.getFirstElement();
			String uri = "bundleclass://" + cd.bundleName + "/" + cd.className; //$NON-NLS-1$ //$NON-NLS-2$
			Command cmd = SetCommand.create(editingDomain, contribution, feature, uri);
			if (cmd.canExecute()) {
				editingDomain.getCommandStack().execute(cmd);
				super.okPressed();
			}
		}
	}

	private ClassContributionCollector getCollector() {
		Bundle bundle = FrameworkUtil.getBundle(ContributionClassDialog.class);
		BundleContext context = bundle.getBundleContext();
		ServiceReference ref = context.getServiceReference(ClassContributionCollector.class.getName());
		if (ref != null) {
			return (ClassContributionCollector) context.getService(ref);
		}
		return null;
	}

	private static class ContributionResultHandlerImpl implements ContributionResultHandler {
		private boolean cancled = false;
		private IObservableList list;

		public ContributionResultHandlerImpl(IObservableList list) {
			this.list = list;
		}

		public void result(ContributionData data) {
			if (!cancled) {
				list.add(data);
			}
		}

	}
}