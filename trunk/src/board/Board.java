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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import board.exception.IllegalMoveException;
import board.exception.WrongColorToMoveException;

/**
 * The object of the class is to represent a chess board 
 * and parse algebraic notation to make moves - ultimately 
 * this will allow me to get the FEN for the current position.
 * Hopefully this class will be flexible enough to work in Raptor, Morphy, a chess engine, and my bugdb importer.
 * 
 * For now, I need to figure out the best way to represent a board - two dimensional arrays, single FEN string, bitboards, etc. 
 * This would obviously go in the PositionState class rather than this one.
 * @author John
 * @since Wednesday, March 23, 2011
 */
public class Board {
	private List<PositionState> positions;
	
	public Board() {
		positions = new ArrayList<PositionState>();
		positions.add(PositionState.getStartingPositionState());
	}
	
	/** Tries to make <tt>move</tt> with color <tt>white</tt> 
	 * @param white Whether the side to play with is white (true) or black (false).
	 * @param move The move to be played (eg e4,e2e4, etc)
	 * @throws IllegalMoveException This exception is thrown if the move is not legal for this side.
	 * @throws WrongColorToMoveException This exception is thrown if it is the wrong color to move.
	 * */
	public boolean move(boolean white,String move) throws IllegalMoveException, WrongColorToMoveException {
		return parseAlgebraic(white,move);
	}

	private void makeMove(String from,String to,String promotionPiece,String notation,String pretty) {
		PositionState state = getLatestMove().deepCopy();
		boolean whiteMove = !getLatestMove().isWhitesMove();
		if (positions.size() == 1) whiteMove = true;
		state.setWhitesMove(whiteMove);
		state.setNotationInput(notation);
		state.setPrettyNotation(pretty);
		state.makeMove(from, to, promotionPiece);
		// ... do more logic here ...
		//System.out.println(state.getPrettyNotation() + " " + state.getFEN());
		positions.add(state);
	}
	
	/** This method should be used ONLY for the last moves position for validation and what not. 
	 * Should NEVER be modified (eg getLatestMove().castle(), etc) */
	public PositionState getLatestMove() {
		if (positions.size() == 0) return null;
		return positions.get(positions.size()-1);
	}
	
	/** This method will remove ALL POSITIONS from storage except the latest move.<br />
	 * This will mostly be useful for making a smaller footprint in RAM, when needed.<br />
	 * Note that this will disable the rollback() method. */
	public void cleanup() {
		PositionState p = getLatestMove();
		positions.clear();
		((ArrayList<PositionState>)positions).ensureCapacity(1);
		positions.add(p);
		return;
	}
	
	/** Returns if the given notation is valid. */
	public static boolean isValidSAN(String notation) {
		// TODO why doesn't this support "kxq"?
		if (notation.equals("00") || notation.equals("OO") || notation.equals("000") || notation.equals("OOO")) return false;
		notation = notation.toUpperCase().replaceAll("[^A-Z0-8@\\=]","");
		//System.out.print(notation + " ");
		return notation.matches("[A-H][1-8]") || notation.matches("[PNBRQK]?[A-H][1-8]X?[A-H][1-8]") ||
			notation.matches("[PNBRQKA-H]X?[A-H][1-8]") || notation.matches("[QRBNP][A-H1-8]X?[A-H][1-8]") || 
			notation.matches("[P|A-H](X[A-H])?[1-8]=[KQRBN]") || notation.matches("[PNBRQK]@[A-H][1-8]") || 
			notation.matches("[A-H][1-8][A-H][1-8]=[KQRBN]") ||
			notation.equals("00") || notation.equals("OO") || notation.equals("000") || notation.equals("OOO");
	}
	
	/** Makes a deep-copy of this Board instance, including all PositionState objects belonging to this instance.
	 * This may be useful for the <i>copy</i> command on an ICS. */
	public Board deepCopy() {
		Board b = new Board();
		int ct = positions.size();
		b.positions = new ArrayList<PositionState>(ct);
		for(int i=0;i<ct;i++) {
			b.positions.add(positions.get(i).deepCopy());
		}
		
		return b;
	}
	
	
//	private boolean findValidMoves(PositionState s,String[] fromSquares,String toSquare,boolean white) {
//		// ambiguous move; presumably only one of the moves
//		// is actually legal and the others are prevented
//		// by pins
//		String king = white ? s.whiteKing : s.blackKing;
//		int numLegal = 0;
//		for(int i=0;i<fromSquares.length;i++) {
//			if (!s.isSquarePinned(fromSquares[i], toSquare, king)) {
//				fromSquare = fromSquares[i];
//				// hack to pass the correct from square outside this method
//				fromSquares[0] = fromSquare;
//				numLegal++;
//			}
//		}
//
//		if (numLegal != 1) {
//			for(int i=0;i<fromSquares.length;i++) {
//				if (!s.isSquarePinned(fromSquares[i], toSquare, king)) {
//					System.out.println("not pinned " + fromSquares[i] + ", " + toSquare + ", " + king);
//				}
//			}
//			System.err.println(s.getFEN());
//			System.err.println("Ambiguous move ("+input+", "+numLegal+" interpretations).");
//			System.err.println(java.util.Arrays.toString(fromSquares));
//			System.err.println("king " + king);
//			return false;
//		}
//	}
	
	/**
	 * This method validates whether a move is legal or not, as well as takes into consideration whether the piece is absolutely pinned to the king.
	 * @param s The PositionState object of the latest move played to do positional validations.
	 * @param piece The piece that is being moved.
	 * @param fromSquares An array of from squares that can reach the toSquare given that the <tt>piece</tt> object is on each of the squares
	 * @param toSquare The square that this piece is being moved to
	 * @param input The move that is being made
	 * @param white Whether or not it is white's move
	 * @return true|false For whether or not the move is legal (according to the <tt>s</tt> object)
	 * @throws IllegalMoveException If fromSquares has a length of 0, representing the fact that there are no pieces of type <tt>piece</tt> that can move to the toSquare.
	 */
	private boolean validateMove(PositionState s,Piece piece,String[] fromSquares,String toSquare,String input,boolean white) throws IllegalMoveException {		
		//System.err.println(piece.getAbbreviation() + " " + java.util.Arrays.toString(fromSquares) + " " + toSquare + " " + input);
		//System.err.println(s.pieceLocations);
		
		String line = java.util.Arrays.toString(fromSquares) + " " + toSquare + " " + input;
		if (fromSquares.length == 0) {
			IllegalMoveException t = new IllegalMoveException(input,s.getFEN(),"validateMove(): No fromSquares passed in. toSquare="+toSquare+" input="+input+" white="+white,line);
			throw t;
		}
	
		String fromSquare = fromSquares[0].toUpperCase();
		toSquare = toSquare.toUpperCase();
	
		if(fromSquares.length > 1) {
			System.err.println("Ambiguous move ("+input+").");
			System.err.println(java.util.Arrays.toString(fromSquares));
			return false;
		}
		
		String king = white ? s.whiteKing : s.blackKing;
		if (s.isInCheck(white) && !s.moveEliminatesCheck(white, fromSquare, toSquare)) {
			throw new IllegalMoveException(input,s.getFEN(),"King is in check, " + fromSquare + "-" + toSquare + " does not resolve.",null);
		}
		
		boolean retval = false;
		switch(piece) {
			case WHITE_KING: case BLACK_KING: {
				retval = s.isLegalKingMove(fromSquare,toSquare);
				break;
			}
			case WHITE_QUEEN: case BLACK_QUEEN: {
				retval = s.isLegalQueenMove(fromSquare, toSquare) && !s.isSquarePinned(fromSquare, toSquare, king);
				break;
			}
			case WHITE_ROOK: case BLACK_ROOK: {
				retval = s.isLegalRookMove(fromSquare, toSquare) && !s.isSquarePinned(fromSquare, toSquare, king);
				break;
			}
			case WHITE_BISHOP: case BLACK_BISHOP: {
				retval = s.isLegalBishopMove(fromSquare, toSquare) && !s.isSquarePinned(fromSquare, toSquare, king);
				break;
			}
			case WHITE_KNIGHT: case BLACK_KNIGHT: {
				retval = s.isLegalKnightMove(fromSquare, toSquare) && !s.isSquarePinned(fromSquare, toSquare, king);
				break;
			}
			case WHITE_PAWN: { 
				retval = s.isLegalPawnMove(true,fromSquare, toSquare) && !s.isSquarePinned(fromSquare, toSquare, king);
				break;
			}
			case BLACK_PAWN: {
				retval = s.isLegalPawnMove(false,fromSquare, toSquare) && !s.isSquarePinned(fromSquare, toSquare, king);
				break;
			}
		}
		
		if (retval == true) return true;
		
		line = java.util.Arrays.toString(fromSquares) + " " + toSquare + " " + input;
		IllegalMoveException t = new IllegalMoveException(input,s.getFEN(),"Piece cannot legally move from " + fromSquare + " to " + toSquare + ".",line);
		throw t;
	}
	
	/**
	 * Main code that parses algebraic input
	 * 
	 * @param strict This parameter defines whether the SAN standards should be strictly adhered to. If this is false, parsing will be much more lenient.
	 * @param white Whether this is a white move (true) or black move (false).
	 * @param input The move to make.
	 * @see http://en.wikipedia.org/wiki/Algebraic_chess_notation#Notation_for_moves
	 * @see http://en.wikipedia.org/wiki/Algebraic_chess_notation#Long_algebraic_notation
	 * @return If the parsed move is valid.
	 */
	private boolean parseAlgebraic(boolean white,String input) throws IllegalMoveException, WrongColorToMoveException {
		if (getLatestMove() != null) {
			if (!getLatestMove().getPrettyNotation().equals("none")) {
				if ((getLatestMove().isWhitesMove() && white) || (!getLatestMove().isWhitesMove() && !white)) {
					throw new WrongColorToMoveException("Wrong color to move. Input (boolean white,String input): [" + white + " " + input + "]");
				}
			}
		} /*else if (getLatestMove() == null && !white) {
			throw new WrongColorToMoveException("Wrong color to move. (First move) Input (boolean white,String input): [" + white + " " + input + "]");
		}*/
		
		//getPositions().add(getLatestMove().deepCopy());
		
		input = input.replaceAll("[^a-zA-Z0-9@\\=]",""); // replace -,#,+,!, etc etc
		/* NOTE: cases like "a2-a4" do not need to be handled since 
		 * all non-alphanumeric characters are being removed... we can parse this as "a2a4".
		 * Because of this, we also need to watch out for O-O, 0-0, O-O-O, 0-0-0, all ignoring 
		 * case sensitivity - currently these would be parsed as "OO", "00", "OOO", and "000" respectively.
		 * I would imagine it would be easier if it stayed this way due to the MUCH higher possibility 
		 * of something going wrong! 
		 */
		
		/* FICS does not accept something like Qd8xPd5 (this is 7 characters long) as a valid move
		 * 
		 * If you have an "x" on FICS (indicating a capture) and the destination square is empty, 
		 * FICS goes ahead and makes the move anyway. Example: FICS processes Qd8xd5 and executes 
		 * Qd5 even if d5 does not contain an enemy piece.
		 * 
		 * Sometimes a square doesn't even have to be given: QxP is valid if there 
		 * is only one pawn to take - if not, it says ambiguous move.
		 */
		
		
		//System.err.println(input);
		
		int length = input.length();
		boolean isCapture = input.toLowerCase().contains("x");
		String[] fromSquares;
		
		if (length == 2) {
			// O-O, 0-0
			if (input.toUpperCase().equals("OO") || input.equals("00")) {
				// handle castling here
				if (white && getLatestMove().canWhiteCastleKingside()) { 
					PositionState p = getLatestMove().deepCopy();
					boolean isWhitesMove = p.isWhitesMove();
					p.castle(white,true); 
					p.setWhitesMove(!isWhitesMove);
					positions.add(p);
				}
				if (!white && getLatestMove().canBlackCastleKingside()) { 
					PositionState p = getLatestMove().deepCopy();
					boolean isWhitesMove = p.isWhitesMove();
					p.castle(white,true);
					p.setWhitesMove(!isWhitesMove);
					positions.add(p);
				}
			} else {
				// this is obviously a pawn move
				Piece piece = white?Piece.WHITE_PAWN:Piece.BLACK_PAWN;
				fromSquares = getLatestMove().getPiecesThatCanGoToSquare(piece,input.toUpperCase());
				//System.err.println(piece + " " + java.util.Arrays.toString(fromSquares));
				if (validateMove(getLatestMove(),piece,fromSquares,input,input,white)) {	
						makeMove(fromSquares[0],input,null,input,input.toLowerCase());
				}
			}
			return true;
		}
		if (length == 3 && isCapture) {
			// this is a move like "qxp"
			String firstPiece = input.substring(0,1);
			firstPiece = white?firstPiece.toUpperCase():firstPiece.toLowerCase();
			String lastPiece = input.substring(2,3);
			lastPiece = !white?lastPiece.toUpperCase():lastPiece.toLowerCase();
			Piece p1 = Piece.parsePiece(firstPiece);
			Piece p2 = Piece.parsePiece(lastPiece);
			//System.err.println(p1.name() + " " + p2.name());
			//fromSquares = getLatestMove().getAllSquaresWithPiece(p1);
			String[] toSquares = getLatestMove().getAllSquaresWithPiece(p2);
			if (toSquares.length < 1) {
				System.err.println("Illegal move ("+input+").");
				return false;
			}
			for(int i=0;i<toSquares.length;i++) {
				fromSquares = getLatestMove().getPiecesThatCanGoToSquare(p1,toSquares[i]);
				if (fromSquares.length == 0) continue;
				if (validateMove(getLatestMove(), p1, fromSquares, toSquares[i], input, white)) {
					String pretty = p1.getAbbreviation() + "x" + toSquares[i];
					makeMove(fromSquares[0],toSquares[i],null,input,pretty);
					break;
				}
			}
//			for(int i=0;i<toSquares.length;i++) {
//				if (validateMove(getLatestMove(), p1, fromSquares, toSquares[i], input, white)) {
//					makeMove(fromSquares[0],toSquares[i],null,input);
//					break;
//				}
//			}
			return true;
		} else if (length == 3) {
			// O-O-O, 0-0-0
			if (input.toUpperCase().equals("OOO") || input.equals("000")) {
				// handle castling here
				if (white && getLatestMove().canWhiteCastleQueenside()) { 
					PositionState p = getLatestMove().deepCopy();
					boolean isWhitesMove = p.isWhitesMove();
					p.castle(white,false);
					p.setWhitesMove(!isWhitesMove);
					positions.add(p); 
				}
				if (!white && getLatestMove().canBlackCastleQueenside()) { 
					PositionState p = getLatestMove().deepCopy();
					boolean isWhitesMove = p.isWhitesMove();
					p.castle(white,false);
					p.setWhitesMove(!isWhitesMove);
					positions.add(p);
				}
			} else {
				// this is a move by a piece to a square
				char charp = input.toUpperCase().charAt(0);
				String square = input.substring(1);
				
				Piece piece = null;
				if (charp == 'K') { piece = white?Piece.WHITE_KING:Piece.BLACK_KING; }
				else if (charp == 'Q') { piece = white?Piece.WHITE_QUEEN:Piece.BLACK_QUEEN; }
				else if (charp == 'R') { piece = white?Piece.WHITE_ROOK:Piece.BLACK_ROOK; }
				else if (charp == 'B') { piece = white?Piece.WHITE_BISHOP:Piece.BLACK_BISHOP; }
				else if (charp == 'N') { piece = white?Piece.WHITE_KNIGHT:Piece.BLACK_KNIGHT; }
				else if (charp == 'P') { piece = white?Piece.WHITE_PAWN:Piece.BLACK_PAWN; }
				else {
					throw new IllegalArgumentException("bad charp: " + charp);
				}
				
				fromSquares = getLatestMove().getPiecesThatCanGoToSquare(piece,square);
				//System.err.println(java.util.Arrays.toString(fromSquares));
				if (validateMove(getLatestMove(),piece,fromSquares,square,input,white)) {
					String pretty = piece.getAbbreviation().toUpperCase() + "" + square.toLowerCase();
					makeMove(fromSquares[0],square,null,input,pretty);
				}	
			}
			return true;
		}
		
		if (length == 4 && isCapture) {
			// this is a capture (exd5, Bxc7)
			
			// A,C,D,E,F,G,H will be easy to parse - we know they are pawns because none of these are piece abbreviations
			// b/B on the other hand won't be so easy - it could be the b file or a Bishop.
			
			String charp = (""+input.charAt(0)).toUpperCase();
			// we are skipping element 1 because we are presuming that there is an "x" there - we don't need this, just parse it like normal.
			String square = input.substring(2);

			fromSquares = new String[] { };
			Piece p = null;
			if (charp.matches("[A-H]")) {
				p = white?Piece.WHITE_PAWN:Piece.BLACK_PAWN;
				fromSquares = getLatestMove().getPiecesOnFile(p,charp.charAt(0)-65);
				List<String> arr = new ArrayList<String>();
				for(int i=0;i<fromSquares.length;i++) {
					if (getLatestMove().isPawnAttackingSquare(Piece.isWhitePiece(p.getAbbreviation()),fromSquares[i],square)) { 
						arr.add(fromSquares[i]);
					}
				}
				fromSquares = arr.toArray(new String[arr.size()]);
				//System.err.println(java.util.Arrays.toString(fromSquares));
				if (charp.equals("B")) {
					p = white?Piece.WHITE_BISHOP:Piece.BLACK_BISHOP;
					String[] tmp = getLatestMove().getPiecesThatCanGoToSquare(p,square);
					if (fromSquares.length > 0 && tmp.length > 0) {
						if (input.substring(0,1).equals("B")) { 
							fromSquares = tmp;
						}
//						System.err.println("Board.java:227 Ambiguous move ("+input+").");
//						System.err.println(java.util.Arrays.toString(fromSquares) + " || " + java.util.Arrays.toString(tmp));
//						return false;
					}
					if (tmp.length > 0 && fromSquares.length == 0) {
						fromSquares = tmp;
					}
				}
			} else {
				p = Piece.parsePiece(white?charp.toUpperCase():charp.toLowerCase());
				if (p != null) {
					fromSquares = getLatestMove().getPiecesThatCanGoToSquare(p,square);
				}
			}
			

		
			if (validateMove(getLatestMove(), p, fromSquares, square, input, white)) {
				String pretty = "" + square.toLowerCase();
				makeMove(fromSquares[0],square,null,input,pretty);
				return true;
			}
			
			System.err.println("in:" + input);
			return parseAlgebraic(white, charp + square);
		} else if (length == 4) {
			/* this could be one of many things:
			 * - this could be old algebraic notation (d2d4)
			 * - this could be the resolution of an ambiguous move (Qde5, Q7e5)
			 * - this could be a pawn move with promotion (e8=Q)
			 * - this could be a piece drop (N@e4)
			 * ... more likely ... 
			 * */
			
			//System.out.println("in: " + input);
			
			if (input.contains("=")) {
				Piece p = white?Piece.WHITE_PAWN:Piece.BLACK_PAWN;
				String square = input.substring(0,2); // e8
				String promotionPiece = input.substring(3,4);
				List<String> arr = toList(getLatestMove().getPiecesThatCanGoToSquare(p,square));
				for(String s : arr) {
					if (PositionState.isDiagonal(s,square)) arr.remove(s);
				}
				fromSquares = arr.toArray(new String[arr.size()]);
				if (validateMove(getLatestMove(), p, fromSquares, square, input, white)) {
					String pretty = square+"="+promotionPiece;
					makeMove(fromSquares[0], square, promotionPiece, input, pretty);
				}
				return true;
			}
			
			// watch for the special BISHOP / B PAWN case!!
			// b8c6, for example, will match both Old Algebraic and Ambiguous Resolution patterns.
			Pattern PIECE_DROP = Pattern.compile("([PNBRQK])@([A-H][1-8])");
			Pattern OLD_ALGEBRAIC = Pattern.compile("([A-H][1-8])([A-H][1-8])");
			Pattern AMBIGUOUS_RESOLUTION = Pattern.compile("([QRBNP])([A-H1-8])([A-H][1-8])");
			
//			if (input.contains("b5d4")) {
//				System.out.println("in: " + input);
//				System.out.println(input.toUpperCase().matches(PIECE_DROP.pattern()));
//				System.out.println(input.toUpperCase().matches(AMBIGUOUS_RESOLUTION.pattern()));
//				System.out.println(input.toUpperCase().matches(OLD_ALGEBRAIC.pattern()));
//				System.exit(0);
//			}
			
			Matcher m1 = PIECE_DROP.matcher(input.toUpperCase());
			//System.out.println("m1: " + m1.matches());
			if (m1.matches()) {
				String square = m1.group(2);
				if (getLatestMove().getPiece(square) == Piece.EMPTY) {
					Piece p = Piece.parsePiece(white?m1.group(1).toUpperCase():m1.group(1).toLowerCase());
					PositionState ps = getLatestMove().deepCopy();
					ps.placePiece(p,square);
					ps.setWhitesMove(white);
					ps.setNotationInput(input);
					ps.setPrettyNotation(p.getAbbreviation()+"@"+square);
					ps.setVerboseNotation(p.getAbbreviation().toUpperCase()+"/@@-"+square);
					positions.add(ps);
					return true;
				}
			}
			
			m1 = OLD_ALGEBRAIC.matcher(input.toUpperCase());
			if (m1.matches()) {
				String from = m1.group(1);
				String to = m1.group(2);
				//System.err.println(from + " " + to);
				Piece p = getLatestMove().getPiece(from);
				
				if (p == Piece.WHITE_KING && from.equals("E1") && to.equals("G1") && getLatestMove().canWhiteCastleKingside()) {
					return parseAlgebraic(true,"O-O");
				}
				if (p == Piece.WHITE_KING && from.equals("E1") && to.equals("C1") && getLatestMove().canWhiteCastleQueenside()) {
					return parseAlgebraic(true,"O-O-O");
				}
				if (p == Piece.BLACK_KING && from.equals("E8") && to.equals("G8") && getLatestMove().canBlackCastleKingside()) {
					return parseAlgebraic(false,"O-O");
				}
				if (p == Piece.BLACK_KING && from.equals("E8") && to.equals("C8") && getLatestMove().canBlackCastleQueenside()) {
					return parseAlgebraic(false,"O-O-O");
				}
				
				if (validateMove(getLatestMove(),p,new String[] { from } , to, input, white)) {
					String pretty = p.getAbbreviation().toUpperCase()+""+to.toLowerCase();
					if (p == Piece.WHITE_PAWN || p == Piece.BLACK_PAWN) { pretty = to.toLowerCase(); }
					makeMove(from, to, null, input, pretty);
					return true;
				}
			}
			
			m1 = AMBIGUOUS_RESOLUTION.matcher(input.toUpperCase());
			if (m1.matches()) {
				//System.out.println("groupCount = " + m1.groupCount());
				String piece = m1.group(1);
				piece = white?piece.toUpperCase():piece.toLowerCase();
				String rankOrFile = m1.group(2).toUpperCase();
				String square = m1.group(3).toUpperCase();
				
				if (rankOrFile.matches("[1-8]")) {
					// this is a rank
					Piece myPiece = Piece.parsePiece(piece);
					fromSquares = getLatestMove().getPiecesOnRank(myPiece,Integer.parseInt(rankOrFile)-1);					
					List<String> tmp = toList(getLatestMove().getPiecesThatCanGoToSquare(myPiece,square));
					List<String> arr = new ArrayList<String>();
					for(int i=0;i<fromSquares.length;i++) {
						if (tmp.contains(fromSquares[i])) { 
							arr.add(fromSquares[i]);
						}
					}
					fromSquares = arr.toArray(new String[arr.size()]);
					
					if (validateMove(getLatestMove(), myPiece, fromSquares, square, input, white)) {
						String pretty = myPiece.getAbbreviation().toUpperCase()+""+square.toLowerCase();
						makeMove(fromSquares[0],square,null,input,pretty);
						return true;
					}
				} else {
					// this is a file
					
					String promotionPiece = null;
					//System.out.println("groupCount = " + m1.groupCount());
					if (input.contains("=") && m1.groupCount() == 4) { promotionPiece = m1.group(4);  }
					
					Piece myPiece = Piece.parsePiece(piece);
					int file = rankOrFile.toUpperCase().charAt(0)-65;
					
					// lets get the list of squares with pieces on the given file
					fromSquares = getLatestMove().getPiecesOnFile(myPiece,file);
					
					// lets get the list of squares of pieces that can go to square
					List<String> tmp = toList(getLatestMove().getPiecesThatCanGoToSquare(myPiece,square));
					
					// find which squares can be found in both lists
					List<String> arr = new ArrayList<String>();
					for(int i=0;i<fromSquares.length;i++) {
						if (tmp.contains(fromSquares[i])) { 
							arr.add(fromSquares[i]);
						}
					}
					fromSquares = arr.toArray(new String[arr.size()]);
					
					if (validateMove(getLatestMove(), myPiece, fromSquares, square, input, white)) {
						String pretty = myPiece.getAbbreviation().toUpperCase()+""+rankOrFile.toLowerCase()+square.toLowerCase();
						makeMove(fromSquares[0],square,promotionPiece,input,pretty);
						return true;
					}
				}
			}
			
			//throw new IllegalArgumentException("not parsed: " + input);
			
			//return true;
		}
		if (length == 5 && isCapture) {
			 // this is a disambiguating capture (Ngxf3, N5xf3,NxNf3)
			
			// we already wrote the parser for Ngf3, so just remove the capture sign!
			return parseAlgebraic(white, input.replaceAll("x","").replaceAll("X",""));
		} else if (length == 5) {
			// this could be old algebraic notation with the piece name in front (Qd8d5)
			// since there can only be one piece on one square, let's just treat it as d8d5 - but first we must validate that the piece they told us is actually on the square!
			String fromSquare = input.substring(1,3);
			String toSquare = input.substring(3);
			
			String piece = ""+input.charAt(0);
			piece = white?piece.toUpperCase():piece.toLowerCase();
			//System.out.println(getLatestMove().getPiece(square).name() + " " + Piece.parsePiece(piece).name());
			if (getLatestMove().getPiece(fromSquare) != Piece.parsePiece(piece)) {
				throw new IllegalMoveException(input,getLatestMove().getFEN(),
						"Illegal move (" + input + ").","Wrong piece on " + fromSquare +  ", expected " + piece + ", actual " + getLatestMove().getPiece(fromSquare).getAbbreviation() + ".");
			}
			
			Piece p = getLatestMove().getPiece(fromSquare);
			if (validateMove(getLatestMove(),p,new String[] { fromSquare } , toSquare, input, white)) {
				String pretty = "";
				pretty = p.getAbbreviation().toUpperCase()+""+toSquare;
				makeMove(fromSquare, toSquare, null, input, pretty);
				return true;
			}
			
//			square = square+input.substring(3);
//			System.err.println("\""+square+"\"");
//			System.exit(0);
//			return parseAlgebraic(white, square);
		}
		if (length == 6 && isCapture) {
			// this must be a capture using old algebraic notation with the piece name in front (Qd8xd5,NdxNf3,Q1xNd4) OR a pawn capture with promotion (fxg8=Q)
			
			if (input.toLowerCase().indexOf("x") == 1) {
				// fxg8=Q
				boolean isPromotion = input.contains("=");
				if (isPromotion) {
					Piece myPiece = white?Piece.WHITE_PAWN:Piece.BLACK_PAWN;
					String toSquare = input.substring(2,4);
					String promotionPiece = input.substring(5,6);
//					System.err.println("sq: " + toSquare);
//					System.err.println(promotionPiece);
					fromSquares = getLatestMove().getPiecesThatCanGoToSquare(myPiece,toSquare);
					List<String> tmp = toList(fromSquares);
					for(int i=0;i<tmp.size();i++) {
						if (!tmp.get(i).toLowerCase().startsWith(input.substring(0,1).toLowerCase())) { tmp.remove(i); }
					}
					fromSquares = tmp.toArray(new String[tmp.size()]);
					
					if (validateMove(getLatestMove(), myPiece, fromSquares, toSquare, input, white)) {
						String pretty = myPiece.getAbbreviation().toUpperCase()+""+toSquare;
						makeMove(fromSquares[0],toSquare,promotionPiece,input,pretty);
						return true;
					}
					return false;
				}
			}
			
			if (input.toLowerCase().indexOf("x") == 2) {
				// NdxNf3, Q1xNd4, 
				
				String piece = input.substring(0,1);
				String fromRankOrFile = input.substring(1,2).toUpperCase();
				String pieceToCapture = input.substring(3,4);
				pieceToCapture = !white?pieceToCapture.toUpperCase():pieceToCapture.toLowerCase();
				String square = input.substring(4,6).toUpperCase();
				//System.out.println(piece + " " + fromRankOrFile + " " + pieceToCapture + " " + square);
				
				//System.out.println(getLatestMove().getPiece(square).name() + " " + Piece.parsePiece(pieceToCapture).name());
				if (getLatestMove().getPiece(square) != Piece.parsePiece(pieceToCapture)) {
					System.err.println("Illegal move ("+input+").");
					return false;
				}
				
				Piece myPiece = Piece.parsePiece(piece);
				
				// this is a rank
				fromSquares = new String[] { };
				if (fromRankOrFile.matches("[1-8]")) { fromSquares = getLatestMove().getPiecesOnRank(myPiece,Integer.parseInt(fromRankOrFile)-1); }
				if (fromRankOrFile.matches("[A-H]")) { fromSquares = getLatestMove().getPiecesOnFile(myPiece,(fromRankOrFile.toUpperCase().charAt(0)-65)); }
				if (validateMove(getLatestMove(), myPiece, fromSquares, square, input, white)) {
					String pretty = myPiece.getAbbreviation().toUpperCase()+""+square;
					makeMove(fromSquares[0],square,null,input,pretty);
					return true;
				}
			}
			if (input.toLowerCase().indexOf("x") == 3) {
				// Nb6xd5
				
				String strPiece = input.substring(0,1);
				strPiece = white?strPiece.toUpperCase():strPiece.toLowerCase();
				String fromSquare = input.substring(1,3);
				String toSquare = input.substring(4,6);
			
				if (getLatestMove().getPiece(fromSquare) != Piece.parsePiece(strPiece)) {
					System.err.println("Illegal move ("+input+").");
					return false;
				}
				return parseAlgebraic(white,strPiece+fromSquare+toSquare);
			}
			
			return true;
		} else if (length == 6) {
			// c7b8=Q
			Pattern p = Pattern.compile("([A-H][1-8])([A-H][1-8])=([KQRBN])");
			Matcher m = p.matcher(input.toUpperCase());
			if (m.matches()) {
				String from = m.group(1);
				String square = m.group(2);
				String promotionPiece = m.group(3);
				Piece myPiece = getLatestMove().getPiece(from);
				if (validateMove(getLatestMove(), myPiece, new String[] { from }, square, input, white)) {
					String pretty = (from.charAt(0)+"").toLowerCase()+(PositionState.isDiagonal(from,square)?"x":"")+square.toLowerCase()+"="+promotionPiece;
					makeMove(from,square,promotionPiece,input,pretty);
					return true;
				}
			}
		}
		
		return false;
	}
	
	/** Rolls back the specified number of half moves.<br />
	 * Note that this method <b>permenantly deletes</b> the moves 
	 * and there is no way to retrieve them.
	 * @param count Number of half moves to go back */
	public void rollback(int count) {
		int size = getPositions().size();
		int to = size-count;
		while(size > to) {
			getPositions().remove(size-1);
			size = getPositions().size();
		}
		Runtime.getRuntime().gc();
	}
	
	protected List<String> toList(String[] arr) {
		List<String> list = new ArrayList<String>();
		for(String s : arr) {
			list.add(s);
		}
		return list;
	}

	/** Returns a list of PositionState objects as stored by this Board instance.<br />
	 * @see {@link Board#rollback(int) rollback()}
	 * @see {@link Board#cleanup() cleanup()}
	 * */
	public List<PositionState> getPositions() {
		return positions;
	}
}
