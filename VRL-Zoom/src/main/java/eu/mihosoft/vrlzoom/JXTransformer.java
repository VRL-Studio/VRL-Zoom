/*
 * Copyright 2006 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package eu.mihosoft.vrlzoom;

import eu.mihosoft.vrl.visual.EffectPane;
import eu.mihosoft.vrl.visual.TransformingParent;
//import eu.mihosoft.vrl.visual.VisualUtilities;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Map;
import java.util.HashMap;

/**
 * Container which can transform its children, for example:<br>
 * <pre>
 * JButton button = new JButton("Hello");
 * JXTransformer t = new JXTransformer(button);
 * t.rotate(Math.PI/2);</pre>
 *
 * <strong>Note:</strong>
 * This component was designed to transform simple components like JButton,
 * JLabel etc.
 *
 * @author Alexander Potochkin
 *
 * https://swinghelper.dev.java.net/ http://weblogs.java.net/blog/alexfromsun/
 */
public class JXTransformer extends JPanel implements TransformingParent {

    private Component glassPane = new MagicGlassPane();
    private Component view;
    private Rectangle visibleRect;
    private Map<?, ?> renderingHints;
    private AffineTransform at;

    private BufferedImage renderBuffer;
    private BufferedImage renderBufferDst;
    private AffineTransformOp renderBufferScaleOp;

    public JXTransformer() {
        this(null);
    }

    public JXTransformer(JComponent view) {
        this(view, new AffineTransform());
    }

    public JXTransformer(final JComponent view, AffineTransform at) {
        super(null);
        setTransform(at);
        super.addImpl(glassPane, null, 0);
        setView(view);
        Handler handler = new Handler();
        addHierarchyBoundsListener(handler);
        addComponentListener(handler);

        view.addComponentListener(new ComponentListener() {

            public void componentResized(ComponentEvent e) {
//                if (getView() != null && getView() instanceof JComponent) {
//                    JComponent c = (JComponent) getView();
//                    c.revalidate();
//                }
                view.revalidate();
            }

            public void componentMoved(ComponentEvent e) {
//                if (getView() != null && getView() instanceof JComponent) {
//                    JComponent c = (JComponent) getView();
//                    c.revalidate();
//                }
                view.revalidate();
            }

            public void componentShown(ComponentEvent e) {
//                if (getView() != null && getView() instanceof JComponent) {
//                    JComponent c = (JComponent) getView();
//                    c.revalidate();
//                }
                view.revalidate();
            }

            public void componentHidden(ComponentEvent e) {
//                if (getView() != null && getView() instanceof JComponent) {
//                    JComponent c = (JComponent) getView();
//                    c.revalidate();
//                }
                view.revalidate();
            }
        });
    }

    public Component getView() {
        return view;
    }

    public void setView(Component view) {
        if (getView() != null) {
            super.remove(getView());
        }
        if (view != null) {
            super.addImpl(view, null, 1);
        }
        this.view = view;
        doLayout();
        revalidate();
        repaint();
    }

    public Map<?, ?> getRenderingHints() {
        if (renderingHints == null) {
            return null;
        }
        return new HashMap<Object, Object>(renderingHints);
    }

    public void setRenderingHints(Map<?, ?> renderingHints) {
        if (renderingHints == null) {
            this.renderingHints = null;
        } else {
            this.renderingHints = new HashMap<Object, Object>(renderingHints);
        }
        repaint();
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        setView(comp);
    }

    @Override
    public void remove(int index) {
        Component c = getComponent(index);
        if (c == view) {
            view = null;
            super.remove(index);
        } else if (c == glassPane) {
            throw new IllegalArgumentException("GlassPane can't be removed");
        } else {
            throw new AssertionError("Unknown component with index " + index);
        }
    }

    @Override
    public void removeAll() {
        remove(view);
    }

    //This is important
    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        if (mgr != null) {
            throw new IllegalArgumentException("Only null layout is supported");
        }
        super.setLayout(mgr);
    }

    @Override
    public void doLayout() {
        if (view != null) {
//            view.setSize(view.getPreferredSize());
//            view.setSize(new Dimension(getVisibleRect().x,getVisibleRect().y));

            Point2D originalViewSize = new Point2D.Double(
                    view.getPreferredSize().width,
                    view.getPreferredSize().height);

            Point2D zoomedViewSize = new Point2D.Double();

            at.transform(originalViewSize, zoomedViewSize);

            int width
                    = Math.max(
                            getVisibleRect().width, (int) zoomedViewSize.getX());
            int height
                    = Math.max(
                            getVisibleRect().height, (int) zoomedViewSize.getY());

            Point2D originalSize = new Point2D.Double(width, height);
            Point2D transformedSize = new Point2D.Double();

            try {
                at.inverseTransform(originalSize, transformedSize);
            } catch (NoninvertibleTransformException ex) {
                // cannot invert
            }

            view.setSize(new Dimension(
                    (int) transformedSize.getX(), (int) transformedSize.getY()));

            visibleRect = getVisibleRect();

            // this causes problems while using scrollpane
//            view.setLocation(visibleRect.x, visibleRect.y);
            view.setLocation(0, 0);
        }
        glassPane.setLocation(0, 0);
        glassPane.setSize(getWidth(), getHeight());

//        System.out.println("SIZE: " + getSize());
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        Dimension size = getTransformedSize().getSize();
        Insets insets = getInsets();
        size.width += insets.left + insets.right;
        size.height += insets.top + insets.bottom;
        return size;
    }

    private Rectangle getTransformedSize() {
        if (view != null) {
            Dimension viewSize = view.getSize();
            Rectangle viewRect = new Rectangle(viewSize);
            return at.createTransformedShape(viewRect).getBounds();
        }
        return new Rectangle(super.getPreferredSize());
    }

    @Override
    public void paint(Graphics g) {
        //repaint the whole transformer in case the view component was repainted
        Rectangle clipBounds = g.getClipBounds();
        if (clipBounds != null && !clipBounds.equals(visibleRect)) {
            repaint();
        }

        super.paint(g);
    }

    @Override
    protected void paintChildren(Graphics g) {

        if (renderBuffer == null || renderBuffer.getWidth() != view.getWidth()
                || renderBuffer.getHeight() != view.getHeight()) {

            GraphicsConfiguration gc
                    = GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDefaultConfiguration();

            renderBuffer = gc.createCompatibleImage(view.getWidth(), view.getHeight(),
                    Transparency.TRANSLUCENT);
            
            renderBufferScaleOp
                = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            
            renderBufferDst = renderBufferScaleOp.createCompatibleDestImage(renderBuffer,
                    ColorModel.getRGBdefault());
        }
        
//        Graphics2D g2 = (Graphics2D) g;

        Graphics2D g2 = renderBuffer.createGraphics();

        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, renderBuffer.getWidth(), renderBuffer.getHeight());
        g2.setComposite(originalComposite);

        super.paintChildren(g2);

        g2.dispose();
        
        g2.setTransform(at);
        
//        super.paintChildren(g2);
        
//        if (true)return;

        Graphics2D g2R = (Graphics2D) g;

        g2R.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        renderBufferDst = renderBufferScaleOp.filter(renderBuffer, renderBufferDst);

        g2R.drawImage(renderBufferDst, 0, 0, null);
    }

    @Override
    public double getScaleX() {
        return getTransform().getScaleX();
    }

    @Override
    public double getScaleY() {
        return getTransform().getScaleY();
    }

    private class MagicGlassPane extends JPanel {

        private Component mouseEnteredComponent;
        private Component mousePreviouslyEnteredComponent;
        private Component mouseDraggedComponent;
        private Component mouseCurrentComponent;

        public MagicGlassPane() {
            super(null);
            setOpaque(false);
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
            enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
            enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
            ToolTipManager.sharedInstance().registerComponent(this);
        }

        private MouseEvent transformMouseEvent(MouseEvent event) {
            if (event == null) {
                throw new IllegalArgumentException("MouseEvent is null");
            }
            MouseEvent newEvent;
            if (event instanceof MouseWheelEvent) {
                MouseWheelEvent mouseWheelEvent = (MouseWheelEvent) event;
                newEvent = new MouseWheelEvent(mouseWheelEvent.getComponent(), mouseWheelEvent.getID(),
                        mouseWheelEvent.getWhen(), mouseWheelEvent.getModifiers(),
                        mouseWheelEvent.getX(), mouseWheelEvent.getY(),
                        mouseWheelEvent.getClickCount(), mouseWheelEvent.isPopupTrigger(),
                        mouseWheelEvent.getScrollType(), mouseWheelEvent.getScrollAmount(),
                        mouseWheelEvent.getWheelRotation());
            } else {
//                System.out.println("MouseEvent 1: " + event.getModifiersEx());
                newEvent = new TransformedMouseEvent(event.getComponent(), event.getID(),
                        event.getWhen(), event.getModifiers(),
                        event.getX(), event.getY(),
                        event.getClickCount(), event.isPopupTrigger(), event.getButton());
////                newEvent = event;
//                System.out.println("MouseEvent 2: " + newEvent.getModifiersEx());
            }
            if (view != null && at.getDeterminant() != 0) {
                Rectangle viewBounds = getTransformedSize();
                Insets insets = JXTransformer.this.getInsets();
                int xgap = (getWidth() - (viewBounds.width + insets.left + insets.right)) / 2;
                int ygap = (getHeight() - (viewBounds.height + insets.top + insets.bottom)) / 2;

                double x = newEvent.getX() + viewBounds.getX() - insets.left;
                double y = newEvent.getY() + viewBounds.getY() - insets.top;
                Point2D p = new Point2D.Double(x - xgap, y - ygap);

                Point2D tp;
                try {
                    tp = at.inverseTransform(p, null);
                } catch (NoninvertibleTransformException ex) {
                    //can't happen, we check it before
                    throw new AssertionError("NoninvertibleTransformException");
                }

                //Use transformed coordinates to get the current component
                mouseCurrentComponent
                        = VisualUtilities.getMouseEventReceiver(
                                view, event.getX(), event.getY(), event);

                if (mouseCurrentComponent != null
                        && VisualUtilities.hasParentOfClass(
                                mouseCurrentComponent, EffectPane.class)) {
                    newEvent = event;

                    Component tempComponent = mouseCurrentComponent;

                    if (mouseDraggedComponent != null) {
                        tempComponent = mouseDraggedComponent;
                    }

                    newEvent.setSource(tempComponent);
                } else {
                    mouseCurrentComponent
                            = VisualUtilities.getMouseEventReceiver(
                                    view, (int) tp.getX(), (int) tp.getY(), event);

                    if (mouseCurrentComponent != null
                            && !VisualUtilities.hasParentOfClass(
                                    mouseCurrentComponent, EffectPane.class)) {

                        Component tempComponent = mouseCurrentComponent;

                        if (mouseDraggedComponent != null) {
                            tempComponent = mouseDraggedComponent;
                        }

                        Point point = SwingUtilities.convertPoint(
                                view, (int) tp.getX(), (int) tp.getY(),
                                tempComponent);
                        newEvent.setSource(tempComponent);
                        newEvent.translatePoint(
                                point.x - event.getX(), point.y - event.getY());
                    } else {
                        newEvent = null;
                    }
                }

                if (mouseCurrentComponent == null) {

                    if (view != null) {
                        mouseCurrentComponent = view;
                    } else {
                        mouseCurrentComponent = JXTransformer.this;
                    }
                }
            }

            return newEvent;
        }

        @Override
        protected void processMouseEvent(MouseEvent e) {
            MouseEvent transformedEvent = transformMouseEvent(e);

            if (transformedEvent == null) {
                return;
            }

            switch (e.getID()) {
                case MouseEvent.MOUSE_ENTERED:
//                    System.out.println("ENTERED");
                    if (mouseDraggedComponent == null
                            || mouseCurrentComponent == mouseDraggedComponent) {
                        dispatchMouseEvent(transformedEvent);

                    }
                    break;
                case MouseEvent.MOUSE_EXITED:
//                    System.out.println("EXITED");
                    if (mouseEnteredComponent != null) {
                        dispatchMouseEvent(
                                createEnterExitEvent(mouseEnteredComponent, MouseEvent.MOUSE_EXITED, e));
                        mouseEnteredComponent = null;
                    }
                    break;
                case MouseEvent.MOUSE_RELEASED:
//                    System.out.println("RELEASED");
                    if (mouseDraggedComponent != null && e.getButton() == MouseEvent.BUTTON1) {
                        transformedEvent.setSource(mouseDraggedComponent);
                        mouseDraggedComponent = null;
                    }
                    dispatchMouseEvent(transformedEvent);
                    break;
                default:
                    dispatchMouseEvent(transformedEvent);
            }
            super.processMouseEvent(e);
        }

        private void dispatchMouseEvent(MouseEvent event) {
            MouseListener[] mouseListeners
                    = event.getComponent().getMouseListeners();

            for (MouseListener listener : mouseListeners) {
                //skip all ToolTipManager's related listeners
                if (!listener.getClass().getName().startsWith("javax.swing.ToolTipManager")) {
                    switch (event.getID()) {
                        case MouseEvent.MOUSE_PRESSED:
                            listener.mousePressed(event);
                            break;
                        case MouseEvent.MOUSE_RELEASED:
                            listener.mouseReleased(event);
                            break;
                        case MouseEvent.MOUSE_CLICKED:
                            listener.mouseClicked(event);
//                            System.out.println("MouseEvent 3: " + event.getModifiersEx());
                            break;
                        case MouseEvent.MOUSE_EXITED:
                            listener.mouseExited(event);
                            break;
                        case MouseEvent.MOUSE_ENTERED:
                            listener.mouseEntered(event);
                            break;
                        default:
                            throw new AssertionError();
                    }
                }
            }
        }

        @Override
        protected void processMouseMotionEvent(MouseEvent e) {
            MouseEvent transformedEvent = transformMouseEvent(e);

            if (transformedEvent == null) {
                return;
            }

            if (mouseEnteredComponent == null && mousePreviouslyEnteredComponent == null) {
                mouseEnteredComponent = mouseCurrentComponent;
                mousePreviouslyEnteredComponent = mouseCurrentComponent;
            }

            if (mouseEnteredComponent == null) {
                mouseEnteredComponent = mouseCurrentComponent;
            }

            switch (e.getID()) {
                case MouseEvent.MOUSE_MOVED:
//                    System.out.println("MOVED");
                    if (mouseCurrentComponent != mouseEnteredComponent) {
                        dispatchMouseEvent(createEnterExitEvent(
                                mouseEnteredComponent, MouseEvent.MOUSE_EXITED, e));
                        dispatchMouseEvent(createEnterExitEvent(
                                mouseCurrentComponent, MouseEvent.MOUSE_ENTERED, e));
                    }
                    break;
                case MouseEvent.MOUSE_DRAGGED:
//                    System.out.println("DRAGGED");
                    if (mouseDraggedComponent == null) {
                        mouseDraggedComponent = mouseEnteredComponent;
                    }

                    if (mouseEnteredComponent == mouseDraggedComponent
                            && mouseCurrentComponent != mouseDraggedComponent) {
                        dispatchMouseEvent(
                                createEnterExitEvent(
                                        mouseCurrentComponent, MouseEvent.MOUSE_ENTERED, e));
//                        System.out.println("ENTEREXITEVENT : 1");
                    } else if (mouseEnteredComponent != mouseDraggedComponent
                            && mouseCurrentComponent == mouseDraggedComponent) {
                        dispatchMouseEvent(
                                createEnterExitEvent(
                                        mouseEnteredComponent, MouseEvent.MOUSE_EXITED, e));
//                        System.out.println("ENTEREXITEVENT : 2");
                        mousePreviouslyEnteredComponent = mouseEnteredComponent;
                    } else if (mouseEnteredComponent != mouseDraggedComponent
                            && mouseCurrentComponent != mouseDraggedComponent
                            && mouseEnteredComponent != mouseCurrentComponent) {

                        if (mousePreviouslyEnteredComponent != mouseCurrentComponent) {
                            dispatchMouseEvent(
                                    createEnterExitEvent(
                                            mouseCurrentComponent, MouseEvent.MOUSE_ENTERED, e));
                            System.out.println("ENTER : 4");
                            mousePreviouslyEnteredComponent = mouseEnteredComponent;
                        } else {
                            System.out.println("EXIT : 4");
                            dispatchMouseEvent(
                                    createEnterExitEvent(
                                            mouseEnteredComponent, MouseEvent.MOUSE_EXITED, e));
                        }
                    }

                    if (mouseDraggedComponent != null) {
                        transformedEvent.setSource(mouseDraggedComponent);
                    }
                    break;
            }
            mouseEnteredComponent = mouseCurrentComponent;
            //dispatch MouseMotionEvent
            MouseMotionListener[] mouseMotionListeners
                    = transformedEvent.getComponent().getMouseMotionListeners();
            for (MouseMotionListener listener : mouseMotionListeners) {
                //skip all ToolTipManager's related listeners
                if (!listener.getClass().getName().startsWith("javax.swing.ToolTipManager")) {
                    switch (transformedEvent.getID()) {
                        case MouseEvent.MOUSE_MOVED:
                            listener.mouseMoved(transformedEvent);
                            break;
                        case MouseEvent.MOUSE_DRAGGED:
                            listener.mouseDragged(transformedEvent);
                            break;
                        default:
                            throw new AssertionError();
                    }
                }
            }
            super.processMouseMotionEvent(e);
        }

        @Override
        protected void processMouseWheelEvent(MouseWheelEvent e) {
            MouseWheelEvent transformedEvent = (MouseWheelEvent) transformMouseEvent(e);
            MouseWheelListener[] mouseWheelListeners
                    = transformedEvent.getComponent().getMouseWheelListeners();
            for (MouseWheelListener listener : mouseWheelListeners) {
                listener.mouseWheelMoved(transformedEvent);
            }
            super.processMouseWheelEvent(e);
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            if (mouseEnteredComponent instanceof JComponent) {
                return ((JComponent) mouseEnteredComponent).getToolTipText();
            }
            return null;
        }

        private MouseEvent createEnterExitEvent(Component c, int eventId, MouseEvent mouseEvent) {
            return new MouseEvent(c, eventId, mouseEvent.getWhen(), 0,
                    mouseEvent.getX(), mouseEvent.getY(), 0,
                    false, MouseEvent.NOBUTTON);
        }

        @Override
        public String toString() {
            return "GlassPane";
        }
    }

    /**
     * This class helps view component to be in the visible area; this is
     * important when transformer is inside JScrollPane
     */
    private class Handler extends ComponentAdapter implements HierarchyBoundsListener {

        @Override
        public void componentMoved(ComponentEvent e) {
            update();
        }

        public void ancestorMoved(HierarchyEvent e) {
            update();
        }

        public void ancestorResized(HierarchyEvent e) {
            update();
        }

        private void update() {
            if (!getVisibleRect().equals(visibleRect)) {
                revalidate();
            }
        }
    }

    /**
     * Never returns null
     *
     * @return
     */
    public AffineTransform getTransform() {
        return new AffineTransform(at);
    }

    public void setTransform(AffineTransform at) {
        if (at == null) {
            throw new IllegalArgumentException("AffineTransform is null");
        }
        this.at = new AffineTransform(at);
        revalidate();
        repaint();
    }

    public void rotate(double theta) {
        AffineTransform transform = getTransform();
        transform.rotate(theta);
        setTransform(transform);
    }

    public void scale(double sx, double sy) {
        AffineTransform transform = getTransform();
        transform.scale(sx, sy);
        setTransform(transform);
    }

    public void shear(double sx, double sy) {
        AffineTransform transform = getTransform();
        transform.shear(sx, sy);
        setTransform(transform);
    }
}
