package ics4u.threebodyproblem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.io.IOException;

public class ThreeBodyApplication extends Application{
    int scale = 100;
    int frameLength = 1250;
    int frameWidth = 750;

    double gridSize = 700;
    double spacing = 35;
    double lineThickness = 2;

    @Override
    public void start(Stage primaryStage) throws IOException{
        Group root = new Group();


        Point3D origin = new Point3D((double) frameLength /2, (double) frameWidth /2, 0);

        Color textColour = Color.rgb(197, 198, 208, 0.3);

        Color redPlane = Color.web("#fa8072aa");
        Color greenPlane = Color.web("#98fb98aa");
        Color bluePlane = Color.web("#73c2fbaa");

        //xy plane
        Group xyPlane = drawPlaneXY(redPlane);
        //xz plane
        Group xzPlane = drawPlaneXZ(greenPlane);
        //yz plane
        Group yzPlane = drawPlaneYZ(bluePlane);

        root.getChildren().addAll(xyPlane, xzPlane, yzPlane);

        Scene scene = new Scene(root, frameLength, frameWidth, true);
        scene.setFill(Color.rgb(34, 32, 33));

        PerspectiveCamera camera = new PerspectiveCamera(true);
        //camera.setFieldOfView(60);

        camera.setTranslateX(1200);
        camera.setTranslateY(-550);
        camera.setTranslateZ(-1700);

        camera.getTransforms().addAll(
                new Rotate(-15, Rotate.X_AXIS),
                new Rotate(-495, Rotate.Y_AXIS),
                new Rotate(-7, Rotate.Z_AXIS)    // Turn to face origin
        );



        camera.setFarClip(3000);

        scene.setCamera(camera);

        primaryStage.setTitle("Three Body Problem");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Group drawPlaneXY(Color lineColor){
        Group plane = new Group();
        PhongMaterial material = new PhongMaterial(lineColor);

        // Horizontal lines (along X axis)
        for(int i = 0; i <= gridSize; i += spacing) {
            Box hLine = new Box(gridSize, lineThickness, lineThickness);
            hLine.setMaterial(material);
            hLine.setTranslateY(i - gridSize/2);
            plane.getChildren().add(hLine);
        }

        // Vertical lines (along Y axis)
        for(int i = 0; i <= gridSize; i += spacing) {
            Box vLine = new Box(lineThickness, gridSize, lineThickness);
            vLine.setMaterial(material);
            vLine.setTranslateX(i - gridSize/2);
            plane.getChildren().add(vLine);
        }

        return plane;
    }

    private Group drawPlaneXZ(Color lineColor){
        Group plane = new Group();
        PhongMaterial material = new PhongMaterial(lineColor);

        // Lines along X axis
        for(int i = 0; i <= gridSize; i += spacing) {
            Box hLine = new Box(gridSize, lineThickness, lineThickness);
            hLine.setMaterial(material);
            hLine.setTranslateZ(i - gridSize/2);
            plane.getChildren().add(hLine);
        }

        // Lines along Z axis
        for(int i = 0; i <= gridSize; i += spacing) {
            Box vLine = new Box(lineThickness, lineThickness, gridSize);
            vLine.setMaterial(material);
            vLine.setTranslateX(i - gridSize/2);
            plane.getChildren().add(vLine);
        }

        return plane;
    }

    private Group drawPlaneYZ(Color lineColor){
        Group plane = new Group();
        PhongMaterial material = new PhongMaterial(lineColor);

        // Lines along Y axis
        for(int i = 0; i <= gridSize; i += spacing) {
            Box hLine = new Box(lineThickness, gridSize, lineThickness);
            hLine.setMaterial(material);
            hLine.setTranslateZ(i - gridSize/2);
            plane.getChildren().add(hLine);
        }

        // Lines along Z axis
        for(int i = 0; i <= gridSize; i += spacing) {
            Box vLine = new Box(lineThickness, lineThickness, gridSize);
            vLine.setMaterial(material);
            vLine.setTranslateY(i - gridSize/2);
            plane.getChildren().add(vLine);
        }

        return plane;
    }
}
