// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import common.*;
import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;
  private boolean sc = false;
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port) 
  {
    super(port);
  }

  
  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient
    (Object msg, ConnectionToClient client)
  {
    System.out.println("Message received: " + msg + " from " + client);
    try{
    	String idcommand = (String)(msg);
	String[] aidcommand = idcommand.split(" ");
	if(aidcommand[0].equals("#login")){
		if(client.getInfo("loginid") == null){
			String idname = aidcommand[1];
			client.setInfo("loginid",idname);
		}
		else{
		client.sendToClient("Error,wrong command!");
		client.close();
		}
	}
    }
    catch(Exception ex){
    }
    Object echmsg = (String)(client.getInfo("loginid")) + " " + (String)(msg);
    this.sendToAllClients(msg);
  }
    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
      ("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
  }
  
    protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
  }
  //hml
  protected void clientConnected(ConnectionToClient client) {
    System.out.println("A new client is attempting to connect to the server.");
    System.out.println(client + " has logged on.");
  }

  synchronized protected void clientDisconnected(ConnectionToClient client) {
    System.out.println(client + " has logged out.");
  }

  protected void serverClosed(){
    sc = true;
  }

  public class ServerConsole implements ChatIF{
    
    EchoServer server;
    
    public ServerConsole(EchoServer server){
      this.server = server;
    }

    public void accept(){
      try
      {
        BufferedReader fromConsole = 
          new BufferedReader(new InputStreamReader(System.in));
        String message;

        while (true){
          message = fromConsole.readLine();
          char messagecheck = message.charAt(0);
          if (messagecheck == '#'){
            String[] cmessage = message.split(" ");
            String command = cmessage[0];
            System.out.println(command);
            if (command.equals("#quit")){
              System.out.println("The server has quit");
              try
              {
                server.close();
              }
              catch(IOException e) {}
              System.exit(0);
            }
            else if (command.equals("#stop")){
              server.stopListening();
            }
            else if (command.equals("#close")){
              server.close();
            }
            else if (command.equals("#setport")){
              if (!server.sc){
                System.out.println("The server is still connected.");
              }
              else{
                int newport = Integer.parseInt(cmessage[1]);
                EchoServer nsv = new EchoServer(newport);
                ServerConsole newPortServer = new ServerConsole(nsv);
                newPortServer.accept();
                try
                {
                  nsv.listen();
                } 
                catch (Exception ex) 
                {
                  System.out.println("ERROR - Could not listen for clients!");
                }
              }
            }
            else if (command.equals("#start")){
                server.listen();
            }  
          }
          server.sendToAllClients("SERVER MSG> " + message);
        }
      }
      catch(Exception ex){

      }
    }
    public void display(String message) 
    {
      System.out.println("> " + message);
    }
  } 
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
  public static void main(String[] args) 
  {
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }
    EchoServer sv = new EchoServer(port);
    EchoServer.ServerConsole serverco = sv.new ServerConsole(sv);
    
    Thread listenmessage = new Thread (new Runnable(){
   	public void run(){
	try{
	sv.listen();
	}
	catch(Exception ex){
		System.out.println("Error - couldn't listen for clients!");
	}
	}
    });
    Thread sendmessage = new Thread (new Runnable(){
      public void run(){
        serverco.accept();
      }
    });
    listenmessage.start();
    sendmessage.start();    
  }
}
//End of EchoServer class
