package amazons;
import org.junit.Test;
import static org.junit.Assert.*;
import ucb.junit.textui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** Junit tests for our Board iterators.
 *  @author Anastasia
 */
public class IteratorTests {

    /** Run the JUnit tests in this package. */
    public static void main(String[] ignored) {
        textui.runClasses(IteratorTests.class);
    }

    /** Tests reachableFromIterator to make sure it returns all reachable
     *  Squares. This method may need to be changed based on
     *   your implementation. */
    @Test
    public void testReachableFrom() {
        Board b = new Board();
        buildBoard(b, REACHEABLE_FROM_TEST);
        int numSquares = 0;
        Set<Square> squares = new HashSet<>();
        Iterator<Square> reachableFrom = b.reachableFrom(Square.sq(5, 4), null);
        while (reachableFrom.hasNext()) {
            Square s = reachableFrom.next();
            assertTrue(REACHEABLE_SQUARES_TEST.contains(s));
            numSquares += 1;
            squares.add(s);
        }
        assertEquals(REACHEABLE_SQUARES_TEST.size(), numSquares);
        assertEquals(REACHEABLE_SQUARES_TEST.size(), squares.size());
    }

    /** Tests legalMovesIterator to make sure it returns all legal Moves.
     *  This method needs to be finished and may need to be changed
     *  based on your implementation. */
    @Test
    public void testLegalMoves() {
        Board b = new Board();
        Board c = new Board();

        buildBoard(b, TEST_LEGAL);
        buildBoard(c, TEST_FULL);

        int zero = 0;
        int numMoves = 0;

        Set<Move> moves = new HashSet<>();
        Set<Move> movesC = new HashSet<>();

        Iterator<Move> legalMoves = b.legalMoves(Piece.WHITE);
        Iterator<Move> legalMovesFull = c.legalMoves(Piece.WHITE);

        while (legalMoves.hasNext()) {
            Move m = legalMoves.next();
            assertTrue(LEGALTESTMOVES.contains(m));
            Square from = m.from();
            Square to = m.to();
            numMoves += 1;
            moves.add(m);
        }

        while (legalMoves.hasNext()) {
            Move m = legalMoves.next();
            numMoves += 1;
            moves.add(m);

        }

        assertEquals(4, numMoves);
        assertEquals(4, moves.size());

    }


    private void buildBoard(Board b, Piece[][] target) {
        for (int col = 0; col < Board.SIZE; col++) {
            for (int row = Board.SIZE - 1; row >= 0; row--) {
                Piece piece = target[Board.SIZE - row - 1][col];
                b.put(piece, Square.sq(col, row));
            }
        }
        System.out.println(b);
    }

    static final Piece E = Piece.EMPTY;

    static final Piece W = Piece.WHITE;

    static final Piece B = Piece.BLACK;

    static final Piece S = Piece.SPEAR;

    static final Piece[][] REACHEABLE_FROM_TEST = {
            { E, E, E, E, E, E, E, E, E, E },
            { E, E, E, E, E, E, E, E, W, W },
            { E, E, E, E, E, E, E, S, E, S },
            { E, E, E, S, S, S, S, E, E, S },
            { E, E, E, S, E, E, E, E, B, E },
            { E, E, E, S, E, W, E, E, B, E },
            { E, E, E, S, S, S, B, W, B, E },
            { E, E, E, E, E, E, E, E, E, E },
            { E, E, E, E, E, E, E, E, E, E },
            { E, E, E, E, E, E, E, E, E, E }};

    static final Set<Square> REACHEABLE_SQUARES_TEST =
            new HashSet<>(Arrays.asList(
                    Square.sq(5, 5),
                    Square.sq(4, 5),
                    Square.sq(4, 4),
                    Square.sq(6, 4),
                    Square.sq(7, 4),
                    Square.sq(6, 5),
                    Square.sq(7, 6),
                    Square.sq(8, 7)));


    static final Piece[][] TEST_LEGAL = {
            { S, S, S, E, E, E, E, E, E, E},
            { S, W, S, S, E, E, E, E, E, E},
            { S, E, E, S, E, E, E, E, E, E},
            { S, S, S, S, E, E, E, E, E, E},
            { E, E, E, E, E, E, E, E, E, E},
            { E, E, E, E, S, S, S, E, E, E},
            { E, E, E, B, B, W, S, E, E, E},
            { E, E, E, B, W, W, S, E, E, E},
            { E, E, E, B, S, S, S, E, E, E},
            { E, E, E, E, E, E, E, E, E, E}};

    static final Piece[][] TEST_FULL = {
            { S, S, S, S, S, S, S, S, S, S},
            { S, W, S, S, S, S, S, S, S, S},
            { S, S, S, S, S, S, S, S, S, S},
            { S, S, S, S, S, S, S, S, S, S},
            { S, S, S, S, S, S, S, S, S, S},
            { S, S, S, S, S, S, S, S, S, S},
            { S, S, S, B, B, W, S, S, S, S},
            { S, S, S, B, W, W, S, S, S, S},
            { S, S, S, B, S, S, S, S, S, S},
            { S, S, S, S, S, S, S, S, S, S}};

    static final Set<Move> LEGALTESTMOVES =
            new HashSet<>(Arrays.asList(
                    Move.mv("b9 b8 c8"),
                    Move.mv("b9 b8 b9"),
                    Move.mv("b9-c8(b9)"),
                    Move.mv("b9-c8(b8)")
            )
            );
}
