/*
 * VisualUtilities.java
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (C) 2010 Michael Hoffer <info@michaelhoffer.de>
 * 
 * Supported by the Goethe Center for Scientific Computing of Prof. Wittum
 * (http://gcsc.uni-frankfurt.de)
 * 
 * This file is part of Visual Reflection Library (VRL).
 * 
 * VRL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * VRL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */
package eu.mihosoft.vrlzoom;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class VisualUtilities {

    /**
     * Returns the deepest visible descendent Component of <code>parent</code>
     * that contains the location <code>x</code>, <code>y</code>.
     * If <code>parent</code> does not contain the specified location,
     * then <code>null</code> is returned.  If <code>parent</code> is not a
     * container, or none of <code>parent</code>'s visible descendents
     * contain the specified location, <code>parent</code> is returned.
     *
     * @param parent the root component to begin the search
     * @param x the x target location
     * @param y the y target location
     * @param cond the condition
     * @return
     */
    public static Component getDeepestComponentAt(Component parent, int x, int y, ComponentCondition cond) {
        if (!parent.contains(x, y)) {
            return null;
        }
        if (parent instanceof Container) {
            Component components[] = ((Container) parent).getComponents();
            for (int i = 0; i < components.length; i++) {
                Component comp = components[i];
                if (comp != null && comp.isVisible()) {
                    Point loc = comp.getLocation();
                    if (comp instanceof Container) {
                        comp = getDeepestComponentAt(comp, x - loc.x, y - loc.y, cond);
                    } else {
                        comp = comp.getComponentAt(x - loc.x, y - loc.y);
                    }
                    if (comp != null && cond.eval(comp)) {
                        return comp;
                    }
                }
            }
        }
        return parent;
    }

    public static Component getMouseEventReceiver(Component parent, int x, int y, final MouseEvent event) {
        return getDeepestComponentAt(parent, x, y, new ComponentCondition() {

            @Override
            public boolean eval(Component c) {
                return hasMouseListeners(c, event);
            }
        });
    }

    public static boolean isMouseEvent(MouseEvent e) {
        boolean result = false;

        if (e.getID() == MouseEvent.MOUSE_ENTERED
                || e.getID() == MouseEvent.MOUSE_EXITED
                || e.getID() == MouseEvent.MOUSE_PRESSED
                || e.getID() == MouseEvent.MOUSE_RELEASED
                || e.getID() == MouseEvent.MOUSE_CLICKED) {
            result = true;
        }

        return result;
    }

    public static boolean isMouseMotionEvent(MouseEvent e) {
        boolean result = false;

        if (e.getID() == MouseEvent.MOUSE_MOVED
                || e.getID() == MouseEvent.MOUSE_DRAGGED) {
            result = true;
        }

        return result;
    }

    public static boolean isMouseWheelEvent(MouseEvent e) {
        boolean result = false;

        if (e.getID() == MouseEvent.MOUSE_WHEEL) {
            result = true;
        }

        return result;
    }

    /**
     * Determines whether a specified component has registered
     * <code>MouseListener</code>.
     * @param c the component
     * @return <code>true</code> if the specified component has mouse listeners;
     *         <code>false</code> otherwise
     */
    public static boolean hasMouseListeners(Component c, MouseEvent e) {
        boolean result = false;

        if (isMouseEvent(e)) {
            result = c.getMouseListeners().length > 0;
        } else if (isMouseMotionEvent(e)) {
            result = c.getMouseMotionListeners().length > 0;
        } else if (isMouseWheelEvent(e)) {
            result = c.getMouseWheelListeners().length > 0;
        }

//        System.out.println("Component Listeners: " + c.getClass() + " :: " + c.getMouseListeners().length);

        return result;
    }

    public static Double scaleX(AffineTransform at, Double x) {
        return at.getScaleX() * x;
    }

    public static Double scaleY(AffineTransform at, Double y) {
        return at.getScaleY() * y;
    }

    public static Double invertScaleX(AffineTransform at, Double x) {
        Double result = null;
        try {
            result = at.createInverse().getScaleX() * x;
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(VisualUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public static Double invertScaleY(AffineTransform at, Double y) {
        Double result = null;
        try {
            result = at.createInverse().getScaleY() * y;
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(VisualUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public static Point2D scale(AffineTransform at, Point2D p) {
        return new Point2D.Double(scaleX(at, p.getX()), scaleY(at, p.getY()));
    }

    public static Point point2DToPoint(Point2D p) {
        return new Point((int) p.getX(), (int) p.getY());
    }

    public static Point2D pointToPoint2D(Point p) {
        return new Point2D.Double(p.getX(), p.getY());
    }

    public static Dimension scale(AffineTransform at, Dimension d) {
        return new Dimension(scaleX(at, d.getWidth()).intValue(), scaleY(at, d.getHeight()).intValue());
    }

    public static Rectangle2D inverseTransform(AffineTransform at, Rectangle r) {
        Rectangle2D result = null;
        try {
            result = transform(at.createInverse(), r);
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(VisualUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public static Rectangle2D transform(AffineTransform at, Rectangle r) {
        Rectangle2D result = null;

        Point2D xy = new Point2D.Double(r.x, r.y);
        Point2D wh = new Point2D.Double(r.width, r.height);

        Point2D xyNew;
        Point2D whNew;

        xyNew = at.transform(xy, null);

        whNew = scale(at, wh);

        result = new Rectangle2D.Double(xyNew.getX(), xyNew.getY(),
                whNew.getX(), whNew.getY());

        return result;
    }

    public static boolean hasParent(Component c, Container parent) {

        boolean result = false;

        Container nextParent = c.getParent();

        while (nextParent != null) {
            if ( nextParent==parent) {
                result = true;
                break;
            }
            nextParent = nextParent.getParent();
        }

        return result;
    }

    public static boolean hasParentOfClass(Component c, Class<?> parent) {

        boolean result = false;

        Container nextParent = c.getParent();

        while (nextParent != null) {
            if ( nextParent.getClass().equals(parent)) {
                result = true;
                break;
            }
            nextParent = nextParent.getParent();
        }

        return result;
    }

}
