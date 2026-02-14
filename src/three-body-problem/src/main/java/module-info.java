module ics4u.threebodyproblem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens ics4u.threebodyproblem to javafx.fxml;
    exports ics4u.threebodyproblem;
}