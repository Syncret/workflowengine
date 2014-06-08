package cit.workflow.view;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;


public class ViewUtil {
  public ViewUtil() {
  }

  public static void centerComponent(Component comp) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension compSize = comp.getSize();
    if (compSize.height > screenSize.height) {
      compSize.height = screenSize.height;
    }
    if (compSize.width > screenSize.width) {
      compSize.width = screenSize.width;
    }
    comp.setLocation( (screenSize.width - compSize.width) / 2,
                     (screenSize.height - compSize.height) / 2);
  }
  
}