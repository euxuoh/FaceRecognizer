package com.houxue.facerec.bean;

/**
 * @Description: 用户信息类
 * @author Hou Xue
 * @Date 2015.4.20
 */
public class Person {
	
	private int userID;
	private String userName = null;
	private String password = null;
	
	public Person() {
		// TODO Auto-generated constructor stub
	}
	
	public void setUserID(int id) {
		userID = id;
	}
	
	public int getUserID() {
		return userID;
	}
	
	public void setName(String name) {
		userName = name;
	}
	
	public String getName() {
		return userName;
	}
	
	public void setPsw(String psw) {
		password = psw;
	}
	
	public String getPsw() {
		return password;
	}
}
