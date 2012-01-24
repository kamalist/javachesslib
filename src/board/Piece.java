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

/** This enum represents a piece. */
public enum Piece {
	WHITE_KING ("K"),
	WHITE_QUEEN ("Q"),
	WHITE_ROOK ("R"),
	WHITE_BISHOP ("B"),
	WHITE_KNIGHT ("N"),
	WHITE_PAWN ("P"),
	BLACK_KING ("k"),
	BLACK_QUEEN ("q"),
	BLACK_ROOK ("r"),
	BLACK_BISHOP ("b"),
	BLACK_KNIGHT ("n"),
	BLACK_PAWN ("p"),
	EMPTY ("-");
	
	private String abbreviation;
	private Piece(String abbr) {
		abbreviation = abbr;
	}

	public String getAbbreviation() {
		return abbreviation;
	}
	
	public boolean isWhite() {
		return abbreviation == abbreviation.toUpperCase();
	}
	
	public static Piece parsePiece(String abbr) {
		Piece[] pieces = Piece.values();
		for(int i=0;i<pieces.length;i++) {
			if (pieces[i].getAbbreviation().equals(abbr)) return pieces[i];
		}
		return null;
	}
	
	/** Returns if the specified abbreviation denotes a white-colored piece. Returns false for Piece.EMPTY. */
	public static boolean isWhitePiece(String abbr) {
		if (abbr.equals("-")) return false;
		return abbr == abbr.toUpperCase();
	}
	
	/*@Override
	public String toString() {
		return getAbbreviation();
	}*/
}
