package ics4u.threebodyproblem;

import java.util.ArrayList;

public class SimulationState {
    private int numBodies;
    private ArrayList<Body> bodies;

    public SimulationState() {
        this.numBodies = 0;
        this.bodies = new ArrayList<>();
    }


    public void setNumBodies(int numBodies) {
        this.numBodies = numBodies;
    }

    public int getNumBodies() {
        return numBodies;
    }

    public void setBodies(ArrayList<Body> bodies) {
        this.bodies.clear();

        if (!bodies.isEmpty()) {
            for (int i = 0; i < bodies.size(); i++) {
                this.bodies.add(new Body(bodies.get(i)));
            }
        }
    }

    public ArrayList<Body> getBodies() {
        return bodies;
    }


}
