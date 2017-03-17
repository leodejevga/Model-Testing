package org.eclipse.emf.henshin.paths.builder;

// TODO: Auto-generated Javadoc
/**
 * The Class PathException.
 */
public class PathException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1546845184351L;
	
	/** The ep. */
	ErrorPosition ep;

	/**
	 * Instantiates a new path exception.
	 *
	 * @param text the text
	 * @param ep the ep
	 */
	public PathException(String text, ErrorPosition ep) {
		super(text);
		this.ep = ep;
	}

	/**
	 * Gets the position.
	 *
	 * @return the position
	 */
	public ErrorPosition getPosition() {
		return ep;
	}
	
	/**
	 * The Class ErrorPosition.
	 */
	public static class ErrorPosition {
		
		/** The line. */
		public int line;
		
		/** The start. */
		public int start;
		
		/** The end. */
		public int end;

		/** The start temp. */
		private int startTemp = 0;
		
		/** The end temp. */
		private int endTemp = 0;

		/**
		 * Sets the.
		 *
		 * @param start the start
		 * @param end the end
		 */
		public void set(int start, int end) {
			if (startTemp == 0)
				startTemp = this.start;
			if (endTemp == 0)
				endTemp = this.end;
			this.start = start;
			this.end = end;
		}

		/**
		 * Reset.
		 */
		public void reset() {
			if (startTemp != 0)
				start = startTemp;
			if (endTemp != 0)
				end = endTemp;
			startTemp = 0;
			endTemp = 0;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "Line = " + line + " from: " + start + " to: " + end;
		}

		/**
		 * Sets the.
		 *
		 * @param row the row
		 * @param target the target
		 */
		public void set(String row, String target) {
			int s = this.start - (row.length() - row.indexOf(target)) + 1;
			int e = this.end - (row.length() - row.indexOf(target) - target.length());
			set(s, e);
		}
	}

}
