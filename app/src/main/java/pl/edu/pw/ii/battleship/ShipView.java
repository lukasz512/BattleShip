package pl.edu.pw.ii.battleship;


import android.widget.ImageView;

class ShipView {
    private final ImageView shipImage;
    private final Ship ship;
    private boolean isSelected = false;

    ShipView(ImageView image, Ship newShip) {
        shipImage = image;
        ship = newShip;
    }

    boolean isSelected() {
        return isSelected;
    }

    void setSelected(boolean selected) {
        isSelected = selected;
    }

    ImageView getShipImage() {
        return shipImage;
    }

    public Ship getShip() {
        return ship;
    }
}
