package com.ecc;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Comparator;
import java.util.Collections;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

public class Exercise4_bak {

	private static class InnerCell implements Comparable<InnerCell> {
		private String leftCell;
		private String rightCell;

		public InnerCell() {}

		public InnerCell(String leftCell, String rightCell) {
			this.leftCell = leftCell;
			this.rightCell = rightCell;
		}

		public void setLeftCell(String leftCell) {
			this.leftCell = leftCell;
		}

		public void setRightCell(String rightCell) {
			this.rightCell = rightCell;
		}

		public String getLeftCell() {
			return this.leftCell;
		}

		public String getRightCell() {
			return this.rightCell;
		}

		@Override
		public String toString() {
			return leftCell + rightCell;
		}

		@Override
		public int compareTo(InnerCell other) {
			return new CompareToBuilder().append(this.toString(), other.toString()).toComparison();
		}
	}

	public static final char OUTER_CELL_DELIMITER = '/';
	public static final char INNER_CELL_DELIMITER = ',';

	private String tableFilePath;

	List<List<InnerCell>> rowCells;

	public Exercise4_bak(String tableFilePath) throws FileNotFoundException, IOException {
		this(new File(tableFilePath));
	}

	public Exercise4_bak(File tableFile) throws FileNotFoundException, IOException {
		this.tableFilePath = tableFile.getAbsolutePath();
		this.rowCells = parseTable(tableFile);
	}
 
	public int[] getCell(boolean isNullCell) {
		int[] cellLocation = new int[2];

		boolean inputValidated = false;

		do {
			cellLocation[0] = Utility.getIntegerInput("Enter Row:", 0, getRowCount() - 1);
			cellLocation[1] = Utility.getIntegerInput("Enter Column:", 0, getColumnCount() - 1);

			if (isNullCell == isCellNull(cellLocation[0], cellLocation[1])) {
				inputValidated = true;
			}
			else {
				System.out.println(isNullCell ? 
					"Chosen cell is not null. Please choose another" : 
					"Chosen cell is null. Try \"Add Column\" first."
				);
			}
		} while (!inputValidated);

		return cellLocation;
	}

	public int getRowCount() {
		return this.rowCells.size();
	}

	public int getColumnCount() {

		if (getRowCount() == 0) {
			return 0;
		}		

		return (this.rowCells.size() == 0) ? 0 : this.rowCells.get(0).size();
	}

	public void searchTable(String searchString) {

		for (int i = 0, s = this.rowCells.size(); i < s; i++) {
			List<InnerCell> columnCells = this.rowCells.get(i);

			for (int j = 0, t = columnCells.size(); j < t; j++) {

				if (isCellNull(i, j)) {
					continue;
				}

				InnerCell innerCell = columnCells.get(j);
				int leftCellCount = StringUtils.countMatches(innerCell.getLeftCell(), searchString);
				// int leftCellCount = Utility.countOccurrence(innerCell.getLeftCell(), searchString);

				if (leftCellCount > 0) {
					System.out.printf(
							"@(%d,%d) Left Inner Cell, Found %d occurrences.\n", i, j, leftCellCount);
				}

				int rightCellCount = StringUtils.countMatches(innerCell.getRightCell(), searchString);
				// int rightCellCount = Utility.countOccurrence(innerCell.getRightCell(), searchString);

				if (rightCellCount > 0) {
					System.out.printf(
							"@(%d,%d) Right Inner Cell, Found %d occurrences.\n", i, j, rightCellCount);
				}
			}
		}

		System.out.println();
	}

	public boolean isCellNull(int row, int col) {
		if (row >= getRowCount() || col >= getColumnCount() || row < 0 || col < 0) {
			return true;
		}

		return this.rowCells.get(row).get(col).getLeftCell() == null;
	}

	public void editCell(String str, int row, int col, boolean isRightPart) {
		if (row >= getRowCount() || col >= getColumnCount() || row < 0 || col < 0 ||
			isCellNull(row, col)) {
			return;
		}

		if (isRightPart) {
			this.rowCells.get(row).get(col).setRightCell(str);
		}
		else {
			this.rowCells.get(row).get(col).setLeftCell(str);
		}
	}

	public void addRow() {
		List<InnerCell> columnCells = new ArrayList<>();

		for (int j = 0, s = getColumnCount(); j < s; j++) {

			columnCells.add((j == 0) ? 
				new InnerCell(getRandomString(), getRandomString()) : 
				new InnerCell());	
		}

		this.rowCells.add(columnCells);
	}

	public void addColumn(String leftStr, String rightStr, int row, int col) {
		if (row >= getRowCount() || col >= getColumnCount() || row < 0 || col < 0 ||
			!isCellNull(row, col)) {
			return;
		}

		InnerCell innerCell = this.rowCells.get(row).get(col);
		innerCell.setLeftCell(leftStr);
		innerCell.setRightCell(rightStr);
	}

	public void sortRow(int row, boolean isAscending) {
		if (row >= getRowCount() || row < 0) {
			return;
		}

		if (isAscending) {
			Collections.sort(this.rowCells.get(row));
		}
		else {
			Collections.sort(this.rowCells.get(row), Collections.reverseOrder());
		}
	}

	public void resetTable(int row, int col) {
		this.rowCells = new ArrayList<>();

		for (int i = 0; i < row; i++) {
			List<InnerCell> columnCells = new ArrayList<>();
			
			for (int j = 0; j < col; j++) {
				InnerCell innerCell = new InnerCell(getRandomString(), getRandomString());
				columnCells.add(innerCell);;
			}

			this.rowCells.add(columnCells);
		}
	}

	public void displayTable() {
		for (int i = 0, s = this.rowCells.size(); i < s; i++) {
			List<InnerCell> columnCells = this.rowCells.get(i);

			for (int j = 0, t = columnCells.size(); j < t; j++) {
				InnerCell innerCell = columnCells.get(j);

				if (isCellNull(i, j)) {
					System.out.print("NULL");
				}
				else {
					System.out.printf("%s,%s", innerCell.getLeftCell(), innerCell.getRightCell());
				}
				
				if (j < t - 1) {
					System.out.print(OUTER_CELL_DELIMITER);
				}
			}
			System.out.println();
		}

		System.out.println();
	}

	public void persistTable() {
		TextStringBuilder textStringBuilder = new TextStringBuilder();

		// this.rowCells.stream().forEach((columnCells) -> {
		// 	columnCells.stream().map(innerCell -> innerCell == null ? innerCell.getLeftCell() + )

		// 	textStringBuilder.map()

		// });

		// for (int i = 0, s = getRowCount(); i < s; i++) {
		// 	for (int j = 0, k = getColumnCount(); j < k; j++) {
		// 		if (!isCellNull(i, j)) {
		// 			InnerCell innerCell = this.rowCells.get(i).get(j);

		// 			bufferedWriter.write(
		// 				String.format("%s%s%s", innerCell.getLeftCell(), INNER_CELL_DELIMITER, innerCell.getRightCell()));
		// 		}

		// 		if (j < k - 1) {
		// 			bufferedWriter.write(OUTER_CELL_DELIMITER);
		// 		}
		// 	}

		// 	bufferedWriter.write("\n");
		// }

		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.tableFilePath))) {

			for (int i = 0, s = getRowCount(); i < s; i++) {
				for (int j = 0, k = getColumnCount(); j < k; j++) {
					if (!isCellNull(i, j)) {
						InnerCell innerCell = this.rowCells.get(i).get(j);

						bufferedWriter.write(
							String.format("%s%s%s", innerCell.getLeftCell(), INNER_CELL_DELIMITER, innerCell.getRightCell()));
					}

					if (j < k - 1) {
						bufferedWriter.write(OUTER_CELL_DELIMITER);
					}
				}

				bufferedWriter.write("\n");
			}

			bufferedWriter.write(textStringBuilder.toString());

			bufferedWriter.flush();

		} catch (Exception e) {
			System.out.println("Data persistence failed");
		}
	}

	private List<List<InnerCell>> parseTable(File tableFile) 
		throws FileNotFoundException, IOException {

		BufferedReader bufferedReader = new BufferedReader(new FileReader(tableFile));

		List<List<InnerCell>> rowCells = new ArrayList<>();

		String stringLine;
		int columnSize = -1;

		while ((stringLine = bufferedReader.readLine()) != null && !stringLine.trim().isEmpty()) {

			List<InnerCell> columnCells = new ArrayList<InnerCell>();

			String[] outerCells = extractOuterCells(stringLine);
			
			if (columnSize == -1) {
				columnSize = outerCells.length;
			}
			else if (columnSize != outerCells.length) {
				System.out.println("Please ensure that all rows have the same number of columns");
				throw new IOException();
			}

			for (int i = 0, s = outerCells.length; i < s; i++) {
				String outerCell = outerCells[i];

				InnerCell innerCell = extractInnerCell(outerCell);
	        	columnCells.add(innerCell);
            }

            rowCells.add(columnCells);
		}

		if (rowCells.size() == 0) {
			rowCells.add(Arrays.asList(new InnerCell()));
		}

		return rowCells;
	}

	private String[] extractOuterCells(String stringLine) {
		String[] outerCells;

		if (stringLine.charAt(stringLine.length() - 1) == OUTER_CELL_DELIMITER) {
			stringLine += " ";
			outerCells = stringLine.split(OUTER_CELL_DELIMITER + Utility.EMPTY_STRING);
			outerCells[outerCells.length - 1] = Utility.EMPTY_STRING;
		}
		else {
			outerCells = stringLine.split(OUTER_CELL_DELIMITER + Utility.EMPTY_STRING);
		}

		return outerCells;
	}

	private InnerCell extractInnerCell(String outerCell) throws IOException {
		String[] innerCellArray;

		if (outerCell.length() > 0 && 
			outerCell.charAt(outerCell.length() - 1) == INNER_CELL_DELIMITER) {
			outerCell += " ";
        	innerCellArray = outerCell.split(INNER_CELL_DELIMITER + Utility.EMPTY_STRING);
        	return new InnerCell(innerCellArray[0], Utility.EMPTY_STRING);
		}
		else {
        	innerCellArray = outerCell.split(INNER_CELL_DELIMITER + Utility.EMPTY_STRING);

        	if (innerCellArray.length > 2) {
				System.out.println("Each inner cells can only have 1 delimiter");
            	throw new IOException();
			}
			else if (innerCellArray.length == 2) {
				return new InnerCell(innerCellArray[0], innerCellArray[1]);
			}
			else if (innerCellArray.length == 1) {
				if (innerCellArray[0].equals(Utility.EMPTY_STRING)) {
					return new InnerCell();
            	}
            	else {
            		System.out.println("Each non-empty cell should have a delimiter");
            		throw new IOException();
            	}
			}
			else {
				return new InnerCell(innerCellArray[0], innerCellArray[1]);
			}
		}
	}

	private String getRandomString() {
		StringBuilder sb = new StringBuilder();

		Random rnd = new Random();

		for (int i = 0; i < 5; i++) {

			int ascii = -1;

			do {
				// ascii character between 0 ~ 31 represent symbols not found in the keyboard. Also, 127 
				// ascii is an empty character. Hence, allowed ascii range is between 32 ~ 126 only.
				ascii = rnd.nextInt(127 - 32) + 32; 
			} while (ascii == INNER_CELL_DELIMITER || ascii == OUTER_CELL_DELIMITER);
			

			sb.append((char) ascii);	
		}

		return sb.toString();
	}
}
