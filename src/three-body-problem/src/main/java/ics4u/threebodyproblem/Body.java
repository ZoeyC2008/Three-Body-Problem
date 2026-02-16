package ics4u.threebodyproblem;

import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Body {
    private double mass; //needs to be scientific notation
    private double radius;
    private Vector3D position;
    private Vector3D velocity;
    private Vector3D acceleration;
    private Vector3D previousAcceleration; //it's for leapfrog
    private Vector3D force;
    private static String integrationMethod = "Leapfrog";
    private static final String[] INTEGRATION_METHODS = {"Runge-Kutta 4", "Leapfrog", "Euler"};
    private static double timestep = 864000.0;
    private final double GRAVITATIONAL_CONSTANT = 6.67430e-11;
    Color colour;

    public Body(){
        this.radius = 20;
        this.mass = 50;
    }

    public Body(Color colour){
        this.radius = 20;
        this.colour = colour;
        this.position = new Vector3D(0, 0, 0);
        this.velocity = new Vector3D(0, 0, 0);
        this.acceleration = new Vector3D(0, 0, 0);
        this.previousAcceleration = new Vector3D(0, 0, 0);
        this.force = new Vector3D(0, 0, 0);
        this.mass = 5.97e24;
    }

    public Body(double mass, Vector3D position) {
        this.mass = mass;
        this.position = new Vector3D(position);
        this.velocity = new Vector3D(0, 0, 0);
        this.acceleration = new Vector3D(0, 0, 0);
        this.radius = 20;

    }

    public Body(double mass, Vector3D position, Vector3D velocity, Vector3D acceleration) {
        this.mass = mass;
        this.position = new Vector3D(position);
        this.velocity = new Vector3D(velocity);
        this.acceleration = new Vector3D(0, 0, 0);
        this.radius = 20;

    }

    public Body (Body body){
        this.mass = body.getMass();
        this.radius = body.getRadius();
        this.position = new Vector3D(body.getPosition());
        this.velocity = new Vector3D(body.getVelocity());
        this.acceleration = new Vector3D(body.getAcceleration());
        this.previousAcceleration = new Vector3D(body.getPreviousAcceleration());
        this.colour = body.getColour();
        this.force = new Vector3D(body.getForce());
    }

    public static void integrate (ArrayList<Body> bodies){
        Body[] bodiesArray = new Body[bodies.size()];
        for (int i = 0; i < bodies.size(); i++) {
            bodiesArray[i] = bodies.get(i);
        }
        integrate(bodiesArray);
    }

    public static void integrate(Body[] bodies) {
        switch (Body.integrationMethod) {
            case "Runge-Kutta 4":
                //tragically need to work on this later
            case "Leapfrog":
                //leapfrogIntegration(bodies);
                for (int i = 0; i < bodies.length; i++) {
                    bodies[i].updateAcceleration(bodies);
                }

                leapfrogUpdatePosition(bodies);

                for (int i = 0; i < bodies.length; i++) {
                    bodies[i].cacheAcceleration();
                    bodies[i].updateAcceleration(bodies);
                }

                leapfrogUpdateVelocity(bodies);
                break;
            case "Euler":
            default:
        }
    }

    //integration methods that i'm using in the static one
    private static void leapfrogUpdatePosition(Body[] bodies) {
        for (int i = 0; i < bodies.length; i++) {
            double newXPosition = (bodies[i].getPosition().getXValue()) + (bodies[i].getVelocity().getXValue() * Body.getTimeStep()) + (bodies[i].getAcceleration().getXValue() * Body.getTimeStep() * Body.getTimeStep() / 2);

            double newYPosition = (bodies[i].getPosition().getYValue()) + (bodies[i].getVelocity().getYValue() * Body.getTimeStep()) + (bodies[i].getAcceleration().getYValue() * Body.getTimeStep()  * Body.getTimeStep()/ 2);

            double newZPosition = (bodies[i].getPosition().getZValue()) + (bodies[i].getVelocity().getZValue() * Body.getTimeStep()) + (bodies[i].getAcceleration().getZValue() * Body.getTimeStep() * Body.getTimeStep() / 2);

            Vector3D newPosition = new Vector3D(newXPosition, newYPosition, newZPosition);

            bodies[i].setPosition(newPosition);
        }
    }

    private static void leapfrogUpdateVelocity(Body[] bodies) {
        for (int i = 0; i < bodies.length; i++) {
            double newXVelocity = bodies[i].getVelocity().getXValue() + (bodies[i].getAcceleration().getXValue() + bodies[i].getPreviousAcceleration().getXValue()) * Body.getTimeStep() / 2;

            double newYVelocity = bodies[i].getVelocity().getYValue() + (bodies[i].getAcceleration().getYValue() + bodies[i].getPreviousAcceleration().getYValue()) * Body.getTimeStep() / 2;

            double newZVelocity = bodies[i].getVelocity().getZValue() + (bodies[i].getAcceleration().getZValue() + bodies[i].getPreviousAcceleration().getZValue()) * Body.getTimeStep() / 2;

            Vector3D newVelocity = new Vector3D(newXVelocity, newYVelocity, newZVelocity);

            bodies[i].setVelocity(newVelocity);
        }
    }
    //returns acceleration
    private Vector3D lawOfGravitation(Body other) {
        Vector3D vector = other.getPosition().subtract(this.getPosition());

        double distance = vector.getMagnitude();
        double scale = this.getMass() * other.getMass() * GRAVITATIONAL_CONSTANT / Math.pow(distance, 3);

        vector = vector.scale(scale);

        double inverseMass = 1 / this.getMass();

        vector = vector.scale(inverseMass);

        return vector;
    }

    private void updateAcceleration(Body[] bodies) {
        Vector3D newAcceleration = new Vector3D();
        for (int i = 0; i < bodies.length; i++) {
            if (this.equals(bodies[i])) {
                continue;
            }

            newAcceleration = newAcceleration.add(lawOfGravitation(bodies[i]));
        }

        this.setAcceleration(newAcceleration);
    }

    //integration methods, but it's more like a blueprint (there will be liberal copy-pasting, but a blueprint nonetheless)
    private void rk4Integration() {
    }

    private void leapfrogIntegration(Body[] bodies) {
        //find acceleration at current position
        this.updateAcceleration(bodies);

        //using acceleration and current velocity, find position
        double newXPosition = (this.getPosition().getXValue()) + (this.getVelocity().getXValue() * this.getTimeStep()) + (this.getAcceleration().getXValue() * this.getTimeStep() * this.getTimeStep() / 2);
        double newYPosition = (this.getPosition().getYValue()) + (this.getVelocity().getYValue() * this.getTimeStep()) + (this.getAcceleration().getYValue() * this.getTimeStep()  * this.getTimeStep()/ 2);
        double newZPosition = (this.getPosition().getZValue()) + (this.getVelocity().getZValue() * this.getTimeStep()) + (this.getAcceleration().getZValue() * this.getTimeStep() * this.getTimeStep() / 2);
        Vector3D newPosition = new Vector3D(newXPosition, newYPosition, newZPosition);
        this.setPosition(newPosition);

        //calculate acceleration at new position
        Vector3D oldAcceleration = new Vector3D(this.getAcceleration());
        this.updateAcceleration(bodies);

        //update velocity
        double newXVelocity = this.getVelocity().getXValue() + (this.getAcceleration().getXValue() + oldAcceleration.getXValue()) * this.getTimeStep() / 2;
        double newYVelocity = this.getVelocity().getYValue() + (this.getAcceleration().getYValue() + oldAcceleration.getYValue()) * this.getTimeStep() / 2;
        double newZVelocity = this.getVelocity().getZValue() + (this.getAcceleration().getZValue() + oldAcceleration.getZValue()) * this.getTimeStep() / 2;
        Vector3D newVelocity = new Vector3D(newXVelocity, newYVelocity, newZVelocity);
        this.setVelocity(newVelocity);
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }

    public void setAcceleration(Vector3D acceleration) {
        this.acceleration = acceleration;
    }

    public static void setTimestep(double timestep) {
        Body.timestep = timestep;
    }

    public static void setIntegrationMethod(String integrationMethod) {
        for (int i = 0; i < Body.INTEGRATION_METHODS.length; i++) {
            if (Body.INTEGRATION_METHODS[i].equals(integrationMethod)) {
                Body.integrationMethod = INTEGRATION_METHODS[i];
            }
        }
    }

    public void setColour(Color color) {
        this.colour = color;
    }

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }

    public Vector3D getPosition() {
        return position;
    }

    public Vector3D getVelocity() {
        return velocity;
    }

    public Vector3D getAcceleration() {
        return acceleration;
    }

    public Vector3D getForce() {
        return force;
    }

    public static double getTimeStep() {
        return timestep;
    }

    public static String getIntegrationMethod(){
        return Body.integrationMethod.toLowerCase();
    }

    public static String[] getIntegrationMethods(){
        return Body.INTEGRATION_METHODS;
    }

    public Color getColour() {
        return colour;
    }

    private void cacheAcceleration() {
        this.previousAcceleration = new Vector3D(this.acceleration);
    }

    public Vector3D getPreviousAcceleration() {
        return previousAcceleration;
    }
}
