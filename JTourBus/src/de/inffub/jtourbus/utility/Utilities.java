/*
 * Created on 08.02.2005
 *
 */
package de.inffub.jtourbus.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import de.inffub.jtourbus.BusStop;
import de.inffub.jtourbus.BusStopJavaElement;
import de.inffub.jtourbus.plugin.JTourBusPlugin;

public class Utilities {

    public static IJavaProject getProject(IStructuredSelection selection) {

        // First try by using the selection
        // ISelection selection = window.getSelectionService().getSelection("org.eclipse.jdt.ui.PackageExplorer");
        if (selection != null && selection instanceof IStructuredSelection) {
            IStructuredSelection structured = (IStructuredSelection) selection;
            Object o = structured.getFirstElement();
            IJavaProject newProject = null;
            if (o != null
                && o instanceof IJavaElement
                && null != (newProject = ((IJavaElement) o)
                        .getJavaProject())) {
                return newProject;
            }
        }
        
        // Otherwise use the editor
        IWorkbenchWindow window = JTourBusPlugin.getDefault().getWorkbench()
                .getActiveWorkbenchWindow();
        if (window != null) {

            // Try to determine the project from the editor
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                
                IEditorPart part = page.getActiveEditor();
                if (part != null) {
                    IEditorInput editorInput = part.getEditorInput();
                    if (editorInput != null) {
                        IJavaElement input = (IJavaElement) editorInput
                                .getAdapter(IJavaElement.class);
                        if (input != null) {
                            return input.getJavaProject();
                        }
                    }
                }
            }
        }
        return null;
    }

    public static ICompilationUnit[] getCompilationUnits(IJavaProject project) {
        HashSet<ICompilationUnit> result = new HashSet<ICompilationUnit>();

        try {
            IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
            for (int k = 0; k < roots.length; k++) {
                if (roots[k].getKind() == IPackageFragmentRoot.K_SOURCE) {
                    IJavaElement[] children = roots[k].getChildren();
                    for (int i = 0; i < children.length; i++) {
                        result.addAll(Arrays
                                .asList(((IPackageFragment) children[i])
                                        .getCompilationUnits()));
                    }
                }
            }
        } catch (JavaModelException e) {
            JTourBusPlugin.log(e);
        }

        return result.toArray(new ICompilationUnit[result.size()]);
    }

    public static String getJavaDoc(IJavaElement je) {
        if (!(je instanceof IMember))
            return null;

        Reader reader;
        try {
            reader = JavadocContentAccess.getContentReader((IMember) je, true);
        } catch (JavaModelException ex) {
            return null;
        }

        if (reader != null) {
            BufferedReader br = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();
            do {
                String s = null;
                try {
                    s = br.readLine();
                    sb.append(s);
                } catch (IOException e) {
                }
                if (s == null)
                    break;

            } while (true);
            return sb.toString();
        }
        return null;
    }

    public static Vector<BusStop> findJTourBusStops(IProgressMonitor monitor,
            IResource resource) {
        return findJTourBusStops(monitor, new IResource[] { resource });
    }

    public static Vector<BusStop> findJTourBusStops(IProgressMonitor monitor,
            IResource[] resources) {

        final IProgressMonitor s;
        if (monitor != null)
            s = new SubProgressMonitor(monitor, 1);
        else
            s = new NullProgressMonitor();

        final Vector<BusStop> result = new Vector<BusStop>();

        TextSearchScope scope = TextSearchScope.newSearchScope(resources, Pattern.compile(".*\\.java"), false);
 
        TextSearchEngine.create().search(scope, new TextSearchRequestor() {

                    @Override
                    public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {
                    	try {
                            BusStop stop = BusStopJavaElement
                                    .BusStopJavaElementFromText(matchAccess.getFile()
                                         , matchAccess.getMatchOffset(), matchAccess.getMatchLength());
                            if (stop != null)
                                result.add(stop);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    	
                    	return true;
                    }

                }, Pattern
                        .compile("(?s)@JTourBusStop.*?(\\*/|:)"), s);

        if (s != null) {
            s.done();
        }
        return result;
    }
}
