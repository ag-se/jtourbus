package de.fu_berlin.inf.jtourbus;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * This BusStop is used when we only know the java element but not the AST
 * 
 * @version 0.1 - 27.07.2005
 * @author Christopher Oezbek
 */

public class BusStopJavaElement extends BusStop {

    public BusStopJavaElement(ICompilationUnit cu, double stopNumber,
            String route, String description) {
        super(cu);
        fStopNumber = stopNumber;
        fRoute = route;
        fDescription = description;
        fCompilationUnit = cu;
    }
    
    /* public String toString() {
        if ("".equals(fDescription)) {
            return "Stop #"
                   + fStopNumber
                   + " @ "
                   + getCompilationUnit().getElementName();

        }
        return fDescription + " (" + fStopNumber + ")";
    } */

    public int length;

    public int offset;

    @Override
    public ISourceRange getSourceRange() {
        return new ISourceRange() {

            public int getLength() {
                return length;
            }

            public int getOffset() {
                return offset;
            }
        };
    }

    /**
     * Will return a String array with the following values filled:
     * 
     * result[0] == stop number result[1] == route name result[2] == route
     * description result[3] == everything after the colon
     */
    public static String[] processJavaDoc(String javaDoc) {

        javaDoc = javaDoc.replaceAll("(?s)\n\\s*?\\*", " ");
        javaDoc = javaDoc.replaceAll("\\s{2,}", " ");
        
        
        String[] result = { "0.0", "Main Route", "", "" };

        if (javaDoc.startsWith("@JTourBusStop")){
            javaDoc = javaDoc.substring(13);
        }        
        
        int pos = javaDoc.indexOf(":");
        if (pos != -1) {
            result[3] = javaDoc.substring(pos + 1, javaDoc.length()).trim();
            javaDoc = javaDoc.substring(0, pos).trim();
        }
        String[] elements = javaDoc.split(",", 3);

        boolean gotNumber = false;
        boolean gotRoute = false;
        boolean gotDescription = false;

        for (String s : elements) {
            
            s = s.trim();

            if (!gotNumber) {
                try {
                    gotNumber = true;
                    if ("".equals(s))
                        continue;
                    result[0] = s;

                    continue;
                } catch (Exception e) {

                }
            }

            if (!gotRoute) {
                gotRoute = true;
                if ("".equals(s))
                    continue;
                result[1] = s;
            } else {
                if (!gotDescription) {
                    result[2] = s;
                    gotDescription = true;
                } else {
                    result[2] += s;
                }
            }

        }
        
        for (int i = 0; i <= 2; i++){
            // result[i] = result[i].replaceAll("(?s)\n\\s*?\\*", " ");
            result[i] = result[i].replaceAll("\\s{2,}", " ");
        }
        return result;
    }

    public static BusStop BusStopJavaElementFromText(IResource resource,
            int start, int length) {
        ICompilationUnit cu = JavaCore
                .createCompilationUnitFrom((IFile) resource);

        String javaDoc;
        try {
            
            
            javaDoc = cu.getSource().substring(start, start + length);
        } catch (JavaModelException e1) {
            return null;
        }

        if (javaDoc.endsWith("*/")){
            javaDoc = javaDoc.substring(0, javaDoc.length() - 2);
            length = length - 2;
        }
        
        String[] s = processJavaDoc(javaDoc);

        double stopNumber = 0.0;
        try {
            stopNumber = Double.parseDouble(s[0]);
        } catch (Exception e) {
        }

        BusStopJavaElement result = new BusStopJavaElement(cu, stopNumber,
                s[1], s[2]);
        result.length = length;
        result.offset = start;

        return result;
    }

}