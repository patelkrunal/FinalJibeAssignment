package controllers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Image;
import models.ImageComparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FlickrApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Krunal
 * Controller to handle Flicker related functions.
 */
public class JibeFlickrController extends Controller{
	public static String apiKey = "8550e8c54503c1293ac3916ce54ffc97";
	public static String apiSecret = "8a0275ae7a187f19";
	public static OAuthService service = new ServiceBuilder().provider(FlickrApi.class).apiKey(apiKey).apiSecret(apiSecret).callback("http://localhost:9000/jibeFlickr/auth").build();
	private static final String PROTECTED_RESOURCE_URL = "https://api.flickr.com/services/rest/";
	private static final String TAG="dctech";
	static Verifier verifier;
	static Token requestToken;
	static Token accessToken;
	/**
	 * Redirect User to authorization url to get oauth_verifier.
	 * @return 
	 */
	public static Result index() {
		//obtain request token 
		requestToken= service.getRequestToken();
		String authorizationUrl = service.getAuthorizationUrl(requestToken);
		return redirect(authorizationUrl);
	}
	
	public static Result auth() throws JSONException {
		Map<String,String[]> queryStrings = request().queryString();
		Set<String> keySet = queryStrings.keySet();
		Logger.info(Integer.toString(keySet.size()));
		if(keySet.contains("oauth_verifier"))
			verifier = new Verifier(queryStrings.get("oauth_verifier")[0]);
		else
		{
			//there is an error.  
			return ok("Some error please go to http://localhost:9000/jibeFlicker");
		}
		try{
			accessToken = service.getAccessToken(requestToken, verifier);
		}catch(Exception e)
		{
			return redirect("/jibeFlickr");
		}
		OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
		request.addQuerystringParameter("method", "flickr.photos.search");
		request.addQuerystringParameter("tags", TAG);
		//request.addQuerystringParameter("min_upload_date", "1420848000"); for last month there is no photos with this tag.
		/**
		 * url_s : 75 px small images for thumbnails.
		 * url_c : 800 px large image. 
		 */
		request.addQuerystringParameter("extras", "tags,views,url_s,url_c,date_upload");
	    service.signRequest(accessToken, request);
	    Response response = request.send();
	    JSONObject json = null;
	    try {
			 json = XML.toJSONObject(response.getBody());
		} catch (Exception e) {
			return ok("Data conversion error please go to http://localhost:9000/jibeFlicker");
		}
	    List<Image> imageList = createImageList(json);
		return ok(views.html.JibeFlickerView.render(imageList));
	}
	
	public static List<Image> createImageList(JSONObject jObject) throws JSONException
	{
		List<Image> imageList = new ArrayList<Image>();
		JSONArray j =null;
		try {
			j = jObject.getJSONObject("rsp").getJSONObject("photos").getJSONArray("photo");
		} catch (JSONException e) {
			// in case of data conversion error return fake sample data.
			imageList =new Image().fakeData();
			return imageList;
		}
		
		for(int i=0;i<j.length();i++)
	    {
			JSONObject temp = j.getJSONObject(i);
			imageList.add(new Image(temp.getString("url_c"),temp.getInt("views"),temp.getString("tags"),temp.getString("url_s")));
	    }
		//sorting images based on number of Views. 
		Collections.sort(imageList,new ImageComparator());
		return imageList;
	}
}
