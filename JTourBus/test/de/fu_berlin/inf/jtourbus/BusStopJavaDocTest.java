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

package de.fu_berlin.inf.jtourbus;

import static org.junit.Assert.*;

import org.junit.Test;


public class BusStopJavaDocTest  {

    /*
     * Test method for
     * 'de.inffub.jtourbus.BusStopJavaDoc.processJavaDoc(String)'
     */
    @Test
    public void testProcessJavaDoc() {

        {
            String[] s = BusStopJavaElement
                    .processJavaDoc("0.0, Hallo Welt, Test: ByeBye");

            assertEquals("0.0", s[0]);
            assertEquals("Hallo Welt", s[1]);
            assertEquals("Test", s[2]);
            assertEquals("ByeBye", s[3]);
        }
        {
            String[] s = BusStopJavaElement
                    .processJavaDoc("@JTourBusStop1.0, Hallo Welt, Test: ByeBye");

            assertEquals("1.0", s[0]);
            assertEquals("Hallo Welt", s[1]);
            assertEquals("Test", s[2]);
            assertEquals("ByeBye", s[3]);
        }
        {
            String[] s = BusStopJavaElement
                    .processJavaDoc("@JTourBusStop 1.0, Hallo Welt, Test: ByeBye");

            assertEquals("1.0", s[0]);
            assertEquals("Hallo Welt", s[1]);
            assertEquals("Test", s[2]);
            assertEquals("ByeBye", s[3]);
        }
        {
            String[] s = BusStopJavaElement.processJavaDoc("");

            assertEquals("0.0", s[0]);
            assertEquals("Main Route", s[1]);
            assertEquals("", s[2]);
            assertEquals("", s[3]);
        }
        {
            String[] s = BusStopJavaElement.processJavaDoc(",,Hallo:Bye");

            assertEquals("0.0", s[0]);
            assertEquals("Main Route", s[1]);
            assertEquals("Hallo", s[2]);
            assertEquals("Bye", s[3]);
        }
        {
            String[] s = BusStopJavaElement.processJavaDoc(":Bye");

            assertEquals("0.0", s[0]);
            assertEquals("Main Route", s[1]);
            assertEquals("", s[2]);
            assertEquals("Bye", s[3]);
        }
        {
            String[] s = BusStopJavaElement.processJavaDoc("3.0:ByeBye");

            assertEquals("3.0", s[0]);
            assertEquals("Main Route", s[1]);
            assertEquals("", s[2]);
            assertEquals("ByeBye", s[3]);
        }

        {
            String[] s = BusStopJavaElement.processJavaDoc("3.0:ByeBye");

            assertEquals("3.0", s[0]);
            assertEquals("Main Route", s[1]);
            assertEquals("", s[2]);
            assertEquals("ByeBye", s[3]);
        }
        {
            String[] s = BusStopJavaElement
                    .processJavaDoc("@JTourBusStop 2.0, Handle Tour, Invoke-Start and Invoke-Stop are used to\n"
                                    + "     *               start operations:");
            assertEquals("2.0", s[0]);
            assertEquals("Handle Tour", s[1]);
            assertEquals(
                    "Invoke-Start and Invoke-Stop are used to start operations",
                    s[2]);
            assertEquals("", s[3]);

        }
        {
            String[] s = BusStopJavaElement
                    .processJavaDoc("@JTourBusStop 1, Route 3 - The selection tool selects a handle,\n"
                                    + "      *               SelectionTool.mouseDown(...) - The tool gets the mouse down\n"
                                    + "      *               event:");
            assertEquals("1", s[0]);
            assertEquals("Route 3 - The selection tool selects a handle", s[1]);
            assertEquals(
                    "SelectionTool.mouseDown(...) - The tool gets the mouse down event",
                    s[2]);
            assertEquals("", s[3]);
        }

        {
            String[] s = BusStopJavaElement
                    .processJavaDoc("@JTourBusStop 2.0, Handle Tour, Invoke-Start * Invoke-Stop are used to\n"
                                    + "     *               start operations:");
            assertEquals("2.0", s[0]);
            assertEquals("Handle Tour", s[1]);
            assertEquals(
                    "Invoke-Start * Invoke-Stop are used to start operations",
                    s[2]);
            assertEquals("", s[3]);

        }
        {
            String[] s = BusStopJavaElement
                    .processJavaDoc("@JTourBusStop 2.0, JavaDocTour, A Little bit longer, longer and longer:");
            assertEquals("2.0", s[0]);
            assertEquals("JavaDocTour", s[1]);
            assertEquals("A Little bit longer, longer and longer", s[2]);
            assertEquals("", s[3]);

        }

    }
}
