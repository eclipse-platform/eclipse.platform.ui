E4/CSS
======

Contents
--------

*   [1 Overview](#Overview)
*   [2 Sample](#Sample)
*   [3 SWT Mapping](#SWT-Mapping)
    *   [3.1 Widget: Control](#Widget:-Control)
    *   [3.2 Widget: Button](#Widget:-Button)
    *   [3.3 Widget: Label](#Widget:-Label)
    *   [3.4 Widget: CTabFolder](#Widget:-CTabFolder)
    *   [3.5 Widget: CTabItem](#Widget:-CTabItem)
    *   [3.6 Widget: CTabFolder with e4Renderer](#Widget:-CTabFolder-with-e4Renderer)
*   [2 Pseudo classes which can be used in CSS to style SWT widgets](#Pseudo-classes-which-can-be-used-in-CSS-to-style-SWT-widgets)

*   [4 Customize](#Customize)
*   [5 Using CSS in Eclipse 3.6](#Using-CSS-in-Eclipse-3.6)

Overview
--------

The new **CSS** declarative styling support provides developers with the flexibility of styling their user interface based on a set of properties defined within a CSS style sheet. Property values are mapped to changes to SWT widgets, such as fonts and colors. The following CSS selector formats are supported:

*   Element name: SWT widget class is used instead of HTML element type
*   CSS ID: assignable from Java
*   CSS classname: assignable from Java
*   Pseudo selector: some SWT widget states are captured in pseudo selectors (e.g. `Button:checked`)
*   widget data: the key elements are available as attributes and the values as data (e.g., a widget where `widget.setData("foo", "bar")` will be matched by `*\[foo='bar'\]`)
*   widget style bits: the style bits (normally passed through the constructor) are available through the _style_ attribute (e.g., `Button\[style~='SWT.CHECK'\]`

Note that while the support for SWT widgets is the primary focus of the current developers that are working on the CSS code, the core engine is **headless** and can be used to "style" other things such as for applying arbitrary properties to a model.

For information on how CSS will be used to create a new visual style for the e4 workbench, see [E4/CSS/Visual Design](/E4/CSS/Visual_Design "E4/CSS/Visual Design")

Sample
------

The sample code below creates a simple Label within a Shell but the Label's foreground colour is styled to a blue colour by the CSS engine.

Display display = new Display();
Shell shell = new Shell();
shell.setLayout(new GridLayout());
 
Label label = new Label(shell, SWT.LEAD);
label.setText("Hello world!");
label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
CSSEngine engine = new CSSSWTEngineImpl(display);
engine.parseStyleSheet(new StringReader("Label { color: blue }"));
engine.setErrorHandler(new CSSErrorHandler() {
  public void error(Exception e) {
    e.printStackTrace();
  }
});
// applying styles to the child nodes means that the engine
// should recurse downwards, in this example, the engine
// should style the children of the Shell
engine.applyStyles(shell, /\* applyStylesToChildNodes */ true);
 
shell.setSize(400, 300);
shell.open();
 
while (!shell.isDisposed()) {
  if (!display.readAndDispatch()) {
    display.sleep();
  }
}
display.dispose();

    Display display = new Display();
    Shell shell = new Shell();
    shell.setLayout(new GridLayout());
     
    Label label = new Label(shell, SWT.LEAD);
    label.setText("Hello world!");
    label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
     
    CSSEngine engine = new CSSSWTEngineImpl(display);
    engine.parseStyleSheet(new StringReader("Label { color: blue }"));
    engine.setErrorHandler(new CSSErrorHandler() {
      public void error(Exception e) {
        e.printStackTrace();
      }
    });
    // applying styles to the child nodes means that the engine
    // should recurse downwards, in this example, the engine
    // should style the children of the Shell
    engine.applyStyles(shell, /* applyStylesToChildNodes */ true);
     
    shell.setSize(400, 300);
    shell.open();
     
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();

SWT Mapping
-----------

CSS style sheets can be used to modify SWT widget properties. 


Many SWT property setting methods can be accessed via CSS. 
These tables show the equivalent mapping from SWT method to CSS property. 
They also show pseudo selectors which can be used to choose styling based on widget state.


### Widget: Control

| SWT Method | CSS Property Name | CSS Example |
| --- | --- | --- |
| setBackground(Color) | background-color | Button { background-color: #FF0000 } |
| (vertical gradient) | background-color | Button { background-color: #FF0000 #00FF00 100% } |
| (horizontal gradient) | background-color | Button { background-color: #FF0000 #00FF00 100% false } |
| setBackgroundImage(Image) | background-image | Button { background-image: _some url_ } |
|  | border-color | Button { border-color: #FF0000; } |
|  | border-width | Button { border-width: 3 } |
|  | border-style | Button { border-style: dotted } |
| setCursor(Cursor) | cursor | Shell { cursor:crosshair } |
| setFont(Font) | font | Label { font: italic 12 bold "Terminal"; } |
|  | font-style    font-size   font-weight    font-family | Label { font-style: italic;              font-size: 12;             font-weight: bold;              font-family: "Terminal"; } |
| setForeground(Color) | color | Button { color: #FF0000 } |

  

### Widget: Button

| SWT Method | CSS Property Name | CSS Example |
| --- | --- | --- |
| setAlignment(int) | swt-alignment | Label { swt-alignment: up; } /* if pushbutton mode */ |

  

### Widget: Label

| SWT Method | CSS Property Name | CSS Example |
| --- | --- | --- |
| setAlignment(int) | swt-alignment | Label { swt-alignment: center; } |

  

### Widget: CTabFolder

| SWT Method | CSS Property Name | CSS Example |
| --- | --- | --- |
| setBorderVisible(boolean) | border-visible | CTabFolder { border-visible: true } |
| setMaximized(boolean) | swt-maximized | CTabFolder { swt-maximized: true } |
| setMinimized(boolean) | swt-minimized | CTabFolder { swt-minimized: true } |
| setMaximizeVisible(boolean) | swt-maximize-visible | CTabFolder { swt-maximize-visible: true } |
| setMinimizeVisible(boolean) | swt-minimize-visible | CTabFolder {swt- minimize-visible: true } |
| setMRUVisible(boolean) | swt-mru-visible | CTabFolder { swt-mru-visible: true } |
| setSimple(boolean) | swt-simple | CTabFolder { swt-simple: true } |
| setSingle(boolean) | swt-single | CTabFolder { swt-single: true } |
| setUnselectedCloseVisible(boolean) | swt-unselected-close-visible | CTabFolder { swt-unselected-close-visible: true } |
| setUnselectedImageVisible(boolean) | swt-unselected-image-visible | CTabFolder { swt-unselected-image-visible: true } |
| setRenderer(CTabFolderRenderer) | swt-tab-renderer | CTabFolder { swt-tab-renderer: url('bundleclass://org.eclipse.e4.ui.workbench.renderers.swt/org.eclipse.e4.ui.workbench.renderers.swt.CTabRendering'); } |
| setSelectionBackground(Color\[\],int\[\]) | swt-selected-tabs-background | CTabFolder { swt-selected-tabs-background: #FF0000 #FFFFFF 100%; } |
| setBackground(Color\[\],int\[\]) | swt-unselected-tabs-color | CTabFolder { swt-unselected-tabs-color: #FF0000 #FFFFFF 100%; } |
| setTabHeight(int) | swt-tab-height | CTabFolder { swt-tab-height: 10px; } |

  

### Widget: CTabItem

| SWT Method | CSS Property Name | CSS Example |
| --- | --- | --- |
| setShowClose(boolean) | swt-show-close | CTabItem { swt-show-close: true } |

  

### Widget: CTabFolder with e4Renderer

Note: The following examples assume that you have first set the tab-renderer to use CTabRendering (see the tab-renderer CSS property in the CTabFolder table above)

| e4Renderer Method | CSS Property Name | CSS Example |
| --- | --- | --- |
| setOuterKeyline(Color) | swt-outer-keyline-color | CTabFolder { swt-outer-keyline-color: #B6BCCC; } |
| setCornerRadius(int) | swt-corner-radius | CTabFolder { swt-corner-radius: 20; } |
| setShadowVisible(boolean) | swt-shadow-visible | CTabFolder { swt-shadow-visible: false; } |
| setShadowColor(Color) | swt-shadow-color | CTabFolder { swt-shadow-color: #F79402; } |
| setSelectedTabFill(Color) | swt-selected-tab-fill | CTabFolder { swt-selected-tab-fill: #F79402; } |
| setTabOutline(Color) | swt-tab-outline | CTabFolder { swt-tab-outline: #F79402; } |

  

### Pseudo classes which can be used in CSS to style SWT widgets

| SWT Widgets | CSS Pseudo Selector | CSS Example |
| --- | --- | --- |
| Control | :focus † | CTabFolder:focus { background-color: #FF0000; } |
| Control | :visible † | Shell:visible { background-color: #FF0000; } |
| Control | :enabled † | Text:enabled { background-color: #FF0000; } |
| Control | :disabled † | Text:disabled { background-color: #FF0000; } |
| Shell | :active | Shell:active { background-color: #FF0000; } |
| Button | :checked | Button:checked { background-color: #FF0000; } |
| CTabFolder | :selected | CTabFolder:selected { background-color: #FF0000; } |
| CTabItem | :selected | CTabItem:selected { font-weight: bold; } |

  
† As of yet styles are only applied when SWT UI is initially loaded, if widget state is changed afterwards, changes will not take effect




Customize
---------

*   [Add Selection](/E4/CSS/Add_Selector "E4/CSS/Add Selector")
*   [Add Styleable Property](/E4/CSS/Add_Styleable_Property "E4/CSS/Add Styleable Property")




E4/CSS/Add Selector
===================


This is a "how-to" that will explain the steps needed to add a CSS Selector in E4.

  
\- Create a class in org.eclipse.e4.ui.css.swt.selectors, and name it "DynamicPseudoClassesSWTxxxxHandler" where "xxxx" is the name of the selector

  
\- Make this new class extend "AbstractDynamicPseudoClassesControlHandler"


         public class DynamicPseudoClassesSWTActiveHandler extends AbstractDynamicPseudoClassesControlHandler
    

 

  
\- Within the class, create a IDynamicPseudoClassesHandler and set it equal to an instance of DynamicPseudoClassesSWTxxxxHandler

         public static final IDynamicPseudoClassesHandler INSTANCE = new DynamicPseudoClassesSWTxxxxHandler();
    

 

  
\- Add the following two methods:

         protected void intialize(final Control control, final CSSEngine engine) {}
         protected void dispose(Control control, CSSEngine engine) {}
    


         Note: method name is intilize is not initialize
    

 

  
\- In the intialize method, add the code needed (most likely listeners to look for change of state). For an example, see org.eclipse.e4.ui.css.swt.selectors.DynamicPseudoClassesSWTActiveHandler

  
\- Make use of the setData() method on the widget (to get information about the widget in another class), as well as applying the styles to the engine. For example, in a listener's method, you can do the following:

         try {
              control.setData("Some Qualified String", Boolean.TRUE);
              engine.applyStyles(control, false, true);
         } catch (Exception ex) {
              engine.handleExceptions(ex);
         }
    
        
    

 

\- It is preferable to use a qualified string, and to keep it in org.eclipse.e4.ui.css.swt.CSSSWTConstants

  
\- In the dispose method, get rid of all listeners that were created in the above intialize method

  
\- In org.eclipse.e4.ui.css.swt.dom.SWTElement#isPseudoInstanceOf add the new selector with the use of an "if" statement
         if ("xxxx".equals(s)) {}
    

 

  
\- Within the if statement, return the appropriate boolean value based on the setData() you used in your listener, and add any other conditional statements that may be necessary

         if ("xxxx".equals(s)) {
             Widget widget = getNativeWidget();
             if (widget != null) {
                return widget.getData("Some Qualified String") == null;
             }
         }
    

 

  
\- Now, we must go in org.eclipse.e4.ui.css.swt.engine.AbstractCSSSWTEngineImpl , and register the name of the selector and the instance of the above class just created:

         super.registerDynamicPseudoClassHandler("xxxx", DynamicPseudoClassesSWTxxxxHandler.INSTANCE);
    

 

E4/CSS/Add Styleable Property
=============================


This page is a "how-to" that will explain the steps needed to add a styleable CSS property in E4.

**An easier approach than what is described here is to simply provide an extension to the** org.eclipse.e4.ui.css.core.propertyHandler extension point.


Create the Property Handler Class
---------------------------------

Create a class in org.eclipse.e4.ui.css.swt.properties.custom and name it "CSSPropertyXXXXSWTHandler", where XXXX is the name of the property

  
\- Make this new class extend "AbstractCSSPropertySWTHandler"

        public class CSSPropertyXXXXSWTHandler extends AbstractCSSPropertySWTHandler
    

 

  
\- Add the following two methods:

         public void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine) throws Exception {}
    

         public String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine) throws Exception {}
    

 

Implement Property Setting
--------------------------

Within the applyCSSProperty method, write the code needed to apply the wanted styles. For example, for the border-visible property (CTabFolder) we write the following: 

         public void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine) throws Exception {
                boolean isBorderVisible = (Boolean)engine.convert(value, Boolean.class, null);
                if (control instanceof CTabFolder) {
                    CTabFolder folder = (CTabFolder) control;
                    folder.setBorderVisible(isBorderVisible);
                }
         }
    

 

Implement Property Retrieval
----------------------------

Within the retrieveCSSProperty method, write the code needed to retrieve the value of the applied property. Again, for border-visible, we write the following:

         public String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine) throws Exception {
                if (control instanceof CTabFolder) {
                    CTabFolder folder = (CTabFolder) control;
                    return Boolean.toString( folder.getBorderVisible() );
                }
         }
    

 

Expose the Property
-------------------

Finally we must wire in the property handler to the CSS Engine. There are two approaches:

*   Configure the property using the org.eclipse.e4.ui.css.core.propertyHandler extension point.
*   Explicitly configure the CSS engine instance in the initializeCSSPropertyHandlers() method. In this case, we must first register the name of the property and the above class just created. We must also register the CSSPropertyHandler created in the class:

         super.registerCSSProperty("xxxx", CSSPropertyXXXXSWTHandler.class);
         super.registerCSSPropertyHandler(CSSPropertyXXXXSWTHandler.class, new CSSPropertyXXXXSWTHandler());


Using CSS in Eclipse 3.6
------------------------

The CSS Theme Engine can be used in Eclipse 3.6. See the example ["org.eclipse.e4.ui.examples.css.rcp"](https://github.com/vogellacompany/org.eclipse.e4.ui.examples.css.rcp.git) on GitHub.



