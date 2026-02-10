package ics4u.threebodyproblem;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        Application.launch(ThreeBodyApplication.class, args);
        //test();
    }


    public static void test() {
        Body bod1 = new Body(150000, new Vector3D(0, 3, 0));
        Body bod2 = new Body(150000, new Vector3D(10, 1, 0));

        Body[] bods = new Body[]{bod1, bod2};

        for (int i = 0; i < 100; i++) {
            System.out.println("Position of body 1: " + bod1.getPosition().toString() + "Position of body 2: " + bod2.getPosition().toString());
            bod1.integrate(bods);
            bod2.integrate(bods);
        }
    }
}
