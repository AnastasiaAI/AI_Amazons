package amazons;

import ucb.gui2.Pad;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import java.awt.Color;
import java.awt.Graphics2D;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import static amazons.Piece.*;
import static amazons.Square.sq;


/** A widget that displays an Amazons game.
 *  @author Anastasia
 */
class BoardWidget extends Pad {

    /* Parameters controlling sizes, speeds, colors, and fonts. */

    /** Colors of empty squares and grid lines. */
    static final Color
            SPEAR_COLOR = new Color(64, 64, 64),
            LIGHT_SQUARE_COLOR = new Color(238, 207, 161),
            DARK_SQUARE_COLOR = new Color(205, 133, 63),
            MAKE_MOVE = new Color(100, 150, 90),
            THROW_SPEAR = new Color(150, 0, 50);

    /** Locations of images of white and black queens. */
    private static final String
            WHITE_QUEEN_IMAGE = "wq4.png",
            BLACK_QUEEN_IMAGE = "bq4.png";

    /** Size parameters. */
    private static final int
            SQUARE_SIDE = 30,
            BOARD_SIDE = SQUARE_SIDE * 10,
            SIZE = Board.SIZE;

    /** A graphical representation of an Amazons board that sends commands
     *  derived from mouse clicks to COMMANDS.  */
    BoardWidget(ArrayBlockingQueue<String> commands) {
        _commands = commands;
        setMouseHandler("click", this::mouseClicked);
        setPreferredSize(BOARD_SIDE, BOARD_SIDE);

        try {
            _whiteQueen = ImageIO.read(Utils.getResource(WHITE_QUEEN_IMAGE));
            _blackQueen = ImageIO.read(Utils.getResource(BLACK_QUEEN_IMAGE));
        } catch (IOException excp) {
            System.err.println("Could not read queen images.");
            System.exit(1);
        }
        _choosen = null;
        _aim = null;
        _acceptingMoves = false;

    }

    /** Square being drawn. */
    private Square _choosen;

    /** Square target. */
    private Square _aim;

    /** Iterator over pixels.
     * @param g is Graphics in 2D.
     * @param newColors is a map of new pixels. */
    public void color(HashMap<Square, Color> newColors, Graphics2D g) {

        for (int coloumn = 0; coloumn < SIZE; coloumn += 1) {
            for (int row = 0; row < SIZE; row += 1) {
                Piece check = _board.get(coloumn, row);
                if (check.equals(SPEAR)) {
                    g.setColor(SPEAR_COLOR);
                } else if (newColors.containsKey(Square.sq(coloumn, row))) {
                    g.setColor(newColors.get(Square.sq(coloumn, row)));
                } else {
                    int to = coloumn + (row % 2);
                    g.setColor(to % 2 == 0
                            ?
                            LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR);
                }


                int x = coloumn * SQUARE_SIDE;
                int y = (Board.SIZE - 1 - row) * SQUARE_SIDE;
                g.fillRect(x, y, SQUARE_SIDE, SQUARE_SIDE);

            }
        }
    }

    /** Draw the board without pieces or spears.
     * @param g is Graphics in 2D. */
    private void drawGrid(Graphics2D g) {
        g.setColor(LIGHT_SQUARE_COLOR);
        g.fillRect(0, 0, BOARD_SIDE, BOARD_SIDE);
        HashMap<Square, Color> newColors = new HashMap<>();
        if (_aim != null && _choosen != null) {
            Iterator<Square> option = _board.reachableFrom(_aim, _choosen);
            while (option.hasNext()) {
                Square key = option.next();
                newColors.put(key, THROW_SPEAR);
                newColors.put(_aim, MAKE_MOVE);
            }
        } else if (_aim == null) {
            Iterator<Square> option = _board.reachableFrom(_choosen, null);
            while (option.hasNext()) {
                Square key = option.next();
                newColors.put(key, MAKE_MOVE);
            }
        }
        color(newColors, g);
    }

    /** Paint.  */
    @Override
    public synchronized void paintComponent(Graphics2D g) {
        drawGrid(g);
        int size = SIZE ^ 2;
        for (int i = 0; i < size; i++) {
            Square check = Square.sq(i);
            Piece p = _board.get(check);
            if (p == WHITE || p == BLACK) {
                drawQueen(g, check, p);
            }
        }
    }

    /** @param g graphics
     * @param s square
     * @param piece  piece */
    private void drawQueen(Graphics2D g, Square s, Piece piece) {
        int x = cx(s.col()) + 2;
        int y = cy(s.row()) + 4;
        g.drawImage(piece == WHITE ? _whiteQueen : _blackQueen,
                x, y, null);
    }

    /** Make a click on S. */
    private void click(Square s) {

        if (_board.get(s) == _board.turn() && s != _choosen) {
            _choosen = s;
            _aim = null;
        } else if (_aim != null && _board.isUnblockedMove(_aim, s, _choosen)) {
            _commands.add(String.format("%s-%s(%s)", _choosen, _aim, s));
            _choosen = null;
            _aim = null;
        } else if (_choosen != null
                && _board.isUnblockedMove(_choosen, s, null)) {
            _aim = s;
        } else {
            _choosen = null;
            _aim = null;
        }
        repaint();
    }

    /** Make mouse click event E. */
    private synchronized void mouseClicked(String unused, MouseEvent e) {
        int px = e.getX(), py = e.getY();
        int x = px / SQUARE_SIDE;
        int y = (BOARD_SIDE - py) / SQUARE_SIDE;
        if (_acceptingMoves
                && (x > -1) && (SIZE > x) && (y > -1) && (SIZE > y)) {
            click(sq(x, y));
        }
    }

    /** Revise the displayed board according to BOARD. */
    synchronized void update(Board board) {
        _board.copy(board);
        repaint();
    }

    /** Turn on move collection iff COLLECTING, and clear any current
     *  partial selection.   When move collection is off, ignore clicks on
     *  the board. */
    void setMoveCollection(boolean collecting) {
        _acceptingMoves = collecting;
        repaint();
    }

    /** Return x-pixel coordinate of the left corners of column X
     *  relative to the upper-left corner of the board. */
    private int cx(int x) {
        return x * SQUARE_SIDE;
    }

    /** Return y-pixel coordinate of the upper corners of row Y
     *  relative to the upper-left corner of the board. */
    private int cy(int y) {
        return (SIZE - y - 1) * SQUARE_SIDE;
    }

    /** Return x-pixel coordinate of the left corner of S
     *  relative to the upper-left corner of the board. */
    private int cx(Square s) {
        return cx(s.col());
    }

    /** Return y-pixel coordinate of the upper corner of S
     *  relative to the upper-left corner of the board. */
    private int cy(Square s) {
        return cy(s.row());
    }

    /** Queue on which to post move commands (from mouse clicks). */
    private ArrayBlockingQueue<String> _commands;
    /** Board being displayed. */
    private final Board _board = new Board();

    /** Image of white queen. */
    private BufferedImage _whiteQueen;
    /** Image of black queen. */
    private BufferedImage _blackQueen;

    /** True iff accepting moves from user. */
    private boolean _acceptingMoves;
}

