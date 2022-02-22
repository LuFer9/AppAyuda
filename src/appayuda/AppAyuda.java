/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appayuda;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import netscape.javascript.JSObject;

/**
 *
 * @author Luis
 */
public class AppAyuda extends Application {
    private Scene scene;
    @Override
    public void start(Stage stage) {
        
        //Creamos la escena
        stage.setTitle("Web View");
        scene = new Scene(new Browser(),750, 500, Color.web("#666970"));
        stage.setScene(scene);
        //Mostramos
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

}

    class Browser extends Region 
    {
        private HBox toolBar;
        final private String[] imageFiles = new String[]{
        AppAyuda.class.getResource("Imagenes/persona.png").toString(),
        AppAyuda.class.getResource("Imagenes/persona.png").toString(),
        AppAyuda.class.getResource("Imagenes/persona.png").toString(),
        AppAyuda.class.getResource("Imagenes/persona.png").toString()
        };
        final private String[] captions = new String[]{
        "Moodle",
        "Ayuda base de datos",
        "Ayuda Pagina principal",
        "Help"
        };
        final private String[] urls = new String[]{
        "https://moodle.org/?lang=es",
        AppAyuda.class.getResource("fuentes/TOPICS.html").toString(),
        AppAyuda.class.getResource("fuentes/TOPICSVENTANAPRNCIPAL.html").toString(),
        AppAyuda.class.getResource("fuentes/help.html").toExternalForm()};
        
        final ImageView selectedImage = new ImageView();
        final Hyperlink[] hpls = new Hyperlink[captions.length];
        final Image[] images = new Image[imageFiles.length];
       
        
        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();
        
        final Button toggleHelpTopics = new Button("Toggle Help Topics");
        final WebView smallView = new WebView();
        private boolean needDocumentationButton = false;
        
       
        public Browser() 
        {
            getStyleClass().add("browser");
            //Para Tratar los tres enlaces
            for(int i=0; i<captions.length; i++)
            {
                Hyperlink hpl = hpls[i] = new Hyperlink(captions[i]);
                
                String ruta = (imageFiles[i]);
                Image image = images[i] = new Image(ruta);
                
                hpl.setGraphic(new ImageView (image));
                final String url = urls[i];
                final boolean addButton = (hpl.getText().equals("Ayuda"));
                
                //Proces event
                hpl.setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent e) {
                        needDocumentationButton = addButton;
                        webEngine.load(url);
                    }
                });
            }

            //Creamos el toolbar donde nos apareceran los enlaces a a las paginas que hemos determinados en los arrays 
            toolBar = new HBox();
            toolBar.setAlignment(Pos.CENTER);
            toolBar.getStyleClass().add("broswer-toolbar");
            toolBar.getChildren().addAll(hpls);
            toolBar.getChildren().add(createSpacer());
            
            //set action del boton toogleHelpTopics
            toggleHelpTopics.setOnAction(new EventHandler(){
                @Override
                public void handle(Event t) {
                    webEngine.executeScript("toggle_visibility('help_topics')");
                }
            });
            
            smallView.setPrefSize(120,80);
            // Manejo de ventanas emergentes 
            webEngine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>(){
                @Override
                public WebEngine call(PopupFeatures config) {
                    smallView.setFontScale(0.8);
                    if(!toolBar.getChildren().contains(smallView))
                    {
                        toolBar.getChildren().add(smallView);
                    }
                    return smallView.getEngine();
                }                
            });
            
            //Proceso carga de las ventanas
            webEngine.getLoadWorker().stateProperty().addListener(
            new ChangeListener<State>(){
                @Override
                public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                    toolBar.getChildren().remove(toggleHelpTopics);
                    if(newState == State.SUCCEEDED){
                        JSObject win = (JSObject) webEngine.executeScript("window");
                        win.setMember("AppAyuda", new JavaApp());
                        if(needDocumentationButton){
                            toolBar.getChildren().add(toggleHelpTopics);
                        }
                    }  
                }
            });
            
            //Cargamos la pagina principal que se nos mostrara al inciar la aplicacion
            webEngine.load("http://www.ieslosmontecillos.es/");
            
            // Añadimos la vista de la web a la escena
            getChildren().add(toolBar);
            getChildren().add(browser);
            
           // habrá que definir la combo como propiedad de la clase Brower
            final ComboBox comboBox = new ComboBox();
            //En el constructor de la clase Browser damos formato al combobox y lo
            //incluimos en la toolbar
            comboBox.setPrefWidth(60);
            toolBar.getChildren().add(comboBox);
            //también el constructor de la clase Browser declaramos el manejador
            //del histórico
            final WebHistory history = webEngine.getHistory();
            history.getEntries().addListener(new ListChangeListener<WebHistory.Entry>(){
                @Override
                public void onChanged(Change<? extends Entry> c) {
                    c.next();
                    for (Entry e : c.getRemoved()) {
                        comboBox.getItems().remove(e.getUrl());
                    }
                    for (Entry e : c.getAddedSubList()) {
                        comboBox.getItems().add(e.getUrl());
                    }
                }
            });
            //Se define el comportamiento del combobox
            comboBox.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent ev) {
                    int offset =comboBox.getSelectionModel().getSelectedIndex()- history.getCurrentIndex();
                    history.go(offset);
                }
            });
            
        }
        
        //objeto javascript 
        public class JavaApp{
            public void exit(){
                Platform.exit();
            }
        }
        
        private Node createSpacer() 
        {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return spacer;
        }
        
        @Override
        protected void layoutChildren() 
        {
            double w = getWidth();
            double h = getHeight();
            double tbHeight = toolBar.prefHeight(w);
            layoutInArea(browser,0,0,w,h-tbHeight,0, HPos.CENTER, VPos.CENTER);
            layoutInArea(toolBar,0,h-tbHeight,w,tbHeight,0,HPos.CENTER,VPos.CENTER);
        }
        
        @Override
        protected double computePrefWidth(double heigth)
        {
            return 750;
        }
        
        @Override
        protected double computePrefHeight(double width) 
        {
            return 500;
        }
        
    }
    