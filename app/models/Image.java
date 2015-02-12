package models;

import java.util.ArrayList;
import java.util.List;

import play.db.ebean.Model;

/**
 * @author Krunal
 * Model to hold Image data.
 */
public class Image extends Model{
	public String url;
	public int comment;
	public String tag;
	public String thumbnailUrl;
	
	public Image(){}
	public Image(String standardUrl, int comment, String tag, String thumbnailUrl){
		this.url = standardUrl;
		this.comment=comment;
		this.tag=tag;
		this.thumbnailUrl=thumbnailUrl;
	}
	
	
	public List<Image> fakeData(){
		List<Image> imageList = new ArrayList<Image>();
		imageList.add(new Image("http://scontent-b.cdninstagram.com/hphotos-xfa1/t51.2885-15/e15/10946366_400688473437314_109180668_n.jpg",10,"dctech","http://scontent-b.cdninstagram.com/hphotos-xfa1/t51.2885-15/s150x150/e15/10946366_400688473437314_109180668_n.jpg"));
		imageList.add(new Image("http://scontent-a.cdninstagram.com/hphotos-xaf1/t51.2885-15/e15/10890940_854606034600942_702589693_n.jpg",4,"dctech","http://scontent-a.cdninstagram.com/hphotos-xaf1/t51.2885-15/s150x150/e15/10890940_854606034600942_702589693_n.jpg"));
		imageList.add(new Image("http://scontent-b.cdninstagram.com/hphotos-xap1/t51.2885-15/e15/1517028_715152281937219_1428080000_n.jpg",9,"dctech","http://scontent-b.cdninstagram.com/hphotos-xap1/t51.2885-15/s150x150/e15/1517028_715152281937219_1428080000_n.jpg"));
		return imageList;
	}
}