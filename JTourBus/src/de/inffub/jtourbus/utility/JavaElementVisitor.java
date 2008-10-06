/*
 * Created on 02.12.2005
 * 
 */
package de.inffub.jtourbus.utility;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;

public abstract class JavaElementVisitor {

    public abstract void visit(IJavaElement element);

    public static void accept(IJavaElement root, JavaElementVisitor visitor,
            IProgressMonitor monitor) {

        SubProgressMonitor s = null;
        if (monitor != null)
            s = new SubProgressMonitor(monitor, 1);

        _internalAccept(root, visitor, s);

        if (s != null) {
            s.worked(1);
            s.done();
        }
    }

    protected static void _internalAccept(IJavaElement root,
            JavaElementVisitor visitor, IProgressMonitor monitor) {

        if (root instanceof IParent && root.exists()) {

            IParent parent = (IParent) root;

            IJavaElement[] children = {};
            try {
                children = parent.getChildren();
            } catch (JavaModelException e) {
                e.printStackTrace();
            }

            if (monitor != null)
                monitor.beginTask("", children.length + 1);
            for (IJavaElement je : children) {
                accept(je, visitor, monitor);
                if (monitor != null)
                    monitor.worked(1);
            }
        } else {
            if (monitor != null)
                monitor.beginTask("", 1);
        }

        if (!(root instanceof IMember))
            return;

        visitor.visit(root);
    }
}
