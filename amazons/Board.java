package amazons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import static amazons.Piece.*;


/** The state of an Amazons Game.
 *  @author Anastasia
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 10;

    /** Size of the Board. */
    static final int BOARD_SIZE = SIZE * SIZE;

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {

        if (this == model) {
            return;
        }

        init();

        this._whoseTurn = model._whoseTurn;
        this._winner = model._winner;
        this._movesSoFar = new ArrayList<Move>();

        for (Move m: _movesSoFar) {
            _movesSoFar.add(Move.mv(m.from(), m.to(), m.spear()));
        }

        this._boardLayout = new Piece[BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            _boardLayout[i] = model._boardLayout[i];
        }
    }


    /** Clears the board to the initial position. */
    void init() {


        _movesSoFar = new ArrayList<amazons.Move>();
        _boardLayout = new amazons.Piece[BOARD_SIZE];
        _whoseTurn = WHITE;
        _winner = EMPTY;

        for (int i = 0; i < BOARD_SIZE; i++) {
            _boardLayout[i] = EMPTY;
        }

        int w1 = (SIZE / 2) - 2;
        int w2 = SIZE * 3;
        int w3 = w2 * 2;
        int w4 = w2 * 3 + 3;
        int b1 = w1 * 2;
        int b2 = w2 + 3 * w1;
        int b3 = w3 + 3 * w1;
        int b4 = w4 + w1;

        int[] startingWhite = {w1, w2, w3, w4};
        int[] startingBlack = {b1, b2, b3, b4};
        for (int sqW : startingWhite) {
            _boardLayout[sqW] = WHITE;
        }
        for (int sqB : startingBlack) {
            _boardLayout[sqB] = BLACK;
        }
    }

    /** Return the Piece whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _whoseTurn;
    }

    /** Return the number of moves (that have not been undone) for this
     *  board. */
    int numMoves() {
        return _movesSoFar.size();
    }

    /** Return the winner in the current position, or null if the game is
     *  not yet finished. */
    Piece winner() {
        return _winner;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return _boardLayout[s.index()];
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW < 9. */
    final Piece get(int col, int row) {

        return get(Square.sq(col, row));
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {

        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {

        _boardLayout[s.index()] = p;
    }

    /** Set square (COL, ROW) to P. */
    final void put(Piece p, int col, int row) {

        put(p, Square.sq(col, row));
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, col - 'a', row - '1');
    }

    /** Return true iff FROM - TO is an unblocked queen move on the current
     *  board, ignoring the contents of ASEMPTY, if it is encountered.
     *  For this to be true, FROM-TO must be a queen move and the
     *  squares along it, other than FROM and ASEMPTY, must be
     *  empty. ASEMPTY may be null, in which case it has no effect. */
    boolean isUnblockedMove(Square from, Square to, Square asEmpty) {
        int direction = from.direction(to);
        if (direction == -1) {
            return false;
        }

        int steps = 0;
        Square check = null;

        while (check != to) {
            check = from.queenMove(direction, steps + 1);
            steps += 1;
            if (check == null) {
                return false;
            } else if (check == asEmpty) {
                continue;
            } else if (_boardLayout[check.index()] != EMPTY) {
                return false;
            }
        }

        return true;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return _boardLayout[from.index()] == _whoseTurn;
    }

    /** Return true iff FROM-TO is a valid first part of move, ignoring
     *  spear throwing. */
    boolean isLegal(Square from, Square to) {

        boolean notBlocked = isUnblockedMove(from, to, null);
        return isLegal(from) && notBlocked;
    }

    /** Return true iff FROM-TO(SPEAR) is a legal move in the current
     *  position. */
    boolean isLegal(Square from, Square to, Square spear) {
        return isLegal(from)
                && isLegal(from, to)
                && isUnblockedMove(to, spear, from);
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to(), move.spear());
    }

    /** Move FROM-TO(SPEAR), assuming this is a legal move. */
    void makeMove(Square from, Square to, Square spear) {
        assert (isLegal(from, to, spear));
        makeMove(Move.mv(from, to, spear));
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        _movesSoFar.add(move);
        _boardLayout[move.from().index()] = EMPTY;
        _boardLayout[move.to().index()] = _whoseTurn;
        _boardLayout[move.spear().index()] = SPEAR;

        if (_whoseTurn == BLACK) {
            _whoseTurn = WHITE;
        } else {
            _whoseTurn = BLACK;
        }
        LegalMoveIterator i = new LegalMoveIterator(_whoseTurn);
        if (!i.hasNext()) {
            if (_whoseTurn == BLACK) {
                System.out.println(_whoseTurn);
                _winner = WHITE;
            } else {
                _winner = BLACK;
            }
        }
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {

        if (_movesSoFar.size() == 0) {
            return;
        }

        if (_whoseTurn == WHITE) {
            _whoseTurn = BLACK;
        } else {
            _whoseTurn = WHITE;
        }

        Move mv = _movesSoFar.get(_movesSoFar.size() - 1);
        _boardLayout[mv.from().index()] = _whoseTurn;
        _boardLayout[mv.spear().index()] = EMPTY;
        _boardLayout[mv.to().index()] = EMPTY;
        int last = _movesSoFar.size() - 1;
        _movesSoFar.remove(last);
    }

    /** Return an Iterator over the Squares that are reachable by an
     *  unblocked queen move from FROM. Does not pay attention to what
     *  piece (if any) is on FROM, nor to whether the game is finished.
     *  Treats square ASEMPTY (if non-null) as if it were EMPTY.  (This
     *  feature is useful when looking for Moves, because after moving a
     *  piece, one wants to treat the Square it came from as empty for
     *  purposes of spear throwing.) */
    Iterator<Square> reachableFrom(Square from, Square asEmpty) {
        return new ReachableFromIterator(from, asEmpty);
    }

    /** Return an Iterator over all legal moves on the current board. */
    Iterator<Move> legalMoves() {
        return new LegalMoveIterator(_whoseTurn);
    }

    /** Return an Iterator over all legal moves on the current board for
     *  SIDE (regardless of whose turn it is). */
    Iterator<Move> legalMoves(Piece side) {
        return new LegalMoveIterator(side);
    }

    /** An iterator used by reachableFrom. */
    private class ReachableFromIterator implements Iterator<Square> {

        /** Iterator of all squares reachable by queen move from FROM,
         *  treating ASEMPTY as empty. */
        ReachableFromIterator(Square from, Square asEmpty) {
            _from = from;
            _dir = -1;
            _steps = 0;
            _asEmpty = asEmpty;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return _dir < 8;
        }

        @Override
        public Square next() {
            Square next = null;
            if (hasNext()) {
                next = _from.queenMove(_dir, _steps);
                toNext();
            }
            return next;
        }

        /** Advance _dir and _steps, so that the next valid Square is
         *  _steps steps in direction _dir from _from. */
        private void toNext() {

            _steps += 1;
            Square to = _from.queenMove(_dir, _steps);
            while (_dir < 8
                    && (to == null
                    || !isUnblockedMove(_from, to, _asEmpty))) {

                _steps = 1;
                to = _from.queenMove(_dir + 1, 1);
                boolean goodNext = to != null && to != _asEmpty;

                if (goodNext && _boardLayout[to.index()] != EMPTY) {
                    to = null;
                }
                _dir++;
            }
        }

        /** Starting square. */
        private Square _from;
        /** Current direction. */
        private int _dir;
        /** Current distance. */
        private int _steps;
        /** Square treated as empty. */
        private Square _asEmpty;
    }

    /** An iterator used by legalMoves. */
    private class LegalMoveIterator implements Iterator<amazons.Move> {

        /** All legal moves for SIDE (WHITE or BLACK). */
        LegalMoveIterator(amazons.Piece side) {
            _startingSquares = amazons.Square.iterator();
            _spearThrows = NO_SQS;
            _pieceMoves = NO_SQS;
            _fromPiece = side;
            _nextSquare = _start = null;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return _start != null;
        }

        @Override
        public amazons.Move next() {
            Move mov = Move.mv(_start, _nextSquare, _spearThrows.next());
            toNext();
            return mov;
        }

        /** Advance so that the next valid Move is
         *  _start-_nextSquare(sp), where sp is the next value of
         *  _spearThrows. */
        private void toNext() {

            while (!_spearThrows.hasNext()) {
                if (_pieceMoves.hasNext()) {
                    _nextSquare = _pieceMoves.next();
                    Square n = _nextSquare;
                    _spearThrows = new ReachableFromIterator(n, _start);
                } else if (_startingSquares.hasNext()) {
                    while (_startingSquares.hasNext()) {
                        _start = _startingSquares.next();
                        if (_boardLayout[_start.index()] == _fromPiece) {
                            Square st = _start;
                            _pieceMoves = new ReachableFromIterator(st, null);
                            _spearThrows = NO_SQS;
                            break;
                        }
                    }
                } else {
                    _start = null;
                    break;
                }
            }
        }

        /** Color of side whose moves we are iterating. */
        private Piece _fromPiece;
        /** Current starting square. */
        private Square _start;
        /** Remaining starting squares to consider. */
        private Iterator<Square> _startingSquares;
        /** Current piece's new position. */
        private Square _nextSquare;
        /** Remaining moves from _start to consider. */
        private Iterator<Square> _pieceMoves;
        /** Remaining spear throws from _piece to consider. */
        private Iterator<Square> _spearThrows;
        /** Current spear throw.*/
        private Square _spear;
    }

    @Override
    public String toString() {
        String result = "";

        for (int r = SIZE - 1; r >= 0; r--) {
            result += "   ";
            for (int c = 0; c < SIZE; c++) {
                Piece thing = get(c, r);
                result += thing.toString();
                if (c != SIZE - 1) {
                    result += " ";
                }
            }
            result += "\n";
        }
        return result;
    }

    /** An empty iterator for initialization. */
    private static final Iterator<Square> NO_SQS = Collections.emptyIterator();

    /** Piece whose turn it is (BLACK or WHITE). */
    private Piece _whoseTurn;

    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;

    /** Current board. */
    private Piece[] _boardLayout;

    /** Moves made so far. */
    private ArrayList<Move> _movesSoFar;

    /** Returns moves made so far. */
    public ArrayList<Move> moves() {
        return _movesSoFar;
    }

    /** @return score integer.*/
    public int boardScore() {
        int[] blackPieces = new int[4], whitePieces = new int[4];
        int boardSize = SIZE * SIZE;

        for (int p = 0, wc = 0, bc = 0; p < boardSize; p++) {
            if (_boardLayout[p].equals(Piece.WHITE)) {
                whitePieces[wc] = p;
                wc++;
            } else if (_boardLayout[p].equals(Piece.BLACK)) {
                blackPieces[bc] = p;
                bc++;
            }
        }

        int[] whiteSpears = new int[boardSize];
        int[] blackSpears = new int[boardSize];
        Arrays.fill(whiteSpears, 0);
        Arrays.fill(blackSpears, 0);
        for (int i = 0; i < boardSize; i++) {
            if (_boardLayout[i].equals(Piece.EMPTY)) {
                continue;
            } else {
                whiteSpears[i] = -1;
                blackSpears[i] = -1;
            }
        }

        for (int placement: whitePieces) {
            Square queenSquare = Square.sq(placement);
            claimSquare(queenSquare, 1, whiteSpears);
        }

        for (int placement: blackPieces) {
            Square queenSquare = Square.sq(placement);
            claimSquare(queenSquare, 1, blackSpears);
        }

        boolean searchWhite = true, searchBlack = true;
        int f = 1;

        while (searchWhite || searchBlack) {
            boolean wS = searchWhite;
            boolean bS = searchBlack;
            searchWhite = false;
            searchBlack = false;
            for (int i2 = 0; i2 < boardSize; i2++) {
                if (whiteSpears[i2] == f && wS) {
                    boolean b1 = claimSquare(Square.sq(i2), f + 1, whiteSpears);
                    searchWhite = searchWhite | b1;
                }
                if (blackSpears[i2] == f && bS) {
                    boolean b2 = claimSquare(Square.sq(i2), f + 1, blackSpears);
                    searchBlack = searchBlack | b2;
                }
            }
        }
        return score(whiteSpears, blackSpears);
    }

    /** @param player1 is white
     * @param player2 is black.
     * @return score integer */
    private int score(int[] player1, int[] player2) {
        int gain = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (player1[i] > 0 && (player1[i] < player2[i])) {
                gain++;
            } else if (player2[i] > 0 && (player1[i] > player2[i])) {
                gain -= 1;
            }
        }
        return gain;
    }


    /** Squares that can be reached in 1 move by throwing a spear.  */
    private int _squareOwnership;

    /** Squares that can be reached in 1 move by throwing a spear.
     * @return how many squeres player owns. */
    public int squareOwnership() {
        return _squareOwnership;
    }

    /** Claims the square by a Spear
     * AND @return fills the mapSpears of WhiteSpears or BlackSpears.
     * @param from is starting square
     * @param value is a flag
     * @param mapSpears is map of scores for white ore black player. */
    private boolean claimSquare(Square from, int value, int[] mapSpears) {
        ReachableFromIterator allPlaces = new ReachableFromIterator(from, null);
        boolean owned = false;
        while (allPlaces.hasNext()) {
            int placeIndex = allPlaces.next().index();
            if (mapSpears[placeIndex] == 0) {
                mapSpears[placeIndex] = value;
                owned = true;
            }
        }
        return  owned;
    }


}
