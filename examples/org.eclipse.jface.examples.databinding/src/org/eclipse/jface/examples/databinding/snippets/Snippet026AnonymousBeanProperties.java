/*******************************************************************************
 * Copyright (c) 2008, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 247997)
 *     Matthew Hall - bugs 261843, 260337, 265561
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;
import org.eclipse.core.databinding.property.set.DelegatingSetProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.set.SimpleSetProperty;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * @since 3.2
 *
 */
public class Snippet026AnonymousBeanProperties {
	private ComboViewer statusViewer;
	private Combo combo;
	private Text nameText;
	private TreeViewer contactViewer;

	public static void main(String[] args) {
		Display display = new Display();
		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			try {
				Snippet026AnonymousBeanProperties window = new Snippet026AnonymousBeanProperties();
				window.open();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private ApplicationModel model;
	private Shell shell;
	private Tree tree;

	// Minimal JavaBeans support
	public static abstract class AbstractModelObject {
		private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
				this);

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(listener);
		}

		public void addPropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(propertyName,
					listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(propertyName,
					listener);
		}

		protected void firePropertyChange(String propertyName, Object oldValue,
				Object newValue) {
			propertyChangeSupport.firePropertyChange(propertyName, oldValue,
					newValue);
		}
	}

	public static class ContactGroup extends AbstractModelObject implements Comparable<ContactGroup> {
		private String name;
		private Set<Contact> contacts = new TreeSet<>();

		ContactGroup(String name) {
			this.name = checkNull(name);
		}

		private String checkNull(String string) {
			if (string == null)
				throw new NullPointerException();
			return string;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			firePropertyChange("name", this.name, this.name = checkNull(name));
		}

		public Set<Contact> getContacts() {
			return new TreeSet<>(contacts);
		}

		public void addContact(Contact contact) {
			Set<Contact> oldValue = getContacts();
			contacts.add(contact);
			Set<Contact> newValue = getContacts();
			firePropertyChange("contacts", oldValue, newValue);
		}

		public void removeContact(Contact contact) {
			Set<Contact> oldValue = getContacts();
			contacts.remove(contact);
			Set<Contact> newValue = getContacts();
			firePropertyChange("contacts", oldValue, newValue);
		}

		@Override
		public int compareTo(ContactGroup that) {
			return this.name.compareTo(that.name);
		}
	}

	public static class Contact extends AbstractModelObject implements Comparable<Contact> {
		private String name;
		private String status;

		private String checkNull(String string) {
			if (string == null)
				throw new NullPointerException();
			return string;
		}

		public Contact(String name, String status) {
			this.name = checkNull(name);
			this.status = checkNull(status);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			firePropertyChange("name", this.name, this.name = checkNull(name));
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			firePropertyChange("status", this.status,
					this.status = checkNull(status));
		}

		@Override
		public int compareTo(Contact that) {
			int result = this.name.compareTo(that.name);
			if (result == 0)
				result = this.status.compareTo(that.status);
			return result;
		}
	}

	public static class ApplicationModel extends AbstractModelObject {
		private Set<ContactGroup> groups = new TreeSet<>();

		public Set<ContactGroup> getGroups() {
			return new TreeSet<>(groups);
		}

		public void setGroups(Set<ContactGroup> groups) {
			Set<ContactGroup> oldValue = getGroups();
			this.groups = new TreeSet<>(groups);
			Set<ContactGroup> newValue = getGroups();
			firePropertyChange("groups", oldValue, newValue);
		}
	}

	/**
	 * Set property for the "contacts" property of a ContactGroup. Since
	 * ContactGroup does not have a setContacts() method we have to write our
	 * own property to apply set changes incrementally through the addContact
	 * and removeContact methods.
	 */
	public static class ContactGroupContactsProperty extends SimpleSetProperty<ContactGroup, Contact> {
		@Override
		public Object getElementType() {
			return Contact.class;
		}

		@Override
		protected Set<Contact> doGetSet(ContactGroup source) {
			if (source == null)
				return Collections.emptySet();
			return source.getContacts();
		}

		@Override
		protected void doSetSet(ContactGroup source, Set<Contact> set, SetDiff<Contact> diff) {
			doUpdateSet(source, diff);
		}

		@Override
		protected void doUpdateSet(ContactGroup group, SetDiff<Contact> diff) {
			for (Contact contact : diff.getRemovals()) {
				group.removeContact(contact);
			}
			for (Contact contact : diff.getAdditions()) {
				group.addContact(contact);
			}
		}

		@Override
		public INativePropertyListener<ContactGroup> adaptListener(
				final ISimplePropertyListener<ContactGroup, SetDiff<Contact>> listener) {
			return new Listener(this, listener);
		}

		private class Listener extends NativePropertyListener<ContactGroup, SetDiff<Contact>>
				implements PropertyChangeListener {
			Listener(IProperty property, ISimplePropertyListener<ContactGroup, SetDiff<Contact>> listener) {
				super(property, listener);
			}

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				fireChange((ContactGroup) evt.getSource(), null);
			}

			@Override
			protected void doAddTo(ContactGroup source) {
				source.addPropertyChangeListener("contacts", this);
			}

			@Override
			protected void doRemoveFrom(ContactGroup source) {
				source.removePropertyChangeListener("contacts", this);
			}
		}
	}

	public void open() {
		model = createDefaultModel();

		final Display display = Display.getDefault();
		createContents();
		bindUI();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	private static final String[] statuses = new String[] { "Online", "Idle",
			"Busy", "Offline" };

	/**
	 * @return
	 */
	private ApplicationModel createDefaultModel() {
		ContactGroup swtGroup = new ContactGroup("SWT");
		swtGroup.addContact(new Contact("Steve Northover", "Busy"));
		swtGroup.addContact(new Contact("Grant Gayed", "Online"));
		swtGroup.addContact(new Contact("Veronika Irvine", "Offline"));
		swtGroup.addContact(new Contact("Mike Wilson", "Online"));
		swtGroup.addContact(new Contact("Christophe Cornu", "Idle"));
		swtGroup.addContact(new Contact("Lynne Kues", "Online"));
		swtGroup.addContact(new Contact("Silenio Quarti", "Idle"));

		ContactGroup jdbGroup = new ContactGroup("JFace Data Binding");
		jdbGroup.addContact(new Contact("Boris Bokowski", "Online"));
		jdbGroup.addContact(new Contact("Matthew Hall", "Idle"));

		Set<ContactGroup> groups = new TreeSet<>();
		groups.add(swtGroup);
		groups.add(jdbGroup);
		ApplicationModel model = new ApplicationModel();
		model.setGroups(groups);

		return model;
	}

	/**
	 * Create contents of the window
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(379, 393);
		shell.setText("Snippet026AnonymousBeanProperties");
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		shell.setLayout(gridLayout);

		contactViewer = new TreeViewer(shell, SWT.BORDER);
		tree = contactViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		final TreeColumn nameColumn = new TreeColumn(tree, SWT.NONE);
		nameColumn.setWidth(163);
		nameColumn.setText("Name");

		final TreeColumn newColumnTreeColumn = new TreeColumn(tree, SWT.NONE);
		newColumnTreeColumn.setWidth(100);
		newColumnTreeColumn.setText("Status");

		final Label nameLabel = new Label(shell, SWT.NONE);
		nameLabel.setText("Name");

		nameText = new Text(shell, SWT.BORDER);
		final GridData gd_nameText = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		nameText.setLayoutData(gd_nameText);

		final Label statusLabel = new Label(shell, SWT.NONE);
		statusLabel.setLayoutData(new GridData());
		statusLabel.setText("Status");

		statusViewer = new ComboViewer(shell, SWT.READ_ONLY);
		combo = statusViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	private void bindUI() {
		ISetProperty<Object, Object> treeChildrenProperty = new DelegatingSetProperty<Object, Object>() {
			ISetProperty<ApplicationModel, ContactGroup> modelGroups = BeanProperties.set(
					ApplicationModel.class, "groups", ContactGroup.class);
			ISetProperty<ContactGroup, Contact> groupContacts = BeanProperties.set(ContactGroup.class,
					"contacts", Contact.class);

			@SuppressWarnings("unchecked")
			@Override
			protected ISetProperty<Object, Object> doGetDelegate(Object source) {
				if (source instanceof ApplicationModel)
					return (ISetProperty<Object, Object>) (Object) modelGroups;
				if (source instanceof ContactGroup)
					return (ISetProperty<Object, Object>) (Object) groupContacts;
				return null;
			}
		};

		ViewerSupport.bind(contactViewer, model, treeChildrenProperty, BeanProperties.values("name", "status"));

		contactViewer.expandAll();

		final IObservableValue<Object> selection = ViewerProperties.singleSelection().observe(contactViewer);

		DataBindingContext dbc = new DataBindingContext();

		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(nameText),
				BeanProperties.value("name").observeDetail(selection));

		statusViewer.setContentProvider(new ArrayContentProvider());
		statusViewer.setInput(statuses);

		dbc.bindValue(ViewerProperties.singleSelection().observe(statusViewer),
				BeanProperties.value("status").observeDetail(selection));

		dbc.bindValue(WidgetProperties.enabled().observe(statusViewer.getControl()),
				ComputedValue.create(() -> selection.getValue() instanceof Contact));
	}
}
