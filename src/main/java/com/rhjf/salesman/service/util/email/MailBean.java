package com.rhjf.salesman.service.util.email;

import java.util.Vector;

public class MailBean {
	Vector<String> to; // 收件人
	private String from; // 发件人
	Vector<String> copyColumn;	// 抄送人
	private String host; // SMTP主机
	private String username; // 发件人的用户名
	private String password; // 发件人的密码
	private String subject; // 邮件主题
	private String content; // 邮件正文
	Vector<String> file; // 多个附件
	private String filename; // 附件的文件名


	
	
	

	public Vector<String> getTo() {
		return to;
	}

	public void setTo(String toName) {
		if(to==null){
			to = new Vector<String>();
		}
		to.addElement(toName);
	}

	public Vector<String> getCopyColumn(){
		return copyColumn;
	}
	
	public void setCopyColumn(String ccName){
		if(copyColumn==null){
			copyColumn = new Vector<String>();
		}
		copyColumn.addElement(ccName);
	}
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Vector<String> getFile() {
		return file;
	}

	public void attachFile(String fileName) {
		if (file == null)
			file = new Vector<String>();
		file.addElement(fileName);
	}

	
}
