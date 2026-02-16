package ics4u.threebodyproblem;

import java.util.ArrayList;

public class SimulationState {
    private int numBodies;
    private ArrayList<Body> bodies;

    SimulationState(int numBodies, ArrayList<Body> bodies) {
        this.numBodies = numBodies;
        this.bodies = bodies;
    }

    public int getNumBodies() {
        return numBodies;
    }

    public ArrayList<Body> getBodies() {
        return bodies;
    }
}
