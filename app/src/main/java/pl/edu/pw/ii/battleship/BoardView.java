package pl.edu.pw.ii.battleship;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class BoardView extends View {
    public interface BoardTouchListener {
        void onTouch(int x, int y);
    }

    private final List<BoardTouchListener> listeners = new ArrayList<>();

    private final int boardColor = Color.rgb(22, 41, 82);

    private final Paint boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        boardPaint.setColor(boardColor);
    }

    private final Paint boardLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        int boardLineColor = Color.LTGRAY;
        boardLinePaint.setColor(boardLineColor);
        boardLinePaint.setStrokeWidth(2);
    }

    private boolean displayShips = false;

    private Board board;

    private int boardSize = 10;

    // Create a new board view to be run in the given context
    public BoardView(Context context) {
        super(context);
    }

    // Create a new board view with the given attribute set.
    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Create a new board view with the given attribute set and style.
    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBoard(Board board) {
        this.board = board;
        this.boardSize = board.size();

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int xy = locatePlace(event.getX(), event.getY());
            if (xy >= 0) {
                notifyBoardTouch(xy / 100, xy % 100);
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGrid(canvas);
        drawShotPlaces(canvas);
        if (displayShips) {
            drawShips(canvas);
        }
        drawShipHitPlaces(canvas);
        drawShipSunkPlaces(canvas);
    }

    private void drawShips(Canvas canvas) {
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (board.placeAt(x, y).hasShip()) {
                    drawSquare(canvas, Color.argb(215, 255, 255, 255), x, y);
                }
            }
        }
    }

    private void drawShotPlaces(Canvas canvas) {
        // check the state of each place of the board and draw it.
        if (board == null) {
            return;
        }
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (board.placeAt(x, y).isHit()) {
                    drawHitPlace(canvas, x, y, "miss");
                }
            }
        }
    }

    public void drawSquare(Canvas canvas, int color, int x, int y) {
        boardPaint.setColor(color);
        float viewSize = maxCoord();
        float tileSize = viewSize / 10;  //10 Is how many tiles there are
        float offSet = 8;
        canvas.drawRect((tileSize * x) + offSet, (tileSize * y) + offSet, ((tileSize * x) + tileSize) - offSet, (((viewSize / 10) * y) + tileSize) - offSet, boardPaint);
    }

    public void drawHitPlace(Canvas canvas, int x, int y, String type) {
        Bitmap b;
        switch (type) {
            case "miss":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.miss);
                break;
            case "hit":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.hit);
                break;
            case "sunk":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.sunk);
                break;
            default:
                return;
        }

        float viewSize = maxCoord();
        float tileSize = viewSize / 10;  //10 Is how many tiles there are
        float offSet = 8;

        Bitmap crossed = Bitmap.createScaledBitmap(b, (int) (tileSize - 2 * offSet), (int) (tileSize - 2 * offSet), false);
        crossed.setHasAlpha(false);
        canvas.drawBitmap(crossed, (tileSize * x) + offSet, (tileSize * y) + offSet, boardPaint);
    }

    public void drawShipHitPlaces(Canvas canvas) {
        if (board == null) {
            return;
        }
        List<Place> shipHitPlaces = board.getShipHitPlaces();
        for (Place places : shipHitPlaces) {
            drawHitPlace(canvas, places.getX(), places.getY(), "hit");
        }
    }

    public void drawShipSunkPlaces(Canvas canvas) {
        if (board == null) {
            return;
        }
        List<Place> shipSunkPlaces = board.getShipSunkPlaces();
        for (Place places : shipSunkPlaces) {
            drawHitPlace(canvas, places.getX(), places.getY(), "sunk");
        }
    }

    private void drawGrid(Canvas canvas) {
        final float maxCoord = maxCoord();
        final float placeSize = lineGap();
        boardPaint.setColor(boardColor);
        canvas.drawRect(0, 0, maxCoord, maxCoord, boardPaint);
        for (int i = 0; i < numOfLines(); i++) {
            float xy = i * placeSize;
            canvas.drawLine(0, xy, maxCoord, xy, boardLinePaint); // horizontal line
            canvas.drawLine(xy, 0, xy, maxCoord, boardLinePaint); // vertical line
        }
    }

    protected float lineGap() {
        return Math.min(getMeasuredWidth(), getMeasuredHeight()) / (float) boardSize;
    }

    private int numOfLines() {
        return boardSize + 1;
    }

    protected float maxCoord() {
        return lineGap() * (numOfLines() - 1);
    }

    public int locatePlace(float x, float y) {
        if (x <= maxCoord() && y <= maxCoord()) {
            final float placeSize = lineGap();
            int ix = (int) (x / placeSize);
            int iy = (int) (y / placeSize);
            return ix * 100 + iy;
        }
        return -1;
    }

    public void addBoardTouchListener(BoardTouchListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private void notifyBoardTouch(int x, int y) {
        for (BoardTouchListener listener : listeners) {
            listener.onTouch(x, y);
        }
    }

    public void displayBoardsShips(boolean display) {
        displayShips = display;
    }
}
