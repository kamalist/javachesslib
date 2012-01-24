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
package board.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import board.Board;
import board.exception.IllegalMoveException;
import board.exception.WrongColorToMoveException;
import board.printer.DefaultPrinter;

public class Runner {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws WrongColorToMoveException 
	 * @throws IllegalMoveException 
	 */
	public static void main(String[] args) throws IOException, IllegalMoveException, WrongColorToMoveException {
		// TODO Auto-generated method stub
		
		Board b = new Board();
		
		BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));
		do {
			System.out.println(b.getPositions().size());
			
			b.getLatestMove().setPrinter(DefaultPrinter.getSingletonInstance());
			System.out.println(b.getLatestMove().draw());
			
			System.out.print("Your Move: ");
			
			String input = rdr.readLine();
			if (input.equals("quit")) {
				break;
			} else if (input.startsWith("takeback ")) {
				
			} else {
				boolean isWhitesMove = !b.getLatestMove().isWhitesMove();
				if (b.getPositions().size() == 1) isWhitesMove = true; 
				b.move(isWhitesMove, input);
			}
		} while(true);
		
		System.out.println("Goodbye.");
	}

}
