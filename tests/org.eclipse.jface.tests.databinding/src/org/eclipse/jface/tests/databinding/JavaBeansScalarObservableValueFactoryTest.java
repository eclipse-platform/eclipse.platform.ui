package org.eclipse.jface.tests.databinding;

import junit.framework.TestCase;

import org.eclipse.jface.internal.databinding.api.DataBinding;
import org.eclipse.jface.internal.databinding.api.IDataBindingContext;
import org.eclipse.jface.internal.databinding.api.IObservableFactory;
import org.eclipse.jface.internal.databinding.api.Property;
import org.eclipse.jface.internal.databinding.api.beans.JavaBeansScalarObservableValueFactory;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;

public class JavaBeansScalarObservableValueFactoryTest extends TestCase {
   
   public static class TestBean {
      private String field = "Hello, world";

      public String getField() {
         return field;
      }

      public void setField(String field) {
         this.field = field;
      }
   }
   
   public void test_getObservableValue() throws Exception {
      TestBean test = new TestBean();
      
      IDataBindingContext dbc = DataBinding.createContext(new IObservableFactory[] {
              new JavaBeansScalarObservableValueFactory()
        });
      IObservableValue observable = (IObservableValue) dbc.createObservable(new Property(test, "field"));
      assertEquals("Hello, world", observable.getValue());
   }
   
}
