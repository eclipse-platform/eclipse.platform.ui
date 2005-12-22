/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.internal.databinding.viewers;

import java.util.HashMap;

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.converter.IConverter;
import org.eclipse.jface.databinding.converters.IdentityConverter;
import org.eclipse.jface.databinding.validator.IValidator;
import org.eclipse.jface.databinding.viewers.TreeViewerDescription;
import org.eclipse.jface.databinding.viewers.TableViewerDescription.Column;
import org.eclipse.jface.internal.databinding.beans.PropertyHelper;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;

/**
 * @since 3.2
 *
 */
public class TreeViewerUpdatableTreeExtended extends TreeViewerUpdatableTree {
	
	class ColumnInfo{ // boolean default value is false
		boolean converterDefaulted;
		boolean validatorDefaulted;
		boolean cellEditorDefaulted;
	}
	
	class TreeLabelProvider extends LabelProvider implements ITableLabelProvider {
		private Object getValue(Object element, Column column) {
			Object value = treeViewerDescription.getCellModifier().getValue(
					element, Integer.toString(treeViewerDescription.getColumnIndex(element.getClass(), column)));
			return value;
		}

		private Object getConvertedValue(Object element, Column column) {
			String propertyName = column.getPropertyName();
			Object value;
			if (propertyName == null) 
				value = element;
			else 
				value = getValue(element, column);
			
			Object convertedValue = column.getConverter().convertModelToTarget(
					value);
			return convertedValue;
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex<treeViewerDescription.getColumnCount(element.getClass()) && columnIndex>=0) {
				Column column = getColumn(element.getClass(), columnIndex);
				IConverter converter = column.getConverter();
				if (converter.getTargetType().equals(ViewerLabel.class)) {
					ViewerLabel viewerLabel = (ViewerLabel) getConvertedValue(
							element, column);
					return viewerLabel.getImage();
				}
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			Object convertedValue=null;
			if (columnIndex<treeViewerDescription.getColumnCount(element.getClass()) && columnIndex>=0) {
			  Column column = getColumn(element.getClass(), columnIndex);
			  convertedValue = getConvertedValue(element, column);
			  IConverter converter = column.getConverter();
			  if (converter.getTargetType().equals(ViewerLabel.class)) {
					ViewerLabel viewerLabel = (ViewerLabel) convertedValue;
					return viewerLabel.getText();
			  }				
			}
			return convertedValue==null? "": (String) convertedValue; //$NON-NLS-1$
		}

		// support the case where a TreeColumn() was not placed on the tree
		public Image getImage(Object element) {			
			return getColumnImage(element,0);
		}

		public String getText(Object element) {
			return getColumnText(element, 0);
		}
	}
	

	
	private HashMap columnInfos = new HashMap();
	
	private IDataBindingContext dataBindingContext;

	private final TreeViewerDescription treeViewerDescription;

	private final IChangeListener dummyListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
		}
	};

	/**
	 * @param treeViewerDescription
	 * @param dataBindingContext 
	 */
	public TreeViewerUpdatableTreeExtended(
			TreeViewerDescription treeViewerDescription, IDataBindingContext dataBindingContext) {
		super(treeViewerDescription.getTreeViewer(), treeViewerDescription.getClassTypes());
		this.treeViewerDescription = treeViewerDescription;
		this.dataBindingContext=dataBindingContext;
		
		fillDescriptionDefaults(dataBindingContext);
		TreeViewer tableViewer = treeViewerDescription.getTreeViewer();
		// TODO synchronize columns on the widget side (create missing columns,
		// set name if not already set)
		tableViewer.setLabelProvider(new TreeLabelProvider());
		tableViewer.setCellModifier(treeViewerDescription.getCellModifier());
		int columnCount = getColumnCount();
		String[] columnProperties = new String[columnCount];
		//	TODO need to use the fix for Bug 113713
		//  It is possible that one column may have multiple cell editors
		CellEditor[] cellEditors = new CellEditor[columnCount];
		for (int i = 0; i < columnCount; i++) {
			columnProperties[i] = Integer.toString(i);
			cellEditors[i] = createCellEditor(i);
		}
		tableViewer.setColumnProperties(columnProperties);
		tableViewer.setCellEditors(cellEditors);
	}
	
	private int getColumnCount() {
		int count=0;
		Class[] types = treeViewerDescription.getClassTypes();
		for (int i = 0; i < types.length; i++) {
			int cc = treeViewerDescription.getColumnCount(types[i]);
			if (cc>count)
				count = cc;
		}
		return count;
	}
	
	protected CellEditor createCellEditor(final int columnIndex) {
		Class[] classTypes = treeViewerDescription.getClassTypes();
		CellEditor customEditor = null;
		for (int i = 0; i < classTypes.length; i++) {
			Column column = treeViewerDescription.getColumn(classTypes[i], columnIndex);
			if (column!=null && column.getCellEditor()!=null) {
				if (customEditor!=null) {
					if (customEditor!=column.getCellEditor()) // Limitation, see Bug 113713	 
						throw new BindingException("Different Cell Editors on Column: "+columnIndex); //$NON-NLS-1$
				}
				else
					customEditor = column.getCellEditor();				
			}			
		}
		
		if (customEditor==null) 			
		    // Use a default
			return new TextCellEditor(treeViewerDescription.getTreeViewer().getTree()) ;
		
		for (int i = 0; i < classTypes.length; i++) {
			Column column = treeViewerDescription.getColumn(classTypes[i], columnIndex);
			column.setCellEditor(customEditor);
		}
		return customEditor;
		
	}
	
	protected ICellModifier createCellModifier(final IDataBindingContext dataBindingContext) {
		return new ICellModifier() {

			private Column findColumn(Class instanceType, String property) {
				int index = Integer.parseInt(property);
				return treeViewerDescription.getColumn(instanceType, index);
			}

			public boolean canModify(Object element, String property) {
				if (element instanceof Item) {
					element = ((Item) element).getData();
				}
				Column column = findColumn(element.getClass(), property);
				if (column!=null && column.getPropertyName()!=null) {
					return new PropertyHelper(column.getPropertyName(), element.getClass()).canSet(element); 
				}
				return false;
			}

			
			public Object getValue(Object element, String property) {
				if (element instanceof Item) {
					element = ((Item) element).getData();
				}
				Column column = findColumn(element.getClass(), property);
				if (column == null) {
					return null;
				}
				try {								
					Object value = new PropertyHelper(column.getPropertyName(), element.getClass()).get(element);
					return column.getConverter().convertModelToTarget(value);
				} catch (SecurityException e) {
					// TODO log
				} catch (IllegalArgumentException e) {
					// TODO log
				}
				return null;
			}
			
			public void modify(Object element, String property, Object value) {
				if (element instanceof Item) {
					element = ((Item) element).getData();
				}
				Column column = findColumn(element.getClass(), property);
				if (column == null) {
					return;
				}
				value = column.getConverter().convertTargetToModel(value);
				IValidator columnValidator = column.getValidator();
				if(columnValidator != null){
					String errorMessage = columnValidator.isValid(value);
					if(errorMessage != null){
						dataBindingContext.updateValidationError(dummyListener, errorMessage);
						return;
					}
				}
				new PropertyHelper(column.getPropertyName(), element.getClass()).set(element, value);
				viewer.update(element, new String[] { property } );
			}
		};
	}
	
	private void initializeColumnConverter(Column column, Class propertyType){
		   column.setConverter(dataBindingContext.createConverter(String.class, propertyType, treeViewerDescription));		
	}
	
	private void initializeColumnValidator(Column column, Class propertyType){
		column.setValidator(new IValidator() {
			public String isPartiallyValid(Object value) {
				return null;
			}

			public String isValid(Object value) {
				return null;
			}
		});		
	}
	
	protected CellEditor createCellEditor(final Column column) {
		return new TextCellEditor(treeViewerDescription.getTreeViewer().getTree()) {				
			protected void doSetValue(Object value) {
				super.doSetValue( column.getConverter().convertModelToTarget(value));
			}
			protected Object doGetValue() {
				String textValue = (String)super.doGetValue();
				return column.getConverter().convertTargetToModel(textValue);
			}
		};
		
	}

	private void fillDescriptionDefaults(final IDataBindingContext dataBindingContext) {
		Class[] types = treeViewerDescription.getClassTypes();
		if (types==null) return;
		
		for (int i=0; i<types.length; i++) {
			int colCount = treeViewerDescription.getColumnCount(types[i]);					
			for (int j = 0; j < colCount; j++) {
				Column column = treeViewerDescription.getColumn(types[i], j);
				ColumnInfo info = new ColumnInfo();
				columnInfos.put(column, info);
				if (column.getConverter() == null) {
					info.converterDefaulted = true;
					if (column.getPropertyType()!=null)
						initializeColumnConverter(column,column.getPropertyType());
					else
					   column.setConverter(new IdentityConverter(String.class));
				}
				if (column.getValidator() == null) {
					info.validatorDefaulted = true;
					initializeColumnValidator(column, column.getPropertyType());
				}
// TODO need to use this when Bug 113713 is fixed				
//				if (column.getCellEditor() == null) {
//					info.cellEditorDefaulted = true;
//					column.setCellEditor(createCellEditor(column));
//				}
				
			}
		}
		if (treeViewerDescription.getCellModifier() == null) {			
			treeViewerDescription.setCellModifier(createCellModifier(dataBindingContext));
		}
		if (columnInfos.isEmpty())
			columnInfos=null;
	}


	
	protected Column getColumn(Class instanceType, int columnIndex) {
		return treeViewerDescription.getColumn(instanceType, columnIndex);
	}
	
	protected int addElement(final Object parentElement, int index, final Object value, boolean fire) {
		// Verify defaults, the first time data cames through
		if(columnInfos != null && value!=null){
			for (int i = 0; i < treeViewerDescription.getColumnCount(value.getClass()); i++) {
				Column column = treeViewerDescription.getColumn(value.getClass(), i);
				ColumnInfo columnInfo = (ColumnInfo) columnInfos.remove(column);
				if (columnInfo!=null && column.getPropertyName()!=null) {
					if(column.getPropertyType() == null && (columnInfo.cellEditorDefaulted || columnInfo.converterDefaulted || columnInfo.validatorDefaulted)){
						// Work out the type of the column from the property name from the element type itself
						PropertyHelper helper = new PropertyHelper(column.getPropertyName(), value.getClass());
						Class columnType =helper.getGetter().getReturnType();
						if(columnType != null){
							// We have a more explicit property type that was supplied
							if(columnInfo.converterDefaulted){	
								initializeColumnConverter(column,columnType);
							}
							if(columnInfo.validatorDefaulted){
								initializeColumnValidator(column,columnType);
							}
						}
					}
				}
			}			
			if (columnInfos.isEmpty())
				columnInfos = null;	// No More checking
		}
		return super.addElement(parentElement, index, value, fire);
	}
}
