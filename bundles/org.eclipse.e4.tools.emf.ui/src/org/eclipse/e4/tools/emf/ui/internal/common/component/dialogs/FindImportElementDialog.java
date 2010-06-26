package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.e4.ui.model.application.MApplication;

import org.eclipse.swt.widgets.Button;

import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Label;

import org.eclipse.swt.layout.GridData;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

import org.eclipse.swt.graphics.Image;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.emf.edit.domain.EditingDomain;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.TitleAreaDialog;

public class FindImportElementDialog extends TitleAreaDialog {
	private MApplication runningApp;
	
	public FindImportElementDialog(Shell parentShell, EditingDomain domain, EObject element/*, MApplication runningApp*/) {
		super(parentShell);
		this.runningApp = runningApp;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		
		final Image titleImage = new Image(parent.getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/wizban/import_wiz.png"));
		setTitleImage(titleImage);
		getShell().addDisposeListener(new DisposeListener() {
			
			public void widgetDisposed(DisposeEvent e) {
				titleImage.dispose();
			}
		});
		
		getShell().setText("Find Import Elements");
		setTitle("Find Import Elements");
		setMessage("Search for an elements whose ID you'd like to import");
		
		Composite container = new Composite(comp,SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(3, false));
		
		
		Label l = new Label(container, SWT.NONE);
		l.setText("Search");
		
		final Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		searchText.setLayoutData(gd);
		
		new Label(container, SWT.NONE);

		l = new Label(container, SWT.NONE);
		l.setText("File");
		
		Text t = new Text(container, SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		t.setText("memory://running-model");
		
		Button b = new Button(container, SWT.PUSH);
		b.setText("Browse");
		
		l = new Label(container, SWT.PUSH);
		
		TableViewer viewer = new TableViewer(container);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		return comp;
	}
}