package com.sinpo.xnfc.mail;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Email {
	// 发送邮件的服务器的IP和端口
	private String mailServerHost;
	private String mailServerPort = "25";

	// 邮件发送者的地址
	private String fromAddress;
	// 邮件接收者的地址
	private String toAddress;
	// 登陆邮件发送服务器的用户名和密码
	private String userName;
	private String password;
	// 是否需要身份验证
	private boolean validate = true;
	// 邮件主题
	private String subject;
	// 邮件的文本内容
	private String content;
	// 邮件附件的文件名
	private String attachFile;

	public Email(String mailServerHost, String toAddress,
				 boolean validate) {
		this(mailServerHost, "admin@amdin.eysin", toAddress, validate);
	}

	public Email(String mailServerHost, String fromAddress,
				 String toAddress, boolean validate) {
		this(mailServerHost, fromAddress, toAddress, null, null);
		this.validate = validate;
	}

	public Email(String mailServerHost, String fromAddress,
				 String toAddress, String userName, String password) {
		super();
		if (mailServerHost == null) {
			mailServerHost = getDomain(toAddress);
		}
		this.mailServerHost = mailServerHost;
		this.fromAddress = fromAddress;
		this.toAddress = toAddress;
		this.userName = userName;
		this.password = password;
	}

	/**
	 * 实名发送邮件，需要用户名密码
	 *
	 * @param smtp
	 *            服务器地址
	 * @param to
	 *            收件人
	 * @param from
	 *            发件人
	 * @param password
	 *            发件人邮箱密码
	 */
	public static Email create(String smtp, String to, String from,
							   String password) {
		return new Email(smtp, from, to, from, password);
	}

	/**
	 * 匿名邮件发送，需要获取邮箱domain地址；例如：qq.com->mx1.qq.com.
	 *
	 * @param to
	 * @param domain
	 * @return
	 */
	public static Email create(String to, String domain) {
		return new Email(domain, to, false);
	}

	/**
	 * 匿名邮件发送，只需要填写接收人
	 *
	 * @param to
	 * @return
	 */
	public static Email create(String to) {
		return new Email(null, to, false);
	}

	/**
	 * 获得邮件会话属性
	 */
	public Properties getProperties() {
		Properties p = new Properties();
		p.put("mail.smtp.host", this.mailServerHost);
		p.put("mail.smtp.port", this.mailServerPort);
		p.put("mail.smtp.auth", validate ? "true" : "false");
		return p;
	}

	public String getMailServerHost() {
		return mailServerHost;
	}

	public void setMailServerHost(String mailServerHost) {
		this.mailServerHost = mailServerHost;
	}

	public String getMailServerPort() {
		return mailServerPort;
	}

	public void setMailServerPort(String mailServerPort) {
		this.mailServerPort = mailServerPort;
	}

	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	public String getAttachFile() {
		return attachFile;
	}

	public void setAttachFile(String fileNames) {
		this.attachFile = fileNames;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public void setContent(String textContent) {
		this.content = textContent;
	}


	private String getDomain(String email) {
		String domain = email.substring(email.indexOf('@') + 1);
		return addr.get(domain);
	}

	/**
	 * 以文本格式发送邮件
	 *
	 *            待发送的邮件的信息
	 */
	public boolean sendTextMail() {
		Email o = this;
		// 判断是否需要身份认证
		PopupAuthenticator authenticator = null;
		Properties pro = getProperties();
		if (isValidate()) {
			// 如果需要身份认证，则创建一个密码验证器
			authenticator = new PopupAuthenticator(getUserName(),
					getPassword());
		}
		// 根据邮件会话属性和密码验证器构造一个发送邮件的session
		Session sendMailSession = Session
				.getDefaultInstance(pro, authenticator);
		try {
			// 根据session创建一个邮件消息
			Message mailMessage = new MimeMessage(sendMailSession);
			// 创建邮件发送者地址
			Address from = new InternetAddress(getFromAddress(),
					"Eysin");
			// 设置邮件消息的发送者
			mailMessage.setFrom(from);
			// 创建邮件的接收者地址，并设置到邮件消息中
			Address to = new InternetAddress(getToAddress(), "Eysin");
			mailMessage.setRecipient(Message.RecipientType.TO, to);
			// 设置邮件消息的主题
			mailMessage.setSubject(getSubject());
			// 设置邮件消息发送的时间
			mailMessage.setSentDate(new Date());
			// 设置邮件消息的主要内容
			String mailContent = getContent();
			// mailMessage.setText(mailContent);
			mailMessage.setContent(mailContent, "text/plain");
			// 设置附件
			String attachPath = getAttachFile();
			if (attachPath != null && !attachPath.equals("")) {
				mailMessage.setFileName(attachPath);
			}
			// 发送邮件
			Transport.send(mailMessage);
			return true;
		} catch (MessagingException ex) {
			ex.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 以HTML格式发送邮件
	 *
	 *            待发送的邮件信息
	 */
	public boolean sendHtmlMail() {
		// 判断是否需要身份认证
		PopupAuthenticator authenticator = null;
		Properties pro = getProperties();
		// 如果需要身份认证，则创建一个密码验证器
		if (isValidate()) {
			authenticator = new PopupAuthenticator(getUserName(),
					getPassword());
		}
		// 根据邮件会话属性和密码验证器构造一个发送邮件的session
		Session sendMailSession = Session
				.getDefaultInstance(pro, authenticator);
		try {
			// 根据session创建一个邮件消息
			Message mailMessage = new MimeMessage(sendMailSession);
			// 创建邮件发送者地址
			Address from = new InternetAddress(getFromAddress(),
					"Eysin");
			// 设置邮件消息的发送者
			mailMessage.setFrom(from);
			// 创建邮件的接收者地址，并设置到邮件消息中
			Address to = new InternetAddress(getToAddress(), "Eysin");
			// Message.RecipientType.TO属性表示接收者的类型为TO
			mailMessage.setRecipient(Message.RecipientType.TO, to);
			// 设置邮件消息的主题
			mailMessage.setSubject(getSubject());
			// 设置邮件消息发送的时间
			mailMessage.setSentDate(new Date());
			// MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象
			Multipart mainPart = new MimeMultipart();
			// 创建一个包含HTML内容的MimeBodyPart
			BodyPart html = new MimeBodyPart();
			// 设置HTML内容
			html.setContent(getContent(), "text/html; charset=utf-8");
			mainPart.addBodyPart(html);
			// 将MiniMultipart对象设置为邮件内容
			mailMessage.setContent(mainPart, "text/plain");
			// 设置附件
			String attachPath = getAttachFile();
			if (attachPath != null && !attachPath.equals("")) {
				mailMessage.setFileName(attachPath);
			}
			// 发送邮件
			Transport.send(mailMessage);
			return true;
		} catch (MessagingException ex) {
			ex.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	static class PopupAuthenticator extends Authenticator {
		private String userName;
		private String pwd;

		public PopupAuthenticator(String paramString1, String paramString2) {
			this.userName = paramString1;
			this.pwd = paramString2;
		}

		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(this.userName, this.pwd);
		}
	}

	//	class MailInfo implements Serializable {
	//
	//		/**
	//		 * 
	//		 */
	//		private static final long serialVersionUID = -7827855623211101370L;
	//		// 发送邮件的服务器的IP和端口
	//		private String mailServerHost;
	//		private String mailServerPort = "25";
	//
	//		// 邮件发送者的地址
	//		private String fromAddress;
	//		// 邮件接收者的地址
	//		private String toAddress;
	//		// 登陆邮件发送服务器的用户名和密码
	//		private String userName;
	//		private String password;
	//		// 是否需要身份验证
	//		private boolean validate = true;
	//		// 邮件主题
	//		private String subject;
	//		// 邮件的文本内容
	//		private String content;
	//		// 邮件附件的文件名
	//		private String attachFile;
	//
	//		public MailInfo(String mailServerHost, String toAddress,
	//				boolean validate) {
	//			this(mailServerHost, "admin@amdin.eysin", toAddress, validate);
	//		}
	//
	//		public MailInfo(String mailServerHost, String fromAddress,
	//				String toAddress, boolean validate) {
	//			this(mailServerHost, fromAddress, toAddress, null, null);
	//			this.validate = validate;
	//		}
	//
	//		public MailInfo(String mailServerHost, String fromAddress,
	//				String toAddress, String userName, String password) {
	//			super();
	//			if (mailServerHost == null) {
	//				mailServerHost = getDomain(toAddress);
	//			}
	//			this.mailServerHost = mailServerHost;
	//			this.fromAddress = fromAddress;
	//			this.toAddress = toAddress;
	//			this.userName = userName;
	//			this.password = password;
	//		}
	//
	//		/**
	//		 * 获得邮件会话属性
	//		 */
	//		public Properties getProperties() {
	//			Properties p = new Properties();
	//			p.put("mail.smtp.host", this.mailServerHost);
	//			p.put("mail.smtp.port", this.mailServerPort);
	//			p.put("mail.smtp.auth", validate ? "true" : "false");
	//			return p;
	//		}
	//
	//		public String getMailServerHost() {
	//			return mailServerHost;
	//		}
	//
	//		public void setMailServerHost(String mailServerHost) {
	//			this.mailServerHost = mailServerHost;
	//		}
	//
	//		public String getMailServerPort() {
	//			return mailServerPort;
	//		}
	//
	//		public void setMailServerPort(String mailServerPort) {
	//			this.mailServerPort = mailServerPort;
	//		}
	//
	//		public boolean isValidate() {
	//			return validate;
	//		}
	//
	//		public void setValidate(boolean validate) {
	//			this.validate = validate;
	//		}
	//
	//		public String getAttachFile() {
	//			return attachFile;
	//		}
	//
	//		public void setAttachFile(String fileNames) {
	//			this.attachFile = fileNames;
	//		}
	//
	//		public String getFromAddress() {
	//			return fromAddress;
	//		}
	//
	//		public void setFromAddress(String fromAddress) {
	//			this.fromAddress = fromAddress;
	//		}
	//
	//		public String getPassword() {
	//			return password;
	//		}
	//
	//		public void setPassword(String password) {
	//			this.password = password;
	//		}
	//
	//		public String getToAddress() {
	//			return toAddress;
	//		}
	//
	//		public void setToAddress(String toAddress) {
	//			this.toAddress = toAddress;
	//		}
	//
	//		public String getUserName() {
	//			return userName;
	//		}
	//
	//		public void setUserName(String userName) {
	//			this.userName = userName;
	//		}
	//
	//		public String getSubject() {
	//			return subject;
	//		}
	//
	//		public void setSubject(String subject) {
	//			this.subject = subject;
	//		}
	//
	//		public String getContent() {
	//			return content;
	//		}
	//
	//		public void setContent(String textContent) {
	//			this.content = textContent;
	//		}
	//	}

	private static Map<String, String> addr = new HashMap<String, String>();
	static {
		addr.put("foxmail.com", "mx1.qq.com.");
		addr.put("vip.qq.com", "mx3.qq.com.");
		addr.put("qq.com", "mx1.qq.com.");
		addr.put("163.com", "163mx00.mxmail.netease.com.");
		addr.put("126.com", "126mx02.mxmail.netease.com.");
		addr.put("yeah.net", "yeahmx00.mxmail.netease.com.");
		addr.put("vip.163.com", "vip163mx01.mxmail.netease.com.");
		addr.put("sina.com", "freemx1.sinamail.sina.com.cn.");
		addr.put("sohu.com", "sohumx1.sohu.com.");
		addr.put("yahoo.com.cn", "mx1.mail.aliyun.com.");
		addr.put("tom.com", "tommx1.tom.com.");
		addr.put("hotmail.com", "mx1.hotmail.com.");
		addr.put("gmail.com", "alt1.gmail-smtp-in.l.google.com.");
		addr.put("189.com", "mxbiz2.qq.com.");
		addr.put("189.cn", "mta-189.21cn.com.");
		addr.put("tianya.cn", "mx.tianya.cn.");
		addr.put("live.com", "mx3.hotmail.com.");
		addr.put("wo.com.cn", "womx.wo.com.cn.");
		addr.put("139.com", "mx1.mail.139.com.");
	}
}
