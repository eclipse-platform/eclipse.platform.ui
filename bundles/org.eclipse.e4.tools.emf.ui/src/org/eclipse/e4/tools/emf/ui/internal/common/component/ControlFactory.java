package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.Arrays;

import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ControlFactory {
	public static void createTagsWidget( Composite parent, final AbstractComponentEditor editor ) {
		Label l = new Label(parent, SWT.NONE);
		l.setText("Tags");
		l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

		final Text tagText = new Text(parent, SWT.BORDER);
		tagText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tagText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.LF) {
					handleAddText(editor, tagText);
				}
			}
		});

		Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
		b.setText("Add");
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAddText(editor, tagText);
			}
		});
		b.setImage(editor.getImage(b.getDisplay(), AbstractComponentEditor.TABLE_ADD_IMAGE));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));

		l = new Label(parent, SWT.NONE);
		final TableViewer viewer = new TableViewer(parent);
		viewer.setContentProvider(new ObservableListContentProvider());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return element.toString();
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 120;
		viewer.getControl().setLayoutData(gd);

		IEMFEditListProperty prop = EMFEditProperties.list(editor.getEditingDomain(), MApplicationPackage.Literals.APPLICATION_ELEMENT__TAGS);
		viewer.setInput(prop.observeDetail(editor.getMaster()));

		Composite buttonComp = new Composite(parent, SWT.NONE);
		buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
		GridLayout gl = new GridLayout();
		gl.marginLeft = 0;
		gl.marginRight = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		buttonComp.setLayout(gl);

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText("Up");
		b.setImage(editor.getImage(b.getDisplay(), AbstractComponentEditor.ARROW_UP));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText("Down");
		b.setImage(editor.getImage(b.getDisplay(), AbstractComponentEditor.ARROW_DOWN));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText("Remove");
		b.setImage(editor.getImage(b.getDisplay(), AbstractComponentEditor.TABLE_DELETE_IMAGE));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
				if( ! s.isEmpty() ) {
					MApplicationElement appEl = (MApplicationElement) editor.getMaster().getValue();
					Command cmd = RemoveCommand.create(editor.getEditingDomain(), appEl, MApplicationPackage.Literals.APPLICATION_ELEMENT__TAGS, s.toList());
					if( cmd.canExecute() ) {
						editor.getEditingDomain().getCommandStack().execute(cmd);
					}
				}
			}
		});
	}

	private static void handleAddText( AbstractComponentEditor editor, Text tagText) {
		if (tagText.getText().trim().length() > 0) {
			String[] tags = tagText.getText().split(";");
			for( int i = 0; i < tags.length;i++ ) {
				tags[i] = tags[i].trim();
			}

			MApplicationElement appEl = (MApplicationElement) editor.getMaster().getValue();
			Command cmd = AddCommand.create(editor.getEditingDomain(), appEl, MApplicationPackage.Literals.APPLICATION_ELEMENT__TAGS, Arrays.asList(tags));
			if (cmd.canExecute()) {
				editor.getEditingDomain().getCommandStack().execute(cmd);
			}
			tagText.setText("");
		}
	}
}
