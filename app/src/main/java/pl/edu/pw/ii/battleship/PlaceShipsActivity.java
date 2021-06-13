package pl.edu.pw.ii.battleship;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.LinkedList;
import java.util.List;

public class PlaceShipsActivity extends AppCompatActivity {
    private BoardView boardView;
    private Board playerBoard;
    private ShipView shipBeingDragged = null;
    private final List<ShipView> fleetView = new LinkedList<>();
    private Button placeButton;
    private String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.content_place_ships, null);
        setContentView(layout);

        //Activity entrance and exit animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // set place button
        placeButton = (Button) findViewById(R.id.placeButton);
        enablePlaceButton(false);

        // get player data
        Intent intent = getIntent();
        uuid = intent.getStringExtra("uuid");

        // set the Board
        boardView = (BoardView) findViewById(R.id.placeShipsBoardView);
        playerBoard = new Board();
        boardView.setBoard(playerBoard);
        boardView.displayBoardsShips(true);

        ImageView minesweeper = (ImageView) findViewById(R.id.minesweeperStatus);
        ImageView frigate = (ImageView) findViewById(R.id.frigate);
        ImageView submarine = (ImageView) findViewById(R.id.submarine);
        ImageView battleship = (ImageView) findViewById(R.id.battleship);
        ImageView aircraftcarrier = (ImageView) findViewById(R.id.aircraftcarrier);

        fleetView.add(new ShipView(minesweeper, new Ship("minesweeper", 2)));
        fleetView.add(new ShipView(frigate, new Ship("frigate", 3)));
        fleetView.add(new ShipView(submarine, new Ship("submarine", 3)));
        fleetView.add(new ShipView(battleship, new Ship("battleship", 4)));
        fleetView.add(new ShipView(aircraftcarrier, new Ship("aircraftcarrier", 5)));

        for (ShipView shipView : fleetView) {
            setShipImage(shipView);
        }
        setContentView(layout);
        setBoardDragListener(boardView, playerBoard);
        boardView.invalidate();
    }

    public void setBoardDragListener(final BoardView boardView, final Board board) {
        boardView.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                float x = event.getX();
                float y = event.getY();
                int width;
                int height;

                if (!shipBeingDragged.getShip().getDir()) {
                    width = shipBeingDragged.getShipImage().getHeight();
                    height = shipBeingDragged.getShipImage().getWidth();

                } else {
                    width = shipBeingDragged.getShipImage().getWidth();
                    height = shipBeingDragged.getShipImage().getHeight();
                }

                //x and y coordinates of top-left of image, relative to the board
                float boardX = x - (width / 2);
                float boardY = y - (height / 2);

                int xy = boardView.locatePlace(boardX, boardY);
                if (xy == -1) {
                    return true;
                }
                int xGrid = xy / 100;
                int yGrid = xy % 100;

                if (!board.placeShip(shipBeingDragged.getShip(), xGrid, yGrid, shipBeingDragged.getShip().getDir())) {
                    return true;
                }

                if (!shipBeingDragged.getShip().getDir()) {
                    shipBeingDragged.getShipImage().setX(v.getX() + (xGrid * (v.getWidth() / 10)) - (height / 2) + (width / 2));
                    shipBeingDragged.getShipImage().setY(v.getY() + (yGrid * (v.getHeight() / 10)) + (height / 2) - (width / 2));

                } else {
                    shipBeingDragged.getShipImage().setX(v.getX() + (xGrid * (v.getWidth() / 10)));
                    shipBeingDragged.getShipImage().setY(v.getY() + (yGrid * (v.getHeight() / 10)));
                }

                boardView.invalidate();
                if (allShipsPlaced()) {
                    enablePlaceButton(true);
                }
            }
            return true;
        });
    }

    public boolean allShipsPlaced() {
        for (ShipView ship : fleetView) {
            if (ship.getShip() == null) {
                return false;
            }
            if (!ship.getShip().isPlaced()) {
                return false;
            }
        }
        return true;
    }

    private void setShipImage(final ShipView shipView) {
        setImageScaling(shipView.getShipImage());
        setTouchListener(shipView);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListener(final ShipView shipView) {
        final ImageView image = shipView.getShipImage();
        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText("", "");
                    double rotationRad = Math.toRadians(image.getRotation());
                    final int w = (int) (image.getWidth() * image.getScaleX());
                    final int h = (int) (image.getHeight() * image.getScaleY());
                    double s = Math.abs(Math.sin(rotationRad));
                    double c = Math.abs(Math.cos(rotationRad));
                    final int width = (int) (w * c + h * s);
                    final int height = (int) (w * s + h * c);
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(image) {
                        @Override
                        public void onDrawShadow(Canvas canvas) {
                            canvas.scale(image.getScaleX(), image.getScaleY(), width / 2,
                                    height / 2);
                            canvas.rotate(image.getRotation(), width / 2, height / 2);
                            canvas.translate((width - image.getWidth()) / 2,
                                    (height - image.getHeight()) / 2);
                            super.onDrawShadow(canvas);
                        }

                        @Override
                        public void onProvideShadowMetrics(Point shadowSize,
                                                           Point shadowTouchPoint) {
                            shadowSize.set(width, height);
                            shadowTouchPoint.set(shadowSize.x / 2, shadowSize.y / 2);
                        }
                    };
                    image.startDrag(data, shadowBuilder, image, 0);
                    shipBeingDragged = shipView;
                    deselectAllShipViews();
                    select(shipView);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    public void rotateButtonTapped(View v) {
        ShipView shipToRotate = findSelectedShip();
        if (shipToRotate != null) {
            rotateShip(shipToRotate);
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        assert shipToRotate != null;
        shipToRotate.getShipImage().setX(width / 3 + 10);
        shipToRotate.getShipImage().setY((height / 4) - 20);

        enablePlaceButton(false);

        if (shipToRotate.getShip() != null) {
            for (Place place : shipToRotate.getShip().getPlacement()) {
                place.setShip(null);
            }
            shipToRotate.getShip().removeShip();
        }
        shipToRotate.getShipImage().setOnTouchListener(null);
        setTouchListener(shipToRotate); //Creates new touch listener to update the shadow builder
        boardView.invalidate();
    }

    private void enablePlaceButton(Boolean enable) {
        if (enable) {
            placeButton.setEnabled(true);
            placeButton.setTextColor(Color.WHITE);
            placeButton.setBackgroundColor(Color.rgb(0, 153, 204));
        } else {
            placeButton.setBackgroundColor(Color.rgb(46, 65, 98));
            placeButton.setTextColor(Color.rgb(115, 115, 115));
            placeButton.setEnabled(false);
        }
    }

    private ShipView findSelectedShip() {
        for (ShipView shipView : fleetView) {
            if (shipView.isSelected()) {
                return shipView;
            }
        }
        return null;
    }

    private void rotateShip(ShipView shipToRotate) {
        if (shipToRotate.getShip().getDir()) {
            shipToRotate.getShipImage().setRotation(90);
            shipToRotate.getShip().setDir(false);
        } else {
            shipToRotate.getShipImage().setRotation(0);
            shipToRotate.getShip().setDir(true);
        }
    }

    public void select(ShipView shipView) {
        shipView.setSelected(true);
        shipView.getShipImage().setBackgroundColor(Color.CYAN);
    }

    public void deselectAllShipViews() {
        for (ShipView shipView : fleetView) {
            shipView.setSelected(false);
            shipView.getShipImage().setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void setImageScaling(final ImageView image) {
        image.setAdjustViewBounds(true);
        ViewTreeObserver vto = image.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(() -> image.setMaxHeight(boardView.getMeasuredHeight() / 10));
    }

    public void goToWaitingActivity(View view) {
        Intent intent = new Intent(PlaceShipsActivity.this, MatchingPlayersActivity.class);
        intent.putExtra("uuid", uuid);
        intent.putExtra("playerBoard", playerBoard);
        startActivity(intent);
    }
}
