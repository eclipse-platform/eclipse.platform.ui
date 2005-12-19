/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.databinding.converter.IConverter;
import org.eclipse.jface.databinding.validator.IValidator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;

/**
 * A description object for table viewers.
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
	 * Table column may spedify a nested property
	 */
	public static final String COLUMN_PROPERTY_NESTING_SEPERATOR = "."; //$NON-NLS-1$

	/**
	 * @since 3.2
	 *
	 */
	public static class Column {

		private String propertyName;
		
		private Class  propertyType;

		private IValidator validator;

		private IConverter converter;

		private CellEditor cellEditor;
		
		private boolean readOnly = true;

		/**
		 * @param propertyName
		 * @param cellEditor
		 * @param validator
		 * @param converter
		 */
		public Column(String propertyName, CellEditor cellEditor,
				IValidator validator, IConverter converter) {
			this.propertyName = propertyName;
			this.cellEditor = cellEditor;
			this.validator = validator;
			this.converter = converter;
			readOnly = false;
		}

		
		Column(String propertyName, IConverter converter) {
			this.propertyName = propertyName;
			this.converter = converter;
			readOnly = true;
		}

		/**
		 * @return IConverter
		 */
		public IConverter getConverter() {
			return converter;
		}

		/**
		 * @return String name
		 */
		public String getPropertyName() {
			return propertyName;
		}

		/**
		 * @return IValidator
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
		 * @return CellEditor
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

		/**
		 * @return property Class
		 */
		public Class getPropertyType() {
			return propertyType;
		}

		/**
		 * @param propertyType
		 */
		public void setPropertyType(Class propertyType) {
			this.propertyType = propertyType;
		}


		/**
		 * @return boolean for whether the column is editable
		 */
		public boolean isEditable() {
			return !readOnly;
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
	 * @param columnIndex
	 * @param propertyName
	 * @param cellEditor 
	 * @param validator
	 * @param converter
	 *            converter from model objects to String, or ImageAndString.
	 */
	public void addEditableColumn(int columnIndex, String propertyName,
			CellEditor cellEditor, IValidator validator, IConverter converter) {
		Column column = new Column(propertyName, cellEditor , validator,
				converter);
		if(columnIndex == -1){
			columns.add(column);
		} else {
			columns.add(columnIndex,column);				
		}	
	}
	
	private void addReadOnlyColumn(int columnIndex, String propertyName, IConverter converter) {
		Column column = new Column(propertyName, converter);
		if(columnIndex == -1){
			columns.add(column);
		} else {
			columns.add(columnIndex,column);				
		}	
	}	
	
	/**
	 * @param propertyName
	 * @param cellEditor
	 * @param validator
	 * @param converter
	 */
	public void addEditableColumn(String propertyName,
			CellEditor cellEditor, IValidator validator, IConverter converter) {
		addEditableColumn(-1,propertyName,cellEditor,validator,converter);
	}

	/**
	 * @param columnIndex
	 * @param propertyName
	 * @param converter
	 */
	public void addColumn(int columnIndex, String propertyName,IConverter converter) {
		addReadOnlyColumn(columnIndex,propertyName,converter);
	}
	
	
	/**
	 * @param propertyName
	 * @param converter
	 */
	public void addColumn(String propertyName,IConverter converter) {	
		addReadOnlyColumn(-1,propertyName,converter);
	}

	/**
	 * Add a column that is readOnly
	 * @param propertyName
	 */
	public void addColumn(String propertyName) {
		addReadOnlyColumn(-1, propertyName, null);
	}
	

	/**
	 * Add a column that is editable.  The cell editor will be defaulted based on the type of the property
	 * or can be explicitly set using the more verbose API that includes the CellEditor argument
	 * @param propertyName
	 */
	public void addEditableColumn(String propertyName) {
		addEditableColumn(-1, propertyName);
	}	
	
	/**
	 * Add a column that is editable.  The cell editor will be defaulted based on the type of the property
	 * or can be explicitly set using the more verbose API that includes the CellEditor argument
	 * @param index				The column index
	 * @param propertyName		The property name
	 */
	public void addEditableColumn(int index, String propertyName) {
		addEditableColumn(index, propertyName,null,null,null);
	}	
	
	/**
	 * @param columnIndex
	 * @param propertyName
	 */
	public void addColumn(int columnIndex, String propertyName) {
		addReadOnlyColumn(columnIndex, propertyName, null);
	}	

	/**
	 * @return cellModifier   The cell Modifier
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
	 * @return columnCount  The number of Columns
	 */
	public int getColumnCount() {
		return columns.size();
	}

	/**
	 * @param columnIndex
	 * @return Column  The column at the specified index
	 */
	public Column getColumn(int columnIndex) {
		return (Column) columns.get(columnIndex);
	}

	/**
	 * @return tableViewer
	 */
	public TableViewer getTableViewer() {
		return tableViewer;
	}

}
