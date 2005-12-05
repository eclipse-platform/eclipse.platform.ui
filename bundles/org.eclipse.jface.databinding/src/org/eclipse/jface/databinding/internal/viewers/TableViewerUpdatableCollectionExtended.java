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
package org.eclipse.jface.databinding.internal.viewers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.converter.IConverter;
import org.eclipse.jface.databinding.converters.IdentityConverter;
import org.eclipse.jface.databinding.validator.IValidator;
import org.eclipse.jface.databinding.viewers.TableViewerDescription;
import org.eclipse.jface.databinding.viewers.TableViewerDescription.Column;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;

/**
 * @since 3.2
 * 
 */
public class TableViewerUpdatableCollectionExtended extends
		TableViewerUpdatableCollection {
	
	class TreeLableProvider extends LabelProvider implements
			ITableLabelProvider {
		private Object getValue(Object element, Column column) {
			Object value = tableViewerDescription.getCellModifier().getValue(
					element, column.getPropertyName());
			return value;
		}

		private Object getConvertedValue(Object element, Column column) {
			Object value = getValue(element, column);
			Object convertedValue = column.getConverter().convertModelToTarget(
					value);
			return convertedValue;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex < tableViewerDescription.getColumnCount()
					&& columnIndex >= 0) {
				Column column = getColumn(columnIndex);
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
			Object convertedValue = null;
			if (columnIndex < tableViewerDescription.getColumnCount()
					&& columnIndex >= 0) {
				Column column = getColumn(columnIndex);
				convertedValue = getConvertedValue(element, column);
				IConverter converter = column.getConverter();
				if (converter.getTargetType().equals(ViewerLabel.class)) {
					ViewerLabel viewerLabel = (ViewerLabel) convertedValue;
					return viewerLabel.getText();
				}
			}
			return convertedValue == null ? "" : (String) convertedValue; //$NON-NLS-1$
		}

		// In case that no TreeColumn() was added to the visual
		public Image getImage(Object element) {
			return getColumnImage(element, 0);
		}

		public String getText(Object element) {
			return getColumnText(element, 0);
		}

	}

	private final TableViewerDescription tableViewerDescription;
			
	private ITableLabelProvider tableLabelProvider = new TreeLableProvider(); 

	private IDataBindingContext dataBindingContext;

	/**
	 * @param tableViewerDescription
	 * @param dataBindingContext 
	 */
	public TableViewerUpdatableCollectionExtended(
			TableViewerDescription tableViewerDescription, IDataBindingContext dataBindingContext) {
		super(tableViewerDescription.getTableViewer());
		this.tableViewerDescription = tableViewerDescription;
		fillDescriptionDefaults(dataBindingContext);
		TableViewer tableViewer = tableViewerDescription.getTableViewer();
		// TODO synchronize columns on the widget side (create missing columns,
		// set name if not already set)
		tableViewer.setLabelProvider(tableLabelProvider);
		tableViewer.setCellModifier(tableViewerDescription.getCellModifier());
		int columnCount = tableViewerDescription.getColumnCount();
		String[] columnProperties = new String[columnCount];
		CellEditor[] cellEditors = new CellEditor[columnCount];
		for (int i = 0; i < columnCount; i++) {
			Column column = tableViewerDescription.getColumn(i);
			columnProperties[i] = column.getPropertyName();
			if(column.isEditable()){
				cellEditors[i] = column.getCellEditor();
			}
		}
		tableViewer.setColumnProperties(columnProperties);
		tableViewer.setCellEditors(cellEditors);
	}
	
	protected CellEditor createCellEditor(final Column column) {
		return new TextCellEditor(tableViewerDescription.getTableViewer().getTable()) {				
			protected void doSetValue(Object value) {
				super.doSetValue( column.getConverter().convertModelToTarget(value));
			}
			protected Object doGetValue() {
				String textValue = (String)super.doGetValue();
				return column.getConverter().convertTargetToModel(textValue);
			}
		};
		
	}
	
	protected ICellModifier createCellModifier(final IDataBindingContext dataBindingContext) {
		return new ICellModifier() {
			private Column findColumn(String property) {
				for (int i = 0; i < tableViewerDescription.getColumnCount(); i++) {
					Column column = tableViewerDescription.getColumn(i);
					if (column.getPropertyName().equals(property)) {
						return column;
					}
				}
				return null;
			}

			public boolean canModify(Object element, String property) {
				Column column = findColumn(property);
				return column != null && column.isEditable();
			}

			private List getNestedPorperties (String properties) {	
				//TODO jUnit for nested column
				StringTokenizer stk = new StringTokenizer(properties,TableViewerDescription.COLUMN_PROPERTY_NESTING_SEPERATOR);
				List list = new ArrayList(stk.countTokens());
				while(stk.hasMoreTokens())
					list.add(stk.nextElement());
				return list;					
			}
			
			private Object getGetterValue(Object root, List properties) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
				Object result=root;					
				for (int i = 0; i < properties.size(); i++) {
					if (result==null) return null;
					String prop = (String) properties.get(i);
					Method getter = result.getClass().getMethod(
							"get"+ prop.substring(0, 1).toUpperCase(Locale.ENGLISH) + prop.substring(1), new Class[0]); //$NON-NLS-1$
					result=getter.invoke(result, new Object[0]);						
				}
				return result;
			}
			
			public Object getValue(Object element, String property) {
				Column column = findColumn(property);
				if (column == null) {
					return null;
				}
				try {						
					return getGetterValue(element,getNestedPorperties(property));
				} catch (SecurityException e) {
					// TODO log
				} catch (NoSuchMethodException e) {
					// TODO log
				} catch (IllegalArgumentException e) {
					// TODO log
				} catch (IllegalAccessException e) {
					// TODO log
				} catch (InvocationTargetException e) {
					// TODO log
				}
				return null;
			}

			public void modify(Object element, String property, Object value) {
				Column column = findColumn(property);
				if (column == null) {
					return;
				}
				if (element instanceof Item) {
					element = ((Item) element).getData();
				}
				IValidator columnValidator = column.getValidator();
				if(columnValidator != null){
					String errorMessage = columnValidator.isValid(value);
					if(errorMessage != null){
						dataBindingContext.updateValidationError(new IChangeListener(){
							public void handleChange(ChangeEvent changeEvent) {									
							}								
						},errorMessage);
						return;
					}
				}
				try {
					List getters = getNestedPorperties(property);
					String setterSig;
					Object target;
					if (getters.size()>1) {
						setterSig = (String) getters.get(getters.size()-1);
						getters.remove(getters.size()-1);
						target=getGetterValue(element,getters);
					}
					else {
						setterSig = property;
						target = element;
					}
						
					Method setter = target
							.getClass()
							.getMethod(
									"set"	+ setterSig.substring(0, 1).toUpperCase(Locale.ENGLISH) + setterSig.substring(1), new Class[] { column.getConverter().getModelType() }); //$NON-NLS-1$
					setter.invoke(target, new Object[] { value });
					tableViewerDescription.getTableViewer().refresh(element);
					return;
				} catch (SecurityException e) {
					// TODO log
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO log
					e.printStackTrace();					
				} catch (IllegalArgumentException e) {
					// TODO log
					e.printStackTrace();					
				} catch (IllegalAccessException e) {
					// TODO log
					e.printStackTrace();					
				} catch (InvocationTargetException e) {
					// TODO log
					e.printStackTrace();					
				}
			}
		};
	}
	class ColumnInfo{
		boolean converterDefaulted;
		boolean validatorDefaulted;
		boolean cellEditorDefaulted;
	}
	private ColumnInfo[] columnInfos;
	private void fillDescriptionDefaults(final IDataBindingContext dataBindingContext) {
		columnInfos = new ColumnInfo[tableViewerDescription.getColumnCount()];
		this.dataBindingContext = dataBindingContext;		
		for (int i = 0; i < tableViewerDescription.getColumnCount(); i++) {
			Column column = tableViewerDescription.getColumn(i);
			columnInfos[i] = new ColumnInfo();
			if (column.getConverter() == null) {
				columnInfos[i].converterDefaulted = true;
				if (column.getPropertyType()!=null)
					initializeColumnConverter(column,column.getPropertyType());
				else
				   column.setConverter(new IdentityConverter(String.class));
			}
			if (column.getValidator() == null && column.isEditable()) {
				columnInfos[i].validatorDefaulted = true;
				initializeColumnValidator(column, column.getPropertyType());
			}
			if (column.getCellEditor() == null && column.isEditable()) {
				columnInfos[i].cellEditorDefaulted = true;
				column.setCellEditor(createCellEditor(column));
			}
		}
		if (tableViewerDescription.getCellModifier() == null) {
			tableViewerDescription.setCellModifier(createCellModifier(dataBindingContext));
		}
	}
	
	private void initializeColumnConverter(Column column, Class propertyType){
		   column.setConverter(dataBindingContext.createConverter(String.class, propertyType, tableViewerDescription));		
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
	
	public int addElement(Object element, int index) {
		// If first time through and any of the columns had default information supplied then we may now have
		// more detailed information available from the explicit type which means we can calculate better
		// converters, validators and cell editors
		if(columnInfos != null){
			for (int i = 0; i < tableViewerDescription.getColumnCount(); i++) {
				Column column = tableViewerDescription.getColumn(i);
				ColumnInfo columnInfo = columnInfos[i];
				if(column.getPropertyType() == null && (columnInfo.cellEditorDefaulted || columnInfo.converterDefaulted || columnInfo.validatorDefaulted)){
					// Work out the type of the column from the property name from the element type itself
					Property columnPropertyDesc = new Property(element,column.getPropertyName());
					IUpdatableValue dummyUpdatable = (IUpdatableValue) dataBindingContext.createUpdatable(columnPropertyDesc);
					Class columnType = dummyUpdatable.getValueType();
					if(columnType != null){
						// We have a more explicit property type that was supplied
						if(columnInfo.converterDefaulted){	
							initializeColumnConverter(column,columnType);
						}
						if(columnInfo.validatorDefaulted){
							initializeColumnValidator(column,columnType);
						}
						if(columnInfo.cellEditorDefaulted){
							createCellEditor(column);							
						}
					}
				}
			}
			columnInfos = null;	// Clear the variable so we don't re-default again
		}
		return super.addElement(element, index);
	}

	protected Column getColumn(int columnIndex) {
		return tableViewerDescription.getColumn(columnIndex);
	}

}
