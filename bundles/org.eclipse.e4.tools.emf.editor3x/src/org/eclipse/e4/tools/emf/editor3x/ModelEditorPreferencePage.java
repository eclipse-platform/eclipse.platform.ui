package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

public class ModelEditorPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	private boolean autoCreateElementId;
	private IEclipsePreferences node;
	
	public ModelEditorPreferencePage() {
	}

	public ModelEditorPreferencePage(String title) {
		super(title);
	}

	public ModelEditorPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	public void init(IWorkbench workbench) {
		node = new InstanceScope().getNode("org.eclipse.e4.tools.emf.ui");
		autoCreateElementId = node.getBoolean("autoCreateElementId", false);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);
		result.setLayout(new GridLayout(2,false));
		
		Label l = new Label(result, SWT.NONE);
		l.setText("Autogenerate Element-Id");
		final Button b = new Button(result, SWT.CHECK);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				autoCreateElementId = b.getSelection();
			}
		});
		
		return result;
	}
	
	@Override
	public boolean performOk() {
		node.putBoolean("autoCreateElementId", autoCreateElementId);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.performOk();
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
