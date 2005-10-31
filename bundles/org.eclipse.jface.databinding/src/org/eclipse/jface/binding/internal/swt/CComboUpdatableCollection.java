package org.eclipse.jface.binding.internal.swt;

import org.eclipse.jface.binding.IChangeEvent;
import org.eclipse.jface.binding.IUpdatableCollection;
import org.eclipse.jface.binding.Updatable;
import org.eclipse.jface.binding.swt.SWTBindingConstants;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

/**
 * @since 3.2
 *
 */
public class CComboUpdatableCollection extends Updatable implements IUpdatableCollection {
	
	private final CCombo ccombo;

	private final String attribute;

	private boolean updating = false;

	/**
	 * @param ccombo
	 * @param attribute
	 */
	public CComboUpdatableCollection(CCombo ccombo, String attribute) {
		this.ccombo = ccombo;
		
		
		if (attribute.equals(SWTBindingConstants.CONTENT))
			this.attribute = SWTBindingConstants.ITEMS;
		else
			this.attribute = attribute;
		
		if (this.attribute.equals(SWTBindingConstants.ITEMS)) {
			ccombo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updating) {
						fireChangeEvent(IChangeEvent.CHANGE, null, null);
					}
				}
			});
		}
		else
			throw new IllegalArgumentException();
	}

	public int getSize() {
		return ccombo.getItemCount();
	}

	public int addElement(Object value, int index) {
		updating=true;		
		try {
			if (index<0 || index>getSize())
				index=getSize();
			String[] newItems = new String[getSize()+1];			
			System.arraycopy(ccombo.getItems(), 0, newItems,0, index);
			newItems[index]=(String)value;
			System.arraycopy(ccombo.getItems(), index, newItems,index+1, getSize()-index);
			ccombo.setItems(newItems);
			fireChangeEvent(IChangeEvent.ADD, null, value, index);
		}
		finally{
			updating=false;
		}
		return index;
	}

	public void removeElement(int index) {
		updating=true;		
		try {
			if (index<0 || index>getSize())
				index=getSize();
			String[] newItems = new String[getSize()-1];
			String old = ccombo.getItem(index);
			System.arraycopy(ccombo.getItems(), 0, newItems,0, index);			
			System.arraycopy(ccombo.getItems(), index, newItems,index-1, getSize()-index);			
			ccombo.setItems(newItems);
			fireChangeEvent(IChangeEvent.REMOVE, old, null, index);
		}
		finally{
			updating=false;
		}		
	}

	public void setElement(int index, Object value) {
		String old = ccombo.getItem(index);
		ccombo.setItem(index, (String)value);
		fireChangeEvent(IChangeEvent.CHANGE, old, value, index);
	}

	public Object getElement(int index) {
		return ccombo.getItem(index);
	}

	public Class getElementType() {
		return String.class;
	}

}
