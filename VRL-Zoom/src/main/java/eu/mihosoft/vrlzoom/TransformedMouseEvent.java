/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrlzoom;

import java.awt.Component;
import java.awt.event.MouseEvent;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class TransformedMouseEvent extends MouseEvent{

    public TransformedMouseEvent(Component c, int id, long when, int modifiers, int x, int y, int count, boolean popup, int button) {
        super(c, id, when, modifiers, x, y, count, popup, button);
    }
}
