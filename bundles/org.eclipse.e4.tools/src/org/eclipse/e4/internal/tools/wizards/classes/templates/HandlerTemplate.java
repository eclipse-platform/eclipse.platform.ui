package org.eclipse.e4.internal.tools.wizards.classes.templates;

import org.eclipse.e4.internal.tools.wizards.classes.NewHandlerClassWizard.HandlerClass;

public class HandlerTemplate
{
  protected static String nl;
  public static synchronized HandlerTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    HandlerTemplate result = new HandlerTemplate();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = " ";
  protected final String TEXT_2 = NL + "package ";
  protected final String TEXT_3 = ";";
  protected final String TEXT_4 = NL + NL + "import org.eclipse.e4.core.di.annotations.Execute;";
  protected final String TEXT_5 = NL + "import org.eclipse.e4.core.di.annotations.CanExecute;";
  protected final String TEXT_6 = NL + NL + "public class ";
  protected final String TEXT_7 = " {" + NL + "\t@Execute" + NL + "\tpublic void ";
  protected final String TEXT_8 = "() {" + NL + "\t\t" + NL + "\t}" + NL + "\t";
  protected final String TEXT_9 = NL + "\t" + NL + "\t@CanExecute" + NL + "\tpublic boolean ";
  protected final String TEXT_10 = "() {" + NL + "\t\t" + NL + "\t\treturn true;" + NL + "\t}" + NL + "\t";
  protected final String TEXT_11 = "\t" + NL + "}";

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
     HandlerClass domainClass = (HandlerClass)argument; 
    stringBuffer.append(TEXT_1);
     if( domainClass.getPackageFragment() != null && domainClass.getPackageFragment().getElementName().trim().length() > 0 ) { 
    stringBuffer.append(TEXT_2);
    stringBuffer.append( domainClass.getPackageFragment().getElementName() );
    stringBuffer.append(TEXT_3);
     } 
    stringBuffer.append(TEXT_4);
     if( domainClass.isUseCanExecute() )  { 
    stringBuffer.append(TEXT_5);
     } 
    stringBuffer.append(TEXT_6);
    stringBuffer.append( domainClass.getName() );
    stringBuffer.append(TEXT_7);
    stringBuffer.append( domainClass.getExecuteMethodName() );
    stringBuffer.append(TEXT_8);
     if( domainClass.isUseCanExecute() )  { 
    stringBuffer.append(TEXT_9);
    stringBuffer.append( domainClass.getCanExecuteMethodName() );
    stringBuffer.append(TEXT_10);
     } 
    stringBuffer.append(TEXT_11);
    return stringBuffer.toString();
  }
}
