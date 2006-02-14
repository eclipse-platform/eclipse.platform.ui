package org.eclipse.jface.internal.provisional.databinding.viewers;

import java.util.Map;

import org.eclipse.jface.internal.databinding.viewers.AutoSelectTableViewerCollection;
import org.eclipse.jface.internal.databinding.viewers.AutoSelectTableViewerCollectionExtended;
import org.eclipse.jface.internal.provisional.databinding.BindingException;
import org.eclipse.jface.internal.provisional.databinding.IDataBindingContext;
import org.eclipse.jface.internal.provisional.databinding.IUpdatable;
import org.eclipse.jface.internal.provisional.databinding.IUpdatableFactory;
import org.eclipse.jface.internal.provisional.databinding.Property;
import org.eclipse.jface.viewers.TableViewer;

/**
 * Implements string trimming semantics for any IUpdatableValue
 */
public class AutoSelectTableViewerFactory implements IUpdatableFactory {
   
   public IUpdatable createUpdatable(Map properties, Object description, IDataBindingContext bindingContext) throws BindingException {
      if (description instanceof Property) {
         Object object = ((Property) description)
         .getObject();
         Object attribute = ((Property) description)
         .getPropertyID();
         if (object instanceof TableViewer
               && ViewersProperties.CONTENT.equals(attribute)) {
            return new AutoSelectTableViewerCollection(
                  (TableViewer) object);
         }
      }
      if (description instanceof TableViewerDescription) {
				return new AutoSelectTableViewerCollectionExtended(
						(TableViewerDescription) description, bindingContext, 0);
	  }
      
      return null;
   }


}
