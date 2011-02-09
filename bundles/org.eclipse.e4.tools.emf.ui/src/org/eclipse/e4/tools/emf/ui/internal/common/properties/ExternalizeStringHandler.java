package org.eclipse.e4.tools.emf.ui.internal.common.properties;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.inject.Named;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.tools.services.Translation;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ExternalizeStringHandler {

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, @Translation Messages messages, IModelResource resource, IResourcePool pool, IProject project) {
		TitleAreaDialog dialog = new ExtractionDialog(shell, messages, resource, resource.getRoot(), pool, project);
		dialog.open();
	}

	static class ExtractionDialog extends TitleAreaDialog {
		private Messages messages;
		private IObservableList list;
		private IResourcePool pool;
		private IProject project;
		private CheckboxTableViewer viewer;
		private IModelResource resource;

		public ExtractionDialog(Shell parentShell, Messages messages, IModelResource resource, IObservableList list, IResourcePool pool, IProject project) {
			super(parentShell);
			this.messages = messages;
			this.list = list;
			this.pool = pool;
			this.project = project;
			this.resource = resource;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			getShell().setText(messages.ExternalizeStringHandler_Dialog_ShellTitle);
			setTitle(messages.ExternalizeStringHandler_Dialog_DialogTitle);
			setMessage(messages.ExternalizeStringHandler_Dialog_DialogMessage);
			setTitleImage(pool.getImageUnchecked(ResourceProvider.IMG_Wizban16_extstr_wiz));

			Composite container = (Composite) super.createDialogArea(parent);
			Table t = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK);
			t.setLayoutData(new GridData(GridData.FILL_BOTH));
			t.setHeaderVisible(true);
			t.setLinesVisible(true);

			viewer = new CheckboxTableViewer(t);

			{
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						return ""; //$NON-NLS-1$
					}
				});
			}

			{
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.getColumn().setText(messages.ExternalizeStringHandler_Dialog_ElementName);
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entry e = (Entry) element;
						// FIXME We need the label provider
						return e.object.eClass().getName();
					}
				});
			}

			{
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.getColumn().setText(messages.ExternalizeStringHandler_Dialog_AttributeName);
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entry e = (Entry) element;
						// FIXME We need the label provider
						return e.feature.getName();
					}
				});
			}

			{
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.getColumn().setText(messages.ExternalizeStringHandler_Dialog_Key);
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entry e = (Entry) element;
						return e.key;
					}
				});
			}

			{
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.getColumn().setText(messages.ExternalizeStringHandler_Dialog_Value);
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entry e = (Entry) element;
						return e.value;
					}
				});
			}

			List<Entry> entries = new ArrayList<ExternalizeStringHandler.Entry>();
			TreeIterator<EObject> it = EcoreUtil.getAllContents(list);

			List<String> ids = new ArrayList<String>();

			while (it.hasNext()) {
				EObject o = it.next();
				if (o instanceof MUILabel) {
					MUILabel l = (MUILabel) o;
					if (l.getLabel() != null && l.getLabel().startsWith("%")) { //$NON-NLS-1$
						ids.add(l.getLabel());
					}

					if (l.getTooltip() != null && l.getTooltip().startsWith("%")) { //$NON-NLS-1$
						ids.add(l.getTooltip());
					}
				}
			}

			Properties properties = getBaseProperties();
			for (Object o : properties.keySet()) {
				ids.add(o.toString());
			}

			it = EcoreUtil.getAllContents(list);
			while (it.hasNext()) {
				EObject o = it.next();
				if (o instanceof MUILabel) {
					MUILabel l = (MUILabel) o;
					if (l.getLabel() != null && l.getLabel().trim().length() != 0 && !l.getLabel().startsWith("%")) { //$NON-NLS-1$
						String id = findId(ids, o.eClass().getName().toLowerCase() + ".label"); //$NON-NLS-1$
						entries.add(new Entry(o, UiPackageImpl.Literals.UI_LABEL__LABEL, id, l.getLabel()));
						ids.add(id);
					}

					if (l.getTooltip() != null && l.getTooltip().trim().length() != 0 && !l.getTooltip().startsWith("%")) { //$NON-NLS-1$
						String id = findId(ids, o.eClass().getName().toLowerCase() + ".tooltip"); //$NON-NLS-1$
						entries.add(new Entry(o, UiPackageImpl.Literals.UI_LABEL__TOOLTIP, id, l.getTooltip()));
						ids.add(id);
					}
				}
			}
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setInput(entries);
			viewer.setAllChecked(true);

			for (int i = 1; i < viewer.getTable().getColumnCount(); i++) {
				TableColumn c = viewer.getTable().getColumn(i);
				c.pack();
				if (c.getWidth() < 120) {
					c.setWidth(120);
				}
			}

			return container;
		}

		@Override
		protected void okPressed() {
			Object[] els = viewer.getCheckedElements();
			if (els.length > 0) {
				try {
					IFile f = getBasePropertyFile();
					if (f.exists()) {
						StringBuilder b = new StringBuilder(System.getProperty("line.separator"));
						for (Object o : els) {
							Entry e = (Entry) o;
							b.append(e.key + " = " + e.value + System.getProperty("line.separator")); //$NON-NLS-1$//$NON-NLS-2$
						}

						System.err.println("Appending: " + b);

						ByteArrayInputStream stream = new ByteArrayInputStream(b.toString().getBytes());
						f.appendContents(stream, IFile.KEEP_HISTORY, new NullProgressMonitor());

						for (Object o : els) {
							Entry e = (Entry) o;
							Command cmd = SetCommand.create(resource.getEditingDomain(), e.object, e.feature, "%" + e.key);

							if (cmd.canExecute()) {
								resource.getEditingDomain().getCommandStack().execute(cmd);
							}
						}
					}
					super.okPressed();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		private IFile getBasePropertyFile() throws CoreException, IOException {
			IFile file = project.getFile("META-INF/MANIFEST.MF"); //$NON-NLS-1$
			String base = ProjectOSGiTranslationProvider.extractBasenameFromManifest(file);
			return project.getFile(base + ".properties"); //$NON-NLS-1$
		}

		private Properties getBaseProperties() {
			Properties prop = new Properties();
			try {
				IFile f = getBasePropertyFile();
				if (f.exists()) {

					InputStream in = f.getContents();
					prop.load(in);
					in.close();
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return prop;
		}
	}

	private static String findId(List<String> ids, String prefix) {
		int count = 1;
		String id = prefix + "." + count; //$NON-NLS-1$
		while (ids.contains(id)) {
			id = prefix + "." + ++count; //$NON-NLS-1$
		}
		return id;
	}

	static class Entry {
		private EObject object;
		private EStructuralFeature feature;
		private String key;
		private String value;

		public Entry(EObject object, EStructuralFeature feature, String key, String value) {
			this.object = object;
			this.feature = feature;
			this.key = key;
			this.value = value;
		}
	}
}