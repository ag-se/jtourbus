/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package de.fu_berlin.inf.jtourbus.utility;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import de.fu_berlin.inf.jtourbus.plugin.JTourBusPlugin;

public class IconManager {

    private static URL fgIconBaseURL = null;

    static {
        fgIconBaseURL = JTourBusPlugin.getDefault().getBundle().getEntry(
                "/icons/full/"); //$NON-NLS-1$
    }

    public static final String NEXT = "next.gif"; //$NON-NLS-1$

    public static final String PREVIOUS = "prev.gif"; //$NON-NLS-1$
    
    public static final String STOP = "stop.gif"; //$NON-NLS-1$

    public static void setImageDescriptors(IAction action, String type) {

        try {
            ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL(
                    "dlcl16", type)); //$NON-NLS-1$
            if (id != null)
                action.setDisabledImageDescriptor(id);
        } catch (MalformedURLException e) {
        }

        try {
            ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL(
                    "elcl16", type)); //$NON-NLS-1$
            if (id != null) {
                action.setHoverImageDescriptor(id);
                action.setImageDescriptor(id);
            }
        } catch (MalformedURLException e) {
        }

        action.setImageDescriptor(create("e", type)); //$NON-NLS-1$
    }

    private static ImageDescriptor create(String path, String name) {
        try {
            return ImageDescriptor.createFromURL(makeIconFileURL(path, name));
        } catch (MalformedURLException e) {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    private static URL makeIconFileURL(String path, String name)
            throws MalformedURLException {
        StringBuffer buffer = new StringBuffer(path);
        buffer.append('/');
        buffer.append(name);
        return new URL(fgIconBaseURL, buffer.toString());
    }
}
