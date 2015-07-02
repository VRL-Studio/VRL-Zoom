/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrlzoom;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class UiUtilities {
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
    public static Component getDeepestComponentAt(Component parent, int x, int y, ConditionEval cond) {
        if (!parent.contains(x, y)) {
            return null;
        }
        if (parent instanceof Container) {
            Component components[] = ((Container)parent).getComponents();
            for (int i = 0 ; i < components.length ; i++) {
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
        return getDeepestComponentAt(parent, x, y, new ConditionEval() {

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
}
