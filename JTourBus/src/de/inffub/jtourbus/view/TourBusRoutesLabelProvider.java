/*
 * Created on 08.02.2005
 *
 */
package de.inffub.jtourbus.view;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.inffub.jtourbus.BusStop;


/**
 * The label provider is comparably stupid. It can only display different icons
 * for a BusStop and a Tour-Folder.
 */
class TourBusRoutesLabelProvider extends LabelProvider {

    public String getText(Object obj) {
        return obj.toString();
    }

    public Image getImage(Object obj) {
        String imageKey = null;
        if (obj instanceof BusStop)
            imageKey = ISharedImages.IMG_OBJ_ELEMENT;
        if (obj instanceof String)
            imageKey = ISharedImages.IMG_OBJ_FOLDER;
        return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
    }
}