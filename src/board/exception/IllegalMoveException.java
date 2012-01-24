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
package board.exception;

/** Exception thrown when an illegal move is given.
 * For example, when we try moving into check, etc. */
public class IllegalMoveException extends Exception {
	private static final long serialVersionUID = 1L;

	private String notation;
	private String fen;
	private String message;
	private String extraInfo;
	
	public IllegalMoveException() {
		
	}
	
	public IllegalMoveException(String notation,String fen,String message,String extraInfo) {
		setNotation(notation);
		setFen(fen);
		setMessage(message);
		setExtraInfo(extraInfo);
	}

	public void setNotation(String notation) {
		this.notation = notation;
	}

	public String getNotation() {
		return notation;
	}

	public void setFen(String fen) {
		this.fen = fen;
	}

	public String getFen() {
		return fen;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}

	public String getExtraInfo() {
		return extraInfo;
	}
}
