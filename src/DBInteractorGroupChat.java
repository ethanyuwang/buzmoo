package buzmo;

import java.sql.*;
import java.io.*;
import java.net.*;
import java.lang.*;

public class DBInteractorGroupChat {
	//Used for GroupChatJPanel 
	public static Boolean createGroup(Connection con, String groupName){
		try { 
			Statement st = con.createStatement();
			int groupId = groupName.hashCode();
			if (groupId == 0)
			{
				System.out.println("groupID not found\n");
				return false;
			}
			// Create table
			String sql = "INSERT INTO Group_chats " +
			"VALUES (" + groupId + ", '" +
			groupName + "', " +
			7 + ", " +
			"'" + BuzmoJFrame.userEmail + "')";
			st.executeUpdate(sql);

			// link to group members
			sql = "INSERT INTO Group_chat_members " +
			"VALUES (" + groupId + ", '" +
			BuzmoJFrame.userEmail + "')";

			st.executeUpdate(sql);
			return true;
		}
		catch(Exception e){System.out.println(e); return false;}
}
	//Used for GroupChatJPanel 
	public static String getGroups(Connection con){
		try {
			String ret = "";
			String myEmail = BuzmoJFrame.userEmail;
			Statement st = con.createStatement();
			String sql = "SELECT C.group_id FROM Group_chat_members C " +
			"WHERE C.member='" + myEmail + "'";
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()){
				String groupName = getGroupName(con, rs.getInt(1));
				ret += groupName;
				ret += "\n";
			}
			return ret;
		}
		catch(Exception e){System.out.println(e); return "";}
}
	//Used for GroupChatJPanel 
	public static int getGroupID(Connection con, String groupName){
		try {
			int ret = 0;
			String myEmail = BuzmoJFrame.userEmail;
			Statement st = con.createStatement();
			String sql = "SELECT C.group_id FROM Group_chats C " +
			"WHERE C.group_name='" + groupName + "'";
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()){
				ret = rs.getInt(1);
			}
			return ret;

		}
		catch(Exception e){System.out.println(e); return 0;}
	}
	//Used for GroupChatJPanel 
	public static String getGroupName(Connection con, int groupId){
		try {
			String ret = "";
			String myEmail = BuzmoJFrame.userEmail;
			Statement st = con.createStatement();
			String sql = "SELECT C.group_name FROM Group_chats C " +
			"WHERE C.group_id='" + groupId + "'";
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()){
				ret = rs.getString(1);
			}
			return ret;

		}
		catch(Exception e){System.out.println(e); return "";}
	}

	//Used for GroupChatJPanel 
	public static Boolean changeGroupChatName(Connection con, String groupName, String newGroupName){
		try {
			String myEmail = BuzmoJFrame.userEmail;	
			Boolean isOwner = false;
			Statement st = con.createStatement();
			//check if the current use is the owner
			String sql = "SELECT C.owner FROM Group_chats C WHERE " +
			"C.group_name='" + groupName + "'"; 
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()){
				if (myEmail.equals(rs.getString(1)))
					isOwner = true;
			}

			if(isOwner == false) {
				System.out.println("changeGroupChatName failed at current user isnt the owner\n");
				return false;
			}

			sql = "UPDATE Group_chats SET group_name = '" +
			newGroupName + "' WHERE group_name = '"+
			groupName + "'"; 
			st.executeQuery(sql);			
			return true;
		}
		catch(Exception e){System.out.println(e); return false;}
}
	//Used for GroupChatJPanel 
	public static Boolean changeGroupChatDuration(Connection con, String groupName, String newDuration){
		try {
			String myEmail = BuzmoJFrame.userEmail;	
			Boolean isOwner = false;
			Statement st = con.createStatement();
			//check if the current use is the owner
			String sql = "SELECT C.owner FROM Group_chats C WHERE " +
			"C.group_name='" + groupName + "'"; 
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()){
				if (myEmail.equals(rs.getString(1)))
					isOwner = true;
			}

			if(isOwner == false) {
				System.out.println("changeGroupChatName failed at current user isnt the owner\n");
				return false;
			}

			sql = "UPDATE Group_chats SET duration = " +
			newDuration + " WHERE group_name = '"+
			groupName + "'"; 
			st.executeQuery(sql);			
			return true;
		}
		catch(Exception e){System.out.println(e); return false;}
}
	//Used for GroupChatJPanel 
	public static Boolean isGroup(Connection con, String groupName){
		try {
			String myEmail = BuzmoJFrame.userEmail;	
			Statement st = con.createStatement();
			int groupId = getGroupID(con, groupName);
			if (groupId == 0)
			{
				System.out.println("groupID not found\n");
				return false;
			}
			System.out.println("Group id is : "+groupId);
			String sql = "SELECT G.member FROM Group_chat_members G WHERE " +
			"(G.group_id=" + groupId + ")"; 
			ResultSet rs = st.executeQuery(sql);			
			while(rs.next()){
				if (myEmail.equals(rs.getString(1))){
					return true;
				}
			}
			return false;
		}
		catch(Exception e){System.out.println(e); return false;}
}
	public static int getGroupChatDuration(Connection con, String groupName){
		try {
			int ret=-10;
			String myEmail = BuzmoJFrame.userEmail;	
			Statement st = con.createStatement();
			String sql = "SELECT C.duration FROM Group_chats C WHERE " +
			"C.group_name='" + groupName + "'"; 
			ResultSet rs = st.executeQuery(sql);

			while(rs.next()){
				ret = rs.getInt(1);
			}
			return ret;
		}

		catch(Exception e){System.out.println(e); return -10;}
	}

	public static String getGroupChatDurationWrapper(Connection con, String groupName){
		int	duration = getGroupChatDuration(con, groupName);
		if (duration>0) {
			return Integer.toString(duration);
		}
		else {
			return "Error";
		}
	}

	//Used for GroupChatJPanel 
	public static Boolean addMessageToGroupChat(Connection con, String message, String groupName){
		try {
			String myEmail = BuzmoJFrame.userEmail;
			Timestamp ts = DBInteractor.getCurrentTimeStamp();
			String messageWithTime = message+ts.toString();
			int groupId = getGroupID(con, groupName);
			if (groupId == 0)
			{
				System.out.println("groupID not found\n");
				return false;
			}

			//Add a copy to sender
			String sql = "INSERT INTO MESSAGES VALUES (?,?,?,?,?,?,?,?)";	
			PreparedStatement ps = con.prepareStatement(sql);
			con.setAutoCommit(false);
			String messageWithTimeAndOwner = messageWithTime + myEmail;

			ps.setInt(1, messageWithTimeAndOwner.hashCode());
			ps.setString(2, message);
			ps.setTimestamp(3, ts);
			ps.setString(4, "group");
			ps.setString(5, myEmail);
			ps.setString(6, myEmail);
			ps.setString(7, myEmail);
			ps.setInt(8, groupId);
			ps.addBatch();
			ps.executeBatch();
			con.commit();
			return true;
		}
		catch(Exception e){System.out.println(e); return false;}
}

	//Used for GroupChatJPanel 
	public static String loadGroupChatHistory(Connection con, String groupName){
		try {
			String ret = "";
			String myEmail = BuzmoJFrame.userEmail;	
			int groupId = getGroupID(con, groupName);
			if (groupId == 0)
			{
				System.out.println("groupID not found\n");
				return "";
			}
			// delete messages that are older than duration
			if(cleanOldGroupChatHistory(con, groupName, groupId) == false){
				return "FAILED: automatic old message deletion";
			};
			// get group chat messages
			Statement st = con.createStatement();
			String sql = "SELECT M.text_string, M.sender, M.timestamp FROM MESSAGES M WHERE " +
			"M.type='group' AND M.group_id='" + groupId + 
			"' ORDER BY M.timestamp";
			ResultSet rs = st.executeQuery(sql);			
			while(rs.next()){
				ret += rs.getString(2) + " (";
				ret += rs.getString(3) + "): ";
				ret += rs.getString(1) + "\n";
			}
			return ret;
		}
		catch(Exception e){System.out.println(e); return "FAILED to load group chat messages";}
	}
	public static String loadUsersMessages(Connection con, String groupName){
		// with message_id -> used for delete selection
		try {
			String ret = "";
			String myEmail = BuzmoJFrame.userEmail;	
			int groupId = getGroupID(con, groupName);
			if (groupId == 0)
			{
				System.out.println("groupID not found\n");
				return "";
			}
			// get group chat messages
			Statement st = con.createStatement();
			String sql = "SELECT M.text_string, M.sender, M.timestamp, M.message_id FROM MESSAGES M WHERE " +
			"M.type='group' AND M.group_id='" + groupId + 
			"' AND M.owner='" + myEmail + "' ORDER BY M.timestamp";
			ResultSet rs = st.executeQuery(sql);			
			while(rs.next()){
				ret += "<message_id: " + rs.getString(4) + ">\n";
				ret += rs.getString(2) + " (";
				ret += rs.getString(3) + "): ";
				ret += rs.getString(1) + "\n";
			}
			return ret;
		}
		catch(Exception e){System.out.println(e); return "FAILED to load group chat messages";}
	} 
	// deletion
        public static boolean deleteGroupMessage(Connection con, String message_id_str){
                try {
                        int message_id = Integer.parseInt(message_id_str);
                        String myEmail = BuzmoJFrame.userEmail;
                        String sql = "DELETE FROM MESSAGES M WHERE M.message_id=?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setInt(1, message_id);
                        ps.executeUpdate();
                        return true;
                }
                catch(Exception e){System.out.println(e); return false;}
        }
	public static Boolean cleanOldGroupChatHistory(Connection con, String groupName, int groupId){
		try {
			int duration = getGroupChatDuration(con, groupName);
			Timestamp ts = DBInteractor.getCurrentTimeStamp();
			String myEmail = BuzmoJFrame.userEmail;	
			Statement st = con.createStatement();

			String sql = "SELECT M.timestamp, M.message_id FROM MESSAGES M " +
			"WHERE M.group_id=? ORDER BY M.timestamp";
			PreparedStatement ps = con.prepareStatement(sql);
			PreparedStatement ps2;
			ps.setInt(1, groupId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				if(compareTimestamps(duration, ts, rs.getTimestamp(1))){
					// delete old message
					sql = "DELETE FROM MESSAGES M WHERE M.message_id=?";
					ps2 = con.prepareStatement(sql);
					ps2.setInt(1, rs.getInt(2));
					ps.executeUpdate();
				}
			}
			return true;
		}
		catch(Exception e){System.out.println(e); return false;}
	}
	public static boolean compareTimestamps(int duration, Timestamp cur, Timestamp pre){
		// true: need to delete!
		long milisec_cur = cur.getTime();
		long milisec_pre = cur.getTime();
		long diff = milisec_cur - milisec_pre;
		long diffDays = diff / (24 * 60 * 60 * 1000);
		if(diffDays > duration){
			return true;
		}
		else{
			return false;
		}
	}


	//Used for GroupChatJPanel 
	public static Boolean inviteToGroupChat(Connection con, String groupName, String requestEmail){
		try {
			String senderEmail = BuzmoJFrame.userEmail;
			Statement st = con.createStatement();
			int groupId = getGroupID(con, groupName);
			if (groupId == 0)
			{
				System.out.println("groupID not found\n");
				return false;
			}
			int count;
			// check contact list
			String sql = "SELECT count(*) FROM CONTACT_LISTS C WHERE " +
			"C.owner='" + senderEmail + "' AND " + 
			"C.friend='" + requestEmail + "'";
			ResultSet rs = st.executeQuery(sql);
			if(rs.next()){
				count = rs.getInt(1);
				if(count < 0){
					System.out.println("inviteToGroupChat failed at no such contact\n");
					return false;
				}
			}
			else{
				System.out.println("inviteToGroupChat failed at contact not found\n");
				return false;
			}

			// check pending list
			sql = "SELECT count(*) FROM Group_pending_lists C WHERE " +
			"(C.pending_people='" + requestEmail + "' AND " +
			" C.group_id='" + groupId + "')"; 
			rs = st.executeQuery(sql);			
			if(rs.next()){
				count = rs.getInt(1);
				if(count > 0){
					System.out.println("inviteToGroupChat failed at multiple requests exist\n");
					return false;
				}
			}
			else{
				System.out.println("inviteToGroupChat failed at pending list not found\n");
				return false;
			}

			// add to table
			sql = "INSERT INTO Group_pending_lists " +
			"VALUES ('" + requestEmail + "', " +
			" '" + groupId + "')";
			st.executeUpdate(sql);
			return true;
		}
		catch(Exception e){System.out.println(e); return false;}
	}

	//Used for GroupChatJPanel 
	public static Boolean addGroupChat(Connection con, String groupName){
		try {
			int groupId = getGroupID(con, groupName);
			if (groupId == 0)
			{
				System.out.println("groupID not found\n");
				return false;
			}
			String receiverEmail = BuzmoJFrame.userEmail;
			Statement st = con.createStatement();
			// check pending list
			String sql = "SELECT count(*) FROM Group_pending_lists G WHERE " +
			"(G.pending_people='" + receiverEmail + "' AND " +
			" G.group_id='" + groupId + "')"; 
			ResultSet rs = st.executeQuery(sql);			
			if(rs.next()){
				int count = rs.getInt(1);
				if(count != 1){
					return false;
				}
			}
			// remove from pending list
			sql = "DELETE FROM Group_pending_lists G " +
			"WHERE G.pending_people='" + receiverEmail + "' " + 
			"AND G.group_id='" + groupId + "'";
			st.executeUpdate(sql);
			// add to GROUP MEMEBERS list
			sql = "INSERT INTO Group_chat_members " + 
			"VALUES ('" + groupId + "', " + 
			" '" + receiverEmail + "')";
			st.executeUpdate(sql);
			
			return true;
		}
		catch(Exception e){System.out.println(e); return false;}
	}
	//Used for GroupChatJPanel 
	public static String getGroupMembers(Connection con, String groupName){
		try {
			String ret = "";
			int groupId = getGroupID(con, groupName);
			if (groupId == 0)
			{
				System.out.println("groupID not found\n");
				return "";
			}
			String myEmail = BuzmoJFrame.userEmail;
			Statement st = con.createStatement();
			String sql = "SELECT G.member FROM Group_chat_members G WHERE " +
			"(G.group_id=" + groupId + ")"; 
			ResultSet rs = st.executeQuery(sql);			
			while(rs.next()){
				ret+=rs.getString(1);
				ret += "\n";
			}
			return ret;

		}
		catch(Exception e){System.out.println(e); return "";}
	}

	//Used for GroupChatJPanel 
	public static String getPendingGroupChatInvites(Connection con){
		try {
			String ret = "";
			String myEmail = BuzmoJFrame.userEmail;
			Statement st = con.createStatement();
			String sql = "SELECT G.group_id FROM Group_pending_lists G " +
			"WHERE G.pending_people='" + myEmail + "'";
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()){
				String groupName = getGroupName(con, rs.getInt(1));
				ret += groupName;
				ret += "\n";
			}
			return ret;
		}
		catch(Exception e){System.out.println(e); return "";}
	}
}
