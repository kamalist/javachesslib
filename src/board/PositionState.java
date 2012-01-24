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

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import board.printer.BoardPrinter;
import board.printer.Style12Printer;

/** This is the main class that represents a position and contains metadata (state) regarding it. 
 * The board representation is currently stored as a Piece[][] (2d array). */
public class PositionState {
	private BoardPrinter printer;
	private Piece[][] board = new Piece[8][8];
	private String notationInput;
	private String prettyNotation;
	private String verboseNotation;
	
	/* ALL of the following variables are parsed out 
	 * from the position - none are provided by the user */
	private String castlingRights = "KQkq";
	private boolean isWhitesMove = true;
	private String enpassantSquare = "-";
	/** num half moves since last pawn move */
	private int numHalfMoves;
	/** num of full moves */
	private int numFullMoves;
	private boolean isPawnPromotion = false;
	private Piece piecePromotedTo = null;
	protected HashMap<Piece,List<String>> pieceLocations;
	private List<String> promotions;
	private int isDoublePawnPush = -1;

	public String whiteKing = null;
	public String blackKing = null;
	
	/** Method to deep copy a Piece[][] into another. */
	private static Piece[][] deepCopyBoard(Piece[][] oldBoard) {
		Piece[][] board = new Piece[8][8];
		
		for(int i=0;i<oldBoard.length;i++) {
			for(int j=0;j<oldBoard[i].length;j++) {
				board[i][j] = oldBoard[i][j];
			}
		}
		
		return board;
	}
	
	/** Method to deep copy an instance of HashMap<Piece,List<String>>. 
	 * 
	 * @param oldMap
	 * @since Tuesday, November 08, 2011
	 * @author John */
	private static HashMap<Piece,List<String>> deepCopyHashMap(HashMap<Piece,List<String>> oldMap) {
		Piece[] arr = oldMap.keySet().toArray(new Piece[oldMap.keySet().size()]);
		HashMap<Piece,List<String>> map = new HashMap<Piece,List<String>>(arr.length);
		for(int i=0;i<arr.length;i++) {
			List<String> oldlist = oldMap.get(arr[i]);
			int ct = oldlist.size();
			List<String> newlist = new ArrayList<String>(ct);
			for(int j=0;j<ct;j++) {
				newlist.add(oldlist.get(j));
			}
			map.put(arr[i],newlist);
		}
		
		return map;
	}
	
	/** Scans through the board to find which pieces are on what squares. for use with an optimized version of getAllSquaresWithPiece().*/
	public void addPiecesToHashmap() {
		for(int i=0;i<board.length;i++) {
			for(int j=0;j<board[i].length;j++) {
				Piece key = board[i][j];
				if (!pieceLocations.containsKey(key)) {
					pieceLocations.put(key,new ArrayList<String>());
				}
				pieceLocations.get(key).add(convertFromPointToSquare(new Point(j,i)));
			}
		}
	}
	
	public void clearPiecesHashMap() {
		pieceLocations.clear();
	}
	
	/*private void deepCopyHashMap(HashMap<Piece,List<String>> map) {
		HashMap<Piece,List<String>> newMap = new HashMap<Piece,List<String>>();
		java.util.Set<Piece> keys = map.keySet();
		java.util.Collection<List<String>> values = map.values();
		for(Iterable<List<String>> val : values.iterator()) {
			//newMap.put(keys[i],);
		}
	}*/
	
	/** Deep copies this PositionState instance. */
	protected PositionState deepCopy() {
		PositionState s = new PositionState();
		s.setBoard(deepCopyBoard(board));
		s.setCastlingRights(new String(castlingRights));
		s.isWhitesMove = isWhitesMove;
		s.enpassantSquare = new String(enpassantSquare);
		s.numHalfMoves = numHalfMoves;
		s.numFullMoves = numFullMoves;
		s.isPawnPromotion = isPawnPromotion;
		s.piecePromotedTo = piecePromotedTo;
		s.whiteKing = new String(whiteKing);
		s.blackKing = new String(blackKing);
		s.pieceLocations = deepCopyHashMap(pieceLocations);
		s.isDoublePawnPush = isDoublePawnPush;
		return s;
	}
	
	/** Default constructor. Alias for <tt>this(getStartingPosition());</tt> */
	public PositionState() {
		this(getStartingPosition());
	}
	
	/** Contructor that takes in a board representation (Piece[][]) and sets some additional variables. */
	public PositionState(Piece[][] board) {
		this.board = board;
		pieceLocations = new HashMap<Piece,List<String>>();
		promotions = new ArrayList<String>();
		addPiecesToHashmap();
		// in reality, we don't know the castling rights...
		// we have a hit or miss chance of finding out by finding king and rook squares - not 100% accurate.
		castlingRights = "KQkq";
		// this approach presumes there is only one king! Variants such as suicide may not work well here...
		whiteKing = pieceLocations.get(Piece.WHITE_KING).get(0);
		blackKing = pieceLocations.get(Piece.BLACK_KING).get(0);
		isWhitesMove = false;
	}
	
	/** Concats all strings in <tt>arr</tt> together using the parameter <tt>glue</tt>. 
	 * This parameter should be an empty string ("") or NULL if no glue is wanted.
	 * @param arr
	 * @param glue 
	 * @since
	 * @author John */
	protected static String implodeArray(String[] arr,String glue) {
		StringBuilder b = new StringBuilder();
		for(String s : arr) {
			b.append(s);
			if (glue != null) b.append(glue);
		}
		return b.toString();
	}
	
	protected static String rankToStyle12FromFen(String fenRank) {
		String[] arr = fenRank.split("");
		for(int i=0;i<arr.length;i++) {
			String s = arr[i];
			if (s.matches("[1-8]")) {
				int ints = Integer.parseInt(s);
				StringBuilder b = new StringBuilder(8);
				for(int j=0;j<ints;j++) {
					b.append("-");
				}
				arr[i] = b.toString();
			}
		}
		return implodeArray(arr,"");
	}
	
	/** Not yet implemented */
	protected static PositionState parseFromFen(String str) {
		PositionState s = new PositionState();
		s.clearPiecesHashMap();
		Piece[][] board = getEmptyBoard();
		//str = str.toUpperCase();
		String pos = str.substring(0,str.indexOf(" "));
		// System.out.println(str);
		
		
		// r4r2/ppp1Np1p/8/3BpN2/4P2P/3PPB1k/PPP3P1/R4RK1 b - - 0 26
		
		String[] ranks = pos.split("/");//new StringBuilder(pos).reverse().toString().split("/");
		//for(int i=ranks.length-1;i>=0;i--) {
		for(int i=0;i<ranks.length;i++) {
			ranks[i] = rankToStyle12FromFen(ranks[i]);
			for(int j=0;j<ranks[i].length();j++) {
				String chr = ""+ranks[i].charAt(j);
				if (chr.equals("-")) {
					board[7-i][j] = Piece.EMPTY;
				} else {
					Piece p = Piece.parsePiece(chr);
					board[7-i][j] = p;
					if (p == Piece.WHITE_KING) {
						s.whiteKing = PositionState.convertFromPointToSquare(new Point(j,7-i));
					} else if (p == Piece.BLACK_KING) {
						s.blackKing = PositionState.convertFromPointToSquare(new Point(j,7-i));
						//System.out.println(s.blackKing);
					}
				}
			}
			
			/*int ptr = 0;
			for(int j=0;j<ranks[i].length();j++) {
				String c = ""+ranks[i].charAt(j);
				if (c.matches("[1-8]")) {
					int intc = Integer.parseInt(c);
					for(int k=0;k<intc;k++) {
						ptr = j+k;
						board[i][ptr] = Piece.EMPTY;
					}
				} else {
					Piece p = Piece.parsePiece(c);
					if (p == Piece.WHITE_KING) {
						s.whiteKing = "" + "ABCDEFGH".charAt(8-i) + "12345678".charAt(8-j);
					}
					else if (p == Piece.BLACK_KING) {
						s.blackKing = "" + "ABCDEFGH".charAt(8-i) + "12345678".charAt(8-j);
					}
					board[i][ptr++] = p;
				}
			}*/
		}
		s.board = board;
		
		String metadata = str.substring(str.indexOf(" ")).trim();
		//System.out.println("\"" + metadata + "\"");
		String[] arr = metadata.split(" ");
		s.isWhitesMove = arr[0].equalsIgnoreCase("w");
		s.castlingRights = arr[1];
		s.enpassantSquare = arr[2];
		s.numHalfMoves = Integer.parseInt(arr[3]);
		s.numFullMoves = Integer.parseInt(arr[4])*2;
		s.addPiecesToHashmap();
		return s;
	}
	
	/** Returns the board strength of white; that is, the point values for all white pieces on the board. */
	public int getWhiteBoardStrength() {
		return getColorBoardStrength(true);
	}
	
	/** Returns the board strength of black; that is, the point values for all white pieces on the board. */
	public int getBlackBoardStrength() {
		return getColorBoardStrength(false);
	}
	
	protected int getColorBoardStrength(boolean white) {
		int strength = 0;
		Piece[] arr;
		if (white) {
			arr = new Piece[] { Piece.WHITE_PAWN,Piece.WHITE_KNIGHT,Piece.WHITE_BISHOP, Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_QUEEN };
		} else {
			arr = new Piece[] { Piece.BLACK_PAWN,Piece.BLACK_KNIGHT,Piece.BLACK_BISHOP, Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_QUEEN };
		}
		int[] strengthArr = new int[] { 1,3,3,5,0,9 };
		
		for(int i=0;i<arr.length;i++) {
			Piece key = arr[i];
			if (pieceLocations.containsKey(key)) {
				strength += pieceLocations.get(key).size()*strengthArr[i];
			}
		}
		return strength;
	}
	
	/** Returns if this square is one of the square names on the chess board. */
	public static boolean isValidSquare(String square) {
		return isValidSquare(squareTo0x88(square));
	}
	
	/** Returns if this square (not the piece on it!) is colored white (true) or black (false). 
	 * @param square Square to find the color for */
	public static boolean isWhiteColoredSquare(String square) {
		int file = square.toUpperCase().charAt(0) - 65;
		int rank = Integer.parseInt(""+square.charAt(1));
		
		//return (file%2==0&&rank%2==0) || (file%2==1&&rank%2==1);
		return file%2 == rank%2;
	}
	
	/** This takes in a 0x88 number! */
	private static boolean isValidSquare(int sq) {
		return (sq & 0x88) == 0;
	}
	
	/** Returns adjacent squares in any direction.<br />
	 * This method DOES do bounds checking. */
	public static String[] getAdjacentSquares(String square) {
		Point p = getInternalCoordsOfSquare(square);
		List<String> list = new ArrayList<String>(9);
		//if (p.x == 0 || p.y == 0 || p.x > 7 || p.y > 7) { /* some squares are eliminated */ }
		
		int rank = p.x;
		int file = p.y;
		
		if (rank > 0) list.add(convertFromPointToSquare(new Point(rank-1,file)));
		if (rank < 7) list.add(convertFromPointToSquare(new Point(rank+1,file)));
		if (rank > 0 && file > 0) list.add(convertFromPointToSquare(new Point(rank-1,file-1)));
		if (file > 0) list.add(convertFromPointToSquare(new Point(rank,file-1)));
		if (rank < 7 && file > 0) list.add(convertFromPointToSquare(new Point(rank+1,file-1)));
		if (rank > 0 && file < 7) list.add(convertFromPointToSquare(new Point(rank-1,file+1)));
		if (file < 7) list.add(convertFromPointToSquare(new Point(rank,file+1)));
		if (rank < 7 && file < 7) list.add(convertFromPointToSquare(new Point(rank+1,file+1)));
		
		return list.toArray(new String[list.size()]);
	}
	
	/*public String[] generatePsuedoLegalRookMoves(String startingSquare) {
		int sq = squareTo0x88(startingSquare);
		int mysq = sq;
		while((mysq & 0x88) == 0) {
			mysq = mysq&8;
			String str = squareFrom0x88(mysq);
			System.out.println(str);
		}
		return null;
	}*/
	
	public String[] generatePsuedoLegalMoves(boolean white) {
		/* DEVELOPER NOTE: 
		 * 
		 * While moves generated by this method aren't necessarily legal 
		 * for the current board condition, the squares need to be on the board.
		 * I expect all helper methods to do the bounds checking, NOT this method.
		 * */
		
		List<String> fromSquares = new ArrayList<String>();
		
		String[] knights = getAllSquaresWithPiece(white?Piece.WHITE_KNIGHT:Piece.BLACK_KNIGHT);
		if (knights != null) {
			for(String s : knights) {
				String[] arr = generatePsuedoLegalKnightMoves(s);
				//System.out.println(s + " " + java.util.Arrays.deepToString(arr));
				for(int i=0;i<arr.length;i++) {
					fromSquares.add((s + arr[i]).toLowerCase());
				}
			}
		}
		
		String[] bishops = getAllSquaresWithPiece(white?Piece.WHITE_BISHOP:Piece.BLACK_BISHOP);
		if (bishops != null) {
			for(String s : bishops) {
				String[] arr = generatePsuedoLegalBishopMoves(s);
				//System.out.println(s + " " + java.util.Arrays.deepToString(arr));
				for(int i=0;i<arr.length;i++) {
					fromSquares.add((s + arr[i]).toLowerCase());
				}
			}
		}
		
		String[] rooks = getAllSquaresWithPiece(white?Piece.WHITE_ROOK:Piece.BLACK_ROOK);
		if (rooks != null) {
			for(String s : rooks) {
				String[] arr = generatePsuedoLegalRookMoves(s);
				//System.out.println(s + " " + java.util.Arrays.deepToString(arr));
				for(int i=0;i<arr.length;i++) {
					fromSquares.add((s + arr[i]).toLowerCase());
				}
			}
		}
		
		String[] queens = getAllSquaresWithPiece(white?Piece.WHITE_QUEEN:Piece.BLACK_QUEEN);
		if (queens != null) {
			for(String s : queens) {
				String[] arr = generatePsuedoLegalQueenMoves(s);
				//System.out.println(s + " " + java.util.Arrays.deepToString(arr));
				for(int i=0;i<arr.length;i++) {
					fromSquares.add((s + arr[i]).toLowerCase());
				}
			}
		}
		
		String[] kings = getAllSquaresWithPiece(white?Piece.WHITE_KING:Piece.BLACK_KING);
		for(String s : kings) {
			String[] arr = generatePsuedoLegalKingMoves(s);
			//System.out.println(s + " " + java.util.Arrays.deepToString(arr));
			for(int i=0;i<arr.length;i++) {
				fromSquares.add((s + arr[i]).toLowerCase());
			}
		}
		
		// we know a few properties about bishops by an 0x88 board: 0x11, 0xF
		// left-to-right diagonal always goes up or down by ...
		// right-to-left diagonal always goes up or down by ... 
		
		// we know a few properties about rooks by an 0x88 board:
		// files always go up or down by 10 (0x10)
		// ranks always go left or right by 1 (0-7) (0x1) 
		
		// a queen can move like both a bishop and a rook
		// 
		
		// a king can move only one square in any direction
		// use the static method getAdjacentSquares() for this
		
		// pawns can only move one forward if it has moved before
		// pawns can move two forward if it has never moved before
		return fromSquares.toArray(new String[fromSquares.size()]);
	}
	
	/** Generates psuedo-legal moves as if a knight was on <tt>startingSquare</tt>. */
	private static String[] generatePsuedoLegalKnightMoves(String startingSquare) {
		List<String> list = new ArrayList<String>();
		Point fromPoint = getInternalCoordsOfSquare(startingSquare);
		
		int[][] ints = new int[][] { {+2,-1},{+2,+1},{+1,-2},{+1,+2},{-2,-1},{-2,+1},{-1,-2},{-1,+2} };
		for(int i=0;i<ints.length;i++) {
			Point p = new Point(fromPoint.x+ints[i][0],fromPoint.y+ints[i][1]);
			// check to see if this square is out of bounds
			if (p.x < 0 || p.y < 0 || p.x > 7 || p.y > 7) continue;
			String str = convertFromPointToSquare(p);
			list.add(str);
		}
		
		return list.toArray(new String[list.size()]);
	}
	
	/** Generates psuedo-legal moves as if a bishop was on <tt>startingSquare</tt>. */
	private static String[] generatePsuedoLegalBishopMoves(String startingSquare) {
		List<String> list = new ArrayList<String>();
		
		int sq = squareTo0x88(startingSquare);
		
		/*int[] arr = { +0x11,-0x11,+0xF,-0xF };
		int mysq = sq;
		while (((mysq) & 0x88) == 0) {
			list.add(squareFrom0x88(mysq));
		}*/
		
		//int sq1 = Integer.parseInt(""+sq,16);
		//System.out.println(sq + " " + sq1);
		//System.out.println(Integer.toHexString(sq));
		//new Integer(5).
		
		int mysq = sq;
		while(mysq % 0x10 != 0x7) {
			mysq += 0x11;
			if ((mysq & 0x88) > 0) break;
			list.add(squareFrom0x88(mysq));
		}
		
		mysq = sq;
		while (((mysq-0x11) & 0x88) == 0) {
			mysq -= 0x11;
			list.add(squareFrom0x88(mysq));
		}
		
		mysq = sq;
		while(mysq % 0x10 != 0) {
			mysq += 0xF;
			if ((mysq & 0x88) > 0) break;
			list.add(squareFrom0x88(mysq));
		}
		
		mysq = sq;
		while (((mysq-0xF) & 0x88) == 0) {
			mysq -= 0xF;
			if ((mysq & 0x88) > 0) break;
			list.add(squareFrom0x88(mysq));
		}
		
		
		return list.toArray(new String[list.size()]);
	}
	
	/** Generates psuedo-legal moves as if a rook was on <tt>startingSquare</tt>. */
	private static String[] generatePsuedoLegalRookMoves(String startingSquare) {
		List<String> list = new ArrayList<String>();
		
		final int sq = squareTo0x88(startingSquare);
		
		int mysq = sq;
		int[] arr = { -0x1,+0x1,-0x10,+0x10 };
		for(int i=0;i<arr.length;i++) {
			mysq = sq;
			while((mysq+arr[i] & 0x88) == 0) {
				mysq += arr[i];
				list.add(squareFrom0x88(mysq));
			}
		}
		
		return list.toArray(new String[list.size()]);
	}
	
	/** Generates psuedo-legal moves as if a queen was on <tt>startingSquare</tt>. */
	private static String[] generatePsuedoLegalQueenMoves(String startingSquare) {
		String[] bishop = generatePsuedoLegalBishopMoves(startingSquare);
		String[] rook = generatePsuedoLegalRookMoves(startingSquare);
		
		ArrayList<String> list = new ArrayList<String>();
		list.ensureCapacity(bishop.length+rook.length);
		for(int i=0;i<bishop.length;i++) { list.add(bishop[i]); }
		for(int i=0;i<rook.length;i++) { if (!list.contains(rook[i])) list.add(rook[i]); }
		
		String[] arr = list.toArray(new String[list.size()]);
		java.util.Arrays.sort(arr);
		return arr;
	}
	
	/** Generates psuedo-legal moves as if a king was on <tt>startingSquare</tt>. */
	private static String[] generatePsuedoLegalKingMoves(String startingSquare) {
		return getAdjacentSquares(startingSquare);
	}
	
	public String[] generateLegalKingMoves(boolean white) {
		List<String> list = new ArrayList<String>();
		
		String[] squares = getAllSquaresWithPiece(white?Piece.WHITE_KING:Piece.BLACK_KING);
		if (squares == null) return null;
		for(String square : squares) {
			String[] arr = generatePsuedoLegalKingMoves(square);
			for(String adjSquare : arr) {
				Piece p = getPiece(adjSquare);
				if (p != Piece.EMPTY && p.isWhite() == white) continue;
				String[] attackers = filterSquares(getAttackers(adjSquare),!white);
				if (attackers.length == 0) {
					list.add((square+adjSquare).toLowerCase());
				}
			}
		}
		
		return list.toArray(new String[list.size()]);
	}
	
	public String[] generateLegalKnightMoves(boolean white) {
		List<String> list = new ArrayList<String>();
		
		String[] squares = getAllSquaresWithPiece(white?Piece.WHITE_KNIGHT:Piece.BLACK_KNIGHT);
		if (squares == null) return null;
		for(String fromSquare : squares) {
			String[] arr = generatePsuedoLegalKnightMoves(fromSquare);
			for(String sq : arr) {
				Piece p = getPiece(sq);
				//if (p == Piece.EMPTY) continue;
				if (p != Piece.EMPTY && p.isWhite() == white) continue;
				if (isSquarePinned(fromSquare, sq, white?whiteKing:blackKing)) continue;
				
				boolean check = isInCheck(white);
				boolean moveEliminatesCheck = moveEliminatesCheck(white, fromSquare, sq);
				if (!check || (check && moveEliminatesCheck)) {
					list.add((fromSquare+sq).toLowerCase());
				}
			}
		}
		
		return list.toArray(new String[list.size()]);
	}
	
	public String[] generateLegalBishopMoves(boolean white) {
		List<String> list = new ArrayList<String>();
		
		String[] squares = getAllSquaresWithPiece(white?Piece.WHITE_BISHOP:Piece.BLACK_BISHOP);
		if (squares == null) return null;
		for(String fromSquare : squares) {
			String[] arr = generatePsuedoLegalBishopMoves(fromSquare);
			int lastDirectionBlocked = -1;
			for(String sq : arr) {
				if (lastDirectionBlocked == getDirectionBetween(fromSquare, sq)) continue;
				Piece p = getPiece(sq);
				if (p != Piece.EMPTY) {
					lastDirectionBlocked = getDirectionBetween(fromSquare, sq);
					if (p.isWhite() == white) continue;
				}
				if (isSquarePinned(fromSquare, sq, white?whiteKing:blackKing)) continue;
				
				boolean check = isInCheck(white);
				if (!check || (check && moveEliminatesCheck(white, fromSquare, sq))) {
					list.add((fromSquare + sq).toLowerCase());
				}
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	public String[] generateLegalPawnMoves(boolean white) {
		List<String> list = new ArrayList<String>();
		
		String[] squares = getAllSquaresWithPiece(white?Piece.WHITE_PAWN:Piece.BLACK_PAWN);
		String kingSquare = white?whiteKing:blackKing;
		if (squares == null) return null;
		for(String fromSquare : squares) {
			
			String file = "" + fromSquare.toUpperCase().charAt(0);
			int rank = Integer.parseInt(""+fromSquare.charAt(1));
			
			// can this pawn move at all?
			if (isSquarePinned(fromSquare, file+(white?(rank+1):(rank-1)), kingSquare)) continue;
			
			if (white) {
				boolean check = isInCheck(white);
				if (rank == 2) {
					// If there are no pieces on the two squares in front of this pawn, we can move two squares
					String newSquare = file + (rank+2);
					if (getPiece(file + (rank+1)) == Piece.EMPTY && 
						getPiece(newSquare) == Piece.EMPTY) {
						
						if (!check || (check && moveEliminatesCheck(white, fromSquare, newSquare))) {
							list.add((fromSquare+newSquare).toLowerCase());
						}
					}
				}
				// If there is no piece on the square in front of this one, we can move there.
				String newSquare = file + (rank+1);
				if (getPiece(newSquare) == Piece.EMPTY) {
					if (!check || (check && moveEliminatesCheck(white, fromSquare, newSquare))) {
						list.add((fromSquare+newSquare).toLowerCase());
					}
				}
				
				String[] arr = new String[] { ((char)(file.charAt(0)-1)) + "" + (rank+1),((char)(file.charAt(0)+1)) + "" + (rank+1) };
				//System.out.println(java.util.Arrays.toString(arr));
				for(int i=0;i<arr.length;i++) {
					if (!isValidSquare(arr[i])) continue;
					if (getPiece(arr[i]) == Piece.EMPTY) continue;
					if (!check || (check && moveEliminatesCheck(white, fromSquare, arr[i]))) {
						list.add((fromSquare+arr[i]).toLowerCase());
						System.out.println("Added: " + list.get(list.size()-1));
					}
				}
			} else if (!white) {
				boolean check = isInCheck(white);
				if (rank == 7) {
					// If there are no pieces on the two squares in front of this pawn, we can move two squares
					String newSquare = file + (rank-2);
					if (getPiece(file + (rank-1)) == Piece.EMPTY && 
						getPiece(newSquare) == Piece.EMPTY) {
						if (!check || (check && moveEliminatesCheck(white, fromSquare, newSquare))) {
							list.add((fromSquare+newSquare).toLowerCase());
						}
					}
				}
				String newSquare = file + (rank-1);
				Piece p = getPiece(newSquare); 
				if (p == Piece.EMPTY) {
					if (!check || (check && moveEliminatesCheck(white, fromSquare, newSquare))) {
						list.add((fromSquare+newSquare).toLowerCase());
					}
				}
				
				/*boolean check = isInCheck(white);
				String[] arr = new String[] { ((char)(file.charAt(0)-1)) + "" + (rank-1),((char)(file.charAt(0)+1)) + "" + (rank-1) };
				for(int i=0;i<arr.length;i++) {
					if (!isValidSquare(arr[i])) continue;
					if (getPiece(arr[i]) == Piece.EMPTY) continue;
					if (!check || (check && moveEliminatesCheck(white, fromSquare, arr[i]))) {
						list.add((fromSquare+arr[i]).toLowerCase());
					}
				}*/
			}
		}
		
		return list.toArray(new String[list.size()]);
	}
	
	public String[] generateLegalRookMoves(boolean white) {
		List<String> list = new ArrayList<String>();
		
		String[] squares = getAllSquaresWithPiece(white?Piece.WHITE_ROOK:Piece.BLACK_ROOK);
		if (squares == null) return null;
		for(String fromSquare : squares) {
			String[] arr = generatePsuedoLegalRookMoves(fromSquare);
			int lastDirectionBlocked = -1;
			for(String sq : arr) {
				if (lastDirectionBlocked == getDirectionBetween(fromSquare, sq)) continue;
				Piece p = getPiece(sq);
				if (p != Piece.EMPTY) {
					lastDirectionBlocked = getDirectionBetween(fromSquare, sq);
					if (p.isWhite() == white) continue;
				}
				if (isSquarePinned(fromSquare, sq, white?whiteKing:blackKing)) continue;
				boolean check = isInCheck(white);
				if (!check || (check && moveEliminatesCheck(white, fromSquare, sq))) {
					list.add((fromSquare + sq).toLowerCase());
				}
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	/** Explodes (removes) pieces (but NOT pawns) adjacent to <tt>toSquare</tt>.
	 * Also removes the piece on <tt>toSquare</tt>.
	 * This method will be useful for atomic implementation. 
	 * @param toSquare 
	 * @since Tuesday, November 08, 2011
	 * @author John */
	public void explodeAdjacentPieces(String toSquare) {
		removePieceOnSquare(toSquare);
		String[] squares = getAdjacentSquares(toSquare);
		for(String square : squares) {
			Piece p = getPiece(square);
			if (p != Piece.EMPTY && p != Piece.WHITE_PAWN && p != Piece.BLACK_PAWN) {
				placePiece(Piece.EMPTY, square);
			}
		}
	}
	
	/*protected String[] mergeArrays(String[] arr1,String[] arr2) {
		String[] arr = new String[arr1.length+arr2.length];
		int index=0;
		for(int i=0;i<arr1.length;i++) {
			arr[index] = arr1[index];
			index++;
		}
		for(int i=0;i<arr2.length;i++) {
			arr[index] = arr2[index];
			index++;
		}
		return arr;
	}*/
	
	/** Merge all elements of arr into a single String[] array.
	 * This implementation skips nulls. */
	protected String[] mergeArrays(String[][] arr) {
		List<String> list = new ArrayList<String>();
		for(int i=0;i<arr.length;i++) {
			if (arr[i] == null) continue;
			for(int j=0;j<arr[i].length;j++) {
				if (arr[i][j] == null) continue;
				list.add(arr[i][j]);
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	public String[] generateLegalQueenMoves(boolean white) {
		/*String[] arr1 = generateLegalBishopMoves(white);
		String[] arr2 = generateLegalRookMoves(white);
		
		if (arr1 == null && arr2 != null) { 
			return arr2;
		} else if (arr2 == null && arr1 != null) { 
			return arr1;
		} else {
			return mergeArrays(arr1,arr2);
		}*/
		
		
		List<String> list = new ArrayList<String>();
		
		String[] squares = getAllSquaresWithPiece(white?Piece.WHITE_QUEEN:Piece.BLACK_QUEEN);
		if (squares == null) return null;
		for(String fromSquare : squares) {
			String[] arr = generatePsuedoLegalQueenMoves(fromSquare);
			List<Integer> directionsBlocked = new ArrayList<Integer>(8);
			for(String sq : arr) {
				if (directionsBlocked.contains(getDirectionBetween(fromSquare, sq))) continue;
				Piece p = getPiece(sq);
				if (p != Piece.EMPTY) {
					directionsBlocked.add(getDirectionBetween(fromSquare, sq));
					if (p.isWhite() == white) continue;
				}
				if (isSquarePinned(fromSquare, sq, white?whiteKing:blackKing)) continue;
				boolean check = isInCheck(white);
				if (!check || (check && moveEliminatesCheck(white, fromSquare, sq))) {
					list.add((fromSquare + sq).toLowerCase());
				}
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	/** Gets legal moves for the given side (true = white, false = black). */
	public String[] generateLegalMoves(boolean white) {
		return mergeArrays(new String[][] { 
				generateLegalPawnMoves(white),
				generateLegalRookMoves(white),
				generateLegalKnightMoves(white),
				generateLegalBishopMoves(white),
				generateLegalQueenMoves(white),
				generateLegalKingMoves(white)
		});
	}
	
	public boolean moveEliminatesCheck(boolean white,String fromSquare,String toSquare) {
		//if (!isInCheck(white)) return false;
		if (getPiece(fromSquare) == Piece.EMPTY) return false;
		//System.out.println(java.util.Arrays.deepToString(getBoard()));
		PositionState state = deepCopy();
		state.makeMove(fromSquare, toSquare, null);
		//System.out.println(java.util.Arrays.deepToString(state.getBoard()));
		
		//state.setPrinter(DefaultPrinter.getSingletonInstance());
		//System.out.println(state.draw());
		boolean isInCheck = state.isInCheck(white);
		return !isInCheck;
	}
	
	/** Returns true if the game is a stalemate or one side has checkmate */
	public boolean isGameOver() {
		return isCheckmate(true) || isCheckmate(false) || isStalemate();
	}
	
	/** */
	public boolean isStalemate() {
		//return generateLegalMoves(true).length == 0 || generateLegalMoves(false).length == 0;
		return !canSideMove(true) || !canSideMove(false);
	}
	
	/**  */
	public boolean canSideMove(boolean white) {
		//return generateLegalMoves(white).length == 0;	
		Piece[] pieces = getPiecesOnBoard(white);
		for(int i=0;i<pieces.length;i++) {
			Piece p = pieces[i];
			switch(p) {
				case WHITE_KING: case BLACK_KING: { 
					String[] arr = generateLegalKingMoves(white);
					if (arr.length > 0) return true;
					
					continue;
				}
				case WHITE_PAWN: case BLACK_PAWN: { 
					String[] arr = generateLegalPawnMoves(white);
					if (arr.length > 0) return true;
					
					continue;
				}
				case WHITE_BISHOP: case BLACK_BISHOP: { 
					String[] arr = generateLegalBishopMoves(white);
					if (arr.length > 0) return true;
					
					continue;
				}
				case WHITE_KNIGHT: case BLACK_KNIGHT: { 
					String[] arr = generateLegalKnightMoves(white);
					if (arr.length > 0) return true;
					
					continue;
				}
				case WHITE_QUEEN: case BLACK_QUEEN: { 
					String[] arr = generateLegalQueenMoves(white);
					if (arr.length > 0) return true;
					
					continue;
				}
				case WHITE_ROOK: case BLACK_ROOK: { 
					String[] arr = generateLegalRookMoves(white);
					if (arr.length > 0) return true;
					
					continue;
				}
			}
		}
		
		return false;
	}
	
	public Piece[] getPiecesOnBoard(boolean white) {
		List<Piece> list = new ArrayList<Piece>();
		
		Piece[] allPieces = pieceLocations.keySet().toArray(new Piece[pieceLocations.keySet().size()]);
		for(int i=0;i<allPieces.length;i++) {
			Piece p = allPieces[i];
			if (p == Piece.EMPTY) continue;
			if (p.isWhite() == white && pieceLocations.get(p).size()>0) {
				list.add(p);
			}
		}
		
		return list.toArray(new Piece[list.size()]);
	}
	
	/** Returns if <tt>color</tt> is checkmated.<br />
	 * implementation not yet complete. */
	public boolean isCheckmate(boolean white) {
		// not a check so it can't be a checkmate
		//System.out.println("color = " + white);
		if (!isInCheck(white)) return false;
 		
		String[] arr = getAllSquaresWithPiece(white?Piece.WHITE_KING:Piece.BLACK_KING);
		assert arr.length == 1;
		String kingsquare = arr[0]; // we know there is only one king unless this is some weird variant
		
		//System.out.println("can we move");
		String[] squares = getAdjacentSquares(kingsquare);
		for(String s : squares) {
			Piece p = getPiece(s);
			//if (p == Piece.EMPTY) continue;
			
			// if white and black piece OR black and white piece
			if (p == Piece.EMPTY || (white && !Piece.isWhitePiece(p.getAbbreviation())) || (!white && Piece.isWhitePiece(p.getAbbreviation()))) {
				// is the piece on this square protected?
				String[] tmp = getAttackers(s);
				String[] defenders = filterSquares(tmp,!white);
				if (defenders.length > 0) continue;
				
				// This piece/square is undefended, it's not checkmate since we can take it
				return false;
			}
		}
		
		// 1. find all squares (with pieces of our color) that are attacking the king
		// 2. find all squares between each piece and the king
		// 3. see if any piece can block the check [ or capture the piece ]
		//System.out.println("can we block");
		String[] attackers = filterSquares(getAttackers(kingsquare),!white);
		for(String from : attackers) {
			squares = getSquaresBetween(from,kingsquare);
			if (squares.length == 0) continue; // this is an adjacent square and cannot be blocked
			for(String s : squares) {
				String[] mypieces = filterSquares(getAttackers(s),white);
				if (mypieces.length > 0) continue;
				
				// a piece can move here to block the check, it's not checkmate
				return false;
			}
		}
		
		//System.out.println("can we capture");
		for(String s : attackers) {
			// find squares with the same color as the king
			String[] enemypieces = filterSquares(getAttackers(s),white);
			//System.out.println(java.util.Arrays.toString(enemypieces));
			for(String from : enemypieces) {
				// see if they can capture
				if (isLegalMove(from,s)) { 
					System.out.println("legal move, not checkmate"); 
					return false;
				}
			}
		}
		
		/*String[] attackers = getAttackers(kingsquare);
		for(String s : attackers) {
			Piece p = getPiece(s);
			if (white && !Piece.isWhitePiece(p.getAbbreviation())) {
				return true;
			} else if (!white && Piece.isWhitePiece(p.getAbbreviation())) {
				return true;
			}
		}*/
		return true;
	}
	
	/** <tt>squares</tt> should be the returned value of getAttackers(). 
	 * If <tt>white</tt> is true, black pieces are removed. 
	 * If <tt>white</tt> is false, white pieces are removed. */
	public String[] filterSquares(String[] squares, boolean white) {
		List<String> newsquares = new ArrayList<String>(squares.length);
		for(String s : squares) {
			Piece p = getPiece(s);
			if (p == Piece.EMPTY) continue;
			if (p.isWhite() == white) {
				newsquares.add(s);
			}
		}
		
		return newsquares.toArray(new String[newsquares.size()]);
	}
	
	public static boolean isAdjacentSquare(String from,String to) {
		// uses binary tree for efficiency
		String[] s = getAdjacentSquares(from);
		java.util.Arrays.sort(s);
		return java.util.Arrays.binarySearch(s, to) >= 0;
	}
	
	protected void makeMove(String from,String to,String promotionPiece) {
		Piece p = getPiece(from);
		from = from.toUpperCase();
		to = to.toUpperCase();
		
		numFullMoves++;
		isDoublePawnPush = -1;
		
		String oldEp = enpassantSquare;
		
		enpassantSquare = "-";
		if (isSameFile(from, to)) {
			String[] tmp = getSquaresBetween(from, to);
			if ((p == Piece.WHITE_PAWN || p == Piece.BLACK_PAWN) && tmp.length == 1) {
				enpassantSquare = tmp[0].toLowerCase();
				
				isDoublePawnPush = ((int)from.toUpperCase().charAt(0))-65;
			}
		} else if (p == Piece.WHITE_PAWN || p == Piece.BLACK_PAWN) {
			// this is a pawn capture; handle en passant
			Point p1 = getInternalCoordsOfSquare(from);
			Point p2 = getInternalCoordsOfSquare(to);
			boolean left;
			if (p2.x == p1.x - 1) {
				left = true;
			}
			else {
				left = false;
				assert(p2.x == p1.x + 1);
			}
			if (to.equalsIgnoreCase(oldEp)) {
				String clearSquare;
				if (left) {
					p1.x -= 1;
					clearSquare = convertFromPointToSquare(p1);
				}
				else {
					p1.x += 1;
					clearSquare = convertFromPointToSquare(p1);
				}
				if (p == Piece.WHITE_PAWN) {
					assert(getPiece(clearSquare) == Piece.BLACK_PAWN);
				}
				else {
					assert(getPiece(clearSquare) == Piece.WHITE_PAWN);
				}
				//System.out.println("clearing square " + clearSquare);
				placePiece(Piece.EMPTY, clearSquare);
			}

		}

		// deal with castling privileges here
		if (p == Piece.WHITE_ROOK && from.equals("A1")) castlingRights = castlingRights.replaceFirst("Q","");
		if (p == Piece.WHITE_ROOK && from.equals("H1")) castlingRights = castlingRights.replaceFirst("K","");
		if (p == Piece.BLACK_ROOK && from.equals("A8")) castlingRights = castlingRights.replaceFirst("q","");
		if (p == Piece.BLACK_ROOK && from.equals("H8")) castlingRights = castlingRights.replaceFirst("k","");
		if (p == Piece.WHITE_KING) {
			whiteKing = to;
			castlingRights = castlingRights.replaceFirst("K","").replaceFirst("Q","");
		}
		if (p == Piece.BLACK_KING) {
			blackKing = to;
			castlingRights = castlingRights.replaceFirst("k","").replaceFirst("q","");
		}

		String promotionVerbose = ((p==Piece.WHITE_PAWN || p==Piece.BLACK_PAWN) && promotionPiece!=null?"="+promotionPiece:"");
		setVerboseNotation(p.getAbbreviation().toUpperCase()+"/"+from+"-"+to+promotionVerbose);
		
		String toRank = ""+to.charAt(1);
		// this is a promotion
		boolean isPromotion = false;
		boolean rank8 = toRank.equals("8");
		boolean rank1 = toRank.equals("1");
		if ((p == Piece.WHITE_PAWN || p == Piece.BLACK_PAWN) && (rank1 || rank8)) {
			// TODO move this validation somewhere else
			// this validation basically ensures that we are not moving a 
			// black pawn to the 8th rank or a white pawn to the first rank
			if (rank8 && p == Piece.BLACK_PAWN) { return; }
			if (rank1 && p == Piece.WHITE_PAWN) { return; }
			
			if (p == Piece.WHITE_PAWN) promotionPiece = promotionPiece.toUpperCase();
			if (p == Piece.BLACK_PAWN) promotionPiece = promotionPiece.toLowerCase();
			// swap out this pawn for the new promoted piece
			p = Piece.parsePiece(promotionPiece);
			isPromotion = true;
		}
		
		Piece pieceOnTo = getPiece(to);
		
		if (pieceLocations.get(p).contains(from)) {
			pieceLocations.get(p).remove(from);
		}
		if (!pieceLocations.get(p).contains(to)) {
			pieceLocations.get(p).add(to);
		}
		if (pieceLocations.containsKey(pieceOnTo)) {
			pieceLocations.get(pieceOnTo).remove(to);
		}
		
		if (promotions.contains(from)) {
			promotions.remove(from);
			promotions.add(to);
		} else if (isPromotion) {
			promotions.add(to);
		}
		
		placePiece(Piece.EMPTY,from);
		placePiece(p,to);
		
//		String[] arr = pieceLocations.get(p).toArray(new String[pieceLocations.get(p).size()]);
//		System.err.println(java.util.Arrays.toString(arr));
		
		// this was NOT a pawn move, so we need to increment the number of half moves
		if (p != Piece.WHITE_PAWN && p != Piece.BLACK_PAWN) { numHalfMoves++; } else { numHalfMoves = 0; }
		// we need to tell the board whether this move was made by white
		//isWhitesMove = !isWhitesMove;//!Piece.isWhitePiece(p.getAbbreviation());
		
	}

	/** Adds the specified piece to the board at the given square. */
	public void placePiece(Piece piece,String square) {
		square = square.toUpperCase();
		Point p = getInternalCoordsOfSquare(square);
		Piece pieceOnSquare = getPiece(square);
		if (pieceLocations.containsKey(pieceOnSquare)) {
			if (!pieceLocations.get(pieceOnSquare).contains(square)) {
				pieceLocations.get(pieceOnSquare).remove(square);
			}
		}
		
		board[p.y][p.x] = piece;
		if (pieceLocations.containsKey(piece)) {
			if (!pieceLocations.get(piece).contains(square)) {
				pieceLocations.get(piece).add(square);
			}
		} else {
			pieceLocations.put(piece, new ArrayList<String>());
			pieceLocations.get(piece).add(square);
		}
	}
	
	/** */
	public String convertFENToBFEN(String holding,int whiteClock,int blackClock) {
		// TODO add ability to denote which pieces are promoted
		StringBuilder b = new StringBuilder(getFEN());
		b.insert(b.indexOf(" "),"/"+holding);
		for(int i=0;i<2;i++) b.delete(b.lastIndexOf(" "),b.length());
		b.append(" " + whiteClock + " " + blackClock);
		return b.toString();
	}
	
	/** This method does NO legal verification about whether the king or rooks have moved!
	 * The only verification it does is whether the squares are empty. Returns false if there are pieces in the way, blocking the castling move. */
	protected boolean castle(boolean white,boolean kingside) {
		//System.err.println(castlingRights);
		String square = white?"E1":"E8";
		if (kingside) {
			boolean b = getPiece(white?"F1":"F8") == Piece.EMPTY && 
				getPiece(white?"G1":"G8") == Piece.EMPTY;
			if (!b) return false;
			makeMove(square,white?"G1":"G8",null);
			makeMove(white?"H1":"H8",white?"F1":"F8",null);
		} else {
			boolean b = getPiece(white?"B1":"B8") == Piece.EMPTY && 
				getPiece(white?"C1":"C8") == Piece.EMPTY && 
				getPiece(white?"D1":"D8") == Piece.EMPTY;
			if (!b) return false;
			makeMove(square,white?"C1":"C8",null);
			makeMove(white?"A1":"A8",white?"D1":"D8",null);	
		}
		
		setPrettyNotation(kingside?"O-O":"O-O-O");
		setVerboseNotation(kingside?"o-o":"o-o-o");
		numFullMoves -= 1;
		
		// remove the castling privileges for this player since he has already castled
		castlingRights = castlingRights.replaceFirst(white?"KQ":"kq","");
		
		numHalfMoves += 1;
		isWhitesMove = !isWhitesMove;
		//System.err.println(castlingRights);
		return true;
	}
	
	/** not yet implemented completely */
	public String getFEN() {
		StringBuilder b = new StringBuilder();
		for(int i=board.length-1;i>=0;i--) {
			int numEmpty = 0;
			for(int j=0;j<board[i].length;j++) {
				Piece p = board[i][j];
				if (p == null) p = Piece.EMPTY;
				if (p == Piece.EMPTY) { numEmpty++; continue; }
				if (numEmpty != 0) {
					b.append(numEmpty);
					numEmpty = 0;
				}
				b.append(p.getAbbreviation());
			}
			
			if (numEmpty != 0) {
				b.append(numEmpty);
			}
			
			if (i != 0) {
				b.append("/");
			}
		}
		
		b.append(" "+(isWhitesMove?"w":"b"));
		b.append(" "+(canAnyoneCastle()?castlingRights:"-"));
		b.append(" "+enpassantSquare);
		b.append(" "+numHalfMoves);
		b.append(" "+((1+numFullMoves)/2));
		
		return b.toString();
	}
	
	/** This code returns ALL squares that have the given piece on them - you can pass in Piece.EMPTY to find all empty squares. 
	 * Returns <tt>NULL</tt> if there are no pieces of that type on the board. */
	public String[] getAllSquaresWithPiece(Piece p) {
		if (!pieceLocations.containsKey(p)) return null;
		String[] arr = pieceLocations.get(p).toArray(new String[pieceLocations.get(p).size()]);
		//System.err.println(java.util.Arrays.toString(arr));
		return arr;
		
		/*List<String> list = new ArrayList<String>();
		for(int i=0;i<board.length;i++) {
			for(int j=0;j<board[i].length;j++) {
				if (board[i][j] == p) list.add(convertFromPointToSquare(new Point(j,i)));
			}
		}
		
		arr = list.toArray(new String[list.size()]);
		System.err.println(java.util.Arrays.toString(arr));
		return arr;*/
		
	}
	
	/** Convenience alias of placePiece(Piece.EMPTY, square); */
	protected void removePieceOnSquare(String square) {
		placePiece(Piece.EMPTY, square);
	}
	
	/** This method returns all squares that are attacking this square, REGARDLESS OF COLOR. This method excludes the piece on this square from being labeled as "protecting itself". */
	public String[] getAttackers(String square) {
		square = square.toUpperCase();
		//System.out.println("square="+square);
		List<String> list = new ArrayList<String>();
		
		Piece[] arr = Piece.values();
		for(int i=0;i<arr.length;i++) {
			Piece p = arr[i];
			if (p == Piece.EMPTY) continue;
			String[] fromArr = getAllSquaresWithPiece(p);
			if (fromArr == null) continue; // no pieces of this type on the board
			//System.out.println(p.name() + " " + java.util.Arrays.toString(fromArr));
			for(int k=0;k<fromArr.length;k++) {
				if (fromArr[k].equals(square)) continue;
				if ((p == Piece.WHITE_PAWN || p == Piece.BLACK_PAWN)) {
					boolean v = isPawnAttackingSquare(p == Piece.WHITE_PAWN,fromArr[k],square);
					//if (v) System.out.println("isPawnAttackingSquare("+(p == Piece.WHITE_PAWN)+","+fromArr[k]+","+square.toUpperCase()+") = " + v);
					if (v && !list.contains(fromArr[k])) list.add(fromArr[k]);
					continue;
				}
				if ((p == Piece.WHITE_KNIGHT || p == Piece.BLACK_KNIGHT) &&
					isLegalKnightMove(fromArr[k],square)) {
					if (!list.contains(fromArr[k])) list.add(fromArr[k]);
					continue;
				}
				if ((p == Piece.WHITE_BISHOP || p == Piece.BLACK_BISHOP) &&
					isLegalBishopMove(fromArr[k],square)) {
					if (!list.contains(fromArr[k])) list.add(fromArr[k]);
					continue;
				}
				if ((p == Piece.WHITE_ROOK || p == Piece.BLACK_ROOK) &&
					isLegalRookMove(fromArr[k],square)) {
					if (!list.contains(fromArr[k])) list.add(fromArr[k]);
					continue;
				}
				if ((p == Piece.WHITE_QUEEN || p == Piece.BLACK_QUEEN) &&
					isLegalQueenMove(fromArr[k],square)) {
					if (!list.contains(fromArr[k])) list.add(fromArr[k]);
					continue;
				}
				if ((p == Piece.WHITE_KING || p == Piece.BLACK_KING) &&
					(isLegalKingMove(fromArr[k],square)) || isKingAttackingSquare(fromArr[k],square)) {
					if (!list.contains(fromArr[k])) list.add(fromArr[k]);
					continue;
				}
			}
		}
		
		return list.toArray(new String[list.size()]);
	}
	
	/** Returns the piece on the specified square */
	public Piece getPiece(String square) {
		if (square.length() != 2) throw new IllegalArgumentException(square);
		
		char file = square.toUpperCase().charAt(0);
		int rank = Integer.parseInt(""+square.charAt(1));
		return board[rank-1][file-65];
	}
	
	/** returns a java.awt.Point instance with the (x,y) coordinates: rank = x, file = y 
	 * <br />should be accessed in the following way:<br /><br />
	 * 
	 * <code>
	 * Point p = getInternalCoordsOfSquare(square);<br />
	 * board[p.y][p.x] = ...;
	 * </code>
	 * 
	 * <p>note that this can get confusing!</p>
	 * */
	public static Point getInternalCoordsOfSquare(String square) {
		if (square.length() != 2) throw new IllegalArgumentException("Square should be of length 2. Length = " + square.length());
		square = square.toUpperCase();
		char file = square.charAt(0);
		int rank = Integer.parseInt(""+square.charAt(1));
		return new Point(file-65,rank-1);
	}
	
	/** Returns whether this piece is on the given square. */
	public boolean isPieceOnSquare(Piece type,String square) {
		return getPiece(square) == type;
	}

	/** Returns whether a piece is prevented from moving from fromSquare
     * to toSquare because it is pinned to the king on kingsquare.
     */
	public boolean isSquarePinned(String fromSquare,String toSquare,String kingsquare) {
		int kingDir = getDirectionBetween(fromSquare, kingsquare);
		if (kingDir == 0) {
			return false;
		}
		int moveDir = getDirectionBetween(fromSquare, toSquare);
		if (moveDir == kingDir || moveDir == -kingDir) {
			// can move along the direction of the pin
			return false;
		}
		/*String[] squares = getSquaresBetween(fromSquare, kingsquare);
		for (String sq: squares) {
			if (sq == toSquare) {
				// can move along the direction of the pin
				return false;
			}
			if (getPiece(sq) != Piece.EMPTY) {
				// piece blocks the pin
				return false;
			}
		}

		
		return true;*/
		int from0x88 = squareTo0x88(fromSquare);
		for (int sq = squareTo0x88(kingsquare) - kingDir; sq != from0x88; sq -= kingDir) {
			assert((sq & 0x88) == 0);
			assert(squareFrom0x88(sq) != toSquare);
			String square = squareFrom0x88(sq);
			Piece p = getPiece(square);
			if (p != Piece.EMPTY) {
				// piece blocks the pin
				return false;
			}
		}
		for (int sq = squareTo0x88(fromSquare) - kingDir; (sq & 0x88) == 0; sq -= kingDir) {
			assert(squareFrom0x88(sq) != toSquare);
			Piece p = getPiece(squareFrom0x88(sq));
			if (p != Piece.EMPTY) {
				// potentially pinning piece
				if (p.isWhite() == getPiece(kingsquare).isWhite()) {
					// can't be pinned by own piece
					return false;
				}
				switch (p) {
					case WHITE_BISHOP:
					case BLACK_BISHOP:
						return Math.abs(kingDir) == 0xf || Math.abs(kingDir) == 0x11;
					case WHITE_ROOK:
					case BLACK_ROOK:
						return Math.abs(kingDir) == 0x10 || Math.abs(kingDir) == 1;
					case WHITE_QUEEN:
					case BLACK_QUEEN:
						return true;
					default:
						return false;
				}
			}
		}
		return false;
	}

	private static int[] directionArray = initDirectionArray();
	private static int[] initDirectionArray() {
		int[] directionArray = new int[255];
		for (int i = 0; i < directionArray.length; i++) {
			directionArray[i] = 0;
		}
		int[] kingDirs = {1, -1, 0x10, -0x10, 0xf, 0x11, -0xf, -0x11};
		for (int sq = 0; sq < 128; sq++) {
			if ((sq & 0x88) != 0) {
				continue;
			}
			for (int dir: kingDirs) {
				for (int curSq = sq + dir; (curSq & 0x88) == 0; curSq += dir) {
					directionArray[curSq - sq + 127] = dir;
				}
			}
		}
		return directionArray;
	}

	public int getDirectionBetween(String from, String to) {
		int sq1 = squareTo0x88(from);
		int sq2 = squareTo0x88(to);
		return directionArray[sq2 - sq1 + 127];
	}
	
	/** Returns if the given side is in check.
	 * @param white true for white, false for black. */
	public boolean isInCheck(boolean white) {
		//String[] arr = getAllSquaresWithPiece(white?Piece.WHITE_KING:Piece.BLACK_KING);
		
		// is it a possibility to use 0x88 here? if so, do it
		
		String kingsquare = white?whiteKing:blackKing;//arr[0]; // we know there is only one king unless this is some weird variant
		//System.out.println("kingsquare = " + kingsquare);
		String[] attackers = getAttackers(kingsquare);
		attackers = filterSquares(attackers, !white);
		return attackers.length > 0;
		/*for(String s : attackers) {
			if (white && !Piece.isWhitePiece(getPiece(s).getAbbreviation())) {
				return true;
			} else if (!white && Piece.isWhitePiece(getPiece(s).getAbbreviation())) {
				return true;
			}
		}
		return false;*/
	}
	
	/** Gets all of the squares with pieces on them that are can legally move to a certain square (the <tt>toSquare</tt> parameter) */
	public String[] getPiecesThatCanGoToSquare(Piece type,String toSquare) {
		List<String> list = new ArrayList<String>();
		
		String[] squares = getAllSquaresWithPiece(type); // find the "from" squares
		
		boolean white = Piece.isWhitePiece(type.getAbbreviation());
		String king = white ? whiteKing : blackKing;
		
		for(String s : squares) {
			switch(type) {
				case WHITE_KING: case BLACK_KING: {
					if (isLegalKingMove(s, toSquare)) {
						list.add(s);
					}
					break;
				}
				case WHITE_QUEEN: case BLACK_QUEEN: {
					
					if (isLegalQueenMove(s, toSquare) && !isSquarePinned(s, toSquare, king)) {
						list.add(s);
					} else {
						//System.err.println(type.name() + " " + s + " " + toSquare + " " + isLegalQueenMove(s, toSquare));
					}
					break;
				}
				case WHITE_ROOK: case BLACK_ROOK: {
					if (isLegalRookMove(s, toSquare) && !isSquarePinned(s, toSquare, king)) {
						list.add(s);
					}
					break;
				}
				case WHITE_BISHOP: case BLACK_BISHOP: {
					if (isLegalBishopMove(s, toSquare) && !isSquarePinned(s, toSquare, king)) {
						list.add(s);
					}
					break;
				}
				case WHITE_KNIGHT: case BLACK_KNIGHT: {
					if (isLegalKnightMove(s, toSquare) && !isSquarePinned(s, toSquare, king)) {
						list.add(s);
					}
					break;
				}
				case WHITE_PAWN: {
					if (isLegalPawnMove(true, s, toSquare) && !isSquarePinned(s, toSquare, king)) {
						list.add(s);
					}
					break;
				}
					
				case BLACK_PAWN: {
					if (isLegalPawnMove(false, s, toSquare) && !isSquarePinned(s, toSquare, king)) {
						list.add(s);
					}
					break;
				}
			}
		}
		
		//System.out.println(list);
		return list.toArray(new String[list.size()]);
	}
	
	/** This returns an (x,y) point as notation - eg (0,0) = A1<br />
	 * This method is the compliament of getInternalCoordsOfSquare() */
	public static String convertFromPointToSquare(Point p) {
		return (char)(65+p.x)+""+(p.y+1);
	}
	
	/** Returns if the from and to squares are on the same rank or file. Useful for validating rook moves.<br />
	 * <code>
	 * isSameRankOrFile("H1","A1") = true<br />
	 * isSameRankOrFile("E1","C5") = false<br />
	 * isSameRankOrFile("G3","G7") = true
	 * </code>
	 * */
	public boolean isSameRankOrFile(String from,String to) {
		return isSameRank(from,to) || isSameFile(from,to);
	}
	
	public boolean isSameFile(String from,String to) {
		return from.charAt(0)==to.charAt(0);
	}
	
	public boolean isSameRank(String from,String to) {
		return from.charAt(1)==to.charAt(1);
	}

	/*
	 * convert a square to 0x88 board representation
	 * http://chessprogramming.wikispaces.com/0x88
	 */
	public static int squareTo0x88(String square) {
		int file = square.toUpperCase().charAt(0) - 65;
		int rank = Integer.parseInt(""+square.charAt(1)) - 1;
		return 0x10 * rank + file;
	}

	protected static String squareFrom0x88(int sq) {
		if ((sq & 0x88) != 0) throw new IllegalArgumentException("Invalid 0x88 square: " + sq);
		int rank = sq / 0x10;
		int file = sq % 0x10;
		return "ABCDEFGH".charAt(file) + "" + "12345678".charAt(rank);
	}
	
	/** Gets the square names of all pieces of type <code>p</code> on ZERO-BASED-INDEX <code>rank</code>. */
	public String[] getPiecesOnRank(Piece p,int rank) {
		// if we didn't have this, we would get an ArrayIndexOutOfBoundsException - I just guess which one is worse?
		if (rank < 0 || rank > 7) 
			throw new IllegalArgumentException("Rank must be greater than or equal to 0 and less than or equal to 7. Argument: " + rank);
		List<String> arr = new ArrayList<String>();
		for(int i=0;i<=7;i++) {
			if (board[rank][i] == p) arr.add(convertFromPointToSquare(new Point(i,rank)));
		}
		return arr.toArray(new String[arr.size()]);
	}
	
	public String[] getColorPiecesOnRank(boolean white,int rank) {
		// if we didn't have this, we would get an ArrayIndexOutOfBoundsException - I just guess which one is worse?
		if (rank < 0 || rank > 7) 
			throw new IllegalArgumentException("Rank must be greater than or equal to 0 and less than or equal to 7. Argument: " + rank);
		List<String> arr = new ArrayList<String>();
		for(int i=0;i<=7;i++) {
			Piece p = board[rank][i];
			if (p == Piece.EMPTY) continue;
			if (p.isWhite() == white) arr.add(convertFromPointToSquare(new Point(i,rank)));
		}
		return arr.toArray(new String[arr.size()]);
	}
	
	/** Gets the square names of all pieces of type <code>p</code> on ZERO-BASED-INDEX <code>rank</code>. */
	public String[] getColorPiecesOnFile(boolean white,int file) {
		// if we didn't have this, we would get an ArrayIndexOutOfBoundsException - I just guess which one is worse?
		if (file < 0 || file > 7) throw new IllegalArgumentException("File must be greater than or equal to 0 and less than or equal to 7.");
		List<String> arr = new ArrayList<String>();
		for(int i=0;i<=7;i++) {
			String sq = convertFromPointToSquare(new Point(file,i));
			Piece p = board[i][file];
			if (p == Piece.EMPTY) continue;
			if (p.isWhite() == white) arr.add(sq);
		}
		return arr.toArray(new String[arr.size()]);
	}
	
	/** Gets the square names of all pieces of type <code>p</code> on ZERO-BASED-INDEX <code>rank</code>. */
	public String[] getPiecesOnFile(Piece p,int file) {
		// if we didn't have this, we would get an ArrayIndexOutOfBoundsException - I just guess which one is worse?
		if (file < 0 || file > 7) throw new IllegalArgumentException("File must be greater than or equal to 0 and less than or equal to 7.");
		List<String> arr = new ArrayList<String>();
		for(int i=0;i<=7;i++) {
			String sq = convertFromPointToSquare(new Point(file,i));
			if (board[i][file] == p) arr.add(sq);
		}
		return arr.toArray(new String[arr.size()]);
	}
	
	/** This method will find the squares between the <tt>from</tt> square and the <tt>to</tt> square, if any.<br />
	 * Example: getSquaresBetween("a1","e1") would return: ["b1","c1","d1"].<br />
	 * This method works on ranks, files, or diagonals.
	 * @param from
	 * @param to
	 * @since */
	public String[] getSquaresBetween(String from,String to) {
		List<String> list = new ArrayList<String>();
		int dir = getDirectionBetween(from, to);
		if (dir != 0) {
			int sq1 = squareTo0x88(from);
			int sq2 = squareTo0x88(to);

			for (int curSq = sq1 + dir; (curSq & 0x88) == 0 && curSq != sq2; curSq += dir) {
				list.add(squareFrom0x88(curSq));

			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	/* TODO All isLegal*Move() methods are actually PSUEDO legal move methods. 
	 * They need to be renamed, possibly made static, and an appropriate replacement be given. 
	 */
	/** A pawn is not required to be on the from square in order to do this check - 
	 * this could be a good thing (for engine impl) or a bad thing (for legal move checking)! */
	public boolean isLegalPawnMove(boolean white,String from,String to) {
		//System.out.println("islegalpawn " + from +"->" + to + "; " + enpassantSquare);
		Point fromPoint = getInternalCoordsOfSquare(from);
		Point toPoint = getInternalCoordsOfSquare(to);
		int absval = Math.abs(fromPoint.x-toPoint.x);
		//System.out.println(absval);
		//System.err.println(fromPoint);
		if (absval == 1) {
			// capture or en-passant
			Piece p = getPiece(to);
			
			if (p == Piece.EMPTY) return to.equalsIgnoreCase(enpassantSquare);
			if ((white && Piece.isWhitePiece(p.getAbbreviation())) || (!white && !Piece.isWhitePiece(p.getAbbreviation()))) {
				// we cannot capture our own piece!
				return false;
			}
			if (white) return toPoint.y-fromPoint.y == 1;
			if (!white) return toPoint.y-fromPoint.y == -1;
		}
		if (absval == 0) {
			// this pawn hasn't moved
			if (white) {
	 			if (fromPoint.y==1) {
					// so it can can move two, but no more
					if (toPoint.y > 3) return false;
				} else {
					if (toPoint.y-fromPoint.y!=1) return false;
				}
			} else {
				if (fromPoint.y==6) {
					// so it can can move two, but no more
					if (toPoint.y < 4) return false;
				} else {
					if (fromPoint.y-toPoint.y!=1) return false;
				}
			}
			
			String[] arr = getSquaresBetween(from, to);
			for(String sq : arr) {
				//System.err.println(convertFromPointToSquare(p));
				if (getPiece(sq) != Piece.EMPTY) return false;
			}
			if (getPiece(to) == Piece.EMPTY) return true;
		}
		return false;
	}
	
	/** even if a pawn can't go to a square, it can still be attacking it */
	public boolean isPawnAttackingSquare(boolean white,String from,String to) {
		Point fromPoint = getInternalCoordsOfSquare(from);
		Point toPoint = getInternalCoordsOfSquare(to);
		int absval = Math.abs(fromPoint.x-toPoint.x);
		if (absval == 1) {
			return toPoint.y-fromPoint.y == (white?1:-1);
		}
		return false;
	}
	
	/** Even if a king cant go to that square, it can still be attacking it. */
	public static boolean isKingAttackingSquare(String from,String to) {
		String[] s = getAdjacentSquares(from);
		java.util.Arrays.sort(s);
		return java.util.Arrays.binarySearch(s,to)>=0;
	}
	
	public boolean isLegalKnightMove(String from,String to) {
		if (!isValidSquare(from) || !isValidSquare(to)) return false;
		Point fromPoint = getInternalCoordsOfSquare(from);
		Point toPoint = getInternalCoordsOfSquare(to);
		int beginRow = fromPoint.x,beginCol = fromPoint.y,endRow = toPoint.x,endCol = toPoint.y;
		return (Math.abs(beginRow - endRow) == 2 && Math.abs(beginCol - endCol) == 1)
				|| (Math.abs(beginRow - endRow) == 1 && Math.abs(beginCol
						- endCol) == 2);
	}
	
	public static boolean isDiagonal(String from,String to) {
		from = from.toUpperCase();
		to = to.toUpperCase();
		Point fromPoint = getInternalCoordsOfSquare(from);
		Point toPoint = getInternalCoordsOfSquare(to);
		return Math.abs(fromPoint.x-toPoint.x)==Math.abs(fromPoint.y-toPoint.y);
	}
	
	public boolean isLegalMove(String from,String to) {
		Piece p = getPiece(from);
		switch(p) {
			case WHITE_PAWN: case BLACK_PAWN: return isLegalPawnMove(p==Piece.WHITE_PAWN, from, to);
			case WHITE_BISHOP: case BLACK_BISHOP: return isLegalBishopMove(from, to);
			case WHITE_KNIGHT: case BLACK_KNIGHT: return isLegalKnightMove(from, to);
			case WHITE_ROOK: case BLACK_ROOK: return isLegalRookMove(from, to);
			case WHITE_QUEEN: case BLACK_QUEEN: return isLegalQueenMove(from, to);
			case WHITE_KING: case BLACK_KING: return isLegalKingMove(from, to);
		}
		return false;
	}
	
	public boolean isLegalBishopMove(String from,String to) {
		from = from.toUpperCase();
		to = to.toUpperCase();
		if (!isDiagonal(from, to)) return false;
		
		String[] arr = getSquaresBetween(from, to);
		for(String sq : arr) {
			//System.out.println("pt: "+convertFromPointToSquare(p));
			if (getPiece(sq) != Piece.EMPTY) return false;
		}
		return true;
	}
	
	public boolean isLegalRookMove(String from,String to) {
		from = from.toUpperCase();
		to = to.toUpperCase();
		if (!isSameRankOrFile(from, to)) return false;
		//System.err.println(from + " " + to);
		String[] arr = getSquaresBetween(from, to);
		for(String square : arr) {
			//System.err.println("sq:"+square);
			if (getPiece(square) != Piece.EMPTY) return false;
		}
		return true;
	}
	
	public boolean isLegalQueenMove(final String from,final String to) {
		//System.err.println("isLegalQueenMove "+from+" "+to+" "+isLegalRookMove(from,to));
		return isLegalRookMove(from,to) || isLegalBishopMove(from,to);
	}
	
	/** This method REQUIRES that there actually is a king on the <tt>from</tt> square!<br />
	 * It cannot be used for "hypothetical" purposes.<br />
	 * Workaround: place a king on the <tt>from</tt> square, call this method, remove king from the <tt>from</tt> square. */
	public boolean isLegalKingMove(String from,String to) {
		/* this will have a little more complicated logic:
		 * first a method needs to be written to see if the difference 
		 * between the "from" and "to" squares are in any direction
		 * next we will have to see if the square that the king is 
		 * trying to move to is under attack by the opponent.
		 * afterwards, we need to add castling checks.
		 */
		
		from = from.toUpperCase();
		to = to.toUpperCase();
		
		Point fromPoint = getInternalCoordsOfSquare(from);
		Point toPoint = getInternalCoordsOfSquare(to);
		int beginRow = fromPoint.x,beginCol = fromPoint.y,endRow = toPoint.x,endCol = toPoint.y;
		
		// we CANNOT use Math.abs() here because: what if player tries moving queenside two squares?
		if (beginCol - endCol == 2) {
			// king side castling
//			if (canWhiteCastleKingside()) return true;
//			if (canBlackCastleKingside()) return true;
		}
		if (beginCol - endCol == 3) {
			// queen side castling
//			if (canWhiteCastleQueenside()) return true;
//			if (canBlackCastleQueenside()) return true;
		}
		
		// this code is problematic because of recursion problem with calling getAttackers().
		/*boolean amIWhite = Piece.isWhitePiece(getPiece(from).getAbbreviation());
		String[] attackers = getAttackers(to);
		attackers = filterSquares(attackers, !amIWhite);
		if (attackers.length > 0) return false;*/
		
		//Piece pieceOnSquare = getPiece(from);
		
		/*PositionState ps = this.deepCopy();
		ps.makeMove(from, to, null);
		if (ps.isInCheck(pieceOnSquare.isWhite())) return false;*/
		
		String[] adjacent = getAdjacentSquares(to);
		for(int i=0;i<adjacent.length;i++) {
			if (from.equals(adjacent[i])) continue; // exclude square we're coming from
			Piece p = getPiece(adjacent[i]);
			// if there is a king on an square next to the square we want to go to
			// this may need to be removed for variants such as suicide and atomic
			// kings cannot touch
			if (p == Piece.WHITE_KING || p == Piece.BLACK_KING) return false;
		}
		
		return ((Math.abs(beginRow - endRow) == 0 || Math.abs(beginRow - endRow) == 1) 
				&& (Math.abs(beginCol - endCol) == 0 || Math.abs(beginCol - endCol) == 1));
	}
	
	public static Piece[][] getEmptyBoard() {
		Piece[][] board = new Piece[8][8];
		for(int i=board.length-1;i>=0;i--) {
			for(int j=0;j<board[i].length;j++) {
				board[i][j] = Piece.EMPTY;
			}
		 }
		return board;
	}
	
	public static PositionState getStartingPositionState() {
		PositionState ps = new PositionState(PositionState.getStartingPosition());
		ps.setPrinter(Style12Printer.getSingletonInstance());
		ps.setVerboseNotation("none");
		ps.setPrettyNotation("none");
		ps.setCastlingRights("KQkq");
		ps.setWhitesMove(true);
		return ps;
	}
	
	public static Piece[][] getStartingPosition() {
		 Piece[][] board = new Piece[8][8];
		 
		 for(int i=board.length-1;i>=0;i--) {
			 //System.err.print(i);
			 if (i == 7) { 
				 // initialize black's back rank
				 board[i][0] = board[i][7] = Piece.BLACK_ROOK;
				 board[i][1] = board[i][6] = Piece.BLACK_KNIGHT;
				 board[i][2] = board[i][5] = Piece.BLACK_BISHOP;
				 board[i][3] = Piece.BLACK_QUEEN;
				 board[i][4] = Piece.BLACK_KING;
				 continue;
			 }
			 if (i == 6) {
				 // initialize black's pawns
				 for(int j=0;j<board[i].length;j++) {
					 board[i][j] = Piece.BLACK_PAWN;
				 }
				 continue;
			 }
			 if (i >= 2 && i <= 5) {
				 // set all of the squares to empty squares
				 for(int j=0;j<board[i].length;j++) {
					 board[i][j] = Piece.EMPTY;
				 }
				 continue;
			 }
			 if (i == 1) {
				 // initialize white's pawns
				 for(int j=0;j<board[i].length;j++) {
					 board[i][j] = Piece.WHITE_PAWN;
				 }
				 continue;
			 }
			 if (i == 0) {
				 // initialize white's back rank
				 board[i][0] = board[i][7] = Piece.WHITE_ROOK;
				 board[i][1] = board[i][6] = Piece.WHITE_KNIGHT;
				 board[i][2] = board[i][5] = Piece.WHITE_BISHOP;
				 board[i][3] = Piece.WHITE_QUEEN;
				 board[i][4] = Piece.WHITE_KING;
				 continue;
			 }
		 }
		 return board;
	}
	
	/** Uses the specified BoardPrinter object to return a textual string of the internal board representation. */
	public String draw() {
		return getPrinter().draw(board);
	}
	
	public void setBoard(Piece[][] board) {
		this.board = board;
	}
	public Piece[][] getBoard() {
		return board;
	}
	
	public void setCastlingRights(String castlingRights) {
		this.castlingRights = castlingRights;
	}
	
	/** This method does NOT take into account castling out of, into, through check. 
	 * You will need to determine this on your own. */
	public boolean canWhiteCastleKingside() {
		return castlingRights.contains("K");
	}
	
	/** This method does NOT take into account castling out of, into, through check. 
	 * You will need to determine this on your own. */
	public boolean canWhiteCastleQueenside() {
		return castlingRights.contains("Q");
	}
	
	/** This method does NOT take into account castling out of, into, through check. 
	 * You will need to determine this on your own. */
	public boolean canBlackCastleKingside() {
		return castlingRights.contains("k");
	}
	
	/** This method does NOT take into account castling out of, into, through check. 
	 * You will need to determine this on your own. */
	public boolean canBlackCastleQueenside() {
		return castlingRights.contains("q");
	}
	
	/** convenience method. */
	public boolean canAnyoneCastle() {
		return !castlingRights.equals("");
	}
	
	public boolean isPawnPromotion() {
		return isPawnPromotion;
	}
	public Piece getPiecePromotedTo() {
		return piecePromotedTo;
	}
	public BoardPrinter getPrinter() {
		return printer;
	}
	public void setPrinter(BoardPrinter printer) {
		this.printer = printer;
	}

	public boolean isWhitesMove() {
		return isWhitesMove;
	}

	protected void setWhitesMove(boolean isWhitesMove) {
		this.isWhitesMove = isWhitesMove;
	}

	public String getVerboseNotation() {
		return verboseNotation;
	}

	protected void setVerboseNotation(String verboseNotation) {
		this.verboseNotation = verboseNotation;
	}
	
	public HashMap<Piece, List<String>> getPieceLocations() {
		return pieceLocations;
	}

	public void setPrettyNotation(String prettyNotation) {
		this.prettyNotation = prettyNotation;
	}

	public String getPrettyNotation() {
		return prettyNotation;
	}

	public void setNotationInput(String notationInput) {
		this.notationInput = notationInput;
	}

	public String getNotationInput() {
		return notationInput;
	}

	public void setIsDoublePawnPush(int isDoublePawnPush) {
		this.isDoublePawnPush = isDoublePawnPush;
	}

	/** -1 if the previous move was NOT a double pawn push, otherwise the chess 
  board file  (numbered 0--7 for a--h) in which the double push was made */
	public int getDoublePawnPushFile() {
		return isDoublePawnPush;
	}
	
	public boolean isDoublePawnPush() {
		return isDoublePawnPush > -1;
	}
}
