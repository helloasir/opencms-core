/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsMail.java,v $
 * Date   : $Date: 2000/05/31 12:04:01 $
 * Version: $Revision: 1.6 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.mail.*;
import javax.activation.*;
import javax.mail.internet.*;	

import java.util.*;
import java.io.*;

/**
 * This class is used to send a mail, it uses Threads to send it.
 *
 * @author $Author: w.babachan $
 * @version $Name:  $ $Revision: 1.6 $ $Date: 2000/05/31 12:04:01 $
 * @see java.lang.Thread
 */
public class CmsMail extends Thread implements I_CmsLogChannels {
			
	// constants	
	private final String c_FROM;
	private final String[] c_TO;
	private final String c_MAILSERVER;
	private final String c_SUBJECT;
	private final String c_CONTENT;	
	private final String c_TYPE;
	private final A_CmsObject c_CMS;
    
    private String m_defaultSender = null;
	
	/**
	 * Constructor, that creates an Email Object.
	 * 
	 * @param cms Cms object.
	 * @param from Address of sender.
	 * @param to Address of recipient.
	 * @param subject Subject of email.
	 * @param content Content of email.
	 * @param type ContentType of email.
	 */	
	public CmsMail(A_CmsObject cms,String from, String[] to, String subject, String content, String type)
		throws CmsException{
		// check sender email address
		if (from==null) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
		}
		if (from.equals("")) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
		}
		if (from.indexOf("@")==-1 || from.indexOf(".")==-1) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address: " + from, CmsException.C_BAD_NAME);			
		}
		// check recipient email address
		Vector v=new Vector(to.length);
		for(int i=0;i<to.length;i++) {
			if (to[i]==null) {
				continue;
			}
			if (to[i].equals("")){
				continue;
			}
			if (to[i].indexOf("@")==-1 || to[i].indexOf(".")==-1) {
				throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email, Invalid recipient email address: " + to[i],CmsException.C_BAD_NAME);
			}
			v.addElement(to[i]);
		}
		String users[]=new String[v.size()];
		for(int i=0;i<v.size();i++) {
			users[i]=(String)v.elementAt(i);
		}
		if (users.length==0){
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown recipient email address.", CmsException.C_BAD_NAME);
		}
		c_FROM=from;
		c_TO=users;
		c_SUBJECT=(subject==null?"":subject);
		c_CONTENT=(content==null?"":content);
		CmsXmlWpConfigFile conf=new CmsXmlWpConfigFile(cms);		
		c_MAILSERVER=conf.getMailServer();		
		c_TYPE=type;
		c_CMS=cms;			
	}
	
	/**
	 * Constructor, that creates an Email Object.
	 * 
	 * @param cms Cms object
	 * @param from User object that contains the address of sender.
	 * @param to User object that contains the address of recipient.
	 * @param subject Subject of email.
	 * @param content Content of email.
	 * @param type ContentType of email.
	 */	
	public CmsMail(A_CmsObject cms,A_CmsUser from, A_CmsUser[] to, String subject, String content, String type)
		throws CmsException{
        // Get WORKPLACE.INI
        CmsXmlWpConfigFile conf=new CmsXmlWpConfigFile(cms);		

        // check sender email address
        String fromAddress = from.getEmail();
        if(fromAddress == null || fromAddress.equals("")) {
            fromAddress = conf.getDefaultMailSender();
        }

        if(fromAddress == null || fromAddress.equals("")) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
		}

        if (fromAddress.indexOf("@")==-1 || fromAddress.indexOf(".")==-1) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address: " + fromAddress, CmsException.C_BAD_NAME);
		}
		// check recipient email address
		Vector v=new Vector(to.length);
		for(int i=0;i<to.length;i++) {
			if (to[i].getEmail()==null) {
				continue;
			}
			if (to[i].getEmail().equals("")){
				continue;
			}
			if (to[i].getEmail().indexOf("@")==-1 || to[i].getEmail().indexOf(".")==-1) {
				throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email, Invalid recipient email address: " + to[i].getEmail(),CmsException.C_BAD_NAME);
			}
			v.addElement(to[i].getEmail());
		}
		String users[]=new String[v.size()];
		for(int i=0;i<v.size();i++) {
			users[i]=(String)v.elementAt(i);
		}
		if (users.length==0){
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown recipient email address.", CmsException.C_BAD_NAME);
		}
		c_TO=users;
		c_FROM=fromAddress;
		c_SUBJECT=(subject==null?"":subject);
		c_CONTENT=(content==null?"":content);
		c_MAILSERVER=conf.getMailServer();
		c_TYPE=type;
		c_CMS=cms;
	}
	
	
	/**
	 * Constructor, that creates an Email Object.
	 * 
	 * @param cms Cms object.
	 * @param from User object that contains the address of sender.
	 * @param to Group object that contains the address of recipient.
	 * @param subject Subject of email.
	 * @param content Content of email.	
	 * @param type ContentType of email.
	 */	
	public CmsMail(A_CmsObject cms,A_CmsUser from, A_CmsGroup to, String subject, String content, String type)
		throws CmsException{

        // Get WORKPLACE.INI
        CmsXmlWpConfigFile conf=new CmsXmlWpConfigFile(cms);		

        // check sender email address
        String fromAddress = from.getEmail();
        if(fromAddress == null || fromAddress.equals("")) {
            fromAddress = conf.getDefaultMailSender();
        }

        if(fromAddress == null || fromAddress.equals("")) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
		}

        if (fromAddress.indexOf("@")==-1 || fromAddress.indexOf(".")==-1) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address: " + fromAddress, CmsException.C_BAD_NAME);
		}
		// check recipient email address
		Vector vu=cms.getUsersOfGroup(to.getName());
		Vector v=new Vector(vu.size());
		for(int i=0;i<vu.size();i++) {
			String address=((A_CmsUser)vu.elementAt(i)).getEmail();
			if (address==null) {
				continue;
			}
			if (address.equals("")) {
				continue;
			}
			if (address.indexOf("@")==-1 || address.indexOf(".")==-1) {
				throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email, Invalid recipient email address: " + address,CmsException.C_BAD_NAME);
			}
			v.addElement(address);
		}
		String users[]=new String[v.size()];
		for(int i=0;i<v.size();i++) {
			users[i]=(String)v.elementAt(i);
		}
		if (users.length==0){
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown recipient email address.", CmsException.C_BAD_NAME);
		}
		c_TO=users;
		c_FROM=fromAddress;
		c_SUBJECT=(subject==null?"":subject);
		c_CONTENT=(content==null?"":content);
		c_MAILSERVER=conf.getMailServer();
		c_TYPE=type;
		c_CMS=cms;
	}
	
	
	/**
	 * This method starts sending an email.
	 */
	public void run() {
		// Send the mail
		// create some properties and get the default Session
		Properties props=System.getProperties();
		props.put("mail.smtp.host",c_MAILSERVER);		
		Session session=Session.getDefaultInstance(props,null);	
		MimeMessage msg=new MimeMessage(session);
		try {			
			InternetAddress[] to=new InternetAddress[c_TO.length];
			for(int i=0;i<c_TO.length;i++) {
				to[i]=new InternetAddress(c_TO[i]);
			}
			msg.setFrom(new InternetAddress(c_FROM));
			msg.setRecipients(Message.RecipientType.TO,to);
			msg.setSubject(c_SUBJECT,"ISO-8859-1");
						
			Enumeration enum=c_CMS.getRequestContext().getRequest().getFileNames();
			Vector v=new Vector();
			while(enum.hasMoreElements()) {
				v.addElement(enum.nextElement());
			}		
			int size=v.size();
			if (size!=0) {
				// create and fill the first message part
				MimeBodyPart mbp1=new MimeBodyPart();
				Multipart mp=new MimeMultipart();
				if (c_TYPE.equals("text/html")) {
					mbp1.setDataHandler(new DataHandler(new CmsByteArrayDataSource(c_CONTENT, c_TYPE)));
				} else {
					mbp1.setText(c_CONTENT,"ISO-8859-1");
				}
				mp.addBodyPart(mbp1);
					
				// create another message part					
				// attach the file to the message				
				//FileDataSource fds=new FileDataSource(c_PATH+c_FILE);
				//mbp2.setDataHandler(new DataHandler(fds));
					
				for(int i=0;i<size;i++) {
					String filename=(String)v.elementAt(i);
					System.err.println("Filename: "+filename);
					MimetypesFileTypeMap mimeTypeMap=new MimetypesFileTypeMap();
					String mimeType=mimeTypeMap.getContentType(filename);
					System.err.println("MimeType: "+mimeType);
					MimeBodyPart mbp=new MimeBodyPart();
					mbp.setDataHandler(new DataHandler(new CmsByteArrayDataSource(c_CMS.getRequestContext().getRequest().getFile(filename), mimeType)));
					mbp.setFileName(filename);
					mp.addBodyPart(mbp);
				}
				msg.setContent(mp);
			} else {
				if (c_TYPE.equals("text/html")) {
					msg.setDataHandler(new DataHandler(new CmsByteArrayDataSource(c_CONTENT, c_TYPE)));
				} else {
					msg.setContent(c_CONTENT,c_TYPE);
				}
			}
			msg.setSentDate(new Date());
			Transport.send(msg);
		} catch(Exception e) {
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_DEBUG, "Error in sending email:"+ e.getMessage());
            }
			
		}
		
	}    
}