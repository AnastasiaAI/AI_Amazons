package amazons;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static amazons.Utils.*;

/** Represents a position on an Amazons board.  Positions are numbered
 *  from 0 (lower-left corner) to 99 (upper-right corner).  Squares
 *  are immutable and unique: there is precisely one square created for
 *  each distinct position.  Clients create squares using the factory method
 *  sq, not the constructor.  Because there is a unique Square object for each
 *  position, you can freely use the cheap == operator (rather than the
 *  .equals method) to compare Squares, and the program does not waste time
 *  creating the same square over and over again.
 *  @author Anastasia
 */
final class Square {

    /** The regular expression for a square designation (e.g.,
     *  a3). For convenience, it is in parentheses to make it a
     *  group.  This subpattern is intended to be incorporated into
     *  other pattern that contain square designations (such as
     *  patterns for moves). */
    static final String SQ = "([a-j](?:[1-9]|10))";

    /** Return my row position, where 0 is the bottom row. */
    int row() {
        return _row;
    }

    /** Return my column position, where 0 is the leftmost column. */
    int col() {
        return _col;
    }

    /** Return my index position (0-99).  0 represents square a1, and 99
     *  is square j10. */
    int index() {
        return _index;
    }

    /** Return true iff THIS - TO is a valid queen move. */
    boolean isQueenMove(Square to) {
        if (to == null) {
            return false;
        }

        int col2 = to.col(), row2 = to.row();
        int col1 = this.col(), row1 = this.row();

        int colDir = Math.abs(col2 - col1);
        int rowDir = Math.abs(row2 - row1);

        return (this != to) && (colDir == 0 || rowDir == 0 || colDir == rowDir);
    }

    /** Definitions of direction for queenMove.  DIR[k] = (dcol, drow)
     *  means that to going one step from (col, row) in direction k,
     *  brings us to (col + dcol, row + drow). */
    private static final int[][] DIR = {
            { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 },
            { 0, -1 }, { -1, -1 }, { -1, 0 }, { -1, 1 }
    };

    /** Return the Square that is STEPS>0 squares away from me in direction
     *  DIR, or null if there is no such square.
     *  DIR = 0 for north, 1 for northeast, 2 for east, etc., up to 7 for west.
     *  If DIR has another value, return null. Thus, unless the result
     *  is null the resulting square is a queen move away rom me. */
    Square queenMove(int dir, int steps) {
        if (dir < 0) {
            return null;
        }
        if (dir > 7) {
            return null;
        }
        if (steps < 1) {
            return null;
        }

        int col = _col + DIR[dir][0] * steps;
        int r = _row + DIR[dir][1] * steps;

        if (exists(col, r)) {
            return sq(col, r);
        }
        return null;
    }

    /** Return the direction (an int as defined in the documentation
     *  for queenMove) of the queen move THIS-TO. */
    int direction(Square to) {
        assert isQueenMove(to);

        int col2 = to.col(), row2 = to.row();
        int col1 = this.col(), row1 = this.row();

        int colDir = (col2 - col1);
        if (colDir != 0) {
            colDir /= Math.abs(col2 - col1);
        }
        int rowDir = (row2 - row1);
        if (rowDir != 0) {
            rowDir /= Math.abs(row2 - row1);
        }

        int[] check = {colDir, rowDir};
        for (int index = 0; index < DIR.length; index += 1) {
            int[] curDir = DIR[index];
            if (areSameDir(check, curDir)) {
                return index;
            }
        }

        System.out.println("Weird behavior in Square.direction()");
        return -1;
    }

    /** Returns true if single move of a Queen doesn't have turns.
     * @param a1 is start
     * @param a2 is to. */
    boolean areSameDir(int[] a1, int[] a2) {
        int dcol1 = a1[0];
        int dcol2 = a2[0];

        int drow1 = a1[1];
        int drow2 = a2[1];

        return (dcol1 == dcol2) && (drow1 == drow2);
    }

    @Override
    public String toString() {
        return _str;
    }

    /** Return true iff COL ROW is a legal square. */
    static boolean exists(int col, int row) {
        return row >= 0 && col >= 0 && row < Board.SIZE && col < Board.SIZE;
    }

    /** Return the (unique) Square denoting COL ROW. */
    static Square sq(int col, int row) {
        if (!exists(row, col)) {
            throw error("row or column out of bounds");
        }
        return sq(col * 10 + row);
    }

    /** Return the (unique) Square denoting the position with index INDEX. */
    static Square sq(int index) {
        if (index < 0 || index >= SQUARES.length) {
            throw error("index out of bounds");
        }
        return SQUARES[index];
    }

    /** Return the (unique) Square denoting the position COL ROW, where
     *  COL ROW is the standard text format for a square (e.g., a4). */
    static Square sq(String col, String row) {
        int start = (int) ('a');
        int c1 = (int) (col.charAt(0));
        int coloumn = c1 - start;
        int rw = Integer.parseInt(row);
        return sq(coloumn, rw - 1);
    }

    /** Return the (unique) Square denoting the position in POSN, in the
     *  standard text format for a square (e.g. a4). POSN must be a
     *  valid square designation. */
    static Square sq(String posn) {
        assert posn.matches(SQ);
        String col = posn.substring(0, 1);
        String row = posn.substring(1);

        return sq(col, row);
    }

    /** Return an iterator over all Squares. */
    static Iterator<Square> iterator() {
        return SQUARE_LIST.iterator();
    }

    /** Return the Square with index INDEX. */
    private Square(int index) {
        _index = index;
        _row = index % 10;
        _col = (index - _row) / 10;
        char col = (char) ((int) ('a') + _col);
        _str = col + String.valueOf(_row + 1);
    }

    /** @param index Converts to .
     *  @return array */
    private static int[] toRowCol(int index) {
        int row = index / Board.SIZE;
        int col = index % Board.SIZE;
        int[] res = new int[]{row, col};
        return res;
    }

    /** The cache of all created squares, by index. */
    private static final Square[] SQUARES =
            new Square[Board.SIZE * Board.SIZE];

    /** SQUARES viewed as a List. */
    private static final List<Square> SQUARE_LIST = Arrays.asList(SQUARES);

    static {
        for (int i = Board.SIZE * Board.SIZE - 1; i >= 0; i -= 1) {
            SQUARES[i] = new Square(i);
        }
    }

    /** My index position. */
    private final int _index;

    /** My row and column (redundant, since these are determined by _index). */
    private final int _row, _col;

    /** My String denotation. */
    private final String _str;
}
