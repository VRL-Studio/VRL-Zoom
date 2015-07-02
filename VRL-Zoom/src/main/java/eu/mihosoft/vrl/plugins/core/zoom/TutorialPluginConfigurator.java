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
import eu.mihosoft.vrl.visual.ActionDelegator;
import eu.mihosoft.vrl.visual.VAction;
import eu.mihosoft.vrl.visual.VSwingUtil;
import java.awt.Container;
import java.awt.event.ActionEvent;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javax.swing.JComponent;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class TutorialPluginConfigurator extends VPluginConfigurator {

    private Container canvasParent;

    public TutorialPluginConfigurator() {
        //specify the plugin name and version
        setIdentifier(new PluginIdentifier("Zoom-Plugin01", "0.1"));

        // optionally allow other plugins to use the api of this plugin
        // you can specify packages that shall be
        // exported by using the exportPackage() method:
        //
        // exportPackage("com.your.package");
        // describe the plugin
        setDescription("Enables Canvas zooming.");

        // copyright info
        setCopyrightInfo("Sample-Plugin",
                "(c) Your Name",
                "www.you.com", "License Name", "License Text...");

        // specify dependencies
        addDependency(new PluginDependency("VRL", "0.4.3.0.1", "0.4.x"));
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
//           vapi.addComponent(TutorialComponent01.class);
            vapi.addAction(new VAction("Enable Zooming") {

                @Override
                public void actionPerformed(ActionEvent ae, Object o) {
                    new Thread(()->{enableZoom(vapi);}).start();
                }

            }, ActionDelegator.VIEW_MENU);

            vapi.addAction(new VAction("Disable Zooming") {

                @Override
                public void actionPerformed(ActionEvent ae, Object o) {
                    disableZoom(vapi);
                }

            }, ActionDelegator.VIEW_MENU);

            canvasParent = vapi.getCanvas().getParent();

        }
    }

    private void enableZoom(VPluginAPI vApi) {

        Object[] refs = {null, null};

        VSwingUtil.invokeLater(() -> {
            System.out.println("debug:1");
            VisualCanvas vCanvas = (VisualCanvas) vApi.getCanvas();
            refs[0] = vCanvas;

            final JFXPanel fxPanel = new JFXPanel();
            refs[1] = fxPanel;
            
            
            System.out.println("debug:1:1");

            canvasParent.removeAll();
            System.out.println("debug:1:2");
            canvasParent.add(fxPanel);
        });

        System.out.println("debug:2");

        Platform.runLater(() -> {
            System.out.println("debug:2:1");
            ScalableSwingNode swingNode = new ScalableSwingNode();
            swingNode.setMinSize(800, 600);

            swingNode.setContent((JComponent) refs[0]);
            System.out.println("debug:2:2");

            swingNode.setMaxScaleX(0.5);
            swingNode.setMaxScaleY(0.5);

            ((JFXPanel)refs[1]).setScene(new Scene(swingNode));
            System.out.println("debug:2:3");
        });
        System.out.println("debug:3");
    }

    private void disableZoom(VPluginAPI vApi) {
        VisualCanvas vCanvas = (VisualCanvas) vApi.getCanvas();
        canvasParent.removeAll();
        canvasParent.add(vCanvas);
    }

    @Override
    public void unregister(PluginAPI api) {
        // nothing to unregister
    }

    @Override
    public void init(InitPluginAPI iApi) {
        // nothing to init
    }
}
