package servlet;

import java.io.IOException;

import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import game.GameManager;

@ServerEndpoint(value = "/socket")
public class WebSocketHandler {
	GameManager gameManager = GameManager.getInstance();
	private Session session;
	public static int baseID = 1;
	@OnOpen
	public void onOpen(Session session){
		this.session = session;
		gameManager.addSession(this.session, baseID++);
	}
	@OnMessage
	public void onMessage(Session session, String message){
		gameManager.handleMessage(session, message);
		
	}/*
	@OnError
	public void onError (Session session, Throwable t){
		System.out.println(t.getStackTrace().toString());
	}
	
	*/
}
