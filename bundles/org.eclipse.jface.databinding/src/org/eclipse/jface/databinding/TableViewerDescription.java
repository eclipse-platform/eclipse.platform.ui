/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding;

import java.util.ArrayList;
import java.util.List;

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
		private String name;

		private String propertyName;
		
		private Class  propertyType;

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
		public Column(String propertyName, CellEditor cellEditor,
				IValidator validator, IConverter converter) {
			this.propertyName = propertyName;
			this.cellEditor = cellEditor;
			this.validator = validator;
			this.converter = converter;
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
	public void addColumn(int columnIndex, String propertyName,
			CellEditor cellEditor, IValidator validator, IConverter converter) {
		Column column = new Column(propertyName, cellEditor , validator,
				converter);
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
	public void addColumn(String propertyName,
			CellEditor cellEditor, IValidator validator, IConverter converter) {
		addColumn(-1,propertyName,cellEditor,validator,converter);
	}

	/**
	 * @param columnIndex
	 * @param propertyName
	 * @param validator
	 * @param converter
	 */
	public void addColumn(int columnIndex, String propertyName,
			IValidator validator, IConverter converter) {
		addColumn(columnIndex,propertyName,null,validator,converter);
	}
	
	
	/**
	 * @param propertyName
	 * @param validator
	 * @param converter
	 */
	public void addColumn(String propertyName,
			IValidator validator, IConverter converter) {	
		addColumn(-1,propertyName,validator,converter);
	}

	/**
	 * @param propertyName
	 */
	public void addColumn(String propertyName) {
		addColumn(-1, propertyName, null, null, null);
	}
	
	/**
	 * @param columnIndex
	 * @param propertyName
	 */
	public void addColumn(int columnIndex, String propertyName) {
		addColumn(columnIndex, propertyName, null, null, null);
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
