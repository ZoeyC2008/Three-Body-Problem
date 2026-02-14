package ics4u.threebodyproblem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
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
import java.util.Objects;

public class ThreeBodyApplication extends Application {
    int scale = 100;
    int frameLength = 1250;
    int frameWidth = 750;

    double gridSize = 700;
    double spacing = 35;
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

    //images! I despise you, we'll get back to it eventually

    @Override
    public void start(Stage primaryStage) throws IOException {

        perspectiveCamera.setFarClip(10000);
        setDefaultCamera();


        SubScene subScene3D = draw3D();
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

    private StackPane draw2D() {
        StackPane hud = new StackPane();

        BorderPane border = new BorderPane();
        border.setLeft(drawLeftPane());
        //border.setPickOnBounds(true);

        VBox rightControls = new VBox(10);
        rightControls.setPickOnBounds(false);
        drawRightControls(rightControls);

        rightControls.setAlignment(Pos.TOP_RIGHT);


        hud.getChildren().add(border);
        hud.getChildren().add(rightControls);

        return hud;
    }

    private Node drawLeftPane() {  // Changed return type to Node
        HBox container = new HBox();

        // Tab column (vertical buttons on the left)
        VBox tabColumn = new VBox(5);
        tabColumn.setStyle("-fx-background-color: #222021; -fx-padding: 5;");
        tabColumn.setPrefWidth(100);


        // Content panel
        VBox contentPanel = drawLeftPaneContent();

        drawLeftPanelTabs(tabColumn, contentPanel);


        container.getChildren().addAll(contentPanel, tabColumn);
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

                contentPanel.getChildren().add(title);
                break;

            case "bodies":
                displayBodiesContent();
                break;
            case "pre-sets":
                break;
            case "settings":
                break;
            case "slides":
                break;
        }
    }

    private VBox drawLeftPaneContent() {
        VBox vbox = new VBox(10);

        vbox.setMaxWidth(225);

        vbox.setMaxWidth(250);
        vbox.setPrefWidth(250);
        vbox.setStyle("-fx-background-color: #884000; -fx-padding: 10;");

        //vbox.setStyle("-fx-background-color: #6e3103; -fx-padding: 10;");

        switch (controlPanelSetting) {
            case "general":
                Label title = new Label("Three-Body Problem");
                title.setWrapText(true);
                title.setStyle("-fx-text-fill: #e0dad0;");
                title.setFont(Font.font("Book Antiqua", 36));

                vbox.getChildren().add(title);
                break;

        }

        return vbox;
    }

    private void displayBodiesContent(){

    }

    private void drawRightControls(VBox rightControls){
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
        if (this.cameraMode.equals("drag")){
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
        if (this.cameraMode.equals("pan")){
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

        rightControls.getChildren().addAll(cameraResetButton, dragButton, panButton);
    }

    private SubScene draw3D() {
        Group root = new Group();

        Color redPlane = Color.web("#fa8072");
        Color greenPlane = Color.web("#98fb98");
        Color bluePlane = Color.web("#73c2fb");

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
    }

    private void updateCameraPosition() {
        perspectiveCamera.setTranslateX(cameraTranslationX);
        perspectiveCamera.setTranslateY(cameraTranslationY);
        perspectiveCamera.setTranslateZ(cameraTranslationZ);

        System.out.println("Translations X: " + cameraTranslationX + " | Y: " + cameraTranslationY + " | Z: " + cameraTranslationZ);
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
