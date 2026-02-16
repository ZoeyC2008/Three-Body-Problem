package ics4u.threebodyproblem;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.control.*;

import javafx.stage.Stage;

import javax.swing.text.IconView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ThreeBodyApplication extends Application {
    int scale = 100;
    int frameLength = 1250;
    int frameWidth = 750;

    double gridSize = 700;
    double spacing = 50;
    double lineThickness = 2;

    PerspectiveCamera perspectiveCamera = new PerspectiveCamera(true);
    final double CAMERA_DEFAULT_TRANSLATION_X = 1535;
    final double CAMERA_DEFAULT_TRANSLATION_Y = -685;
    final double CAMERA_DEFAULT_TRANSLATION_Z = -675;
    final double CAMERA_DEFAULT_ROTATION_X = -49.9;
    final double CAMERA_DEFAULT_ROTATION_Y = -60.4564;
    final double CAMERA_DEFAULT_ROTATION_Z = -40.7;

    double cameraTranslationX = 1535;
    double cameraTranslationY = -685;
    double cameraTranslationZ = -675;
    double cameraRotationX = 0;
    double cameraRotationY = 0;

    boolean planeXYVisible = true, planeXZVisible = true, planeYZVisible = true;

    //camera modes
    private String cameraMode = "none";
    private String[] cameraModes = {"none", "pan", "drag"};

    double mouseX;
    double mouseY;

    //control panel settings
    private String controlPanelSetting = "general";
    private String[] controlPanelSettings = {"general", "bodies", "pre-sets", "settings", "slides"};

    //it's so bodies time
    private ArrayList<Body> bodies = new ArrayList<Body>();
    private final Color[] BODY_DEFAULT_COLOURS = {Color.web("#d55e5c"), Color.web("#5c7fd6"), Color.web("#6fcf97"), Color.web("#d68a4c"), Color.web("#8b6fd6"), Color.web("d6c35c"), Color.web("c86bbe"), Color.web("4fbfd8"), Color.web("#b8734f")};
    private boolean isPlaying = false;

    private Group root3D;
    private VBox contentPanel;

    private AnimationTimer simulationTimer;
    private long lastUpdate = 0;

    @Override
    public void start(Stage primaryStage) throws IOException {

        perspectiveCamera.setFarClip(10000);
        setDefaultCamera();

        root3D = new Group();

        update3D(root3D);

        //SubScene subScene3D = draw3D();

        SubScene subScene3D = new SubScene(root3D, frameLength, frameWidth, true, SceneAntialiasing.BALANCED);
        subScene3D.setFill(Color.rgb(34, 32, 33));
        subScene3D.setCamera(perspectiveCamera);

        StackPane hud = draw2D();

        StackPane root = new StackPane(subScene3D, hud);

        root.setOnMousePressed(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - mouseX;
            double deltaY = event.getSceneY() - mouseY;

            if (cameraMode.equals("pan")) {
                cameraRotationX = 0;
                cameraRotationY = 0;

                cameraRotationX += deltaX * 0.002;
                cameraRotationY -= deltaY * 0.002;


                //I have no idea why, but adding negatives and rotate x and rotate y makes it stop tweaking out and I have no idea why, lord help me
                perspectiveCamera.getTransforms().addAll(
                        new Rotate(cameraRotationX, Rotate.Y_AXIS),
                        new Rotate(cameraRotationY, Rotate.X_AXIS)
                );


            } else if (cameraMode.equals("drag")) {
                //cameraTranslationX = 0;
                //cameraTranslationY = 0;

                cameraTranslationX -= deltaX * 0.03;
                cameraTranslationY -= deltaY * 0.03;

                updateCameraPosition();
            }
        });

        root.setOnScroll(event -> {
            double delta = event.getDeltaY();
            double zoomFactor = 1.05;

            if (delta > 0) {
                cameraTranslationX *= zoomFactor;
                cameraTranslationY *= zoomFactor;
                cameraTranslationZ *= zoomFactor;
            } else {
                cameraTranslationX /= zoomFactor;
                cameraRotationY /= zoomFactor;
                cameraTranslationZ /= zoomFactor;
            }

            updateCameraPosition();
        });

        Scene scene = new Scene(root, frameLength, frameWidth);
        primaryStage.setTitle("Three Body Problem");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupSimulationLoop() {
        simulationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double dt = (now - lastUpdate) / 1_000_000_000.0; // seconds
                lastUpdate = now;

                if (!bodies.isEmpty()) {
                    Body.integrate(bodies);
                }
                update3D(root3D);
                drawLeftPaneContent(contentPanel);
            }
        };
    }

    private void play(){
        setupSimulationLoop();
        simulationTimer.start();
    }
    private void pause(){
        simulationTimer.stop();
        lastUpdate = 0;
    }

    private StackPane draw2D() {
        StackPane hud = new StackPane();

        BorderPane border = new BorderPane();
        border.setLeft(drawLeftPane());
        //border.setPickOnBounds(true);

        VBox rightControls = new VBox(10);
        rightControls.setPickOnBounds(false);
        drawRightControls(rightControls);


        rightControls.setPrefWidth(125);
        rightControls.setMaxWidth(125);
        rightControls.setAlignment(Pos.TOP_RIGHT);

        hud.getChildren().add(border);
        hud.getChildren().add(rightControls);

        StackPane.setAlignment(rightControls, Pos.TOP_RIGHT);

        return hud;
    }

    private Node drawLeftPane() {  // Changed return type to Node
        HBox container = new HBox();

        // Tab column (vertical buttons on the left)
        VBox tabColumn = new VBox(5);
        tabColumn.setStyle("-fx-background-color: #222021; -fx-padding: 5;");
        tabColumn.setPrefWidth(100);


        // Content panel
        contentPanel = new VBox(10);
        contentPanel.setMaxWidth(250);
        contentPanel.setPrefWidth(250);
        contentPanel.setStyle("-fx-background-color: #884000; -fx-padding: 10;");
        drawLeftPaneContent(contentPanel);

        //scrollpane to contain the content pane
        ScrollPane scrollPane = new ScrollPane(contentPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxWidth(250);
        scrollPane.setPrefWidth(250);
        scrollPane.setStyle("-fx-background:#884000;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        //scrollPane.addEventFilter(ScrollEvent.SCROLL, Event::consume);
        scrollPane.setOnScroll(event -> {
            event.consume();
        });

        drawLeftPanelTabs(tabColumn, contentPanel);


        container.getChildren().addAll(scrollPane, tabColumn);
        //container.setOnScroll(Event::consume);
        return container;
    }

    private void drawLeftPanelTabs(VBox tabColumn, VBox contentPanel) {
        if (!tabColumn.getChildren().isEmpty()) {
            tabColumn.getChildren().clear();
        }

        Button[] tabs = new Button[5];
        tabs[0] = new Button("General");
        tabs[1] = new Button("Bodies");
        tabs[2] = new Button("Pre-sets");
        tabs[3] = new Button("Settings");
        tabs[4] = new Button("Slides");

        String tabStyle = "-fx-background-color: #884000; -fx-text-fill: #e0dad0; -fx-pref-width: 100; -fx-min-height: 50;-fx-font-size: 16px; -fx-font-family: 'Book Antiqua';";
        String activeTabStyle = "-fx-background-color: linear-gradient(to right, #ffcf57, #ff9c4f); -fx-text-fill: #140d07; -fx-pref-width: 100; -fx-min-height: 50; -fx-font-size: 16px; -fx-font-family:'Book Antiqua'; -fx-font-weight: bold;";

        for (int i = 0; i < tabs.length; i++) {
            Button tab = tabs[i];
            int index = i;

            tab.setStyle(tabStyle);

            //hover effect
            tab.setOnMouseEntered(e -> {
                if (!controlPanelSetting.equals(controlPanelSettings[index])) {
                    tab.setStyle(activeTabStyle);
                }
            });

            tab.setOnMouseExited(e -> {
                if (!controlPanelSetting.equals(controlPanelSettings[index])) {
                    tab.setStyle(tabStyle);
                }
            });

            tabColumn.getChildren().add(tab);
        }


        //set active tab style
        int temp = 0;
        for (int i = 0; i < controlPanelSettings.length; i++) {
            if (controlPanelSettings[i].equals(controlPanelSetting)) {
                temp = i;
            }
        }
        tabs[temp].setStyle(activeTabStyle);

        // Set up tab switching
        tabs[0].setOnAction(e -> {
            controlPanelSetting = controlPanelSettings[0];
            drawLeftPanelTabs(tabColumn, contentPanel);
            drawLeftPaneContent(contentPanel);
        });

        tabs[1].setOnAction(e -> {
            controlPanelSetting = controlPanelSettings[1];
            drawLeftPanelTabs(tabColumn, contentPanel);
            drawLeftPaneContent(contentPanel);
        });

        tabs[2].setOnAction(e -> {
            controlPanelSetting = controlPanelSettings[2];
            drawLeftPanelTabs(tabColumn, contentPanel);
            drawLeftPaneContent(contentPanel);
        });

        tabs[3].setOnAction(e -> {
            controlPanelSetting = controlPanelSettings[3];
            drawLeftPanelTabs(tabColumn, contentPanel);
            drawLeftPaneContent(contentPanel);
        });

        tabs[4].setOnAction(e -> {
            controlPanelSetting = controlPanelSettings[4];
            drawLeftPanelTabs(tabColumn, contentPanel);
            drawLeftPaneContent(contentPanel);
        });
    }

    private void drawLeftPaneContent(VBox contentPanel) {
        if (!contentPanel.getChildren().isEmpty()) {
            contentPanel.getChildren().clear();
        }

        switch (controlPanelSetting) {
            case "general":
                Label title = new Label("Three-Body Problem");
                title.setWrapText(true);
                title.setStyle("-fx-text-fill: #e0dad0;");
                title.setFont(Font.font("Book Antiqua", 36));

                HBox centeredTitle = new HBox(title);
                centeredTitle.setAlignment(Pos.CENTER);

                contentPanel.getChildren().add(centeredTitle);
                break;

            case "bodies":
                displayBodiesContent(contentPanel);
                break;
            case "pre-sets":
                break;
            case "settings":
                break;
            case "slides":
                break;
        }
    }

    private void displayBodiesContent(VBox contentPanel) {
        if (!contentPanel.getChildren().isEmpty()) {
            contentPanel.getChildren().clear();
        }

        //title
        Label title = new Label("On Bodies");
        title.setWrapText(true);
        title.setStyle("-fx-text-fill: #e0dad0;");
        title.setFont(Font.font("Book Antiqua", 36));

        HBox centeredTitle = new HBox(title);
        centeredTitle.setAlignment(Pos.CENTER);

        contentPanel.getChildren().add(centeredTitle);

        //line
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setStyle("-fx-background-color:#e0dad0;");
        contentPanel.getChildren().add(separator);

        //number of bodies in simulation
        Label bodyNumber = new Label("Number of Bodies: ");
        bodyNumber.setStyle("-fx-text-fill: #e0dad0;");
        bodyNumber.setFont(Font.font("Book Antiqua", 24));
        contentPanel.getChildren().add(bodyNumber);

        //some styling for the add and subtract buttons
        String neutralTabStyle = "-fx-background-color: #e0dad0; -fx-text-fill: #140d07; -fx-pref-width: 20; -fx-pref-height: 20;-fx-font-size: 32px; -fx-font-family: 'Book Antiqua'; -fx-background-radius: 10;";
        String inactiveTabStyle = "-fx-background-color: #807b7e; -fx-text-fill: #140d07; -fx-pref-width: 20; -fx-pref-height: 20;-fx-font-size: 32px; -fx-font-family: 'Book Antiqua'; -fx-background-radius: 10;";
        String activeTabStyle = "-fx-background-color: linear-gradient(to right, #ffcf57, #ff9c4f); -fx-text-fill: #140d07; -fx-pref-width: 20; -fx-pref-height: 20; -fx-font-size: 32px; -fx-font-family:'Book Antiqua'; -fx-font-weight: bold; -fx-background-radius: 10;";

        //adding and subtracting
        int numBodies = bodies.size();
        //System.out.println("Content panel, num bodies: " + numBodies);

        Button subtractBodies = new Button("-");

        if (numBodies == 0) {
            subtractBodies.setStyle(inactiveTabStyle);
        } else {
            subtractBodies.setStyle(neutralTabStyle);
        }

        subtractBodies.setOnMouseEntered(e -> {
            if (!(numBodies == 0)) {
                subtractBodies.setStyle(activeTabStyle);
            }
        });

        subtractBodies.setOnMouseExited(e -> {
            if (numBodies == 0) {
                subtractBodies.setStyle(inactiveTabStyle);
            } else {
                subtractBodies.setStyle(neutralTabStyle);
            }
        });

        subtractBodies.setOnAction(e -> {
            if (!bodies.isEmpty()) {
                bodies.remove(bodies.size() - 1);
                displayBodiesContent(contentPanel);
                update3D(root3D);
            }
        });

        Button addBodies = new Button("+");
        if (numBodies == 9) {
            addBodies.setStyle(inactiveTabStyle);
        } else {
            addBodies.setStyle(neutralTabStyle);
        }

        addBodies.setOnMouseEntered(e -> {
            if (!(numBodies == 9)) {
                addBodies.setStyle(activeTabStyle);
            }
        });

        addBodies.setOnMouseExited(e -> {
            if (numBodies == 9) {
                addBodies.setStyle(inactiveTabStyle);
            } else {
                addBodies.setStyle(neutralTabStyle);
            }
        });

        addBodies.setOnAction(e -> {
            if (!(numBodies == 9)) {
                bodies.add(new Body(BODY_DEFAULT_COLOURS[numBodies]));
                displayBodiesContent(contentPanel);
                update3D(root3D);
            }
        });

        Label numBodiesLabel = new Label(String.valueOf(numBodies));
        numBodiesLabel.setStyle("-fx-background-color: #e0dad0; -fx-text-fill: #140d07; -fx-font-size: 40px; -fx-font-family: 'Book Antiqua'; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 10; -fx-min-width: 60; -fx-min-height: 40; -fx-alignment: center;");


        HBox setBodyNum = new HBox(10);
        setBodyNum.getChildren().addAll(subtractBodies, numBodiesLabel, addBodies);
        setBodyNum.setAlignment(Pos.CENTER);

        contentPanel.getChildren().add(setBodyNum);

        //actually displaying the information of the bodies

        if (!bodies.isEmpty()) {
            for (int i = 0; i < bodies.size(); i++) {
                Separator bodySeparator = new Separator(Orientation.HORIZONTAL);
                bodySeparator.setStyle("-fx-background-color:#e0dad0;");
                contentPanel.getChildren().add(bodySeparator);

                VBox bodyVBox = displayBodyInfo(i);
                contentPanel.getChildren().add(bodyVBox);
            }
        }
    }

    private VBox displayBodyInfo(int bodyNum) {
        VBox vbox = new VBox(10);

        //label at the top
        HBox titleHBox = new HBox(10);

        Label bodyTitle = new Label("Body " + (bodyNum + 1) + "  ");
        bodyTitle.setStyle("-fx-text-fill: #e0dad0;");
        bodyTitle.setFont(Font.font("Book Antiqua", 24));

        Rectangle bodyColourRectangle = new Rectangle();
        bodyColourRectangle.setWidth(25);
        bodyColourRectangle.setHeight(25);
        bodyColourRectangle.setFill(bodies.get(bodyNum).getColour());

        titleHBox.getChildren().addAll(bodyTitle, bodyColourRectangle);
        titleHBox.setAlignment(Pos.CENTER_LEFT);
        vbox.getChildren().add(titleHBox);


        //alright, time for silly boxes
        String enabledInputStyle = "-fx-background-color: #e0dad0; -fx-text-fill: #140d07; -fx-font-size: 12px; -fx-font-family:'Book Antiqua'; -fx-background-radius:5; -fx-border-color:#140d07;-fx-border-width:2; -fx-border-radius: 4; -fx-padding: 2; -fx-pref-width: 40;";
        String disabledInputStyle = "-fx-background-color:#807b7e; -fx-text-fill:#140d07; -fx-font-size:12px; -fx-font-family:'Book Antiqua'; -fx-background-radius:5; -fx-border-color:#140d07; -fx-border-width: 2; -fx-border-radius: 4; -fx-padding: 2; -fx-pref-width: 40; -fx-opacity: 1.0";
        String errorInputStyle = "-fx-background-color:#ffcccc; -fx-text-fill:#cc0000; -fx-font-size: 12px; -fx-font-family:'Book Antiqua'; -fx-background-radius:5; -fx-border-color:#cc0000;-fx-border-width:2; -fx-border-radius: 4; -fx-padding: 2; -fx-pref-width: 40;";

        //such a pain, I just wanted the mass boxes to have slightly bigger text is all, whatever, it's mostly copy-paste anyway
        String massEnabledInputStyle = "-fx-background-color: #e0dad0; -fx-text-fill: #140d07; -fx-font-size: 18px; -fx-font-family:'Book Antiqua'; -fx-background-radius:5; -fx-border-color:#140d07;-fx-border-width:2; -fx-border-radius: 4; -fx-padding: 2; -fx-pref-width: 80;";
        String massDisabledInputStyle = "-fx-background-color:#807b7e; -fx-text-fill:#140d07; -fx-font-size:18px; -fx-font-family:'Book Antiqua'; -fx-background-radius:5; -fx-border-color:#140d07; -fx-border-width: 2; -fx-border-radius: 4; -fx-padding: 2; -fx-pref-width: 80; -fx-opacity: 1.0";
        String massErrorInputStyle = "-fx-background-color:#ffcccc; -fx-text-fill:#cc0000; -fx-font-size: 18px; -fx-font-family:'Book Antiqua'; -fx-background-radius:5; -fx-border-color:#cc0000;-fx-border-width:2; -fx-border-radius: 4; -fx-padding: 2; -fx-pref-width: 80;";

        //like, I straight up forgot mass was a thing, somehow
        HBox massHBox = new HBox(10);

        Label mass = new Label("Mass:");
        mass.setStyle("-fx-text-fill: #e0dad0;");
        mass.setFont(Font.font("Book Antiqua", 18));

        TextField massInput = new TextField();
        massInput.setText("" + bodies.get(bodyNum).getMass());
        if (!isPlaying) {
            massInput.setStyle(massEnabledInputStyle);
            massInput.setDisable(false);

            massInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        double value = Double.parseDouble(massInput.getText());
                        bodies.get(bodyNum).setMass(value);
                        massInput.setStyle(massEnabledInputStyle);
                        update3D(root3D);
                    } catch (NumberFormatException e) {
                        massInput.setStyle(massErrorInputStyle);
                        massInput.setText("" + bodies.get(bodyNum).getMass());
                    }
                }
            });
        } else {
            massInput.setStyle(massDisabledInputStyle);
            massInput.setDisable(true);
        }

        massHBox.getChildren().addAll(mass, massInput);
        massHBox.setAlignment(Pos.CENTER_LEFT);
        vbox.getChildren().add(massHBox);

        //positon
        Label position = new Label("Position:");
        position.setStyle("-fx-text-fill: #e0dad0;");
        position.setFont(Font.font("Book Antiqua", 18));
        vbox.getChildren().add(position);

        HBox positionHBox = new HBox(10);

        //x
        Label xPosition = new Label("X:");
        xPosition.setStyle("-fx-text-fill: #e0dad0;");
        xPosition.setFont(Font.font("Book Antiqua", 12));

        TextField xPosInput = new TextField();
        xPosInput.setText("" + bodies.get(bodyNum).getPosition().getXValue());
        if (!isPlaying) {
            xPosInput.setStyle(enabledInputStyle);
            xPosInput.setDisable(false);

            xPosInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) { // Lost focus
                    try {
                        double value = Double.parseDouble(xPosInput.getText());
                        bodies.get(bodyNum).getPosition().setXValue(value);
                        xPosInput.setStyle(enabledInputStyle);
                        update3D(root3D);
                    } catch (NumberFormatException e) {
                        xPosInput.setStyle(errorInputStyle);
                        xPosInput.setText("" + bodies.get(bodyNum).getPosition().getXValue());
                    }
                }
            });
        } else {
            xPosInput.setStyle(disabledInputStyle);
            xPosInput.setDisable(true);
        }

        //y
        Label yPosition = new Label("Y:");
        yPosition.setStyle("-fx-text-fill: #e0dad0;");
        yPosition.setFont(Font.font("Book Antiqua", 12));

        TextField yPosInput = new TextField();
        yPosInput.setText("" + bodies.get(bodyNum).getPosition().getYValue());
        if (!isPlaying) {
            yPosInput.setStyle(enabledInputStyle);
            yPosInput.setDisable(false);

            yPosInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        double value = Double.parseDouble(yPosInput.getText());
                        bodies.get(bodyNum).getPosition().setYValue(value);
                        yPosInput.setStyle(enabledInputStyle);
                        update3D(root3D);
                    } catch (NumberFormatException e) {
                        yPosInput.setStyle(errorInputStyle);
                        yPosInput.setText("" + bodies.get(bodyNum).getPosition().getYValue());
                    }
                }
            });
        } else {
            yPosInput.setStyle(disabledInputStyle);
            yPosInput.setDisable(true);
        }

        //z
        Label zPosition = new Label("Z:");
        zPosition.setStyle("-fx-text-fill: #e0dad0;");
        zPosition.setFont(Font.font("Book Antiqua", 12));

        TextField zPosInput = new TextField();
        zPosInput.setText("" + bodies.get(bodyNum).getPosition().getZValue());
        if (!isPlaying) {
            zPosInput.setStyle(enabledInputStyle);
            zPosInput.setDisable(false);

            zPosInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        double value = Double.parseDouble(zPosInput.getText());
                        bodies.get(bodyNum).getPosition().setZValue(value);
                        zPosInput.setStyle(enabledInputStyle);
                        update3D(root3D);
                    } catch (NumberFormatException e) {
                        zPosInput.setStyle(errorInputStyle);
                        zPosInput.setText("" + bodies.get(bodyNum).getPosition().getZValue());
                    }
                }
            });
        } else {
            zPosInput.setStyle(disabledInputStyle);
            zPosInput.setDisable(true);
        }

        positionHBox.getChildren().addAll(xPosition, xPosInput, yPosition, yPosInput, zPosition, zPosInput);
        positionHBox.setAlignment(Pos.CENTER_LEFT);
        vbox.getChildren().add(positionHBox);

        //velocity (i swear this is all important for when the user is allowed to set up their own thing)
        Label velocity = new Label("Velocity:");
        velocity.setStyle("-fx-text-fill: #e0dad0;");
        velocity.setFont(Font.font("Book Antiqua", 18));
        vbox.getChildren().add(velocity);

        HBox velocityHBox = new HBox(10);

        //x
        Label xVelocity = new Label("X:");
        xVelocity.setStyle("-fx-text-fill: #e0dad0;");
        xVelocity.setFont(Font.font("Book Antiqua", 12));

        TextField xVelocityInput = new TextField();
        xVelocityInput.setText("" + bodies.get(bodyNum).getVelocity().getXValue());
        if (!isPlaying) {
            xVelocityInput.setStyle(enabledInputStyle);
            xVelocityInput.setDisable(false);

            xVelocityInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        double value = Double.parseDouble(xVelocityInput.getText());
                        bodies.get(bodyNum).getVelocity().setXValue(value);
                        xVelocityInput.setStyle(enabledInputStyle);
                        update3D(root3D);
                    } catch (NumberFormatException e) {
                        xVelocityInput.setStyle(errorInputStyle);
                        xVelocityInput.setText("" + bodies.get(bodyNum).getVelocity().getXValue());
                    }
                }
            });
        } else {
            xVelocityInput.setStyle(disabledInputStyle);
            xVelocityInput.setDisable(true);
        }

        //y
        Label yVelocity = new Label("Y:");
        yVelocity.setStyle("-fx-text-fill: #e0dad0;");
        yVelocity.setFont(Font.font("Book Antiqua", 12));

        TextField yVelocityInput = new TextField();
        yVelocityInput.setText("" + bodies.get(bodyNum).getVelocity().getYValue());
        if (!isPlaying) {
            yVelocityInput.setStyle(enabledInputStyle);
            yVelocityInput.setDisable(false);

            yVelocityInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        double value = Double.parseDouble(yVelocityInput.getText());
                        bodies.get(bodyNum).getVelocity().setYValue(value);
                        yVelocityInput.setStyle(enabledInputStyle);
                        update3D(root3D);
                    } catch (NumberFormatException e) {
                        yVelocityInput.setStyle(errorInputStyle);
                        yVelocityInput.setText("" + bodies.get(bodyNum).getVelocity().getYValue());
                    }
                }
            });
        } else {
            yVelocityInput.setStyle(disabledInputStyle);
            yVelocityInput.setDisable(true);
        }

        //z
        Label zVelocity = new Label("Z:");
        zVelocity.setStyle("-fx-text-fill: #e0dad0;");
        zVelocity.setFont(Font.font("Book Antiqua", 12));

        TextField zVelocityInput = new TextField();
        zVelocityInput.setText("" + bodies.get(bodyNum).getVelocity().getZValue());
        if (!isPlaying) {
            zVelocityInput.setStyle(enabledInputStyle);
            zVelocityInput.setDisable(false);

            zVelocityInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        double value = Double.parseDouble(zVelocityInput.getText());
                        bodies.get(bodyNum).getVelocity().setZValue(value);
                        zVelocityInput.setStyle(enabledInputStyle);
                        update3D(root3D);
                    } catch (NumberFormatException e) {
                        zVelocityInput.setStyle(errorInputStyle);
                    }
                }
            });
        } else {
            zVelocityInput.setStyle(disabledInputStyle);
            zVelocityInput.setDisable(true);
        }

        velocityHBox.getChildren().addAll(xVelocity, xVelocityInput, yVelocity, yVelocityInput, zVelocity, zVelocityInput);
        velocityHBox.setAlignment(Pos.CENTER_LEFT);
        vbox.getChildren().add(velocityHBox);

        //acceleration (but the user doesn't get to set the velocity, but I still want to use the same styles
        Label acceleration = new Label("Acceleration:");
        acceleration.setStyle("-fx-text-fill: #e0dad0;");
        acceleration.setFont(Font.font("Book Antiqua", 18));
        vbox.getChildren().add(acceleration);

        HBox accelerationHBox = new HBox(10);

        //x
        Label xAcceleration = new Label("X:");
        xAcceleration.setStyle("-fx-text-fill: #e0dad0;");
        xAcceleration.setFont(Font.font("Book Antiqua", 12));

        TextField xAccelerationDisplay = new TextField();
        xAccelerationDisplay.setStyle(disabledInputStyle);
        xAccelerationDisplay.setDisable(true);

        //y
        Label yAcceleration = new Label("Y:");
        yAcceleration.setStyle("-fx-text-fill: #e0dad0;");
        yAcceleration.setFont(Font.font("Book Antiqua", 12));

        TextField yAccelerationDisplay = new TextField();
        yAccelerationDisplay.setStyle(disabledInputStyle);
        yAccelerationDisplay.setDisable(true);


        //z
        Label zAcceleration = new Label("Z:");
        zAcceleration.setStyle("-fx-text-fill: #e0dad0;");
        zAcceleration.setFont(Font.font("Book Antiqua", 12));

        TextField zAccelerationDisplay = new TextField();
        zAccelerationDisplay.setStyle(disabledInputStyle);
        zAccelerationDisplay.setDisable(true);

        accelerationHBox.getChildren().addAll(xAcceleration, xAccelerationDisplay, yAcceleration, yAccelerationDisplay, zAcceleration, zAccelerationDisplay);
        accelerationHBox.setAlignment(Pos.CENTER_LEFT);
        vbox.getChildren().add(accelerationHBox);

        return vbox;
    }

    private void drawRightControls(VBox rightControls) {
        if (!rightControls.getChildren().isEmpty()) {
            rightControls.getChildren().clear();
        }
        //all the images
        //order is alphabetical (aka the order they show up in on the right)
        Image[] images = new Image[12];
        images[0] = (new Image(getClass().getResourceAsStream("/images/camera_reset_clicked.png")));
        images[1] = (new Image(getClass().getResourceAsStream("/images/camera_reset_unclicked.png")));
        images[2] = (new Image(getClass().getResourceAsStream("/images/drag_clicked.png")));
        images[3] = (new Image(getClass().getResourceAsStream("/images/drag_unclicked.png")));
        images[4] = (new Image(getClass().getResourceAsStream("/images/pan_clicked.png")));
        images[5] = (new Image(getClass().getResourceAsStream("/images/pan_unclicked.png")));
        images[6] = (new Image(getClass().getResourceAsStream("/images/pause_clicked.png")));
        images[7] = (new Image(getClass().getResourceAsStream("/images/pause_unclicked.png")));
        images[8] = (new Image(getClass().getResourceAsStream("/images/play_clicked.png")));
        images[9] = (new Image(getClass().getResourceAsStream("/images/play_unclicked.png")));
        images[10] = (new Image(getClass().getResourceAsStream("/images/reset_clicked.png")));
        images[11] = (new Image(getClass().getResourceAsStream("/images/reset_unclicked.png")));


        //camera reset button
        Button cameraResetButton = new Button();
        //icon
        ImageView cameraResetIcon = new ImageView(images[1]);
        cameraResetIcon.setPreserveRatio(true);
        cameraResetIcon.setFitWidth(100);
        cameraResetIcon.setFitHeight(100);
        //button settings
        cameraResetButton.setStyle("-fx-background-color: transparent;");
        cameraResetButton.setGraphic(cameraResetIcon);
        //animations when hovering
        cameraResetButton.setOnMouseEntered(e -> {
            cameraResetIcon.setImage(images[0]);
        });
        cameraResetButton.setOnMouseExited(e -> {
            cameraResetIcon.setImage(images[1]);
        });
        //on click, stuff happens
        cameraResetButton.setOnAction(e -> {
            setDefaultCamera();
        });

        //drag button
        Button dragButton = new Button();
        //icon
        ImageView dragIcon = new ImageView();
        if (this.cameraMode.equals("drag")) {
            dragIcon.setImage(images[2]);
        } else {
            dragIcon.setImage(images[3]);
        }
        dragIcon.setPreserveRatio(true);
        dragIcon.setFitWidth(100);
        dragIcon.setFitHeight(100);
        //button initially
        dragButton.setStyle("-fx-background-color: transparent;");
        dragButton.setGraphic(dragIcon);
        //on hover animations
        dragButton.setOnMouseEntered(e -> {
            if (this.cameraMode.equals("drag")) {
                dragIcon.setImage(images[3]);  // stay clicked if active
            } else {
                dragIcon.setImage(images[2]);  // unclicked if inactive
            }
        });
        dragButton.setOnMouseExited(e -> {
            if (this.cameraMode.equals("drag")) {
                dragIcon.setImage(images[2]);  // stay clicked if active
            } else {
                dragIcon.setImage(images[3]);  // unclicked if inactive
            }
        });
        //onclick
        dragButton.setOnAction(e -> {
            if (!this.cameraMode.equals("drag")) {
                this.cameraMode = "drag";
            } else {
                this.cameraMode = "none";
            }
            drawRightControls(rightControls);
        });

        //pan button
        Button panButton = new Button();
        //icon
        ImageView panIcon = new ImageView();
        if (this.cameraMode.equals("pan")) {
            panIcon.setImage(images[4]);
        } else {
            panIcon.setImage(images[5]);
        }
        panIcon.setPreserveRatio(true);
        panIcon.setFitWidth(100);
        panIcon.setFitHeight(100);
        //button styling
        panButton.setStyle("-fx-background-color: transparent;");
        panButton.setGraphic(panIcon);
        panButton.setOnMouseEntered(e -> {
            if (this.cameraMode.equals("pan")) {
                panIcon.setImage(images[5]);  // stay clicked if active
            } else {
                panIcon.setImage(images[4]);  // unclicked if inactive
            }
        });
        panButton.setOnMouseExited(e -> {
            if (this.cameraMode.equals("pan")) {
                panIcon.setImage(images[4]);  // stay clicked if active
            } else {
                panIcon.setImage(images[5]);  // unclicked if inactive
            }
        });
        panButton.setOnAction(e -> {
            if (!this.cameraMode.equals("pan")) {
                this.cameraMode = "pan";
            } else {
                this.cameraMode = "none";
            }
            drawRightControls(rightControls);
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        //play & pause button
        Button playButton = new Button();
        //icon
        ImageView playIcon = new ImageView();
        if (isPlaying) {
            playIcon.setImage(images[7]);
        } else {
            playIcon.setImage(images[9]);
        }
        playIcon.setPreserveRatio(true);
        playIcon.setFitWidth(100);
        playIcon.setFitHeight(100);
        //button styling
        playButton.setStyle("-fx-background-color: transparent;");
        playButton.setGraphic(playIcon);
        playButton.setOnMouseEntered(e -> {
            if (isPlaying) {
                playIcon.setImage(images[6]);  // stay clicked if active
            } else {
                playIcon.setImage(images[8]);  // unclicked if inactive
            }
        });
        playButton.setOnMouseExited(e -> {
            if (isPlaying) {
                playIcon.setImage(images[7]);
            } else {
                playIcon.setImage(images[9]);
            }
        });
        playButton.setOnAction(e -> {
            if (isPlaying) {
                isPlaying = false;
                pause();
                drawLeftPaneContent(contentPanel);
            } else {
                isPlaying = true;
                play();
            }
            drawRightControls(rightControls);
        });

        rightControls.getChildren().addAll(cameraResetButton, dragButton, panButton, spacer,  playButton);
    }

    private void update3D(Group root) {
        if (!root.getChildren().isEmpty()) {
            root.getChildren().clear();
        }

        drawPlanes(root);

        if (!bodies.isEmpty()) {
            for (int i = 0; i < bodies.size(); i++) {
                drawSphere(root, i);
            }
        }
    }

    private void drawSphere(Group root, int bodyNum) {
        Sphere body = new Sphere (bodies.get(bodyNum).getRadius());

        body.setTranslateX(bodies.get(bodyNum).getPosition().getXValue());
        body.setTranslateY(bodies.get(bodyNum).getPosition().getYValue());
        body.setTranslateZ(bodies.get(bodyNum).getPosition().getZValue());

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(bodies.get(bodyNum).getColour());
        body.setMaterial(material);

        root.getChildren().add(body);
    }

    private void drawPlanes(Group root) {
        Color redPlane = Color.web("#8a5050");
        Color greenPlane = Color.web("#508a50");
        Color bluePlane = Color.web("#50508a");

        //xy plane
        if (planeXYVisible) {
            Group xyPlane = drawPlaneXY(redPlane);
            root.getChildren().add(xyPlane);
        }

        //xz plane
        if (planeXZVisible) {
            Group xzPlane = drawPlaneXZ(greenPlane);
            root.getChildren().add(xzPlane);
        }

        //yz plane
        if (planeYZVisible) {
            Group yzPlane = drawPlaneYZ(bluePlane);
            root.getChildren().add(yzPlane);
        }
    }

    private SubScene draw3D() {
        Group root = new Group();

        Color redPlane = Color.web("#9a5050");
        Color greenPlane = Color.web("#509a50");
        Color bluePlane = Color.web("#50509a");

        //xy plane
        if (planeXYVisible) {
            Group xyPlane = drawPlaneXY(redPlane);
            root.getChildren().add(xyPlane);
        }

        //xz plane
        if (planeXZVisible) {
            Group xzPlane = drawPlaneXZ(greenPlane);
            root.getChildren().add(xzPlane);
        }

        //yz plane
        if (planeYZVisible) {
            Group yzPlane = drawPlaneYZ(bluePlane);
            root.getChildren().add(yzPlane);
        }

        SubScene scene = new SubScene(root, frameLength, frameWidth, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.rgb(34, 32, 33));

        return scene;
    }

    private void setDefaultCamera() {
        perspectiveCamera.getTransforms().clear();

        perspectiveCamera.setTranslateX(CAMERA_DEFAULT_TRANSLATION_X);
        perspectiveCamera.setTranslateY(CAMERA_DEFAULT_TRANSLATION_Y);
        perspectiveCamera.setTranslateZ(CAMERA_DEFAULT_TRANSLATION_Z);

        perspectiveCamera.getTransforms().addAll(
                new Rotate(CAMERA_DEFAULT_ROTATION_X, Rotate.X_AXIS),
                new Rotate(CAMERA_DEFAULT_ROTATION_Y, Rotate.Y_AXIS),
                new Rotate(CAMERA_DEFAULT_ROTATION_Z, Rotate.Z_AXIS)    // Turn to face origin
        );

        cameraTranslationX = 1535;
        cameraTranslationY = -685;
        cameraTranslationZ = -675;
    }

    private void updateCameraPosition() {
        perspectiveCamera.setTranslateX(cameraTranslationX);
        perspectiveCamera.setTranslateY(cameraTranslationY);
        perspectiveCamera.setTranslateZ(cameraTranslationZ);

        //System.out.println("Translations X: " + cameraTranslationX + " | Y: " + cameraTranslationY + " | Z: " + cameraTranslationZ);
    }

    private Group drawPlaneXY(Color lineColor) {
        Group plane = new Group();
        PhongMaterial material = new PhongMaterial(lineColor);

        // Horizontal lines (along X axis)
        for (int i = 0; i <= gridSize; i += spacing) {
            Box hLine = new Box(gridSize, lineThickness, lineThickness);
            hLine.setMaterial(material);
            hLine.setTranslateY(i - gridSize / 2);
            plane.getChildren().add(hLine);
        }

        // Vertical lines (along Y axis)
        for (int i = 0; i <= gridSize; i += spacing) {
            Box vLine = new Box(lineThickness, gridSize, lineThickness);
            vLine.setMaterial(material);
            vLine.setTranslateX(i - gridSize / 2);
            plane.getChildren().add(vLine);
        }

        return plane;
    }

    private Group drawPlaneXZ(Color lineColor) {
        Group plane = new Group();
        PhongMaterial material = new PhongMaterial(lineColor);

        // Lines along X axis
        for (int i = 0; i <= gridSize; i += spacing) {
            Box hLine = new Box(gridSize, lineThickness, lineThickness);
            hLine.setMaterial(material);
            hLine.setTranslateZ(i - gridSize / 2);
            plane.getChildren().add(hLine);
        }

        // Lines along Z axis
        for (int i = 0; i <= gridSize; i += spacing) {
            Box vLine = new Box(lineThickness, lineThickness, gridSize);
            vLine.setMaterial(material);
            vLine.setTranslateX(i - gridSize / 2);
            plane.getChildren().add(vLine);
        }

        return plane;
    }

    private Group drawPlaneYZ(Color lineColor) {
        Group plane = new Group();
        PhongMaterial material = new PhongMaterial(lineColor);

        // Lines along Y axis
        for (int i = 0; i <= gridSize; i += spacing) {
            Box hLine = new Box(lineThickness, gridSize, lineThickness);
            hLine.setMaterial(material);
            hLine.setTranslateZ(i - gridSize / 2);
            plane.getChildren().add(hLine);
        }

        // Lines along Z axis
        for (int i = 0; i <= gridSize; i += spacing) {
            Box vLine = new Box(lineThickness, lineThickness, gridSize);
            vLine.setMaterial(material);
            vLine.setTranslateY(i - gridSize / 2);
            plane.getChildren().add(vLine);
        }

        return plane;
    }
}
