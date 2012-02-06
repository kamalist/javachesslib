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
package board.printer;

import board.Piece;

/** A BoardPrinter is an interface that provides a draw() method - allowing you to specify your own ways of printing the board in the way that you want it. */
public interface BoardPrinter {
	public String draw(Piece[][] board);
}
