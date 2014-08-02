package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.Serializable;
import java.util.Arrays;

class Message implements Serializable {

	
	private static final long serialVersionUID = 1L;
	public String msg_id;
	public String msg;
	int avd_number;
	int clock[];
	int sequence;
	public String type;
	public String socket;
	
	Message(String msg_id,String type, String msg, int[] clock,int avd_num) {
		this.msg_id= msg_id;
		this.msg= msg;
		this.avd_number= avd_num;
		this.clock= Arrays.copyOf(clock, clock.length);
		this.type = type;
		
	}
	
	Message(String msg_id, String type, int seq) {
		this.msg_id= msg_id;
		this.type = type;
		this.sequence=seq;
		
	}
	
	Message(String socket)
	{
		this.socket = socket;
	}
}