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
import org.eclipse.e4.tools.emf.ui.internal.common.ClassContributionCollector;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
	
	public ContributionClassDialog(Shell parentShell, IProject project) {
		super(parentShell);
		this.project = project;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(comp, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2,false));
		
		Label l = new Label(container, SWT.NONE);
		l.setText("Classname");
		
		final Text t = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		l = new Label(container,SWT.NONE);
		
		TableViewer viewer = new TableViewer(container);
		GridData gd = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(gd);
		viewer.setContentProvider(new ObservableListContentProvider());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ContributionData)element).className;
			}
		});
		
		final WritableList list = new WritableList();
		viewer.setInput(list);
		
		final ClassContributionCollector collector = getCollector();
		
		t.addModifyListener(new ModifyListener() {
			private ContributionResultHandlerImpl currentResultHandler;
			
			public void modifyText(ModifyEvent e) {
				if( currentResultHandler != null ) {
					currentResultHandler.cancled = true;
				}
				list.clear();
				currentResultHandler = new ContributionResultHandlerImpl(list);
				Filter filter = new Filter(project, t.getText());
				collector.findContributions(filter, currentResultHandler);
			}
		});
		
//		collector.findContributions(new Filter(project, "at.bestsolution.e4.handlers.*"), new ContributionResultHandler() {
//			
//			public void result(ContributionData data) {
//				System.err.println(data.className);
//			}
//		});
		
		return comp;
	}
	
	private ClassContributionCollector getCollector() {
		Bundle bundle = FrameworkUtil.getBundle(ContributionClassDialog.class);
		BundleContext context = bundle.getBundleContext();
		ServiceReference ref = context.getServiceReference(ClassContributionCollector.class.getName());
		if( ref != null ) {
			return (ClassContributionCollector) context.getService(ref);
		}
		return null;
	}
	
	private class ContributionResultHandlerImpl implements ContributionResultHandler {
		private boolean cancled = false;
		private IObservableList list;
		
		public ContributionResultHandlerImpl(IObservableList list) {
			this.list = list;
		}
		
		public void result(ContributionData data) {
			if( ! cancled ) {
				list.add(data);
			}
		}
		
	}
}
