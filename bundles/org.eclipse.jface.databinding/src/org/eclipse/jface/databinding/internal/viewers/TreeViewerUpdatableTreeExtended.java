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
package org.eclipse.jface.databinding.internal.viewers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.converter.IConverter;
import org.eclipse.jface.databinding.converters.IdentityConverter;
import org.eclipse.jface.databinding.validator.IValidator;
import org.eclipse.jface.databinding.viewers.TableViewerDescription;
import org.eclipse.jface.databinding.viewers.TreeViewerDescription;
import org.eclipse.jface.databinding.viewers.TableViewerDescription.Column;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
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
	
	private HashMap columnInfos = new HashMap();
	
	private IDataBindingContext dataBindingContext;

	private final TreeViewerDescription treeViewerDescription;
	
	private class SetterMethod {
		private Object target;
		private String setterProperty;
		private Method setter;
		
		
		/**
		 * @param element
		 * @param column
		 */
		public SetterMethod (Object element, Column column) {			    
				try {
					List getters = getNestedPorperties(column.getPropertyName());								
					if (getters.size()>1) {
						setterProperty = (String) getters.get(getters.size()-1);
						getters.remove(getters.size()-1);
						target=getGetterValue(element,getters);
					}
					else {
						setterProperty = column.getPropertyName();
						target = element;
					}
						
					setter = target.getClass().getMethod(
									"set" + setterProperty.substring(0, 1).toUpperCase(Locale.ENGLISH) + setterProperty.substring(1), new Class[] { column.getConverter().getModelType() }); //$NON-NLS-1$
				} catch (SecurityException e) {
					// TODO log
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO log
				} catch (NoSuchMethodException e) {
				} catch (IllegalAccessException e) {
					// TODO log
				} catch (InvocationTargetException e) {
					// TODO log
				}				
			}
			/**
			 * @return true, if a setter method exists
			 */
			public boolean isValid() {
				return setter!=null;
			}
			
			/**
			 * @param argument
			 * @return invocation result
			 */
			public Object invoke(Object argument) {
				if (isValid()) {
					try {
						return setter.invoke(target, new Object[] { argument} );
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return null;
			}
			/**
			 * @return setter's property
			 */
			public String getSetterProperty() {
				return setterProperty;
			}
			/**
			 * @return target
			 */
			public Object getTarget() {
				return target;
			}
	}
	
	private ITableLabelProvider tableLabelProvider = new ITableLabelProvider() {

		private Object getValue(Object element, Column column) {
			Object value = treeViewerDescription.getCellModifier().getValue(
					element, Integer.toString(treeViewerDescription.getColumnIndex(element.getClass(), column)));
			return value;
		}

		private Object getConvertedValue(Object element, Column column) {
			Object value = getValue(element, column);
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

		public void addListener(ILabelProviderListener listener) {
			// ignore
		}

		public void dispose() {
			// ignore
		}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void removeListener(ILabelProviderListener listener) {
			// ignore
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
		tableViewer.setLabelProvider(tableLabelProvider);
		tableViewer.setCellModifier(treeViewerDescription.getCellModifier());
		int columnCount = getColumnCount();
		String[] columnProperties = new String[columnCount];
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
					if (customEditor!=column.getCellEditor()) //TODO is it really the case that a single cell editor is set for a column 
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
				if (column!=null) {
					return new SetterMethod(element, column).isValid(); 
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
					Object value = getGetterValue(element,getNestedPorperties(column.getPropertyName()));
					return column.getConverter().convertModelToTarget(value);
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
						dataBindingContext.updateValidationError(new IChangeListener(){
							public void handleChange(ChangeEvent changeEvent) {									
							}								
						},errorMessage);
						return;
					}
				}
				new SetterMethod(element, column).invoke(value);
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
	protected Column getColumn(Class instanceType, int columnIndex) {
		return treeViewerDescription.getColumn(instanceType, columnIndex);
	}
	
	public int addElement(final Object parentElement, int index, final Object value) {
		// Verify defaults, the first time data cames through
		if(columnInfos != null && value!=null){
			for (int i = 0; i < treeViewerDescription.getColumnCount(value.getClass()); i++) {
				Column column = treeViewerDescription.getColumn(value.getClass(), i);
				ColumnInfo columnInfo = (ColumnInfo) columnInfos.remove(column);
				if (columnInfo!=null) {
					if(column.getPropertyType() == null && (columnInfo.cellEditorDefaulted || columnInfo.converterDefaulted || columnInfo.validatorDefaulted)){
						// Work out the type of the column from the property name from the element type itself
						SetterMethod setter = new SetterMethod(value, column);
						// resolve nesting
						Property columnPropertyDesc = new Property(setter.getTarget(), setter.getSetterProperty());
						
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
//							if(columnInfo.cellEditorDefaulted){
//								createCellEditor(column);							
//							}
						}
					}
				}
			}			
			if (columnInfos.isEmpty())
				columnInfos = null;	// No More checking
		}
		return super.addElement(parentElement, index, value);
	}
}
