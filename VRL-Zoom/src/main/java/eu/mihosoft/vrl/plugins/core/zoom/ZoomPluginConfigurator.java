/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.plugins.core.zoom;

import eu.mihosoft.vrl.reflection.VisualCanvas;
import eu.mihosoft.vrl.system.InitPluginAPI;
import eu.mihosoft.vrl.system.PluginAPI;
import eu.mihosoft.vrl.system.PluginDependency;
import eu.mihosoft.vrl.system.PluginIdentifier;
import eu.mihosoft.vrl.system.VPluginAPI;
import eu.mihosoft.vrl.system.VPluginConfigurator;
import eu.mihosoft.vrl.system.VSysUtil;
import eu.mihosoft.vrl.visual.ActionDelegator;
import eu.mihosoft.vrl.visual.CanvasRepaintManager;
import eu.mihosoft.vrl.visual.VAction;
import eu.mihosoft.vrl.visual.VDialog;
import eu.mihosoft.vrl.visual.VKey;
import eu.mihosoft.vrl.visual.VSeparator;
import eu.mihosoft.vrl.visual.VShortCut;
import eu.mihosoft.vrl.visual.VShortCutAction;
import eu.mihosoft.vrl.visual.VSwingUtil;
import eu.mihosoft.vrlzoom.JXTransformer;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javax.swing.RepaintManager;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class ZoomPluginConfigurator extends VPluginConfigurator {

    private Container canvasParent;

    private ActionListener dialogActionListener;

    private double scale = 1.0;

    VShortCutAction zoomOutAction;
    VShortCutAction zoomInAction;

    ZoomType zoomType = ZoomType.SWING;

    static enum ZoomType {
        SWING,
        JAVAFX
    }

    public ZoomPluginConfigurator() {
        //specify the plugin name and version
        setIdentifier(new PluginIdentifier("Zoom-Plugin", "0.2"));

        // optionally allow other plugins to use the api of this plugin
        // you can specify packages that shall be
        // exported by using the exportPackage() method:
        //
        // exportPackage("com.your.package");
        // describe the plugin
        setDescription("Enables Canvas zooming.");

        // copyright info
        setCopyrightInfo("Zoom-Plugin",
                "(c) Michael Hoffer",
                "www.mihosoft.eu", "LGPL",
                "For author and license info see VRL-Plugin");

        // specify dependencies
        addDependency(new PluginDependency("VRL", "0.4.3.0.1", "0.4.x"));

        setAutomaticallySelected(true);
        setRelevantForPersistence(false);

    }

    @Override
    public void register(PluginAPI api) {

        // register plugin with canvas
        if (api instanceof VPluginAPI) {
            VPluginAPI vapi = (VPluginAPI) api;

            // Register visual components:
            //
            // Here you can add additional components,
            // type representations, styles etc.
            //
            // ** NOTE **
            //
            // To ensure compatibility with future versions of VRL,
            // you should only use the vapi or api object for registration.
            // If you directly use the canvas or its properties, please make
            // sure that you specify the VRL versions you are compatible with
            // in the constructor of this plugin configurator because the
            // internal api is likely to change.
            //
            // examples:
            //
            // vapi.addComponent(MyComponent.class);
            // vapi.addTypeRepresentation(MyType.class);
            
            vapi.addAction(new VSeparator(),ActionDelegator.VIEW_MENU);
            
            vapi.addAction(new VAction("Zoom 100%") {

                @Override
                public void actionPerformed(ActionEvent ae, Object o) {
                    setZoom(1.0, vapi);
                }

            }, ActionDelegator.VIEW_MENU);

            vapi.addAction(new VAction("Zoom 75%") {

                @Override
                public void actionPerformed(ActionEvent ae, Object o) {
                    setZoom(0.5, vapi);
                }

            }, ActionDelegator.VIEW_MENU);

            vapi.addAction(new VAction("Zoom 50%") {

                @Override
                public void actionPerformed(ActionEvent ae, Object o) {
                    setZoom(0.5, vapi);
                }

            }, ActionDelegator.VIEW_MENU);

            vapi.addAction(new VAction("Zoom 25%") {

                @Override
                public void actionPerformed(ActionEvent ae, Object o) {
                    setZoom(0.25, vapi);
                }

            }, ActionDelegator.VIEW_MENU);

            vapi.addAction(new VSeparator(),ActionDelegator.VIEW_MENU);

            vapi.addAction(new VAction("Zoom Out") {

                @Override
                public void actionPerformed(ActionEvent ae, Object o) {
                    decZoom(vapi);
                }

            }, ActionDelegator.VIEW_MENU);

            vapi.addAction(new VAction("Zoom In") {

                @Override
                public void actionPerformed(ActionEvent ae, Object o) {
                    incZoom(vapi);
                }

            }, ActionDelegator.VIEW_MENU);

            vapi.addAction(new VSeparator(),ActionDelegator.VIEW_MENU);

            vapi.addAction(new VAction("Use Swing Zoom") {

                @Override
                public void actionPerformed(ActionEvent ae, Object o) {
                    zoomType = ZoomType.SWING;
                    setZoom(scale, vapi);
                    System.out.println("> using Swing Zoom");
                }

            }, ActionDelegator.VIEW_MENU);

            vapi.addAction(new VAction("Use JavaFX Zoom") {

                @Override
                public void actionPerformed(ActionEvent ae, Object o) {
                    zoomType = ZoomType.JAVAFX;
                    setZoom(scale, vapi);
                    System.out.println("> using JavaFX Zoom");
                }

            }, ActionDelegator.VIEW_MENU);

            if (zoomInAction != null) {
                VSwingUtil.unregisterShortCutAction(zoomInAction);
            }

            if (zoomOutAction != null) {
                VSwingUtil.unregisterShortCutAction(zoomOutAction);
            }

//            VKey modifierKey1;
//            VKey modifierKey2;
//
//            // OS X treats ALT key differently. Therefore, we use the shift
//            // key as modifier on OS X.
//            if (VSysUtil.isMacOSX()) {
//                modifierKey1 = new VKey(KeyEvent.VK_META);
//                modifierKey2 = new VKey(KeyEvent.VK_SHIFT);
//            } else {
//                modifierKey1 = new VKey(KeyEvent.VK_CONTROL);
//                modifierKey2 = new VKey(KeyEvent.VK_ALT);
//            }

//            zoomInAction = new VShortCutAction(
//                    new VShortCut("Zoom In",
//                            modifierKey1,
//                            modifierKey2,
//                            new VKey(KeyEvent.VK_9))) {
//                        @Override
//                        public void performAction() {
//                            incZoom(vapi);
//                        }
//                    };
//            VSwingUtil.registerShortCutAction(zoomInAction);
//            zoomOutAction = new VShortCutAction(
//                    new VShortCut("Zoom Out",
//                            modifierKey1,
//                            modifierKey2,
//                            new VKey(KeyEvent.VK_0))) {
//                        @Override
//                        public void performAction() {
//                            decZoom(vapi);
//                        }
//                    };
//            VSwingUtil.registerShortCutAction(zoomOutAction);

            canvasParent = vapi.getCanvas().getParent();

            dialogActionListener = (ActionEvent e) -> {
                disableZoom(vapi);
            };

            VDialog.addDialogActionListener(dialogActionListener);

        }

    }

    private void setZoom(double zoom, VPluginAPI vApi) {
        this.scale = zoom;

        if (scale < 1.0) {
            if(zoomType == ZoomType.SWING) {
                enableZoomSwing(vApi);
            } else {
                enableZoomFX(vApi);
            }
        } else {
            scale = 1.0;
            disableZoom(vApi);
        }
    }

    private void decZoom(VPluginAPI vApi) {
        scale -= 0.2;

        if (scale < 0.25) {
            scale = 0.25;
        }

        if(zoomType == ZoomType.SWING) {
            enableZoomSwing(vApi);
        } else {
            enableZoomFX(vApi);
        }
    }

    private void incZoom(VPluginAPI vApi) {
        scale += 0.2;

        if (scale < 1.0) {
            if(zoomType == ZoomType.SWING) {
                enableZoomSwing(vApi);
            } else {
                enableZoomFX(vApi);
            }
        } else {
            scale = 1.0;
            disableZoom(vApi);
        }

    }

    private void enableZoomSwing(VPluginAPI vApi) {
        VisualCanvas vCanvas = (VisualCanvas) vApi.getCanvas();
        canvasParent.removeAll();

        JXTransformer canvasContainer = new JXTransformer(vCanvas);

        canvasContainer.scale(scale, scale);
        canvasParent.add(canvasContainer);
        vCanvas.getDock().setVisible(false);

        // RepaintManager.setCurrentManager(new RepaintManager());
    }

    @Deprecated
    private void enableZoomFX(VPluginAPI vApi) {

        System.out.println("debug:1");
        VisualCanvas vCanvas = (VisualCanvas) vApi.getCanvas();

        final JFXPanel fxPanel = new JFXPanel();

        System.out.println("debug:1:1");

        canvasParent.removeAll();
        System.out.println("debug:1:2");
        canvasParent.add(fxPanel);
        System.out.println("debug:2");

//        RepaintManager canvasRepaintMagager = RepaintManager.currentManager(vCanvas);
//        RepaintManager.setCurrentManager(null);

        Platform.runLater(() -> {

            System.out.println("debug:2:1");
            ScalableSwingNode swingNode = new ScalableSwingNode();
            swingNode.setMinSize(800, 600);

            swingNode.setContent(vCanvas);
            System.out.println("debug:2:2");

            swingNode.setMaxScaleX(0.5);
            swingNode.setMaxScaleY(0.5);

//            Stage zoomStage = new Stage();
//
//            zoomStage.setScene(new Scene(swingNode));
//            zoomStage.show();
//            
//            zoomStage.setOnCloseRequest((WindowEvent event) -> {
//                VSwingUtil.invokeLater(()->{canvasParent.add(vCanvas);});
//            });
            fxPanel.setScene(new Scene(swingNode));
            System.out.println("debug:2:3");
        });
        System.out.println("debug:3");
    }

    private void disableZoom(VPluginAPI vApi) {
        scale = 1.0;

        VisualCanvas vCanvas = (VisualCanvas) vApi.getCanvas();
        canvasParent.removeAll();
        canvasParent.add(vCanvas);

        vCanvas.getDock().setVisible(true);
        RepaintManager.setCurrentManager(new CanvasRepaintManager(vCanvas));
    }

    @Override
    public void unregister(PluginAPI api) {

        if (dialogActionListener != null) {
            VDialog.removeDialogActionListener(dialogActionListener);
        }

        if (zoomInAction != null) {
            VSwingUtil.unregisterShortCutAction(zoomInAction);
        }

        if (zoomOutAction != null) {
            VSwingUtil.unregisterShortCutAction(zoomOutAction);
        }
    }

    @Override
    public void init(InitPluginAPI iApi) {
        // nothing to init
    }
}
