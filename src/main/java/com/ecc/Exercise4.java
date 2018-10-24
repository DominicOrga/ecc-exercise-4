package com.ecc;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Comparator;
import java.util.Collections;
import java.util.Optional;
import java.util.Map;

import java.util.function.Supplier;

import java.util.stream.Collectors;
import java.util.stream.Stream;

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

public class Exercise4 {

	private static class InnerCell implements Comparable<InnerCell> {
		private String leftCell;
		private String rightCell;

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
			return new CompareToBuilder()
							.append(this.toString().toLowerCase(), other.toString().toLowerCase())
							.toComparison();
		}
	}

	public static final char OUTER_CELL_DELIMITER = '/';
	public static final char INNER_CELL_DELIMITER = ',';

	private String tableFilePath;

	List<List<Optional<InnerCell>>> rowCells;

	public Exercise4(String tableFilePath) throws FileNotFoundException, IOException {
		this(new File(tableFilePath));
	}

	public Exercise4(File tableFile) throws FileNotFoundException, IOException {
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

	public boolean isCellNull(int row, int col) {
		if (row >= getRowCount() || col >= getColumnCount() || row < 0 || col < 0) {
			return true;
		}

		return !this.rowCells.get(row).get(col).isPresent();
	}

	public void searchTable(String searchString) {

		for (int i = 0, s = this.rowCells.size(); i < s; i++) {
			List<Optional<InnerCell>> columnCells = this.rowCells.get(i);

			for (int j = 0, t = columnCells.size(); j < t; j++) {

				Optional<InnerCell> innerCell = columnCells.get(j);

				if (!innerCell.isPresent()) {
					continue;
				}

				int leftCellCount = StringUtils.countMatches(innerCell.get().getLeftCell(), searchString);

				if (leftCellCount > 0) {
					System.out.printf(
							"@(%d,%d) Left Inner Cell, Found %d occurrences.\n", i, j, leftCellCount);
				}

				int rightCellCount = StringUtils.countMatches(innerCell.get().getRightCell(), searchString);

				if (rightCellCount > 0) {
					System.out.printf(
							"@(%d,%d) Right Inner Cell, Found %d occurrences.\n", i, j, rightCellCount);
				}
			}
		}

		System.out.println();
	}

	public void editCell(String str, int row, int col, boolean isRightPart) {
		if (row >= getRowCount() || col >= getColumnCount() || row < 0 || col < 0 ||
			isCellNull(row, col)) {
			return;
		}

		Optional<InnerCell> innerCell = this.rowCells.get(row).get(col);

		if (innerCell.isPresent()) {
			if (isRightPart) {
				innerCell.get().setRightCell(str);
			}
			else {
				innerCell.get().setLeftCell(str);
			}
		}		
	}

	public void addRow() {
		List<Optional<InnerCell>> columnCells = new ArrayList<>();

		for (int j = 0, s = getColumnCount(); j < s; j++) {
			columnCells.add((j == 0) ? 
				Optional.of(new InnerCell(getRandomString(), getRandomString())) : 
				Optional.empty());
		}

		this.rowCells.add(columnCells);
	}

	public void addColumn(String leftStr, String rightStr, int row, int col) {
		if (row >= getRowCount() || col >= getColumnCount() || row < 0 || col < 0) {
			return;
		}

		Optional<InnerCell> innerCell = this.rowCells.get(row).get(col);

		if (!innerCell.isPresent()) {
			this.rowCells.get(row).set(col, Optional.of(new InnerCell(leftStr, rightStr)));
		}
	}

	public void sortRow(int row, boolean isAscending) {
		if (row >= getRowCount() || row < 0) {
			return;
		}

		Map<Boolean, List<Optional<InnerCell>>> presentInnerCells = 
			this.rowCells.get(row).stream().collect(Collectors.partitioningBy(Optional::isPresent));

		List<Optional<InnerCell>> sortedInnerCells = 
			presentInnerCells.get(true)
					         .stream()
					         .map(Optional::get)
					         .sorted(isAscending ? Comparator.naturalOrder() : Comparator.reverseOrder())
			                 .map(innerCell -> Optional.ofNullable(innerCell))
			                 .collect(Collectors.toList());
   
       	this.rowCells.set(
       		row, 
       		Stream.concat(sortedInnerCells.stream(), presentInnerCells.get(false).stream())
       		      .collect(Collectors.toList())
   		);
	}

	public void resetTable(int row, int col) {
		Supplier<List<Optional<InnerCell>>> rowCellSupplier = () -> {
			return Stream.generate(() -> {
							 	 return Optional.of(new InnerCell(getRandomString(), getRandomString()));
						 })
						 .limit(col)
						 .collect(Collectors.toList());
		};

		this.rowCells = Stream.generate(rowCellSupplier)
							  .limit(row)
							  .collect(Collectors.toList());
	}

	public void displayTable() {
		for (int i = 0, s = this.rowCells.size(); i < s; i++) {
			List<Optional<InnerCell>> columnCells = this.rowCells.get(i);

			for (int j = 0, t = columnCells.size(); j < t; j++) {
				Optional<InnerCell> innerCell = columnCells.get(j);

				if (innerCell.isPresent()) {
					System.out.printf("%s,%s", innerCell.get().getLeftCell(), innerCell.get().getRightCell());
				}
				else {
					System.out.print("NULL");
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

		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.tableFilePath))) {
			TextStringBuilder textStringBuilder = new TextStringBuilder();

			this.rowCells.stream().forEach(
				(columnCells) -> {
					List<String> columnCellsStr = 
						columnCells.stream()
						           .map(innerCell -> 
						           		innerCell.isPresent() ? 
											innerCell.get().getLeftCell() + 
												INNER_CELL_DELIMITER + innerCell.get().getRightCell() :
											Utility.EMPTY_STRING)
							 	   .collect(Collectors.toList());

		 			textStringBuilder.appendWithSeparators(
		 				columnCellsStr, OUTER_CELL_DELIMITER + Utility.EMPTY_STRING);

		 			textStringBuilder.append("\n");
				});

			bufferedWriter.write(textStringBuilder.toString());
			bufferedWriter.flush();

		} catch (IOException e) {
			System.out.println("Data persistence failed");
		}
	}

	private List<List<Optional<InnerCell>>> parseTable(File tableFile) 
		throws FileNotFoundException, IOException {

		BufferedReader bufferedReader = new BufferedReader(new FileReader(tableFile));

		List<List<Optional<InnerCell>>> rowCells = new ArrayList<>();

		String stringLine;
		int columnSize = -1;

		while ((stringLine = bufferedReader.readLine()) != null && !stringLine.trim().isEmpty()) {

			List<Optional<InnerCell>> columnCells = new ArrayList<>();

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
	        	columnCells.add(extractInnerCell(outerCell));
            }

            rowCells.add(columnCells);
		}

		if (rowCells.size() == 0) {
			rowCells.add(Arrays.asList(Optional.empty()));
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

	private Optional<InnerCell> extractInnerCell(String outerCell) throws IOException {
		String[] innerCellArray;

		if (outerCell.length() > 0 && 
			outerCell.charAt(outerCell.length() - 1) == INNER_CELL_DELIMITER) {
			outerCell += " ";
        	innerCellArray = outerCell.split(INNER_CELL_DELIMITER + Utility.EMPTY_STRING);
        	return Optional.of(new InnerCell(innerCellArray[0], Utility.EMPTY_STRING));
		}
		else {
        	innerCellArray = outerCell.split(INNER_CELL_DELIMITER + Utility.EMPTY_STRING);

        	if (innerCellArray.length > 2) {
				System.out.println("Each inner cells can only have 1 delimiter");
            	throw new IOException();
			}
			else if (innerCellArray.length == 2) {
				return Optional.of(new InnerCell(innerCellArray[0], innerCellArray[1]));
			}
			else if (innerCellArray.length == 1) {
				if (innerCellArray[0].equals(Utility.EMPTY_STRING)) {
					return Optional.empty();
            	}
            	else {
            		System.out.println("Each non-empty cell should have a delimiter");
            		throw new IOException();
            	}
			}
			else {
				return Optional.of(new InnerCell(innerCellArray[0], innerCellArray[1]));
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
