package org.eclipse.jface.databinding.viewers;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.databinding.converter.IConverter;
import org.eclipse.jface.databinding.validator.IValidator;
import org.eclipse.jface.databinding.viewers.TableViewerDescription.Column;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @since 3.2
 *
 */
public class TreeViewerDescription {

	/**
	 * Table column may spedify a nested property
	 */
	public static final String COLUMN_PROPERTY_NESTING_SEPERATOR = "."; //$NON-NLS-1$



	private TreeViewer treeViewer;
	
	private HashMap columnsMap = new HashMap();  // TODO Need to deal with instanceof vs. exact type
	

	private ICellModifier cellModifier = null;

	/**
	 * @param treeViewer
	 */
	public TreeViewerDescription(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	/**
	 * @param instanceType 
	 * @param columnIndex
	 * @param propertyName
	 * @param cellEditor 
	 * @param validator
	 * @param converter
	 *            converter from model objects to String, or ImageAndString.
	 */
	public void addColumn(Class instanceType, int columnIndex, String propertyName,
			CellEditor cellEditor, IValidator validator, IConverter converter) {
		List columns = (List)columnsMap.get(instanceType);
		if (columns==null) {
			columns = new ArrayList();
			columnsMap.put(instanceType, columns);
		}
		Column column = new Column(propertyName, cellEditor , validator,
				converter);
		if(columnIndex == -1)
			columns.add(column);			
		else 
			columns.add(columnIndex,column);			
	}
	
	/**
	 * @param instanceType 
	 * @param propertyName
	 * @param cellEditor
	 * @param validator
	 * @param converter
	 */
	public void addColumn(Class instanceType, String propertyName,
			CellEditor cellEditor, IValidator validator, IConverter converter) {
		addColumn(instanceType, -1,propertyName,cellEditor,validator,converter);
	}

	/**
	 * @param instanceType 
	 * @param columnIndex
	 * @param propertyName
	 * @param validator
	 * @param converter
	 */
	public void addColumn(Class instanceType, int columnIndex, String propertyName,
			IValidator validator, IConverter converter) {
		addColumn(instanceType, columnIndex,propertyName,null,validator,converter);
	}
	
	
	/**
	 * @param instanceType 
	 * @param propertyName
	 * @param validator
	 * @param converter
	 */
	public void addColumn(Class instanceType, String propertyName,
			IValidator validator, IConverter converter) {	
		addColumn(instanceType, -1,propertyName,validator,converter);
	}

	/**
	 * @param instanceType 
	 * @param propertyName
	 */
	public void addColumn(Class instanceType, String propertyName) {
		addColumn(instanceType, -1, propertyName, null, null, null);
	}
	
	/**
	 * @param instanceType 
	 * @param columnIndex
	 * @param propertyName
	 */
	public void addColumn(Class instanceType, int columnIndex, String propertyName) {
		addColumn(instanceType, columnIndex, propertyName, null, null, null);
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
	 * @param instanceType 
	 * @return columnCount  The number of Columns
	 */
	public int getColumnCount(Class instanceType) {
		List columns = (List) columnsMap.get(instanceType);
		if (columns!=null)
		  return columns.size();
		return 0;
	}

	/**
	 * @param instanceType 
	 * @param columnIndex
	 * @return Column  The column at the specified index
	 */
	public Column getColumn(Class instanceType, int columnIndex) {
		List columns = (List) columnsMap.get(instanceType);
		if (columns!=null && columns.size()>columnIndex)
		   return (Column) columns.get(columnIndex);
		return null;
	}
	

	/**
	 * @param instanceType
	 * @param column
	 * @return column's index, or -1 if it is not a valid column
	 */
	public int getColumnIndex(Class instanceType, Column column) {
		List columns = (List) columnsMap.get(instanceType);
		if (columns!=null)
			return columns.indexOf(column);
		return -1;
	}

	/**
	 * @return treeViewer
	 */
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
	
	public Class[] getClassTypes() {
		if (columnsMap.keySet().size()==0) return null;
		
		return (Class[]) columnsMap.keySet().toArray(new Class[columnsMap.keySet().size()]);
	}


}
