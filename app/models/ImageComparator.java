package models;

import java.util.Comparator;

/**
 * @author Krunal
 * Comparator to sort ImageList according to number of comment
 */
public class ImageComparator implements Comparator<Image>{

	public int compare(Image arg0, Image arg1) {
		// TODO Auto-generated method stub
		if(arg0.comment<arg1.comment)
		{
			return 1;
		}
		else{
			return -1;
		}
	}
	
	
}
