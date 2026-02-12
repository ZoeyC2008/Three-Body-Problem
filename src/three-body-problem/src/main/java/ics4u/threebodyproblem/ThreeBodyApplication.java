package ics4u.threebodyproblem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.transform.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.control.*;

import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ThreeBodyApplication extends Application {
    int scale = 100;
    int frameLength = 1250;
    int frameWidth = 750;

    double gridSize = 700;
    double spacing = 35;
    double lineThickness = 2;
    Color textColour = Color.rgb(197, 198, 208, 0.3);

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

        Button panButton = new Button("Pan");
        Button dragButton = new Button("Drag");

        panButton.setOnMouseClicked(event -> {
            if (!this.cameraMode.equals("pan")) {
                this.cameraMode = "pan";
            } else {
                this.cameraMode = "none";
            }
        });

        dragButton.setOnMouseClicked(event -> {
            if (!this.cameraMode.equals("drag")) {
                this.cameraMode = "drag";
            } else {
                this.cameraMode = "none";
            }
        });

        VBox controls = new VBox(10, panButton, dragButton);
        controls.setAlignment(Pos.TOP_LEFT);
        controls.setStyle("-fx-padding: 20;");

        hud.getChildren().add(controls);

        return hud;
    }

    private SubScene draw3D() {
        Group root = new Group();

        Color redPlane = Color.web("#fa8072");
        Color greenPlane = Color.web("#98fb98");
        Color bluePlane = Color.web("#73c2fb");

        //xy plane
        Group xyPlane = drawPlaneXY(redPlane);
        //xz plane
        Group xzPlane = drawPlaneXZ(greenPlane);
        //yz plane
        Group yzPlane = drawPlaneYZ(bluePlane);

        root.getChildren().addAll(xyPlane, xzPlane, yzPlane);

        SubScene scene = new SubScene(root, frameLength, frameWidth, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.rgb(34, 32, 33));

        return scene;
    }

    private void setDefaultCamera() {
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

        System.out.println("Translations X: " + cameraTranslationX + " | Y: " + cameraTranslationY +  " | Z: " + cameraTranslationZ);
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
