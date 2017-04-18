package com.helen.database;

import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.helen.commands.CommandData;

public class Pronouns {
	
	private static final Logger logger = Logger.getLogger(Pronouns.class);

	private static ArrayList<String> bannedNouns = new ArrayList<String>();
	
	static{
		//Just a couple examples.
		bannedNouns.add("apache");
		bannedNouns.add("helicopter");
		//More are added on the back end.
		for(Config c: Configs.getProperty("bannedNouns")){
			bannedNouns.add(c.getValue());
		}
	}
	
	public static String getPronouns(String user){
		try{
			StringBuilder str = new StringBuilder();
			CloseableStatement stmt = Connector.getStatement(Queries.getQuery("getPronouns"),user.toLowerCase());
			ResultSet rs = stmt.getResultSet();
			StringBuilder accepted = new StringBuilder();
			StringBuilder pronouns = new StringBuilder();
			if(rs != null){
				while (rs.next()){
					if(rs.getBoolean("accepted")){
						if(accepted.length() > 0){
							accepted.append(", ");
						}
						accepted.append(rs.getString("pronoun"));
					}else{
						if(pronouns.length() > 0){
							pronouns.append(", ");
						}
						pronouns.append(rs.getString("pronoun"));
					}
				}
				if(accepted.length() > 0 || pronouns.length() > 0){
					if(pronouns.length() > 0){
						str.append(user);
						str.append(" uses the following pronouns : ");
						str.append(pronouns.toString());
						str.append(";");
					}else{
						str.append(" I have no record of pronouns;");
					}
					if(accepted.length() > 0){
						str.append(" ");
						str.append(user);
						str.append(" accepts the following pronouns: ");
						str.append(accepted.toString());
					}else{
						str.append(" I have no record of accepted pronouns");
					}
					str.append(".");
				}else{
					str.append("I'm sorry, I don't have any record of pronouns for " + user);
				}
			}else{
				str.append("I'm sorry there was an error.  Please inform Dr Magnus.");
			}
			return str.toString();
		}catch (Exception e){
			logger.error("Error retreiving pronouns",e);
		}
		return "I'm sorry there was an error.  Please inform Dr Magnus.";
	}
	
	public static String insertPronouns(CommandData data){
		
		try{
			StringBuilder str = new StringBuilder();
			CloseableStatement stmt = Connector.getStatement(Queries.getQuery("establishPronoun"),data.getSender().toLowerCase(), data.getSplitMessage()[1].equalsIgnoreCase("accepted") ? true : false);
			ResultSet rs = stmt.execute();
			
			if(rs != null && rs.next()){
				int pronounID = rs.getInt("pronounID");
				int j = 1;
				if(data.getSplitMessage()[1].equalsIgnoreCase("accepted")){
					j = 2;
				}
				
				for(int i = j;i < data.getSplitMessage().length; i++){
					if(bannedNouns.contains(data.getSplitMessage()[i].trim().toLowerCase())){
						return "Your noun list contains a banned term: " + data.getSplitMessage()[i];
					}
				}
				
				for(int i = j;i < data.getSplitMessage().length; i++){
					
					CloseableStatement insertStatement = Connector.getStatement(Queries.getQuery("insertPronoun"),pronounID, data.getSplitMessage()[i]);
					insertStatement.executeUpdate();
					if(str.length() > 0){
						str.append(", ");
					}
					str.append(data.getSplitMessage()[i]);
				}
			}
			return "Inserted the following pronouns: " + str.toString() + " as " + (data.getSplitMessage()[1].equalsIgnoreCase("accepted") ? "accepted pronouns." : "pronouns");
		}catch (Exception e){
			logger.error("Error retreiving pronouns",e);
		}
		return "I'm sorry there was an error.  Please inform Dr Magnus.";
	}
	
	public static String clearPronouns(String username){
		try{
			CloseableStatement stmt = Connector.getStatement(Queries.getQuery("deleteNouns"), username.toLowerCase());
			stmt.executeUpdate();
			
			stmt = Connector.getStatement(Queries.getQuery("deleteNounRecord"), username.toLowerCase());
			stmt.executeUpdate();
			
			
			return "Deleted all pronoun records for " + username + ".";
		}catch (Exception e){
			logger.error("Error retreiving pronouns",e);
		}
		return "I'm sorry there was an error.  Please inform Dr Magnus.";
	}
	
	
	
}
