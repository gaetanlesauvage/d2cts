package org.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class TraceComparator {
	public static void main(String[] args) throws FileNotFoundException {
		Scanner scan1 = null;
		Scanner scan2 = null;
		try {
			scan1 = new Scanner(new File(args[0]));
			scan2 = new Scanner(new File(args[1]));
			boolean ended = false;

			int lineNumber = 1;
			while (scan1.hasNextLine() && !ended) {
				String l1 = scan1.nextLine();
				if (scan2.hasNextLine()) {
					String l2 = scan2.nextLine();
					if (!l1.equals(l2)) {
						System.out.println("First Difference at line "
								+ lineNumber);
						ended = true;
					}

				} else {
					System.out.println("End of file 2 at line " + lineNumber);
					ended = true;
				}
				lineNumber++;
			}
			if (!ended)
				System.out.println("End of file 1 at line " + lineNumber);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (scan1 != null)
				scan1.close();
			if (scan2 != null)
				scan2.close();
		}

	}
}
