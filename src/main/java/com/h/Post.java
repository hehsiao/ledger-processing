package com.h;

public class Post {

	private int _id;
	private String _title;
	private String _body;
	private int _userId;

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public String getTitle() {
		return _title;
	}

	public void setTitle(String title) {
		this._title = title;
	}

	public String getBody() {
		return _body;
	}

	public void setBody(String body) {
		this._body = body;
	}

	public int getUserId() {
		return _userId;
	}

	public void setUserId(int userId) {
		this._userId = userId;
	}

	@Override
	public String toString() {
		return "Post " + _title + "\nBody: " + _body + "\nby " + _userId;
	}

}
