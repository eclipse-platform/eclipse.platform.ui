package org.eclipse.e4.internal.tools.wizards.classes.templates;

import org.eclipse.e4.internal.tools.wizards.classes.NewToolControlClassWizard.ToolControlClass;
@SuppressWarnings("nls")
public class ToolControlTemplate
{
  protected static String nl;
  public static synchronized ToolControlTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    ToolControlTemplate result = new ToolControlTemplate();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "package ";
  protected final String TEXT_2 = ";";
  protected final String TEXT_3 = NL + NL + "import javax.annotation.PostConstruct;";
  protected final String TEXT_4 = NL + "import javax.inject.Inject;";
  protected final String TEXT_5 = NL + NL + "public class ";
  protected final String TEXT_6 = " {" + NL + "\t";
  protected final String TEXT_7 = NL + "\t@Inject" + NL + "\tpublic ";
  protected final String TEXT_8 = "() {" + NL + "\t\t" + NL + "\t}" + NL + "\t";
  protected final String TEXT_9 = NL + "\t@PostConstruct" + NL + "\tpublic void ";
  protected final String TEXT_10 = "() {" + NL + "\t\t" + NL + "\t}" + NL + "}";

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
     ToolControlClass domainClass = (ToolControlClass)argument; 
     if( domainClass.getPackageFragment() != null && domainClass.getPackageFragment().getElementName().trim().length() > 0 ) { 
    stringBuffer.append(TEXT_1);
    stringBuffer.append( domainClass.getPackageFragment().getElementName() );
    stringBuffer.append(TEXT_2);
     } 
    stringBuffer.append(TEXT_3);
     if( domainClass.isCreateDefaultConstructor() )  { 
    stringBuffer.append(TEXT_4);
     } 
    stringBuffer.append(TEXT_5);
    stringBuffer.append( domainClass.getName() );
    stringBuffer.append(TEXT_6);
     if( domainClass.isCreateDefaultConstructor() )  { 
    stringBuffer.append(TEXT_7);
    stringBuffer.append( domainClass.getName() );
    stringBuffer.append(TEXT_8);
     } 
    stringBuffer.append(TEXT_9);
    stringBuffer.append( domainClass.getCreateGuiMethodName() );
    stringBuffer.append(TEXT_10);
    return stringBuffer.toString();
  }
}
