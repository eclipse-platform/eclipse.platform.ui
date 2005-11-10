package org.eclipse.jface.databinding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 *
 */
public class TableViewerDescription {

	/**
	 * @since 3.2
	 *
	 */
	public static class Column {
		private String name;

		private String propertyName;

		private IValidator validator;

		private IConverter converter;

		private CellEditor cellEditor;

		/**
		 * @param name
		 * @param propertyName
		 * @param cellEditor
		 * @param validator
		 * @param converter
		 */
		public Column(String name, String propertyName, CellEditor cellEditor,
				IValidator validator, IConverter converter) {
			this.name = name;
			this.propertyName = propertyName;
			this.cellEditor = cellEditor;
			this.validator = validator;
			this.converter = converter;
		}

		/**
		 * @return
		 */
		public IConverter getConverter() {
			return converter;
		}

		/**
		 * @return
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return
		 */
		public String getPropertyName() {
			return propertyName;
		}

		/**
		 * @return
		 */
		public IValidator getValidator() {
			return validator;
		}

		/**
		 * @param converter
		 */
		public void setConverter(IConverter converter) {
			this.converter = converter;
		}

		/**
		 * @param validator
		 */
		public void setValidator(IValidator validator) {
			this.validator = validator;
		}

		/**
		 * @return
		 */
		public CellEditor getCellEditor() {
			return cellEditor;
		}

		/**
		 * @param cellEditor
		 */
		public void setCellEditor(CellEditor cellEditor) {
			this.cellEditor = cellEditor;
		}
	}

	private TableViewer tableViewer;

	private List columns = new ArrayList();

	private ICellModifier cellModifier = null;

	/**
	 * @param tableViewer
	 */
	public TableViewerDescription(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}

	/**
	 * @param columnName
	 * @param propertyName
	 * @param cellEditor 
	 * @param validator
	 * @param converter
	 *            converter from model objects to String, or ImageAndString.
	 */
	public void addColumn(String columnName, String propertyName,
			CellEditor cellEditor, IValidator validator, IConverter converter) {
		columns.add(new Column(columnName, propertyName, cellEditor, validator,
				converter));
	}

	/**
	 * @param columnName
	 * @param propertyName
	 * @param validator
	 * @param converter
	 */
	public void addColumn(String columnName, String propertyName,
			IValidator validator, IConverter converter) {
		columns.add(new Column(columnName, propertyName, null, validator,
				converter));
	}

	/**
	 * @param columnName
	 * @param propertyName
	 */
	public void addColumn(String columnName, String propertyName) {
		addColumn(columnName, propertyName, null, null, null);
	}

	/**
	 * @return
	 */
	public ICellModifier getCellModifier() {
		return cellModifier;
	}

	/**
	 * @param cellModifier
	 */
	public void setCellModifier(ICellModifier cellModifier) {
		this.cellModifier = cellModifier;
	}

	/**
	 * @return
	 */
	public int getColumnCount() {
		return columns.size();
	}

	/**
	 * @param columnIndex
	 * @return
	 */
	public Column getColumn(int columnIndex) {
		return (Column) columns.get(columnIndex);
	}

	/**
	 * @return
	 */
	public TableViewer getTableViewer() {
		return tableViewer;
	}

}
