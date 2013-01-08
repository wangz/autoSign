package bean;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 参考：http://blog.csdn.net/mxiaochi/article/details/7255508
 * @author wz 
 * 领取mobi mb 
 *
 */
public class MoofeelSign {
	private static String base = "http://www.moofeel.com/";
	private static String site = "http://www.moofeel.com/forum-96-1.html";
	 private static String loginUrl = "http://www.moofeel.com/member.php?mod=logging&action=login&loginsubmit=yes&infloat=yes";
	 private static String userName = "username";
	 private static String passWord = "password";
	 private String realName;
	 private String realpasswd;

	// The HttpClient is used in one session
	 private HttpResponse response;
	 @Autowired
	 private DefaultHttpClient httpclient;
	// 创建一个本地Cookie存储的实例  
	 private  CookieStore cookieStore = new BasicCookieStore();  
     //创建一个本地上下文信息  
	 private HttpContext localContext = new BasicHttpContext();  
     //在本地上下问中绑定一个本地存储  
	 public MoofeelSign(String realName,String realpasswd){
		 this.realName =realName;
		 this.realpasswd = realpasswd;
	 }
	 private void replyAndFetch(String siteUrl){
			 String formhash = null;
			 String form_action = null;
			 String fetch_mb = null;
			 Document doc = Jsoup.parse(getText(siteUrl));
			 //获取formhash
			 Elements inputs = doc.getElementsByTag("input");
			 Iterator it =inputs.iterator();
			 while(it.hasNext()){
				 Element e = (Element) it.next();
				 if(e.attr("name").equals("formhash")){
					 formhash = e.attr("value");
				 }
			 }
			//获取form action
			 Elements forms = doc.getElementsByTag("form");
			 it =forms.iterator();
			 while(it.hasNext()){
				 Element e = (Element) it.next();
				 if(e.attr("id").equals("fastpostform")){
					 form_action = e.attr("action");
				 }
			 }
			 //获取领取MB地址
			 Elements as = doc.getElementsByTag("a");
			 it =as.iterator();
			 while(it.hasNext()){
				 Element e = (Element) it.next();
				 Element img;
				 if(e.getElementsByTag("img").size()>0){
					 img=e.getElementsByTag("img").get(0);
					 if(img.attr("src")!=null&&img.attr("src").toString().indexOf("signin_reply")>0){
						 fetch_mb = e.attr("href");
					 }
				 }
			 }

			 String replay = base + form_action;
			 HttpPost httpost = new HttpPost(replay);
			 // 回复信息message
			 List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			 //下面标记部分需替换为真实账号密码
			 nvps.add(new BasicNameValuePair("message", "I Like this ~~"));
			 nvps.add(new BasicNameValuePair("formhash", formhash));
			 nvps.add(new BasicNameValuePair("subject", ""));
			 try {
				 httpost.setEntity(new UrlEncodedFormEntity(nvps, "gbk"));
				 //有此句反而不能登录，莫名其妙
//				 httpost.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:17.0) Gecko/20100101 Firefox/17.0");
				 httpost.addHeader("Referer", siteUrl);
				 
				 System.out.println(httpost.toString());
				 response = httpclient.execute(httpost,localContext);
				 System.out.println(response.toString());
				 int statuscode = response.getStatusLine().getStatusCode();
				 HttpEntity he = response.getEntity();
				 BufferedReader br = new BufferedReader(
						 new InputStreamReader(he.getContent(),"gbk")
						  );
				 String temp;
				 StringBuffer bs = new StringBuffer();
				 while((temp=br.readLine())!=null){
					 bs.append(temp);
				 }
				 System.out.println(bs.toString());
			 } catch (Exception e1) {
				 e1.printStackTrace();
			 }finally {
				 httpost.abort();
			 }
			 
			 //领取mb
			 getText(fetch_mb);
	 }
	 
	 private boolean login() {
	  HttpPost httpost = new HttpPost(loginUrl);
	  // All the parameters post to the web site
	  List<NameValuePair> nvps = new ArrayList<NameValuePair>();
	  //下面标记部分需替换为真实账号密码
	  nvps.add(new BasicNameValuePair(userName, realName));
	  nvps.add(new BasicNameValuePair(passWord, realpasswd));

	  httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
	  localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);  
//	  httpost.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:17.0) Gecko/20100101 Firefox/17.0");
	  try {
	   httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
	   response = httpclient.execute(httpost,localContext);
	  } catch (Exception e) {
	   e.printStackTrace();
	   return false;
	  } finally {
	   httpost.abort();
	  }
	  return true;
	 }

	 private String getText(String redirectLocation) {
	  HttpGet httpget = new HttpGet(redirectLocation);
	  // Create a response handler
	  ResponseHandler<String> responseHandler = new BasicResponseHandler();
	  String responseBody = "";
	  try {
	   responseBody = httpclient.execute(httpget, responseHandler,localContext);
	  } catch (Exception e) {
	   e.printStackTrace();
	   responseBody = null;
	  } finally {
	   httpget.abort();
//	   httpclient.getConnectionManager().shutdown();
	  }
	  return responseBody;
	 }

	 public void sign() throws InterruptedException {
		 String signUrl = null;
		 int count = 0;
		 if (login()) {
			 boolean isfind = false;
			 //找到最新一条，如果没有则等待一下继续找
			 while(!isfind){
				 System.out.println("第"+count++ + "次尝试");
				try {
					Document doc = Jsoup.parse(getText(site));
					 //找到所有class为new的，其中可有有提醒，要过滤一下
					 Elements tbodys = doc.getElementsByClass("new");
					 tbodys.append("<a href=\"http://www.baidu.com\"></a>");
					 Element e = null;
					 for (Element element : tbodys) {
						if(element.getElementsByTag("a").get(0).attr("class").equals("xst")){
							e = element.getElementsByTag("a").get(0);
						}
					}
					 if(e==null) continue;
					 String title = e.childNodes().get(0).toString();
					 Calendar calendar = Calendar.getInstance();
					 Pattern pattern = Pattern.compile(".*"+calendar.get(Calendar.YEAR)+".*"+(calendar.get(Calendar.MONTH)+1)+".*"+calendar.get(Calendar.DAY_OF_MONTH));
					 if(pattern.matcher(title).find()){
						 isfind = true; 
						 signUrl = e.attr("abs:href");
					 }
					 Thread.sleep(200);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			 }
			 //回复最新的签到帖子，并领取MB
			 this.replyAndFetch(signUrl);
		 }
		 httpclient.getConnectionManager().shutdown();
	 }
	 public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getRealpasswd() {
		return realpasswd;
	}
	public void setRealpasswd(String realpasswd) {
		this.realpasswd = realpasswd;
	}
	
	public void test(){
		System.out.println(this.realName + " " + this.realpasswd);
	}
	 public static void main(String[] args) throws Exception {
//		 Thread.sleep(1000*60*16);
		 MoofeelSign moofeel = new MoofeelSign("nightfight","nightfight");
		 moofeel.sign();
	 }
}
