package com.weguard.websocket;

import java.io.IOException;
import java.util.HashMap;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import net.sf.json.JSONObject;

//该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@ServerEndpoint(value="/wuziqisocket") 
public class WebSocket {
  //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
  private static HashMap<String,WebSocket> webSocketMap = new HashMap<String,WebSocket>();
  //与某个客户端的连接会话，需要通过它来给客户端发送数据
  private Session session;
  //连上来的页面序号，用来配对对战，1与2一组，3与4一组，依次类推，奇数为黑先走，偶数为白，后走
  private static int index = 0;
  //同上，用来从hashMap中获取websocket，（我也忘记当时为啥要另外用一个mykey了，而不是直接用index来获取）
  private int mykey = 0;
   
  /**
   * 连接建立成功调用的方法
   * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
 * @throws IOException 
   */
  @OnOpen
  public void onOpen( Session session){
      this.session = session;
      index++;
      try {
    	  Result result = new Result();
    	  if(index%2==0){
        	  WebSocket socket1 = webSocketMap.get((index-1)+"");
        	  if(socket1!=null){
        		  result.setBout(true);
        		  result.setMessage("系统：游戏开始，请您先落子！");
        		  result.setColor("black");
        		  JSONObject json1 = JSONObject.fromObject(result);
        		  socket1.sendMessage(json1.toString());
        		  //对先落子的对象发送数据结束
        		  result.setMessage("系统：游戏开始，请等待对手落子！");
        		  result.setBout(false);
        		  result.setColor("white");
            	  this.sendMessage(JSONObject.fromObject(result).toString());
            	  //对后出手的发送消息结束
        	  }else{//偶数时没有查询到与之对应的对手，则其变为奇数，成为等待匹配的人
        		  index--;
        		  result.setMessage("系统：等待玩家匹配！");
        		  this.sendMessage(JSONObject.fromObject(result).toString());
        	  }
          }else{
        	  result.setMessage("系统：等待玩家匹配！");
    		  this.sendMessage(JSONObject.fromObject(result).toString());
          }
    	  this.mykey = index;
          webSocketMap.put(mykey+"", this);     //加入map中
          System.out.println(webSocketMap.size());
	} catch (Exception e) {
		e.printStackTrace();
	}
  }
   
  /**
   * 连接关闭调用的方法
 * @throws IOException 
   */
  @OnClose
  public void onClose(){
      webSocketMap.remove(mykey+"");  //从set中删除
      try {
    	  WebSocket socket = null;
    	  if(mykey%2==0){
        	  socket = webSocketMap.get((mykey-1)+"");
          }else{
        	  socket = webSocketMap.get((mykey+1)+"");
          }
    	  if(socket!=null){
    		  Result result = new Result();
    		  result.setMessage("你的对手已离开！");
    		  socket.sendMessage(JSONObject.fromObject(result).toString());
    	  }
	} catch (Exception e) {
		e.printStackTrace();
	}
  }
  /**
   * 收到客户端消息后调用的方法
   * @param message 客户端发送过来的消息
   *
   */
  @OnMessage
  public void onMessage(String message) {
	  System.out.println(message);
	  JSONObject json = JSONObject.fromObject(message);
	  Result result = (Result) JSONObject.toBean(json,Result.class);
      try {
    	  WebSocket socket = null;
    	  if(mykey%2==0){
        	  socket = webSocketMap.get((mykey-1)+"");
          }else{
        	  socket = webSocketMap.get((mykey+1)+"");
          }
    	  if(socket!=null){
    		  if(result.getXy()!=null&&!"".equals(result.getXy())){//有坐标表示为落子，反之则为发送信息
        		  this.sendMessage(message);
        		  result.setBout(true);//对手的bout改为true，表示接下来可以落子
        		  result.setMessage("系统：对方已落子，正在等待您落子！");
        		  socket.sendMessage(JSONObject.fromObject(result).toString());
        	  }else{//没有坐标表示为单纯的聊天
        		  Result newResult = new Result();
        		  newResult.setMessage("自己："+result.getMessage());
        		  this.sendMessage(JSONObject.fromObject(newResult).toString());
        		  newResult.setMessage("对方："+result.getMessage());
        		  socket.sendMessage(JSONObject.fromObject(newResult).toString());
        	  }
    	  }
    	  
    	  
	} catch (Exception e) {
		e.printStackTrace();
	}
  }
   
  /**
   * 发生错误时调用
   * @param session
   * @param error
   */
  @OnError
  public void onError(Session session, Throwable error){
      System.out.println("连接断开");
//      error.printStackTrace();
  }
  /**
   * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
   * @param message
   * @throws IOException
   */
  public void sendMessage(String message) throws IOException{
      this.session.getBasicRemote().sendText(message);
      //this.session.getAsyncRemote().sendText(message);
  }
}
