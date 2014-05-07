/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 175735)
 *     Matthew Hall - bugs 262407, 260337
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.DuplexingObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.2
 * 
 */
public class Snippet028DuplexingObservableValue {
	protected Shell shell;
	private TableViewer viewer;
	private Table table;
	private Text releaseDate;
	private Text title;
	private Text director;
	private Text writer;

	/**
	 * Launch the application
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Snippet028DuplexingObservableValue window = new Snippet028DuplexingObservableValue();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window
	 */
	public void open() {
		final Display display = Display.getDefault();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				createContents();
				shell.open();
				shell.layout();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			}
		});
	}

	protected void createContents() {
		shell = new Shell();
		shell.setSize(509, 375);
		shell.setText("Snippet028DuplexingObservableValue.java");
		final GridLayout gridLayout = new GridLayout();
		gridLayout.makeColumnsEqualWidth = true;
		gridLayout.numColumns = 4;
		shell.setLayout(gridLayout);

		viewer = new TableViewer(shell, SWT.FULL_SELECTION | SWT.MULTI
				| SWT.BORDER);
		table = viewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		final TableColumn newColumnTableColumn_1 = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn_1.setWidth(120);
		newColumnTableColumn_1.setText("Movie");

		final TableColumn newColumnTableColumn = new TableColumn(table,
				SWT.NONE);

		newColumnTableColumn.setWidth(120);
		newColumnTableColumn.setText("Release Date");

		final TableColumn newColumnTableColumn_2 = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn_2.setWidth(120);
		newColumnTableColumn_2.setText("Director");

		final TableColumn newColumnTableColumn_3 = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn_3.setWidth(120);
		newColumnTableColumn_3.setText("Writer");

		final Label movieLabel = new Label(shell, SWT.NONE);
		movieLabel.setText("Movie");

		final Label directorLabel = new Label(shell, SWT.NONE);
		directorLabel.setLayoutData(new GridData());
		directorLabel.setText("Release Date");

		final Label producerLabel = new Label(shell, SWT.NONE);
		producerLabel.setText("Director");

		final Label scoreLabel = new Label(shell, SWT.NONE);
		scoreLabel.setText("Writer");

		title = new Text(shell, SWT.BORDER);
		final GridData gd_title = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		title.setLayoutData(gd_title);

		releaseDate = new Text(shell, SWT.BORDER);
		final GridData gd_releaseDate = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		releaseDate.setLayoutData(gd_releaseDate);

		director = new Text(shell, SWT.BORDER);
		final GridData gd_director = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		director.setLayoutData(gd_director);

		writer = new Text(shell, SWT.BORDER);
		final GridData gd_writer = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		writer.setLayoutData(gd_writer);

		bindUI();
	}

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

	public static class MovieInfo extends AbstractModelObject {
		private String title;
		private String releaseDate;
		private String director;
		private String writer;

		public MovieInfo(String title, String releaseDate, String director,
				String writer) {
			this.title = title;
			this.releaseDate = releaseDate;
			this.director = director;
			this.writer = writer;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			firePropertyChange("title", this.title, this.title = title);
		}

		public String getReleaseDate() {
			return releaseDate;
		}

		public void setReleaseDate(String releaseDate) {
			firePropertyChange("releaseDate", this.releaseDate,
					this.releaseDate = releaseDate);
		}

		public String getDirector() {
			return director;
		}

		public void setDirector(String director) {
			firePropertyChange("director", this.director,
					this.director = director);
		}

		public String getWriter() {
			return writer;
		}

		public void setWriter(String writer) {
			firePropertyChange("writer", this.writer, this.writer = writer);
		}
	}

	private void bindUI() {
		IObservableList movies = new WritableList();
		movies.add(new MovieInfo("007: Quantum of Solace", "October 31, 2008",
				"Marc Forster", "Robert Wade"));
		movies.add(new MovieInfo("Batman Begins", "June 15, 2005",
				"Christopher Nolan", "David S. Goyer"));
		movies.add(new MovieInfo("Cloverfield", "January 18, 2008",
				"Matt Reeves", "Drew Goddard"));
		movies.add(new MovieInfo("The Dark Knight", "July 18, 2008",
				"Christopher Nolan", "David S. Goyer"));
		movies.add(new MovieInfo("Finding Nemo", "May 30, 2003",
				"Andrew Stanton", "Andrew Stanton"));
		movies.add(new MovieInfo("Get Smart", "June 20, 2008", "Peter Segal",
				"Tom J. Astle"));
		movies.add(new MovieInfo(
				"Indiana Jones and the Kingdom of the Crystal Skull",
				"May 22, 2008", "Steven Spielberg", "Drunken Lemurs"));
		movies.add(new MovieInfo("Iron Man", "May 2, 2008", "Jon Favreau",
				"Mark Fergus"));
		movies.add(new MovieInfo("Raiders of the Lost Ark", "June 12, 1981",
				"Steven Spielberg", "George Lucas"));
		movies.add(new MovieInfo("Valkyrie", "December 25, 2008",
				"Bryan Singer", "Christopher McQuarrie"));
		movies.add(new MovieInfo("Wall-E", "June 27, 2008", "Andrew Stanton",
				"Andrew Stanton"));
		movies.add(new MovieInfo("Wanted", "June 27, 2008",
				"Timur Bekmambetov", "Michael Brandt"));

		ViewerSupport.bind(viewer, movies, BeanProperties.values(
				MovieInfo.class, new String[] { "title", "releaseDate",
						"director", "writer" }));

		// Select Batman Begins and The Dark Knight, which have the same
		// director and writer
		viewer.setSelection(new StructuredSelection(new Object[] {
				movies.get(1), movies.get(3) }));

		DataBindingContext dbc = new DataBindingContext();

		IObservableList selections = ViewerProperties.multipleSelection()
				.observe(viewer);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(title),
				DuplexingObservableValue.withDefaults(BeanProperties.value(
						"title").observeDetail(selections), "",
						"<Multiple titles>"));
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(releaseDate),
				DuplexingObservableValue.withDefaults(BeanProperties.value(
						"releaseDate").observeDetail(selections), "",
						"<Multiple dates>"));
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(director),
				DuplexingObservableValue.withDefaults(BeanProperties.value(
						"director").observeDetail(selections), "",
						"<Multiple directors>"));
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(writer),
				DuplexingObservableValue.withDefaults(BeanProperties.value(
						"writer").observeDetail(selections), "",
						"<Multiple writers>"));
	}
}
