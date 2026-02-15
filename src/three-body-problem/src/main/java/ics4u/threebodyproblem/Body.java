package ics4u.threebodyproblem;

import javafx.scene.paint.Color;

public class Body {
    private double mass;
    private double radius;
    private Vector3D position;
    private Vector3D velocity;
    private Vector3D acceleration;
    private Vector3D force;
    private static String integrationMethod = "leapfrog";
    private static String[] integrationMethods = {"rk4", "leapfrog", "euler"};
    private static double timestep = 2.0e-5;
    private final double GRAVITATIONAL_CONSTANT = 6.67430e-11;
    Color colour;

    public Body(){
        this.radius = 10;
    }

    public Body(Color colour){
        this.radius = 10;
        this.colour = colour;
        this.position = new Vector3D(0, 0, 0);
        this.velocity = new Vector3D(0, 0, 0);
        this.acceleration = new Vector3D(0, 0, 0);
        this.force = new Vector3D(0, 0, 0);
    }

    public Body(double mass, Vector3D position) {
        this.mass = mass;
        this.position = new Vector3D(position);
        this.velocity = new Vector3D(0, 0, 0);
        this.acceleration = new Vector3D(0, 0, 0);
        this.radius = 10;

    }

    public Body(double mass, Vector3D position, Vector3D velocity, Vector3D acceleration) {
        this.mass = mass;
        this.position = new Vector3D(position);
        this.velocity = new Vector3D(velocity);
        this.acceleration = new Vector3D(0, 0, 0);
        this.radius = 10;

    }

    public void integrate(Body[] bodies) {
        switch (this.integrationMethod) {
            case "rk4":
                //tragically need to work on this later
            case "leapfrog":
                leapfrogIntegration(bodies);
            case "euler":
            default:
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
        integrationMethod = integrationMethod.toLowerCase();
        for (int i = 0; i < Body.integrationMethods.length; i++) {
            if (Body.integrationMethods[i].equals(integrationMethod)) {
                Body.integrationMethod = integrationMethods[i];
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

    public Color getColour() {
        return colour;
    }
}
