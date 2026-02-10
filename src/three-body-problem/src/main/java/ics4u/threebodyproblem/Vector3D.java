package ics4u.threebodyproblem;

public class Vector3D {
    private double XValue;
    private double YValue;
    private double ZValue;

    public Vector3D(){
        this.XValue = 0;
        this.YValue = 0;
        this.ZValue = 0;
    }

    public Vector3D(double XValue, double YValue, double ZValue) {
        this.XValue = XValue;
        this.YValue = YValue;
        this.ZValue = ZValue;
    }

    public Vector3D(Vector3D vector) {
        this.XValue = vector.getXValue();
        this.YValue = vector.getYValue();
        this.ZValue = vector.getZValue();
    }

    public Vector3D subtract(Vector3D vectorB) {
        double XDifference = this.getXValue() - vectorB.getXValue();
        double YDifference = this.getYValue() - vectorB.getYValue();
        double ZDifference = this.getZValue() - vectorB.getZValue();

        return new Vector3D(XDifference, YDifference, ZDifference);
    }

    public Vector3D add(Vector3D vectorB) {
        double XSum = this.getXValue() + vectorB.getXValue();
        double YSum = this.getYValue() + vectorB.getYValue();
        double ZSum = this.getZValue() + vectorB.getZValue();

        return new Vector3D(XSum, YSum, ZSum);
    }

    public static Vector3D addMany(Vector3D[] vectors) {
        double XSum = 0;
        double YSum = 0;
        double ZSum = 0;

        for (int i = 0; i < vectors.length; i++) {
            XSum += vectors[i].getXValue();
            YSum += vectors[i].getYValue();
            ZSum += vectors[i].getZValue();
        }

        return new Vector3D(XSum, YSum, ZSum);
    }

    public Vector3D scale(double scalar) {
        return new Vector3D(XValue * scalar, YValue * scalar, ZValue * scalar);
    }

    public double getMagnitude() {
        return Math.sqrt((XValue * XValue) + (YValue * YValue) + (ZValue * ZValue));
    }

    public double getXValue() {
        return XValue;
    }
    public void setXValue(double XValue) {
        this.XValue = XValue;
    }
    public double getYValue() {
        return YValue;
    }
    public void setYValue(double YValue) {
        this.YValue = YValue;
    }
    public double getZValue() {
        return ZValue;
    }
    public void setZValue(double ZValue) {
        this.ZValue = ZValue;
    }

    @Override
    public String toString() {
        return "XValue: " + this.getXValue() + " | YValue: " + this.getYValue() + " | ZValue: " + this.getZValue() + "\n";
    }
}
