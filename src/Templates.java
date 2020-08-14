package MatchThree;

import javafx.scene.paint.Color;

class Templates {

    private static Color[] colors = {
            Color.web("#845ec2"),
            Color.web("#ff6f91"),
            Color.web("#ffc75f"),
            Color.web("#0081cf"),
            Color.web("#ff8066"),
            Color.web("#00c2a8"),
            Color.web("#bf34b4")};

    static Color getColor(int index) {
        return colors[index];
    }
}