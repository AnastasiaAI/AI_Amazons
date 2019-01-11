package amazons;


import static java.lang.Math.*;
import java.util.Iterator;

import static amazons.Piece.*;

/** A Player that automatically generates moves.
 *  @author Anastasia
 */
class AI extends Player {

    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, INFTY, -INFTY);
        } else {
            findMove(b, maxDepth(b), true, -1, INFTY, -INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** MaxValue in Alpha Beta prunning.
     * @return @return maximizer value
     * @param beta is score
     * @param alpha is score
     * @param sense is player
     * @param saveMove is result move
     * @param board is board */
    private int maxValue(Board board, boolean saveMove, int sense,
                         int alpha, int beta) {
        int v = -INFTY;
        Iterator<Move> legalMoves = board.legalMoves();
        while (legalMoves.hasNext()) {
            Move m = legalMoves.next();
            Board successor = new Board(board);
            successor.makeMove(m);

            int d = maxDepth(successor);
            int cT = findMove(successor, d, saveMove, -sense, alpha, beta);
            v = Math.max(v, cT);
            if (v >= beta) {
                if (saveMove) {
                    this._lastFoundMove = m;
                }
                return v;
            }
            alpha = Math.max(alpha, v);
        }
        return v;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {

        if (depth == 0 || board.winner() != EMPTY) {

            return staticScore(board);
        }

        int result = sense == 1 ? beta : alpha;
        Iterator<Move> moves = board.legalMoves(sense == 1 ? WHITE : BLACK);

        int currScore = staticScore(board);
        if (sense == 1 && currScore < beta) {
            return 0;
        }
        if (sense == -1 && currScore > alpha) {
            return 0;
        }

        while (moves.hasNext()) {
            Board check = new Board(board);
            Move mov = moves.next();
            check.makeMove(mov);
            int newAlpha = sense == 1 ? alpha : currScore;
            int newBeta = sense == 1 ? currScore : beta;
            int advantage = findMove(check,
                    depth - 1,
                    false,
                    -sense,
                    newAlpha,
                    newBeta);

            if ((sense == 1 && advantage > result)
                    || (sense == -1 && advantage < result)) {
                result = advantage;
                if (saveMove) {
                    _lastFoundMove = mov;
                }
            }
        }

        return result;
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private int maxDepth(Board board) {
        int N = board.numMoves();
        System.out.println("hi");
        int size = Board.SIZE;
        int cutoff = 3;
        int check = size * cutoff;
        int countSpears = 0;
        for (int col = 0; col < 10; col++) {
            for (int row = 0; row < 10; row++) {
                if (board.get(col, row) == SPEAR) {
                    countSpears += 1;
                }
            }
        }
        if (N == 0 && (countSpears > check + 2 * cutoff)) {
            return 2;
        }
        if (N < check) {
            return 1;
        } else {
            cutoff += 2;
            check = size * cutoff;
            if (N < check) {
                return 2;
            } else {
                cutoff += 2;
                check = size * cutoff;
                if (N < check) {
                    return 3;
                } else {
                    cutoff += 1;
                    check = size * cutoff;
                    if (N < check) {
                        return 4;
                    }
                }
            }
        }
        System.out.println("at Depth");
        return 5;
    }


    /** MinValue in Alpha Beta prunning.
     * @return @return maximizer value
     * @param saveMove for chosen move
     * @param beta minimizer
     * @param alpha maximizer
     * @param sense player
     * @param board  board.*/
    private int minValue(Board board, boolean saveMove, int sense,
                         int alpha, int beta) {
        int v = INFTY;
        Iterator<Move> legalMoves = board.legalMoves();
        while (legalMoves.hasNext()) {
            Move m = legalMoves.next();
            Board successor = new Board(board);
            successor.makeMove(m);

            int d = maxDepth(successor);
            int c = findMove(successor, d, saveMove, -sense, alpha, beta);
            v = Math.min(v, c);
            if (v <= alpha) {
                if (saveMove) {
                    this._lastFoundMove = m;
                }
                return v;
            }
            beta = Math.min(beta, v);
        }
        return v;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        Piece winner = board.winner();
        if (winner == BLACK) {
            return -WINNING_VALUE;
        } else if (winner == WHITE) {
            return WINNING_VALUE;
        }

        return board.boardScore();
    }


}

