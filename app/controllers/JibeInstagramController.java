package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import models.Image;
import models.ImageComparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Krunal
 * Instagram related functions handled by this Controller.
 */
public class JibeInstagramController extends Controller{
	private static String CLIEND_ID="b81e9971336f4d3dbe64606a545d9134";
	private static String CLIEND_SECRET = "5c4ac23a8c964a569ad271f46f677fd6";
	private static String REDIRECT_URL= "http://localhost:9000/jibeInstagram/auth";
	private static String ACCESS_TOKEN="";
	private static String TAG = "dctech";
	private static String INSTAGRAM_URL = "https://api.instagram.com/v1/tags/"+TAG+"/media/recent";
	private static String instagramAuthorizationUrl = "https://api.instagram.com/oauth/authorize/";
	
	/**
	 * controller for index page.
	 * It will redirect user to Authorizarion Url of Instagram for getting access.
	 * @return
	 */
	public static Result index(){
		String redirectUrl = instagramAuthorizationUrl+"?"
				+"client_id=b81e9971336f4d3dbe64606a545d9134"+"&"
				+"redirect_uri=http://localhost:9000/jibeInstagram/auth"+"&"
				+"response_type=code";
		return redirect(redirectUrl);
	}
	/**
	 * Fetches Json data and render imageList
	 * It's default redirect uri set in Instagram App.
	 * @return
	 */
	 public static Result auth() {
		Map<String,String[]> queryStringMap= request().queryString();
		if(queryStringMap.containsKey("code"))
		{
			WSRequestHolder holder = WS.url("https://api.instagram.com/oauth/access_token").setContentType("application/x-www-form-urlencoded");
			String headerQueryString = "client_id="+CLIEND_ID+"&"
					+"client_secret="+CLIEND_SECRET+"&"
					+"grant_type=authorization_code"+"&"
					+"redirect_uri="+REDIRECT_URL+"&"
					+"code="+queryStringMap.get("code")[0];
			//Logger.info(headerQueryString);
			//promise of JsonNode will contain access_token.
			Promise<JsonNode> jsonPromise= holder.post(headerQueryString).map(
			        new Function<WSResponse, JsonNode>() {
			            public JsonNode apply(WSResponse response) {
			                JsonNode json = response.asJson();
			                return json;
			            }
			        }
			);
			JsonNode node = jsonPromise.get(8000);
			
			//get access_token 
			try{
			ACCESS_TOKEN = node.get("access_token").textValue();
			}catch(Exception e)
			{
				//error handling 
				return ok("Error try again go to localhost:9000/jibeInstagram");
			}
			
			//we got access token 
			if(ACCESS_TOKEN.isEmpty())
			{
				return ok("Error getting access_token go to localhost:9000/jibeInstagram");
			}
			
			//get photos.
			JsonNode photoJsonNode=null;
			WSRequestHolder getPhotoRequestHolder = WS.url(INSTAGRAM_URL).setQueryParameter("access_token",ACCESS_TOKEN);
			Promise<JsonNode> photoJsonPromise= getPhotoRequestHolder.get().map(
			        new Function<WSResponse, JsonNode>() {
			            public JsonNode apply(WSResponse response) {
			                JsonNode json = response.asJson();
			                return json;
			            }
			        }
			);
			photoJsonNode = photoJsonPromise.get(8000);
			JSONObject jsonObject = new JSONObject(photoJsonNode.toString());
			List<Image> imageList = parseJsonCreateImageList(jsonObject);
			return ok(views.html.JibeInstagramView.render(imageList));
		}
		else
		{
			//if user denied access then it will have error: access_denied, error_reason: user_denied, error_description: The user denied your request 
			return ok("Please give access:  http://localhost:9000/jibeInstagram");
		}
		
	  }
	 
	 /**
	  * 
	  * @param jObject: takes Json Object 
	  * @return list of sorted images
	  * @throws JSONException
	  */
	  private static List<Image> parseJsonCreateImageList(JSONObject jObject) throws JSONException{
		  List<Image> imageList = new ArrayList<Image>();
		  JSONArray dataArray = jObject.getJSONArray("data");
		  for(int i =0;i<dataArray.length();i++)
		  {
			  JSONObject temp = dataArray.getJSONObject(i);
			  int comment = temp.getJSONObject("comments").getInt("count");
			  String tUrl = temp.getJSONObject("images").getJSONObject("thumbnail").getString("url");
			  String sUrl = temp.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
			  imageList.add(new Image(sUrl,comment,TAG,tUrl));
		  }
		  Collections.sort(imageList,new ImageComparator());
		  return imageList;
		  
	  }

	
}
