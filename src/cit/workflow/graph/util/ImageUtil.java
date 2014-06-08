package cit.workflow.graph.util;

import java.awt.*;
import javax.swing.*;


/**
 * <p>Title: ImageUtil</p>
 * <p>Description:图锟疥公锟斤拷Util </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SoftAnywhere</p>
 * @author lujinyi
 * @version 1.0
 */


public class ImageUtil {

   /**
    * @since 2003-11-28
    */
   private ImageUtil() {

   }

   /**
    * 锟斤拷取Icon锟侥硷拷
    * @param name
    * @return javax.swing.ImageIcon
    */
   public static ImageIcon getIcon(String imageName) {
     //通锟斤拷classLoader4取ICon
      if (imageName == null) return(null);
      ImageIcon icon = new ImageIcon(ImageUtil.class.getClassLoader()
                                                .getResource(GraphConst.IMAGE_RESOURCE_PATH + imageName + ".gif"));
      return icon;
   }
}
