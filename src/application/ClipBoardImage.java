package application;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

/** Handles data from the clipboard */
public class ClipBoardImage{
	
/** If an image is on the system clipboard, this method returns it.
 * otherwise it returns null. */
@SuppressWarnings("unchecked")
public Image getImageFromClipboard() {
    @SuppressWarnings("rawtypes")
	Clipboard systemClipboard = (Clipboard) AccessController.doPrivileged(new PrivilegedAction() {
        public Object run() {
            Clipboard tempClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         return tempClipboard;
        }
    });

    // get the contents on the clipboard in a 
    // Transferable object
    Transferable clipboardContents = systemClipboard.getContents(null);

    // check if contents are empty, if so, return null
    if (clipboardContents == null){
    	System.out.println("no clipboard contents");
        return null;
    }else{
        try {
            // Ensure content on clipboard falls under a format supported by imageFlavor Flavor
            if (clipboardContents.isDataFlavorSupported(DataFlavor.imageFlavor)){
                // convert the Transferable object to an Image object
                Image image = (Image) clipboardContents.getTransferData(DataFlavor.imageFlavor);
                return image;
            }
        } catch (UnsupportedFlavorException ufe){
        	System.out.println("NOT SUPPORTED TYPE");
            ufe.printStackTrace();
        } catch (IOException ioe){
        	System.out.println("i/o exception");

            ioe.printStackTrace();
        }
    }
    return null;
}

public Image getCBImage()
{
    Image image = getImageFromClipboard();
    if (image != null)
    {
        return image;
    } else
    {
        return null;
    }
}
}