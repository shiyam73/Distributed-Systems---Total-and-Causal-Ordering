package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;

import android.os.AsyncTask;
import android.util.Log;
import edu.buffalo.cse.cse486586.groupmessenger.GroupMessengerActivity.ClientTask;

public class Sequencer implements Runnable {

	Message msg;
	
	
	public Sequencer(Message msg) {
		super();
		this.msg = msg;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		Message toBe = new Message(msg.msg_id,"order",GroupMessengerActivity.globalSequence);
		GroupMessengerActivity.globalSequence++;
		for(int i=0; i<GroupMessengerActivity.client_soc.length;i++)
    	{
    		Message socket = new Message(GroupMessengerActivity.client_soc[i]);
        	new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, toBe, socket);
    	}
	}
	
    public class ClientTask extends AsyncTask<Message, Void, Void> {

        @Override
        protected Void doInBackground(Message... msgs) {
            try {
                String remotePort = msgs[1].socket;
               System.out.println("inside sequencer"+remotePort);

                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort));
                
                Message msgToSend = msgs[0];
               // Log.v(TAG,"Sending message "+msgToSend+" "+remotePort);
               // PrintWriter out= new PrintWriter(socket.getOutputStream(),true);
//                	out.println(msgToSend);
                
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(msgToSend);
                out.flush();
                out.close();
                
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                
                socket.close();
            } catch (UnknownHostException e) {
               // Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
               // Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }

		public void executeOnExecutor(Executor serialExecutor, Message m,
				String string) {
			// TODO Auto-generated method stub
			
		}
    }
}
