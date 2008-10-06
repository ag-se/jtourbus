/*******************************************************************************
 * Copyright (c) 2005-2008 Christopher Oezbek
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christopher Oezbek - initial API and implementation
 *******************************************************************************/
package de.fu_berlin.inf.jtourbus.view;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.jtourbus.BusStop;


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