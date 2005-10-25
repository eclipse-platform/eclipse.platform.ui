package org.eclipse.jface.binding.swt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.binding.IConverter;
import org.eclipse.jface.binding.IValidator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;

public class TableViewerDescription {

	public static interface ImageAndString {
		public Image getImage();

		public String getString();
	}

	public static class Column {
		private String name;

		private String propertyName;

		private IValidator validator;

		private IConverter converter;

		private CellEditor cellEditor;

		public Column(String name, String propertyName, CellEditor cellEditor,
				IValidator validator, IConverter converter) {
			this.name = name;
			this.propertyName = propertyName;
			this.cellEditor = cellEditor;
			this.validator = validator;
			this.converter = converter;
		}

		public IConverter getConverter() {
			return converter;
		}

		public String getName() {
			return name;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public IValidator getValidator() {
			return validator;
		}

		public void setConverter(IConverter converter) {
			this.converter = converter;
		}

		public void setValidator(IValidator validator) {
			this.validator = validator;
		}

		public CellEditor getCellEditor() {
			return cellEditor;
		}

		public void setCellEditor(CellEditor cellEditor) {
			this.cellEditor = cellEditor;
		}
	}

	private TableViewer tableViewer;

	private List columns = new ArrayList();

	private ICellModifier cellModifier = null;

	public TableViewerDescription(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}

	/**
	 * @param columnName
	 * @param propertyName
	 * @param validator
	 * @param converter
	 *            converter from model objects to String, or ImageAndString.
	 */
	public void addColumn(String columnName, String propertyName,
			CellEditor cellEditor, IValidator validator, IConverter converter) {
		columns.add(new Column(columnName, propertyName, cellEditor, validator,
				converter));
	}

	public void addColumn(String columnName, String propertyName,
			IValidator validator, IConverter converter) {
		columns.add(new Column(columnName, propertyName, null, validator,
				converter));
	}

	public void addColumn(String columnName, String propertyName) {
		addColumn(columnName, propertyName, null, null, null);
	}

	public ICellModifier getCellModifier() {
		return cellModifier;
	}

	public void setCellModifier(ICellModifier cellModifier) {
		this.cellModifier = cellModifier;
	}

	public int getColumnCount() {
		return columns.size();
	}

	public Column getColumn(int columnIndex) {
		return (Column) columns.get(columnIndex);
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

}
