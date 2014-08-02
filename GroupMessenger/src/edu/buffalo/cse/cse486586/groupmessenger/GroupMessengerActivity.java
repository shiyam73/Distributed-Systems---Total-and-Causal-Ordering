package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

	private ServerSocket serverSocket;
	private  String avd_name;
	private  int avd_number;
	private boolean isSequencer=false;
	private final int recvPort = 10000;
	protected final static String[] client_soc= {"11108","11112","11116","11120","11124"};
	private final Handler uiHandle= new Handler();
	private String myPort=null;
	private  ConcurrentLinkedQueue<Message> holdBack= new ConcurrentLinkedQueue<Message>();
	private  Map<String,Message> msgTable = new HashMap<String,Message>();
	public static int globalSequence=0;
	private  int groupSequence=0;
	private int messageCount =0;
	private int[] clock = new int[5];
	public static final String KEY_FIELD = "key";
	public static final String VALUE_FIELD = "value";
	private ContentResolver contentResolver;
	private int[] receiverClock = new int[5];
	private Vector<Message> causalBuffer = new Vector<Message>();
	Queue<Message> messagePriorityQueue;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        final TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        final Button btn = (Button) findViewById(R.id.button4);
        
       messagePriorityQueue = new PriorityQueue<Message>(10, idComparator);
        
    	TelephonyManager telephone = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    	String portStr = telephone.getLine1Number().substring(telephone.getLine1Number().length() - 4);	
    	final String port = String.valueOf((Integer.parseInt(portStr) * 2));
    	
    	contentResolver = getContentResolver();
    	//Log.v("PORT",port+"");
    	myPort = port;
    	if (port.equalsIgnoreCase("11108")) {
    		
			avd_name = "avd0";
			avd_number = 0;
			
		} else if (port.equalsIgnoreCase("11112")) {
			avd_name = "avd1";
			avd_number = 1;
		    isSequencer = true;
		
		} else if (port.equalsIgnoreCase("11116")) {
			avd_name = "avd2";
			avd_number = 2;
		}
		else if (port.equals("11120")) {
			avd_name = "avd3";
			avd_number = 3;
		}
		else if (port.equals("11124")) {
			avd_name = "avd4";
			avd_number = 4;
		}
    	
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(recvPort);
		
        new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				sendMessage();
			}
		});
		
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    
  public void sendMessage() {
    	
    	EditText et= (EditText) findViewById(R.id.editText1);
    	final String msg= et.getText().toString();
    	++clock[avd_number];
    	String msg_id = avd_name+"|"+messageCount;
    	++messageCount;
    	Message m = new Message(msg_id,"normal",msg,clock,avd_number);
    	et.setText("");
    	for(int i=0; i<client_soc.length;i++)
    	{
    		Message socket = new Message(client_soc[i]);
        	new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, m, socket);
    	}
    	
    }
    
    
  
  private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

      @Override
      protected Void doInBackground(ServerSocket... sockets) {
          ServerSocket serverSocket = sockets[0];
          
          Socket client = new Socket();
          boolean running = true;
          BufferedReader read = null;
          String message= null;
          ObjectInputStream in =null;
      	  Message obj=null;
          /*
           * TODO: Fill in your server code that receives messages and passes them
           * to onProgressUpdate().
           */
          try {
	            
          while(running)
          {
          	
        	  client = serverSocket.accept();
        	  in =new ObjectInputStream(client.getInputStream());
        	  try {
        		  obj = (Message) in.readObject();
        	  } catch (ClassNotFoundException e) {
        		  Log.e("ClientServed", e.getMessage());
        	  }
        	  
        	  if(obj.type.equalsIgnoreCase("normal"))
      		{
      			msgTable.put(obj.msg_id, obj);
      			if(isSequencer )
      			{
      					Executors.newSingleThreadExecutor().execute(new Sequencer(obj));
      			}
      		}
      		else
      		{
      			messagePriorityQueue.add(obj);
      		}
      		if(messagePriorityQueue.size() > 0)
      			 publishProgress("Dummy");
        	  
        	 
          }
          } catch (IOException e) {
        	  // TODO Auto-generated catch block
        	  e.printStackTrace();
          }
          finally{
        	  if(client != null)
        	  {
        		  try {
        			  client.close();
        		  } catch (IOException e) {
        			  // TODO Auto-generated catch block
        			  e.printStackTrace();
        		  }

        	  }

        	  if(read != null)
        	  {
        		  try {
        			  read.close();
        		  } catch (IOException e) {
        			  // TODO Auto-generated catch block
        			  e.printStackTrace();
        		  }
        	  }
          	}
          
          	
          
          return null;
      }

      protected void onProgressUpdate(String... strings) {
    	  while (messagePriorityQueue.size() > 0) {
    			
    			Message front = messagePriorityQueue.peek();
    			if (front.sequence == groupSequence && msgTable.containsKey(front.msg_id)) {
    					final Message msg = msgTable.get(front.msg_id);		
    					msgTable.remove(front.msg_id);						
    					messagePriorityQueue.poll();
    					groupSequence++;
    					ContentValues cv = new ContentValues();
    					cv.put(GroupMessengerActivity.KEY_FIELD, Integer.toString(front.sequence));
    		            cv.put(GroupMessengerActivity.VALUE_FIELD, msg.msg);
    		            contentResolver.insert(GroupMessengerProvider.CONTENT_URI, cv);
    						 TextView textView = (TextView)findViewById(R.id.textView1);
    							textView.setMovementMethod(new ScrollingMovementMethod());
    							textView.append(msg.msg);
    							textView.append("\n");

    			} else {
    				break;
    			}

          return;
      }
  }
  }

public static Comparator<Message> idComparator = new Comparator<Message>(){

	@Override
	public int compare(Message c1, Message c2) {
        return (int) (c1.sequence - c2.sequence);
    }
};


    public class ClientTask extends AsyncTask<Message, Void, Void> {

        @Override
        protected Void doInBackground(Message... msgs) {
            try {
                String remotePort = msgs[1].socket;

                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort));
                
                Message msgToSend = msgs[0];
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(msgToSend);
                out.flush();
                out.close();
                
                
                 // TODO: Fill in your client code that sends out a message.
                 
                
                socket.close();
            } catch (UnknownHostException e) {
                Log.e("Client", "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e("Client", "ClientTask socket IOException");
            }

            return null;
        }

		public void executeOnExecutor(Executor serialExecutor, Message m,
				String string) {
			// TODO Auto-generated method stub
			
		}
    }
    
}
