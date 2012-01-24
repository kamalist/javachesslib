/**
 *     javachesslib - A Java Chess Library for multi-use applications.
 *     Copyright (C) 2012 http://code.google.com/p/javachesslib/
 *     
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package board;

import board.exception.IllegalMoveException;
import board.exception.WrongColorToMoveException;
import board.printer.DefaultPrinter;

/** This class executes test cases to help 
 * make sure the code is working properly. */
//@SuppressWarnings("unused")
public class test {

	/**
	 * @param args
	 * @throws WrongColorToMoveException 
	 * @throws IllegalMoveException 
	 */
	public static void main(String[] args) throws IllegalMoveException, WrongColorToMoveException {
		/* if they return true, they are sucessful. if they return false, they failed. */
		
		/* DEVELOPER NOTE: 
		 * 
		 * Tests do not have to be entirely comprehensive. Just use a few varied examples, 
		 * like maybe 3-4 tests in a single method; but more comprehensive tests are also
		 * welcome, as long as the methods can still be easily read. 
		 * 
		 * The more tests located in a method, the less likely the program is to be buggy. */
		
		//System.out.println(Board.isValidSAN("kxq"));
		//String file = "A";
		//System.out.println((char)(file.charAt(0)-1));
		System.out.println(System.currentTimeMillis());
		Board b = new Board();
		b.move(true,"e4");
		b.move(false, "e5");
		b.move(true, "Ke2");
		b.move(false, "Qg5");
		b.move(true, "a2a4");
		b.move(false, "Qg4+");
		b.move(true, "Ke1");
		b.getLatestMove().placePiece(Piece.WHITE_PAWN, "H3");
		PositionState ps = b.getLatestMove();
		System.out.println(ps.isInCheck(true) && ps.moveEliminatesCheck(true, "h3", "g4"));
		System.out.println(System.currentTimeMillis());
		
		//b.move(true, "f2f3");
		//b.move(false, "Qxf3");
		//b.getLatestMove().explodeAdjacentPieces("f3");
		//System.out.println(b.getLatestMove().isInCheck(true));
		
		//b.move(true, "e2e3");
		
		/*System.out.println(java.util.Arrays.toString(b.getLatestMove().generateLegalMoves(true)));
		b.getLatestMove().setPrinter(DefaultPrinter.getSingletonInstance());
		System.out.println(b.getLatestMove().draw());
		System.out.println(b.getLatestMove().moveEliminatesCheck(true, "e2", "e3"));
		System.out.println(b.getLatestMove().draw());
		System.out.println(System.currentTimeMillis());*/
		
		/*PositionState[] arr = b.getPositions().toArray(new PositionState[b.getPositions().size()]);
		for(PositionState ps : arr) {
			System.out.println(ps.isWhitesMove() + " " + ps.getFEN());
		}*/
		
		//PositionState ps = PositionState.parseFromFen("8/5p2/1np3p1/1k6/1n6/4q3/3r4/5K2 w - - 0 62");
		//ps.placePiece(Piece.WHITE_PAWN, "G4");
		//ps.placePiece(Piece.WHITE_BISHOP,"A2");
		//ps.placePiece(Piece.WHITE_ROOK, "A1");
		//ps.generatePsuedoLegalMoves(true);
		/*String[] moves = ps.generateLegalMoves(true);
		System.out.println(ps.isInCheck(true) && ps.moveEliminatesCheck("g1", "h1", null));
		//System.out.println(ps.isStalemate());
		System.out.println(java.util.Arrays.toString(moves));*/
		//System.exit(0);
		
		System.out.println("testMoveIsMade: " + testMoveIsMade());
		System.out.println("testGetPiece: " + testGetPiece());
		System.out.println("testIsPieceOnSquare: " + testIsPieceOnSquare());
		System.out.println("testSquaresBetweenHorizontal: " + testSquaresBetweenHorizontal());
		System.out.println("testSquaresBetweenVertical: " + testSquaresBetweenVertical());
		System.out.println("testSquaresBetweenDiagonal: " + testSquaresBetweenDiagonal());
		System.out.println("testKingMoves: " + testKingMoves());
		System.out.println("testRookMoves: " + testRookMoves());
		System.out.println("testBishopMoves: " + testBishopMoves());
		System.out.println("testKnightMoves: " + testKnightMoves());
		System.out.println("testPawnMoves: " + testPawnMoves());
		System.out.println("testGetInternalCoordsOfSquare: " + testGetInternalCoordsOfSquare());
		System.out.println("testGetAllSquaresWithPiece: " + testGetAllSquaresWithPiece());
		System.out.println("testGetPiecesThatCanGoToSquare: " + testGetPiecesThatCanGoToSquare());
		System.out.println("testParseFromFEN: " + testParseFromFEN());
		System.out.println("testGetPiecesOnFile: " + testGetPiecesOnFile());
		System.out.println("testGetPiecesOnRank: " + testGetPiecesOnRank());
		System.out.println("testIsPiecePinned: " + testIsPiecePinned());
		System.out.println("testDirectionBetween: " + testDirectionBetween());
		System.out.println("testGetPieceLocations: " + testGetPieceLocations());
		System.out.println("testGetAdjacentSquares: " + testGetAdjacentSquares());
		System.out.println("testIsCheckmate: " + testIsCheckmate());
		System.out.println("testGetFEN: " + testGetFEN());
		System.out.println("testFilterSquares: " + testFilterSquares());
		System.out.println("testIsWhitesMove: " + testIsWhitesMove());
		System.out.println("testIsValidSquare: " + testIsValidSquare());
		System.out.println("testIsAdjacentSquare: " + testIsAdjacentSquare());
		System.out.println("testIsKingAttackingSquare: " + testIsKingAttackingSquare());
		System.out.println("testIsWhiteColoredSquare: " + testIsWhiteColoredSquare());
	}
	
//	private static int squareTo0x88(String square) {
//		return PositionState.squareTo0x88(square);
//	}
	
	private static boolean testMoveIsMade() {
		PositionState s = new PositionState();
		s.makeMove("e2","e4",null);
		return s.getPiece("e4") == Piece.WHITE_PAWN && s.getPiece("e2") == Piece.EMPTY;
	}
	
	private static boolean testGetPiece() {
		PositionState s = new PositionState();
		return s.getPiece("a1") == Piece.WHITE_ROOK;
	}
	
	private static boolean testIsPieceOnSquare() {
		PositionState s = new PositionState();
		return s.isPieceOnSquare(Piece.BLACK_KING,"e8");
	}
	
	private static String testSquaresBetween(String from,String to) {
		PositionState s = new PositionState();
		String[] squares = s.getSquaresBetween(from,to);
		
		return java.util.Arrays.toString(squares);
	}
	
	private static boolean testSquaresBetweenHorizontal() {
		//System.out.println(testSquaresBetween("a1","d1"));
		//System.out.println(testSquaresBetween("f1","b1"));
		return testSquaresBetween("a1","d1").equals("[B1, C1]") && testSquaresBetween("f1","b1").equals("[E1, D1, C1]");
	}
	
	private static boolean testSquaresBetweenVertical() {
//		System.out.println(testSquaresBetween("a1","a5").equals("[A2, A3, A4]"));
//		System.out.println(testSquaresBetween("D6","D3").equals("[D5, D4]"));
		return testSquaresBetween("a1","a5").equals("[A2, A3, A4]") && testSquaresBetween("D6","D3").equals("[D5, D4]");
	}
	
	private static boolean testSquaresBetweenDiagonal() {
		return testSquaresBetween("a1","h8").equals("[B2, C3, D4, E5, F6, G7]") && 
		testSquaresBetween("a8","e4").equals("[B7, C6, D5]") && 
		testSquaresBetween("h4","e1").equals("[G3, F2]") && 
		testSquaresBetween("e4","a8").equals("[D5, C6, B7]");
	}
	
	private static boolean testKingMoves() {
		PositionState s = new PositionState();
		return s.isLegalKingMove("e4","e5") && s.isLegalKingMove("e4","d5") && 
		s.isLegalKingMove("e4","f5") && s.isLegalKingMove("e4","d4") && 
		s.isLegalKingMove("e4","f4") && s.isLegalKingMove("e4","e3") && 
		s.isLegalKingMove("e4","f3") && s.isLegalKingMove("e4","d3");
	}
	
	private static boolean testRookMoves() {
		PositionState s = new PositionState();
		return s.isLegalRookMove("a3","a6") && s.isLegalRookMove("b3","g3") && 
			s.isLegalRookMove("d6","d3") && s.isLegalRookMove("c5","e5");
	}
	
	private static boolean testBishopMoves() {
		PositionState s = new PositionState();
		return s.isLegalBishopMove("a3","c5") && s.isLegalBishopMove("h5","f3") && 
		s.isLegalBishopMove("g5","e3") && s.isLegalBishopMove("c4","a6");
	}
	
	private static boolean testKnightMoves() {
		PositionState s = new PositionState();
		return s.isLegalKnightMove("b1","c3") && s.isLegalKnightMove("e4","f6");
	}
	
	private static boolean testPawnMoves() {
		PositionState s = new PositionState();
		s.makeMove("e2","e4",null);
		s.placePiece(Piece.BLACK_PAWN,"d5");
		return s.isLegalPawnMove(true,"e4","d5") && !s.isLegalPawnMove(true,"e3","e5");
	}
	
	private static boolean testGetInternalCoordsOfSquare() {
		java.awt.Point p = PositionState.getInternalCoordsOfSquare("a8");
		return p.x == 0 && p.y == 7;
	}
	
	private static boolean testGetAllSquaresWithPiece() {
		PositionState s = new PositionState();
		s.makeMove("g7","g5",null);
		String str = java.util.Arrays.toString(s.getAllSquaresWithPiece(Piece.BLACK_PAWN));
		//System.out.println(str);
		return str.equals("[A7, B7, C7, D7, E7, F7, H7, G5]");
	}
	
	private static boolean testGetPiecesThatCanGoToSquare() {
		PositionState s1 = new PositionState();
		s1.makeMove("e2","e4",null);
		s1.makeMove("e7","e5",null);
		s1.makeMove("b1","c3",null);
		s1.makeMove("b8","c6",null);
		PositionState s2 = new PositionState();
		return java.util.Arrays.toString(s1.getPiecesThatCanGoToSquare(Piece.WHITE_KNIGHT,"e2")).equals("[G1, C3]")&&java.util.Arrays.toString(s2.getPiecesThatCanGoToSquare(Piece.WHITE_PAWN,"e4")).equals("[E2]");
	}
	
	private static boolean testParseFromFEN() {
		String fen = "r4r2/ppp1Np1p/8/3BpN2/4P2P/3PPB1k/PPP3P1/R4RK1 b - - 0 26";
		PositionState s = PositionState.parseFromFen(fen);
		s.setPrinter(DefaultPrinter.getSingletonInstance());
		//System.out.println(s.getFEN());
		//System.out.println(s.draw());
		return s.getFEN().equals(fen);
	}
	
	private static boolean testGetPiecesOnFile() {
		PositionState s = new PositionState();
		s.placePiece(Piece.WHITE_PAWN,"E3");
		s.placePiece(Piece.WHITE_PAWN,"E5");
		return java.util.Arrays.toString(s.getPiecesOnFile(Piece.WHITE_PAWN,'E'-65)).equals("[E2, E3, E5]");
		
	}
	
	private static boolean testGetPiecesOnRank() {
		PositionState s = new PositionState();
		return java.util.Arrays.toString(s.getPiecesOnRank(Piece.BLACK_PAWN,6)).equals("[A7, B7, C7, D7, E7, F7, G7, H7]");
	}
	
	private static boolean testIsPiecePinned() {
		PositionState s = new PositionState(PositionState.getEmptyBoard());
		s.placePiece(Piece.BLACK_QUEEN,"A6");
		s.placePiece(Piece.WHITE_QUEEN,"D3");
		s.placePiece(Piece.WHITE_PAWN,"A2");
		s.placePiece(Piece.WHITE_ROOK,"A1");
		s.placePiece(Piece.WHITE_KING,"F1");
		
		return s.isSquarePinned("D3","D4","F1");
	}

	private static boolean testDirectionBetween() {
		PositionState s = new PositionState(PositionState.getEmptyBoard());
		if (s.getDirectionBetween("E4", "E5") != 0x10) {
			return false;
		}
		if (s.getDirectionBetween("E4", "D5") != 0xf) {
			return false;
		}
		if (s.getDirectionBetween("E4", "F5") != 0x11) {
			return false;
		}
		if (s.getDirectionBetween("E4", "E2") != -0x10) {
			return false;
		}
		if (s.getDirectionBetween("E4", "A1") != 0) {
			return false;
		}
		if (s.getDirectionBetween("E4", "D3") != -0x11) {
			return false;
		}
		return true;
	}
	
	private static boolean testGetPieceLocations() {
		PositionState s = new PositionState();
		s.makeMove("e2","e4",null);
		String line = java.util.Arrays.toString(s.getPieceLocations().get(Piece.WHITE_PAWN).toArray(new String[0]));
		return line.equals("[A2, B2, C2, D2, F2, G2, H2, E4]");
	}
	
	private static boolean testGetAdjacentSquares() {
		String line = java.util.Arrays.toString(PositionState.getAdjacentSquares("G3"));
		//System.out.println(line);
		return line.equals("[F3, H3, F2, G2, H2, F4, G4, H4]");
	}
	
	private static boolean testIsCheckmate() {
		//return PositionState.parseFromFen("r4r2/ppp1Np1p/8/3BpN2/4P2P/3PPB1k/PPP3P1/R4RK1 b - - 0 26").isLegalKingMove("H3","H2");
		return PositionState.parseFromFen("r4r2/ppp1Np1p/8/3BpN2/4P2P/3PPB1k/PPP3P1/R4RK1 b - - 0 26").isCheckmate(false) &&
			   PositionState.parseFromFen("1r5r/p1p2kpp/2p1p3/3pPb2/P2P1QP1/R1P2p1n/2P2PbP/5R1K w - - 0 28").isCheckmate(true);
			   //PositionState.parseFromFen("1r6/p2NPpp1/2pPp3/1kNpP2p/P7/1NPPp1PK/3pPp1P/3BnR2 b - a3 0 38").isCheckmate(false);
			   //PositionState.parseFromFen("6k1/1p5p/2b3pP/1r4P1/4pK2/5q2/8/8 w - - 4 47").isCheckmate(true);
	}
	
	private static boolean testGetFEN() {
		PositionState s = new PositionState();
		s.makeMove("e2","e4",null);
		if (!s.getFEN().equals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")) return false;
		s.makeMove("e7","e5",null);
		s.makeMove("g1","f3",null);
		s.makeMove("b8","c6",null);
		s.makeMove("f1","c4",null);
		s.makeMove("g8","f6",null);
		s.castle(true,true);
		s.makeMove("f8","e7",null);
		s.makeMove("d2","d3",null);
		String line = s.getFEN();
		//System.out.println(line);
		return line.equals("r1bqk2r/ppppbppp/2n2n2/4p3/2B1P3/3P1N2/PPP2PPP/RNBQ1RK1 b kq - 0 5");
	}
	
	private static boolean testFilterSquares() {
		PositionState s = new PositionState();
		String line = java.util.Arrays.toString(s.filterSquares(new String[] { "A1","H1","E2","F7","D8","F1" },true));
		//System.out.println(line);
		return line.equals("[A1, H1, E2, F1]");
	}
	
	private static boolean testIsWhitesMove() {
		PositionState s = new PositionState();
		s.makeMove("e2","e4",null);
		return s.isWhitesMove() == false;
	}
	
	private static boolean testIsValidSquare() {
		return PositionState.isValidSquare("E6") && PositionState.isValidSquare("A4") && 
			   !PositionState.isValidSquare("A9") && !PositionState.isValidSquare("I3");
	}
	
	private static boolean testIsAdjacentSquare() {
		return PositionState.isAdjacentSquare("E2","E3") && PositionState.isAdjacentSquare("A4","B5") && !PositionState.isAdjacentSquare("E7","G7");
	}
	
	private static boolean testIsKingAttackingSquare() {
		return PositionState.isKingAttackingSquare("E2","E3") && PositionState.isKingAttackingSquare("E2","F2") && PositionState.isKingAttackingSquare("F5","G4");
	}
	
	private static boolean testIsWhiteColoredSquare() {
		// on this test, I chose to be comprehensive
		String[] white = { "A2","A4","A6","A8","B1","B3","B5","B7","C2","C4","C6","C8","D1","D3","D5","D7","E2","E4","E6","E8","F1","F3","F5","F7","G2","G4","G6","G8","H1","H3","H5","H7" };
		String[] black = { "A1","A3","A5","A7","B2","B4","B6","B8","C1","C3","C5","C7","D2","D4","D6","D8","E1","E3","E5","E7","F2","F4","F6","F8","G1","G3","G5","G7","H2","H4","H6","H8" };
		
		boolean pass = true;
		for(int i=0;i<white.length;i++) {
			if (!PositionState.isWhiteColoredSquare(white[i])) { System.out.println("false positive (white): " + white[i]); pass = false; }
		}
		for(int i=0;i<black.length;i++) {
			if (PositionState.isWhiteColoredSquare(black[i])) { System.out.println("false positive (black): " + black[i]); pass = false; }
		}
		return pass;
	}
}
