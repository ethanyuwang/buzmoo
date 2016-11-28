package buzmo;

import java.sql.*;
import java.io.*;
import java.net.*;
import java.lang.*;

public class DBInteractorManager {

	public enum loadDBControl {
		none, addUser, addContacts, addPrivateMessages, addGroups, addCircle, addTopicWords, addManager, finish
	} 

	public static Boolean loadDB(Connection con){
		
		loadDBControl ldbc=loadDBControl.none;
		String database="resource/data_base_sampler.txt";
		//URL url=getClass().getResource(database);
		//File db = new File(url.getPath());
		File db = new File(database);

		try (BufferedReader br = new BufferedReader(new FileReader(db))){
			String line;
			line = br.readLine();

			while ((line = br.readLine())!=null){
				//decide what to do
				if (line.equals("Users"))
				{
					ldbc=loadDBControl.addUser;
					System.out.println("Start adding" + line);
					continue;
				}
				else if(line.equals("Friends"))
				{
					ldbc=loadDBControl.addContacts;
					System.out.println("Start adding" + line);
					continue;
				}
				else if(line.equals("Private_Messages"))
				{
					ldbc=loadDBControl.addPrivateMessages;
					System.out.println("Start adding" + line);
					continue;
				}
				else if(line.equals("Chat_groups"))
				{
					ldbc=loadDBControl.addGroups;
					System.out.println("Start adding" + line);
					continue;
				}
				else if(line.equals("Circle_feeds"))
				{
					ldbc=loadDBControl.addCircle;
					System.out.println("Start adding" + line);
					continue;
				}
				else if(line.equals("User_Topic_words"))
				{
					ldbc=loadDBControl.addTopicWords;
					System.out.println("Start adding" + line);
					continue;
				}
				else if(line.equals("Managers"))
				{
					ldbc=loadDBControl.addManager;
					System.out.println("Start adding" + line);
					continue;
				}
				else if(line.equals("Finish"))
				{
					ldbc=loadDBControl.finish;
					System.out.println("Start adding" + line);
					continue;
				}

				//do specific task
				switch(ldbc) {
					case addUser: {
						System.out.println("Adding user " + line);
						if (addUserWithString(con, line)==false)
							return false;
						break;
					}
					case addContacts: {
						System.out.println("Adding contacts " + line);
						if (addContactsCircle(con, line)==false)
							return false;
						break;
					}
					case addPrivateMessages: {
						System.out.println("Adding private messages " + line);
						if (addPrivateMessagesWithString(con, line)==false)
							return false;
						break;
					}
					case addGroups: {
						//if (addContactsCircle(con, line)==false)
						//	return false;
						break;
					}

					default: {
						break;
					}				
				}
			}
			if (ldbc==loadDBControl.finish)
				return true;
			else
				return false;
		}
		catch(Exception e){System.out.println(e); return false;}
	}

	public static Boolean addUserWithString(Connection con, String line){
		String[] informations = line.split(",");
		//System.out.println(informations[1]+ informations[2]+ informations[0]+ informations[3]+ informations[4]);
		return DBInteractor.addUser(con, informations[1], informations[2], informations[0], informations[3], informations[4]);
	}

	public static Boolean addContactsCircle(Connection con, String line){
		String[] contacts = line.split(",");
		//for (int i=0; i<contacts.length;i++)
		//	System.out.println("We are contacts: "+contacts[i]+"\n");

		for (int owner = 0; owner<(contacts.length-1); owner++) {
			for (int contact = owner+1; contact<(contacts.length); contact++) {
				if (addContactsDirectly(con, contacts[owner], contacts[contact])==false)
					return false;
			}
		}
		return true;
	}

	public static Boolean addPrivateMessagesWithString(Connection con, String line){
		String[] info = line.split(";");
		return(addMessageToPrivateChatWithMoreInfo(con, info[2], info[1], info[0], info[3]));
	}
	

	//debug and managing purpose, not normal program flow
	public static Boolean addContactsDirectly(Connection con, String owner, String contact){
		try {
			System.out.println("Adding contacts: "+owner+contact);
			Statement st = con.createStatement();
			// check pending list
			String acceptEmail = getEmialWithName(con, owner);
			String receiverEmail = getEmialWithName(con, contact);
			// add to contact list both ways
			String sql = "INSERT INTO CONTACT_LISTS " + 
			"VALUES ('" + acceptEmail + "', " + 
			" '" + receiverEmail + "')";
			st.executeUpdate(sql);
			sql = "INSERT INTO CONTACT_LISTS " + 
			"VALUES ('" + receiverEmail + "', " + 
			" '" + acceptEmail + "')";
			st.executeUpdate(sql);
			return true;
		}
		catch(Exception e){System.out.println(e); return false;}
	}



	public static Boolean addMessageToPrivateChatWithMoreInfo(Connection con, String message, String recipientEmail, String SenderEmail, String time){
		try {
			String myEmail = BuzmoJFrame.userEmail;
			Timestamp ts = parseTimeStamp(time);
			String messageWithTime = message+ts.toString();

			//Add a copy to sender
			String sql = "INSERT INTO MESSAGES VALUES (?,?,?,?,?,?,?,?)";	
			PreparedStatement ps = con.prepareStatement(sql);
			con.setAutoCommit(false);
			String messageWithTimeAndOwner = messageWithTime + myEmail;
			ps.setInt(1, messageWithTimeAndOwner.hashCode());
			ps.setString(2, message);
			ps.setTimestamp(3, ts);
			ps.setString(4, "private");
			ps.setString(5, myEmail);
			ps.setString(6, myEmail);
			ps.setString(7, recipientEmail);
			ps.setNull(8, java.sql.Types.INTEGER);
			ps.addBatch();
			ps.executeBatch();
			con.commit();

			//Add a copy to recipient
			ps = con.prepareStatement(sql);
			messageWithTimeAndOwner = messageWithTime + recipientEmail;
			ps.setInt(1, messageWithTimeAndOwner.hashCode());
			ps.setString(2, message);
			ps.setTimestamp(3, ts);
			ps.setString(4, "private");
			ps.setString(5, recipientEmail);
			ps.setString(6, myEmail);
			ps.setString(7, recipientEmail);
			ps.setNull(8, java.sql.Types.INTEGER);
			ps.addBatch();
			ps.executeBatch();
			con.commit();
			con.setAutoCommit(true);
			return true;
		}
		catch(Exception e){System.out.println(e); return false;}
	}

	//Used for GroupChatJPanel 
	public static String getEmialWithName(Connection con, String name){
		try {
			String ret ="";
			Statement st = con.createStatement();
			String sql = "SELECT U.email_address FROM Users U " +
			"WHERE U.name='" + name + "'";
			ResultSet rs = st.executeQuery(sql);
			if(rs.next()){
				ret = rs.getString(1);
			}
			return ret;
		}
		catch(Exception e){System.out.println(e); return "";}
	}

	public static Timestamp parseTimeStamp(String line) {
		String properFormat;

		String[] dateTime = line.split(", ");
		String[] date = dateTime[0].split(".");
		String[] time = dateTime[1].split(" ");

		String month = String.format("%02s", date[0]);
		String day = String.format("%02s", date[1]);

		properFormat = date[2]+"-"+month+"-"+day+" ";

		String[] exactTime = time[0].split(":");
		
		if (time[1].equals("PM"))
			exactTime[0]=String.valueOf(Integer.parseInt(exactTime[0])+12);

		properFormat += (exactTime[0]+":"+exactTime[1]+":0.0");

		System.out.println("Parsed timestamp is "+properFormat);
		java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(properFormat);
		//java.util.Date today = new java.util.Date();
		return timestamp;
	}

}
