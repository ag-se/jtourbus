package de.inffub.jtourbus;


import de.inffub.jtourbus.BusStopJavaElement;
import junit.framework.TestCase;

public class _TestBusStopJavaDoc extends TestCase {

    /*
     * Test method for
     * 'de.inffub.jtourbus.BusStopJavaDoc.processJavaDoc(String)'
     */
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
