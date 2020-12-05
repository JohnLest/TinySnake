import java.io.Serializable;
import java.util.*;

/**
 * Represents a rectangle using top left corner, length and height
 */
public class Rectangle implements Iterable<Coord> , Serializable
{
    private Coord topLeft;
    private final int length;
    private final int height;
    private final int minSize;

    /**
     * Creates new Rectangle
     *
     * @param topLeftL The line of the top left corner
     * @param topLeftC The column of the top left corner
     * @param length The length of the Rectangle
     * @param height The height of the Rectangle
     * @param minSize The size under which an IllegalArgumentException is throuwn
     */
    public Rectangle(int topLeftL, int topLeftC, int length, int height, int minSize) 
    {
	    if (length < minSize || height < minSize)
	        throw new IllegalArgumentException("Rectangle length/height properties must be non-negative");

	    topLeft = new Coord(topLeftL, topLeftC);
	    this.length = length;
	    this.height = height;
        this.minSize = minSize;
    }

    /**
     * Constructor for serialization
     */
    public Rectangle() {
		this.length = 0;
		this.height = 0;
		this.minSize = 0;
    }
    /**
     * Creates new Rectangle with minimum size 0
     *
     * @param topLeftL The line of the top left corner
     * @param topLeftC The column of the top left corner
     * @param length The length of the Rectangle
     * @param height The height of the Rectangle
     */
    public Rectangle(int topLeftL, int topLeftC, int length, int height) {
        this(topLeftL, topLeftC, length, height, 0);
    }

    /**
     * Creates new Rectangle attached to origin with minimum size 0
     *
     * @param length The length of the Rectangle
     * @param height The height of the Rectangle
     */
    public Rectangle(int length, int height)
    {
        this(0, 0, length, height, 0);
    }

    /**
     * Creates new Rectangle attached to origin
     *
     * @param length The length of the Rectangle
     * @param height The height of the Rectangle
     * @param minSize The size under which an IllegalArgumentException is throuwn
     */
    public Rectangle(int length, int height, int minSize)
    {
        this(0, 0, length, height, minSize);
    }

    /**
     * Creates new Rectangle based on top left corner Coord with minimum size 0
     *
     * @param topLeft The Coord of the top left corner
     * @param length The length of the Rectangle
     * @param height The height of the Rectangle
     */
    public Rectangle(Coord topLeft, int length, int height) 
    {
	    this(topLeft.getLine(), topLeft.getCol(), length, height, 0);
    }

    /**
     * Creates new Rectangle based on top left corner Coord
     *
     * @param topLeft The Coord of the top left corner
     * @param length The length of the Rectangle
     * @param height The height of the Rectangle
     * @param minSize The size under which an IllegalArgumentException is throuwn
     */
    public Rectangle(Coord topLeft, int length, int height, int minSize) 
    {
	    this(topLeft.getLine(), topLeft.getCol(), length, height, minSize);
    }

    /**
     * Creates new Rectangle based on another Rectangle
     *
     * @param rect The Rectangle to copy
     */
    public Rectangle(Rectangle rect) 
    {
	    this(rect.topLine(), 
	            rect.leftCol(), 
	            rect.getLength(), 
	            rect.getHeight(),
                rect.getMinSize());
    }

    /**
     * Creates new Rectangle based on top left corner and bottom right Coord 
     *
     * @param corner1 The Coord of the top left corner
     * @param corner2 The Coord of the top left corner
     * @param minSize The size under which an IllegalArgumentException is throuwn
     */
    public Rectangle(Coord corner1, Coord corner2, int minSize) 
    {
	    int corner1Line = corner1.getLine();
	    int corner1Col = corner1.getCol();
	    int corner2Line = corner2.getLine();
	    int corner2Col = corner2.getCol();
	    int topLine = corner1Line < corner2Line ? corner1Line : corner2Line;
	    int leftCol = corner1Col < corner2Col ? corner1Col : corner2Col;
	    topLeft = new Coord(topLine, leftCol);
	    length = Math.abs(corner2Col - corner1Col) + 1;
	    height = Math.abs(corner2Line - corner1Line) + 1;	
        this.minSize = minSize;
    }
    
    /**
     * Creates new Rectangle based on top left corner and bottom right Coord of minimum size 0
     *
     * @param corner1 The Coord of the top left corner
     * @param corner2 The Coord of the top left corner
     */
    public Rectangle(Coord corner1, Coord corner2)
    {
        this(corner1, corner2, 0);
    }

    /**
     * Creates empty Rectangle
     *
     * @return An empty rectangle
     */
    public static Rectangle empty() 
    {
	    return new Rectangle(0, 0, 0, 0, 0);
    }

    /**
     * Test if empty
     *
     * @return True if rectangle is empty
     */
    public boolean isEmpty() 
    {
	    return length == 0 && height == 0;
    }

    public int topLine() 
    {
	    return topLeft.getLine();
    }

    public int leftCol() 
    {
	    return topLeft.getCol();
    }

    public int bottomLine() 
    {
	    return topLine() + height - 1;
    }

    public int rightCol() 
    {
	    return leftCol() + length - 1;
    }

    public int getLength() 
    {
	    return length;
    }

    public int getHeight() 
    {
	    return height;
    }

    public int getMinSize()
    {
        return minSize;
    }

    /**
     * Creates an iterator on the squares of the array
     */
    public Iterator<Coord> iterator() {
	    return new Iterator<Coord>() {
	        private int top = topLine();
	        private int left = leftCol();
	        private int bottom = bottomLine();
	        private int right = rightCol();
	        private int row = top;
	        private int col = left;   

	        public boolean hasNext() {
		        // as long as we haven't finished with last row, we're not done
		        return (row <= bottom);
	        }

	        public Coord next() throws NoSuchElementException {
		        if (row > bottom) {
		            throw new NoSuchElementException();
		        }
		        Coord curr = new Coord(row, col);
		        if (col++ == right) { // just finished a row
		            // go to start of next row
		            row++;
		            col = left; // col always in [left, right]
		        }
		        return curr;
	        }

	        public void remove() {
		        throw new UnsupportedOperationException();
	        }
	    };
    }

    public String toString() {
	    return topLeft.toString() + " - length: " + length + " - height: " + height;
    }

}
